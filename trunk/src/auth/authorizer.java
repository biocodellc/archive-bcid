package auth;

import bcid.database;
import bcidExceptions.ServerErrorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.Calendar;

/**
 * Class to check whether a user has certain permissions, such as if they are a project admin
 */
public class authorizer {
    protected Connection conn;
    private database db;
    private static Logger logger = LoggerFactory.getLogger(authorizer.class);

    public authorizer() {
        db = new database();
        conn = db.getConn();
    }

    /**
     * determine if the user is an admin for any projects
     * @param username
     * @return
     */
    public Boolean userProjectAdmin(String username) {
        PreparedStatement stmt;
        try {
            Integer users_id = db.getUserId(username);
            String selectString = "SELECT count(*) as count FROM projects WHERE users_id = ?";

            stmt = conn.prepareStatement(selectString);
            stmt.setInt(1, users_id);

            ResultSet rs = stmt.executeQuery();
            rs.next();
            return rs.getInt("count") >= 1;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * determine if the password reset token is still valid
     * @param token
     * @return
     */
    public Boolean validResetToken(String token) {
        PreparedStatement stmt;
        try {
            String sql = "SELECT pass_reset_expiration as ts FROM users WHERE pass_reset_token = ?";

            stmt = conn.prepareStatement(sql);
            stmt.setString(1, token);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                Timestamp expirationTs = rs.getTimestamp("ts");
                Timestamp ts = new Timestamp(Calendar.getInstance().getTime().getTime());
                if (expirationTs != null && expirationTs.after(ts)) {
                    return true;
                }
            }
        } catch (SQLException e) {
            throw new ServerErrorException("Server Error while validating reset token",
                    "db error retrieving reset token expiration", e);
        }
        return false;
    }

    public void close() {
        try {
            conn.close();
        } catch (SQLException e) {
            logger.warn("SQLException while trying to close connection.", e);
        }
    }
}
