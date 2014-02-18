package rest;

import auth.authenticator;
import auth.authorizer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.io.IOException;

/**
 * REST interface to log a user in
 * Created by rjewing on 1/14/14.
 */
@Path("authenticationService")
public class authenticationService {

    @POST
    @Path("/login")
    @Produces(MediaType.TEXT_HTML)
    public void login(@FormParam("username") String usr,
                      @FormParam("password") String pass,
                      @Context HttpServletRequest req,
                      @Context HttpServletResponse res)
        throws IOException{

        if (!usr.isEmpty() && !pass.isEmpty()) {
            authenticator authenticator = new auth.authenticator();
            // Verify that the entered and stored passwords match
            Boolean isAuthenticated = authenticator.login(usr, pass);

            HttpSession session = req.getSession();

            if (isAuthenticated) {
                // Place the user in the session
                session.setAttribute("user", usr);

                try {
                    authorizer authorizer = new auth.authorizer();

                    // Check if the user is an admin for any projects
                    if (authorizer.userProjectAdmin(usr)) {
                        session.setAttribute("projectAdmin", true);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }

                // Check if the user has created their own password, if they are just using the temporary password, inform the user to change their password
                if (!authenticator.userSetPass(usr)) {
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

    @GET
    @Path("/logout")
    @Produces(MediaType.TEXT_HTML)
    public void logout(@Context HttpServletRequest req,
                       @Context HttpServletResponse res)
            throws IOException{

        HttpSession session = req.getSession();

        session.invalidate();
        res.sendRedirect("/bcid/index.jsp");
    }
}
