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
 * The bcid class encapsulates all of the information we know about a particular identifier, including the
 * status of EZID creation, associated dataset calls, and any metadata.
 * There are several ways to construct this class.  Some of the constructors create
 * a bcid representation out of the box and are meant to then use to pass onto the database
 * so it can be created.  Other constructors create a bcid by looking up in the database and
 * populating the bcid class variables that way.
 *
 */
public class bcid {


    protected URI webAddress = null;        // URI for the webAddress, EZID calls this _target (e.g. http://biocode.berkeley.edu/specimens/MBIO56)
    protected String sourceID = null;       // Source or local identifier (e.g. MBIO056)
    protected ResourceType resourceType;    // The ResourceType, using the BCID system definitions
    protected String what = null;           // erc.what
    protected String when = null;           // erc.when
    protected String who = null;            // erc.who
    protected String title = null;            // erc.who\
    protected boolean datasetsEzidMade;
    protected boolean datasetsEzidRequest;
    protected String datasetsPrefix;
    protected String datasetsTs;
    protected boolean identifiersEzidRequest;
    protected boolean identifiersEzidMade;
    protected boolean identifiersSuffixPassthrough;
    protected String identifiersTs;
    protected String ark;

    // HEADER to use with row() method
    protected static final String HEADER = "URI\tresourceTypeIdentifier\tsourceID\twebAddress";

    /**
     * Create a bcid given a source identifier, and a resource type identifier
     * @param sourceID
     * @param resourceTypeIdentifier
     */
    public bcid(String sourceID, int resourceTypeIdentifier) {
        this(sourceID, null, resourceTypeIdentifier);
    }

    /**
     * Create a bcid given a source identifier, web address for resolution, and a resource type identifier
     * @param sourceID
     * @param webAddress
     * @param resourceTypeIdentifier
     */
    public bcid(String sourceID, URI webAddress, int resourceTypeIdentifier) {
        when = new dates().now();
        this.webAddress = webAddress;
        this.sourceID = sourceID;
        ResourceTypes types = new ResourceTypes();
        resourceType = types.get(resourceTypeIdentifier);
        what = resourceType.uri;
        what = this.resourceType.string;
    }

    /**
     * Create a bcid given a source identifier and a web address for resolution.  The resource type
     * identifier becomes simply "Resource"
     * @param sourceID
     * @param webAddress
     */
    public bcid(String sourceID, URI webAddress) {
        this(sourceID, webAddress, ResourceTypes.RESOURCE);
    }

    /**
     * Create a bcid given only a source identifier.  No web address redirection and the resource type will be
     * simply "Resource"
      * @param sourceID
     */
    public bcid(String sourceID) {
        this(sourceID, null, ResourceTypes.RESOURCE);
    }

    /**
     * Create a bcid by passing in an BigInteger for the specific slot in the database and a string representation of this
     * ARK.
     * This class should probably only be instantiated by the resolver class, after it figures out what BigInteger
     * a particular ARK belongs to.
     *
     * @param identifiers_id indicating the integer of this identifier in the BCID system
     * @param ark is the Full identifier
     */
    public bcid(BigInteger identifiers_id, String ark) {
        try {
            database db = new database();
            Statement stmt = db.conn.createStatement();
            String datasets = "SELECT " +
                    "   d.ezidMade," +
                    "   d.ezidRequest," +
                    "   d.prefix,d.ts," +
                    "   i.ezidMade," +
                    "   i.ezidRequest," +
                    "   i.suffixPassthrough," +
                    "   i.localid," +
                    "   i.webaddress," +
                    "   i.what," +
                    "   i.ts," +
                    //"   concat_ws('',u.fullname,' &lt;',u.email,'&gt;') as username " +
                    "   u.username " +
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
            identifiersSuffixPassthrough = rs.getBoolean(count++);
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
     * Create a bcid by passing in a single integer.  In this case, we assume this is simply a dataset.
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
                    //"   concat_ws('',u.fullname,' &lt;',u.email,'&gt;') as username " +
                    "   u.username " +
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
            who = rs.getString(count++);
            ark = datasetsPrefix;
            what = new ResourceTypes().get(ResourceTypes.DATASET).uri;
            when = datasetsTs;

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
     * Return JSON representation of this bcid
     *
     * @return JSON representation of this BCID
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

            appender()  {
                sb = new StringBuilder();
                sb.append("[{");
            }

            void append(String key, String value) {
                if (value != null) {
                    if (sb.length() > 2)
                        sb.append(",");
                    sb.append("\"" + key + "\":\"" + value + "\"");
                }

            }

            void append(String key, boolean value) {
                if (sb.length() > 2)
                    sb.append(",");
                if (value)
                    sb.append("\"" + key + "\":\"true\"");
                else
                    sb.append("\"" + key + "\":\"false\"");
            }

            void close() {
                // Strip the last comma

                // Close this
                sb.append("}]");
            }

            public String toString() {
                return sb.toString();
            }
        }
        appender a = new appender();
        a.append("ark", ark);
        a.append("who", who);
        a.append("what", what);
        a.append("when", when);
        a.append("title", title);
        a.append("sourceID", sourceID);
        a.append("datasetsEzidMade", datasetsEzidMade);
        a.append("datasetsEzidRequest", datasetsEzidRequest);
        a.append("datasetsPrefix", datasetsPrefix);
        a.append("datasetsTs", datasetsTs);
        a.append("identifiersEzidRequest", identifiersEzidRequest);
        a.append("identifiersEzidMade", identifiersEzidMade);
        a.append("identifiersSuffixPassthrough", identifiersSuffixPassthrough);
        a.append("identifiersTs", identifiersTs);
        a.close();

        return a.toString();
    }
}

