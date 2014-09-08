package edu.rit.honors.gyfp.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.appengine.auth.oauth2.AbstractAppEngineAuthorizationCodeServlet;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.Drive.Files;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

import edu.rit.honors.gyfp.util.State;
import edu.rit.honors.gyfp.util.Utils;

import java.util.logging.Logger;

/**
 * Entry servlet for the Drive API App Engine Sample. Demonstrates how to make
 * an authenticated API call using OAuth 2 helper classes.
 */
public class DriveSampleServlet extends
		AbstractAppEngineAuthorizationCodeServlet {

	private static final Logger log = Logger.getLogger(DriveSampleServlet.class.getName());
	
	/**
	 * Be sure to specify the name of your application. If the application name
	 * is {@code null} or blank, the application will log a warning. Suggested
	 * format is "MyCompany-ProductName/1.0".
	 */
	private static final String APPLICATION_NAME = "RIT-Honors-Drive/1.0";
	
	private static final String FOLDER_MIME = "application/vnd.google-apps.folder";

	private static final long serialVersionUID = 1L;

	private List<File> getChildren(Drive service, String rootFolderId, PrintWriter log) throws IOException {
		List<File> result = new ArrayList<File>();
		Files.List request = service.files().list();
		request.setQ(String.format("'%s' in parents", rootFolderId));
		request.setFields("items(id,mimeType,ownerNames,owners(displayName,kind,permissionId),parents(id,isRoot,kind),title),kind,nextPageToken");
		
		log.println("<ul>");
		do {
			try {
				FileList files = request.execute();
				
				// Add every file / folder in the hierarchy
				for (File f : files.getItems())
				{
					log.println("<li>");
					DriveSampleServlet.log.info(f.toPrettyString());
					if (f.getMimeType().equals(FOLDER_MIME))
					{
						log.println("<strong>" + f.getTitle() + "</strong>: " + f.getOwnerNames().get(0));
						//log.println(rootFolderId + " -> " + f.getTitle() + ": " + f.getId());
						result.addAll(getChildren(service, f.getId(), log));
					}
					else
					{
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
		
		String stateJson = req.getParameter("state");
		if (stateJson != null)
		{
			State driveState = new State(stateJson);
		
			// Send the results as the response
			writer.println("<h1>Listing Directory Contents</h1>");
			
			List<File> files = new ArrayList<File>();
			for (String id : driveState.ids)
			{
				files.addAll(getChildren(drive, id, writer));
			}
		}
		else
		{
			writer.println("Please use google drive to open a folder (or folders!) you want processed.");
		}
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
