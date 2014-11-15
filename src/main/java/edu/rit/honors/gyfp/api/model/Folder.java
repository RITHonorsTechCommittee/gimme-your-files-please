package edu.rit.honors.gyfp.api.model;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.api.server.spi.response.ForbiddenException;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.Drive.Files;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.api.services.drive.model.Permission;
import com.google.appengine.api.users.User;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Unindex;

import edu.rit.honors.gyfp.api.Constants;
import edu.rit.honors.gyfp.util.OfyService;
import edu.rit.honors.gyfp.util.Utils;

@Entity
public class Folder {

	private static Logger log = Logger.getLogger(Folder.class.getName());

	/**
	 * The id of the folder stored in Google Drive
	 */
	@Id
	private String id;

	/**
	 * The contents of the folder, indexed by owner id
	 */
	private Map<String, FileUser> files;
	
	/**
	 * The ID of the user who owns this folder
	 */
	private String ownerUserId;
	
	
	/**
	 * The time at which this folder was last updated. Used to invalidate old
	 * cached entries.
	 */
	@Unindex
	private long updateTime;


	/**
	 * Needed for Objectify
	 */
	private Folder() { }

	/**
	 * Creates a new folder object.
	 * 
	 * @param folderid
	 */
	private Folder(String folderid) {
		this.id = checkNotNull(folderid);
		this.files = new HashMap<>();
	}

	/**
	 * Verifies that an instance of a Folder is not too old. Folder data
	 * generated more than the max age in the past should be regenerated to
	 * ensure fresh data
	 * 
	 * If the object is found to be dirty, it is scheduled for deletion from the
	 * datastore
	 * 
	 * @return True if the folder should be regenerated, false otherwise
	 */
	public boolean isDirty() {
		boolean isDirty = System.currentTimeMillis() - updateTime > 1000 * 3600;
		if (isDirty) {
			OfyService.ofy().delete().entity(this);
		}
		
		return isDirty;
	}

	/**
	 * Gets the maps of users to owned files
	 * 
	 * @return
	 */
	public Map<String, FileUser> getFiles() {
		return files;
	}

	/**
	 * @return  The ID of the folder
	 */
	public String getId() {
		return id;
	}
	
	/**
	 * @return  The ID of the user who owns this folder
	 */
	public String getOwnerUserId() {
		return ownerUserId;
	}

	/**
	 * Creates a folder from a given google file id, and preloads it with its
	 * children.
	 * 
	 * This method first attempts to load a cached version of the folder data,
	 * and will only create a new Folder object if the data does not yet exist
	 * or the cache is invalid
	 * 
	 * @param fileid
	 *            The google fileid of the folder to load
	 * @param user
	 *            A user who is authenticated to google drive
	 * 
	 * @return The corresponding folder
	 * @throws ForbiddenException   If the drive service cannot be created
	 */
	public static Folder fromGoogleId(String fileid, User user) throws ForbiddenException {
		
		
		// First step: Check the cache for an existing load of this folder
		Folder cached = OfyService.ofy().load().type(Folder.class).id(fileid).now();

		if (cached != null && !cached.isDirty()) {
			if (cached.ownerUserId.equals(user.getUserId())) {
				return cached;
			}
			
			throw new ForbiddenException("User " + user.getUserId() + " is not authorized to control this folder");
		}

		Drive service = Utils.createDriveFromUser(user);
		// The cache did not contain a valid folder, we need to load it fresh.
		Folder folder = new Folder(fileid);
		folder.updateTime = System.currentTimeMillis();

		folder.loadChildren(service);
		folder.ownerUserId = user.getUserId();

		OfyService.ofy().save().entity(folder);

		return folder;
	}

	/**
	 * Recursively loads the children of this folder and stores the children's
	 * owners and permissions
	 * 
	 * @param service
	 *            An authenticated instance of the google drive service
	 */
	private void loadChildren(Drive service) {
		List<File> children = getChildren(service, id);
		for (File f : children) {
			TransferableFile tf = new TransferableFile(f);
			for (Permission permission : f.getPermissions()) {
				this.loadPermission(tf, permission);
			}
		}
	}

	/**
	 * Helper method to load the user associated with a given permission
	 * 
	 * If the map already exists in the files map, it is returned. Otherwise a
	 * new entry is added to the map lookup
	 * 
	 * @param permission
	 *            The permission that will be looked up
	 * @return The populated map of permissions for this user
	 */
	private FileUser getUser(Permission permission) {
		FileUser user = files.get(permission.getId());

		if (user == null) {
			user = new FileUser(permission.getId(), permission.getName(),
					permission.getValue());
			files.put(permission.getId(), user);
		}

		return user;
	}

	/**
	 * Saves a permission for a specific user and file.
	 * 
	 * @param file
	 *            The file which to which the user has access
	 * @param permission
	 *            The actual permission of the user
	 */
	private void loadPermission(TransferableFile file, Permission permission) {
		getUser(permission).addFile(permission.getRole(), file);
	}

	/**
	 * A helper method which can look up all the children of a specific file ID.
	 * This is the generic utility version of the loadChildren method. This
	 * version can retrieve the children of any id, not just the for the current
	 * folder.
	 * 
	 * @param service
	 *            An authenticated instance of the google drive service
	 * @param id
	 *            The id of the parent folder.
	 * @return A list of files that have {@code id} as a parent
	 */
	private static List<File> getChildren(Drive service, String id) {
		List<File> result = new ArrayList<File>();
		Files.List request;
		try {
			request = service.files().list();

			request.setQ(String.format("'%s' in parents", id));
			request.setFields(Constants.Field.FOLDER_ENUM);

			do {
				try {
					FileList files = request.execute();

					// Add every file / folder in the hierarchy
					for (File f : files.getItems()) {
						result.add(f);
						if (f.getMimeType().equals(Constants.MimeType.FOLDER)) {
							result.addAll(getChildren(service, f.getId()));
						}
					}

					request.setPageToken(files.getNextPageToken());

				} catch (IOException e) {
					request.setPageToken(null);
					throw e;
				}
			} while (request.getPageToken() != null
                && request.getPageToken().length() > 0);

			return result;
		} catch (IOException e) {
			log.log(Level.SEVERE, "Error loading folder", e);
			return new ArrayList<>();
		}
	}

	/**
	 * Looks up a user and their files within this folder
	 *
	 * @param userId
	 *         The id of the user
	 * @return The FileUser stored for this folder, or null if none exists
	 */
	public FileUser getUser(String userId) {
		return files.get(userId);
	}
}