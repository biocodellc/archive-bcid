package rest;

import auth.authenticator;
import auth.authorizer;
import auth.oauth2.provider;
import util.errorInfo;
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
import java.net.URL;

/**
 * REST interface for handling user authentication
 */
@Path("authenticationService")
public class authenticationService {

    @Context
    static HttpServletRequest request;

    /**
     * Service to log a user into the bcid system
     * @param usr
     * @param pass
     * @param return_to the url to return to after login
     * @throws IOException
     */
    @POST
    @Path("/login")
    @Produces(MediaType.TEXT_HTML)
    public void login(@FormParam("username") String usr,
                      @FormParam("password") String pass,
                      @QueryParam("return_to") String return_to,
                      @Context HttpServletResponse res)
        throws IOException{

        if (!usr.isEmpty() && !pass.isEmpty()) {
            authenticator authenticator = new auth.authenticator();
            // Verify that the entered and stored passwords match
            Boolean isAuthenticated = authenticator.login(usr, pass);

            HttpSession session = request.getSession();

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
                        res.sendRedirect("/bcid/secure/profile.jsp?error=Update Your Password" + new queryParams().getQueryParams(request.getParameterMap(), false));
                        return;
                    } else {
                        res.sendRedirect("/bcid/secure/profile.jsp?error=Update Your Password");
                        return;
                    }
                }

                // redirect to return_to uri if provided
                if (return_to != null) {

                    res.sendRedirect(return_to + new queryParams().getQueryParams(request.getParameterMap(), true));
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
            res.sendRedirect("/bcid/login.jsp?error=bad_credentials" + new queryParams().getQueryParams(request.getParameterMap(), false));
            return;
        }
        res.sendRedirect("/bcid/login.jsp?error");
    }

    /**
     * Service to log a user out of the bcid system
     * @throws IOException
     */
    @GET
    @Path("/logout")
    @Produces(MediaType.TEXT_HTML)
    public void logout(@Context HttpServletResponse res)
            throws IOException{

        HttpSession session = request.getSession();

        session.invalidate();
        res.sendRedirect("/bcid/index.jsp");
        return;
    }

    /**
     * Service for a client app to log a user into the bcid system via oauth.
     * @param clientId
     * @param redirectURL
     * @param state
     * @param response
     */
    @GET
    @Path("/oauth/authorize")
    @Produces(MediaType.TEXT_HTML)
    public void authorize(@QueryParam("client_id") String clientId,
                          @QueryParam("redirect_uri") String redirectURL,
                          @QueryParam("state") String state,
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
            //TODO ask user if they want to share profile information with requesting party

            String code = p.generateCode(clientId, redirectURL, username.toString());

            redirectURL += "?code=" + code;

            if (state != null) {
                redirectURL += "&state=" + state;
            }
            response.sendRedirect(redirectURL);
            return;
        } catch(Exception e) {
            response.sendError(500, "server error");
        }
    }

    /**
     * Service for a client app to exchange an oauth code for an access token
     * @param code
     * @param clientId
     * @param clientSecret
     * @param redirectURL
     * @param state
     * @return
     */
    @POST
    @Path("/oauth/access_token")
    @Produces(MediaType.APPLICATION_JSON)
    public Response access_token(@FormParam("code") String code,
                                 @FormParam("client_id") String clientId,
                                 @FormParam("client_secret") String clientSecret,
                                 @FormParam("redirect_uri") String redirectURL,
                                 @FormParam("state") String state) {
        try {
            provider p = new provider();

            if (redirectURL == null) {
                return Response.status(400).entity("{\"error\": \"invalid_request\"}").build();
            }
            URI url = new URI(redirectURL);

            if (clientId == null || clientSecret == null || !p.validateClient(clientId, clientSecret)) {
                return Response.status(400).entity("{\"error\": \"invalid_client\"}").location(url).build();
            }

            if (code == null || !p.validateCode(clientId, code, redirectURL)) {
                return Response.status(400).entity("{\"error\": \"invalid_grant\"}").location(url).build();
            }

            return Response.ok(p.generateToken(clientId, state, code))
                    .header("Cache-Control", "no-store")
                    .header("Pragma", "no-cache")
                    .location(url)
                    .build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(500).entity(new errorInfo(e, request).toJSON()).build();
        }
    }

    /**
     * Service for an oauth client app to exchange a refresh token for a valid access token.
     * @param clientId
     * @param clientSecret
     * @param refreshToken
     * @return
     */
    @POST
    @Path("/oauth/refresh")
    @Produces(MediaType.APPLICATION_JSON)
    public Response refresh(@FormParam("client_id") String clientId,
                            @FormParam("client_secret") String clientSecret,
                            @FormParam("refresh_token") String refreshToken) {
        try {
            provider p = new provider();

            if (clientId == null || clientSecret == null || !p.validateClient(clientId, clientSecret)) {
                return Response.status(400).entity("{\"error\": \"invalid_client\"}").build();
            }

            if (refreshToken == null || !p.validateRefreshToken(refreshToken)) {
                return Response.status(400).entity("{\"error\": \"invalid_grant\"}").build();
            }

            String accessToken = p.generateToken(refreshToken);

            // refresh tokens are only good once, so delete the old access token so the refresh token can no longer be used
            p.deleteAccessToken(refreshToken);

            return Response.ok(accessToken)
                    .header("Cache-Control", "no-store")
                    .header("Pragma", "no-cache")
                    .build();

        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(500).entity(new errorInfo(e, request).toJSON()).build();
        }
    }

    /**
     * Service for a user to exchange their reset token in order to update their password
     * @param password
     * @param token
     * @param response
     * @throws Exception
     */
    @POST
    @Path("/reset")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.TEXT_HTML)
    public void resetPassword(@FormParam("password") String password,
                              @FormParam("token") String token,
                              @Context HttpServletResponse response)
        throws Exception {
        if (token == null) {
            response.sendRedirect("/bcid/resetPass.jsp?error=Invalid Reset Token");
            return;
        }

        if (password.isEmpty()) {
            response.sendRedirect("/bcid/resetPass.jsp?error=Invalid Password");
            return;
        }

        authorizer authorizer = new authorizer();
        authenticator authenticator = new authenticator();

        if (!authorizer.validResetToken(token)) {
            response.sendRedirect("/bcid/resetPass.jsp?error=Expired Reset Token");
            return;
        }

        if (authenticator.resetPass(token, password)) {
            response.sendRedirect("/bcid/login.jsp");
            return;
        }
    }

    /**
     * Service for a user to request that a password reset token is sent to their email
     * @param username
     * @return
     */
    @POST
    @Path("/sendResetToken")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response sendResetToken(@FormParam("username") String username) {

        if (username.isEmpty()) {
            return Response.status(400).entity("{\"error\": \"User not found\"}").build();
        }
        try {
            authenticator a = new authenticator();
            String email = a.sendResetToken(username);

            if (email != null) {
                return Response.ok("{\"success\": \"" + email + "\"}").build();
            } else {
                return Response.status(400).entity("{\"error\": \"User not found\"}").build();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(500).entity(new errorInfo(e, request).toJSON()).build();
        }
    }
}
