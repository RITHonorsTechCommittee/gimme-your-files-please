package edu.rit.honors.gyfp.servlets;

import com.google.api.services.drive.Drive;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by greg on 2/18/2015.
 */
public class InitializeServlet extends DriveServlet {

    @Override
    protected String getTitle(HttpServletRequest req) {
        return null;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Drive service = getDriveService(req);

        resp.sendRedirect("/");
    }
}
