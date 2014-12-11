package edu.rit.honors.gyfp.api.model;

import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.users.User;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;
import edu.rit.honors.gyfp.api.Constants;
import edu.rit.honors.gyfp.util.OfyService;
import org.joda.time.DateTime;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import static com.google.common.base.Preconditions.*;

@Entity
public class TransferRequest {

	private static final Logger log = Logger.getLogger(TransferRequest.class.getName());

	/**
	 * The ID of this transfer request.  Automatically assigned by Objectify
	 */
	@Id
	Long id;

	/**
	 * The User who initiated the request
	 */
	@Index
	private FileUser requester;

	/**
	 * The email address of the user as which the request is directed
	 */
	@Index
	private FileUser target;

	/**
	 * The date/time that the request was made.
	 */
	@Index
	private DateTime requestCreation;

	/**
	 * The files that will be transferred
	 */
	Set<TransferableFile> files;

	/**
	 * Whether this is a polite request or a forced transfer
	 */
	boolean isForced;
	
	private TransferRequest(FileUser requester, FileUser target, Collection<TransferableFile> files) {
		this.requester = checkNotNull(requester);
		this.target = checkNotNull(target);
		this.files = new HashSet<>(checkNotNull(files));
	}
	
	public static TransferRequest fromFolder(Folder folder, User user, String targetId) {
		checkNotNull(folder);
		checkNotNull(user);
		checkNotNull(targetId);

		// Attempt to load the an existing TransferRequest
		TransferRequest request = OfyService.ofy().load()
				.type(TransferRequest.class)
				.filter("target.permission", targetId)
				.filter("requester.permission", user.getUserId())
				.first().now();

		FileUser target = checkNotNull(folder.getUser(targetId));
		log.info("Creating transfer request for user " + targetId + " files: " + target.getFiles());
		List<TransferableFile> files = target.getFiles().get(Constants.Role.OWNER);

		if (request == null) {
			log.info("Creating a new transfer request.");
			FileUser requester = new FileUser(user);

			request = new TransferRequest(requester, target, files);
		} else {
			log.info("Updating existing transfer request.");
			request.getFiles().addAll(files);
		}

		OfyService.ofy().save().entity(request).now();

		return request;
	}

	public Long getId() {
		return id;
	}

	public String getRequestingUser() {
		return requester.getPermission();
	}

	public String getRequestingEmail() {
		return requester.getEmail();
	}

	public String getTargetUser() {
		return target.getPermission();
	}

	public String getTargetEmail() {
		return target.getEmail();
	}

	public DateTime getRequestCreation() {
		return requestCreation;
	}

	public Set<TransferableFile> getFiles() {
		return files;
	}

	public void setIsForced(boolean isForced) {
		this.isForced = isForced;
	}



}
