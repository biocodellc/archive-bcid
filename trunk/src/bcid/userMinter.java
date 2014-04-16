package bcid;

import auth.authenticator;
import auth.oauth2.provider;

import java.sql.*;
import java.util.Enumeration;
import java.util.Hashtable;

/**
 * Created by rjewing on 2/11/14.
 */
public class userMinter {
    protected Connection conn;

    public userMinter() throws Exception {
        database db = new database();
        conn = db.getConn();
    }

    public String createUser(Hashtable<String, String> userInfo, Integer projectId) {
        authenticator auth = new authenticator();
        Boolean success = auth.createUser(userInfo);

        // if user was created, add user to project
        if (success) {
            try {
                database db = new database();
                Integer userId = db.getUserId(userInfo.get("username"));
                projectMinter p = new projectMinter();

                if (p.addUserToProject(userId, projectId)) {
                    return "[{\"success\": \"successfully created new user\"}]";
                } else {
                    return "[{\"error\": \"error adding user to project\"}]";
                }
            } catch (Exception e) {
                e.printStackTrace();
                return "[{\"error\": \"error adding user to project\"}]";
            }
        }
        return "[{\"error\": \"error creating new user\"}]";
    }

    public String getCreateForm() {
        StringBuilder sb = new StringBuilder();
        sb.append("\t<form id=\"submitForm\" method=\"POST\">\n");

        sb.append("<table>\n");

        sb.append("\t\t<tr>\n");
        sb.append("\t\t\t<td>Username</td>\n");
        sb.append("\t\t\t<td><input type=\"text\" name=\"username\"></td>\n");
        sb.append("\t\t</tr>\n");

        sb.append("\t\t<tr>\n");
        sb.append("\t\t\t<td>First Name</td>\n");
        sb.append(("\t\t\t<td><input type=\"text\" name=\"firstName\"></td>\n"));
        sb.append("\t\t</tr>\n");

        sb.append("\t\t<tr>\n");
        sb.append("\t\t\t<td>Last Name</td>\n");
        sb.append(("\t\t\t<td><input type=\"text\" name=\"lastName\"></td>\n"));
        sb.append("\t\t</tr>\n");

        sb.append("\t\t<tr>\n");
        sb.append("\t\t\t<td>Email</td>\n");
        sb.append(("\t\t\t<td><input type=\"text\" name=\"email\"></td>\n"));
        sb.append("\t\t</tr>\n");

        sb.append("\t\t<tr>\n");
        sb.append("\t\t\t<td>Institution</td>\n");
        sb.append(("\t\t\t<td><input type=\"text\" name=\"institution\"></td>\n"));
        sb.append("\t\t</tr>\n");

        sb.append("\t\t<tr>\n");
        sb.append("\t\t\t<td>Password</td>\n");
        sb.append("\t\t\t<td><input class=\"pwcheck\" type=\"password\" name=\"password\" data-indicator=\"pwindicator\"></td>\n");
        sb.append("\t\t</tr>");

        sb.append("\t\t<tr>\n");
        sb.append("\t\t\t<td></td>\n");
        sb.append("\t\t\t<td><div id=\"pwindicator\"><div class=\"label\"></div></div></td>\n");
        sb.append("\t\t</tr>\n");

        sb.append("\t\t<tr>\n");
        sb.append("\t\t\t<td></td>\n");
        sb.append("\t\t\t<td><div class=\"error\" align=\"center\"></div></td>\n");
        sb.append("\t\t</tr>\n");

        sb.append("\t\t<tr>\n");
        sb.append("\t\t\t<td></td>\n");
        sb.append("\t\t\t<td><input type=\"button\" id=\"createFormButton\" value=\"Submit\"><input type=\"button\" id=\"createFormCancelButton\" value=\"Cancel\"></td>\n");
        sb.append("\t\t</tr>\n");
        sb.append("\t\t<input type=\"hidden\" name=\"project_id\">\n");

        sb.append("</table>\n");
        sb.append("\t</form>\n");


        return sb.toString();
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
        sb.append("\t\t<td><a href=\"javascript:void(0)\">Edit Profile</a></td>\n");
        sb.append("\t</tr>\n");

        sb.append("\t</tr>\n</table>\n");

        return sb.toString();
    }

    /**
     * return JSON on user profile information
     * @param username
     * @return
     */
    public String getProfileEditorAsTable(String username, Boolean isAdmin) {
        StringBuilder sb = new StringBuilder();
        String firstName = getFirstName(username);
        String lastName = getLastName(username);
        String email = getEmail(username);
        String institution = getInstitution(username);

        sb.append("<form method=\"POST\" action=\"/id/userService/profile/update\">\n");

        sb.append("<table>\n");
        sb.append("\t<tr>\n");
        sb.append("\t\t<td>First Name</td>\n");
        sb.append(("\t\t<td><input type=\"text\" name=\"firstName\" value=\""));
        sb.append(firstName);
        sb.append("\"></td>\n\t</tr>");

        sb.append("\t<tr>\n");
        sb.append("\t\t<td>Last Name</td>\n");
        sb.append(("\t\t<td><input type=\"text\" name=\"lastName\" value=\""));
        sb.append(lastName);
        sb.append("\"></td>\n\t</tr>");

        sb.append("\t<tr>\n");
        sb.append("\t\t<td>Email</td>\n");
        sb.append(("\t\t<td><input type=\"text\" name=\"email\" value=\""));
        sb.append(email);
        sb.append("\"></td>\n\t</tr>");

        sb.append("\t<tr>\n");
        sb.append("\t\t<td>Institution</td>\n");
        sb.append(("\t\t<td><input type=\"text\" name=\"institution\" value=\""));
        sb.append(institution);
        sb.append("\"></td>\n\t</tr>");

        sb.append("\t<tr>\n");
        sb.append("\t\t<td>New Password</td>\n");
        sb.append("\t\t<td><input class=\"pwcheck\" type=\"password\" name=\"new_password\" data-indicator=\"pwindicator\">");
        sb.append("</td>\n\t</tr>");

        sb.append("\t<tr>\n");
        sb.append("\t\t<td></td>\n");
        sb.append("\t\t<td><div id=\"pwindicator\"><div class=\"label\"></div></div></td>\n");
        sb.append("\t</tr>");

        if (!isAdmin) {
            sb.append("\t<tr>\n");
            sb.append("\t\t<td>Old Password</td>\n");
            sb.append("\t\t<td><input type=\"password\" name=\"old_password\">");
            sb.append("</td>\n\t</tr>");
        }

        sb.append("\t<tr>\n");
        sb.append("\t\t<td></td>\n");
        sb.append("<td class=\"error\" align=\"center\">");
        sb.append("</td>\n\t</tr>");

        sb.append("\t<tr>\n");
        sb.append("\t\t<td></td>\n");
        sb.append(("\t\t<td><input id=\"profile_submit\" type=\"button\" value=\"Submit\"><input type=\"button\" id=\"cancelButton\" value=\"Cancel\">"));
        sb.append("</td>\n\t</tr>\n");
        sb.append("</table>\n");
        sb.append("</form>\n");


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
                sb.append("[{\n");

                sb.append("\t\"first_name\": \"" + getFirstName(username) + "\",\n");
                sb.append("\t\"last_name\": \"" + getLastName(username) + "\",\n");
                sb.append("\t\"email\": \"" + getEmail(username) + "\",\n");
                sb.append("\t\"institution\": \"" + getInstitution(username) + "\",\n");
                sb.append("\t\"user_id\": \"" + user_id + "\",\n");
                sb.append("\t\"username\": \"" + username + "\"\n");

                sb.append("}]");

                return sb.toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "[{\"error\": \"invalid_grant\"}]";
    }

    /**
     * check if a username already exists
     * @param username
     * @return
     */
    public Boolean checkUsernameExists(String username) {
        try {
            String sql = "SELECT count(*) as count FROM users WHERE username = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);

            stmt.setString(1, username);

            ResultSet rs = stmt.executeQuery();
            rs.next();
            return rs.getInt("count") >= 1;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * update a users profile
     * @param info
     * @param username
     * @return
     */
    public Boolean updateProfile(Hashtable<String, String> info, String username) {
        String updateString = "UPDATE users SET ";

        // Dynamically create our UPDATE statement depending on which fields the user wants to update
        for (Enumeration e = info.keys(); e.hasMoreElements();){
            String key = e.nextElement().toString();
            updateString += key + " = ?";

            if (e.hasMoreElements()) {
                updateString += ", ";
            }
            else {
                updateString += " WHERE username = ?;";
            }
        }


        try {
            PreparedStatement stmt = conn.prepareStatement(updateString);

            // place the parametrized values into the SQL statement
            {
                int i = 1;
                for (Enumeration e = info.keys(); e.hasMoreElements();) {
                    String key = e.nextElement().toString();
                    stmt.setString(i, info.get(key));
                    i++;

                    if (!e.hasMoreElements()) {
                        stmt.setString(i, username);
                    }
                }
            }

            Integer result = stmt.executeUpdate();

            // result should be '1', if not, an error occurred during the UPDATE statement
            return result == 1;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
