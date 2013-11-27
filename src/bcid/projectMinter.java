package bcid;

import edu.ucsb.nceas.ezid.EZIDException;
import edu.ucsb.nceas.ezid.EZIDService;
import util.SettingsManager;

import java.math.BigInteger;
import java.sql.*;
import java.util.*;

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

        projectResources = new ArrayList<Integer>();
        // Populate projectResources with a default set of resources to use for all projects
        projectResources.add(ResourceTypes.DATASET);                    // Describe the project dataset itself
        projectResources.add(ResourceTypes.AGENT);                      // Agents (people, machines)
        projectResources.add(ResourceTypes.MATERIALSAMPLE);             // MaterialSample/Specimen descriptions
        projectResources.add(ResourceTypes.INFORMATIONCONTENTENTITY);   // InformationContentEntities (photos, text)
        projectResources.add(ResourceTypes.EVENT);                      // Event descriptions (collecting, assay)

        // Initialize settings manager

        sm = SettingsManager.getInstance();
        try {
            sm.loadProperties();
        } catch (Exception e) {
            e.printStackTrace();
        }
        /*
        // Initialize ezid account
        ezidAccount = new EZIDService();
        try {
            // Setup EZID account/login information
            System.out.println("eziduser" + sm.retrieveValue("eziduser"));
            System.out.println("ezidpass" + sm.retrieveValue("ezidpass"));

            ezidAccount.login(sm.retrieveValue("eziduser"), sm.retrieveValue("ezidpass"));

        } catch (EZIDException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        */
    }

    /**
     * mint Project
     *
     * @param project_code
     * @param project_title
     * @param strAbstract
     * @param bioValidator_validation_xml
     * @param users_id
     * @return
     */
    public Integer mint(
            String project_code,
            String project_title,
            String strAbstract,
            String ResolverWebAddress,
            String bioValidator_validation_xml,
            Integer users_id) throws Exception {

        Integer project_id = null;

        /**
         *  Insert the values into the projects table
         */
        try {
            // Generate an internal ID to track this submission
            UUID internalID = UUID.randomUUID();

            // Use auto increment in database to assign the actual identifier.. this is threadsafe this way
            String insertString = "INSERT INTO projects " +
                    "(internalID, project_code, project_title, abstract, bioValidator_validation_xml, users_id) " +
                    "values (?,?,?,?,?,?)";

            PreparedStatement insertStatement = null;
            insertStatement = conn.prepareStatement(insertString);
            insertStatement.setString(1, internalID.toString());
            insertStatement.setString(2, project_code);
            insertStatement.setString(3, project_title);
            insertStatement.setString(4, strAbstract);
            insertStatement.setString(5, bioValidator_validation_xml);
            insertStatement.setInt(6, users_id);
            insertStatement.execute();

            // Get the datasets_id that was assigned
            project_id = getProjectIdentifier(internalID);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new Exception(e.getMessage());
        }

        /**
         *  Create dataGroup identifiers to associate with this project
         */
        try {
            Iterator it = projectResources.iterator();
            while (it.hasNext()) {
                Integer resource = (Integer) it.next();
                ResourceTypes rt = new ResourceTypes();
                // Construct a default resolver web Address
                String thisResolverWebAddress = ResolverWebAddress + rt.get(resource).getShortName() + "/";

                // Mint a data group for each of these resources we are looping
                dataGroupMinter minterDataset = new dataGroupMinter(true, true);
                minterDataset.mint(
                        new Integer(sm.retrieveValue("bcidNAAN")),
                        users_id,
                        resource,
                        null,
                        thisResolverWebAddress,
                        project_title);
                minterDataset.close();
                Integer datasetsId = minterDataset.getDatasets_id();

                // Use auto increment in database to assign the actual identifier.. this is threadsafe this way
                String insertString = "INSERT INTO projectsBCIDs " +
                        "(project_id, datasets_id) " +
                        "values (?,?)";
                PreparedStatement insertStatement = null;
                insertStatement = conn.prepareStatement(insertString);
                insertStatement.setInt(1, project_id);
                insertStatement.setInt(2, datasetsId);
                insertStatement.execute();
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception(e.getMessage());
        }

        // Create EZIDs right away for the datagroups we just made
        manageEZID creator = null;
        try {
            creator = new manageEZID();
            creator.createDatasetsEZIDs(ezidAccount);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return project_id;
    }

    /**
     * Return the project identifier given the internalID
     *
     * @param datasetUUID
     * @return
     * @throws SQLException
     */
    private Integer getProjectIdentifier(UUID datasetUUID) throws SQLException {
        Statement stmt = conn.createStatement();
        String sql = "select project_id from projects where internalID = '" + datasetUUID.toString() + "'";
        ResultSet rs = stmt.executeQuery(sql);
        rs.next();
        return rs.getInt("project_id");
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
            String rtString = "<a href='" + rs.getString("resourceType") + "'>" + rt.get(rs.getString("resourceType")).string + "</a>";

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

    /**
     * Find the BCID that denotes the validation file location for a particular project
     *
     * @param project_code defines the BCID project_code to lookup
     * @return returns the BCID for this project and conceptURI combination
     */
    public String getValidationXML(String project_code) throws Exception {

        try {
            Statement stmt = conn.createStatement();

            String query = "select \n" +
                    "biovalidator_validation_xml \n" +
                    "from \n" +
                    "projects \n" +
                    "where \n" +
                    "project_code='" + project_code + "'";
            ResultSet rs = stmt.executeQuery(query);
            rs.next();
            return rs.getString("biovalidator_validation_xml");
        } catch (SQLException e) {
            throw new Exception("Trouble getting Validation XML", e);
        } catch (Exception e) {
            throw new Exception("Trouble getting Validation XML", e);
        }
    }

    public static void main(String args[]) {
        try {
            // Mint a project
            projectMinter project = new projectMinter();
            Integer project_id = project.mint(
                    "DEMOG",
                    "DEMO TITLE",
                    null,
                    "http://example.com/",
                    null,
                    8);

            System.out.println(project.printMetadata(project_id));

            //projectMinter p = new projectMinter();
            //System.out.println(p.projectTable("demo"));
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}
