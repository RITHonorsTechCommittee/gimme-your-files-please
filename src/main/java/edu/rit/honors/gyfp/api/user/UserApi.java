package edu.rit.honors.gyfp.api.user;

import java.util.List;

import javax.inject.Named;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.Nullable;
import com.google.api.server.spi.response.BadRequestException;
import com.google.api.server.spi.response.ForbiddenException;
import com.google.api.server.spi.response.NotFoundException;
import com.google.appengine.api.users.User;

import edu.rit.honors.gyfp.api.Constants;
import edu.rit.honors.gyfp.api.model.TransferRequest;

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
	public TransferRequest acceptRequest(@Named("request") long request, @Named("limit") @Nullable int limit, User user)
			throws BadRequestException, ForbiddenException, NotFoundException {
		return null;
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
	public TransferRequest getRequest(@Named("request") long request, User user)
			throws ForbiddenException, NotFoundException {
		return null;
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
	public void removeFilesFromList(List<String> ids, User user) {
		// TODO
	}
}
