package edu.rit.honors.gyfp.servlets;

import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.api.client.auth.oauth2.AuthorizationCodeResponseUrl;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.appengine.auth.oauth2.AbstractAppEngineAuthorizationCodeCallbackServlet;
import edu.rit.honors.gyfp.util.Utils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class Oauth2Callback extends
        AbstractAppEngineAuthorizationCodeCallbackServlet {

    private static final long serialVersionUID = -8146690178186062866L;

    @Override
    protected void onSuccess(HttpServletRequest req, HttpServletResponse resp, Credential credential)
            throws ServletException, IOException {
        resp.sendRedirect(Utils.MAIN_SERVLET_PATH);
    }

    @Override
    protected void onError(
            HttpServletRequest req, HttpServletResponse resp, AuthorizationCodeResponseUrl errorResponse)
            throws ServletException, IOException {

        // Redirect them to the list page after the callback.
        resp.sendRedirect("/#/install/error");
    }

    @Override
    protected AuthorizationCodeFlow initializeFlow() throws ServletException, IOException {
        return Utils.initializeFlow();
    }

    @Override
    protected String getRedirectUri(HttpServletRequest req) throws ServletException, IOException {
        return Utils.getRedirectUri(req);
    }

}
