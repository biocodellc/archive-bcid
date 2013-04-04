package bcid;

import util.SettingsManager;

import java.lang.Exception;
import java.lang.Integer;
import java.lang.NullPointerException;
import java.lang.String;
import java.lang.System;
import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.UUID;

/**
 * the bcidMinter class is the mechanism by which bcids interact with the mysql backend database.
 * "Mint" means that the identifier is
 * created by the application and "create" means that the identifier is created outside the application.  Thus,
 * bcid is "minting" and EZID "creates" the identifiers.  bcidMinter extends dataset and thus relies
 * on the notion of a dataset to be in existence before creating ANY identifier.
 * <p/>
 * Options when working with this class include:
 * 1) uuid/EZID:  creating uuids EZIDs, which is essentially an ARK representation of the uuid (suffixPassthrough=true;ezidRequest=true)
 * 2) bcid: a bcid with no EZID request for pre-generating identifiers (suffixPassthrough=false; ezidRequest=false)
 * 3) EZID: a bcid with a request for creating an EZID. (suffixPassthrough=false;ezidRequest=true)
 */
public class bcidMinter extends dataset {


    // some number to start with
    // This could be 0 in most cases, but for testing and in cases where we already created a test encoded EZID
    // this can be set manually here to bypass existing EZIDS
    private Integer startingNumber;

    static int TRUE = 1;
    static int FALSE = 0;

    /**
     * TEST Case, uses test case dataset.
     */
    public bcidMinter(boolean ezidRequest) throws Exception {
        super(ezidRequest);
        init();
    }

    /**
     * This constructor is used to prepare for minting BCID data elements when the dataset is known by
     * its NAAN + shoulder
     *
     * @param NAAN
     * @param shoulder
     * @param ezidRequest
     * @throws Exception
     */
    public bcidMinter(Integer NAAN, String shoulder, boolean ezidRequest) throws Exception {
        super(NAAN, shoulder, ezidRequest);
        init();
    }

    /**
     * This constructor is used to prepare for minting BCID data elements when the dataset is known by its dataset_id
     * @throws Exception
     */
    public bcidMinter(Integer dataset_id) throws Exception {
        super(dataset_id);
        init();
    }

    /**
     * Initialize stuff
     *
     * @throws Exception
     */
    private void init() throws Exception {
        try {
            SettingsManager sm = SettingsManager.getInstance();
            sm.loadProperties();
            startingNumber = Integer.parseInt(sm.retrieveValue("bcidStartingNumber"));
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("Exception in init() function " + e.getMessage());
        }
    }


    /**
     * Set the starting number for AutoIncrement
     */
    private void setAutoIncrement() {
        String alterString = "ALTER TABLE identifiers AUTO_INCREMENT = ?";
        PreparedStatement alterStatement = null;
        try {
            alterStatement = conn.prepareStatement(alterString);
            alterStatement.setInt(1, startingNumber);
            alterStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    /**
     * Get a UniqueDataSetID to identify the set of elements inserted at one time
     *
     * @return
     * @throws URISyntaxException
     */
    private String generateUUIDString() throws URISyntaxException {
        return UUID.randomUUID().toString();
    }

    /**
     * Mint a Single bcid
     * Takes a bcid object and inserts these objects into bcid database.
     * Inserts bcid class members: DOI, webAddress, sourceID, resourceType
     *
     * @param b
     * @throws Exception
     */
    public void mint(bcid b, boolean suffixPassthrough, boolean isUUID) throws Exception {
        ArrayList<bcid> arrayList = new ArrayList<bcid>();
        arrayList.add(b);

        mintList(arrayList, suffixPassthrough, isUUID);
    }

    /**
     * Delete identifiers in table for a particular loadedSetuuid
     *
     * @param uuid
     * @return an int representing results from the executeUpdate command, i believe indicating the number
     *         of items deleted.
     */
    public int deleteLoadedSetUUID(String uuid) {
        try {
            Statement stmt = conn.createStatement();
            String sql = "DELETE FROM identifiers WHERE loadedSetUUID='" + uuid + "'";
            return stmt.executeUpdate(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Mint a group of bcids (see single bcid for further explanation)
     *
     * @param bcidList
     * @param suffixPassthrough
     * @param isUUID
     * @return returns a DatasetIdentifier String
     * @throws Exception
     */
    public String mintList(ArrayList bcidList, boolean suffixPassthrough, boolean isUUID) throws Exception {
        // First validate the list before doing anything if this is uuids
        if (suffixPassthrough && isUUID) {
            Iterator validateIds = bcidList.iterator();
            while (validateIds.hasNext()) {
                bcid id = (bcid) validateIds.next();
                if (!validateUUID(id.sourceID)) {
                    throw new Exception("One or more invalid Identifiers, violating either checksum, uuid construction, or Uniqueness of uuid rules: " + id.sourceID);
                }
            }
        }

        // A single ID to refer to this list of elements that are being inserted in the database
        String loadedSetUUID = this.generateUUIDString();

        // Turn off autocommits just for this method
        conn.setAutoCommit(false);
        PreparedStatement insertStatement = null;

        try {
            // Use auto increment in database to assign the actual identifier.. this is threadsafe this way
            // Also, use auto date assignment feature for when this was applied.
            StringBuffer sql = new StringBuffer("INSERT INTO identifiers ( webaddress, localid, what, loadedSetUUID, suffixPassthrough, datasets_id) " +
                    "values (?,?,?,?,?,?)");
            for (int i = 1; i < bcidList.size(); i++) {
                sql.append(",(?,?,?,?,?,?)");
            }
            insertStatement = conn.prepareStatement(sql.toString());

            Iterator ids = bcidList.iterator();
            int count = 1;
            while (ids.hasNext()) {
                bcid id = (bcid) ids.next();

                if (id.webAddress != null)
                    insertStatement.setString(count++, id.webAddress.toString());
                else
                    insertStatement.setString(count++, null);
                insertStatement.setString(count++, id.sourceID);
                insertStatement.setString(count++, id.resourceType.uri);
                insertStatement.setString(count++, loadedSetUUID.toString());
                insertStatement.setBoolean(count++, suffixPassthrough);
                insertStatement.setInt(count++, datasets_id);

                // Execute a commit at every 10000 rows
                /*if (count + 1 % 10000 == 0) {
                   insertStatement.execute();
                    conn.commit();
                }
                count++;
                */
            }
            insertStatement.execute();
            insertStatement.close();
            conn.commit();
        } finally {
            insertStatement.close();
            conn.setAutoCommit(true);
        }

        return loadedSetUUID.toString();
    }

    /**
     * Returns an arrayList of encoded identifiers given a dataset identifier
     * The dataset identifier indicates a batch of identifers added all at the same time
     *
     * @param dataset
     * @return An ArrayList of identifiers
     */

    public ArrayList getIdentifiers(String dataset) {
        ArrayList results = new ArrayList();
        try {
            Statement stmt = conn.createStatement();
            String sql = "SELECT " +
                    "i.identifiers_id as id," +
                    "i.suffixPassthrough as suffixPassthrough," +
                    "d.prefix as prefix," +
                    "i.localid as localid" +
                    " FROM identifiers as i, datasets as d " +
                    " WHERE i.loadedSetUUID = '" + dataset + "'" +
                    " AND i.datasets_id=d.datasets_id";
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                // If this is suffixPassthrough then use prefix + localid
                if (rs.getBoolean("suffixPassthrough")) {
                    results.add(rs.getString("prefix") + "/" + rs.getString("localid"));
                    // else use the current encode function
                } else {
                    results.add(new bcidEncoder(prefix).encode(new BigInteger(rs.getString("id"))));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return results;
    }

    /**
     * Return the next available start number from the mysql database as a BigInteger
     * Note that this method is probably not needed with the Mysql Auto_Increment
     *
     * @return
     * @throws Exception
     */
    private BigInteger start() throws Exception {
        BigInteger big = null;
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("select max(identifiers_id) as maxid from identifiers");

            if (rs.next()) {

                try {
                    big = new BigInteger(rs.getString("maxid"));
                } catch (NullPointerException e) {
                    // In case this is the very first, returning NULL means there are no integers here
                    // start with some number, set by class
                    big = new BigInteger(startingNumber.toString());
                }

            } else {
                throw new Exception();
            }

            // Add 1 to the start value
            return big.add(new BigInteger("1"));

        } catch (SQLException e) {
            throw new Exception("Unable to find start");
        }
    }


    /**
     * Create a bunch of bcids.  These are not yet EZIDs but we will create a place-holder for them
     * in our database.  If the user that requests these returns to the triplifier then we will turn them
     * into EZIDs for them at that time.  As such, all that is needed to register these is an email address
     * of a responsible party.
     *
     * @param numIdentifiers
     * @return An ArrayList of all the GUIDs
     * @throws SQLException
     * @throws URISyntaxException
     */
    public String createBCIDs(int numIdentifiers, URI what) throws SQLException, URISyntaxException {
        String datasetIdentifier = this.generateUUIDString();

        // Turn off autocommits just for this method
        conn.setAutoCommit(false);
        PreparedStatement insertStatement = null;

        try {
            // Use auto increment in database to assign the actual identifier.. this is threadsafe this way
            // Also, use auto date assignment feature for when this was applied.
            String insertString = "INSERT INTO identifiers(ezidRequest,what,loadedSetUUID, datasets_id) " +
                    "values (?,?,?,?)";
            insertStatement = conn.prepareStatement(insertString);

            int count = 0;
            while (count < numIdentifiers) {
                insertStatement.setInt(1, FALSE);
                insertStatement.setString(2, what.toString());
                insertStatement.setString(3, datasetIdentifier.toString());
                insertStatement.setInt(4, datasets_id);
                insertStatement.addBatch();
                // Execute a commit at every 1000 rows
                if (count + 1 % 1000 == 0) {
                    insertStatement.executeBatch();
                    conn.commit();
                }
                count++;
            }
            // Execute remainder as batch
            insertStatement.executeBatch();
            conn.commit();
        } finally {
            insertStatement.close();
            conn.setAutoCommit(true);
        }

        return datasetIdentifier.toString();
    }


    /**
     * validate uuid, to make sure it conforms to the generic structure expected of uuids
     *
     * @param uuid
     * @return
     */
    private boolean validateUUID(String uuid) {
        if (uuid == null) return false;
        if (uuid.matches("[0-9a-zA-Z]{8}-[0-9a-zA-Z]{4}-[0-9a-zA-Z]{4}-[0-9a-zA-Z]{4}-[0-9a-zA-Z]{12}"))
            return true;
        return false;
    }

    /**
     * Generate an ARK version for this uuid, by appending the uuid onto the ARK prefix
     *
     * @param uuidAsString
     * @return A full ARK representation of this ID
     * @throws Exception
     */
    public String createUUIDARK(String uuidAsString) throws Exception {
        // Validate this UUID
        if (!validateUUID(uuidAsString)) {
            throw new Exception("Invalid uuid: " + uuidAsString);
        }
        return prefix + "/" + UUID.fromString(uuidAsString).toString();
    }

    /**
     * Use main function for testing
     *
     * @param args
     */
    public static void main(String args[]) {
        SettingsManager sm = SettingsManager.getInstance();
        try {
            sm.loadProperties();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Integer naan = new Integer(sm.retrieveValue("bcidNAAN"));

        // Create the shoulder
        dataset minterDataset = null;
        try {

           /*
           minterDataset = new dataset();
           minterDataset.mint(
                    naan,
                    1,
                    new ResourceTypes().RESOURCE,
                    null,
                    null,
                    null);
                    */
            minterDataset = new dataset();
            System.out.println("Using dataset  = " + minterDataset.prefix);
        //} catch (URISyntaxException e) {
        //    e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }


        // Check to see if this exists
        System.out.println(minterDataset.getDatasetId(minterDataset.prefix));

        // Encode an BigInteger
        String value = "105";
        System.out.println("Encode BigInteger = " + value);

        String myIdentifier = new bcidEncoder(minterDataset.prefix).encode(new BigInteger(value));
        System.out.println("  " + myIdentifier);
               //myIdentifier = "ark:/99999/fk4/aQH";
        // Decode an Identifier
        System.out.println("Decode Identifier = " + myIdentifier);
        try {
            BigInteger bigInt = new bcidEncoder(minterDataset.prefix).decode(myIdentifier);
            System.out.println("  Decoded BCID Value = " + bigInt);
            System.out.println("  Decoded dataset Value = "+ new datasetEncoder().decode(myIdentifier));
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Decode an Identifier with LocalID
        System.out.println("Decode bcid = " + myIdentifier);
        try {
            BigInteger bigInt = new bcidEncoder(minterDataset.prefix).decode(myIdentifier);
            System.out.println("  Decoded Value = " + bigInt);
        } catch (Exception e) {
            e.printStackTrace();
        }


        // Attempt to Decode a uuid (we don't want to do this, just check behaviour)
        //myIdentifier = "ark:/99999/fk409d5a2c3-e166-4bc0-a3ab-69ed0e3e9616";
        // System.out.println("Attempt to Decode uuid = " + myIdentifier);
        //try {
        //    BigInteger bigInt = minter.decode(myIdentifier);
        //    System.out.println("  Decoded Value = " + bigInt);
        //} catch (Exception e) {
        //    e.printStackTrace();
        //}

        bcidMinter minter = null;
        // Create EZID w/ uuid on end
        try {
            minter = new bcidMinter(naan, minterDataset.shoulder, false);
            String uuid = "09d5a2c3-e166-4bc0-a3ab-69ed0e3e9616";
            System.out.println("Creating an ARK w/ uuid: " + uuid);
            String uuidArk = minter.createUUIDARK(uuid);
            System.out.println("  " + uuidArk);
        } catch (Exception e) {
            e.printStackTrace();
        }

        minter.close();
        minterDataset.close();
    }
}
