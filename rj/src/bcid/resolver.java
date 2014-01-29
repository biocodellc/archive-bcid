package bcid;

import bcid.Renderer.HTMLTableRenderer;
import bcid.Renderer.JSONRenderer;
import bcid.Renderer.RDFRenderer;
import bcid.Renderer.Renderer;
import edu.ucsb.nceas.ezid.EZIDException;
import edu.ucsb.nceas.ezid.EZIDService;
import util.SettingsManager;
import util.timer;

import javax.swing.text.html.HTMLDocument;
import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Resolves any incoming identifier to the BCID and/or EZID systems.
 * Resolver first checks if this is a data group.  If so, it then checks if there is a decodable BCID.  If not,
 * then check if there is a suffix and if THAT is resolvable.
 */
public class resolver extends database {
    String ark = null;
    String scheme = "ark:";
    String naan = null;
    String shoulder = null;
    String blade = null;
    BigInteger element_id = null;
    Integer datagroup_id = null;
    static SettingsManager sm;

    /**
     * Load settings manager, set ontModelSpec.
     */
    static {
        // Initialize settings manager
        sm = SettingsManager.getInstance();
        try {
            sm.loadProperties();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * Pass an ARK identifier to the resolver
     *
     * @param ark
     * @throws Exception
     */
    public resolver(String ark) throws Exception {
        super();
        try {
            this.ark = ark;
            // Pull off potential last piece of string which would represent the local Identifier
            // The piece to decode is ark:/NAAN/bcidIdentifer (anything else after a last trailing "/" not decoded)
            StringBuilder stringBuilder = new StringBuilder();
            String bits[] = ark.split("/", 3);
            // just want the first chunk between the "/"'s
            naan = bits[1];
            // Now decipher the shoulder and blade in the next bit
            setShoulderAndBlade(bits[2]);
            // Call isDataGroup() to set datagroup_id
            isDataGroup();
        } catch (Exception e) {
            System.out.println("The ark = " + ark);
            throw new Exception("Invalid ARK", e);
        }
    }

    /**
     * Find the appropriate BCID for this project given an conceptAlias.  This instantiation of the resolver
     * looks up a project specific BCID given the project code and an concept Alias
     *
     * @param project_code defines the BCID project_code to lookup
     * @param conceptAlias defines the alias to narrow this,  a one-word reference denoting a BCID
     * @return returns the BCID for this project and conceptURI combination
     */
    public resolver(String project_code, String conceptAlias) throws Exception {
        ResourceTypes resourceTypes = new ResourceTypes();
        ResourceType rt = resourceTypes.getByShortName(conceptAlias);
        String uri = rt.uri;
        try {
            Statement stmt = conn.createStatement();

            String query = "select \n" +
                    "d.prefix as prefix \n" +
                    "from \n" +
                    "datasets d, projectsBCIDs pb, projects p \n" +
                    "where \n" +
                    "d.datasets_id=pb.datasets_id && \n" +
                    "pb.project_id=p.project_id && \n" +
                    "p.project_code='" + project_code + "' && \n" +
                    "d.resourceType='" + uri + "'";
            ResultSet rs = stmt.executeQuery(query);
            rs.next();
            this.ark = rs.getString("prefix");
        } catch (SQLException e) {
            this.ark = null;
        } catch (Exception e) {
            this.ark = null;
        }
    }


    public String getArk() {
        return ark;
    }

    /**
     * Return an identifier representing a data set
     *
     * @return
     */
    public Integer getDataGroupID() {
        return datagroup_id;
    }

    /**
     * Return an identifier representing a data element
     *
     * @return
     */
    public BigInteger getElementID() {
        return element_id;
    }

    /**
     * Set the shoulder and blade variables for this ARK
     *
     * @param a
     */
    private void setShoulderAndBlade(String a) {
        boolean reachedShoulder = false;
        StringBuilder sbShoulder = new StringBuilder();
        StringBuilder sbBlade = new StringBuilder();

        for (int i = 0; i < a.length(); i++) {
            char c = a.charAt(i);
            if (!reachedShoulder)
                sbShoulder.append(c);
            else
                sbBlade.append(c);
            if (Character.isDigit(c))
                reachedShoulder = true;
        }
        shoulder = sbShoulder.toString();
        blade = sbBlade.toString();

        // String the slash between the shoulder and the blade
        if (!sm.retrieveValue("divider").equals("")) {
            if (blade.startsWith(sm.retrieveValue("divider"))) {
                blade = blade.substring(1);
            }
        }
    }

    /**
     * Attempt to resolve a particular ARK.  If there is no webaddress defined for resolution then
     * it points to the biscicol.org/bcid homepage.
     *
     * @return JSON String with content for the interface
     */
    public URI resolveARK() throws URISyntaxException {
        bcid bcid = null;
        URI resolution = null;

        // First  option is check if dataset, then look at other options after this is determined
        if (isDataGroup()) {
            bcid = new bcid(datagroup_id);

            // Set resolution target to that specified by the datagroup ID webAddress, only if it exists
            // and only if it does not specify suffixPassThrough.  If it specifies suffix passthrough
            // the assumption here is we only want to resolve suffixes but not the dataset itself.
            // TODO: update documentation with this behaviour!

            /**
             * GROUP RESOLUTION
             */
            // Group has a specified resolution target
            if (bcid.getResolutionTarget() != null && !bcid.getResolutionTarget().toString().trim().equals("")) {
                // Group specifies suffix passthrough
                if (bcid.getDatasetsSuffixPassthrough()) {
                    resolution = bcid.getMetadataTarget();
                    // Group does not specify suffix passthrough
                } else {
                    resolution = bcid.getResolutionTarget();

                }
                // Determine if this has some suffix on the datagroup and if so, then provide re-direct to
                // location specified in the system
                if (blade != null && bcid.getResolutionTarget() != null && !blade.trim().equals("") && !bcid.getResolutionTarget().equals("")) {
                    resolution = new URI(bcid.getResolutionTarget() + blade);
                }
            }
            // This is a group and no resolution target is specified then just return metadata.
            else {
                resolution = bcid.getMetadataTarget();
            }

        }
        return resolution;
    }

    /**
     * Print Metadata for a particular ARK
     *
     * @return JSON String with content for the interface
     */
    public String printMetadata(Renderer renderer) {
        GenericIdentifier bcid = null;

        // First  option is check if dataset, then look at other options after this is determined
        if (isDataGroup()) {

            bcid = new bcid(datagroup_id);
            // Has a registered, resolvable suffix
            //if (isResolvableSuffix(datagroup_id)) {
            //    bcid = new bcid(element_id, ark);
            //}
            // Has a suffix, but not resolvable
            //else {
            try {
                if (blade != null && bcid.getResolutionTarget() != null) {
                    bcid = new bcid(blade, bcid.getResolutionTarget(), datagroup_id);
                }
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
            //}

        }
        return renderer.render(bcid);
    }

    /**
     * Determine if this ARK has a matching localID
     *
     * @return
     */
    private boolean isLocalID() {
        String select = "SELECT identifiers_id FROM identifiers " +
                "where i.localid = " + ark;
        Statement stmt = null;
        try {
            stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(select);
            rs.next();
            // TODO: enable returning multiple possible identifiers here
            element_id = new BigInteger(rs.getString("identifiers_id"));
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    /**
     * Resolve an EZID version of this ARK
     *
     * @param ezidService
     * @return JSON string to send to interface
     */
    public String resolveEZID(EZIDService ezidService, Renderer renderer) {
        // First fetch from EZID, and populate a map
        GenericIdentifier ezid = null;

        try {
            ezid = new ezid(ezidService.getMetadata(ark));
        } catch (EZIDException e) {
            e.printStackTrace();
        }
        return renderer.render(ezid);
    }


    /**
     * Resolve identifiers through BCID AND EZID -- This method assumes JSONRenderer
     *
     * @param ezidService
     * @return JSON string with information about BCID/EZID results
     */
    public String resolveAllAsJSON(EZIDService ezidService) {
        timer t = new timer();
        Renderer renderer = new JSONRenderer();
        StringBuilder sb = new StringBuilder();
        sb.append("[\n");

        try {
            sb.append("  " + this.resolveARK().toString());
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        t.lap("resolveARK");
        sb.append("\n  ,\n");
        sb.append("  " + this.resolveEZID(ezidService, renderer));
        t.lap("resolveEZID");
        sb.append("\n]");
        return sb.toString();
    }

    /**
     * Check if this is a dataset and set the datasets_id
     *
     * @return
     */
    private boolean isDataGroup() {
        // Test Dataset is #1
        if (shoulder.equals("fk4") && naan.equals("99999")) {
            datagroup_id = 1;
            return true;
        }

        // Decode a typical dataset
        datagroup_id = new dataGroupEncoder().decode(shoulder).intValue();

        if (datagroup_id == null) {
            return false;
        } else {
            // Now we need to figure out if this datasets_id exists or not in the database
            String select = "SELECT count(*) as count FROM datasets where datasets_id = " + datagroup_id;
            Statement stmt = null;
            try {
                stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(select);
                rs.next();
                int count = rs.getInt("count");
                if (count < 1) {
                    datagroup_id = null;
                    return false;
                } else {
                    return true;
                }
            } catch (SQLException e) {
                return false;
            }
        }
    }

    /**
     * Tell us if this ARK is a BCID that has an individually resolvable suffix.  This means that the user has
     * registered the identifier and provided a specific target URL
     *
     * @return
     */
    private boolean isResolvableSuffix(Integer d) {
        // Only attempt this method if the blade has some content, else we know there is no suffix
        if (blade != null && !blade.equals("")) {
            // Establish database connection so we can lookup suffixes here
            try {
                String select = "SELECT identifiers_id FROM identifiers where datasets_id = " + d + " && localid = '" + blade + "'";
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(select);
                rs.next();
                element_id = new BigInteger(rs.getString("identifiers_id"));
            } catch (Exception e) {
                return false;
            }
        }
        if (element_id == null) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * Tell us if this ARK is a BCID by decoding the ARK itself and determining if we can
     * assign an integer to it.  This then is a native BCID that uses character encoding.
     *
     * @return
     */
    private boolean isElement(int datasets_id) {

        String bow = scheme + "/" + naan + "/";
        String prefix = bow + shoulder;
        // if prefix and ark the same then just return false!
        if (prefix.equals(ark)) {
            return false;
        }
        BigInteger bigInt = null;

        // Look at Check Digit, a BCID should validate here... if the check-digit doesn't work its not a BCID
        // We do the check-digit function first since this is faster than looking it up in the database and
        // if it is bad, we will know right away.
        try {
            bigInt = new elementEncoder(prefix).decode(ark);
        } catch (Exception e) {
            return false;
        }

        // Now, see if this exists in the database
        try {
            element_id = bigInt;
            // First test is to see if this is a valid number
            if (bigInt.signum() == 1) {
                // Now test to see if this actually exists in the database.
                try {
                    String select = "SELECT count(*) as count FROM identifiers where identifiers_id = " + bigInt +
                            " && datasets_id = " + datasets_id;
                    Statement stmt = conn.createStatement();
                    ResultSet rs = stmt.executeQuery(select);
                    rs.next();
                    if (rs.getInt("count") > 0)
                        return true;
                    else
                        return false;
                } catch (Exception e) {
                    return false;
                }

            } else {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    /**
     * Main function for testing.
     *
     * @param args
     */
    public static void main(String args[]) {
        resolver r = null;
        SettingsManager sm = SettingsManager.getInstance();
        try {
            sm.loadProperties();
        } catch (Exception e) {
            e.printStackTrace();
        }

        /* try {
            r = new resolver("ark:/87286/C2/AOkI");
            System.out.println("  " + r.resolveARK());

        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            r = new resolver("ark:/87286/C2/64c82d19-6562-4174-a5ea-e342eae353e8");
            System.out.println("  " + r.resolveARK());
        } catch (Exception e) {
            e.printStackTrace();
        }
         */

        try {
            //r = new resolver("ark:/21547/P2_JDeck1");
            //r = new resolver("ark:/21547/R2");


            // EZIDService service = new EZIDService();
            // service.login(sm.retrieveValue("eziduser"), sm.retrieveValue("ezidpass"));
            //System.out.println(r.);
            //Renderer ren = new RDFRenderer();
            //System.out.println(r.printMetadata(ren));

            r = new resolver("DEMOH", "Sequencing");
            System.out.println(r.getArk());
            //System.out.println(r.resolveARK().toString());
        } catch (Exception e) {
            e.printStackTrace();
        }


        /* String result = null;
        try {
            result = URLDecoder.decode("ark%3A%2F87286%2FC2", "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        try {
            r = new resolver(result);
            r.resolveARK();
            System.out.println(r.ark + " : " + r.datasets_id);
            //    EZIDService service = new EZIDService();
            //    System.out.println("  " + r.resolveAll(service));
        } catch (Exception e) {
            e.printStackTrace();
        }
        */

    }


}
