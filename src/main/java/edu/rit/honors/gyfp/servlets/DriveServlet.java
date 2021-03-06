package edu.rit.honors.gyfp.servlets;

import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.appengine.auth.oauth2.AbstractAppEngineAuthorizationCodeServlet;
import com.google.api.services.drive.Drive;
import edu.rit.honors.gyfp.util.Utils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class DriveServlet extends
        AbstractAppEngineAuthorizationCodeServlet {

    private static final Logger log = Logger.getLogger(DriveServlet.class.getName());

    private static final long serialVersionUID = 3919061798514265674L;

    public static final String APPLICATION_NAME = "gimme-your-files-please/0.1";

    protected Drive getDriveService(HttpServletRequest req) {

        Drive drive = null;
        try {
            // Get the stored credentials using the Authorization Flow
            AuthorizationCodeFlow authFlow = initializeFlow();
            Credential credential = authFlow.loadCredential(getUserId(req));
            // Build the Drive object using the credentials
            drive = new Drive.Builder(Utils.HTTP_TRANSPORT,
                    Utils.JSON_FACTORY, credential).setApplicationName(
                    APPLICATION_NAME).build();
        } catch (IOException | ServletException e) {
            log.log(Level.SEVERE, "Error authenticating", e);
        }

        return drive;
    }

    @Override
    protected AuthorizationCodeFlow initializeFlow() throws ServletException,
            IOException {
        return Utils.initializeFlow();
    }

    @Override
    protected String getRedirectUri(HttpServletRequest req)
            throws ServletException, IOException {
        return Utils.getRedirectUri(req);
    }

}
