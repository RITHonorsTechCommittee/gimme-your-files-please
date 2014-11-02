package edu.rit.honors.gyfp.api.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;

/**
 * Class that stores information about users and the files they own.
 * 
 * This class mainly serves as a map between the three different permission
 * types (read, write, own)
 */
@Entity
public class FileUser {

	@Id
	private String permission;

	private String name;

	private String value;

	private Map<String, List<TransferableFile>> files;
	
	/**
	 * Needed for Objectify
	 */
	@SuppressWarnings("unused")
	private FileUser() { }

	/**
	 * Constructor
	 * 
	 * @param permission
	 *            The permission id for this user
	 * @param name
	 *            The name of the user
	 * @param value
	 *            The user's / group's email address
	 */
	public FileUser(String permission, String name, String value) {
		this.permission = permission;
		this.name = name;
		this.value = value;
		this.files = new HashMap<>();
	}

	public String getPermission() {
		return permission;
	}

	public String getName() {
		return name;
	}

	public String getValue() {
		return value;
	}
	
	public Map<String, List<TransferableFile>> getFiles() {
		return files;
	}

	/**
	 * @param role
	 * @param file
	 */
	public void addFile(String role, TransferableFile file) {
		List<TransferableFile> roleFiles = files.get(role);
		if (roleFiles ==  null) {
			roleFiles = new ArrayList<>();
			files.put(role, roleFiles);
		}
		roleFiles.add(file);
	}
}
