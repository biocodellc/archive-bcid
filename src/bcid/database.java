package bcid;

import util.SettingsManager;

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
    public database() throws Exception {
        SettingsManager sm = SettingsManager.getInstance();
        sm.loadProperties();
        String bcidUser = sm.retrieveValue("bcidUser");
        String bcidPassword = sm.retrieveValue("bcidPassword");
        String bcidUrl = sm.retrieveValue("bcidUrl");
        String bcidClass = sm.retrieveValue("bcidClass");


        try {
            Class.forName(bcidClass);
            conn = DriverManager.getConnection(bcidUrl, bcidUser, bcidPassword);
        } catch (java.lang.ClassNotFoundException e) {
            e.printStackTrace();
            throw new Exception("Driver issues accessing BCID system");
        } catch (SQLException e) {
            e.printStackTrace();
            throw new Exception("SQL Exception accessing BCID system");
        }
    }

    public Connection getConn() {
        return conn;
    }

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
}
