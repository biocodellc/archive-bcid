package rest;

import auth.authenticator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import java.io.IOException;

/**
 * REST interface to log a user in
 * Created by rjewing on 1/14/14.
 */
@Path("loginService")
public class loginService {

    @POST
    public void login(@FormParam("username") String usr,
                      @FormParam("password") String pass,
                      @Context HttpServletRequest req,
                      @Context HttpServletResponse res)
        throws IOException{

        if (!usr.isEmpty() && !pass.isEmpty()) {
            authenticator authenticator = new auth.authenticator(usr, pass);
            // Verify that the entered and stored passwords match
            Boolean isAuthenticated = authenticator.login();

            HttpSession session = req.getSession();

            if (isAuthenticated) {
                // Place the user in the session
                session.setAttribute("user", usr);

                // Check if the user has created their own password, if they are just using the temporary password, inform the user to change their password
                if (!authenticator.userSetPass()) {
                    res.sendRedirect("/bcid/secure/profile.jsp?error=Update Your Password");
                    return;
                }
                res.sendRedirect("/bcid/index.jsp");
                return;
            }
            // stored and entered passwords don't match, invalidate the session to be sure that a user is not in the session
            else {
                session.invalidate();
            }
        }

        res.sendRedirect("/bcid/login.jsp?error");
    }
}
