package edu.rit.honors.gyfp.servlets;

import edu.rit.honors.gyfp.util.State;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Helper servlet to redirect the user when they open a folder from Google Drive.
 */
public class DriveOpenRedirectServlet extends DriveServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        // the state parameter contains a JSON object as a string with the ids of the file that
        // we're tyring to open
        String stateJson = req.getParameter("state");
        if (stateJson != null) {
            State driveState = new State(stateJson);

            // Only redirect if there's actually one folder (opening 0 or more than 1 would be
            // forbidden behavior for now.
            if (driveState.ids.size() != 1) {
                resp.sendRedirect("/#/error/only-one-file");
            } else {
                resp.sendRedirect("/#/manage/" + driveState.ids.iterator().next());
            }
        } else {
            resp.sendRedirect("/#/error/no-state");
        }
    }

    @Override
    protected String getTitle(HttpServletRequest req) {
        return "Redirect";
    }
}
