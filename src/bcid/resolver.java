package bcid;

import bcid.Renderer.JSONRenderer;
import bcid.Renderer.Renderer;
import edu.ucsb.nceas.ezid.EZIDException;
import edu.ucsb.nceas.ezid.EZIDService;
import util.SettingsManager;
import util.timer;

import java.math.BigInteger;
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
        } catch (Exception e) {
            System.out.println("The ark = " + ark);
            throw new Exception("Invalid ARK");
        }
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
        if (blade.startsWith("_")) {
            blade = blade.substring(1);
        }
    }

    /**
     * Attempt to resolve a particular ARK
     *
     * @return JSON String with content for the interface
     */
    public String resolveARK(Renderer renderer) {
        GenericIdentifier bcid = null;

        // First  option is check if dataset, then look at other options after this is determined
        if (isDataGroup()) {
            bcid = new bcid(datagroup_id);
            // Check if this is an element that we can resolve
            if (isElement(datagroup_id)) {
                bcid = new bcid(element_id, ark);
                // If not an element then check to see if this has a resolvable suffix
            } else if (isResolvableSuffix(datagroup_id)) {
                bcid = new bcid(element_id, ark);
            }
        }

        return renderer.renderIdentifier(bcid);
    }


    private boolean isLocalID() {

        // Now we need to figure out if this datasets_id exists or not in the database
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
        return renderer.renderIdentifier(ezid);
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

        sb.append("  " +this.resolveARK(renderer));
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
     * Tell us if this ARK is an EZID with suffix passthrough.  Note that BCIDs can resolve suffixes
     * where EZID cannot since it actively registers individual instances.
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
     * Tell us if this ARK is a bcid
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
            r = new resolver("ark:/87286/C2_393939");
            EZIDService service = new EZIDService();
            service.login(sm.retrieveValue("eziduser"), sm.retrieveValue("ezidpass"));
            System.out.println(r.resolveAllAsJSON(service));
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
