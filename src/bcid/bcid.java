package bcid;

import util.dates;

import java.lang.String;
import java.math.BigInteger;
import java.net.URI;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;

/**
 * The bcid class encapsulates all of the information we know about a particular identifier.
 * This includes data such as the
 * status of EZID creation, associated dataset calls, and any metadata.
 * There are several ways to construct an element, including creating it from scratch, or instantiating by looking
 * up an existing identifier from the database.
 */
public class bcid extends GenericIdentifier {
    protected URI webAddress = null;        // URI for the webAddress, EZID calls this _target (e.g. http://biocode.berkeley.edu/specimens/MBIO56)
    protected String sourceID = null;       // Source or local identifier (e.g. MBIO056)
    protected String what = null;           // erc.what
    protected String when = null;           // erc.when
    protected String who = null;            // erc.who
    protected String title = null;            // erc.who\
    protected Boolean datasetsEzidMade;
    protected Boolean datasetsEzidRequest;
    protected String datasetsPrefix;
    protected String datasetsTs;
    protected Boolean identifiersEzidRequest;
    protected Boolean identifiersEzidMade;
    protected Boolean datasetsSuffixPassthrough;
    protected String identifiersTs;
    protected String ark;
    protected dataGroupMinter dataset;
    protected String doi;
    protected String level = "data element";     // Default is element, can also be data group.

    // HashMap to store metadata values
    private HashMap<String, String> map = new HashMap<String, String>();

    /**
     * Create an element given a source identifier, and a resource type identifier
     *
     * @param sourceID
     * @param dataset_id
     */
    public bcid(String sourceID, Integer dataset_id) {
        this(sourceID, null, dataset_id);
    }

    /**
     * Create an element given a source identifier, web address for resolution, and a resource type identifier
     *
     * @param sourceID
     * @param webAddress
     * @param dataset_id
     */
    public bcid(String sourceID, URI webAddress, Integer dataset_id) {

        try {
            dataset = new dataGroupMinter(dataset_id);
        } catch (Exception e) {
            e.printStackTrace();
        }
        when = new dates().now();
        this.webAddress = webAddress;
        this.sourceID = sourceID;
        what = dataset.getResourceType();
    }

    /**
     * Create an element by passing in an BigInteger for the specific slot in the database and a string representation of this
     * ARK.
     * This class should probably only be instantiated by the resolver class, after it figures out what BigInteger
     * a particular ARK belongs to.
     *
     * @param identifiers_id indicating the integer of this identifier in the BCID system
     * @param ark            is the Full identifier
     */
    public bcid(BigInteger identifiers_id, String ark) {
        try {
            database db = new database();
            Statement stmt = db.conn.createStatement();
            String datasets = "SELECT " +
                    "   d.ezidMade," +
                    "   d.ezidRequest," +
                    "   d.prefix,d.ts," +
                    "   d.title," +
                    "   i.ezidMade," +
                    "   i.ezidRequest," +
                    "   d.suffixPassthrough," +
                    "   i.localid," +
                    "   i.webaddress," +
                    "   d.resourceType," +
                    "   i.ts," +
                    //"   concat_ws('',u.fullname,' &lt;',u.email,'&gt;') as username " +
                    "   u.fullname " +
                    " FROM datasets d, identifiers i, users u " +
                    " WHERE d.datasets_id = i.datasets_id && " +
                    " d.users_id = u.user_id && " +
                    " i.identifiers_id = " + identifiers_id.toString();
            ResultSet rs = stmt.executeQuery(datasets);
            rs.next();
            int count = 1;
            datasetsEzidMade = rs.getBoolean(count++);
            datasetsEzidRequest = rs.getBoolean(count++);
            datasetsPrefix = rs.getString(count++);
            datasetsTs = rs.getString(count++);
            title = rs.getString(count++);
            identifiersEzidMade = rs.getBoolean(count++);
            identifiersEzidRequest = rs.getBoolean(count++);
            datasetsSuffixPassthrough = rs.getBoolean(count++);
            sourceID = rs.getString(count++);
            String webaddress = rs.getString(count++);
            if (webaddress != null) {
                webAddress = new URI(webaddress);
            }
            what = rs.getString(count++);
            when = rs.getString(count++);
            who = rs.getString(count++);
            this.ark = ark;

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Create an element, dataset reference
     *
     * @param datasets_id
     */
    public bcid(Integer datasets_id) {
        try {
            database db = new database();
            Statement stmt = db.conn.createStatement();
            String datasets = "SELECT d.ezidMade," +
                    "   d.ezidRequest," +
                    "   d.prefix," +
                    "   d.ts," +
                    "   d.title," +
                    "   d.resourceType," +
                    "   d.suffixPassthrough," +
                    "   d.doi," +
                    //"   concat_ws('',u.fullname,' &lt;',u.email,'&gt;') as username " +
                    "   u.fullname " +
                    " FROM datasets d, users u " +
                    " WHERE " +
                    " d.datasets_id = " + datasets_id + " && " +
                    " d.users_id = u.user_id";

            ResultSet rs = stmt.executeQuery(datasets);
            rs.next();
            int count = 1;
            datasetsEzidMade = rs.getBoolean(count++);
            datasetsEzidRequest = rs.getBoolean(count++);
            datasetsPrefix = rs.getString(count++);
            datasetsTs = rs.getString(count++);
            title = rs.getString(count++);
            what = rs.getString(count++);
            datasetsSuffixPassthrough = rs.getBoolean(count++);
            doi = rs.getString(count++);
            who = rs.getString(count++);
            ark = datasetsPrefix;
            //what = new ResourceTypes().get(ResourceTypes.DATASET).uri;
            when = datasetsTs;
            level = "data group";

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public HashMap<String, String> getMetadata() {
        put("ark", ark);
        put("who", who);
        put("when", when);
        put("what", what);
        put("level", level);
        put("title", title);
        put("sourceID", sourceID);
        put("doi", doi);
        put("datasetsEzidMade", datasetsEzidMade);
        put("datasetsSuffixPassThrough", datasetsSuffixPassthrough);
        put("datasetsPrefix", datasetsPrefix);
        put("datasetsTs", datasetsTs);
        put("identifiersEzidMade", identifiersEzidMade);
        put("identifiersTs", identifiersTs);
        return map;
    }

    private void put(String key, String val) {
        if (val != null)
            map.put(key, val);
    }
    private void put(String key, Boolean val) {
        if (val != null)
            map.put(key, val.toString());
    }
}

