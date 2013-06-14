package rest;

import bcid.TestingRenderer.Renderer.Renderer;
import bcid.resolver;
import edu.ucsb.nceas.ezid.EZIDException;
import edu.ucsb.nceas.ezid.EZIDService;
import util.SettingsManager;

import javax.servlet.ServletContext;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.FileNotFoundException;
import java.lang.Exception;
import java.lang.String;
import java.net.URI;

/**
 * The resolver service searches for identifiers in the BCID system and in EZID and returns a JSON
 * representation of results containing identifier metadata.
 * One can parse the JSON result for "Error" for non-responses or bad identifiers.
 * This service is open to ALL and does not require authentication.
 * <p/>
 * Resolution determines if this is a Data Group, a Data Element with an encoded ID, or a
 * Data Element with a suffix.
 */
@Path("ark:")
public class resolverService {
    static SettingsManager sm;
    @Context
    static ServletContext context;

    /**
     * Load settings manager
     */
    static {
        // Initialize settings manager
        sm = SettingsManager.getInstance();
        try {
            sm.loadProperties();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * User passes in an identifier of the form scheme:/naan/shoulder_identifier
     * @param naan
     * @param shoulderPlusIdentifier
     * @return
     */
    @GET
    @Path("/{naan}/{shoulderPlusIdentifier}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response run(
            @PathParam("naan") String naan,
            @PathParam("shoulderPlusIdentifier") String shoulderPlusIdentifier) {

        // Clean up input
        //scheme = scheme.trim();
        String scheme = "ark:";
        shoulderPlusIdentifier = shoulderPlusIdentifier.trim();
        
        // Structure the identifier element from path parameters
        String element = scheme + "/" + naan + "/" + shoulderPlusIdentifier;

        // SettingsManager
        SettingsManager sm = SettingsManager.getInstance();
        try {
            sm.loadProperties();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
            //return new serviceErrorReporter(e, "Unable to load properties file on server").json();
        }

        // Setup ezid account/login information
        EZIDService ezidService = new EZIDService();
        try {
            ezidService.login(sm.retrieveValue("eziduser"), sm.retrieveValue("ezidpass"));
        } catch (EZIDException e) {
            // For now, just print stack trace here and proceed.
            e.printStackTrace();
        }


        URI seeOtherUri = null;
        try {
            seeOtherUri = new resolver(element).resolveARK();
        } catch (Exception e) {
            e.printStackTrace();
           return null;
        }

        return Response.status(303).location(seeOtherUri).build();

       /* // Run Resolver
        try {
            return new bcid.resolver(element).resolveAllAsJSON(ezidService);                               
        } catch (EZIDException e) {
            return new serviceErrorReporter(e).json();
        } catch (Exception e) {
            return new serviceErrorReporter(e).json();
        }
        return null;
        */
    }
}
