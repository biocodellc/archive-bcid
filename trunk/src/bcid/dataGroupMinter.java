package bcid;

import util.SettingsManager;

import javax.ws.rs.core.Response;
import java.math.BigInteger;
import java.net.URI;
import java.sql.*;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.UUID;

/**
 * This class mints shoulders for use in the  EZID systems known as data groups.
 * Minting data groups are important in establishing the ownership of particular data
 * elements.
 */
public class dataGroupMinter extends dataGroupEncoder {

    // Mysql Connection
    protected Connection conn;
    protected String prefix = "";
    protected String bow = "";
    protected String scheme = "ark:";
    protected String shoulder = "";
    protected String doi = "";
    protected String title = "";
    protected String ts = "";
    private Boolean suffixPassThrough = false;
    private Integer datasets_id = null;
    protected boolean ezidRequest;
    protected boolean ezidMade;
    protected String who = "";
    protected URI webAddress;
    public String projectCode;

    /**
     * Default to ezidRequest = false using default Constructor
     *
     * @throws Exception
     */
    public dataGroupMinter() throws Exception {
        this(false, false);
    }

    public Integer getDatasets_id() {
        return datasets_id;
    }

    public Boolean getSuffixPassThrough() {
        return suffixPassThrough;
    }

    public URI getWebAddress() {
        return webAddress;
    }


    /**
     * Default constructor for data group uses the temporary ARK ark:/99999/fk4.  Values can be overridden in
     * the mint method.
     *
     * @throws Exception
     */
    public dataGroupMinter(boolean ezidRequest, Boolean suffixPassThrough) throws Exception {
        database db = new database();
        conn = db.getConn();
        // Generate defaults in constructor, these will be overridden later
        shoulder = "fk4";
        setBow(99999);
        prefix = bow + shoulder;
        datasets_id = this.getDatasetId(prefix);
        this.ezidRequest = ezidRequest;
        this.suffixPassThrough = suffixPassThrough;
    }

    /**
     * Constructor for a dataset value that already exists in database, used to setup element minting
     *
     * @param NAAN
     * @param shoulder
     * @param ezidRequest
     *
     * @throws Exception
     */
    public dataGroupMinter(Integer NAAN, String shoulder, boolean ezidRequest, Boolean suffixPassThrough) throws Exception {
        database db = new database();
        conn = db.getConn();
        setBow(NAAN);
        prefix = bow + shoulder;
        this.shoulder = shoulder;
        this.ezidRequest = ezidRequest;
        this.suffixPassThrough = suffixPassThrough;
        try {
            datasets_id = this.getDatasetId(prefix);
        } catch (Exception e) {
            throw new Exception("problem getting shoulder " + e.getMessage());
        }
    }


    /**
     * create a minterDataset object by passing in a datasets_id.  An integer database value that we get immediately
     * after minting.
     *
     * @param datasets_id
     *
     * @throws Exception
     */
    public dataGroupMinter(Integer datasets_id) throws Exception {
        database db = new database();
        conn = db.getConn();
        Statement stmt = conn.createStatement();
        String sql = "SELECT " +
                "d.prefix as prefix," +
                "d.ezidRequest as ezidRequest," +
                "d.ezidMade as ezidMade," +
                "d.suffixPassthrough as suffixPassthrough," +
                "d.doi as doi," +
                "d.title as title," +
                "d.ts as ts, " +
                "CONCAT_WS(' ',u.firstName, u.lastName) as who, " +
                "d.webAddress as webAddress" +
                // NOTE: the projectCode query here fails if dataset has not been associated yet.
                // I removed the reference here so we can return just information on the datagroup and
                // not rely on any other dependencies.
                //"p.project_code as projectCode" +
                //" FROM datasets d, users u, projects p,expeditions e, expeditionsBCIDs eb " +
                " FROM datasets d, users u " +
                " WHERE d.datasets_id = '" + datasets_id.toString() + "'" +
                " AND d.users_id = u.user_id ";
        //" AND d.`datasets_id`=eb.datasets_id " +
        //" AND eb.expedition_id=e.expedition_id " +
        //" AND e.project_id=p.project_id";

        ResultSet rs = stmt.executeQuery(sql);
        rs.next();
        prefix = rs.getString("prefix");
        identifier = new URI(prefix);
        ezidRequest = rs.getBoolean("ezidRequest");
        ezidMade = rs.getBoolean("ezidMade");
        shoulder = encode(new BigInteger(datasets_id.toString()));
        this.doi = rs.getString("doi");
        this.title = rs.getString("title");
        //this.projectCode = rs.getString("projectCode");
        this.ts = rs.getString("ts");
        this.who = rs.getString("who");
        Integer naan = new Integer(prefix.split("/")[1]);
        this.datasets_id = datasets_id;
        this.suffixPassThrough = rs.getBoolean("suffixPassthrough");
        setBow(naan);

        try {
            this.webAddress = new URI(rs.getString("webAddress"));
        } catch (NullPointerException e) {
            this.webAddress = null;
        }
    }


    /**
     * Get the projectCode given a datasets_id
     *
     * @param datasets_id
     *
     * @throws Exception
     */
    public String getProject(Integer datasets_id) throws Exception {
        database db = new database();
        conn = db.getConn();
        Statement stmt = conn.createStatement();
        String sql = "select p.project_code from projects p, expeditionsBCIDs eb, expeditions e, datasets d where d.datasets_id = eb.datasets_id and e.expedition_id=eb.`expedition_id` and e.`project_id`=p.`project_id` and d.datasets_id=" + datasets_id;

        try {
            ResultSet rs = stmt.executeQuery(sql);
            rs.next();
            return rs.getString("project_code");
        } catch (Exception e) {
            return null;
        }  finally {
        }
    }

    /**
     * Set the bow using this method always
     *
     * @param naan
     */
    private void setBow(Integer naan) {
        this.bow = scheme + "/" + naan + "/";
    }

    /**
     * Tells us if the ezidRequest object is true or false
     *
     * @return true or false
     */
    public boolean isEzidRequest() {
        return ezidRequest;
    }

    /**
     * Mint a dataset, providing information to insert into database
     *
     * @param NAAN
     * @param who
     * @param resourceType
     * @param doi
     * @param webaddress
     * @param title
     *
     * @throws Exception
     */
    public Integer mint(Integer NAAN, Integer who, String resourceType, String doi, String webaddress, String graph, String title) throws Exception {

        database db = new database();

        // Never request EZID for user=demo
        if (db.getUserName(who).equalsIgnoreCase("demo")) {
            ezidRequest = false;
        }
        this.bow = scheme + "/" + NAAN + "/";

        // Generate an internal ID to track this submission
        UUID internalID = UUID.randomUUID();

        // Insert the values into the database
        try {
            // Use auto increment in database to assign the actual identifier.. this is threadsafe this way
            String insertString = "INSERT INTO datasets (users_id, resourceType, doi, webaddress, graph, title, internalID, ezidRequest, suffixPassThrough) " +
                    "values (?,?,?,?,?,?,?,?,?)";

            PreparedStatement insertStatement = null;
            insertStatement = conn.prepareStatement(insertString);
            insertStatement.setInt(1, who);
            insertStatement.setString(2, resourceType);
            insertStatement.setString(3, doi);
            insertStatement.setString(4, webaddress);
            insertStatement.setString(5, graph);
            insertStatement.setString(6, title);
            insertStatement.setString(7, internalID.toString());
            insertStatement.setBoolean(8, ezidRequest);
            insertStatement.setBoolean(9, suffixPassThrough);
            insertStatement.execute();

            // Get the datasets_id that was assigned
            datasets_id = getDatasetIdentifier(internalID);

            // Update the shoulder, and hence prefix, now that we know the datasets_id
            String updateString = "UPDATE datasets " +
                    " SET prefix='" + bow.toString() + encode(new BigInteger(datasets_id.toString())) + "'" +
                    " WHERE datasets_id = " + datasets_id;
            Statement u = conn.createStatement();
            u.execute(updateString);

        } catch (SQLException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        // Create the shoulder identifier (String dataset identifier)
        shoulder = encode(new BigInteger(datasets_id.toString()));

        // Create the prefix
        prefix = bow + shoulder;

        return datasets_id;
    }

    /**
     * Return the dataset identifier  given the internalID
     *
     * @param datasetUUID
     *
     * @return
     *
     * @throws java.sql.SQLException
     */
    private Integer getDatasetIdentifier(UUID datasetUUID) throws SQLException {
        Statement stmt = conn.createStatement();
        String sql = "select datasets_id from datasets where internalID = '" + datasetUUID.toString() + "'";
        ResultSet rs = stmt.executeQuery(sql);
        rs.next();
        return rs.getInt("datasets_id");
    }

    /**
     * Check to see if a dataset exists or not
     *
     * @param prefix
     *
     * @return An Integer representing a dataset
     *
     * @throws java.sql.SQLException
     */
    public Integer getDatasetId(String prefix) {
        Statement stmt = null;
        Integer datasetId = null;
        try {
            stmt = conn.createStatement();
            String sql = "select datasets_id from datasets where prefix = '" + prefix + "'";
            ResultSet rs = stmt.executeQuery(sql);
            rs.next();
            datasetId = rs.getInt("datasets_id");
        } catch (SQLException e) {
            return null;
        }
        return datasetId;
    }

    public String getPrefix() {
        return prefix;
    }

    /**
     * Close the SQL connection
     *
     * @throws java.sql.SQLException
     */
    public void close() {
        try {
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    /**
     * Get the resourcetype defined for a particular dataset
     *
     * @return
     */
    public String getResourceType() {
        Statement stmt;
        try {
            stmt = conn.createStatement();

            String sql = "select d.resourceType as resourceType from datasets d where d.datasets_id = " + datasets_id;
            ResultSet rs = stmt.executeQuery(sql);
            rs.next();
            return rs.getString("resourceType");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Return a JSON representation of a datasetList
     * TODO: find a more appropriate spot for this
     *
     * @param username
     *
     * @return
     */
    public String datasetList(String username) {
        Statement stmt = null;
        Integer datasetId = null;
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"0\":\"Create new group\"");
        try {
            stmt = conn.createStatement();
            String sql = "select d.datasets_id as datasets_id,concat_ws(' ',prefix,title) as prefix from datasets d, users u where u.username = '" + username + "' && " +
                    "d.users_id=u.user_id";
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                sb.append(",\"" + rs.getInt("datasets_id") + "\":\"" + rs.getString("prefix") + "\"");
            }
            sb.append("}");

        } catch (SQLException e) {
            return null;
        }
        return sb.toString();
    }

    /**
     * Return an HTML table of datasets owned by a particular user
     * TODO: find a more appropriate spot for this
     *
     * @param username
     *
     * @return
     */
    public String datasetTable(String username) {
        Statement stmt = null;
        Integer datasetId = null;
        ResourceTypes rts = new ResourceTypes();

        StringBuilder sb = new StringBuilder();
        try {
            stmt = conn.createStatement();
            String sql = "SELECT \n\t" +
                    "d.datasets_id as datasets_id," +
                    "prefix," +
                    "ifnull(title,'') as title," +
                    "ifnull(doi,'') as doi," +
                    "ifnull(webaddress,'') as webaddress," +
                    "ifnull(resourceType,'') as resourceType," +
                    "suffixPassthrough as suffixPassthrough " +
                    "\nFROM\n\t" +
                    "datasets d, users u " +
                    "\nWHERE\n\t" +
                    "u.username = '" + username + "' && " +
                    "d.users_id=u.user_id";

            ResultSet rs = stmt.executeQuery(sql);
            sb.append("<table>\n");
            sb.append("\t");
            sb.append("<tr>");
            sb.append("<th>BCID</th>");
            sb.append("<th>Title</th>");
            //sb.append("<th>DOI</th>");
            //sb.append("<th>webAddress</th>");
            sb.append("<th>resourceType</th>");
            sb.append("<th>Follow Suffixes</th>");

            sb.append("</tr>\n");
            while (rs.next()) {
                sb.append("\t<tr>");
                //sb.append("<td>" + getEZIDLink(rs.getString("prefix"), username) + " " + getEZIDMetadataLink(rs.getString("prefix"), username) + "</td>");
                sb.append("<td>" +
                        rs.getString("prefix") +
                        " " +
                        // Normally we would use resolverMetadataPrefix here but i'm stripping the host so this
                        // can be more easily tested on localhost
                        "(<a href='javascript:void();' class='edit' data-ark='" + rs.getString("prefix") + "'>edit</a>)" +
                        "</td>");

                sb.append("<td>" + rs.getString("title") + "</td>");
                //sb.append("<td>" + getDOILink(rs.getString("doi")) + " " + getDOIMetadataLink(rs.getString("doi")) + "</td>");
                //sb.append("<td>" + rs.getString("webaddress") + "</td>");

                try {
                    sb.append("<td><a href='" + rs.getString("resourceType") + "'>" + rts.get(rs.getString("resourceType")).string + "</a></td>");
                } catch (Exception e) {
                    sb.append("<td><a href='" + rs.getString("resourceType") + "'>" + rs.getString("resourceType") + "</a></td>");
                }
                sb.append("<td>" + rs.getBoolean("suffixPassthrough") + "</td>");

                sb.append("</tr>\n");
            }
            sb.append("\n</table>");

        } catch (SQLException e) {
            return null;
        }
        return sb.toString();
    }

    /**
     * return a BCID formatted with LINK
     *
     * @param pPrefix
     *
     * @return
     */
    public String getEZIDLink(String pPrefix, String username, String linkText) {
        if (!username.equals("demo")) {
            return "(<a href='http://n2t.net/" + pPrefix + "'>" + linkText + "</a>)";
        } else {
            return "";
        }
    }

    /**
     * return a BCID formatted with LINK
     *
     * @param pPrefix
     *
     * @return
     */
    public String getEZIDMetadataLink(String pPrefix, String username, String linkText) {
        if (!username.equals("demo")) {
            return "(<a href='http://ezid.cdlib.org/id/" + pPrefix + "'>" + linkText + "</a>)";
        } else {
            return "";
            //return "(<a href='" + resolverTargetPrefix + pPrefix + "'>metadata</a>)";
        }
    }

    /**
     * return a DOI formatted with LINK
     *
     * @param pDOI
     *
     * @return
     */
    public String getDOILink(String pDOI) {
        if (pDOI != null && !pDOI.trim().equals("")) {
            return "<a href='http://dx.doi.org/" + pDOI + "'>http://dx.doi.org/" + pDOI + "</a>";
        } else {
            return "";
        }
    }

    /**
     * Return a Metadata link for DOI
     *
     * @param pDOI
     *
     * @return
     */
    public String getDOIMetadataLink(String pDOI) {
        if (pDOI != null && !pDOI.trim().equals("")) {
            return "(<a href='http://data.datacite.org/text/html/" + pDOI.replace("doi:", "") + "'>metadata</a>)";
        } else {
            return "";
        }
    }

    public static void main(String args[]) {
        try {
            //dataGroupMinter d = new dataGroupMinter();
            //System.out.println(d.datasetTable("biocode"));
            dataGroupMinter d = new dataGroupMinter(913);
            System.out.println(d.projectCode);
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    /**
     * fetch a BCID's configuration given a prefix and username
     *
     * @param prefix
     * @param username
     *
     * @return
     */
    public Hashtable<String, String> getDataGroupConfig(String prefix, String username) {
        Hashtable<String, String> config = new Hashtable<String, String>();
        ResourceTypes rts = new ResourceTypes();
        try {
            database db = new database();
            Integer userId = db.getUserId(username);

            String sql = "SELECT suffixPassThrough as suffix, doi, title, webaddress, resourceType " +
                    "FROM datasets WHERE BINARY prefix = ? AND users_id = \"" + userId + "\"";
            PreparedStatement stmt = conn.prepareStatement(sql);

            stmt.setString(1, prefix);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                config.put("suffix", String.valueOf(rs.getBoolean("suffix")));
                if (rs.getString("doi") != null) {
                    config.put("doi", rs.getString("doi"));
                }
                if (rs.getString("title") != null) {
                    config.put("title", rs.getString("title"));
                }
                if (rs.getString("webaddress") != null) {
                    config.put("webaddress", rs.getString("webaddress"));
                }

                try {
                    config.put("resourceType", rts.get(rs.getString("resourceType")).string);
                } catch (Exception e) {
                    config.put("resourceType", rs.getString("resourceType"));
                }

            } else {
                config.put("error", "Dataset not found. Are you the owner of this dataset?");
            }
        } catch (Exception e) {
            e.printStackTrace();
            config.put("error", "server error");
        }
        return config;
    }

    /**
     * update a BCID's configuration
     *
     * @param config a Hashtable<String, String> which has the datasets table fields to be updated as key, new value
     *               pairs
     * @param prefix the ark:// for the BICD
     *
     * @return
     */
    public Boolean updateDataGroupConfig(Hashtable<String, String> config, String prefix, String username) {
        try {
            database db = new database();
            Integer userId = db.getUserId(username);
            String sql = "UPDATE datasets SET ";

            // update resourceTypeString to the correct uri
            if (config.containsKey("resourceTypeString")) {
                config.put("resourceType", new ResourceTypes().getByShortName(config.get("resourceTypeString")).uri);
                config.remove("resourceTypeString");
            }

            // Dynamically create our UPDATE statement depending on which fields the user wants to update
            for (Enumeration e = config.keys(); e.hasMoreElements(); ) {
                String key = e.nextElement().toString();
                sql += key + " = ?";

                if (e.hasMoreElements()) {
                    sql += ", ";
                } else {
                    sql += " WHERE BINARY prefix =\"" + prefix + "\" AND users_id =\"" + userId + "\";";
                }
            }

            PreparedStatement stmt = conn.prepareStatement(sql);

            // place the parametrized values into the SQL statement
            {
                int i = 1;
                for (Enumeration e = config.keys(); e.hasMoreElements(); ) {
                    String key = e.nextElement().toString();
                    if (key.equals("suffixPassthrough")) {
                        if (config.get(key).equalsIgnoreCase("true")) {
                            stmt.setBoolean(i, true);
                        } else {
                            stmt.setBoolean(i, false);
                        }
                    } else if (config.get(key).equals("")) {
                        stmt.setString(i, null);
                    } else {
                        stmt.setString(i, config.get(key));
                    }
                    i++;
                }
            }

            Integer result = stmt.executeUpdate();
            // result should be '1', if not, an error occurred during the UPDATE statement
            if (result >= 1) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * return an HTML table to edit a bcid's config
     *
     * @param username
     * @param prefix
     *
     * @return
     */
    public String bcidEditorAsTable(String username, String prefix) {
        StringBuilder sb = new StringBuilder();
        Hashtable<String, String> config = getDataGroupConfig(prefix, username);

        sb.append("<form method=\"POST\" id=\"dataGroupEditForm\">\n");
        sb.append("\t<input type=hidden name=resourceTypes id=resourceTypes value=\"Dataset\">\n");
        sb.append("\t<table>\n");

        sb.append("\t\t<tr>\n");
        sb.append("\t\t\t<td align=\"right\">Title*</td>\n");
        sb.append("\t\t\t<td><input name=\"title\" type=\"textbox\" size=\"40\" value=\"");
        sb.append(config.get("title"));
        sb.append("\"></td>\n");
        sb.append("\t\t</tr>\n");

        sb.append("\t\t<tr>\n");
        sb.append("\t\t\t<td align=\"right\"><a href='/bcid/concepts.jsp'>Concept*</a></td>\n");
        sb.append("\t\t\t<td><select name=\"resourceTypesMinusDataset\" id=\"resourceTypesMinusDataset\" data-resource_type=\"");
        sb.append(config.get("resourceType"));
        sb.append("\"></select></td>\n");
        sb.append("\t\t</tr>\n");

        sb.append("\t\t<tr>\n");
        sb.append("\t\t\t<td align=\"right\">Target URL</td>\n");
        sb.append("\t\t\t<td><input name=\"webaddress\" type=\"textbox\" size=\"40\" value=\"");
        sb.append(config.get("webaddress"));
        sb.append("\"></td>\n");
        sb.append("\t\t</tr>\n");

        sb.append("\t\t<tr>\n");
        sb.append("\t\t\t<td align=\"right\">DOI</td>\n");
        sb.append("\t\t\t<td><input name=\"doi\" type=\"textbox\" size=\"40\" value=\"");
        sb.append(config.get("doi"));
        sb.append("\"></td>\n");
        sb.append("\t\t</tr>\n");

        sb.append("\t\t<tr>\n");
        sb.append("\t\t\t<td align=\"right\">Rights</td>\n");
        sb.append("\t\t\t<td><a href=\"http://creativecommons.org/licenses/by/3.0/\">Creative Commons Attribution 3.0</a></td>");
        sb.append("\t\t</tr>\n");

        sb.append("\t\t<tr>\n");
        sb.append("\t\t\t<td align=\"right\">Follow Suffixes</td>\n");
        sb.append("\t\t\t<td><input name=\"suffixPassThrough\" type=\"checkbox\"");
        if (config.get("suffix").equalsIgnoreCase("true")) {
            sb.append(" checked=\"checked\"");
        }
        sb.append("\"></td>\n");
        sb.append("\t\t</tr>\n");

        sb.append("\t\t<tr>\n");
        sb.append("\t\t\t<td></td>\n");
        sb.append("\t\t\t<td class=\"error\"></td>\n");
        sb.append("\t\t</tr>\n");

        sb.append("\t\t<tr>\n");
        sb.append("\t\t\t<td><input type=\"hidden\" name=\"prefix\" value=\"" + prefix + "\"></td>\n");
        sb.append("\t\t\t<td><input type=\"button\" value=\"Submit\" onclick=\"dataGroupEditorSubmit();\" /><input type=\"button\" id=\"cancelButton\" value=\"Cancel\" /></td>\n");
        sb.append("\t\t</tr>\n");

        sb.append("\t</table>\n");
        sb.append("</form>\n");

        return sb.toString();
    }
}