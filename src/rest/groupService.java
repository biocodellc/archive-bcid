package rest;

import bcid.Renderer.JSONRenderer;
import bcid.Renderer.Renderer;
import bcid.Renderer.TextRenderer;
import bcid.dataGroupMinter;
import bcid.projectMinter;
import bcid.database;
import bcid.manageEZID;
import bcid.GenericIdentifier;
import bcid.resolver;
import bcid.bcid;


import edu.ucsb.nceas.ezid.EZIDException;
import edu.ucsb.nceas.ezid.EZIDService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.authentication.AuthenticationManagerBeanDefinitionParser;
import org.springframework.security.config.authentication.AuthenticationManagerFactoryBean;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.GrantedAuthorityImpl;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import util.SettingsManager;
import util.sendEmail;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.swing.plaf.basic.BasicInternalFrameTitlePane;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

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
     * @param resourceType
     * @param request
     * @return
     * @throws Exception
     */
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.TEXT_HTML)
    public Response mint(@FormParam("doi") String doi,
                       @FormParam("webaddress") String webaddress,
                       @FormParam("title") String title,
                       @FormParam("resourceTypesMinusDataset") Integer resourceType,
                       @FormParam("suffixPassThrough") String stringSuffixPassThrough,
                       @Context HttpServletRequest request) throws Exception {

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
        Integer user_id = db.getUserId(request.getRemoteUser());
        if (user_id == null) {
            // status=401 means unauthorized user
            return Response.status(401).build();
        }

        // Detect if this is user=demo or not.  If this is "demo" then do not request EZIDs.
        // User account Demo can still create Data Groups, but they just don't get registered and will be purged periodically
        boolean ezidRequest = true;
        if (request.getRemoteUser().equals("demo")) {
            ezidRequest = false;
        }

        // Mint the data group
        dataGroupMinter minterDataset = new dataGroupMinter(ezidRequest, suffixPassthrough);
        minterDataset.mint(
                new Integer(sm.retrieveValue("bcidNAAN")),
                user_id,
                resourceType,
                doi,
                webaddress,
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
        dataGroupMinter d = null;
        try {
            d = new dataGroupMinter();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return d.datasetList(request.getRemoteUser());
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
        dataGroupMinter d = null;
        try {
            d = new dataGroupMinter();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return d.datasetTable(request.getRemoteUser());
    }

      /**
     * Return HTML response showing a table of groups belonging to this user
     *
     * @return String with HTML response
     */
    @GET
    @Path("/listUserProjectsAsTable")
    @Produces(MediaType.TEXT_HTML)
    public String listUserProjectsAsTable(@Context HttpServletRequest request) {
        projectMinter p = null;
        try {
            p = new projectMinter();
            return p.projectTable(request.getRemoteUser());
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return "Exception encountered attempting to list projects";
    }

}