package rest;

import auth.authenticator;
import bcid.database;
import bcid.projectMinter;
import bcid.userMinter;
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
    public String createUser(@FormParam("username") String username,
                             @FormParam("password") String password,
                             @FormParam("firstName") String firstName,
                             @FormParam("lastName") String lastName,
                             @FormParam("email") String email,
                             @FormParam("institution") String institution,
                             @FormParam("project_id") Integer projectId) {

        HttpSession session = request.getSession();

        if (session.getAttribute("projectAdmin") == null) {
            // only project admins are able to create users
            return "{\"error\": \"only project admins are able to create users\"}";
        }

        if ((username == null || username.isEmpty()) ||
                (password == null || password.isEmpty()) ||
                (firstName == null || firstName.isEmpty()) ||
                (lastName == null || lastName.isEmpty()) ||
                (email == null || email.isEmpty()) ||
                (institution == null) || institution.isEmpty()) {
            return "{\"error\": \"all fields are required\"}";
        }

        // check that a valid email is given
        if (!email.toUpperCase().matches("[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,4}")) {
            return "{\"error\": \"please enter a valid email\"}";
        }

        Hashtable<String, String> userInfo = new Hashtable<String, String>();
        userInfo.put("username", username);
        userInfo.put("firstName", firstName);
        userInfo.put("lastName", lastName);
        userInfo.put("email", email);
        userInfo.put("institution", institution);
        userInfo.put("password", password);

        try {
            userMinter u = new userMinter();
            projectMinter p = new projectMinter();
            String admin = session.getAttribute("user").toString();
            database db = new database();

            if (u.checkUsernameExists(username)) {
                return "{\"error\": \"username already exists\"}";
            }
            // check if the user is this project's admin
            if (!p.userProjectAdmin(db.getUserId(admin), projectId)) {
                return "{\"error\": \"you can't add a user to a project that you're not an admin\"}";
            }

            return u.createUser(userInfo, projectId);
        } catch (Exception e) {
            e.printStackTrace();
            return new errorInfo(e, request).toJSON();
        }
    }

    /**
     * Returns an HTML table in order to create a user.
     * @return
     */
    @GET
    @Path("/createFormAsTable")
    public String createFormAsTable() {
        try {
            userMinter u = new userMinter();
            return u.getCreateForm();
        } catch (Exception e) {
            e.printStackTrace();
            return new errorInfo(e, request).toHTMLTable();
        }
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
    public String adminUpdateProfile(@FormParam("firstName") String firstName,
                                     @FormParam("lastName") String lastName,
                                     @FormParam("email") String email,
                                     @FormParam("institution") String institution,
                                     @FormParam("new_password") String new_password,
                                     @PathParam("username") String username) {
        HttpSession session = request.getSession();
        String error = "";
        Hashtable<String, String> update = new Hashtable<String, String>();

        if (session.getAttribute("projectAdmin") == null) {
            return "{\"error\": \"you must be a project admin to edit another user's profile\"}";
        }

        // set new password if given
        if (!new_password.isEmpty()) {
            authenticator authenticator = new authenticator();
            Boolean success = authenticator.setHashedPass(username, new_password);
            if (!success) {
                error = "server error hashing password";
            } else {
                // Make the user change their password next time they login
                update.put("set_password", "0");
            }
        }

        // Check if any other fields should be updated
        try {
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

        } catch (Exception e) {
            e.printStackTrace();
            return new errorInfo(e, request).toJSON();
        }

        if (error.isEmpty()) {
            return "[{\"success\": \"true\"}]";
        } else {
            return "{\"error\": \"" + error + "\"}";
        }
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
            throws IOException{

        HttpSession session = request.getSession();
        String username = session.getAttribute("user").toString();
        String error = "";
        Hashtable<String, String> update = new Hashtable<String, String>();

        // Only update user's password if both old_password and new_password fields contain values
        if (!old_password.isEmpty() && !new_password.isEmpty()) {
            authenticator authenticator = new authenticator();
            // Call the login function to verify the user's old_password
            Boolean valid_pass = authenticator.login(username, old_password);

            // If user's old_password matches stored pass, then update the user's password to the new value
            if (valid_pass) {
                Boolean success = authenticator.setHashedPass(username, new_password);
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
        }

        database db;

        // Check if any other fields should be updated
        try {
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
        } catch (Exception e) {
            e.printStackTrace();
            error = new errorInfo(e, request).toJSON();
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

        try {
            userMinter u = new userMinter();
            return u.getProfileEditorAsTable(username, true);
        } catch (Exception e) {
            e.printStackTrace();
            return new errorInfo(e, request).toHTMLTable();
        }
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

        userMinter u;

        try {
            u = new userMinter();
            return u.getProfileEditorAsTable(username.toString(), false);
        }   catch (Exception e) {
            e.printStackTrace();
            return new errorInfo(e, request).toHTMLTable();
        }
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

        try {
            u = new userMinter();
            return u.getProfileHTML(username.toString());
        } catch (Exception e) {
            e.printStackTrace();
            return new errorInfo(e, request).toHTMLTable();
        }
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
            try {
                userMinter u = new userMinter();

                return Response.ok(u.getOauthProfile(access_token)).build();
            } catch (Exception e) {
                e.printStackTrace();
                return Response.status(500).entity(new errorInfo(e, request).toJSON()).build();
            }
        }
        return Response.status(400).entity("{\"error\": \"invalid_grant\"}").build();
    }
}
