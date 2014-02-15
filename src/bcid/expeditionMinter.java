package bcid;

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
public class expeditionMinter {
    protected Connection conn;
    public ArrayList<Integer> projectResources;
    private SettingsManager sm;


    /**
     * The constructor defines the class-level variables used when minting Projects.
     * It defines a generic set of entities (process, information content, objects, agents)
     * that can be used for any project.
     *
     * @throws Exception
     */
    public expeditionMinter() throws Exception {
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
     * Find the BCID that denotes the validation file location for a particular project
     *
     * @param expedition_id defines the expedition_id to lookup
     * @return returns the BCID for this project and conceptURI combination
     */
    public String getValidationXML(Integer expedition_id) throws Exception {

        try {
            Statement stmt = conn.createStatement();

            String query = "select \n" +
                    "biovalidator_validation_xml\n" +
                    "from \n" +
                    " expeditions\n" +
                    "where \n" +
                    "expedition_id=" + expedition_id;
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
     * List all the defined expeditions
     *
     * @return returns the BCID for this project and conceptURI combination
     */
    public String listExpeditions() throws Exception {
        StringBuilder sb = new StringBuilder();

        try {
            Statement stmt = conn.createStatement();

            String query = "SELECT \n" +
                    "\texpedition_id,\n" +
                    "\texpedition_code,\n" +
                    "\texpedition_title,\n" +
                    "\tbiovalidator_validation_xml\n" +
                    "FROM \n" +
                    "\texpeditions";
            ResultSet rs = stmt.executeQuery(query);

            sb.append("{\n");
            sb.append("\t\"expeditions\": [\n");
            while (rs.next()) {
                sb.append("\t\t{\n");
                sb.append("\t\t\t\"expedition_id\":\"" + rs.getString("expedition_id") + "\",\n");
                sb.append("\t\t\t\"expedition_code\":\"" + rs.getString("expedition_code") + "\",\n");
                sb.append("\t\t\t\"expedition_title\":\"" + rs.getString("expedition_title") + "\",\n");
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
     * A utility function to get the very latest graph loads for each project
     *
     * @param expedition_id pass in an expedition identifier to limit the set of projects we are looking at
     * @return
     */
    public String getLatestGraphs(int expedition_id) throws SQLException {
        StringBuilder sb = new StringBuilder();

        // Construct the query
        Statement stmt = conn.createStatement();
        // This query is built to give us a groupwise maximum-- we want the graphs that correspond to the
        // maximum timestamp (latest) loaded for a particular project.
        // Help on solving this problem came from http://jan.kneschke.de/projects/mysql/groupwise-max/
        String sql = "select p.project_code as project_code,p.project_title,d1.graph as graph,d1.ts as ts \n" +
                "from datasets as d1, \n" +
                "(select p.project_code as project_code,d.graph as graph,max(d.ts) as maxts \n" +
                "    \tfrom datasets d,projects p, projectsBCIDs pB\n" +
                "    \twhere pB.datasets_id=d.datasets_id\n" +
                "    \tand pB.project_id=p.project_id\n" +
                " and d.resourceType = \"http://purl.org/dc/dcmitype/Dataset\"\n" +
                "    and p.expedition_id = " + expedition_id + "\n" +
                "    \tgroup by p.project_code) as  d2,\n" +
                "projects p,  projectsBCIDs pB\n" +
                "where p.project_code = d2.project_code and d1.ts = d2.maxts\n" +
                " and pB.datasets_id=d1.datasets_id \n" +
                " and pB.project_id=p.project_id\n" +
                " and d1.resourceType = \"http://purl.org/dc/dcmitype/Dataset\"\n" +
                "    and p.expedition_id =" + expedition_id;

       // System.out.println(sql);
        sb.append("{\n\t\"data\": [\n");
        ResultSet rs = stmt.executeQuery(sql);
        while (rs.next()) {
            // Grap the prefixes and concepts associated with this
            sb.append("\t\t{\n");
            sb.append("\t\t\t\"project_code\":\"" + rs.getString("project_code") + "\",\n");
            sb.append("\t\t\t\"project_title\":\"" + rs.getString("project_title") + "\",\n");
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
            // See if the user owns this project or no
            expeditionMinter expedition = new expeditionMinter();
            System.out.println("results = \n" + expedition.getLatestGraphs(5));

        } catch (Exception e) {
            throw new Exception(e);
        }
    }
    /**
     * Return a JSON representation of the expeditions a user is an admin for
     * @param username
     * @return
     */
    public String listUserAdminExpeditions(String username) {
        StringBuilder sb = new StringBuilder();
        sb.append("[{");

        try {
            database db = new database();
            Integer user_id = db.getUserId(username);

            Statement stmt = conn.createStatement();
            String sql = "SELECT expedition_id, expedition_title FROM expeditions WHERE users_id = \"" + user_id + "\"";

            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                sb.append("\"" + rs.getInt("expedition_id") + "\":\"" + rs.getString("expedition_title") + "\",");
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
}

