package edu.rit.honors.gyfp.servlets;

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import edu.rit.honors.gyfp.api.model.TransferRequest;
import edu.rit.honors.gyfp.util.OfyService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Authenticates user and loads transfer requests for TransferRequestView.jsp
 *
 * Created by regdoug on 5/13/15.
 */
public class TransferRequestViewServlet extends HttpServlet {

    public static final Set<String> authorizedUsers = new HashSet<>();

    static {
        authorizedUsers.add("rdp2575@g.rit.edu");
        authorizedUsers.add("gjd6793@g.rit.edu");
    }

    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        UserService userService = UserServiceFactory.getUserService();
        User user = userService.getCurrentUser();
        req.setAttribute("user",user);
        if(null == user) {
            req.setAttribute("loginURL", userService.createLoginURL(req.getRequestURI()));
        } else {
            req.setAttribute("logoutURL",userService.createLogoutURL(req.getRequestURI()));
            boolean validUser = true || authorizedUsers.contains(user.getEmail());
            req.setAttribute("validuser", validUser);
            if (validUser) {
                List<TransferRequest> transferRequests = OfyService.ofy().load().type(TransferRequest.class).list();
                req.setAttribute("trlist", transferRequests);
            }
        }
        req.getRequestDispatcher("/TransferRequestView.jsp").forward(req,resp);
    }
}
