package edu.rit.honors.gyfp.api;

public class Constants {

	public static class Scope {
		public static final String DRIVE_METADATA_READONLY = "https://www.googleapis.com/auth/drive.readonly.metadata";
		public static final String USER_EMAIL = "https://www.googleapis.com/auth/userinfo.email";
	}

	public static class Field {
		public static final String KIND_OWNER = "mimeType,owners(displayName,emailAddress,isAuthenticatedUser)";
		public static final String FOLDER_ENUM = "items(id,mimeType,permissions(additionalRoles,id,role,name,type,value),title),nextLink,nextPageToken";
	}

	public static class MimeType {
		public static final String FOLDER = "application/vnd.google-apps.folder";
	}

	public static class Role {
		public static final String OWNER = "owner";
		public static final String WRITER = "writer";
		public static final String READER = "reader";
	}
	
	public static class Error {
		public static final String AUTH_REQUIRED = "Authentication is required to use this API.";
		public static final String TRANSFER_REQUEST_NOT_FOUND = "The transfer request %d could not be found.";
		public static final String TRANSFER_REQUEST_INCORRECT_USER = "You do not have access to this transfer request.";
	}
}
