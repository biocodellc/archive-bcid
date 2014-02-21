package rest;

import auth.authenticator;
import auth.authorizer;
import auth.oauth2.provider;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;

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
                      @QueryParam("return_to") String return_to,
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

                // redirect to return_to uri if provided
                if (return_to != null) {
                    res.sendRedirect(return_to);
                    return;
                } else {
                    res.sendRedirect("/bcid/index.jsp");
                    return;
                }
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
        return;
    }

    @GET
    @Path("/oauth/authorize")
    @Produces(MediaType.TEXT_HTML)
    public void authorize(@QueryParam("client_id") String clientId,
                                         @QueryParam("redirect_uri") String redirectURL,
                                         @QueryParam("state") String state,
                                         @Context HttpServletRequest request,
                                         @Context HttpServletResponse response)
        throws IOException {
        HttpSession session = request.getSession();

        try {
            provider p = new provider();

            if (clientId == null || !p.validClientId(clientId)) {
                if (redirectURL == null) {
                    response.sendError(401);
                    return;
                }
                redirectURL += "?error=unauthorized_client";
                response.sendRedirect(redirectURL);
                return;
            }

            if (redirectURL == null) {
                redirectURL = p.getCallback(clientId);
            }

            if (session.getAttribute("user") == null) {
                // need the user to login
                response.sendRedirect("/bcid/login.jsp?return_to=/id/authenticationService/oauth/authorize?"
                                      + request.getQueryString());
                return;
            }
            //TODO ask user if they want to share with request.host

            String code = p.generateCode(clientId);

            redirectURL += "?code=" + code;

            if (state != null) {
                redirectURL += "&state=" + state;
            }
            response.sendRedirect(redirectURL);
            return;

        } catch (Exception e) {
            e.printStackTrace();
        }

        // If we are here, there was a server error
        response.sendError(500);
        return;
    }
    @POST
    @Path("/oauth/access_token")
    @Produces(MediaType.APPLICATION_JSON)
    public String access_token(@FormParam("code") String code,
                               @FormParam("client_id") String clientId,
                               @FormParam("client_secret") String clientSecret,
                               @FormParam("redirect_uri") String redirectURL,
                               @Context HttpServletResponse response,
                               @Context HttpServletResponse request)
        throws IOException {
        try {
            provider p = new provider();

            if (clientId == null || clientSecret == null || !p.validateClient(clientId, clientSecret)) {
                response.setStatus(400);
                // response.sendRedirect?
                return "[{\"error\": \"invalid_client\"}]";
            }

            if (code == null || !p.validateCode(clientId, code)) {
                response.setStatus(400);
                return "[{\"error\": \"invalid_grant\"}]";
            }
            // TODO if redirect_uri in authorize, then must match here

            return p.generateToken(clientId);
        } catch (Exception e) {
            e.printStackTrace();
        }
        response.setStatus(500);
        return "[{}]";
    }
}
