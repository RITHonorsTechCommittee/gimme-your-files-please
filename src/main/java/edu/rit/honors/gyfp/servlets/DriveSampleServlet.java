package edu.rit.honors.gyfp.servlets;

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.Drive.Files;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import edu.rit.honors.gyfp.util.State;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Entry servlet for the Drive API App Engine Sample. Demonstrates how to make an authenticated API call using OAuth 2
 * helper classes.
 */
public class DriveSampleServlet extends
        DriveServlet {

    private static final Logger log = Logger.getLogger(DriveSampleServlet.class.getName());

    private static final String FOLDER_MIME = "application/vnd.google-apps.folder";

    private static final long serialVersionUID = 1L;

    private List<File> getChildren(Drive service, String rootFolderId, PrintWriter log) throws IOException {
        List<File> result = new ArrayList<>();
        Files.List request = service.files().list();
        request.setQ(String.format("'%s' in parents", rootFolderId));
        request.setFields("items(id,mimeType,ownerNames,owners(displayName,kind,permissionId),parents(id,isRoot,kind),title),kind,nextPageToken");

        log.println("<ul>");
        do {
            try {
                FileList files = request.execute();

                // Add every file / folder in the hierarchy
                for (File f : files.getItems()) {
                    log.println("<li>");
                    DriveSampleServlet.log.info(f.toPrettyString());
                    if (f.getMimeType().equals(FOLDER_MIME)) {
                        log.println("<strong>" + f.getTitle() + "</strong>: " + f.getOwnerNames().get(0));
                        //log.println(rootFolderId + " -> " + f.getTitle() + ": " + f.getId());
                        result.addAll(getChildren(service, f.getId(), log));
                    } else {
                        log.println(f.getTitle() + ": " + f.getOwnerNames().get(0));
                        result.add(f);
                    }
                    log.println("</li>");
                }

                request.setPageToken(files.getNextPageToken());

            } catch (IOException e) {
                request.setPageToken(null);
                throw e;
            }
        } while (request.getPageToken() != null
                && request.getPageToken().length() > 0);

        log.println("</ul>");

        return result;
    }

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException, ServletException {
        Drive drive = getDriveService(req);


        resp.setStatus(200);
        resp.setContentType("text/html");
        PrintWriter writer = resp.getWriter();

        String stateJson = req.getParameter("state");
        if (stateJson != null) {
            State driveState = new State(stateJson);

            // Send the results as the response
            writer.println("<h1>Listing Directory Contents</h1>");

            List<File> files = new ArrayList<>();
            for (String id : driveState.ids) {
                files.addAll(getChildren(drive, id, writer));
            }
        } else {
            writer.println("Please use google drive to open a folder (or folders!) you want processed.");
        }
    }

    @Override
    protected String getTitle(HttpServletRequest req) {
        return "Drive Sample Servlet";
    }
}
