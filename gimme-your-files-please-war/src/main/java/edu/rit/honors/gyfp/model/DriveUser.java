package edu.rit.honors.gyfp.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.api.services.drive.model.Permission;
import com.google.api.services.drive.model.User;

import static com.google.common.base.Preconditions.checkNotNull;

public class DriveUser {
	
	private String name;
	
	private String email; 
	
	private Map<UserRole, List<String>> permissions; 
	
	public DriveUser(Permission user) {
		this(user.getName(), user.getEmailAddress());
	}
	
	public DriveUser(String name, String email) {
		this.name = name;
		this.email = email;
		
		permissions = new HashMap<>();
		for (UserRole role : UserRole.values()) {
			permissions.put(role, new ArrayList<String>());
		}
	}
	
	public void countFile(Permission perm) {
		UserRole role = UserRole.lookupRole(perm.getRole());
		permissions.get(role).add(perm.getId());
	}

	public String getName() {
		return name;
	}

	public String getEmail() {
		return email;
	}

	public int getFilesOwner() {
		return permissions.get(UserRole.OWNER).size();
	}

	public int getFilesWriter() {
		return permissions.get(UserRole.WRITER).size();
	}

	public int getFilesReader() {
		return permissions.get(UserRole.READER).size();
	}
	
	

}
