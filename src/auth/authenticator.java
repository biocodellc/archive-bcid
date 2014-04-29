package auth;

import bcid.database;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.*;
import java.util.Calendar;
import java.util.Hashtable;

import org.apache.commons.cli.*;
import util.SettingsManager;
import util.sendEmail;
import util.stringGenerator;

/**
 * Used for all authentication duties such as login, changing passwords, creating users, resetting passwords, etc.
 */
public class authenticator {
    private database db;
    protected Connection conn;

    /**
     * Constructor that initializes the class level variables
     */
    public authenticator() {

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
    public Boolean login(String username, String password) {

        String hashedPass = getHashedPass(username);

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
    private String getHashedPass(String username) {
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
    public Boolean setHashedPass(String username, String password) {
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
     * Update the user's password associated with the given token.
     * @param token
     * @param password
     * @return
     */
    public Boolean resetPass(String token, String password) {
        try {
            String username = null;
            String sql = "SELECT username FROM users where pass_reset_token = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);

            stmt.setString(1, token);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                username = rs.getString("username");
            }
            if (username != null) {
                String updateSql = "UPDATE users SET pass_reset_token = null, pass_reset_expiration = null WHERE username = \"" + username + "\"";
                Statement stmt2 = conn.createStatement();
                stmt2.executeUpdate(updateSql);

                return setHashedPass(username, password);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * create a hash of a password string to be stored in the db
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
     * @param userInfo
     * @return
     */
    public Boolean createUser(Hashtable<String, String> userInfo) {
        PreparedStatement stmt;
        Boolean success = false;
        String hashedPass = createHash(userInfo.get("password"));

        if (hashedPass != null) {
            try {
                String insertString = "INSERT INTO users (username, password, email, firstName, lastName, institution)" +
                        " VALUES(?,?,?,?,?,?)";
                stmt = conn.prepareStatement(insertString);

                stmt.setString(1, userInfo.get("username"));
                stmt.setString(2, hashedPass);
                stmt.setString(3, userInfo.get("email"));
                stmt.setString(4, userInfo.get("firstName"));
                stmt.setString(5, userInfo.get("lastName"));
                stmt.setString(6, userInfo.get("institution"));

                stmt.execute();
                success = true;
            } catch (SQLException e) {
                e.printStackTrace();
                success = false;
            }
        }

        return success;
    }

    /**
     * Check if the user has set their own password or if they are using a temporary password
     *
     * @return
     */
    public Boolean userSetPass(String username) {
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
     * In the case where a user has forgotten their password, generate a token that can be used to create a new
     * password. This method will send to the user's registered email a link that can be used to change their password.
     * @param username
     * @return
     * @throws Exception
     */
    public String sendResetToken(String username) throws Exception{
        String email = null;
        String sql = "SELECT email FROM users WHERE username = ?";
        PreparedStatement stmt = conn.prepareStatement(sql);

        stmt.setString(1, username);
        ResultSet rs = stmt.executeQuery();

        if (rs.next()) {
            email = rs.getString("email");
        }

        if (email != null) {
            stringGenerator sg = new stringGenerator();
            String token = sg.generateString(20);
            // set for 24hrs in future
            Timestamp ts = new Timestamp(Calendar.getInstance().getTime().getTime() + (1000*60*60*24));
            Statement stmt2 = conn.createStatement();

            String updateSql = "UPDATE users SET " +
                    "pass_reset_token = \"" + token + "\", " +
                    "pass_reset_expiration = \"" + ts + "\" " +
                    "WHERE username = \"" + username + "\"";

            stmt2.executeUpdate(updateSql);

            String emailBody = "You requested a password reset for your BCID account.\n\n" +
                    "Use the following link within the next 24 hrs to reset your password.\n\n" +
                    "http://biscicol.org/bcid/resetPass.jsp?token=" + token + "\n\n" +
                    "Thanks";

            // Initialize settings manager
            SettingsManager sm = SettingsManager.getInstance();
            try {
                sm.loadProperties();
            } catch (Exception e) {
                e.printStackTrace();
            }

            // Send an Email that this completed
            sendEmail sendEmail = new sendEmail(
                    sm.retrieveValue("mailUser"),
                    sm.retrieveValue("mailPassword"),
                    sm.retrieveValue("mailFrom"),
                    email,
                    "Reset Password",
                    emailBody);
            sendEmail.start();
        }
        return email;
    }

    /**
     * This will update a given users password. Better to use the web interface
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

        authenticator authenticator = new authenticator();

        Boolean success = authenticator.setHashedPass(username, password);

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
