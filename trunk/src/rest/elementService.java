package rest;

import bcid.*;
import bcid.Renderer.TextRenderer;
import net.sf.json.JSONArray;
import util.SettingsManager;
import ezid.EZIDException;
import ezid.EZIDService;
import util.errorInfo;
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
            return "{}";
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
    public Response creator(@FormParam("datasetList") Integer dataset_id,
                            @FormParam("title") String title,
                            @FormParam("resourceTypesMinusDataset") Integer resourceType,
                            @FormParam("data") String data,
                            @FormParam("doi") String doi,
                            @FormParam("webaddress") String webaddress,
                            @FormParam("graph") String graph,
                            @FormParam("suffixPassThrough") String stringSuffixPassThrough,
                            @Context HttpServletRequest request) {

        try {
            dataGroupMinter dataset = null;
            database db = null;
            Boolean suffixPassthrough = false;
            HttpSession session = request.getSession();
            String username = session.getAttribute("user").toString();


            // Initialize database
            db = new database();

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
                    return Response.status(400).entity("{\"error: Must choose a valid concept!\"}").build();
                }
                // TODO: check for valid local ID's, no reserved characters

                // Create a new dataset
                dataset = new dataGroupMinter(true, suffixPassthrough);
                // we don't know DOI or webaddress from this call, so we set them to NULL
                dataset.mint(
                        new Integer(sm.retrieveValue("bcidNAAN")),
                        user_id,
                        new ResourceTypes().get(resourceType).uri,
                        doi,
                        webaddress,
                        graph,
                        title);
                // Load an existing dataset we've made already
            } else {
                dataset = new dataGroupMinter(dataset_id);

                // TODO: check that dataset.users_id matches the user that is logged in!

            }

            // Parse input file
            ArrayList elements = null;
            elements = new inputFileParser(data, dataset).elementArrayList;

            // Create a bcid Minter instance
            elementMinter minter = null;
            minter = new elementMinter(dataset.getDatasets_id());

            // Mint the list of identifiers
            String datasetUUID = null;
            datasetUUID = minter.mintList(elements);

            // Array of identifiers, or an error message
            String returnVal = JSONArray.fromObject(minter.getIdentifiers(datasetUUID)).toString();


            // Send an Email that this completed
           /* sendEmail sendEmail = new sendEmail(sm.retrieveValue("mailUser"),
                    sm.retrieveValue("mailPassword"),
                    sm.retrieveValue("mailFrom"),
                    sm.retrieveValue("mailTo"),
                    "New Elements From " + username,
                    returnVal);
            sendEmail.start();
            */

            return Response.ok(returnVal).build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(500).entity(new errorInfo(e, request).toJSON()).build();
        }
    }
}