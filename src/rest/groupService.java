package rest;

import auth.oauth2.provider;
import bcid.Renderer.JSONRenderer;
import bcid.Renderer.Renderer;
import bcid.Renderer.TextRenderer;
import bcid.dataGroupMinter;
import bcid.expeditionMinter;
import bcid.database;
import bcid.manageEZID;
import bcid.GenericIdentifier;
import bcid.resolver;
import bcid.bcid;
import bcid.ResourceTypes;

import ezid.EZIDException;
import ezid.EZIDService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.SettingsManager;
import util.errorInfo;
import util.sendEmail;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Hashtable;

/**
 * REST interface calls for working with data groups.    This includes creating a group, looking up
 * groups by user associated with them, and JSON representation of group metadata.
 */
@Path("groupService")
public class groupService {

    final static Logger logger = LoggerFactory.getLogger(groupService.class);

    @Context
    static ServletContext context;
    static String bcidShoulder;
    static String doiShoulder;
    //static SettingsManager sm;
    static EZIDService ezidAccount;

    /**
     * Load settings manager, set ontModelSpec.
     */

    /**
     * Create a data group
     *
     * @param doi
     * @param webaddress
     * @param title
     * @param request
     *
     * @return
     */
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public Response mint(@FormParam("doi") String doi,
                         @FormParam("webaddress") String webaddress,
                         @FormParam("graph") String graph,
                         @FormParam("title") String title,
                         @FormParam("resourceType") String resourceTypeString,
                         @FormParam("resourceTypesMinusDataset") Integer resourceTypesMinusDataset,
                         @FormParam("suffixPassThrough") String stringSuffixPassThrough,
                         @QueryParam("access_token") String accessToken,
                         @Context HttpServletRequest request) {

        // If resourceType is specified by an integer, then use that to set the String resourceType.
        // If the user omits
        try {
            if (resourceTypesMinusDataset != null && resourceTypesMinusDataset > 0) {
                resourceTypeString = new ResourceTypes().get(resourceTypesMinusDataset).uri;
            }
        } catch (IndexOutOfBoundsException e) {
            return Response.status(400).entity(new errorInfo("BCID System Unable to set resource type",
                    "There was an error retrieving the resource type uri. Did you provide a valid resource type?",
                    400,
                    e
            ).toJSON()).build();
        }

        String username;

        // if accessToken != null, then OAuth client is accessing on behalf of a user
        if (accessToken != null) {
            provider p = new provider();
            username = p.validateToken(accessToken);
        } else {
            HttpSession session = request.getSession();
            username = (String) session.getAttribute("user");
        }

        if (username == null) {
            // status=401 means unauthorized user
            return Response.status(401).entity(new errorInfo("You must be logged in to create a data group.",
                    401
            ).toJSON()).build();
        }

        Boolean suffixPassthrough;
        // Format Input variables
        if (!stringSuffixPassThrough.isEmpty() && (stringSuffixPassThrough.equalsIgnoreCase("true") ||
                stringSuffixPassThrough.equalsIgnoreCase("on"))) {
            suffixPassthrough = true;
        } else {
            suffixPassthrough = false;
        }

        // Initialize settings manager
        SettingsManager sm = SettingsManager.getInstance();
        sm.loadProperties();

        // Create a Dataset
        database db = new database();
        // Check for remote-user
        Integer user_id = db.getUserId(username);

        // Detect if this is user=demo or not.  If this is "demo" then do not request EZIDs.
        // User account Demo can still create Data Groups, but they just don't get registered and will be purged periodically
        boolean ezidRequest = true;
        if (username.equals("demo")) {
            ezidRequest = false;
        }
        if (sm.retrieveValue("ezidRequests").equalsIgnoreCase("false")) {
            ezidRequest = false;
        }

        // Mint the data group
        dataGroupMinter minterDataset = new dataGroupMinter(ezidRequest, suffixPassthrough);
        minterDataset.mint(
                new Integer(sm.retrieveValue("bcidNAAN")),
                user_id,
                resourceTypeString,
                doi,
                webaddress,
                graph,
                title);
        minterDataset.close();
        String datasetPrefix = minterDataset.getPrefix();

        // Create EZIDs right away for Dataset level Identifiers
        // Initialize ezid account
        // NOTE: On any type of EZID error, we DON'T want to fail the process.. This means we need
        // a separate mechanism on the server side to check creation of EZIDs.  This is easy enough to do
        // in the database.
        if (ezidRequest) {
            try {
                ezidAccount = new EZIDService();
                // Setup EZID account/login information
                ezidAccount.login(sm.retrieveValue("eziduser"), sm.retrieveValue("ezidpass"));
                manageEZID creator = new manageEZID();
                creator.createDatasetsEZIDs(ezidAccount);
            } catch (EZIDException e) {
                logger.warn("EZID NOT CREATED FOR DATASET = " + minterDataset.getPrefix(), e);
            }
        }

        return Response.ok("{\"prefix\": \"" + datasetPrefix + "\"}").build();
    }

    /**
     * Return a JSON representation of dataset metadata
     *
     * @param dataset_id
     *
     * @return
     */
    @GET
    @Path("/metadata/{dataset_id}")
    @Produces(MediaType.APPLICATION_JSON)
    public String run(@PathParam("dataset_id") Integer dataset_id) {
        GenericIdentifier bcid = new bcid(dataset_id);
        Renderer renderer = new JSONRenderer();

        return "[" + renderer.render(bcid) + "]";
    }

    /**
     * Return JSON response showing data groups available to this user
     *
     * @return String with JSON response
     */
    @GET
    @Path("/list")
    @Produces(MediaType.APPLICATION_JSON)
    public Response datasetList(@Context HttpServletRequest request) {
//      TODO send a 401 if the user isn't logged in
        HttpSession session = request.getSession();
        String username = session.getAttribute("user").toString();

        dataGroupMinter d = null;
        try {
            d = new dataGroupMinter();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(500).entity(new errorInfo("test", 500).toJSON()).build();
        }

        return Response.ok(d.datasetList(username)).build();
    }

    /**
     * Return HTML response showing a table of groups belonging to this user
     *
     * @return String with HTML response
     */
    @GET
    @Path("/listUserBCIDsAsTable")
    @Produces(MediaType.TEXT_HTML)
    public String listUserBCIDsAsTable(@Context HttpServletRequest request) {
        HttpSession session = request.getSession();
        String username = session.getAttribute("user").toString();

        dataGroupMinter d = null;
        try {
            d = new dataGroupMinter();
        } catch (Exception e) {
            e.printStackTrace();
            return new errorInfo("test", 500).toHTMLTable();
        }

        return d.datasetTable(username);
    }

    /**
     * Return HTML response showing a table of groups belonging to this user
     *
     * @return String with HTML response
     */
    @GET
    @Path("/listUserExpeditionsAsTable")
    @Produces(MediaType.TEXT_HTML)
    public String listUserExpeditionsAsTable(@Context HttpServletRequest request) {
        HttpSession session = request.getSession();
        String username = session.getAttribute("user").toString();

        expeditionMinter p = null;
        try {
            p = new expeditionMinter();
            String tablename = p.expeditionTable(username);
            return tablename;
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            return new errorInfo("test", 500).toHTMLTable();
        }
    }

    /**
     * returns an HTML table used to edit a bcid's configuration.
     *
     * @param prefix
     *
     * @return
     */
    @GET
    @Path("/dataGroupEditorAsTable")
    @Produces(MediaType.TEXT_HTML)
    public String dataGroupEditorAsTable(@QueryParam("ark") String prefix,
                                         @Context HttpServletRequest request) {
        HttpSession session = request.getSession();
        Object username = session.getAttribute("user");

        if (username == null) {
            return "You must be logged in to edit a BCID.";
        }

        if (prefix == null) {
            return "You must provide an \"ark\" query parameter.";
        }

        try {
            dataGroupMinter d = new dataGroupMinter();
            return d.bcidEditorAsTable(username.toString(), prefix);
        } catch (Exception e) {
            e.printStackTrace();
            return new errorInfo("test", 500).toHTMLTable();
        }
    }

    /**
     * Service to update a bcid's configuration.
     *
     * @param doi
     * @param webaddress
     * @param title
     * @param resourceTypeString
     * @param resourceTypesMinusDataset
     * @param stringSuffixPassThrough
     * @param prefix
     *
     * @return
     */
    @POST
    @Path("/dataGroup/update")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public Response dataGroupUpdate(@FormParam("doi") String doi,
                                    @FormParam("webaddress") String webaddress,
                                    @FormParam("title") String title,
                                    @FormParam("resourceType") String resourceTypeString,
                                    @FormParam("resourceTypesMinusDataset") Integer resourceTypesMinusDataset,
                                    @FormParam("suffixPassThrough") String stringSuffixPassThrough,
                                    @FormParam("prefix") String prefix,
                                    @Context HttpServletRequest request) {
        HttpSession session = request.getSession();
        Object username = session.getAttribute("user");
        Hashtable<String, String> config;
        Hashtable<String, String> update = new Hashtable<String, String>();

        if (username == null) {
            return Response.status(401).entity("{\"error\": \"You must be logged in to edit BCIDs.\"}").build();
        }

        // get this BCID's config

        try {
            dataGroupMinter d = new dataGroupMinter();
            config = d.getDataGroupConfig(prefix, username.toString());

            if (config.containsKey("error")) {
                // Some error occured when fetching BCID configuration
                return Response.status(500).entity("{\"error\": \"" + config.get("error") + "\"}").build();
            }

            if (resourceTypesMinusDataset != null && resourceTypesMinusDataset > 0) {
                resourceTypeString = new ResourceTypes().get(resourceTypesMinusDataset).string;
            }

            // compare every field and if they don't match, add them to the update hashtable
            if (doi != null && (!config.containsKey("doi") || !config.get("doi").equals(doi))) {
                update.put("doi", doi);
            }
            if (webaddress != null && (!config.containsKey("webaddress") || !config.get("webaddress").equals(webaddress))) {
                update.put("webaddress", webaddress);
            }
            if (!config.containsKey("title") || !config.get("title").equals(title)) {
                update.put("title", title);
            }
            if (!config.containsKey("resourceType") || !config.get("resourceType").equals(resourceTypeString)) {
                update.put("resourceTypeString", resourceTypeString);
            }
            if ((stringSuffixPassThrough != null && (stringSuffixPassThrough.equals("on") || stringSuffixPassThrough.equals("true")) && config.get("suffix").equals("false")) ||
                    (stringSuffixPassThrough == null && config.get("suffix").equals("true"))) {
                if (stringSuffixPassThrough != null && (stringSuffixPassThrough.equals("on") || stringSuffixPassThrough.equals("true"))) {
                    update.put("suffixPassthrough", "true");
                } else {
                    update.put("suffixPassthrough", "false");
                }
            }

            if (!update.isEmpty()) {
                if (d.updateDataGroupConfig(update, prefix, username.toString())) {
                    return Response.ok("{\"success\": \"BCID successfully updated.\"}").build();
                }
            } else {
                return Response.ok("{\"success\": \"Nothing needed to be updated.\"}").build();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(500).entity(new errorInfo("test", 500).toJSON()).build();
        }
        // if we are here, there was an error during d.updateDataGroupConfig
        return Response.status(500).entity("{\"error\": \"server error.\"}").build();
    }
}
