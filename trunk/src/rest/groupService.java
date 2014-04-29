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
import util.SettingsManager;
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
     * @return
     * @throws Exception
     */
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.TEXT_HTML)
    public Response mint(@FormParam("doi") String doi,
                         @FormParam("webaddress") String webaddress,
                         @FormParam("graph") String graph,
                         @FormParam("title") String title,
                         @FormParam("resourceType") String resourceTypeString,
                         @FormParam("resourceTypesMinusDataset") Integer resourceTypesMinusDataset,
                         @FormParam("suffixPassThrough") String stringSuffixPassThrough,
                         @QueryParam("access_token") String accessToken,
                         @Context HttpServletRequest request) throws Exception {

        // If resourceType is specified by an integer, then use that to set the String resourceType.
        // If the user omits
        try {
            if (resourceTypesMinusDataset != null && resourceTypesMinusDataset > 0) {
                resourceTypeString = new ResourceTypes().get(resourceTypesMinusDataset).uri;
            }
        } catch (Exception e) {
            return Response.ok("ERROR: BCID System Unable to set resource type").build();
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
            return Response.status(401).build();
        }

        Boolean suffixPassthrough = false;
        // Format Input variables
        try {
            if (stringSuffixPassThrough.equalsIgnoreCase("true") || stringSuffixPassThrough.equalsIgnoreCase("on")) {
                suffixPassthrough = true;
            }
        } catch (NullPointerException e) {
            suffixPassthrough = false;
        }

        // Initialize settings manager
        SettingsManager sm = SettingsManager.getInstance();
        try {
            sm.loadProperties();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.ok("ERROR: " + e.getMessage()).build();
        }

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
        // TODO: fix the ezidAccount registration here--- it seems to be hanging things up
        // Initialize ezid account

        ezidAccount = new EZIDService();
        try {
            // Setup EZID account/login information
            ezidAccount.login(sm.retrieveValue("eziduser"), sm.retrieveValue("ezidpass"));

        } catch (EZIDException e) {
            e.printStackTrace();
            return Response.ok("ERROR: " + e.getMessage()).build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.ok("ERROR: " + e.getMessage()).build();
        }

        manageEZID creator = new manageEZID();
        creator.createDatasetsEZIDs(ezidAccount);

        // Send an Email that this completed
        sendEmail sendEmail = new sendEmail(sm.retrieveValue("mailUser"),
                sm.retrieveValue("mailPassword"),
                sm.retrieveValue("mailFrom"),
                sm.retrieveValue("mailTo"),
                "New Dataset Group",
                new resolver(minterDataset.getPrefix()).printMetadata(new TextRenderer()));
        sendEmail.start();

        return Response.ok(datasetPrefix).build();

    }

    /**
     * Return a JSON representation of dataset metadata
     *
     * @param dataset_id
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
    public String datasetList(@Context HttpServletRequest request) {
        HttpSession session = request.getSession();
        String username = session.getAttribute("user").toString();

        dataGroupMinter d = null;
        try {
            d = new dataGroupMinter();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return d.datasetList(username);
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
            return p.expeditionTable(username);
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return "Exception encountered attempting to list expeditions";
    }

    /**
     * returns an HTML table used to edit a bcid's configuration.
     * @param prefix
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
        }
        return "Server error loading BCID editor.";
    }

    /**
     * Service to update a bcid's configuration.
     * @param doi
     * @param webaddress
     * @param title
     * @param resourceTypeString
     * @param resourceTypesMinusDataset
     * @param stringSuffixPassThrough
     * @param prefix
     * @return
     */
    @POST
    @Path("/dataGroup/update")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public String dataGroupUpdate(@FormParam("doi") String doi,
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
            return "[{\"error\": \"You must be logged in to edit BCIDs.\"}]";
        }

        // get this BCID's config

        try {
            dataGroupMinter d = new dataGroupMinter();
            config = d.getDataGroupConfig(prefix, username.toString());

            if (config.containsKey("error")) {
                // Some error occured when fetching BCID configuration
                return "[{\"error\": \"" + config.get("error") + "\"}]";
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
                    return "[{\"success\": \"BCID successfully updated.\"}]";
                }
            } else {
                return "[{\"success\": \"Nothing needed to be updated.\"}]";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // if we are here, there was a server error
        return "[{\"error\": \"server error.\"}]";
    }
}
