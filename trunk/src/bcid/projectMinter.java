package bcid;

import util.SettingsManager;

import java.sql.*;
import java.util.*;
import java.util.Hashtable;

/**
 * Mint new expeditions.  Includes the automatic creation of a core set of entity types
 */
public class projectMinter {
    protected Connection conn;
    public ArrayList<Integer> expeditionResources;
    private SettingsManager sm;


    /**
     * The constructor defines the class-level variables used when minting Expeditions.
     * It defines a generic set of entities (process, information content, objects, agents)
     * that can be used for any expedition.
     *
     * @throws Exception
     */
    public projectMinter() throws Exception {
        database db = new database();
        conn = db.getConn();

        // Initialize settings manager
        sm = SettingsManager.getInstance();
        try {
            sm.loadProperties();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Find the BCID that denotes the validation file location for a particular expedition
     *
     * @param project_id defines the project_id to lookup
     * @return returns the BCID for this expedition and conceptURI combination
     */
    public String getValidationXML(Integer project_id) throws Exception {

        try {
            Statement stmt = conn.createStatement();

            String query = "select \n" +
                    "biovalidator_validation_xml\n" +
                    "from \n" +
                    " projects\n" +
                    "where \n" +
                    "project_id=" + project_id;
            ResultSet rs = stmt.executeQuery(query);
            rs.next();
            return rs.getString("biovalidator_validation_xml");
        } catch (SQLException e) {
            e.printStackTrace();
            throw new Exception("Trouble getting Validation XML", e);
        } catch (Exception e) {
            e.printStackTrace();

            throw new Exception("Trouble getting Validation XML", e);
        }
    }

    /**
     * List all the defined projects
     *
     * @return returns the BCID for this expedition and conceptURI combination
     */
    public String listProjects() throws Exception {
        StringBuilder sb = new StringBuilder();

        try {
            Statement stmt = conn.createStatement();

            String query = "SELECT \n" +
                    "\tproject_id,\n" +
                    "\tproject_code,\n" +
                    "\tproject_title,\n" +
                    "\tbiovalidator_validation_xml\n" +
                    " FROM \n" +
                    "\tprojects\n" +
                    " WHERE \n" +
                    "\tpublic = true\n";
            ResultSet rs = stmt.executeQuery(query);

            sb.append("{\n");
            sb.append("\t\"projects\": [\n");
            while (rs.next()) {
                sb.append("\t\t{\n");
                sb.append("\t\t\t\"project_id\":\"" + rs.getString("project_id") + "\",\n");
                sb.append("\t\t\t\"project_code\":\"" + rs.getString("project_code") + "\",\n");
                sb.append("\t\t\t\"project_title\":\"" + rs.getString("project_title") + "\",\n");
                sb.append("\t\t\t\"biovalidator_validation_xml\":\"" + rs.getString("biovalidator_validation_xml") + "\"\n");
                sb.append("\t\t}");
                if (!rs.isLast())
                    sb.append(",\n");
                else
                    sb.append("\n");
            }
            sb.append("\t]\n}");

            return sb.toString();

        } catch (SQLException e) {
            e.printStackTrace();
            throw new Exception("Trouble getting Validation XML", e);
        } catch (Exception e) {
            e.printStackTrace();

            throw new Exception("Trouble getting Validation XML", e);
        }
    }

    /**
     * A utility function to get the very latest graph loads for each expedition
     * This is a public accessible function from the REST service so it only returns results that are declared as public
     *
     * @param project_id pass in an project identifier to limit the set of expeditions we are looking at
     * @return
     */
    public String getLatestGraphs(int project_id) throws SQLException {
        StringBuilder sb = new StringBuilder();

        // Construct the query
        Statement stmt = conn.createStatement();
        // This query is built to give us a groupwise maximum-- we want the graphs that correspond to the
        // maximum timestamp (latest) loaded for a particular expedition.
        // Help on solving this problem came from http://jan.kneschke.de/expeditions/mysql/groupwise-max/
        String sql = "select p.expedition_code as expedition_code,p.expedition_title,d1.graph as graph,d1.ts as ts \n" +
                "from datasets as d1, \n" +
                "(select p.expedition_code as expedition_code,d.graph as graph,max(d.ts) as maxts \n" +
                "    \tfrom datasets d,expeditions p, expeditionsBCIDs pB\n" +
                "    \twhere pB.datasets_id=d.datasets_id\n" +
                "    \tand pB.expedition_id=p.expedition_id\n" +
                " and d.resourceType = \"http://purl.org/dc/dcmitype/Dataset\"\n" +
                "    and p.project_id = " + project_id + "\n" +
                "    \tgroup by p.expedition_code) as  d2,\n" +
                "expeditions p,  expeditionsBCIDs pB\n" +
                "where p.expedition_code = d2.expedition_code and d1.ts = d2.maxts\n" +
                " and pB.datasets_id=d1.datasets_id \n" +
                " and pB.expedition_id=p.expedition_id\n" +
                " and d1.resourceType = \"http://purl.org/dc/dcmitype/Dataset\"\n" +
                "    and p.public = 1\n" +
                "    and p.project_id =" + project_id;

        //System.out.println(sql);
        sb.append("{\n\t\"data\": [\n");
        ResultSet rs = stmt.executeQuery(sql);
        while (rs.next()) {
            // Grap the prefixes and concepts associated with this
            sb.append("\t\t{\n");
            sb.append("\t\t\t\"expedition_code\":\"" + rs.getString("expedition_code") + "\",\n");
            sb.append("\t\t\t\"expedition_title\":\"" + rs.getString("expedition_title") + "\",\n");
            sb.append("\t\t\t\"ts\":\"" + rs.getString("ts") + "\",\n");
            sb.append("\t\t\t\"graph\":\"" + rs.getString("graph") + "\"\n");
            sb.append("\t\t}");
            if (!rs.isLast())
                sb.append(",");

            sb.append("\n");
        }
        sb.append("\t]\n}\n");
        return sb.toString();
    }

    public static void main(String args[]) throws Exception {
        try {
            // See if the user owns this expedition or no
            projectMinter project = new projectMinter();
            //System.out.println(project.listProjects());
            System.out.println("results = \n" + project.getLatestGraphs(5));

        } catch (Exception e) {
            throw new Exception(e);
        }
    }

    /**
     * Return a JSON representation of the projects a user is an admin for
     *
     * @param username
     * @return
     */
    public String listUserAdminProjects(String username) {
        StringBuilder sb = new StringBuilder();

        try {
            database db = new database();
            Integer user_id = db.getUserId(username);

            Statement stmt = conn.createStatement();
            String sql = "SELECT project_id, project_code, project_title, project_title, biovalidator_validation_xml FROM projects WHERE users_id = \"" + user_id + "\"";

            ResultSet rs = stmt.executeQuery(sql);

            sb.append("{\n");
            sb.append("\t\"projects\": [\n");
            while (rs.next()) {
                sb.append("\t\t{\n");
                sb.append("\t\t\t\"project_id\":\"" + rs.getString("project_id") + "\",\n");
                sb.append("\t\t\t\"project_code\":\"" + rs.getString("project_code") + "\",\n");
                sb.append("\t\t\t\"project_title\":\"" + rs.getString("project_title") + "\",\n");
                sb.append("\t\t\t\"biovalidator_validation_xml\":\"" + rs.getString("biovalidator_validation_xml") + "\"\n");
                sb.append("\t\t}");
                if (!rs.isLast())
                    sb.append(",\n");
                else
                    sb.append("\n");
            }
            sb.append("\t]\n}");

        } catch (Exception e) {
            e.printStackTrace();
        }

        return sb.toString();
    }

    /**
     * return a JSON representation of the projects that a user is a member of
     * @param username
     * @return
     */
    public String listUsersProjects(String username) {
        StringBuilder sb = new StringBuilder();

        try {
            database db = new database();
            Integer userId = db.getUserId(username);

            Statement stmt = conn.createStatement();
            String sql = "SELECT p.project_id, p.project_code, p.project_title, p.biovalidator_validation_xml FROM projects p, usersProjects u WHERE p.project_id = u.project_id && u.users_id = \"" + userId + "\"";

            ResultSet rs = stmt.executeQuery(sql);

            sb.append("{\n");
            sb.append("\t\"projects\": [\n");
            while (rs.next()) {
                sb.append("\t\t{\n");
                sb.append("\t\t\t\"project_id\":\"" + rs.getString("project_id") + "\",\n");
                sb.append("\t\t\t\"project_code\":\"" + rs.getString("project_code") + "\",\n");
                sb.append("\t\t\t\"project_title\":\"" + rs.getString("project_title") + "\",\n");
                sb.append("\t\t\t\"biovalidator_validation_xml\":\"" + rs.getString("biovalidator_validation_xml") + "\"\n");
                sb.append("\t\t}");
                if (!rs.isLast())
                    sb.append(",\n");
                else
                    sb.append("\n");
            }
            sb.append("\t]\n}");

        } catch (Exception e) {
            e.printStackTrace();
        }

        return sb.toString();
    }

    /**
     * return an HTML table of a project's configuration.
     * @param project_id
     * @param username
     * @return
     */
    public String getProjectConfigAsTable(Integer project_id, String username) {
        StringBuilder sb = new StringBuilder();
        Hashtable<String, String> config = getProjectConfig(project_id, username);

        if (config.contains("error")) {
            return "You must be this project's admin in order to view its configuration.";
        } else {
            sb.append("<table>\n");
            sb.append("\t<tbody>\n");
            sb.append("\t\t<tr>\n");
            sb.append("\t\t\t<td>Title:</td>\n");
            sb.append("\t\t\t<td>");
            sb.append(config.get("title"));
            sb.append("</td>\n");
            sb.append("\t\t</tr>\n");

            sb.append("\t\t<tr>\n");
            sb.append("\t\t\t<td>Validation XML:</td>\n");
            sb.append("\t\t\t<td>");
            sb.append(config.get("validation_xml"));
            sb.append("</td>\n");
            sb.append("\t\t</tr>\n");

            sb.append("\t\t<tr>\n");
            sb.append("\t\t\t<td>Public Project</td>\n");
            sb.append("\t\t\t<td>\n");
            sb.append(config.get("public"));
            sb.append("</td>\n");
            sb.append("\t\t</tr>\n");

            sb.append("\t\t<tr>\n");
            sb.append("\t\t\t<td></td>\n");
            sb.append("\t\t\t<td><a href=\"javascript:void()\" id=\"edit_config\">Edit Configuration</a></td>\n");
            sb.append("\t\t</tr>\n");

            sb.append("\t</tbody>\n</table>\n");

            return sb.toString();
        }
    }

    /**
     * return an HTML table in order to edit a project's configuration
     * @param projectId
     * @param username
     * @return
     */
    public String getProjectConfigEditorAsTable(Integer projectId, String username) {
        StringBuilder sb = new StringBuilder();
        Hashtable<String, String> config = getProjectConfig(projectId, username);

        if (config.contains("error")) {
            return "You must me this project's admin in order to edit its configuration.";
        } else {
            sb.append("<form id=\"submitForm\" method=\"POST\">\n");
            sb.append("<table>\n");
            sb.append("\t<tbody>\n");
            sb.append("\t\t<tr>\n");
            sb.append("\t\t\t<td>Title</td>\n");
            sb.append(("\t\t\t<td><input type=\"text\" class=\"project_config\" name=\"title\" value=\""));
            sb.append(config.get("title"));
            sb.append("\"></td>\n\t\t</tr>\n");

            sb.append("\t\t<tr>\n");
            sb.append("\t\t\t<td>Validation XML</td>\n");
            sb.append(("\t\t\t<td><input type=\"text\" class=\"project_config\" name=\"validation_xml\" value=\""));
            sb.append(config.get("validation_xml"));
            sb.append("\"></td>\n\t\t</tr>\n");

            sb.append("\t\t<tr>\n");
            sb.append("\t\t\t<td>Public Project</td>\n");
            sb.append("\t\t\t<td><input type=\"checkbox\" name=\"public\"");
            if (config.get("public").equalsIgnoreCase("true")) {
                sb.append(" checked=\"checked\"");
            }
            sb.append("></td>\n\t\t</tr>\n");

            sb.append("\t\t<tr>\n");
            sb.append("\t\t\t<td></td>\n");
            sb.append("\t\t\t<td class=\"error\" align=\"center\">");
            sb.append("</td>\n\t\t</tr>\n");

            sb.append("\t\t<tr>\n");
            sb.append("\t\t\t<td></td>\n");
            sb.append(("\t\t\t<td><input id=\"configSubmit\" type=\"button\" value=\"Submit\">"));
            sb.append("</td>\n\t\t</tr>\n");
            sb.append("\t</tbody>\n");
            sb.append("</table>\n");
            sb.append("</form>\n");


            return sb.toString();
        }
    }

    /**
     * Update the project's configuration with the values in the Hashtable.
     * @param updateTable
     * @param projectId
     * @return
     */
    public Boolean updateConfig(Hashtable<String, String> updateTable, Integer projectId) {
        try {
            database db = new database();
            Connection conn = db.getConn();

            String updateString = "UPDATE projects SET ";

            // Dynamically create our UPDATE statement depending on which fields the user wants to update
            for (Enumeration e = updateTable.keys(); e.hasMoreElements(); ) {
                String key = e.nextElement().toString();
                updateString += key + " = ?";

                if (e.hasMoreElements()) {
                    updateString += ", ";
                } else {
                    updateString += " WHERE project_id =\"" + projectId + "\";";
                }
            }

            PreparedStatement stmt = conn.prepareStatement(updateString);

            // place the parametrized values into the SQL statement
            {
                int i = 1;
                for (Enumeration e = updateTable.keys(); e.hasMoreElements(); ) {
                    String key = e.nextElement().toString();
                    if (key.equals("public")) {
                        if (updateTable.get(key).equalsIgnoreCase("true")) {
                            stmt.setBoolean(i, true);
                        } else {
                            stmt.setBoolean(i, false);
                        }
                    } else if (updateTable.get(key).equals("")) {
                        stmt.setString(i, null);
                    } else {
                        stmt.setString(i, updateTable.get(key));
                    }
                    i++;
                }
            }

            Integer result = stmt.executeUpdate();

            // result should be '1', if not, an error occurred during the UPDATE statement
            if (result == 1) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Return a hashTable of project configuration options for a given project_id and user_id
     * @param projectId
     * @param username
     * @return
     */
    public Hashtable<String, String> getProjectConfig(Integer projectId, String username) {
        Hashtable<String, String> config = new Hashtable<String, String>();
        try {
            database db = new database();
            Integer user_id = db.getUserId(username);

            Statement stmt = conn.createStatement();
            String sql = "SELECT project_title as title, public, bioValidator_validation_xml as validation_xml FROM projects WHERE project_id=\""
                    + projectId + "\" AND users_id=\"" + user_id + "\"";

            ResultSet rs = stmt.executeQuery(sql);
            if (rs.next()) {
                config.put("title", rs.getString("title"));
                config.put("public", String.valueOf(rs.getBoolean("public")));
                if (rs.getString("validation_xml") != null) {
                    config.put("validation_xml", rs.getString("validation_xml"));
                }
            } else {
                config.put("error", "true");
            }
        } catch (Exception e) {
            e.printStackTrace();
            config.put("error", "true");
        }
        return config;
    }

    /**
     * Check if a user is a given project's admin
     * @param userId
     * @param projectId
     * @return
     */
    public Boolean userProjectAdmin(Integer userId, Integer projectId) {
        try {
            String sql = "SELECT count(*) as count FROM projects WHERE users_id = \"" + userId + "\" AND project_id = \"" + projectId + "\"";
            Statement stmt = conn.createStatement();

            ResultSet rs = stmt.executeQuery(sql);
            rs.next();

            return rs.getInt("count") >= 1;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Remove a user from a project. Once removed, a user can no longer create/view expeditions in the project.
     * @param userId
     * @param projectId
     * @return
     */
    public Boolean removeUser(Integer userId, Integer projectId) {
        try {
            String sql = "DELETE FROM usersProjects WHERE users_id = \"" + userId + "\" AND project_id = \"" + projectId + "\"";
            Statement stmt = conn.createStatement();

            stmt.executeUpdate(sql);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Add a user as a member to the project. This user can then create expeditions in this project.
     * @param userId
     * @param projectId
     * @return
     */
    public Boolean addUserToProject(Integer userId, Integer projectId) {
        PreparedStatement stmt;
        Boolean success;

        try {
            String insertStatement = "INSERT INTO usersProjects (users_id, project_id) VALUES(?,?)";
            stmt = conn.prepareStatement(insertStatement);

            stmt.setInt(1, userId);
            stmt.setInt(2, projectId);

            stmt.execute();
            success = true;
        } catch (Exception e) {
            e.printStackTrace();
            success = false;
        }

        return success;
    }

    /**
     * return an HTML table of all the members of a given project.
     * @param projectId
     * @return
     */
    public String listProjectUsersAsTable(Integer projectId) {
        StringBuilder sb = new StringBuilder();

        try {
            String userProjectSql = "SELECT users_id FROM usersProjects WHERE project_id = \"" + projectId + "\"";
            String userSql = "SELECT username, user_id FROM users";
            String projectSql = "SELECT project_title FROM projects WHERE project_id = \"" + projectId + "\"";
            List projectUsers = new ArrayList();
            Statement stmt = conn.createStatement();
            database db = new database();

            ResultSet rs3 = stmt.executeQuery(projectSql);
            rs3.next();
            String project_title = rs3.getString("project_title");

            sb.append("\t<form method=\"POST\">\n");

            sb.append("<table data-project_id=\"" + projectId + "\" data-project_title=\"" + project_title + "\">\n");
            sb.append("\t<tr>\n");
            ResultSet rs = stmt.executeQuery(userProjectSql);

            while (rs.next()) {
                Integer userId = rs.getInt("users_id");
                String username = db.getUserName(userId);
                projectUsers.add(userId);
                sb.append("\t<tr>\n");
                sb.append("\t\t<td>");
                sb.append(username);
                sb.append("</td>\n");
                sb.append("\t\t<td><a id=\"remove_user\" data-user_id=\"" + userId + "\" data-username=\"" + username + "\" href=\"javascript:void();\">(remove)</a> ");
                sb.append("<a id=\"edit_profile\" data-username=\"" + username + "\" href=\"javascript:void();\">(edit)</a></td>\n");
                sb.append("\t</tr>\n");
            }

            sb.append("\t<tr>\n");
            sb.append("\t\t<td>Add User:</td>\n");
            sb.append("\t\t<td>");
            sb.append("<select name=user_id>\n");
            sb.append("\t\t\t<option value=\"0\">Create New User</option>\n");

            ResultSet rs2 = stmt.executeQuery(userSql);

            while (rs2.next()) {
                Integer userId = rs2.getInt("user_id");
                if (!projectUsers.contains(userId)) {
                    sb.append("\t\t\t<option value=\"" + userId + "\">" + db.getUserName(userId) + "</option>\n");
                }
            }

            sb.append("\t\t</select></td>\n");
            sb.append("\t</tr>\n");
            sb.append("\t<tr>\n");
            sb.append("\t\t<td></td>\n");
            sb.append("\t\t<td><div class=\"error\" align=\"center\"></div></td>\n");
            sb.append("\t</tr>\n");
            sb.append("\t<tr>\n");
            sb.append("\t\t<td><input type=\"hidden\" name=\"project_id\" value=\"" + projectId + "\"></td>\n");
            sb.append("\t\t<td><input type=\"button\" value=\"Submit\" onclick=\"projectUserSubmit(\'" + project_title.replaceAll(" ", "_") + '_' + projectId + "\')\"></td>\n");
            sb.append("\t</tr>\n");

        } catch (Exception e) {
            e.printStackTrace();
            sb.append("<table>\n");
            sb.append("\t</form>\n");

        }

        sb.append("</table>\n");
        sb.append("\t</form>\n");

        return sb.toString();
    }
}

