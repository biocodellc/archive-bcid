package rest;

import auth.authenticator;
import auth.authorizer;
import auth.oauth2.provider;
import util.queryParams;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.net.URI;

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
                    if (return_to != null) {
                        res.sendRedirect("/bcid/secure/profile.jsp?error=Update Your Password" + new queryParams().getQueryParams(req.getParameterMap(), false));
                        return;
                    } else {
                        res.sendRedirect("/bcid/secure/profile.jsp?error=Update Your Password");
                        return;
                    }
                }

                // redirect to return_to uri if provided
                if (return_to != null) {

                    res.sendRedirect(return_to + new queryParams().getQueryParams(req.getParameterMap(), true));
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

        if (return_to != null) {
            res.sendRedirect("/bcid/login.jsp?error=bad_credentials" + new queryParams().getQueryParams(req.getParameterMap(), false));
            return;
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
        Object username = session.getAttribute("user");

        try {
            provider p = new provider();

            if (redirectURL == null) {
                String callback = p.getCallback(clientId);

                if (callback != null) {
                    response.sendRedirect(callback + "?error=invalid_request");
                    return;
                }
                response.sendError(400, "invalid_request");
                return;
            }

            if (clientId == null || !p.validClientId(clientId)) {
                redirectURL += "?error=unauthorized_client";
                response.sendRedirect(redirectURL);
                return;
            }

            if (username == null) {
                // need the user to login
                response.sendRedirect("/bcid/login.jsp?return_to=/id/authenticationService/oauth/authorize?"
                                      + request.getQueryString());
                return;
            }
            //TODO ask user if they want to share with request.host

            String code = p.generateCode(clientId, redirectURL, username.toString());

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
        if (redirectURL == null) {
            response.sendError(500);
        } else {
            response.sendRedirect(redirectURL + "?error=server_error");
        }
        return;
    }
    @POST
    @Path("/oauth/access_token")
    @Produces(MediaType.APPLICATION_JSON)
    public Response access_token(@FormParam("code") String code,
                                 @FormParam("client_id") String clientId,
                                 @FormParam("client_secret") String clientSecret,
                                 @FormParam("redirect_uri") String redirectURL,
                                 @FormParam("state") String state)
        throws IOException {
        try {
            provider p = new provider();

            if (redirectURL == null) {
                return Response.status(400).entity("[{\"error\": \"invalid_request\"}]").build();
            }
            // TODO redirect_uri must match redirect_uri in authorize
            URI url = new URI(redirectURL);

            if (clientId == null || clientSecret == null || !p.validateClient(clientId, clientSecret)) {
                return Response.status(400).entity("[{\"error\": \"invalid_client\"}]").location(url).build();
            }

            if (code == null || !p.validateCode(clientId, code, redirectURL)) {
                return Response.status(400).entity("[{\"error\": \"invalid_grant\"}]").location(url).build();
            }

            return Response.ok(p.generateToken(clientId, state, code)).location(url).build();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Response.status(500).entity("[{}]").build();
    }
}
