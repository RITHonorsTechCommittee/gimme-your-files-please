package edu.rit.honors.gyfp.api.model;

import com.google.api.services.drive.model.File;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;

@Entity
public class TransferableFile {

    private String filename;

    @Id
    private String fileid;

    /**
     * Needed for Objectify
     */
    @SuppressWarnings("unused")
    private TransferableFile() {
    }

    public TransferableFile(File file) {
        this.filename = file.getTitle();
        this.fileid = file.getId();
    }

    public TransferableFile(TransferableFile file) {
        this.filename = file.getFileName();
        this.fileid = file.getFileId();
    }

    public String getFileName() {
        return filename;
    }

    public String getFileId() {
        return fileid;
    }

    public String toString() {
        return fileid + " (" + filename + ")";
    }

}
