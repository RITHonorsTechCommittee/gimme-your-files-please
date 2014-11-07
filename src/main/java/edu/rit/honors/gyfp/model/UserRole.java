package edu.rit.honors.gyfp.model;

import java.util.HashMap;
import java.util.Map;

public enum UserRole {
	OWNER("owner"),
	READER("reader"),
	WRITER("writer");
	
	private String name;
	
	private static final Map<String, UserRole> roles;
	
	static {
		roles = new HashMap<String, UserRole>();
		
		for (UserRole role : values()) {
			roles.put(role.getName(), role);
		}
	}

	private UserRole(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public static UserRole lookupRole(String role) {
		return roles.get(role);
	}
	

}
