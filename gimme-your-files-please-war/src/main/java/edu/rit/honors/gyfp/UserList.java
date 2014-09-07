package edu.rit.honors.gyfp;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.appengine.auth.oauth2.AbstractAppEngineAuthorizationCodeServlet;
import com.google.api.services.drive.Drive;

import edu.rit.honors.gyfp.util.Utils;

/**
 * Entry servlet for the Drive API App Engine Sample. Demonstrates how to make
 * an authenticated API call using OAuth 2 helper classes.
 */
public class UserList extends
		AbstractAppEngineAuthorizationCodeServlet {

	private static final Logger log = Logger.getLogger(UserList.class.getName());
	
	/**
	 * Be sure to specify the name of your application. If the application name
	 * is {@code null} or blank, the application will log a warning. Suggested
	 * format is "MyCompany-ProductName/1.0".
	 */
	private static final String APPLICATION_NAME = "Give-Me-Your-Files-Please/1.0";
	
	private static final String FOLDER_MIME = "application/vnd.google-apps.folder";

	private static final long serialVersionUID = 1L;

	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException {
		// Get the stored credentials using the Authorization Flow
		AuthorizationCodeFlow authFlow = initializeFlow();
		Credential credential = authFlow.loadCredential(getUserId(req));
		// Build the Drive object using the credentials
		Drive drive = new Drive.Builder(Utils.HTTP_TRANSPORT,
				Utils.JSON_FACTORY, credential).setApplicationName(
				APPLICATION_NAME).build();

		resp.setStatus(200);
		resp.setContentType("text/html");
		PrintWriter writer = resp.getWriter();
		
		RequestDispatcher view = req.getRequestDispatcher("UserList.jsp");
		
		List<String> users = Arrays.asList("Greg", "Joe", "Bob");
		
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
