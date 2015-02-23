package edu.rit.honors.gyfp.api.model;

import com.google.appengine.api.users.User;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Class that stores information about users and the files they own.
 * 
 * This class mainly serves as a map between the three different permission
 * types (read, write, own)
 */
@Entity
public class FileUser {

    @Id
    @Index
    private String permission;

    private String name;

    private String email;

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
     * @param email
     *            The user's / group's email address
     */
    public FileUser(String permission, String name, String email) {
        this.permission = checkNotNull(permission);
        this.name = checkNotNull(name);
        this.email = checkNotNull(email);
        this.files = new HashMap<>();
    }

    public FileUser(User user) {
        this(user.getUserId(), user.getNickname(), user.getEmail());
    }

    public String getPermission() {
        return permission;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public Map<String, List<TransferableFile>> getFiles() {
        return files;
    }

    /**
     * @param role
     *          The role which the user has for the given file
     * @param file
     *          The file itself
     */
    public void addFile(String role, TransferableFile file) {
        List<TransferableFile> roleFiles = files.get(role);
        if (roleFiles ==  null) {
            roleFiles = new ArrayList<>();
            files.put(role, roleFiles);
        }
        roleFiles.add(file);
    }

    /**
     * A safe (non-null) way to get the list of files a user owns by the role.
     *
     * @param role
     *         The role for which the files will be returned
     * @return The files (not null)
     */
    public List<TransferableFile> getFiles(String role) {
        if (files.containsKey(role)) {
            return this.files.get(role);
        } else {
            return new ArrayList<>();
        }
    }
}
