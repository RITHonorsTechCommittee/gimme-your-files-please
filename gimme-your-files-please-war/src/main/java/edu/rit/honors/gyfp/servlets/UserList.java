package edu.rit.honors.gyfp.servlets;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.Permission;

import edu.rit.honors.gyfp.drive.FileHelper;
import edu.rit.honors.gyfp.model.DriveUser;
import edu.rit.honors.gyfp.util.Utils;

/**
 * Entry servlet for the Drive API App Engine Sample. Demonstrates how to make
 * an authenticated API call using OAuth 2 helper classes.
 */
public class UserList extends DriveServlet {

	private static final long serialVersionUID = -5877306066505657461L;

	private static final String PARAM_FOLDER = "folderId";

	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException {

		// Get the stored credentials using the Authorization Flow
		RequestDispatcher view = req.getRequestDispatcher("UserList.jsp");

		String folderId = req.getParameter(PARAM_FOLDER);
		Drive service = getDriveService(req);
		FileHelper files = getFileHelper(service);
		File rootFolder = null;
		if (folderId != null) {
			rootFolder = files.getFileById(folderId);
		}

		if (rootFolder != null) {
			Collection<File> children = files.getChildren(rootFolder);

			Map<String, DriveUser> users = new HashMap<>();

			for (File child : children) {
				List<Permission> filePermissions = child.getPermissions();

				if (filePermissions != null) {

					for (Permission permission : filePermissions) {
						String user = permission.getEmailAddress();
						DriveUser driveUser = users.get(user);
						if (driveUser == null) {
							driveUser = new DriveUser(permission);
							users.put(user, driveUser);
						}

						driveUser.countFile(permission);

					}
				}
			}

			req.setAttribute("users", users.values());
		} else {
			req.setAttribute("users", new ArrayList<String>());
		}
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
