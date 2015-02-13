package rest;

import auth.authenticator;
import auth.authorizer;
import auth.oauth2.provider;
import bcidExceptions.OAUTHException;
import bcidExceptions.BadRequestException;
import bcidExceptions.ServerErrorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.SettingsManager;
import util.errorInfo;
import util.queryParams;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * REST interface for handling user authentication
 */
@Path("authenticationService")
public class authenticationService {

    @Context
    static HttpServletRequest request;
    private static Logger logger = LoggerFactory.getLogger(authenticationService.class);

    static SettingsManager sm;
    @Context
    static ServletContext context;

    /**
     * Load settings manager
     */
    static {
        // Initialize settings manager
        sm = SettingsManager.getInstance();
        sm.loadProperties();
    }

    /**
     * Service to log a user into the bcid system
     *
     * @param usr
     * @param pass
     * @param return_to the url to return to after login
     *
     * @throws IOException
     */
    @POST
    @Path("/login")
    @Produces(MediaType.APPLICATION_JSON)
    public Response login(@FormParam("username") String usr,
                          @FormParam("password") String pass,
                          @QueryParam("return_to") String return_to,
                          @Context HttpServletResponse res) {

        if (!usr.isEmpty() && !pass.isEmpty()) {
            authenticator authenticator = new auth.authenticator();
            Boolean isAuthenticated;

            // Verify that the entered and stored passwords match
            isAuthenticated = authenticator.login(usr, pass);
            HttpSession session = request.getSession();

            logger.debug("BCID SESS_DEBUG login: sessionid=" + session.getId());

            if (isAuthenticated) {
                // Place the user in the session
                session.setAttribute("user", usr);
                authorizer myAuthorizer = null;

                myAuthorizer = new auth.authorizer();

                // Check if the user is an admin for any projects
                if (myAuthorizer.userProjectAdmin(usr)) {
                    session.setAttribute("projectAdmin", true);
                }

                myAuthorizer.close();

                // Check if the user has created their own password, if they are just using the temporary password, inform the user to change their password
                if (!authenticator.userSetPass(usr)) {
                    // don't need authenticator anymore
                    authenticator.close();

                    return Response.ok("{\"url\": \"/bcid/secure/profile.jsp?error=Update Your Password" +
                                    new queryParams().getQueryParams(request.getParameterMap(), false) + "\"}")
                                    .build();
                } else {
                    // don't need authenticator anymore
                    authenticator.close();
                }


                // Redirect to return_to uri if provided
                if (return_to != null) {
                    return Response.ok("{\"url\": \"" + return_to +
                                new queryParams().getQueryParams(request.getParameterMap(), true) + "\"}")
                            .build();
                } else {
                    return Response.ok("{\"url\": \"/bcid/index.jsp\"}").build();
                }
            }
            // stored and entered passwords don't match, invalidate the session to be sure that a user is not in the session
            else {
                session.invalidate();
                authenticator.close();
            }
        }

        return Response.status(400)
                .entity(new errorInfo("Bad Credentials", 400).toJSON())
                .build();
    }

    /**
     * Service to log a user into the bcid system using LDAP
     *
     * @param usr
     * @param pass
     * @param return_to the url to return to after login
     *
     * @throws IOException
     */
    @POST
    @Path("/loginLDAP")
    @Produces(MediaType.APPLICATION_JSON)
    public Response loginLDAP(@FormParam("username") String usr,
                              @FormParam("password") String pass,
                              @QueryParam("return_to") String return_to,
                              @Context HttpServletResponse res) {

        if (!usr.isEmpty() && !pass.isEmpty()) {
            authenticator authenticator = new auth.authenticator();
            Boolean isAuthenticated = false;

            // Verify that the entered and stored passwords match
            isAuthenticated = authenticator.loginLDAP(usr, pass, true);
            HttpSession session = request.getSession();

            if (isAuthenticated) {
                // Place the user in the session
                session.setAttribute("user", usr);
                authorizer myAuthorizer = null;

                myAuthorizer = new auth.authorizer();
                // Check if the user is an admin for any projects
                if (myAuthorizer.userProjectAdmin(usr)) {
                    session.setAttribute("projectAdmin", true);
                }
                myAuthorizer.close();

                // Redirect to return_to uri if provided
//                if (return_to != null) {
//                    res.sendRedirect(return_to + new queryParams().getQueryParams(request.getParameterMap(), true));
//                    return;
//                } else {
//                    res.sendRedirect("/bcid/index.jsp");
//                    return;
//                }
                authenticator.close();
                if (return_to != null) {
                    return Response.ok("{\"url\": \"" + return_to +
                            new queryParams().getQueryParams(request.getParameterMap(), true) + "\"}")
                            .build();
                } else {
                    return Response.ok("{\"url\": \"/bcid/index.jsp\"}").build();
                }
            }
            // stored and entered passwords don't match, invalidate the session to be sure that a user is not in the session
            else {
                authenticator.close();
                session.invalidate();
            }
            // Shouldn't need this anymore due to new error handling
            // Check for error message on LDAP
//            if (authenticator.getLdapAuthentication() != null) {
//                System.out.println("start6");
//                if (authenticator.getLdapAuthentication().getStatus() != authenticator.getLdapAuthentication().SUCCESS) {
//                    res.sendRedirect("/bcid/login.jsp?error=" + authenticator.getLdapAuthentication().getMessage() + new queryParams().getQueryParams(request.getParameterMap(), false));
//                    System.out.println("start8");
//                    return;
//                }
//            }
        }

//        if (return_to != null) {
//            res.sendRedirect("/bcid/login.jsp?error=bad_credentials" + new queryParams().getQueryParams(request.getParameterMap(), false));
//            return;
//        }
//        res.sendRedirect("/bcid/login.jsp?error");
        return Response.status(400)
                .entity(new errorInfo("Bad Credentials", 400).toJSON())
                .build();
    }

    /**
     * Service to log a user into the bcid system using RADIUS server
     *
     * @param usr
     * @param pass
     * @param return_to the url to return to after login
     *
     * @throws IOException
     */
    @POST
    @Path("/loginRadius")
    @Produces(MediaType.APPLICATION_JSON)
    public Response loginRadius(@FormParam("username") String usr,
                                @FormParam("password") String pass,
                                @QueryParam("return_to") String return_to,
                                @Context HttpServletResponse res) {

        if (!usr.isEmpty() && !pass.isEmpty()) {
            authenticator authenticator = new auth.authenticator();
            Boolean isAuthenticated;

            // Verify that the entered and stored passwords match
            isAuthenticated = authenticator.loginRadius(usr, pass);
            HttpSession session = request.getSession();

            logger.debug("BCID SESS_DEBUG login: sessionid=" + session.getId());

            if (isAuthenticated) {
                // Place the user in the session
                session.setAttribute("user", usr);
                authorizer myAuthorizer = null;

                myAuthorizer = new auth.authorizer();

                // Check if the user is an admin for any projects
                if (myAuthorizer.userProjectAdmin(usr)) {
                    session.setAttribute("projectAdmin", true);
                }

                myAuthorizer.close();

                // Check if the user has created their own password, if they are just using the temporary password, inform the user to change their password
                if (!authenticator.userSetPass(usr)) {
                    // don't need authenticator anymore
                    authenticator.close();

                    return Response.ok("{\"url\": \"/bcid/secure/profile.jsp?error=Update Your Password" +
                            new queryParams().getQueryParams(request.getParameterMap(), false) + "\"}")
                            .build();
                } else {
                    // don't need authenticator anymore
                    authenticator.close();
                }


                // Redirect to return_to uri if provided
                if (return_to != null) {
                    return Response.ok("{\"url\": \"" + return_to +
                            new queryParams().getQueryParams(request.getParameterMap(), true) + "\"}")
                            .build();
                } else {
                    return Response.ok("{\"url\": \"/bcid/index.jsp\"}").build();
                }
            }
            // stored and entered passwords don't match, invalidate the session to be sure that a user is not in the session
            else {
                session.invalidate();
                authenticator.close();
            }
        }

        return Response.status(400)
                .entity(new errorInfo("Bad Credentials", 400).toJSON())
                .build();
    }

    /**
     * Service to log a user out of the bcid system
     *
     * @throws IOException
     */
    @GET
    @Path("/logout")
    @Produces(MediaType.TEXT_HTML)
    public Response logout(@QueryParam("redirect_uri") String redirect_uri,
                           @Context HttpServletResponse res) {

        HttpSession session = request.getSession();

        session.invalidate();

        if (redirect_uri != null && !redirect_uri.equals("")) {
            try {
                return Response.status(307)
                        .location(new URI(redirect_uri))
                        .build();
            } catch (URISyntaxException e) {
                throw new BadRequestException("invalid redirect_uri");
            }
        } else {
            try {
                return Response.status(307)
                        .location(new URI("../bcid/index.jsp"))
                        .build();
            } catch (URISyntaxException e) {
                throw new ServerErrorException(e);
            }
        }
    }

    /**
     * Service for a client app to log a user into the bcid system via oauth.
     *
     * @param clientId
     * @param redirectURL
     * @param state
     * @param response
     */
    @GET
    @Path("/oauth/authorize")
    @Produces(MediaType.TEXT_HTML)
    public Response authorize(@QueryParam("client_id") String clientId,
                              @QueryParam("redirect_uri") String redirectURL,
                              @QueryParam("state") String state,
                              @Context HttpServletResponse response) {
        HttpSession session = request.getSession();
        Object username = session.getAttribute("user");

        provider p = new provider();

        if (redirectURL == null) {
            String callback = null;
            try {
                callback = p.getCallback(clientId);
            } catch (OAUTHException e) {
                logger.warn("OAUTHException retrieving callback for OAUTH clientID {}", clientId, e);
            }

            if (callback != null) {
                try {
                    p.close();
                    return Response.status(302).location(new URI(callback + "?error=invalid_request")).build();
                } catch (URISyntaxException e) {
                    logger.warn("Malformed callback URI for oauth client {} and callback {}", clientId, callback);
                }
            }
            p.close();
            throw new BadRequestException("invalid_request");
        }

        if (clientId == null || !p.validClientId(clientId)) {
            redirectURL += "?error=unauthorized_client";
            try {
                p.close();
                return Response.status(302).location(new URI(redirectURL)).build();
            } catch (URISyntaxException e) {
                p.close();
                throw new BadRequestException("invalid_request", "invalid redirect_uri provided");
            }
        }

        if (username == null) {
            // need the user to login
            try {
                p.close();
                return Response.status(Response.Status.TEMPORARY_REDIRECT)
                        .location(new URI("../bcid/login.jsp?return_to=/id/authenticationService/oauth/authorize?"
                                    + request.getQueryString()))
                        .build();
            } catch (URISyntaxException e) {
                p.close();
                throw new ServerErrorException(e);
            }
        }
        //TODO ask user if they want to share profile information with requesting party
        String code = p.generateCode(clientId, redirectURL, username.toString());
        p.close();

        redirectURL += "?code=" + code;

        if (state != null) {
            redirectURL += "&state=" + state;
        }
        try {
            return Response.status(302)
                    .location(new URI(redirectURL))
                    .build();
        } catch (URISyntaxException e) {
            throw new BadRequestException("invalid_request", "invalid redirect_uri provided");
        }
    }

    /**
     * Service for a client app to exchange an oauth code for an access token
     *
     * @param code
     * @param clientId
     * @param clientSecret
     * @param redirectURL
     * @param state
     *
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
        provider p = null;
        p = new provider();
        if (redirectURL == null) {
            throw new BadRequestException("invalid_request", "redirect_uri is null");
        }
        URI url = null;
        try {
            url = new URI(redirectURL);
        } catch (URISyntaxException e) {
            logger.warn("URISyntaxException for the following url: {}", redirectURL, e);
            p.close();
            throw new BadRequestException("invalid_request", "URISyntaxException thrown with the following redirect_uri: " + redirectURL);
        }

        if (clientId == null || clientSecret == null || !p.validateClient(clientId, clientSecret)) {
            p.close();
            throw new BadRequestException("invalid_client");
        }

        if (code == null || !p.validateCode(clientId, code, redirectURL)) {
            p.close();
            throw new BadRequestException("invalid_grant", "Either code was null or the code doesn't match the clientId");
        }
        String response = p.generateToken(clientId, state, code);
        p.close();

        return Response.ok(response)
                .header("Cache-Control", "no-store")
                .header("Pragma", "no-cache")
                .location(url)
                .build();
    }

    /**
     * Service for an oauth client app to exchange a refresh token for a valid access token.
     *
     * @param clientId
     * @param clientSecret
     * @param refreshToken
     *
     * @return
     */
    @POST
    @Path("/oauth/refresh")
    @Produces(MediaType.APPLICATION_JSON)
    public Response refresh(@FormParam("client_id") String clientId,
                            @FormParam("client_secret") String clientSecret,
                            @FormParam("refresh_token") String refreshToken) {
        provider p = new provider();

        if (clientId == null || clientSecret == null || !p.validateClient(clientId, clientSecret)) {
            p.close();
            throw new BadRequestException("invalid_client");
        }

        if (refreshToken == null || !p.validateRefreshToken(refreshToken)) {
            p.close();
            throw new BadRequestException("invalid_grant", "refresh_token is invalid");
        }

        String accessToken = p.generateToken(refreshToken);

        // refresh tokens are only good once, so delete the old access token so the refresh token can no longer be used
        p.deleteAccessToken(refreshToken);
        p.close();

        return Response.ok(accessToken)
                .header("Cache-Control", "no-store")
                .header("Pragma", "no-cache")
                .build();
    }

    /**
     * Service for a user to exchange their reset token in order to update their password
     *
     * @param password
     * @param token
     * @param response
     *
     * @throws IOException
     */
    @POST
    @Path("/reset")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.TEXT_HTML)
    public void resetPassword(@FormParam("password") String password,
                              @FormParam("token") String token,
                              @Context HttpServletResponse response)
        throws IOException {
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
            authenticator.close();
            authorizer.close();
            return;
        }
        authorizer.close();

        if (authenticator.resetPass(token, password)) {
            response.sendRedirect("/bcid/login.jsp");
            authenticator.close();
            return;
        }
        authenticator.close();
    }

    /**
     * Service for a user to request that a password reset token is sent to their email
     *
     * @param username
     *
     * @return
     */
    @POST
    @Path("/sendResetToken")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response sendResetToken(@FormParam("username") String username) {

        if (username.isEmpty()) {
            throw new BadRequestException("User not found.", "username is null");
        }
        authenticator a = new authenticator();
        String email = a.sendResetToken(username);
        a.close();
        if (email != null) {
            return Response.ok("{\"success\": \"" + email + "\"}").build();
        } else {
            throw new BadRequestException("User not found.");
        }
    }
}
