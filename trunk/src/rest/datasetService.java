package rest;

import bcid.database;
import bcid.dataset;
import bcid.resolver;
import bcid.bcid;

import edu.ucsb.nceas.ezid.EZIDException;
import edu.ucsb.nceas.ezid.EZIDService;
import util.SettingsManager;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

/**
 * REST interface for creating and minting dataset ARK
 */
@Path("datasetService")
public class datasetService {

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
     * Mint a dataset
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
    @Produces(MediaType.TEXT_HTML)
    public String mint(@FormParam("doi") String doi,
                       @FormParam("webaddress") String webaddress,
                       @FormParam("title") String title,
                       @FormParam("concept") Integer resourceType,
                       @Context HttpServletRequest request) throws Exception {

        // TODO: go through and validate these values before submitting-- need to catch all input from UI
        // Create a Dataset
        database db = new database();
        Integer user_id = db.getUserId(request.getRemoteUser());
        dataset minterDataset = new dataset(false);
        minterDataset.mint(
                new Integer(sm.retrieveValue("bcidNAAN")),
                user_id,
                resourceType,
                doi,
                webaddress,
                title);
        minterDataset.close();

        return minterDataset.getPrefix();
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
        return new bcid(dataset_id).json();

    }

    /**
     * Return JSON response showing datasets available to this user
     *
     * @return String with JSON response
     */
    @GET
    @Path("/list")
    @Produces(MediaType.APPLICATION_JSON)
    public String datasetList(@Context HttpServletRequest request) {
        dataset d = null;
        try {
            d = new dataset(true);
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        System.out.println("authenticated as " + request.getRemoteUser());
        return d.datasetList(request.getRemoteUser());
    }
}
