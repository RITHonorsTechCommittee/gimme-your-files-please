package edu.rit.honors.gyfp.api.model;

import com.google.api.client.googleapis.batch.BatchRequest;
import com.google.api.client.googleapis.batch.json.JsonBatchCallback;
import com.google.api.client.googleapis.json.GoogleJsonError;
import com.google.api.client.http.HttpHeaders;
import com.google.api.server.spi.response.ForbiddenException;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.Drive.Files;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.api.services.drive.model.Permission;
import com.google.appengine.api.users.User;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Ignore;
import com.googlecode.objectify.annotation.Unindex;
import edu.rit.honors.gyfp.api.Constants;
import edu.rit.honors.gyfp.util.OfyService;
import edu.rit.honors.gyfp.util.Utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.google.common.base.Preconditions.checkNotNull;

@Entity
public class Folder {

    private static Logger log = Logger.getLogger(Folder.class.getName());

    /**
     * The id of the folder stored in Google Drive
     */
    @Id
    private String id;

    /**
     * The contents of the folder, indexed by owner id
     */
    private Map<String, FileUser> files;

    /**
     * The ID of the user who owns this folder
     */
    private String ownerUserId;


    /**
     * The time at which this folder was last updated. Used to invalidate old cached entries.
     */
    @Unindex
    private long updateTime;

    @Ignore
    private ArrayList<AsyncFileList> currentFileLists;

    /**
     * Needed for Objectify
     */
    private Folder() {
    }

    /**
     * Creates a new folder object.
     *
     * @param folderid
     *         The google file id of the folder
     */
    private Folder(String folderid) {
        this.id = checkNotNull(folderid);
        this.files = new HashMap<>();
        this.currentFileLists = new ArrayList<>();
    }

    /**
     * Creates a folder from a given google file id, and preloads it with its children.
     * <p/>
     * This method first attempts to load a cached version of the folder data, and will only create a new Folder object
     * if the data does not yet exist or the cache is invalid
     *
     * @param fileid
     *         The google fileid of the folder to load
     * @param user
     *         A user who is authenticated to google drive
     *
     * @return The corresponding folder
     *
     * @throws ForbiddenException
     *         If the drive service cannot be created
     */
    public static Folder fromGoogleId(String fileid, User user) throws ForbiddenException {
        return fromGoogleId(fileid, user, false);
    }

    /**
     * Creates a folder from a given google file id, and preloads it with its children.
     * <p/>
     * This method first attempts to load a cached version of the folder data, and will only create a new Folder object
     * if the data does not yet exist or the cache is invalid
     *
     * @param fileid
     *         The google fileid of the folder to load
     * @param user
     *         A user who is authenticated to google drive
     * @param forceRefresh
     *         Whether the cached version of the folder should be ignored and the entire folder recalculated
     *
     * @return The corresponding folder
     *
     * @throws ForbiddenException
     *         If the drive service cannot be created
     */
    public static Folder fromGoogleId(String fileid, User user, boolean forceRefresh) throws ForbiddenException {

        // First step: Check the cache for an existing load of this folder
        Folder cached = null;

        if (!forceRefresh) {
            cached = OfyService.ofy().load().type(Folder.class).id(fileid).now();
        }

        if (cached != null && !cached.isDirty()) {
            return cached;
        }

        Drive service = Utils.createDriveFromUser(user);
        // The cache did not contain a valid folder, we need to load it fresh.
        Folder folder = new Folder(fileid);
        folder.updateTime = System.currentTimeMillis();

        folder.loadChildren(service);
        folder.ownerUserId = user.getUserId();

        OfyService.ofy().save().entity(folder);

        return folder;
    }

    /**
     * A helper method which can look up all the children of a specific file ID. This is the generic utility version of
     * the loadChildren method. This version can retrieve the children of any id, not just the for the current folder.
     *
     * @param service
     *         An authenticated instance of the google drive service
     * @param id
     *         The id of the parent folder.
     *
     * @return A list of files that have {@code id} as a parent
     */
    private static List<File> getChildren(Drive service, String id) {
        List<File> result = new ArrayList<>();
        Files.List request;
        try {
            request = service.files().list();

            request.setQ(String.format("'%s' in parents", id));
            request.setFields(Constants.Field.FOLDER_ENUM);

            do {
                try {
                    FileList files = request.execute();

                    // Add every file / folder in the hierarchy
                    for (File f : files.getItems()) {
                        result.add(f);
                        if (f.getMimeType().equals(Constants.MimeType.FOLDER)) {
                            result.addAll(getChildren(service, f.getId()));
                        }
                    }

                    request.setPageToken(files.getNextPageToken());

                } catch (IOException e) {
                    request.setPageToken(null);
                    throw e;
                }
            } while (request.getPageToken() != null
                    && request.getPageToken().length() > 0);

            return result;
        } catch (IOException e) {
            log.log(Level.SEVERE, "Error loading folder", e);
            return new ArrayList<>();
        }
    }

    private synchronized void receiveFileList(AsyncFileList list) {
        log.log(Level.FINE, "Received AFL " + list.id);
        currentFileLists.add(list);
    }

    private synchronized AsyncFileList getNextList() {
        if (!currentFileLists.isEmpty()) {
            AsyncFileList afl = currentFileLists.remove(currentFileLists.size() - 1);
            log.log(Level.FINE, "Enqueued request " + afl.id);
            return afl;
        }
        log.log(Level.FINE, "No more AFLs remain.");
        return null;
    }

    /**
     * Verifies that an instance of a Folder is not too old. Folder data generated more than the max age in the past
     * should be regenerated to ensure fresh data
     * <p/>
     * If the object is found to be dirty, it is scheduled for deletion from the datastore
     *
     * @return True if the folder should be regenerated, false otherwise
     */
    public boolean isDirty() {
        boolean isDirty = System.currentTimeMillis() - updateTime > 1000 * 3600;
        if (isDirty) {
            OfyService.ofy().delete().entity(this);
        }

        return isDirty;
    }

    /**
     * Gets the maps of users to owned files
     *
     * @return The the fileuser objects in this folder
     */
    public Map<String, FileUser> getFiles() {
        return files;
    }

    /**
     * @return The ID of the folder
     */
    public String getId() {
        return id;
    }

    /**
     * @return The ID of the user who owns this folder
     */
    public String getOwnerUserId() {
        return ownerUserId;
    }

    /**
     * Loads the folder using batches to minimize the number of required requests
     *
     * @param service
     *         The drive service
     */
    private void loadChildren(Drive service) {
        try {
            final Files.List request = service.files().list();

            request.setQ(String.format("'%s' in parents", id));
            request.setFields(Constants.Field.FOLDER_ENUM);

            FileList fileList = request.execute();
            receiveFileList(new AsyncFileList(fileList, request, id));

            loadFileBatchHelper(service);
        } catch (IOException e) {
            log.log(Level.SEVERE, "Error loading folder", e);
        }
    }

    /**
     * The recursive helper method that processes all AFLs and enqueues all the new requests into a batch.
     *
     * @param service
     *         The authenticated drive service
     */
    private void loadFileBatchHelper(Drive service) {
        AsyncFileList afl = getNextList();
        BatchRequest batch = service.batch();

        while (afl != null && batch.size() < 100) {
            for (final File f : afl.list.getItems()) {

                // Store the file
                TransferableFile tf = new TransferableFile(f);
                for (Permission permission : f.getPermissions()) {
                    this.loadPermission(tf, permission);
                }

                // If the file is a folder, make a new request to load the children and add it to
                // the batch.
                if (f.getMimeType().equals(Constants.MimeType.FOLDER)) {
                    try {
                        final Files.List request = service.files().list();


                        request.setQ(String.format("'%s' in parents", f.getId()));
                        request.setFields(Constants.Field.FOLDER_ENUM);
                        request.queue(batch, new JsonBatchCallback<FileList>() {
                            @Override
                            public void onFailure(GoogleJsonError e, HttpHeaders responseHeaders) throws IOException {
                                log.log(Level.SEVERE, "Error processing child load request", e);
                            }

                            @Override
                            public void onSuccess(FileList fileList, HttpHeaders responseHeaders) throws IOException {
                                receiveFileList(new AsyncFileList(fileList, request, f.getId()));
                            }
                        });

                    } catch (IOException e) {
                        log.log(Level.SEVERE, "Error processing child load request", e);
                    }
                }
            }

            // Handle the paging.  Some requests could be split across multiple pages, and this sort of recursion can be
            // treated exactly the same
            final Files.List request = afl.request;
            final String id = afl.id + " paged";
            // Also queue up the next page
            if (request.getPageToken() != null
                    && request.getPageToken().length() > 0) {
                request.setPageToken(afl.list.getNextPageToken());

                try {
                    request.queue(batch, new JsonBatchCallback<FileList>() {
                        @Override
                        public void onFailure(GoogleJsonError e, HttpHeaders responseHeaders) throws IOException {
                            log.log(Level.SEVERE, "Error processing next page request", e);
                        }

                        @Override
                        public void onSuccess(FileList fileList, HttpHeaders responseHeaders) throws IOException {
                            receiveFileList(new AsyncFileList(fileList, request, id));
                        }
                    });
                } catch (IOException e) {
                    log.log(Level.SEVERE, "Error queueing next page request", e);
                }
            }

            afl = getNextList();
        }

        // Actually execute the batch
        if (batch.size() > 0) {
            try {
                log.info("Executing batch request with " + batch.size() + " requests.");
                batch.execute();
                loadFileBatchHelper(service);
            } catch (IOException e) {
                log.log(Level.SEVERE, "Error running batch", e);
            }
        }
    }

    /**
     * Helper method to load the user associated with a given permission
     * <p/>
     * If the map already exists in the files map, it is returned. Otherwise a new entry is added to the map lookup
     *
     * @param permission
     *         The permission that will be looked up
     *
     * @return The populated map of permissions for this user
     */
    private FileUser getUser(Permission permission) {
        FileUser user = files.get(permission.getId());

        if (user == null) {

            // Handle the two special cases:  "anyone" and "anyoneWithLink".  These users cannot own files so we can
            // safely create "dummy" FileUsers
            if (Constants.Role.ANYONE.equals(permission.getId())) {
                user = new FileUser(permission.getId(), permission.getId(), permission.getId());
            } else if (Constants.Role.ANYONE_WITH_LINK.equals(permission.getId())) {
                user = new FileUser(permission.getId(), permission.getId(), permission.getId());
            } else {
                user = new FileUser(permission.getId(), permission.getName(),
                        permission.getValue());
            }
            files.put(permission.getId(), user);
        }

        return user;
    }

    /**
     * Saves a permission for a specific user and file.
     *
     * @param file
     *         The file which to which the user has access
     * @param permission
     *         The actual permission of the user
     */
    private void loadPermission(TransferableFile file, Permission permission) {
        getUser(permission).addFile(permission.getRole(), file);
    }

    /**
     * Looks up a user and their files within this folder
     *
     * @param userId
     *         The id of the user
     *
     * @return The FileUser stored for this folder, or null if none exists
     */
    public FileUser getUser(String userId) {
        if (files != null) {
            return files.get(userId);
        }
        return null;
    }

    /**
     * Helper data storage class.
     * <p/>
     * This facilitates the recursive loading of the folder.
     * <p/>
     * <ol> <li>Load the base folder in a single request.  This cannot be made more efficient via a batch, as there is
     * only one api call to be made at this level.  The result of this is added to currentFileLists</li> <li>The
     * recursive helper method then takes each AFL from the list currentFileLists.  Each file is added to the folder. If
     * the file is a folder, a ew request is created and added to the batch.  The callback of this api call is then
     * added to currentFileLists</li> <li>After processing all the files from the AFL, there is a check for paging.  If
     * there is a next page token present, the execution of the next page is also added to the batch</li> </ol>
     */
    private class AsyncFileList {
        private final FileList list;
        private final Files.List request;
        private String id;

        /**
         * Constructor.
         *
         * @param list
         *         The file list result of the request
         * @param request
         *         The actual request that caused this execution
         * @param id
         *         The ID of the folder being loaded
         */
        public AsyncFileList(FileList list, Files.List request, String id) {
            this.list = checkNotNull(list);
            this.request = checkNotNull(request);
            this.id = checkNotNull(id);
        }
    }
}