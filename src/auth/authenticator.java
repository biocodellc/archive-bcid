package auth;

import bcid.database;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.*;

import org.apache.commons.cli.*;
import sun.print.resources.serviceui_sv;

/**
 * Used for all authentication duties
 * Created by rjewing on 1/14/14.
 */
public class authenticator {
    private String username;
    private String password;
    private database db;
    protected Connection conn;

    /**
     * Constructor that initializes the class level variables
     *
     * @param username
     * @param password
     */

    public authenticator(String username, String password) {
        this.username = username;
        this.password = password;

        // Initialize database
        try {
            this.db = new database();
            this.conn = db.getConn();
        } catch (Exception e) {
            e.printStackTrace();
            throw new WebApplicationException(Response.Status.UNAUTHORIZED);
        }
    }

    /**
     * Public method to verify a users password
     *
     * @return
     */
    public Boolean login() {

        String hashedPass = getHashedPass();

        if (!hashedPass.isEmpty()) {
            try {
                return passwordHash.validatePassword(password, hashedPass);
            } catch (InvalidKeySpecException e) {
                e.printStackTrace();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
        }

        return false;
    }

    /**
     * retrieve the user's hashed password from the db
     *
     * @return
     */
    private String getHashedPass() {
        PreparedStatement stmt;
        try {
            String selectString = "SELECT password FROM users WHERE username = ?";
            stmt = conn.prepareStatement(selectString);

            stmt.setString(1, username);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getString("password");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Takes a new password for a user and stores a hashed version
     *
     * @param password
     * @return
     */
    public Boolean setHashedPass(String password) {
        PreparedStatement stmt;

        String hashedPass = createHash(password);

        // Store the hashed password in the db
        if (hashedPass != null){
            try {
                String updateString = "UPDATE users SET password = ? WHERE username = ?";
                stmt = conn.prepareStatement(updateString);

                stmt.setString(1, hashedPass);
                stmt.setString(2, username);
                Integer result = stmt.executeUpdate();

                if (result == 1) {
                    return true;
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * create a hash of a password string
     * @param password
     * @return
     */
    private String createHash(String password) {
        String hashedPass = null;

        try {
            hashedPass = passwordHash.createHash(password);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        }

        return hashedPass;
    }

    /**
     * create a user given a username and password
     * @param username
     * @param password
     * @return
     */
    public Boolean createUser(String username, String password) {
        String hashedPass = createHash(password);
        PreparedStatement stmt;
        Boolean success = false;

        if (hashedPass != null) {
            try {
                String insertString = "INSERT INTO users username = ?, password = ?";
                stmt = conn.prepareStatement(insertString);

                stmt.setString(1, username);
                stmt.setString(2, hashedPass);

                success = stmt.execute();
            } catch (SQLException e) {
                e.printStackTrace();;
            }
        }

        return success;
    }

    /**
     * Check if the user has set their own password or if they are using a temporary password
     *
     * @return
     */
    public Boolean userSetPass() {
        PreparedStatement stmt;
        try {
            String selectString = "SELECT set_password FROM users WHERE username = ?";
            stmt = conn.prepareStatement(selectString);

            stmt.setString(1, username);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Integer set_password = rs.getInt("set_password");
                if (set_password == 1) {
                    return true;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * This will update a given users password. Probably not the best way to do this though.
     *
     * @param args username and password
     */
    public static void main(String args[]) {

        // Some classes to help us
        CommandLineParser clp = new GnuParser();
        CommandLine cl;

        Options options = new Options();
        options.addOption("U", "username", true, "Username you would like to set a password for");
        options.addOption("P", "password", true, "The temporary password you would like to set");

        try {
            cl = clp.parse(options, args);
        } catch (UnrecognizedOptionException e) {
            System.out.println("Error: " + e.getMessage());
            return;
        } catch (ParseException e) {
            System.out.println("Error: " + e.getMessage());
            return;
        }

        if (!cl.hasOption("U") || !cl.hasOption("P")) {
            System.out.println("You must enter a username and a password");
            return;
        }

        String username = cl.getOptionValue("U");
        String password = cl.getOptionValue("P");

        authenticator authenticator = new authenticator(username, password);

        Boolean success = authenticator.setHashedPass(password);

        if (!success) {
            System.out.println("Error updating password for " + username);
            return;
        }

        // change set_password field to 0 so user has to create new password next time they login
        Statement stmt;
        try {
            stmt = authenticator.conn.createStatement();
            Integer result = stmt.executeUpdate("UPDATE users SET set_password=\"0\" WHERE username=\"" + username + "\"");

            if (result == 0) {
                System.out.println("Error updating set_password value to 0 for " + username);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        System.out.println("Successfully set new password for " + username);
    }


}