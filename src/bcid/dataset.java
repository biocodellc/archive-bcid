package bcid;

import javax.ws.rs.core.Response;
import java.math.BigInteger;
import java.sql.*;
import java.util.UUID;

/**
 * This class mints shoulders for use in the BCID and EZID systems, defining datasets.
 * Minting dataset shoulders is important in establishing the ownership of particular data
 * elements.
 */
public class dataset extends datasetEncoder {

    // Mysql Connection
    protected Connection conn;
    protected String prefix = "";
    protected String bow = "";
    protected String scheme = "ark:";
    protected String shoulder = "";
    protected Integer datasets_id = null;
    private boolean ezidRequest;

    /**
     * Default to ezidRequest = false using default Constructor
     *
     * @throws Exception
     */
    protected dataset() throws Exception {
        this(false);
    }

    /**
     * Default constructor for dataset uses the temporary ARK ark:/99999/fk4.  Values can be overridden in
     * the mint method.
     *
     * @throws Exception
     */
    public dataset(boolean ezidRequest) throws Exception {
        database db = new database();
        conn = db.getConn();
        // Generate defaults in constructor, these will be overridden later
        shoulder = "fk4";
        setBow(99999);
        prefix = bow + shoulder;
        datasets_id = this.getDatasetId(prefix);
        this.ezidRequest = ezidRequest;
    }

    /**
     * Constructor for a dataset value that already exists in database, used to setup BCID minting
     *
     * @param NAAN
     * @param shoulder
     * @param ezidRequest
     * @throws Exception
     */
    public dataset(Integer NAAN, String shoulder, boolean ezidRequest) throws Exception {
        database db = new database();
        conn = db.getConn();
        setBow(NAAN);
        prefix = bow + shoulder;
        this.shoulder = shoulder;
        this.ezidRequest = ezidRequest;
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
     * @throws Exception
     */
    public dataset(Integer datasets_id) throws Exception {
        database db = new database();
        conn = db.getConn();
        Statement stmt = conn.createStatement();
        String sql = "select prefix,ezidRequest from datasets where datasets_id = '" + datasets_id.toString() + "'";
        ResultSet rs = stmt.executeQuery(sql);
        rs.next();
        prefix = rs.getString("prefix");
        ezidRequest = rs.getBoolean("ezidRequest");
        shoulder = encode(new BigInteger(datasets_id.toString()));
        Integer naan = new Integer(prefix.split("/")[1]);
        setBow(naan);
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
     * @throws Exception
     */
    public Integer mint(Integer NAAN, Integer who, Integer resourceType, String doi, String webaddress, String title) throws Exception {
        this.bow = scheme + "/" + NAAN + "/";

        // Generate an internal ID to track this submission
        UUID internalID = UUID.randomUUID();

        // Insert the values into the database
        try {
            // Use auto increment in database to assign the actual identifier.. this is threadsafe this way
            String insertString = "INSERT INTO datasets (users_id, resourceType, doi, webaddress, title, internalID, ezidRequest) " +
                    "values (?,?,?,?,?,?,?)";

            PreparedStatement insertStatement = null;
            insertStatement = conn.prepareStatement(insertString);
            insertStatement.setInt(1, who);
            insertStatement.setString(2, new ResourceTypes().get(resourceType).uri);
            insertStatement.setString(3, doi);
            insertStatement.setString(4, webaddress);
            insertStatement.setString(5, title);
            insertStatement.setString(6, internalID.toString());
            insertStatement.setBoolean(7, ezidRequest);

            insertStatement.execute();

            // Get the datasets_id that was assigned
            datasets_id = getDatasetIdentifier(internalID);

            // Update the shoulder, and hence prefix, now that we know the datasets_id
            String updateString = "UPDATE datasets SET prefix='" + bow.toString() + encode(new BigInteger(datasets_id.toString())) + "' WHERE datasets_id = " + datasets_id;
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
     * @return
     * @throws SQLException
     */
    private int getDatasetIdentifier(UUID datasetUUID) throws SQLException {
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
     * @return An Integer representing a dataset
     * @throws SQLException
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
     * @throws SQLException
     */
    public void close() {
        try {
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    public String datasetList(String username) {
        Statement stmt = null;
        Integer datasetId = null;
        StringBuilder sb = new StringBuilder();
        sb.append("[{");
        sb.append("\"0\":\"Add new group\"");
        try {
            stmt = conn.createStatement();
            String sql = "select d.datasets_id as datasets_id,d.prefix as prefix from datasets d, users u where u.username = '" + username + "' && " +
                    "d.users_id=u.user_id";
            ResultSet rs = stmt.executeQuery(sql);
            int count = 0;
            while (rs.next()) {
                sb.append(",\"" + rs.getInt("datasets_id") + "\":\"" + rs.getString("prefix") + "\"");
                count++;
            }
            sb.append("}]");

        } catch (SQLException e) {
            return null;
        }
        return sb.toString();
    }

    public static void main(String args[]) {
        try {
            dataset d  = new dataset();
            System.out.println(d.datasetList("biocode"));
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}