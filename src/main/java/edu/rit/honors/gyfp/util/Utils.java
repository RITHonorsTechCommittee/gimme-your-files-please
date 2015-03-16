/*
 * Copyright (c) 2013 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package edu.rit.honors.gyfp.util;

import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.appengine.datastore.AppEngineDataStoreFactory;
import com.google.api.client.extensions.appengine.http.UrlFetchTransport;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.Preconditions;
import com.google.api.client.util.store.DataStoreFactory;
import com.google.api.server.spi.response.ForbiddenException;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.appengine.api.users.User;
import edu.rit.honors.gyfp.api.Constants;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

public class Utils {

    private static final Logger log = Logger.getLogger(Utils.class.getName());

    /**
     * Global instance of the {@link DataStoreFactory}. The best practice is to make it a single globally shared
     * instance across your application.
     */
    private static final AppEngineDataStoreFactory DATA_STORE_FACTORY = AppEngineDataStoreFactory
            .getDefaultInstance();

    private static GoogleClientSecrets clientSecrets = null;
    public static final String MAIN_SERVLET_PATH = "/#/installed";
    public static final String AUTH_CALLBACK_SERVLET_PATH = "/oauth2callback";
    public static final HttpTransport HTTP_TRANSPORT = new UrlFetchTransport();
    public static final JacksonFactory JSON_FACTORY = JacksonFactory
            .getDefaultInstance();

    private static GoogleClientSecrets getClientSecrets() throws IOException {
        log.info("Setting the stuff.");
        if (clientSecrets == null) {
            clientSecrets = GoogleClientSecrets.load(
                    JSON_FACTORY,
                    new InputStreamReader(Utils.class
                            .getResourceAsStream("/client_secrets.json")));
            Preconditions
                    .checkArgument(
                            !clientSecrets.getDetails().getClientId()
                                    .startsWith("Enter ")
                                    && !clientSecrets.getDetails()
                                    .getClientSecret()
                                    .startsWith("Enter "),
                            "Download client_secrets.json file from "
                                    + "https://code.google.com/apis/console/?api=drive#project:456052621 into "
                                    + "src/main/resources/client_secrets.json");
        }
        return clientSecrets;
    }

    public static GoogleAuthorizationCodeFlow initializeFlow()
            throws IOException {
        Set<String> scopes = new HashSet<String>();
        scopes.add(DriveScopes.DRIVE);
        scopes.add(DriveScopes.DRIVE + ".install");
        scopes.add("email");
        scopes.add("profile");

        return new GoogleAuthorizationCodeFlow.Builder(HTTP_TRANSPORT,
                JSON_FACTORY, getClientSecrets(), scopes)
                .setDataStoreFactory(DATA_STORE_FACTORY)
                .setApprovalPrompt("force")
                .setAccessType("offline").build();
    }

    public static String getRedirectUri(HttpServletRequest req) {
        GenericUrl requestUrl = new GenericUrl(req.getRequestURL().toString());
        requestUrl.setRawPath(AUTH_CALLBACK_SERVLET_PATH);
        return requestUrl.build();
    }

    /**
     * @param user
     *         The user for which the service will be created
     *
     * @return The instantiated drive service
     *
     * @throws ForbiddenException
     *         If the service could not be created
     */
    public static Drive createDriveFromUser(User user) throws ForbiddenException {
        try {
            AuthorizationCodeFlow authFlow = Utils.initializeFlow();
            Credential credential = authFlow.loadCredential(user.getUserId());
            return new Drive.Builder(Utils.HTTP_TRANSPORT, Utils.JSON_FACTORY, credential)
                    .setApplicationName(Constants.Strings.APP_NAME)
                    .build();
        } catch (IOException exc) {
            throw new ForbiddenException("Could not create drive service for user " + user.getUserId(), exc);
        }
    }
}
