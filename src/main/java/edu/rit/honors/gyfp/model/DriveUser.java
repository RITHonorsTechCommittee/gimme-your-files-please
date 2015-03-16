package edu.rit.honors.gyfp.model;

import com.google.api.services.drive.model.Permission;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Entity
public class DriveUser {

    @Id
    Long id;


    private String name;

    private String email;

    private Map<UserRole, List<String>> permissions;

    /**
     * Needed for Objectify
     */
    @SuppressWarnings("unused")
    private DriveUser() {
    }

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

    @SuppressWarnings("unused")
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

    @SuppressWarnings("unused")
    public int getFilesOwner() {
        return permissions.get(UserRole.OWNER).size();
    }

    @SuppressWarnings("unused")
    public int getFilesWriter() {
        return permissions.get(UserRole.WRITER).size();
    }

    @SuppressWarnings("unused")
    public int getFilesReader() {
        return permissions.get(UserRole.READER).size();
    }

    @SuppressWarnings("unused")
    public long getId() {
        return id;
    }


}
