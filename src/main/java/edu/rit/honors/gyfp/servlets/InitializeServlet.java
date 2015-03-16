package edu.rit.honors.gyfp.servlets;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Simple helper servlet that forces the user to install the servlet if not already installed.
 */
public class InitializeServlet extends DriveServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        getDriveService(req);

        resp.sendRedirect("/#/installed");
    }
}
