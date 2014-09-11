package edu.rit.honors.gyfp.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.appengine.auth.oauth2.AbstractAppEngineAuthorizationCodeServlet;
import com.google.api.services.drive.Drive;

import edu.rit.honors.gyfp.model.DriveUser;
import edu.rit.honors.gyfp.util.Utils;

/**
 * Entry servlet for the Drive API App Engine Sample. Demonstrates how to make
 * an authenticated API call using OAuth 2 helper classes.
 */
public class UserList extends AbstractAppEngineAuthorizationCodeServlet {

	private static final long serialVersionUID = -5877306066505657461L;

	private static final Logger log = Logger.getLogger(UserList.class.getName());
	
	/**
	 * Be sure to specify the name of your application. If the application name
	 * is {@code null} or blank, the application will log a warning. Suggested
	 * format is "MyCompany-ProductName/1.0".
	 */
	private static final String APPLICATION_NAME = "Give-Me-Your-Files-Please/1.0";
	

	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException {
		log.warning("In the handler");
		// Get the stored credentials using the Authorization Flow
		RequestDispatcher view = req.getRequestDispatcher("UserList.jsp");
		
		List<DriveUser> users = Arrays.asList(
				new DriveUser("Person A", "notreal-A@rit.edu", 100,  1000, 20),
				new DriveUser("Person B", "notreal-B@rit.edu", 1000, 1100, 20),
				new DriveUser("Person C", "notreal-C@rit.edu", 300,  1100, 200),
				new DriveUser("Person D", "notreal-D@rit.edu", 406,  666,  111),
				new DriveUser("Person E", "notreal-E@rit.edu", 51,   10,   38)
				);
		
		req.setAttribute("users", users);
		view.forward(req, resp);
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
