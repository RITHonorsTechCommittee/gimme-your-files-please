package edu.rit.honors.gyfp.api.folder;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiMethod.HttpMethod;
import com.google.api.server.spi.config.Nullable;
import com.google.api.server.spi.response.BadRequestException;
import com.google.api.server.spi.response.ForbiddenException;
import com.google.api.server.spi.response.InternalServerErrorException;
import com.google.api.server.spi.response.NotFoundException;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.appengine.api.users.User;
import com.google.common.base.Strings;
import edu.rit.honors.gyfp.api.ApiUtil;
import edu.rit.honors.gyfp.api.Constants;
import edu.rit.honors.gyfp.api.model.FileUser;
import edu.rit.honors.gyfp.api.model.Folder;
import edu.rit.honors.gyfp.api.model.TransferRequest;
import edu.rit.honors.gyfp.util.OfyService;
import edu.rit.honors.gyfp.util.Utils;

import javax.inject.Named;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Api(
		name = "gyfp",
		version = "v1",
		scopes = {
				Constants.Scope.USER_EMAIL,
				Constants.Scope.DRIVE_METADATA_READONLY
		},
		clientIds = {
				Constants.Clients.WEB_CLIENT
		})
public class FolderApi {
	
	private static final Logger log = Logger.getLogger(FolderApi.class.getName()); 
	
	/**
	 * Gets a folder object which contains ownership information for all files
	 * contained under the requested fileid. This request is recursive and will
	 * take time for large requests.
	 * 
	 * @param id
	 *            The google fileid of the folder that will be inspected
	 * @param user
	 *            The user making the request. Required for authorization
	 *            purposes.
	 * @return  The folder, with all children loaded
	 * 
	 * @throws NotFoundException
	 *             If a folder with fileid id is not found
	 * @throws ForbiddenException
	 *             If the user is not the owner of the folder
	 * @throws BadRequestException
	 *             If the id is not a folder, or if the id is not provided
	 */
	@ApiMethod(name = "folders.get", httpMethod = HttpMethod.GET)
	public Folder getFolder(@Named("id") String id, User user,
							@Named("ignoreCache") @Nullable Boolean ignoreCache)
			throws NotFoundException, ForbiddenException, BadRequestException {
		
		if (Strings.isNullOrEmpty(id)) {
			throw new BadRequestException(String.format(Constants.Error.MISSING_PARAMETER, "id"));
		}
		
		if (user == null) {
			throw new ForbiddenException(Constants.Error.AUTH_REQUIRED);
		}
		
		Drive service = Utils.createDriveFromUser(user);
		
		File result;
		try {
			result = service.files().get(id).setFields(Constants.Field.KIND_OWNER).execute();
		} catch (IOException e) {
			log.severe(e.getMessage());
			log.log(Level.SEVERE, "Could not load file " + id, e);
			throw new NotFoundException(String.format(Constants.Error.LOAD_FOLDER_FOR_USER_FAIL, id, user.getNickname(), user.getEmail(), e.getMessage()), e);
		}
		
		if (Constants.MimeType.FOLDER.equals(result.getMimeType())) {
			boolean isOwner = false;
			for (com.google.api.services.drive.model.User driveUser : result.getOwners()) {
				if (driveUser.getIsAuthenticatedUser()) {
					isOwner = true;
					break;
				}
			}
			
			if (!isOwner) {
				StringBuilder message = new StringBuilder(Constants.Error.INCORRECT_FOLDER_USER);
				for (com.google.api.services.drive.model.User driveUser : result.getOwners()) {
					message.append(driveUser.getDisplayName());
					message.append(" (");
					message.append(driveUser.getEmailAddress());
					message.append(")\n");
				}
				
				throw new ForbiddenException(message.toString());
			}
		} else {
			throw new BadRequestException(String.format(Constants.Error.INVALID_MIME, result.getMimeType()));
		}
		
		// At this point we know that:
		//   1)  There is a file with the given id
		//   2)  The "file" is actually a folder
		//   3)  The currently logged in user is an owner of the folder
		//
		// This means we can safely begin attempting to load the contents of the folder.

		// Default to not ignoring the cache
		ignoreCache = ignoreCache == null ? false : ignoreCache;
		return Folder.fromGoogleId(id, user, ignoreCache);
	}

	/**
	 * Removes read permissions for all files in the given folder from the list
	 * of users specified.
	 * 
	 * @param folder
	 *            The id of the folder from which access will be revoked
	 * @param user
	 *            The user making the request. Required for authorization
	 *            purposes
	 * @param userId
	 *            The ID of the user who will have read permissions removed
	 * @throws NotFoundException
	 *             If a folder with fileid id is not found
	 * @throws ForbiddenException
	 *             If the user is not the owner of the folder
	 */
	@ApiMethod(name = "folders.revoke.reader", httpMethod = HttpMethod.POST)
	public void revokeReadPermission(@Named("folder") String folder, User user, @Named("userId") String userId)
			throws NotFoundException, ForbiddenException, BadRequestException, InternalServerErrorException {
		
		revokePermission(folder, user, userId, Constants.Role.READER);
	}

	/**
	 * Removes write permissions for all files in the given folder from the list
	 * of users specified.
	 * 
	 * @param folder
	 *            The id of the folder from which access will be revoked
	 * @param user
	 *            The user making the request. Required for authorization
	 *            purposes
	 * @param userId
	 *            The ID of the user who will have read permissions removed
	 * @throws NotFoundException
	 *             If a folder with fileid id is not found
	 * @throws ForbiddenException
	 *             If the user is not the owner of the folder
	 * @throws BadRequestException  If there are no users specified or no permissions were revoked
	 * 
	 */
	@ApiMethod(name = "folders.revoke.writer", httpMethod = HttpMethod.POST)
	public void revokeWritePermission(@Named("folder") String folder, User user, @Named("userId") String userId)
			throws NotFoundException, ForbiddenException, BadRequestException, InternalServerErrorException {
		
		revokePermission(folder, user, userId, Constants.Role.WRITER);
	}
	
	/**
	 * @param folder
	 *            The id of the folder from which access will be revoked
	 * @param user
	 *            The user making the request. Required for authorization
	 *            purposes
	 * @param userId
	 *            The ID of the user who will have read permissions removed
	 * @param role
	 * 			  The role (read or
	 * @throws ForbiddenException
	 * @throws BadRequestException
	 */
	private void revokePermission(String folder, User user, String userId, String role)
			throws ForbiddenException, BadRequestException, InternalServerErrorException {
		if (user == null) {
			throw new ForbiddenException(Constants.Error.AUTH_REQUIRED);
		}
		
		if (userId == null || userId.length() == 0) {
			throw new BadRequestException(String.format(Constants.Error.MISSING_PARAMETER, "userId"));
		}
		
		Folder target = Folder.fromGoogleId(folder, user);
		
		Drive service = Utils.createDriveFromUser(user);
		FileUser targetUser = target.getUser(userId);

		SimplePermissionDeletionExecutor executor = new SimplePermissionDeletionExecutor(service, targetUser);

		ApiUtil.safeExecuteDriveRequestQueue(targetUser.getFiles(role), executor, 50);

		OfyService.ofy().save().entity(folder).now();
	}

	/**
	 * Creates "polite" transfer requests for all the specified users which will
	 * transfer any files owned in the given folder to the requesting user
	 * 
	 * @param folderid
	 *            The id of folder for which the transfer requests will be
	 *            created
	 * @param user
	 *            The user making the request. Required for authorization
	 *            purposes
	 * @param users
	 *            A list of users who will have transfer requests made for their
	 *            files
	 * @return A list of TransferRequests, one for each user who owned files in
	 *         the folder.
	 * @throws NotFoundException
	 *             If a folder with fileid id is not found
	 * @throws ForbiddenException
	 *             If the user is not the owner of the folder
	 * @throws BadRequestException
	 *             If no users are specified
	 */
	@ApiMethod(name = "folders.transfer.polite", httpMethod = HttpMethod.POST)
	public List<TransferRequest> makeTransferRequest(@Named("folder") String folderid, User user, @Named("users") List<String> users) 
			throws NotFoundException, ForbiddenException, BadRequestException {

		return getTransferRequests(folderid, user, users, false);
	}
	
	/**
	 * Creates "hostile" transfer requests for all the specified users which
	 * will transfer any files owned in the given folder to the requesting user.
	 * 
	 * Unlike the "polite" transfer requests, this API call will actually
	 * forcefully transfer ownership of all files to the requesting user
	 * 
	 * @param folderid
	 *            The id of the folder for which the transfer requests will be created
	 * @param user
	 *            The user making the request. Required for authorization
	 *            purposes
	 * @param users
	 *            A list of users who will have transfer requests made for their
	 *            files
	 * @return A list of TransferRequests, one for each user who owned files in
	 *         the folder.
	 * @throws NotFoundException
	 *             If a folder with fileid id is not found
	 * @throws ForbiddenException
	 *             If the user is not the owner of the folder
	 * @throws BadRequestException  If no users are specified
	 */
	@ApiMethod(name = "folders.transfer.hostile", httpMethod = HttpMethod.POST)
	public List<TransferRequest> makeHostileTransferRequest(@Named("folder") String folderid, User user,@Named("users") List<String> users) 
			throws NotFoundException, ForbiddenException, BadRequestException {

		return getTransferRequests(folderid, user, users, true);
	}

	private List<TransferRequest> getTransferRequests(String folderid, User user, List<String> users, boolean isForced) throws ForbiddenException, BadRequestException {
		if (user == null) {
			throw new ForbiddenException(Constants.Error.AUTH_REQUIRED);
		}

		if (users == null || users.isEmpty()) {
			throw new BadRequestException(String.format(Constants.Error.MISSING_PARAMETER, "users"));
		}

		Folder folder = Folder.fromGoogleId(folderid, user);
		List<TransferRequest> requests = new ArrayList<>();

		for (String userId : users) {
			TransferRequest request = TransferRequest.fromFolder(folder, user, userId);
			request.setIsForced(isForced);
			requests.add(request);
		}
		return requests;
	}

	/**
	 * Converts existing polite transfer requests into hostile requests and executes them.
	 * 
	 * @param requests
	 *            A list of all the ids of the transfer requests that will be 
	 *            force-completed
	 * @param user
	 *            The user making the request. Required for authorization
	 *            purposes
	 * @return A list of the modified TransferRequests
	 * @throws ForbiddenException
	 *             If the user is not the owner of the folder
	 */
	@ApiMethod(name = "folders.transfer.convert", httpMethod = HttpMethod.PUT)
	public List<TransferRequest> convertTransferRequest(@Named("requests") List<Long> requests, User user) 
			throws ForbiddenException {
		// TODO
		return new ArrayList<>();
	}
	
	
	/**
	 * Gets a list of the pending transfer requests for a given folder
	 * 
	 * @param folder
	 *            The id of the folder for which transfer requests will be listed
	 * @param user
	 *            The user making the request. Required for authorization
	 *            purposes.
	 * @return  The folder, with all children loaded
	 * @throws NotFoundException
	 *             If a folder with the fiven id is not found
	 * @throws ForbiddenException
	 *             If the user is not the owner of the folder
	 */
	@ApiMethod(name = "folders.transfer.list", httpMethod = HttpMethod.GET)
	public List<TransferRequest> getTransferRequests(@Named("folder") Long folder, User user) 
			throws NotFoundException, ForbiddenException {
		// TODO
		return new ArrayList<>();
	}
	
	
}
