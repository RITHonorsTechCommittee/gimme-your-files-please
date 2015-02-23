package edu.rit.honors.gyfp.api;

public class Constants {

    public static class Scope {
        public static final String USER_EMAIL = "https://www.googleapis.com/auth/userinfo.email";
        public static final String DRIVE_FULL = "https://www.googleapis.com/auth/drive";
    }

    public static class Clients {
        public static final String WEB_CLIENT = "975557209634-fuq8i9nc7466p1nqn8aqv168vv3nttd0.apps.googleusercontent.com";
    }

    public static class Field {
        public static final String KIND_OWNER = "mimeType,owners(displayName,emailAddress,isAuthenticatedUser)";
        public static final String FOLDER_ENUM = "items(id,mimeType,permissions(additionalRoles,id,role,name,type,value),title),nextLink,nextPageToken";
    }

    public static class MimeType {
        public static final String FOLDER = "application/vnd.google-apps.folder";
    }

    public static class Email {
        public static final String ADDRESS = "no-reply@gimmeyourfilesplease.appspotmail.com";
        public static final String NAME = "GYFP Admin";
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
        public static final String FAILED_DRIVE_REQUEST = "The request to Google Drive caused an error.";
        public static final String SLEEP_INTERRUPTED = "Thread.sleep() interrupted during execution";
        public static final String INVALID_TRANSFER_LIMIT = "Invalid file transfer limit %d.  Must be between %d and %d";
        public static final String REMOVE_UNKNOWN_FILE_IDS = "The following file IDs are not part of the transfer request: %s";
        public static final String INVALID_MIME = "The given fileid is not a folder.  It has MIME type %s";
        public static final String INCORRECT_FOLDER_USER = "You are not the owner of this folder.  Only the following users are allowed to manage permissions of this folder:\n";
        public static final String LOAD_FOLDER_FOR_USER_FAIL = "Could not load file with id %s for user %s (%s)\n%s";
        public static final String MISSING_PARAMETER = "Missing required parameter '%s'";
    }

    public static class Strings {
        public static final String APP_NAME = "Gimme Your Files, Please";
    }
}
