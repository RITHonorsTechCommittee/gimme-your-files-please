/**
 * 
 */
package edu.rit.honors.gyfp.drive.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.annotation.Nullable;

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.Drive.Files;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.api.services.drive.model.ParentReference;

import edu.rit.honors.gyfp.drive.FileHelper;
import edu.rit.honors.gyfp.model.DriveUser;

/**
 * Implementation of the File Helper Singleton
 */
public class FileHelperImpl implements FileHelper
{

    private static final String FOLDER_MIME = "application/vnd.google-apps.folder";

    private Drive service;

    private static FileHelper instance;

    /**
     * This doesn't implement singleton correctly (constructor should be
     * private) TODO: One we figure out how this will fit into the lifecycle of
     * the servlet we can refactor
     *
     * @param service
     *            A reference to the drive API that has been authenticated
     */
    public FileHelperImpl(Drive service)
    {
        this.service = service;
        instance = this;
    }

    /**
     * Gets the instance of the FileHelper
     *
     * @return The instance
     */
    public static FileHelper getInstance()
    {
        return instance;
    }

    public Collection<File> getChildren(File file)
    {
        List<File> result = new ArrayList<>();
        Files.List request;
        try
        {
            request = service.files().list();

            request.setQ(String.format("'%s' in parents", file.getId()));
            request.setFields("items(id,mimeType,parents(id,isRoot,kind),permissions(emailAddress,id,name,photoLink,role),title),kind,nextPageToken");

            do
            {
                try
                {
                    FileList files = request.execute();

                    // Add every file / folder in the hierarchy
                    for (File f : files.getItems())
                    {
                        if (f.getMimeType().equals(FOLDER_MIME))
                        {
                            result.addAll(getChildren(f));
                        }
                        else
                        {
                            result.add(f);
                        }
                    }

                    request.setPageToken(files.getNextPageToken());

                }
                catch (IOException e)
                {
                    request.setPageToken(null);
                    throw e;
                }
            } while (request.getPageToken() != null && request.getPageToken().length() > 0);

            return result;
        }
        catch (IOException e1)
        {
            return new ArrayList<com.google.api.services.drive.model.File>();
        }
    }

    public File getParent(File file)
    {
        List<ParentReference> parents = file.getParents();
        if (parents != null && parents.size() > 0)
        {
            return getFileById(parents.get(0).getId());
        }

        return null;
    }

    public Collection<File> getSiblings(File file)
    {
        File parent = getParent(file);
        return getChildren(parent);
    }

    public Collection<DriveUser> getUsers(File file)
    {
        return null;
    }

    public boolean addUser(DriveUser user, File file)
    {
        return false;
    }

    public boolean removeUser(DriveUser user, File file)
    {
        return false;
    }

    public boolean hasUser(DriveUser user, File file)
    {
        return false;
    }

    public boolean isFile(File f)
    {
        return f != null && !f.getMimeType().equals(FOLDER_MIME);
    }

    public boolean isDirectory(File f)
    {
        return f != null && f.getMimeType().equals(FOLDER_MIME);
    }

    public boolean hasChildren(File f)
    {
        // TODO: Should find a better way than just calculating the children.
        return isDirectory(f) && getChildren(f).size() > 0;
    }

    public @Nullable File getFileById(String id)
    {
        try
        {
            File file = service.files().get(id).execute();
            return file;
        }
        catch (IOException e)
        {
            return null;
        }
    }

}
