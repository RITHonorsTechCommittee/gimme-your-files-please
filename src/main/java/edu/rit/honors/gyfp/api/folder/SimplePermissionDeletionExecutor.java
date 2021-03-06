package edu.rit.honors.gyfp.api.folder;

import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.services.drive.Drive;
import edu.rit.honors.gyfp.api.IncrementalExecutor;
import edu.rit.honors.gyfp.api.model.FileUser;
import edu.rit.honors.gyfp.api.model.TransferableFile;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 */
public class SimplePermissionDeletionExecutor implements IncrementalExecutor<TransferableFile> {

    private static final Logger log = Logger.getLogger(SimplePermissionDeletionExecutor.class.getName());

    private final Drive service;
    private final FileUser user;

    /**
     * @param service
     *         The authenticated drive service
     * @param user
     *         The user for whom the permissions will be deleted
     */
    public SimplePermissionDeletionExecutor(Drive service, FileUser user) {
        this.service = service;
        this.user = user;
    }

    @Override
    public boolean execute(TransferableFile item) {
        try {
            service.permissions().delete(item.getFileId(), user.getPermission()).execute();
            log.log(Level.INFO, "Successfully processed " + item);
            return true;
        } catch (GoogleJsonResponseException e) {

            if (e.getDetails().getCode() == 404) {
                log.log(Level.INFO, "Permission already deleted for file " + item);
                return true;
            } else {
                log.log(Level.SEVERE, "Error processing permission revocation for " + item, e);
                return false;
            }
        } catch (IOException e) {
            log.log(Level.SEVERE, "Error processing permission revocation for " + item, e);
            return false;
        }
    }
}
