package rest;

import auth.authenticator;
import bcid.database;
import bcid.userMinter;
import util.queryParams;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.Hashtable;

/**
 * Created by rjewing on 2/11/14.
 */
@Path("userService")
public class userService {

    @GET
    @Path("/list")
    @Produces(MediaType.APPLICATION_JSON)
    public String getSystemUsers(@Context HttpServletRequest request)
            throws Exception {
        HttpSession session = request.getSession();

        if (session.getAttribute("projectAdmin") == null) {
            // only display system users to project admins
            return "[{}]";
        }

        userMinter u = new userMinter();
        return u.listSystemUsers();
    }

    @POST
    @Path("/add")
    public void addUser(@FormParam("projectId") Integer projectId,
                        @FormParam("userId") Integer userId,
                        @Context HttpServletRequest request,
                        @Context HttpServletResponse response)
        throws IOException {

        HttpSession session = request.getSession();
        Boolean success = false;

        if (session.getAttribute("projectAdmin") == null) {
            response.sendError(403);
            return;
        }

        try {
            userMinter u = new userMinter();
            success = u.addUserToProject(userId, projectId);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (success) {
            response.sendRedirect("/bcid/secure/user.jsp");
            return;
        }

        response.sendRedirect("/bcid/secure/user.jsp?addError");
    }

    @POST
    @Path("/create")
    public void createUser(@FormParam("username") String username,
                           @FormParam("password") String password,
                           @FormParam("projectId") Integer projectId,
                           @Context HttpServletRequest request,
                           @Context HttpServletResponse response)
        throws IOException {

        HttpSession session = request.getSession();
        Boolean success = false;

        if (session.getAttribute("projectAdmin") == null) {
            // only project admins are able to create users
            response.sendError(403);
            return;
        }

        try {
            userMinter u = new userMinter();
            success = u.createUser(username, password, projectId);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (success) {
            response.sendRedirect("/bcid/secure/user.jsp");
            return;
        }

        response.sendRedirect("/bcid/secure/user.jsp?createError");
    }

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
                              @Context HttpServletRequest request,
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
                    error = "DB Error";
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
                update.put("email", email);
            }
            if (!institution.equals(u.getInstitution(username))) {
                update.put("institution", institution);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        Connection conn;

        if (!update.isEmpty()) {
            try {
                db = new database();
                conn = db.getConn();
            }   catch   (Exception e){
                e.printStackTrace();
                throw new WebApplicationException(Response.Status.UNAUTHORIZED);
            }

            String updateString = "UPDATE users SET ";

            // Dynamically create our UPDATE statement depending on which fields the user wants to update
            for (Enumeration e = update.keys(); e.hasMoreElements();){
                String key = e.nextElement().toString();
                updateString += key + " = ?";

                if (e.hasMoreElements()) {
                    updateString += ", ";
                }
                else {
                    updateString += " WHERE username = ?;";
                }
            }


            try {
                PreparedStatement stmt = conn.prepareStatement(updateString);

                // place the parametrized values into the SQL statement
                {
                    int i = 1;
                    for (Enumeration e = update.keys(); e.hasMoreElements();) {
                        String key = e.nextElement().toString();
                        stmt.setString(i, update.get(key));
                        i++;

                        if (!e.hasMoreElements()) {
                            stmt.setString(i, username);
                        }
                    }
                }

                Integer result = stmt.executeUpdate();

                // result should be '1', if not, an error occurred during the UPDATE statement
                if (result != 1) {
                    error += " DB Error";
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        // Error occurred somewhere, inform user
        if (error.isEmpty()) {
            if (return_to != null) {
                response.sendRedirect(return_to);
            } else {
                response.sendRedirect("/bcid/secure/user.jsp");
                return;
            }
        }

        if (return_to != null) {
            response.sendRedirect("/bcid/secure/user.jsp?error=" + error + new queryParams().getQueryParams(request.getParameterMap(), false));
            return;
        }
        response.sendRedirect("/bcid/secure/user.jsp?error=" + error);
    }
    @GET
    @Path("/profile/listEditorAsTable")
    @Produces(MediaType.TEXT_HTML)
    public String getProfile(@Context HttpServletRequest request)
            throws IOException {
        HttpSession session = request.getSession();
        Object username = session.getAttribute("user");

        if (username == null) {
            return "You must be logged in to view your profile.";
        }

        userMinter u;

        try {
            u = new userMinter();
            return u.getProfileEditorAsTable(username.toString());
        }   catch (Exception e) {
            e.printStackTrace();
        }
        return "Error loading profile editor";
    }

    /**
     * Return HTML response showing the user's profile
     *
     * @return String with HTML response
     */
    @GET
    @Path("/profile/listAsTable")
    @Produces(MediaType.TEXT_HTML)
    public String listUserProfile(@Context HttpServletRequest request) {
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
        }
        return "Exception encountered attempting to construct profile.";
    }

    @GET
    @Path("/oauth")
    @Produces(MediaType.APPLICATION_JSON)
    public String getUserData(@QueryParam("access_token") String access_token,
                              @Context HttpServletResponse response) {
        if (access_token != null) {
            try {
                userMinter u = new userMinter();

                return u.getOauthProfile(access_token);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        response.setStatus(400);
        return "[{\"error\": \"invalid_grant\"}]";
    }
}
