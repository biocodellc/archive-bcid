package rest;

import bcid.*;
import bcid.Renderer.TextRenderer;
import net.sf.json.JSONArray;
import util.SettingsManager;
import edu.ucsb.nceas.ezid.EZIDException;
import edu.ucsb.nceas.ezid.EZIDService;
import util.sendEmail;


import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.lang.Exception;
import java.lang.String;
import java.net.URISyntaxException;
import java.util.ArrayList;

/**
 * REST interface for creating elements, to be called from the interface or other consuming applications.
 */
@Path("elementService")
public class elementService {

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
     *
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
     * Get resourceTypes as a TABLE
     *
     * @return
     */
    @GET
    @Path("/resourceTypes")
    @Produces(MediaType.TEXT_HTML)
    public String htmlResourceTypes() {
        ResourceTypes rts = new ResourceTypes();
        return rts.getResourceTypesAsTable();
    }


    /**
     * Create a bunch of BCIDs
     *
     * @param dataset_id
     * @param title
     * @param resourceType
     * @param data
     * @param doi
     * @param webaddress
     * @param request
     * @return
     */
    @POST
    @Path("/creator")
    @Produces(MediaType.APPLICATION_JSON)
    public String creator(@FormParam("datasetList") Integer dataset_id,
                          @FormParam("title") String title,
                          @FormParam("resourceTypesMinusDataset") Integer resourceType,
                          @FormParam("data") String data,
                          @FormParam("doi") String doi,
                          @FormParam("webaddress") String webaddress,
                          @FormParam("graph") String graph,
                          @FormParam("suffixPassThrough") String stringSuffixPassThrough,
                          @Context HttpServletRequest request) {

        dataGroupMinter dataset = null;
        database db = null;
        Boolean suffixPassthrough = false;
        HttpSession session = request.getSession();
        String username = session.getAttribute("user").toString();


        // Initialize database
        try {
            db = new database();
        } catch (Exception e) {
            e.printStackTrace();
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }

        // Get the user_id
        Integer user_id = db.getUserId(username);

        // Request creation of new dataset
        if (dataset_id == 0) {

            // Format Input variables
            try {
                if (stringSuffixPassThrough.equalsIgnoreCase("true") || stringSuffixPassThrough.equalsIgnoreCase("on")) {
                    suffixPassthrough = true;
                }
            } catch (NullPointerException e) {
                suffixPassthrough = false;
            }

            // Some input form validation
            // TODO: create a generic way of validating this input form content
            if (dataset_id != 0 &&
                    (resourceType == 0 ||
                            resourceType == ResourceTypes.SPACER1 ||
                            resourceType == ResourceTypes.SPACER2 ||
                            resourceType == ResourceTypes.SPACER3 ||
                            resourceType == ResourceTypes.SPACER4 ||
                            resourceType == ResourceTypes.SPACER5 ||
                            resourceType == ResourceTypes.SPACER6 ||
                            resourceType == ResourceTypes.SPACER7)
                    ) {
                return "[\"Error: Must choose a valid concept!\"]";
            }
            // TODO: check for valid local ID's, no reserved characters

            // Create a new dataset
            try {
                dataset = new dataGroupMinter(true, suffixPassthrough);
                // we don't know DOI or webaddress from this call, so we set them to NULL
                dataset.mint(new Integer(sm.retrieveValue("bcidNAAN")), user_id, resourceType, doi, webaddress, graph, title);
            } catch (Exception e) {
                e.printStackTrace();
                throw new WebApplicationException(Response.Status.BAD_REQUEST);
            }
            // Load an existing dataset we've made already
        } else {
            try {
                dataset = new dataGroupMinter(dataset_id);
            } catch (Exception e) {
                e.printStackTrace();
                throw new WebApplicationException(Response.Status.BAD_REQUEST);
            }

            // TODO: check that dataset.users_id matches the user that is logged in!

        }

        // Parse input file
        ArrayList elements = null;
        try {
            elements = new inputFileParser(data, dataset).elementArrayList;
        } catch (IOException e) {
            e.printStackTrace();
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        } catch (URISyntaxException e) {
            e.printStackTrace();
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }

        // Create a bcid Minter instance
        elementMinter minter = null;
        try {
            minter = new elementMinter(dataset.getDatasets_id());
        } catch (Exception e) {
            e.printStackTrace();
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }

        // Mint the list of identifiers
        String datasetUUID = null;
        try {
            datasetUUID = minter.mintList(elements);
        } catch (Exception e) {
            e.printStackTrace();
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }

        // Array of identifiers, or an error message
        String returnVal = JSONArray.fromObject(minter.getIdentifiers(datasetUUID)).toString();


        // Send an Email that this completed
        sendEmail sendEmail = new sendEmail(sm.retrieveValue("mailUser"),
                sm.retrieveValue("mailPassword"),
                sm.retrieveValue("mailFrom"),
                sm.retrieveValue("mailTo"),
                "New Elements From " + username,
                returnVal);
        sendEmail.start();

        return returnVal;
    }
}