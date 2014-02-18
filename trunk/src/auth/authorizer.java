package auth;

import bcid.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by rjewing on 2/7/14.
 */
public class authorizer {
    protected Connection conn;
    private database db;

    public authorizer() throws Exception{
        db = new database();
        conn = db.getConn();
    }

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
}
