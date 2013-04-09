package rest;

import bcid.dataset;
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
     * Populate select boxes for BCID service options
     *
     * @param 'resourceTypes|resourceTypesMinusDataset'
     * @return String with JSON response
     */
    @GET
    @Path("/select/{select}")
    @Produces(MediaType.APPLICATION_JSON)
    public String jsonSelectOptions(@PathParam("select") String select) {

        if (select.equalsIgnoreCase("resourceTypes")) {
            ResourceTypes rts = new ResourceTypes();
            return rts.getAllAsJSON();
        } else if (select.equalsIgnoreCase("resourceTypesMinusDataset")) {
             ResourceTypes rts = new ResourceTypes();
            return rts.getAllButDatasetAsJSON();
        } else {
            return "[{}]";
        }
    }

    /**
     * Get resoource Types as a TABLE
     * @return
     */
     @GET
    @Path("/resourceTypes")
    @Produces(MediaType.TEXT_HTML)
    public String htmlResourceTypes() {
            ResourceTypes rts = new ResourceTypes();
            return rts.getResourceTypesAsTable();
    }
}