package edu.rit.honors.gyfp.api.model;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;

import org.joda.time.DateTime;

import com.google.appengine.api.users.User;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;

@Entity
public class TransferRequest {
	
	/**
	 * The ID of this transfer request.  Automatically assigned by Objectify
	 */
	@Id
	Long id;
	
	
	/**
	 * The ID of the User who requested the transfer
	 */
	String requestingUser;
	
	/**
	 * The email address of the user who requested the transfer 
	 */
	String requestingEmail;
	
	/**
	 * The ID of the user as which the request is directed
	 */
	String targetUser;
	
	/**
	 * The email address of the user as which the request is directed 
	 */
	String targetEmail;
	
	/**
	 * The date/time that the request was made.
	 */
	DateTime requestCreation;
	
	/**
	 * The files that will be transferred 
	 */
	List<TransferableFile> files;
	
	/**
	 * Whether this is a polite request or a forced transfer
	 */
	boolean isForced;
	
	private TransferRequest(String rUser, String rEmail, String tUser, String tEmail, List<TransferableFile> files) {
		this.requestingUser = checkNotNull(rUser);
		this.requestingEmail = checkNotNull(rEmail);
		this.targetUser = checkNotNull(tUser);
		this.targetEmail = checkNotNull(tEmail);
		this.files = checkNotNull(files);
	}
	
	public static TransferRequest fromFolder(Folder folder, User user, String targetId) {
		// TODO
		
		// 1.  Check for existing transfer request
		// 2.  Create or Update transfer request
		// 3.  Persist this request object
		// 4.  Must store request object in folder
		// 5.  Persist the folder changes
		return null;
	}

	public Long getId() {
		return id;
	}

	public String getRequestingUser() {
		return requestingUser;
	}

	public String getRequestingEmail() {
		return requestingEmail;
	}

	public String getTargetUser() {
		return targetUser;
	}

	public String getTargetEmail() {
		return targetEmail;
	}

	public DateTime getRequestCreation() {
		return requestCreation;
	}

	public List<TransferableFile> getFiles() {
		return files;
	}

	public void setIsForced(boolean isForced) {
		this.isForced = isForced;
	}



}
