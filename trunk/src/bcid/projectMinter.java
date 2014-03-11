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

    public String listProjectConfig(Integer project_id, String username) {
        StringBuilder sb = new StringBuilder();
        sb.append("[{");

        try {
            database db = new database();
            Integer user_id = db.getUserId(username);

            Statement stmt = conn.createStatement();
            String sql = "SELECT project_title as title, abstract, bioValidator_validation_xml as validation_xml FROM projects WHERE project_id=\""
                         + project_id + "\" AND users_id=\"" + user_id + "\"";

            ResultSet rs = stmt.executeQuery(sql);
            if (rs.next()) {
                sb.append("\n\t\"title\": " + "\"" + rs.getString("title") + "\",\n");
                sb.append("\t\"abstract\": " + "\"" + rs.getString("abstract") + "\",\n");
                sb.append("\t\"validation_xml\": " + "\"" + rs.getString("validation_xml") + "\"\n");
            } else {
                sb.append("\"error\": \"You must be this project's admin in order to view its configuration\"");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        sb.append("}]");
        return sb.toString();
    }
}

