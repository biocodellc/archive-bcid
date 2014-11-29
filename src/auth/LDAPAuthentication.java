package auth;

import com.unboundid.ldap.sdk.*;
import com.unboundid.util.ssl.SSLUtil;
import com.unboundid.util.ssl.TrustAllTrustManager;
import org.apache.commons.cli.*;
import util.SettingsManager;

import javax.net.ssl.SSLSocketFactory;
import javax.servlet.ServletContext;
import javax.ws.rs.core.Context;
import java.util.ArrayList;


/**
 * Authenticate names using LDAP
 */
public class LDAPAuthentication {

    public static int SUCCESS = 0;
    public static int ERROR = 1;
    public static int INVALID_CREDENTIALS = 2;

    private BindResult bindResult = null;
    private String message = null;
    private int status;

    static SettingsManager sm;
    @Context
    static ServletContext context;
    static String ldapURI;
    static String defaultLdapDomain;

    private String shortUsername;
    private String longUsername;

    /**
     * Load settings manager
     */
    static {
        // Initialize settings manager
        sm = SettingsManager.getInstance();
        try {
            sm.loadProperties();
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Get the LDAP servers from property file
        // Property file format looks like "ldapServers = mysecureLDAPserver.net:636,myfailoverLDAPServer.net:636"
        ldapURI = sm.retrieveValue("ldapServers");
        defaultLdapDomain = sm.retrieveValue("defaultLdapDomain");
    }

    public static String showShortUserName(String username) {
            return  username.split("@")[0];
    }
    public static String showLongUsername(String username) {
            defaultLdapDomain = sm.retrieveValue("defaultLdapDomain");
          return username.split("@")[0] + "@" + defaultLdapDomain;
    }
    /**
     * Authenticate a user and password using LDAP
     *
     * @param username
     * @param password
     *
     * @return
     *
     * @throws Exception
     */
    public LDAPAuthentication(String username, String password, Boolean recognizeDemo) {

        // strip any domain extension that the user provided (we DON't want to store this)
         shortUsername = showShortUserName(username);
        longUsername = showLongUsername(username);

        if (recognizeDemo && shortUsername.equalsIgnoreCase("demo")) {
            status = SUCCESS;
            return;
        }
        // Create the connection at beginning... must be closed at finally
        LDAPConnection connection = null;

        // Set default status in case it is not specifically set.  This should be an error
        status = ERROR;

        // Creating an array of available servers
        String[] ldapServers = ldapURI.split(",");
        String[] serverAddresses = new String[ldapServers.length];
        int[] serverPorts = new int[ldapServers.length];
        for (int i = 0; i < ldapServers.length; i++) {
            serverAddresses[i] = ldapServers[i].split(":")[0];
            serverPorts[i] = (Integer.valueOf(ldapServers[i].split(":")[1]));
        }
        try {
            SSLUtil sslUtil = new SSLUtil(new TrustAllTrustManager());
            SSLSocketFactory socketFactory = sslUtil.createSSLSocketFactory();

            // Failoverset lets us query multiple servers looking for connection
            FailoverServerSet failoverSet = new FailoverServerSet(serverAddresses, serverPorts, socketFactory);
            // TODO: time this out quicker... Takes a LONG time if answer is no
            System.out.println("initiating connection for "+longUsername);
            connection = failoverSet.getConnection();
            BindRequest bindRequest = new SimpleBindRequest(longUsername, password);

            try {
                bindResult = connection.bind(bindRequest);
            } catch (LDAPException e2) {
                // don't throw any exception if we fail here, this is just a non-passed attempt.
                e2.printStackTrace();
                connection.close();
                status = INVALID_CREDENTIALS;
            }
        } catch (Exception e) {
            status = ERROR;
            e.printStackTrace();
            message = "Problem with LDAP connection.  It is likely we cannot connect to LDAP server";
        } finally {
            try {
                if (connection != null) connection.close();
            } catch (Exception e) {
                status = ERROR;
                message = "Problem closing LDAP connection";
            }
        }
        if (bindResult != null && bindResult.getResultCode() == ResultCode.SUCCESS) {
            status = SUCCESS;
        }
    }

    /**
     * Return a status message.  See constants as part of this class
     *
     * @return
     */
    public int getStatus() {
        return status;
    }

    /**
     * Return an information message.  Should only be associated with an error condition, otherwise null
     *
     * @return
     */
    public String getMessage() {
        return message;
    }


    public static void main(String[] args) throws Exception {

        //return sbEmail.toString();

        // Some classes to help us
        CommandLineParser clp = new GnuParser();
        CommandLine cl;

        Options options = new Options();
        options.addOption("U", "username", true, "fully qualified username");
        options.addOption("P", "password", true, "the password");

        try {
            cl = clp.parse(options, args);
        } catch (UnrecognizedOptionException e) {
            System.out.println("Error: " + e.getMessage());
            return;
        } catch (ParseException e) {
            System.out.println("Error: " + e.getMessage());
            return;
        }

        if (!cl.hasOption("U") || !cl.hasOption("P")) {
            System.out.println("You must enter a username and a password");
            return;
        }

        String username = cl.getOptionValue("U");
        String password = cl.getOptionValue("P");

        LDAPAuthentication t = new LDAPAuthentication(username, password, true);

        if (t.getStatus() == t.SUCCESS) {
            System.out.println("Passed!");
        } else if (t.getStatus() == t.INVALID_CREDENTIALS) {
            System.out.println("Invalid username or password, or expired account");
        } else {
            System.out.println("LDAP Error: " + t.getMessage());
        }
    }


}
