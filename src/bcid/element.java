package bcid;

import util.SettingsManager;
import util.dates;

import java.io.FileNotFoundException;
import java.lang.String;
import java.math.BigInteger;
import java.net.URI;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * The element class encapsulates all of the information we know about a particular identifier.
 * This includes data such as the
 * status of EZID creation, associated dataset calls, and any metadata.
 * There are several ways to construct an element, including creating it from scratch, or instantiating by looking
 * up an existing identifier from the database.
 */
public class element {


    protected URI webAddress = null;        // URI for the webAddress, EZID calls this _target (e.g. http://biocode.berkeley.edu/specimens/MBIO56)
    protected String sourceID = null;       // Source or local identifier (e.g. MBIO056)
    //protected ResourceType resourceType;    // The ResourceType, using the BCID system definitions
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
    protected dataGroup dataset;
    protected String doi;
    protected String level = "data element";     // Default is element, can also be data group.

    // HEADER to use with row() method
    protected static final String HEADER = "URI\tresourceTypeIdentifier\tsourceID\twebAddress";

    /**
     * Create an element given a source identifier, and a resource type identifier
     *
     * @param sourceID
     * @param dataset_id
     */
    public element(String sourceID, Integer dataset_id) {
        this(sourceID, null, dataset_id);
    }

    /**
     * Create an element given a source identifier, web address for resolution, and a resource type identifier
     *
     * @param sourceID
     * @param webAddress
     * @param dataset_id
     */
    public element(String sourceID, URI webAddress, Integer dataset_id) {

        try {
             dataset = new dataGroup(dataset_id);
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
    public element(BigInteger identifiers_id, String ark) {
        try {
            database db = new database();
            Statement stmt = db.conn.createStatement();
            String datasets = "SELECT " +
                    "   d.ezidMade," +
                    "   d.ezidRequest," +
                    "   d.prefix,d.ts," +
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
    public element(Integer datasets_id) {
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

    /**
     * Express this identifier in triple format
     *
     * @return N3 content
     */
    public String triplify() {

        SettingsManager sm = SettingsManager.getInstance();
        try {
            sm.loadProperties();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        String type = sm.retrieveValue("type");
        String relatedTo = sm.retrieveValue("isRelatedTo");

        String result = "";
        result += "<" + ark + "> " + type + " <" + what + ">";
        if (webAddress != null)
            result += ";\n  " + relatedTo + " <" + webAddress + ">";
        if (sourceID != null)
            result += ";\n  <http://purl.org/dc/elements/1.1/identifier> \"" + sourceID + "\"";
        result += " .";
        return result;
    }

    /**
     * Express this identifier as a row.
     *
     * @return Text content, tab delimitied
     */
    public String row() {
        return ark + "\t" + what + "\t" + sourceID + "\t" + webAddress;
    }


    /**
     * Return JSON representation of this element
     *
     * @return JSON representation of this element
     */
    public String json() {
        if (this == null) {
            return "NULL!";
        }
        /**
         * Convenience class to help build JSON responses
         */
        class appender {
            StringBuilder sb;

            appender() {
                sb = new StringBuilder();
                sb.append("{");
            }

            void append(String key, String value) {
                if (value != null) {
                    if (sb.length() > 2)
                        sb.append(",");
                    sb.append("\"" + key + "\":\"" + value + "\"");
                }

            }

            void append(String key, Boolean value) {
                if (value != null) {
                    if (sb.length() > 2)
                        sb.append(",");

                    if (value) {
                        sb.append("\"" + key + "\":\"true\"");
                    } else {
                        sb.append("\"" + key + "\":\"false\"");
                    }
                }
            }

            void close() {
                // Strip the last comma

                // Close this
                sb.append("}");
            }

            public String toString() {
                return sb.toString();
            }
        }
        appender a = new appender();
        a.append("ark", ark);
        a.append("who", who);
        a.append("when", when);
        a.append("what", what);
        a.append("level", level);
        a.append("title", title);
        a.append("sourceID", sourceID);
        a.append("doi", doi);
        a.append("datasetsEzidMade", datasetsEzidMade);
        //a.append("datasetsEzidRequest", datasetsEzidRequest);
        a.append("datasetsSuffixPassThrough", datasetsSuffixPassthrough);
        a.append("datasetsPrefix", datasetsPrefix);
        a.append("datasetsTs", datasetsTs);
        //a.append("identifiersEzidRequest", identifiersEzidRequest);
        a.append("identifiersEzidMade", identifiersEzidMade);
        a.append("identifiersTs", identifiersTs);
        a.close();

        return a.toString();
    }
}

