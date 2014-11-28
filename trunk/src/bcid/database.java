package bcid;

import util.SettingsManager;

import java.io.FileNotFoundException;
import java.math.BigInteger;
import java.sql.*;

/**
 * Creates the connection for the backend bcid database.
 * Settings come from the util.SettingsManager/Property file defining the user/password/url/class
 * for the mysql database where the data lives.
 */
public class database {

    // Mysql Connection
    protected Connection conn;

    /**
     * Load settings for creating this database connection from the bcidsettings.properties file
     */
    public database() {
        try {
            SettingsManager sm = SettingsManager.getInstance();
            sm.loadProperties();
            String bcidUser = sm.retrieveValue("bcidUser");
            String bcidPassword = sm.retrieveValue("bcidPassword");
            String bcidUrl = sm.retrieveValue("bcidUrl");
            String bcidClass = sm.retrieveValue("bcidClass");

            Class.forName(bcidClass);
            conn = DriverManager.getConnection(bcidUrl, bcidUser, bcidPassword);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Driver issues accessing BCID system", e);
        } catch (SQLException e) {
            throw new RuntimeException("SQL Exception accessing BCID system", e);
        }

    }

    public Connection getConn() {
        return conn;
    }

    /**
     * Return the userID given a username
     * @param username
     * @return
     */
    public Integer getUserId(String username) {
        Statement stmt = null;
        try {
            stmt = conn.createStatement();

            ResultSet rs = stmt.executeQuery("Select user_id from users where username=\"" + username + "\"");

            if (rs.next()) {
                return rs.getInt("user_id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    /**
     * Return the username given a userId
     * @param userId
     * @return
     */
    public String getUserName(Integer userId) {
        Statement stmt = null;
        try {
            stmt = conn.createStatement();

            ResultSet rs = stmt.executeQuery("Select username from users where user_id=" + userId + "");

            if (rs.next()) {
                return rs.getString("username");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

}
