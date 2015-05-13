package edu.rit.honors.gyfp.api.model;

import com.google.common.base.Preconditions;
import com.googlecode.objectify.annotation.Id;

import static com.google.common.base.Preconditions.*;

/**
 * Created by greg on 5/11/2015.
 */
public class TransferUser {
    /**
     * The ID of this transfer request.  Same as the permission
     * Objectify doesn't seem to want to let us query when permission is used as the @Id :(
     */
    @Id
    protected String id;
    protected String permission;
    protected String name;
    protected String email;

    public TransferUser(String permission, String name, String email) {
        this.permission = checkNotNull(permission);
        this.id = permission;
        this.name = checkNotNull(name);
        this.email = checkNotNull(email);
    }

    // Unused.  Needed for objectify
    protected TransferUser() { }

    public String getPermission() {
        return permission;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }
}
