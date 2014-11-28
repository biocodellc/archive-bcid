package rest;

import auth.authenticator;
import bcid.BCIDException;
import bcid.database;
import bcid.projectMinter;
import bcid.userMinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import java.util.Hashtable;

/**
 * The REST Interface for dealing with users. Includes user creation and profile updating.
 */
@Path("userService")
public class userService {

    @Context
    static HttpServletRequest request;

    private static Logger logger = LoggerFactory.getLogger(userService.class);

    /**
     * Service to create a new user.
     * @param username
     * @param password
     * @param firstName
     * @param lastName
     * @param email
     * @param institution
     * @param projectId
     * @return
     */
    @POST
    @Path("/create")
    @Produces(MediaType.APPLICATION_JSON)
    public Response createUser(@FormParam("username") String username,
                               @FormParam("password") String password,
                               @FormParam("firstName") String firstName,
                               @FormParam("lastName") String lastName,
                               @FormParam("email") String email,
                               @FormParam("institution") String institution,
                               @FormParam("project_id") Integer projectId) {

        HttpSession session = request.getSession();

        if (session.getAttribute("projectAdmin") == null) {
            // only project admins are able to create users
            return Response.status(403).entity(new errorInfo("Only project admins are able to create users.", 403)
                    .toJSON()).build();
        }

        if ((username == null || username.isEmpty()) ||
                (password == null || password.isEmpty()) ||
                (firstName == null || firstName.isEmpty()) ||
                (lastName == null || lastName.isEmpty()) ||
                (email == null || email.isEmpty()) ||
                (institution == null) || institution.isEmpty()) {
            return Response.status(400).entity(new errorInfo("all fields are required", 400).toJSON()).build();
        }

        // check that a valid email is given
        if (!email.toUpperCase().matches("[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,4}")) {
            return Response.status(400).entity(new errorInfo("please enter a valid email", 400).toJSON()).build();
        }

        Hashtable<String, String> userInfo = new Hashtable<String, String>();
        userInfo.put("username", username);
        userInfo.put("firstName", firstName);
        userInfo.put("lastName", lastName);
        userInfo.put("email", email);
        userInfo.put("institution", institution);
        userInfo.put("password", password);

        userMinter u = new userMinter();
        projectMinter p = new projectMinter();
        String admin = session.getAttribute("user").toString();
        database db = new database();

        if (u.checkUsernameExists(username)) {
            return Response.status(400).entity(new errorInfo("username already exists", 400).toJSON()).build();
        }
        // check if the user is this project's admin
        if (!p.userProjectAdmin(db.getUserId(admin), projectId)) {
            return Response.status(403).entity(new errorInfo("You can't add a user to a project that you're not an admin.",
                    403).toJSON()).build();
        }

        try {
            return Response.ok(u.createUser(userInfo, projectId)).build();
        } catch (BCIDException e) {
            logger.warn("BCIDException while creating new user.", e);
            return Response.serverError().entity(new errorInfo(e.getMessage(), 500).toJSON()).build();
        }
    }

    /**
     * Returns an HTML table in order to create a user.
     * @return
     */
    @GET
    @Path("/createFormAsTable")
    public String createFormAsTable() {
        userMinter u = new userMinter();
        return u.getCreateForm();
    }

    /**
     * Service for a project admin to update a member user's profile
     * @param firstName
     * @param lastName
     * @param email
     * @param institution
     * @param new_password
     * @param username
     * @return
     */
    @POST
    @Path("/profile/update/{username}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response adminUpdateProfile(@FormParam("firstName") String firstName,
                                       @FormParam("lastName") String lastName,
                                       @FormParam("email") String email,
                                       @FormParam("institution") String institution,
                                       @FormParam("new_password") String new_password,
                                       @PathParam("username") String username) {
        HttpSession session = request.getSession();
        Hashtable<String, String> update = new Hashtable<String, String>();

        if (session.getAttribute("projectAdmin") == null) {
            return Response.status(403).entity(new errorInfo("You must be a project admin to edit another user's profile",
                    403).toJSON()).build();
        }

        // set new password if given
        if (!new_password.isEmpty()) {
            authenticator authenticator = new authenticator();
            Boolean success = authenticator.setHashedPass(username, new_password);
            authenticator.close();
            if (!success) {
                return Response.serverError().entity(new errorInfo("server error hashing password", 500).toJSON())
                        .build();
            } else {
                // Make the user change their password next time they login
                update.put("set_password", "0");
            }
        }

        // Check if any other fields should be updated
        userMinter u = new userMinter();

        if (!firstName.equals(u.getFirstName(username))) {
            update.put("firstName", firstName);
        }
        if (!lastName.equals(u.getLastName(username))) {
            update.put("lastName", lastName);
        }
        if (!email.equals(u.getEmail(username))) {
            // check that a valid email is given
            if (email.toUpperCase().matches("[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,4}")) {
                update.put("email", email);
            } else {
                return Response.status(400).entity(new errorInfo("Please enter a valid email.", 400).toJSON()).build();
            }
        }
        if (!institution.equals(u.getInstitution(username))) {
            update.put("institution", institution);
        }


        if (!update.isEmpty()) {
            Boolean success = u.updateProfile(update, username);
            if (!success) {
                return Response.serverError().entity(new errorInfo("server error updating profile", 500).toJSON()).build();
            }
        }
        return Response.ok("{\"success\": \"true\"}").build();
    }

    /**
     * Service for a user to update their profile.
     * @param firstName
     * @param lastName
     * @param email
     * @param institution
     * @param old_password
     * @param new_password
     * @param return_to
     * @throws IOException
     */
    @POST
    @Path("/profile/update")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.TEXT_HTML)
    public void updateProfile(@FormParam("firstName") String firstName,
                              @FormParam("lastName") String lastName,
                              @FormParam("email") String email,
                              @FormParam("institution") String institution,
                              @FormParam("old_password") String old_password,
                              @FormParam("new_password") String new_password,
                              @QueryParam("return_to") String return_to,
                              @Context HttpServletResponse response)
        throws IOException {

        HttpSession session = request.getSession();
        String username = session.getAttribute("user").toString();
        String error = "";
        Hashtable<String, String> update = new Hashtable<String, String>();

        // Only update user's password if both old_password and new_password fields contain values
        if (!old_password.isEmpty() && !new_password.isEmpty()) {
            authenticator myAuth = new authenticator();
            // Call the login function to verify the user's old_password
            Boolean valid_pass = myAuth.login(username, old_password);

            // If user's old_password matches stored pass, then update the user's password to the new value
            if (valid_pass) {
                Boolean success = myAuth.setHashedPass(username, new_password);
                if (!success) {
                    error = "server error hashing password";
                }
                // Make sure that the set_password field is 1 (true) so they aren't asked to change their password after login
                else {
                    update.put("set_password", "1");
                }
            }
            else {
                error = "Wrong Password";
            }
            myAuth.close();

        }
        database db;

        // Check if any other fields should be updated
        userMinter u = new userMinter();

        if (!firstName.equals(u.getFirstName(username))) {
            update.put("firstName", firstName);
        }
        if (!lastName.equals(u.getLastName(username))) {
            update.put("lastName", lastName);
        }
        if (!email.equals(u.getEmail(username))) {
            // check that a valid email is given
            if (email.toUpperCase().matches("[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,4}")) {
                update.put("email", email);
            } else {
                error = "please enter a valid email";
            }
        }
        if (!institution.equals(u.getInstitution(username))) {
            update.put("institution", institution);
        }

        if (!update.isEmpty()) {
            Boolean success = u.updateProfile(update, username);
            if (!success) {
                error = "server error updating profile";
            }
        }

        if (error.isEmpty()) {
            if (return_to != null) {
                response.sendRedirect(return_to);
            } else {
                response.sendRedirect("/bcid/secure/profile.jsp");
                return;
            }
        }

        // Error occurred somewhere, inform user
        if (return_to != null) {
            response.sendRedirect("/bcid/secure/profile.jsp?error=" + error + new queryParams().getQueryParams(request.getParameterMap(), false));
            return;
        }
        response.sendRedirect("/bcid/secure/profile.jsp?error=" + error);
    }

    /**
     * Returns an HTML table for editing a user's profile. Project admin use only.
     * @param username
     * @return
     */
    @GET
    @Path("/profile/listEditorAsTable/{username}")
    @Produces(MediaType.TEXT_HTML)
    public String getUsersProfile(@PathParam("username") String username) {
        HttpSession session = request.getSession();

        if (session.getAttribute("projectAdmin") == null) {
            return "You must be a project admin to edit a user's profile";
        }

        userMinter u = new userMinter();
        return u.getProfileEditorAsTable(username, true);
    }

    /**
     * returns an HTML table for editing a user's profile.
     * @return
     */
    @GET
    @Path("/profile/listEditorAsTable")
    @Produces(MediaType.TEXT_HTML)
    public String getProfile() {
        HttpSession session = request.getSession();
        Object username = session.getAttribute("user");

        if (username == null) {
            return "You must be logged in to view your profile.";
        }

        userMinter u = new userMinter();
        return u.getProfileEditorAsTable(username.toString(), false);
    }

    /**
     * Return a HTML table displaying the user's profile
     *
     * @return String with HTML response
     */
    @GET
    @Path("/profile/listAsTable")
    @Produces(MediaType.TEXT_HTML)
    public String listUserProfile() {
        HttpSession session = request.getSession();
        Object username = session.getAttribute("user");
        userMinter u;

        if (username == null) {
            return "You must be logged in to view your profile.";
        }

        u = new userMinter();
        return u.getProfileHTML(username.toString());
    }

    /**
     * Service for oauth client apps to retrieve a user's profile information.
     * @param access_token
     * @return
     */
    @GET
    @Path("/oauth")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUserData(@QueryParam("access_token") String access_token) {
        if (access_token != null) {
            userMinter u = new userMinter();
            return Response.ok(u.getOauthProfile(access_token)).build();
        }
        return Response.status(400).entity(new errorInfo("invalid_grant", "access_token was null", 400).toJSON()).build();
    }
}
