package rest;

import auth.authenticator;
import bcid.database;

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
import java.sql.Statement;
import java.util.*;

/**
 * REST interface for updating user profile information
 * Created by rjewing on 1/24/14.
 */
@Path("profileService")
public class profileService {
    protected Connection conn;

    @POST
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
            authenticator authenticator = new authenticator(username, old_password);
            // Call the login function to verify the user's old_password
            Boolean valid_pass = authenticator.login();

            // If user's old_password matches stored pass, then update the user's password to the new value
            if (valid_pass) {
                Boolean success = authenticator.setHashedPass(new_password);
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
        if (!fullname.isEmpty()) {
            update.put("fullname", fullname);
        }
        if (!email.isEmpty()) {
            update.put("email", email);
        }
        if (!institution.isEmpty()) {
            update.put("institution", institution);
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
}
