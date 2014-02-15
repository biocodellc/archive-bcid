package rest;

import auth.authenticator;
import bcid.database;
import bcid.profileRetriever;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;

/**
 * REST interface for updating user profile information
 * Created by rjewing on 1/24/14.
 */
@Path("profileService")
public class profileService {
    protected Connection conn;

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.TEXT_HTML)
    public void updateProfile(@FormParam("name") String fullname,
                              @FormParam("email") String email,
                              @FormParam("institution") String institution,
                              @FormParam("old_password") String old_password,
                              @FormParam("new_password") String new_password,
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
            profileRetriever p = new profileRetriever();

            if (!fullname.equals(p.getName(username))) {
                update.put("fullname", fullname);
            }
            if (!email.equals(p.getEmail(username))) {
                update.put("email", email);
            }
            if (!institution.equals(p.getInstitution(username))) {
                update.put("institution", institution);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

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
            response.sendRedirect("/bcid/secure/user.jsp");
            return;
        }

        response.sendRedirect("/bcid/secure/profile.jsp?error=" + error);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String getProfile(@Context HttpServletRequest request)
        throws IOException {
        HttpSession session = request.getSession();
        String username = session.getAttribute("user").toString();
        profileRetriever p;

        try {
            p = new profileRetriever();
            return p.getProfileJSON(username);
        }   catch (Exception e) {
            e.printStackTrace();
        }
        return("Error");
    }

    /**
     * Return HTML response showing the user's profile
     *
     * @return String with HTML response
     */
    @GET
    @Path("/list")
    @Produces(MediaType.TEXT_HTML)
    public String listUserProfile(@Context HttpServletRequest request) {
        HttpSession session = request.getSession();
        Object username = session.getAttribute("user");
        profileRetriever p;

        if (username == null) {
            return "You must be logged in to view your profile.";
        }

        try {
            p = new profileRetriever();
            return p.getProfileHTML(username.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "Exception encountered attempting to construct profile.";
    }
}
