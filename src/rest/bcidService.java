package rest;

import util.SettingsManager;
import bcid.ResourceTypes;
import edu.ucsb.nceas.ezid.EZIDException;
import edu.ucsb.nceas.ezid.EZIDService;


import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.lang.Exception;
import java.lang.String;
import java.util.ArrayList;

/**
 * REST interface for creating and minting BCIDs, to be called from the interface.
 * Security systems need to be configured to only allow access to this service
 * once a user has been authenticated.
 */
@Path("bcidService")
public class bcidService {

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
     * Create a list of Ids from the input data file
     *
     * @param data
     * @param concept
     * @param ezidAsUUID
     * @return String indicating results
     * @throws Exception
     */
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.TEXT_HTML)
    public String createEZIDs(@FormParam("data") String data,
                              @FormParam("concept") int concept,
                              @FormParam("ezidAsUUID") boolean ezidAsUUID,
                              @Context HttpServletRequest request) throws Exception {

        // Create an ArrayList of bcids from the input data
        ArrayList localIds = new bcid.inputFileParser(data, concept).bcidArrayList;
        // Create the minter class
        //TODO: lookup the "who" here

       // minterBCID minter = new minterBCID(new Integer(sm.retrieveValue("bcidNAAN")), ezidAsUUID);
        // Mint the list
        // TODO: get the actual datasets_id here
        //return minter.mintList(localIds, ezidAsUUID, false);
        //return minter.getUniqueDataSetID();
        return "NEED TODO FILL THIS OUT";

    }

    /**
     * Populate select boxes for BCID service options
     *
     * @param select
     * @return String with JSON response
     */
    @GET
    @Path("/select/{select}")
    @Produces(MediaType.APPLICATION_JSON)
    public String jsonSelectOptions(@PathParam("select") String select) {

        if (select.equalsIgnoreCase("resourceTypes")) {
            ResourceTypes rts = new ResourceTypes();
            return rts.getAllAsJSON();
        } else {
            return "[{}]";
        }
    }
}