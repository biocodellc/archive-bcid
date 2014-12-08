package bcid;

import bcidExceptions.BCIDException;
import bcidExceptions.ServerErrorException;
import ezid.EZIDException;
import ezid.EZIDService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Class to work with EZID creation from the bcid database.  requests to this class are controlled by
 * switches in the database indicating whether the intention is to create EZIDS for particular identifiers.
 */
public class manageEZID extends elementMinter {

    private static Logger logger = LoggerFactory.getLogger(manageEZID.class);

    public manageEZID() {
        super();
    }

    public HashMap<String, String> ercMap(String target, String what, String who, String when) {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("_profile", "erc");

        // _target needs to be resolved by biscicol for now
        map.put("_target", target);
        // what is always dataset
        map.put("erc.what", what);
        // who is the user who loaded this
        map.put("erc.who", who);
        // when is timestamp of data loading
        map.put("erc.when", when);
        return map;
    }

    /**
     *  Update EZID dataset metadata for this particular ID
     */
    public void updateDatasetsEZID(EZIDService ezid, int datasets_id) throws EZIDException{
        Statement stmt = null;
        ResultSet rs = null;
        try {
            stmt = conn.createStatement();

            rs = stmt.executeQuery("" +
                    "SELECT " +
                    "d.datasets_id as datasets_id," +
                    "d.prefix as prefix," +
                    "d.ts as ts," +
                    "d.resourceType as what," +
                    "concat_ws('',CONCAT_WS(' ',u.firstName, u.lastName),' <',u.email,'>') as who " +
                    "FROM datasets d,users u " +
                    "WHERE ezidMade && d.users_id=u.USER_ID " +
                    "AND d.datasets_id =" + datasets_id + " " +
                    "LIMIT 1000");

            rs.next();

            // Build the hashmap to pass to ezid
            HashMap<String, String> map = ercMap(
                    resolverTargetPrefix + rs.getString("prefix"),
                    //new ResourceTypes().get(ResourceTypes.DATASET).uri,
                    rs.getString("what"),
                    rs.getString("who"),
                    rs.getString("ts"));

            // The ID string to register with EZID
            String myIdentifier = rs.getString("prefix");

            try {
                ezid.setMetadata(myIdentifier, map);
                logger.info("  Updated Metadata for " + myIdentifier);
            } catch (EZIDException e1) {
                // After attempting to set the Metadata, if another exception is thrown then who knows,
                // probably just a permissions issue.
                throw new EZIDException("  Exception thrown in attempting to create EZID " + myIdentifier + ", likely a permission issue", e1);
            }


        } catch (SQLException e) {
            throw new ServerErrorException(e);
        }
    }

    /**
     * Go through datasets table and create any ezid fields that have yet to be created.
     * This method is meant to be called via a cronjob on the backend.
     * <p/>
     * TODO: throw a special exception on this method so we can follow up why EZIDs are not being made if that is the case
     *
     * @param ezid
     * @throws java.net.URISyntaxException
     */
    public void createDatasetsEZIDs(EZIDService ezid) throws EZIDException{
        // Grab a row where ezid is false
        Statement stmt = null;
        ResultSet rs = null;
        ArrayList<String> idSuccessList = new ArrayList();
        try {
            stmt = conn.createStatement();
            rs = stmt.executeQuery("" +
                    "SELECT d.datasets_id as datasets_id," +
                    "d.prefix as prefix," +
                    "d.ts as ts," +
                    "d.resourceType as what," +
                    "concat_ws('',CONCAT_WS(' ',u.firstName, u.lastName),' <',u.email,'>') as who " +
                    "FROM datasets d,users u " +
                    "WHERE !ezidMade && ezidRequest && d.users_id=u.USER_ID && u.username != 'demo'" +
                    "LIMIT 1000");

            // Attempt to create an EZID for this row
            while (rs.next()) {
                URI identifier = null;

                // Create the hashmap to send to ezid functions
                HashMap<String, String> map = ercMap(
                        resolverTargetPrefix + rs.getString("prefix"),
                        rs.getString("what"),
                        rs.getString("who"),
                        rs.getString("ts"));
                map.put("_profile", "erc");

                // The ID string to register with ezid
                String myIdentifier = rs.getString("prefix");

                // Register this as an EZID
                try {
                    identifier = new URI(ezid.createIdentifier(myIdentifier, map));
                    idSuccessList.add(rs.getString("datasets_id"));
                    logger.info("{}", identifier.toString());
                } catch (EZIDException e) {
                    // Attempt to set Metadata if this is an Exception
                    try {
                        ezid.setMetadata(myIdentifier,map);
                        idSuccessList.add(rs.getString("datasets_id"));
                    } catch (EZIDException e1) {
                        //TODO should we silence this exception?
                        logger.warn("Exception thrown in attempting to create OR update EZID {}, a permission issue?", myIdentifier, e1);
                    }

                } catch (URISyntaxException e) {
                    throw new EZIDException("Bad uri syntax for " + myIdentifier + ", " + map, e);
                }

            }
        } catch (SQLException e) {
            throw new ServerErrorException("Server Error", "SQLException when creating EZID", e);
        }

        // Update the Identifiers Table and let it know that we've created the EZID
        try {
            updateEZIDMadeField(idSuccessList, "datasets");
        } catch (SQLException e) {
            throw new ServerErrorException("Server Error", "It appears we have created " + idSuccessList.size() +
                    " EZIDs but not able to update the identifiers table", e);
        }

    }

    /**
     * Go through identifier table and create any ezid fields that have yet to be created.
     * This method is meant to be called via a cronjob on the backend.
     * <p/>
     * In cases where suffixPassthrough = false then use the "id" field of the table itself to generate the identifier
     * In cases where suffixPassthrough = true then just pass the uuid that is stored to generate the identifier
     * TODO: throw a special exception on this method so we can follow up why EZIDs are not being made if that is the case
     *
     * @param ezid
     * @throws java.net.URISyntaxException
     */
    public void createIdentifiersEZIDs(EZIDService ezid) throws URISyntaxException {
        // Grab a row where ezid is false
        Statement stmt = null;
        ResultSet rs = null;
        ArrayList<String> idSuccessList = new ArrayList();
        try {
            stmt = conn.createStatement();
            rs = stmt.executeQuery("" +
                    "SELECT identifiers_id,webaddress,localid,what,suffixPassthrough " +
                    "FROM identifiers " +
                    "WHERE !ezidMade && ezidRequest " +
                    "LIMIT 1000");
            // Attempt to create an EZID for this row
            while (rs.next()) {
                URI identifier = null;
                HashMap<String, String> map = new HashMap<String, String>();
                map.put("_profile", "erc");

                if (rs.getString("webaddress") == null) {
                    map.put("_target", "");
                } else {
                    map.put("_target", rs.getString("webaddress"));
                }
                map.put("erc.what", rs.getString("what"));
                //TODO: put the correct who here
                //map.put("erc.who", who.toString());
                // when here is very confusing
                //map.put("erc.when", new dates().now());
                String idString = rs.getString("id");
                idSuccessList.add(idString);


                String myIdentifier = "";
                // If this is the uuid case
                if (rs.getBoolean("suffixPassthrough")) {
                    try {
                        myIdentifier = this.createUUIDARK(rs.getString("localID"));
                    } catch (BCIDException e) {
                        // TODO: special exception to handle for unable to create this identifier
                        //TODO should we silence this exception?
                        logger.warn("BCIDException thrown.", e);
                    }
                    // If this is not tagged as a uuid
                } else {
                    myIdentifier = new elementEncoder(prefix).encode(new BigInteger(idString));
                }
                System.out.println("here is the ID being created ... " + myIdentifier);
                identifier = new URI(ezid.createIdentifier(myIdentifier, map));

                // This is just for printing out stuff, probably not necessary
                if (identifier != null) {
                    idSuccessList.add(idString);
                    System.out.println("  " + identifier.toString());
                } else {
                    // Send email, or notify somehow in logs that this threw an error
                    System.out.println("Something happened in creating the EZID identifier, it appears to be null");
                }

            }
        } catch (SQLException e) {
            throw new ServerErrorException(e);
        } catch (EZIDException e) {
            throw new URISyntaxException("trouble minting identifier with EZID service", null);
        } finally {
            try {
                updateEZIDMadeField(idSuccessList, "identifiers");
            } catch (SQLException e) {
                throw new ServerErrorException("Server Error", "It appears we have created " + idSuccessList.size() +
                        " EZIDs but not able to update the identifiers table");
            }
        }
    }

    /**
     * Go through database, search for requests to make EZIDs and update the ezidMade (boolean) to true
     * This function works for both Datasets and Identifiers table
     *
     * @param idSuccessList
     * @throws java.sql.SQLException
     */
    private void updateEZIDMadeField(ArrayList idSuccessList, String table) throws SQLException {

        // Turn off autocommits at beginning of the next block
        conn.setAutoCommit(false);
        PreparedStatement updateStatement = null;

        // Loop and update
        try {
            String updateString = "" +
                    "UPDATE " + table + " " +
                    "SET ezidMade=true " +
                    "WHERE " + table + "_id=? && !ezidMade";
            updateStatement = conn.prepareStatement(updateString);
            Iterator ids = idSuccessList.iterator();
            int count = 0;
            while (ids.hasNext()) {
                String id = (String) ids.next();
                updateStatement.setString(1, id);
                updateStatement.addBatch();
                // Execute every 1000 rows
                if (count + 1 % 1000 == 0) {
                    updateStatement.executeBatch();
                    conn.commit();
                }
                count++;
            }
            updateStatement.executeBatch();
            conn.commit();

        } finally {
            conn.setAutoCommit(true);
        }
    }
}
