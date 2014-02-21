package bcid;

import auth.oauth2.provider;

import java.sql.*;

/**
 * Get an html table of the user's profile
 * Created by rjewing on 1/24/14.
 */
public class profileRetriever {
    protected Connection conn;

    public profileRetriever() throws Exception {
        database db = new database();
        conn = db.getConn();
    }

    /**
     * return a HTML table of the user's profile
     * @param username
     * @return
     */
    public String getProfileHTML(String username) {
        StringBuilder sb = new StringBuilder();
        String name = getName(username);
        String email = getEmail(username);
        String institution = getInstitution(username);

        sb.append("<table id=\"profile\">\n");
        sb.append("\t<tr>\n");
        sb.append("\t\t<td>Name:</td>\n");
        sb.append("\t\t<td>");
        sb.append(name);
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
    public String getProfileJSON(String username) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");

        sb.append("\t\"name\": \"" + getName(username) + "\",\n");
        sb.append("\t\"email\": \"" + getEmail(username) + "\",\n");
        sb.append("\t\"institution\": \"" + getInstitution(username) + "\"\n");

        sb.append("}");

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
     * lookup the user's name
     * @param username
     * @return
     */
    public String getName(String username) {
        PreparedStatement stmt;
        try {
            String selectStatement = "Select fullname from users where username = ?";
            stmt = conn.prepareStatement(selectStatement);

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getString("fullname");
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

                sb.append("\t\"name\": \"" + getName(username) + "\",\n");
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
