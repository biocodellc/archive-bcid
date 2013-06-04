package rest;

import bcid.Renderer.JSONRenderer;
import bcid.Renderer.Renderer;
import bcid.Renderer.TextRenderer;
import bcid.dataGroupMinter;
import bcid.database;
import bcid.bcid;
import bcid.GenericIdentifier;
import bcid.resolver;

import edu.ucsb.nceas.ezid.EZIDException;
import edu.ucsb.nceas.ezid.EZIDService;
import util.SettingsManager;
import util.sendEmail;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

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

        // Initialize ezid account
        EZIDService ezidAccount = new EZIDService();
        try {
            // Setup EZID account/login information
            ezidAccount.login(sm.retrieveValue("eziduser"), sm.retrieveValue("ezidpass"));

        } catch (EZIDException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Create a data group
     *
     * @param doi
     * @param webaddress
     * @param title
     * @param resourceType
     * @param request
     * @return
     * @throws Exception
     */
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public String mint(@FormParam("doi") String doi,
                       @FormParam("webaddress") String webaddress,
                       @FormParam("title") String title,
                       @FormParam("resourceTypesMinusDataset") Integer resourceType,
                       @FormParam("suffixPassThrough") String stringSuffixPassThrough,
                       @Context HttpServletRequest request) throws Exception {

        //System.out.println(doi + "|" + webaddress + "|" + title + "|" + resourceType + "|" +stringSuffixPassThrough);
        Boolean suffixPassthrough = false;
        // Format Input variables
        try {
            if (stringSuffixPassThrough.equalsIgnoreCase("true") || stringSuffixPassThrough.equalsIgnoreCase("on")) {
                suffixPassthrough = true;
            }
        } catch (NullPointerException e) {
            suffixPassthrough = false;
        }

        // TODO: go through and validate these values before submitting-- need to catch all input from UI
        // Create a Dataset
        database db = new database();
        Integer user_id = db.getUserId(request.getRemoteUser());

        dataGroupMinter minterDataset = new dataGroupMinter(false, suffixPassthrough);
        minterDataset.mint(
                new Integer(sm.retrieveValue("bcidNAAN")),
                user_id,
                resourceType,
                doi,
                webaddress,
                title);
        minterDataset.close();

        String datasetPrefix = minterDataset.getPrefix();

        // Send an Email that this completed
        sendEmail sendEmail = new sendEmail(sm.retrieveValue("mailUser"),
                sm.retrieveValue("mailPassword"),
                sm.retrieveValue("mailFrom"),
                sm.retrieveValue("mailTo"),
                "New Dataset Group",
                new resolver(minterDataset.getPrefix()).resolveARK(new TextRenderer()));
        sendEmail.start();

        return "[\"" + datasetPrefix + "\"]";
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

        return "[" + renderer.renderIdentifier(bcid) + "]";
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
        dataGroupMinter d = null;
        try {
            d = new dataGroupMinter();
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        System.out.println("authenticated as " + request.getRemoteUser());
        return d.datasetList(request.getRemoteUser());
    }
}
