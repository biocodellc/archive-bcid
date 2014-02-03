package bcid;

import edu.ucsb.nceas.ezid.EZIDService;
import util.SettingsManager;

import javax.swing.plaf.basic.BasicInternalFrameTitlePane;
import java.sql.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;

/**
 * Mint new projects.  Includes the automatic creation of a core set of entity types
 */
public class projectMinter {
    protected Connection conn;
    public ArrayList<Integer> projectResources;
    private SettingsManager sm;
    private EZIDService ezidAccount;


    /**
     * The constructor defines the class-level variables used when minting Projects.
     * It defines a generic set of entities (process, information content, objects, agents)
     * that can be used for any project.
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
     * mint Project
     *
     * @param project_code
     * @param project_title
     * @param strAbstract
     * @param users_id
     * @return
     */
    public Integer mint(
            String project_code,
            String project_title,
            String strAbstract,
            Integer users_id,
            Integer expedition_id) throws Exception {

        Integer project_id = null;

        /**
         *  Insert the values into the projects table
         */
        try {
            try {
                checkProjectCodeValid(project_code);
                checkProjectCodeAvailable(project_code, expedition_id);
            } catch (Exception e) {
                throw new Exception(e);
            }

            // Generate an internal ID to track this submission
            UUID internalID = UUID.randomUUID();

            // Use auto increment in database to assign the actual identifier.. this is threadsafe this way
            String insertString = "INSERT INTO projects " +
                    "(internalID, project_code, project_title, abstract, users_id, expedition_id) " +
                    "values (?,?,?,?,?,?)";

            PreparedStatement insertStatement = null;
            insertStatement = conn.prepareStatement(insertString);
            insertStatement.setString(1, internalID.toString());
            insertStatement.setString(2, project_code);
            insertStatement.setString(3, project_title);
            insertStatement.setString(4, strAbstract);
            insertStatement.setInt(5, users_id);
            insertStatement.setInt(6, expedition_id);
            insertStatement.execute();

            // Get the datasets_id that was assigned
            project_id = getProjectIdentifier(internalID);
        } catch (SQLException e) {
            //e.printStackTrace();
            throw new Exception(e.getMessage());
        }

        return project_id;
    }


    /**
     * Attach an individual URI reference to a project
     *
     * @param project_code
     * @param bcid
     * @throws Exception
     */
    public void attachReferenceToProject(String project_code, String bcid) throws Exception {
        Integer project_id = getProjectIdentifier(project_code);
        Integer datasetsId = new resolver(bcid).getDataGroupID();

        String insertString = "INSERT INTO projectsBCIDs " +
                "(project_id, datasets_id) " +
                "values (?,?)";

        PreparedStatement insertStatement = null;
        insertStatement = conn.prepareStatement(insertString);
        insertStatement.setInt(1, project_id);
        insertStatement.setInt(2, datasetsId);
        insertStatement.execute();
    }

    /**
     * Return the project identifier given the internalID
     *
     * @param datasetUUID
     * @return
     * @throws java.sql.SQLException
     */
    private Integer getProjectIdentifier(UUID datasetUUID) throws SQLException {
        Statement stmt = conn.createStatement();
        String sql = "select project_id from projects where internalID = '" + datasetUUID.toString() + "'";
        ResultSet rs = stmt.executeQuery(sql);
        try {
            rs.next();
            return rs.getInt("project_id");
        } catch (SQLException e) {
            return null;
        }
    }

    private Integer getProjectIdentifier(String project_code) throws SQLException {
        Statement stmt = conn.createStatement();
        String sql = "select project_id from projects where project_code = '" + project_code + "'";
        ResultSet rs = stmt.executeQuery(sql);
        try {
            rs.next();
            return rs.getInt("project_id");
        } catch (SQLException e) {
            return null;
        }
    }

    public Boolean projectExistsInExpedition(String project_code, Integer ExpeditionId) throws SQLException {
        Statement stmt = conn.createStatement();
        String sql = "select project_id from projects " +
                "where project_code = '" + project_code + "' && " +
                "expedition_id = " + ExpeditionId;
        ResultSet rs = stmt.executeQuery(sql);
        try {
            if (rs.next()) return true;
        } catch (SQLException e) {
            return false;
        }
        return false;
    }

    public String printMetadata(int id) throws SQLException {
        StringBuilder sb = new StringBuilder();
        Statement stmt = conn.createStatement();
        String sql = "select project_id,project_code,project_title,username from projects,users where users.user_id = projects.users_id && project_id =" + id;
        ResultSet rs = stmt.executeQuery(sql);
        sb.append("***project***");

        // Get result set meta data
        ResultSetMetaData rsmd = rs.getMetaData();
        int numColumns = rsmd.getColumnCount();

        while (rs.next()) {
            // Loop mapped values, now we know the type
            for (int i = 1; i <= numColumns; i++) {
                String val = rsmd.getColumnLabel(i);
                sb.append("\n" + val + " = " + rs.getString(val));
            }
        }
        return sb.toString();
    }

    public String printMetadataHTML(int id) throws SQLException {
        StringBuilder sb = new StringBuilder();
        Statement stmt = conn.createStatement();
        String sql = "select project_id,project_code,project_title,username from projects,users where users.user_id = projects.users_id && project_id =" + id;
        ResultSet rs = stmt.executeQuery(sql);
        sb.append("<table>");

        // Get result set meta data
        ResultSetMetaData rsmd = rs.getMetaData();
        int numColumns = rsmd.getColumnCount();

        while (rs.next()) {
            // Loop mapped values, now we know the type
            for (int i = 1; i <= numColumns; i++) {
                String val = rsmd.getColumnLabel(i);
                sb.append("<tr><td>" + val + "</td><td>" + rs.getString(val) + "</td></tr>");
            }
        }
        sb.append("</table>");
        return sb.toString();
    }


    public boolean userOwnsProject(Integer users_id, String project_code) throws SQLException {
        Statement stmt = conn.createStatement();
        //String sql = "select project_id,project_code,project_title,username from projects,users where users.user_id = projects.users_id && users.username =\"" + remoteUser + "\"";

        String sql = "SELECT " +
                "   count(*) as count " +
                "FROM " +
                "   projects " +
                "WHERE " +
                "   project_code='" + project_code + "' && " +
                "   users_id = " + users_id;

        ResultSet rs = stmt.executeQuery(sql);
        rs.next();
        if (rs.getInt("count") < 1)
            return false;
        else
            return true;
    }


    /**
     * Generate a Deep Links Format data file for describing a set of root prefixes and associated concepts
     *
     * @param project_code
     * @return
     * @throws java.sql.SQLException
     */
    public String getDeepRoots(String project_code) throws SQLException {
        // Get todays's date
        DateFormat dateFormat;
        dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = new Date();
        String project_title = null;

        StringBuilder sb = new StringBuilder();

        // Construct the query
        Statement stmt = conn.createStatement();
        String sql =
                "SELECT " +
                        " d.prefix as BCID, " +
                        " d.resourceType as resourceType," +
                        " a.project_title as project_title " +
                        "FROM " +
                        " projects a, projectsBCIDs b, datasets d " +
                        "WHERE" +
                        " a.project_id = b.project_id && " +
                        " b.datasets_id = d.datasets_id && \n" +
                        " a.project_code = '" + project_code + "'";

        // Write the concept/prefix elements section
        sb.append("[\n{\n\t\"data\": [\n");
        ResultSet rs = stmt.executeQuery(sql);
        while (rs.next()) {
            // Grap the project_title in the query
            if (project_title == null & !rs.getString("project_title").equals(""))
                project_title = rs.getString("project_title");

            // Grap the prefixes and concepts associated with this
            sb.append("\t\t{\n");
            sb.append("\t\t\t\"prefix\":\"" + rs.getString("BCID") + "\",\n");
            sb.append("\t\t\t\"concept\":\"" + rs.getString("resourceType") + "\"\n");
            sb.append("\t\t}");
            if (!rs.isLast())
                sb.append(",");

            sb.append("\n");
        }
        sb.append("\t]\n},\n");

        // Write the metadata section
        sb.append("{\n");
        sb.append("\t\"metadata\": {\n");
        sb.append("\t\t\"name\": \" " + project_code + "\",\n");
        if (project_title != null)
            sb.append("\t\t\"description\": \"" + project_title + "\",\n");
        sb.append("\t\t\"date\": \" " + dateFormat.format(date) + "\"\n");
        sb.append("\t}\n");
        sb.append("}\n");
        sb.append("]\n");
        return sb.toString();
    }

    public String projectTable(String remoteUser) throws SQLException {

        StringBuilder sb = new StringBuilder();
        Statement stmt = conn.createStatement();
        //String sql = "select project_id,project_code,project_title,username from projects,users where users.user_id = projects.users_id && users.username =\"" + remoteUser + "\"";

        String sql = "SELECT " +
                "   a.project_id as project_id," +
                "   a.project_code as project_code," +
                "   a.project_title as project_title," +
                "   d.prefix as BCID," +
                "   d.resourceType as resourceType " +
                "FROM " +
                "   projects a,projectsBCIDs b,datasets d,users u " +
                "WHERE " +
                "   a.project_id=b.project_id && " +
                "   b.datasets_id=d.datasets_id && " +
                "   a.users_id = u.user_id && " +
                "   u.username=\"" + remoteUser + "\"";

        ResultSet rs = stmt.executeQuery(sql);

        // Get result set meta data

        sb.append("<table>\n");
        sb.append("\t<tr>\n");
        sb.append("\t\t<td><b>Project Details</b></td>\n");
        sb.append("\t\t<td><b>Project BCIDs</b></td>\n");
        sb.append("\t</tr>\n");

        Integer project_id = 0;
        Integer thisProject_id = 0;
        int count = 0;
        while (rs.next()) {

            thisProject_id = rs.getInt("project_id");

            // Structure the first column-- projects
            if (thisProject_id != project_id) {
                if (count > 0) {
                    sb.append("\t\t\t</table>\n\t\t</td>\n");
                    sb.append("\t</tr>\n");
                }

                sb.append("\t<tr>\n");
                sb.append("\t\t<td valign=top>\n");
                sb.append("\t\t\t<table><tr><td>projectID " + rs.getString("project_id") + "</td></tr>" +
                        "<tr><td>" + rs.getString("project_code") + "</td></tr>" +
                        "<tr><td>" + rs.getString("project_title") + "</td></tr></table>\n");
                sb.append("\t\t</td>\n");

                sb.append("\t\t<td valign=top>\n\t\t\t<table>\n");
            } else {
                //sb.append("\n\t\t<td></td>\n");
            }

            // Structure the second column-- BCIDs associated with projects
            ResourceTypes rt = new ResourceTypes();
            String rtString;
            try {
                rtString = "<a href='" + rs.getString("resourceType") + "'>" + rt.get(rs.getString("resourceType")).string + "</a>";
            } catch (Exception e) {
                rtString = "<a href='" + rs.getString("resourceType") + "'>" + rs.getString("resourceType") + "</a>";
            }

            sb.append("\t\t\t\t<tr><td><a href='http://biscicol.org/id/" + rs.getString("BCID") + "'>" +
                    rs.getString("BCID") + "</a></td>" +
                    "<td>is_a</td><td>" +
                    rtString +
                    "</td></tr>\n");

            // Close the BCID section tag
            if (thisProject_id != project_id) {
                //if (count > 0) {
                //    sb.append("\n\t\t\t</table>");
                //    sb.append("\n\t\t</td>");
                //}
                project_id = thisProject_id;
            }
            count++;
            if (rs.isLast())
                sb.append("\t\t\t</table>\n\t\t</td>\n");
        }

        sb.append("\t</tr>\n</table>\n");

        return sb.toString();
    }


    public static void main(String args[]) {
        try {
            // See if the user owns this project or no
            projectMinter project = new projectMinter();
            //    System.out.println("validation XML for expedition = " +project.getValidationXML(1));
           /*
            if (project.projectExistsInExpedition("DEMOH", 1)) {
                System.out.println("project exists in expedition");
            } else {
                System.out.println("project does not exist in expedition");
            }
            */
            /*System.out.println(project.getDeepRoots("HDIM"));

            if (project.userOwnsProject(8, "DEMOG")) {
                System.out.println("YES the user owns this project");
            } else {
                System.out.println("NO the user does not own this project");
            }

*/
            // System.out.println(project.getLatestGraphsByProject(1));
            // Test associating a BCID to a project
            /*
            project.attachReferenceToProject("DEMOH", "ark:/21547/Fu2");
            */

            // Test creating a project

            Integer project_id = project.mint(
                    "DEMOH",
                    "Test creating project under an expedition for which it already exists",
                    null,
                    8, 4);

            System.out.println(project.printMetadata(project_id));

            //System.out.println(p.projectTable("demo"));

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Check that project code is between 4 and 6 characters
     *
     * @param project_code
     * @return
     */
    private void checkProjectCodeValid(String project_code) throws Exception {
        // Check project_code length
        if (project_code.length() < 4 || project_code.length() > 6)
            throw new Exception("Project code " + project_code + " must be between 4 and 6 characters long");
        // Check to make sure characters are normal!
        if (!project_code.matches("[a-zA-Z0-9]*")) {
            throw new Exception("Project code " + project_code + " contains invalid characters.");
        }
    }

    /**
     * Check that project code is no already in the database
     *
     * @param project_code
     * @return
     */
    private void checkProjectCodeAvailable(String project_code, Integer expedition_id) throws Exception {

        Statement stmt = conn.createStatement();
        String sql = "SELECT count(*) as count " +
                "FROM projects " +
                "WHERE project_code = '" + project_code + "' AND " +
                "expedition_id = " + expedition_id;
        ResultSet rs = stmt.executeQuery(sql);
        rs.next();
        Integer count = rs.getInt("count");
        if (count >= 1) {
            throw new Exception("Project code " + project_code + " already exists for this expedition.");
        }

    }
}
