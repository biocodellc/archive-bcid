package auth;

import bcid.database;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Hashtable;
import java.util.Iterator;

import bcid.projectMinter;
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
    SettingsManager sm;
    private static LDAPAuthentication ldapAuthentication;

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

        // Initialize settings manager
        sm = SettingsManager.getInstance();
        try {
            sm.loadProperties();
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    public static LDAPAuthentication getLdapAuthentication() {
        return ldapAuthentication;
    }

    /**
     * Process login as LDAP
     *
     * @param username
     * @param password
     * @param recognizeDemo
     *
     * @return
     */
    public Boolean loginLDAP(String username, String password, Boolean recognizeDemo) {
        // 1. check that this is a valid username in this system
        if (!validUser(LDAPAuthentication.showShortUserName(username))) {
            System.out.println("attempting to add user " + username+" to system");
            /*
            ADD LDAP authenticated user
            */
            // A. Check LDAP authentication
            System.out.println("LDAP authentication");
            ldapAuthentication = new LDAPAuthentication(username, password, recognizeDemo);
            System.out.println("message = " + ldapAuthentication.getMessage() + ";status=" + ldapAuthentication.getStatus());

            if (ldapAuthentication.getStatus() == ldapAuthentication.SUCCESS) {

                // B. If LDAP is good, then insert account into database (if not return false)
                System.out.println("create LDAP user");
                try {
                    createLdapUser(LDAPAuthentication.showShortUserName(username));
                }   catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }

                // C. enable this user for all projects
                System.out.println("add user to projects");
                try {
                    projectMinter p = new projectMinter();
                    // get the user_id for this username
                    int user_id = getUserId(username);
                    // Loop projects and assign user to them
                    ArrayList<Integer> projects = p.getAllProjects();
                    Iterator projectsIt = projects.iterator();
                    while (projectsIt.hasNext()) {
                        p.addUserToProject(user_id, (Integer) projectsIt.next());
                    }
                } catch (Exception e) {
                    // TODO: if it fails at this point not sure if we should pass or fail this? for now we still say true
                    e.printStackTrace();
                    return true;
                }
                // D. return true because we got this far and already authenticated
                return true;
            } else {
                return false;
            }
        }
        // 2. If a valid username, we just need to check that the LDAP authentication worked
        else {
            System.out.println("the user exists, just authenticating using LDAP");
            ldapAuthentication = new LDAPAuthentication(username, password, recognizeDemo);
            if (ldapAuthentication.getStatus() == ldapAuthentication.SUCCESS) {
                return true;
            } else {
                return false;
            }
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
     * retrieve the user's hashed password from the db
     *
     * @return
     */
    private boolean validUser(String username) {
        int count = 0;
        PreparedStatement stmt;
        try {
            String selectString = "SELECT user_id id FROM users WHERE username = ?";
            stmt = conn.prepareStatement(selectString);

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                count = rs.getInt("id") + count;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        if (count == 1) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Takes a new password for a user and stores a hashed version
     *
     * @param password
     *
     * @return
     */
    public Boolean setHashedPass(String username, String password) {
        PreparedStatement stmt;

        String hashedPass = createHash(password);

        // Store the hashed password in the db
        if (hashedPass != null) {
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
     *
     * @param token
     * @param password
     *
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
     *
     * @param password
     *
     * @return
     */
    public String createHash(String password) {
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
     *
     * @param username
     *
     * @return
     */
    public Boolean createLdapUser(String username) {
        PreparedStatement stmt = null;
        Boolean success = false;

        try {

            String insertString = "INSERT INTO users (username,set_password,institution,email,firstName,lastName,pass_reset_token,password)" +
                    " VALUES(?,?,?,?,?,?,?,?)";
            stmt = conn.prepareStatement(insertString);

            stmt.setString(1, username);
            stmt.setInt(2, 1);
            stmt.setString(3, "Smithsonian Institution");
            stmt.setString(4, "");
            stmt.setString(5, "");
            stmt.setString(6, "");
            stmt.setString(7, "");
            stmt.setString(8, "");

            stmt.execute();
            success = true;
        } catch (SQLException e) {
            e.printStackTrace();
            success = false;
        } finally {
            if (stmt != null) try {
                stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return success;
    }


    /**
     * return the user_id given a username
     *
     * @param username
     *
     * @return
     */
    private Integer getUserId(String username) {
        Integer user_id = null;
        try {
            String selectString = "SELECT user_id FROM users WHERE username=?";
            PreparedStatement stmt = conn.prepareStatement(selectString);

            stmt.setString(1, LDAPAuthentication.showShortUserName(username));

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                user_id = rs.getInt("user_id");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return user_id;
    }

    /**
     * create a user given a username and password
     *
     * @param userInfo
     *
     * @return
     */
    public Boolean createUser(Hashtable<String, String> userInfo) {
        PreparedStatement stmt = null;
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
            } finally {
                if (stmt != null) try {
                    stmt.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
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
        PreparedStatement stmt = null;
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
        } finally {
            if (stmt != null) try {
                stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * In the case where a user has forgotten their password, generate a token that can be used to create a new
     * password. This method will send to the user's registered email a link that can be used to change their password.
     *
     * @param username
     *
     * @return
     *
     * @throws Exception
     */
    public String sendResetToken(String username) throws Exception {
        String email = null;
        String sql = "SELECT email FROM users WHERE username = ?";

        PreparedStatement stmt = null;
        try {
            stmt = conn.prepareStatement(sql);


            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                email = rs.getString("email");
            }

            if (email != null) {
                stringGenerator sg = new stringGenerator();
                String token = sg.generateString(20);
                // set for 24hrs in future
                Timestamp ts = new Timestamp(Calendar.getInstance().getTime().getTime() + (1000 * 60 * 60 * 24));
                Statement stmt2 = conn.createStatement();

                String updateSql = "UPDATE users SET " +
                        "pass_reset_token = \"" + token + "\", " +
                        "pass_reset_expiration = \"" + ts + "\" " +
                        "WHERE username = \"" + username + "\"";

                stmt2.executeUpdate(updateSql);

                // Reset token path
                String resetToken = sm.retrieveValue("resetToken") + token;

                String emailBody = "You requested a password reset for your BCID account.\n\n" +
                        "Use the following link within the next 24 hrs to reset your password.\n\n" +
                        resetToken + "\n\n" +
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
        } catch (Exception e) {
            throw new Exception(e);
        } finally {
            if (stmt != null) try {
                stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
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
        options.addOption("ldap", false, "Use LDAP to set username");


        try {
            cl = clp.parse(options, args);
        } catch (UnrecognizedOptionException e) {
            System.out.println("Error: " + e.getMessage());
            return;
        } catch (ParseException e) {
            System.out.println("Error: " + e.getMessage());
            return;
        }

        if (!cl.hasOption("U") || (!cl.hasOption("P") && cl.hasOption("ldap"))) {
            System.out.println("You must enter a username and a password");
            return;
        }

        String username = cl.getOptionValue("U");
        String password = cl.getOptionValue("P");

        authenticator authenticator = new authenticator();

        // LDAP option
        if (cl.hasOption("ldap")) {
            System.out.println("authenticating using LDAP");
            Boolean success = authenticator.loginLDAP(username, password, true);
            if (!success) {
                System.out.println("Error logging in using LDAP");
            }
            return;
        }

        Boolean success = authenticator.setHashedPass(username, password);

        if (!success) {
            System.out.println("Error updating password for " + username);
            return;
        }

        // change set_password field to 0 so user has to create new password next time they login
        Statement stmt = null;
        try {
            stmt = authenticator.conn.createStatement();
            Integer result = stmt.executeUpdate("UPDATE users SET set_password=\"0\" WHERE username=\"" + username + "\"");

            if (result == 0) {
                System.out.println("Error updating set_password value to 0 for " + username);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            authenticator.close();
        }

        System.out.println("Successfully set new password for " + username);
    }

    public void close() {
        if (conn != null) try {
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}


