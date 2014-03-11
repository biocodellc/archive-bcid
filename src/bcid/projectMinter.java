package bcid;

import util.SettingsManager;

import javax.swing.plaf.basic.BasicInternalFrameTitlePane;
import java.sql.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;

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
                "    and p.project_id =" + project_id;

       // System.out.println(sql);
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
            System.out.println(project.listProjects());
            //System.out.println("results = \n" + project.getLatestGraphs(5));

        } catch (Exception e) {
            throw new Exception(e);
        }
    }
    /**
     * Return a JSON representation of the projects a user is an admin for
     * @param username
     * @return
     */
    public String listUserAdminProjects(String username) {
        StringBuilder sb = new StringBuilder();
        sb.append("[{");

        try {
            database db = new database();
            Integer user_id = db.getUserId(username);

            Statement stmt = conn.createStatement();
            String sql = "SELECT project_id, project_title FROM projects WHERE users_id = \"" + user_id + "\"";

            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                sb.append("\"" + rs.getInt("project_id") + "\":\"" + rs.getString("project_title") + "\",");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (sb.length() > 2) {
            sb.deleteCharAt(sb.lastIndexOf(","));
        }
        sb.append("}]");
        return sb.toString();
    }

    public String getProjectConfigAsTable(Integer project_id, String username) {
        StringBuilder sb = new StringBuilder();
        String title = null;
        String ab= null;
        String validation_xml = null;

        try {
            database db = new database();
            Integer user_id = db.getUserId(username);

            Statement stmt = conn.createStatement();
            String sql = "SELECT project_title as title, abstract, bioValidator_validation_xml as validation_xml FROM projects WHERE project_id=\""
                    + project_id + "\" AND users_id=\"" + user_id + "\"";

            ResultSet rs = stmt.executeQuery(sql);
            if (rs.next()) {
                title = rs.getString("title");
                ab = rs.getString("abstract");
                validation_xml = rs.getString("validation_xml");
            } else {
                sb.append("[{\"error\": \"You must be this project's admin in order to view its configuration\"}]");
                return sb.toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
            sb.append("[{\"error\": \"Server error\"}]");
            return sb.toString();
        }

        sb.append("<table>\n");
        sb.append("\t<tbody>\n");
        sb.append("\t\t<tr>\n");
        sb.append("\t\t\t<td>Title:</td>\n");
        sb.append("\t\t\t<td>");
        sb.append(title);
        sb.append("\t\t\t</td>\n");
        sb.append("\t\t</tr>\n");

        sb.append("\t\t<tr>\n");
        sb.append("\t\t\t<td>Abstract:</td>\n");
        sb.append("\t\t\t<td>");
        sb.append(ab);
        sb.append("\t\t\t</td>\n");
        sb.append("\t\t</tr>\n");

        sb.append("\t\t<tr>\n");
        sb.append("\t\t\t<td>Validation XML:</td>\n");
        sb.append("\t\t\t<td>");
        sb.append(validation_xml);
        sb.append("\t\t\t</td>\n");
        sb.append("\t\t</tr>\n");

        sb.append("\t\t<tr>\n");
        sb.append("\t\t\t<td></td>\n");
        sb.append("\t\t\t<td><a href=\"javascript:void(0)\">Edit Configuration</a></td>\n");
        sb.append("\t\t</tr>\n");

        sb.append("\t</tbody>\n</table>\n");

        return sb.toString();
    }

    public Boolean userProjectAdmin(Integer userId, Integer projectId) {
        try {
            String sql = "SELECT count(*) as count FROM usersProjects WHERE users_id = \"" + userId + "\" AND project_id = \"" + projectId + "\"";
            Statement stmt = conn.createStatement();

            ResultSet rs = stmt.executeQuery(sql);
            rs.next();

            return rs.getInt("count") >= 1;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

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

    public String listProjectUsersAsTable(Integer projectId) {
        StringBuilder sb = new StringBuilder();
        sb.append("<table>\n");
        sb.append("\t<tr>\n");

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

            ResultSet rs = stmt.executeQuery(userProjectSql);

            while (rs.next()) {
                Integer userId = rs.getInt("users_id");
                projectUsers.add(userId);
                sb.append("\t<tr>\n");
                sb.append("\t\t<td>");
                sb.append(db.getUserName(userId));
                sb.append("</td>\n");
                sb.append("\t\t<td><a href=\"javascript:projectRemoveUser(\'" + userId + "\',\'" + projectId +"\',\'" + project_title + "\')\">(remove)</a></td>\n");
                sb.append("\t</tr>\n");
            }

            sb.append("\t<form method=\"POST\">\n");
            sb.append("\t<tr>\n");
            sb.append("\t\t<td>Add User:</td>\n");
            sb.append("\t\t<td>");
            sb.append("<select name=user_id>\n");
            sb.append("\t\t\t<option value=\"0\">Create New User</option>\n");

            ResultSet rs2 = stmt.executeQuery(userSql);

            while (rs2.next()) {
                Integer userId = rs2.getInt("user_id");
                if (!projectUsers.contains(userId)) {
                    sb.append("\t\t\t<option value=\"" + userId + "\">" + db.getUserName(userId) +"</option>\n");
                }
            }



            sb.append("\t\t</select></td>\n");
            sb.append("\t</tr>\n");
            sb.append("\t<tr>\n");
            sb.append("\t\t<td><input type=\"hidden\" name=\"project_id\" value=\"" + projectId + "\"></td>\n");
            sb.append("\t\t<td><input type=\"button\" value=\"Submit\" onclick=\"projectUserSubmit(\'" + project_title + "\')\"></td>\n");
            sb.append("\t</tr>\n");
            sb.append("\t</form>\n");

        } catch (Exception e) {
            e.printStackTrace();
        }

        sb.append("</table>\n");
        return sb.toString();
    }
}

