package bcid;

import auth.authenticator;
import java.sql.*;

/**
 * Created by rjewing on 2/11/14.
 */
public class userMinter {
    protected Connection conn;

    public userMinter() throws Exception {
        database db = new database();
        conn = db.getConn();
    }

    public String listSystemUsers() {
        StringBuilder sb = new StringBuilder();
        sb.append("[{");

        try {
            String sql = "SELECT username, user_id FROM users";
            Statement stmt = conn.createStatement();

            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                sb.append("\"" + rs.getInt("user_id") + "\":\"" + rs.getString("username") + "\",");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        sb.deleteCharAt(sb.lastIndexOf(","));
        sb.append("}]");
        return sb.toString();
    }

    public Boolean createUser(String username, String password, Integer projectId) {
        authenticator auth = new authenticator();
        Boolean success = auth.createUser(username, password);

        // if user was created, add user to project
        if (success) {
            try {
                database db = new database();
                Integer userId = db.getUserId(username);
                success = addUserToProject(userId, projectId);
            } catch (Exception e) {
                e.printStackTrace();
                success = false;
            }
        }

        return success;
    }


    public Boolean addUserToProject(Integer userId, Integer projectId) {
        PreparedStatement stmt;
        Boolean success = false;

        try {
            if (userId != null) {
                String insertStatement = "INSERT INTO usersProjects (users_id, project_id) VALUES(?,?)";
                stmt = conn.prepareStatement(insertStatement);

                stmt.setInt(1, userId);
                stmt.setInt(2, projectId);

                stmt.execute();
                success = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            success = false;
        }

        System.out.println(success.toString());
        return success;
    }
}
