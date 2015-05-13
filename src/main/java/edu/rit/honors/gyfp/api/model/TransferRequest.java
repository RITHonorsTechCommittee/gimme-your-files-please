package edu.rit.honors.gyfp.api.model;

import com.google.appengine.api.users.User;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;
import edu.rit.honors.gyfp.api.Constants;
import edu.rit.honors.gyfp.util.OfyService;
import org.joda.time.DateTime;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import static com.google.common.base.Preconditions.checkNotNull;

@Entity
public class TransferRequest {

    private static final Logger log = Logger.getLogger(TransferRequest.class.getName());

    /**
     * The ID of this transfer request.  Automatically assigned by Objectify
     */
    @Id
    Long id;

    @Index
    String folderId;

    /**
     * The User who initiated the request
     */
    @Index
    private TransferUser requester;

    /**
     * The email address of the user as which the request is directed
     */
    @Index
    private TransferUser target;

    /**
     * The date/time that the request was made.
     */
    private DateTime requestCreation;

    /**
     * The files that will be transferred
     */
    Set<TransferableFile> files;

    /**
     * Whether this is a polite request or a forced transfer
     */
    boolean isForced;

    @SuppressWarnings("unused")
    private TransferRequest() {
        // Required for Objectify
    }

    private TransferRequest(String folderId, FileUser requester, FileUser target, Collection<TransferableFile> files) {
        this.folderId = checkNotNull(folderId);
        checkNotNull(requester);
        this.requester = new TransferUser(requester.getPermission(), requester.getName(), requester.getEmail());

        checkNotNull(target);
        this.target = new TransferUser(target.getPermission(), target.getName(), target.getEmail());

        this.files = new HashSet<>(checkNotNull(files));
    }

    /**
     * Creates a transfer request for a given folder between two users.
     *
     * If no request between the two users exists, or the request between the two users is for a different folder, a new transfer request is returned.
     * If the request already exists for this folder between two users, the contents of the request are updated.  If this update resulted in the addition of any files, the updated request is returned.
     * @param folder  The folder from which files will be transferred
     * @param user  The requesting user (Java Auth User)
     * @param requesterId The requesting user ID (from google drive)
     * @param targetId The ID of the target user
     * @return
     */
    public static TransferRequest fromFolder(Folder folder, User user, String requesterId, String targetId) {
        checkNotNull(folder);
        checkNotNull(user);
        checkNotNull(requesterId);
        checkNotNull(targetId);
        FileUser target = checkNotNull(folder.getUser(targetId));

        // Attempt to load the an existing TransferRequest
        TransferRequest request = OfyService.ofy().load()
                .type(TransferRequest.class)
                .filter("target.permission", targetId)
                .filter("requester.permission", requesterId)
                .filter("folder", folder.getId())
                .first().now();

        log.info("Creating transfer request for user " + targetId + " files: " + target.getFiles());
        List<TransferableFile> files = target.getFiles().get(Constants.Role.OWNER);

        if (request == null) {
            log.info("Creating a new transfer request.");


            FileUser requester;
            if (folder.getUser(requesterId) != null) {
                requester = folder.getUser(requesterId);
            } else {
                requester = new FileUser(requesterId, user.getNickname(), user.getEmail());
                folder.addUser(requester);
            }

            request = new TransferRequest(folder.getId(), requester, target, files);
        } else {
            log.info("Updating existing transfer request.");
            int size = request.getFiles().size();
            request.getFiles().addAll(files);
            if (size == request.getFiles().size()) {
                return null;
            }
        }

        OfyService.ofy().save().entity(request).now();
        OfyService.ofy().clear();

        return request;
    }

    public Long getId() {
        return id;
    }

    public TransferUser getRequestingUser() {
        return requester;
    }

    public TransferUser getTargetUser() {
        return target;
    }

    public DateTime getRequestCreationTime() {
        return requestCreation;
    }

    public Set<TransferableFile> getFiles() {
        return files;
    }

    public void setIsForced(boolean isForced) {
        this.isForced = isForced;
    }


}
