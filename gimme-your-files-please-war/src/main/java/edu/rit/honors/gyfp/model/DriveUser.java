package edu.rit.honors.gyfp.model;

import com.google.api.services.drive.model.User;

import static com.google.common.base.Preconditions.checkNotNull;

public class DriveUser {
	
	private User user;
	
	private String name;
	
	private String email; 
	
	private int filesOwned;
	
	private int filesEditor;
	
	private int filesViewer;
	
	public DriveUser(User user) {
		this.user = checkNotNull(user);
		this.name = user.getDisplayName();
		this.email = user.getEmailAddress();
	}
	
	public DriveUser(String name, String email, int filesOwned, int filesEditor, int filesViewer) {
		this.name = name;
		this.email = email;
		this.filesOwned = filesOwned;
		this.filesEditor = filesEditor;
		this.filesViewer = filesViewer;
	}

	public User getUser() {
		return user;
	}

	public String getName() {
		return name;
	}

	public String getEmail() {
		return email;
	}

	public int getFilesOwned() {
		return filesOwned;
	}

	public int getFilesEditor() {
		return filesEditor;
	}

	public int getFilesViewer() {
		return filesViewer;
	}
	
	

}
