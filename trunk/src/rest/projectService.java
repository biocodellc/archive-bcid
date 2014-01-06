package rest;

import bcid.database;
import bcid.resolver;
import bcid.projectMinter;


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
    @Path("/associate")
    public Response mint(@FormParam("project_code") String project_code,
                         @FormParam("bcid") String bcid) {
        projectMinter project = null;
        try {
            project = new projectMinter();
            project.attachReferenceToProject(project_code, bcid);

        } catch (Exception e) {
            e.printStackTrace();
            return Response.ok("ERROR: " + e.getMessage()).build();
        }
        return Response.ok("Succesfully associated project_code = " + project_code + " with bcid = " + bcid).build();

    }

    @GET
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.TEXT_HTML)
    @Path("/validateUser/{project_code}")
    public Response mint(@PathParam("project_code") String project_code,
                         @Context HttpServletRequest request) throws Exception {

        // Get the user_id
        database db = new database();
        Integer user_id = db.getUserId(request.getRemoteUser());

        if (user_id == null) {
            return Response.status(401).build();
        }
         projectMinter project = null;

        try {
            // Mint a project
            project = new projectMinter();
            //System.out.println("checking user_id = " + user_id + " & project_code = " + project_code);
            if (project.userOwnsProject(user_id,project_code)) {
                return Response.status(200).build();
            } else {
                return Response.status(202).build();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(405).build();
        }

    }

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.TEXT_HTML)
    public Response mint(@FormParam("project_code") String project_code,
                         @FormParam("project_title") String project_title,
                         @FormParam("abstract") String strAbstract,
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
                    bioValidator_Validation_xml,
                    user_id);
        } catch (Exception e) {
            e.printStackTrace();
            return Response.ok("ERROR: " + e.getMessage()).build();
        }


        // Initialize settings manager
        SettingsManager sm = SettingsManager.getInstance();
        try {
            sm.loadProperties();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.ok("ERROR: " + e.getMessage()).build();
        }

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


    /**
     * Given a project code and a resource alias, return a BCID
     *
     * @param project
     * @param resourceAlias
     * @return
     * @throws Exception
     */
    @GET
    @Path("/{project}/{resourceAlias}")
    @Produces(MediaType.TEXT_HTML)
    public Response fetchAlias(@PathParam("project") String project,
                               @PathParam("resourceAlias") String resourceAlias) throws Exception {

        resolver r = new resolver(project, resourceAlias);
        String response = r.getArk();
        if (response == null) {
            return Response.status(204).build();
        } else {
            return Response.ok(r.getArk()).build();
        }
    }

       /**
     * Given a project code and a resource alias, return a BCID
     *
     * @param project
     * @return
     * @throws Exception
     */
    @GET
    @Path("/deepRoots/{project}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response fetchDeepRoots(@PathParam("project") String project) throws Exception {
        projectMinter projectMinter = new projectMinter();

        String response = projectMinter.getDeepRoots(project);

        if (response == null) {
            return Response.status(204).build();
        } else {
            return Response.ok(response).build();
        }
    }

    /**
     * Given a project code return a validation URL
     *
     * @param project
     * @return
     * @throws Exception
     */
    @GET
    @Path("/{project}")
    @Produces(MediaType.TEXT_HTML)
    public Response fetchAlias(@PathParam("project") String project) throws Exception {

        projectMinter projectMinter = new projectMinter();
        String response = projectMinter.getValidationXML(project);

        if (response == null) {
            return Response.status(204).build();
        } else {
            return Response.ok(response).build();
        }
    }
}

