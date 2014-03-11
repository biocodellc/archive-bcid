package bcid;

import auth.authenticator;
import auth.oauth2.provider;

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
    /**
     * return a HTML table of the user's profile
     * @param username
     * @return
     */
    public String getProfileHTML(String username) {
        StringBuilder sb = new StringBuilder();
        String firstName = getFirstName(username);
        String lastName = getLastName(username);
        String email = getEmail(username);
        String institution = getInstitution(username);

        sb.append("<table id=\"profile\">\n");
        sb.append("\t<tr>\n");
        sb.append("\t\t<td>First Name:</td>\n");
        sb.append("\t\t<td>");
        sb.append(firstName);
        sb.append("</td>\n");
        sb.append("\t</tr>\n");

        sb.append("\t<tr>\n");
        sb.append("\t\t<td>Last Name:</td>\n");
        sb.append("\t\t<td>");
        sb.append(lastName);
        sb.append("</td>\n");
        sb.append("\t</tr>\n");

        sb.append("\t<tr>\n");
        sb.append("\t\t<td>Email:</td>\n");
        sb.append("\t\t<td>");
        sb.append(email);
        sb.append("</td>\n");
        sb.append("\t</tr>\n");

        sb.append("\t<tr>\n");
        sb.append("\t\t<td>Institution:</td>\n");
        sb.append("\t\t<td>");
        sb.append(institution);
        sb.append("</td>\n");
        sb.append("\t</tr>\n");

        sb.append("\t<tr>\n");
        sb.append("\t\t<td></td>\n");
        sb.append("\t\t<td><a href=\"/bcid/secure/profile.jsp\">Edit Profile</a></td>\n");
        sb.append("\t</tr>\n");

        sb.append("\t</tr>\n</table>\n");

        return sb.toString();
    }

    /**
     * return JSON on user profile information
     * @param username
     * @return
     */
    public String getProfileEditorAsTable(String username) {
        StringBuilder sb = new StringBuilder();
        String firstName = getFirstName(username);
        String lastName = getLastName(username);
        String email = getEmail(username);
        String institution = getInstitution(username);
        sb.append("[{\n");

        sb.append("<table>\n");
        sb.append("\t<tr>\n");
        sb.append("\t\t<td>First Name</td>\n");
        sb.append("\t\t<td>");
        sb.append(("\t\t<td><input type=\"text\" name=\"firstName\" value=\""));
        sb.append(firstName);
        sb.append("\"></td>\n\t</tr>");

        sb.append("\t\"firstName\": \"" + getFirstName(username) + "\",\n");
        sb.append("\t\"lastName\": \"" + getLastName(username) + "\",\n");
        sb.append("\t\"email\": \"" + getEmail(username) + "\",\n");
        sb.append("\t\"institution\": \"" + getInstitution(username) + "\"\n");

        sb.append("}]");

        return sb.toString();
    }

    /**
     * lookup the user's institution
     * @param username
     * @return
     */
    public String getInstitution(String username) {
        PreparedStatement stmt;
        try {
            String selectStatement = "Select institution from users where username = ?";
            stmt = conn.prepareStatement(selectStatement);

            stmt.setString(1, username);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getString("institution");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * lookup the user's email
     * @param username
     * @return
     */
    public String getEmail(String username) {
        PreparedStatement stmt;
        try {
            String selectStatement = "Select email from users where username = ?";
            stmt = conn.prepareStatement(selectStatement);

            stmt.setString(1, username);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getString("email");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * lookup the user's first name
     * @param username
     * @return
     */
    public String getFirstName(String username) {
        PreparedStatement stmt;
        try {
            String selectStatement = "Select firstName from users where username = ?";
            stmt = conn.prepareStatement(selectStatement);

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getString("firstName");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * lookup the user's first name
     * @param username
     * @return
     */
    public String getLastName(String username) {
        PreparedStatement stmt;
        try {
            String selectStatement = "Select lastName from users where username = ?";
            stmt = conn.prepareStatement(selectStatement);

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getString("lastName");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getOauthProfile(String token) {
        try {
            provider p = new provider();
            database db = new database();

            Integer user_id = p.validateToken(token);

            if (user_id != null) {
                String username = db.getUserName(user_id);

                StringBuilder sb = new StringBuilder();
                sb.append("{\n");

                sb.append("\t\"first_name\": \"" + getFirstName(username) + "\",\n");
                sb.append("\t\"last_name\": \"" + getLastName(username) + "\",\n");
                sb.append("\t\"email\": \"" + getEmail(username) + "\",\n");
                sb.append("\t\"institution\": \"" + getInstitution(username) + "\",\n");
                sb.append("\t\"user_id\": " + user_id +"\n");

                sb.append("}");

                return sb.toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "[{\"error\": \"invalid_grant\"}]";
    }
}
