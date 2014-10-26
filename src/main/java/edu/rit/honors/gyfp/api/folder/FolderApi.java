package edu.rit.honors.gyfp.api.folder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Named;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiMethod.HttpMethod;
import com.google.api.server.spi.response.BadRequestException;
import com.google.api.server.spi.response.ForbiddenException;
import com.google.api.server.spi.response.NotFoundException;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.appengine.api.users.User;
import com.google.common.base.Strings;

import edu.rit.honors.gyfp.api.Constants;
import edu.rit.honors.gyfp.api.model.Folder;
import edu.rit.honors.gyfp.api.model.TransferRequest;
import edu.rit.honors.gyfp.util.Utils;

@Api(name = "gyfp", version = "v1", scopes = {Constants.Scope.USER_EMAIL, Constants.Scope.DRIVE_METADATA_READONLY})
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
	public Folder getFolder(@Named("id") String id, User user) throws NotFoundException, ForbiddenException, BadRequestException {
		
		if (Strings.isNullOrEmpty(id)) {
			throw new BadRequestException("Request did not contain a folderid");
		}
		
		if (user == null) {
			throw new ForbiddenException("You must authenticate to use this API.");
		}
		
		Drive service = Utils.createDriveFromUser(user);
		
		File result = null;
		try {
			result = service.files().get(id).setFields(Constants.Field.KIND_OWNER).execute();
		} catch (IOException e) {
			log.severe(e.getMessage());
			log.log(Level.SEVERE, "Could not load file " + id, e);
			throw new NotFoundException("Could not load file with id " + id + " for user " + user.getUserId() + " (" + user.getEmail() + ")\n" + e.getMessage(), e);
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
				StringBuilder message = new StringBuilder("You are not the owner of this folder.  Only the following users are allowed to manage permissions of this folder:\n");
				for (com.google.api.services.drive.model.User driveUser : result.getOwners()) {
					message.append(driveUser.getDisplayName());
					message.append(" (");
					message.append(driveUser.getEmailAddress());
					message.append(")\n");
				}
				
				throw new ForbiddenException(message.toString());
			}
		} else {
			throw new BadRequestException("The given fileid is not a folder.  It has MIME type " + result.getMimeType());
		}
		
		// At this point we know that:
		//   1)  There is a file with the given id
		//   2)  The "file" is actually a folder
		//   3)  The currently logged in user is an owner of the folder
		//
		// This means we can safely begin attempting to load the contents of the folder.
		
		return Folder.fromGoogleId(id, service);
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
	 * @param users
	 *            A list of users who will have their read access revoked.
	 * @throws NotFoundException
	 *             If a folder with fileid id is not found
	 * @throws ForbiddenException
	 *             If the user is not the owner of the folder
	 */
	@ApiMethod(name = "folders.revoke.read", httpMethod = HttpMethod.POST)
	public void revokeReadPermission(@Named("folder") Long folder, User user, @Named("users") List<String> users) 
			throws NotFoundException, ForbiddenException {
		// TODO
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
	 * @param users
	 *            A list of users who will have their write access revoked.
	 * @throws NotFoundException
	 *             If a folder with fileid id is not found
	 * @throws ForbiddenException
	 *             If the user is not the owner of the folder
	 */
	@ApiMethod(name = "folders.revoke.write", httpMethod = HttpMethod.POST)
	public void revokeWritePermission(@Named("folder") Long folder, User user, @Named("users") List<String> users) 
			throws NotFoundException, ForbiddenException {
		// TODO
	}
	
	/**
	 * Creates "polite" transfer requests for all the specified users which will
	 * transfer any files owned in the given folder to the requesting user
	 * 
	 * @param folder
	 *            The id of folder for which the transfer requests will be created
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
	 */
	@ApiMethod(name = "folders.tranfer.polite", httpMethod = HttpMethod.POST)
	public List<TransferRequest> makeTransferRequest(@Named("folder") Long folder, User user, @Named("users") List<String> users) 
			throws NotFoundException, ForbiddenException {
		// TODO
		return new ArrayList<>();
	}
	
	/**
	 * Creates "hostile" transfer requests for all the specified users which
	 * will transfer any files owned in the given folder to the requesting user.
	 * 
	 * Unlike the "polite" transfer requests, this API call will actually
	 * forcefully transfer ownership of all files to the requesting user
	 * 
	 * @param folder
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
	 */
	@ApiMethod(name = "folders.tranfer.hostile", httpMethod = HttpMethod.POST)
	public List<TransferRequest> makeHostileTransferRequest(@Named("folder") Long folder, User user,@Named("users") List<String> users) 
			throws NotFoundException, ForbiddenException {
		// TODO
		return new ArrayList<>();
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
	@ApiMethod(name = "folders.tranfer.convert", httpMethod = HttpMethod.PUT)
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
	@ApiMethod(name = "folders.tranfer.list", httpMethod = HttpMethod.GET)
	public List<TransferRequest> getTransferRequests(@Named("folder") Long folder, User user) 
			throws NotFoundException, ForbiddenException {
		// TODO
		return new ArrayList<>();
	}
	
	
}
