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
	
	private TransferRequest(String rUser, String rEmail, String tUser, String tEmail, List<TransferableFile> files) {
		this.requestingUser = checkNotNull(rUser);
		this.requestingEmail = checkNotNull(rEmail);
		this.targetUser = checkNotNull(tUser);
		this.targetEmail = checkNotNull(tEmail);
		this.files = checkNotNull(files);
	}
	
	public static TransferRequest fromFolder(Folder folder, User user) {
		// TODO
		
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
	
	

}
