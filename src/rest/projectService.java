package rest;

import bcid.Renderer.JSONRenderer;
import bcid.Renderer.Renderer;
import bcid.Renderer.TextRenderer;
import bcid.dataGroupMinter;
import bcid.database;
import bcid.manageEZID;
import bcid.GenericIdentifier;
import bcid.resolver;
import bcid.bcid;
import bcid.projectMinter;


import edu.ucsb.nceas.ezid.EZIDException;
import edu.ucsb.nceas.ezid.EZIDService;
import util.SettingsManager;
import util.sendEmail;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * REST interface calls for working with projects.  This includes creating, updating and deleting projects.
 */
@Path("projectService")
public class projectService {

    @Context
    static ServletContext context;

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.TEXT_HTML)
    public Response mint(@FormParam("project_code") String project_code,
                       @FormParam("project_title") String project_title,
                       @FormParam("abstract") String strAbstract,
                       @FormParam("resolverWebAddress") String resolverWebAddress,
                       @FormParam("biovalidator_Validation_xml") String bioValidator_Validation_xml,
                       @Context HttpServletRequest request) throws Exception {

        // Get the user_id
        database db = new database();
        Integer user_id = db.getUserId(request.getRemoteUser());
        if (user_id == null) {
            return Response.status(401).build();
        }

        Integer project_id = null;
        projectMinter project = null;

        try {
            // Mint a project
            project = new projectMinter();
            project_id = project.mint(
                    project_code,
                    project_title,
                    strAbstract,
                    resolverWebAddress,
                    bioValidator_Validation_xml,
                    user_id);
        } catch (Exception e) {
            e.printStackTrace();
            return Response.ok("ERROR: " + e.getMessage()).build();
        }

        // Create EZIDs right away for Dataset level Identifiers

        // Initialize settings manager
        SettingsManager sm = SettingsManager.getInstance();
        try {
            sm.loadProperties();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.ok("ERROR: " + e.getMessage()).build();
        }

        // Initialize ezid account
        /*
        // TODO: fix the ezidAccount registration here--- it seems to be hanging things up
        EZIDService ezidAccount = new EZIDService();
        try {
            // Setup EZID account/login information
            ezidAccount.login(sm.retrieveValue("eziduser"), sm.retrieveValue("ezidpass"));
        } catch (Exception e) {
            e.printStackTrace();
            //return "[\"Project not created, error: " + e.getMessage() + "\"]";
        }
        manageEZID creator = new manageEZID();
        creator.createDatasetsEZIDs(ezidAccount);
        */

        // Send an Email that this completed
        sendEmail sendEmail = new sendEmail(
                sm.retrieveValue("mailUser"),
                sm.retrieveValue("mailPassword"),
                sm.retrieveValue("mailFrom"),
                sm.retrieveValue("mailTo"),
                "New Project",
                project.printMetadata(project_id));
        sendEmail.start();

        return Response.ok("Succesfully created project:<br>" + project.printMetadataHTML(project_id)).build();
    }
}

