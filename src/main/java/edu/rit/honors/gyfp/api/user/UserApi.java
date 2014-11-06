package edu.rit.honors.gyfp.api.user;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Named;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.Nullable;
import com.google.api.server.spi.response.BadRequestException;
import com.google.api.server.spi.response.ForbiddenException;
import com.google.api.server.spi.response.InternalServerErrorException;
import com.google.api.server.spi.response.NotFoundException;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.Permission;
import com.google.appengine.api.users.User;
import com.google.apphosting.api.ApiProxy;
import com.googlecode.objectify.ObjectifyService;

import edu.rit.honors.gyfp.api.Constants;
import edu.rit.honors.gyfp.api.model.TransferRequest;
import edu.rit.honors.gyfp.api.model.TransferableFile;
import edu.rit.honors.gyfp.util.OfyService;
import edu.rit.honors.gyfp.util.Utils;

@Api(
		name = "gyfp", 
		version = "v1", 
		scopes = { 
				Constants.Scope.USER_EMAIL,
				Constants.Scope.DRIVE_METADATA_READONLY
		})
public class UserApi {

	/**
	 * Incrementally completes a transfer request.
	 * 
	 * Google drive has an API rate limit of 10 requests per second, and a
	 * maximum request duration of 60 seconds. At the theoretical maximum, 600
	 * requests could be completed in this time span, however in practice this
	 * value is likely much lower. As such, an incremental approach is required
	 * to allow for such large requests. The returned TransferRequest will
	 * contain the updated state after all requests that took place this session
	 * have completed.
	 * 
	 * The limit parameter can be used to help implement a progress bar, as the
	 * total number of requests is known and the number of requests per call is
	 * configurable. To get a higher precision, use a smaller limit.
	 * 
	 * @param request
	 *            The id of the transfer request
	 * @param limit
	 *            The maximum number of files to transfer during this request.
	 *            This is required to ensure we do not exceed the rate limit
	 *            imposed by the Drive API. If not specified, files will be
	 *            transferred until the request is forced to end.
	 * @param user
	 *            The user who is completing the transfer request
	 * @return the current state of the transfer request, with successfully
	 *         transfered files removed.
	 * @throws BadRequestException
	 *             If the requested limit is greater than 600
	 * 
	 * @throws ForbiddenException
	 *             If the user is not authorized to complete this transfer
	 *             request
	 * @throws NotFoundException
	 *             If the transfer request cannot be found
	 */
	public TransferRequest acceptRequest(@Named("request") long requestId, @Named("limit") @Nullable Integer limit, User user)
			throws BadRequestException, ForbiddenException, NotFoundException, InternalServerErrorException {
		
		TransferRequest request = getRequest(requestId, user);
		if (limit == null) {
			limit = 600;
		} else if (limit > 600 || limit <= 0) {
			throw new BadRequestException(String.format(Constants.Error.INVALID_TRANSFER_LIMIT, limit, 0, 600));
		}
		
		Drive service = Utils.createDriveFromUser(user);
		List<TransferableFile> success = new ArrayList<>();
		Permission owner = new Permission();
		owner.setRole("owner");
			
		for (TransferableFile file : request.getFiles()) {
			try {
				service.permissions().update(file.getFileId(), request.getRequestingUser(), owner).execute();
				success.add(file);
				limit--;
				// Maybe not necessary, but just to be safe slow ourselves down slightly to avoid hitting the rate limit
				Thread.sleep(50); 
			} catch (IOException e) {
				throw new InternalServerErrorException(
						Constants.Error.FAILED_DRIVE_REQUEST, e);
			} catch (InterruptedException e) {
				throw new InternalServerErrorException(
						Constants.Error.SLEEP_INTERRUPTED, e);
			}

			// Ensure that we have enough time to remove the processed files,
			// store the updated request, and return the result
			if (ApiProxy.getCurrentEnvironment().getRemainingMillis() <= 2000 || limit == 0) {
				break;
			}
		}
		
		request.getFiles().removeAll(success);
		ObjectifyService.ofy().save().entity(request);
		return request;
	}

	
	/**
	 * Returns details about the given transfer request, including a list of
	 * included files.
	 * 
	 * @param request
	 *            The ID of the request
	 * @param user
	 *            The user who is completing the transfer request
	 * @return The transfer request
	 * @throws ForbiddenException
	 *             If the user does not own this request
	 * @throws NotFoundException
	 *             If the request cannot be found
	 */
	public TransferRequest getRequest(@Named("request") long requestId, User user)
			throws ForbiddenException, NotFoundException {
		if (user == null) {
			throw new ForbiddenException(Constants.Error.AUTH_REQUIRED);
		}
		TransferRequest request = OfyService.ofy().load().type(TransferRequest.class).id(requestId).now();
		
		if (request == null) {
			throw new NotFoundException(String.format(Constants.Error.TRANSFER_REQUEST_NOT_FOUND, requestId));
		}
		
		if (request.getTargetUser().equals(user.getUserId())) {
			return request;
		} else {
			throw new ForbiddenException(Constants.Error.TRANSFER_REQUEST_INCORRECT_USER);
		}
	}

	/**
	 * Allows users to selectively exclude files from the transfer request
	 * 
	 * @param ids  The ids of the files that should not be included in the request
	 * @param user  The user who is completing the transfer request
	 * @throws ForbiddenException
	 *             If the user does not own this request
	 * @throws NotFoundException
	 *             If the request cannot be found
	 */
	public void removeFilesFromList(@Named("ids") List<String> ids, User user) {
		// TODO
	}
}