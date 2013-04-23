package rest;

import bcid.resolver;
import edu.ucsb.nceas.ezid.EZIDException;
import edu.ucsb.nceas.ezid.EZIDService;
import util.SettingsManager;

import javax.servlet.ServletContext;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.io.FileNotFoundException;
import java.lang.Exception;
import java.lang.String;

/**
 * Resolver Service returns JSON representation of results containing identifier metadata.
 * One can parse the JSON result for "Error" for non-responses or bad identifiers.
 * This service should is open to ALL and does not require authentication.
 * 
 * Resolution determines if this is a Data Group, a Data Element with an encoded ID, or a 
 * Data Element with a suffix.
 */
@Path("resolverService")
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
     * User passes in the dataset + identifier
     * @param scheme
     * @param naan
     * @param shoulderPlusIdentifier
     * @return
     */
    @GET
    @Path("/{scheme}/{naan}/{shoulderPlusIdentifier}")
    @Produces(MediaType.APPLICATION_JSON)
    public String run(@PathParam("scheme") String scheme,
                      @PathParam("naan") String naan,
                      @PathParam("shoulderPlusIdentifier") String shoulderPlusIdentifier) {

         scheme = scheme.trim();
        shoulderPlusIdentifier = shoulderPlusIdentifier.trim();
        // Put the identifier components back together, they were separated by incoming REST service
        String element = scheme + "/" + naan + "/" + shoulderPlusIdentifier;

        // Initialize variables
        SettingsManager sm = SettingsManager.getInstance();
        EZIDService ezidService = new EZIDService();
        
        // Setup ezid account/login information
        try {
            ezidService.login(sm.retrieveValue("eziduser"), sm.retrieveValue("ezidpass"));
        } catch (EZIDException e) {
            e.printStackTrace();
        }

        try {
            sm.loadProperties();
            return resolverResults(ezidService, element);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return "[{\"Error\":{\"Message\":\"Unable to load properties file on server: " + e.getMessage() + "\"}}]";
        } catch (EZIDException e) {
            e.printStackTrace();
            return  "[{\"Error\":{\"Message\":\"" + e.getMessage() +"\"}}]";
        } catch (Exception e) {
            e.printStackTrace();
            return "[{\"Error\":{\"Message\":\"" + e.getMessage()+ "\"}}]";
        }
    }

    private String resolverResults(EZIDService ezidService, String identifier) throws Exception {
        resolver r = new resolver(identifier);
        return r.resolveAll(ezidService);
    }
}
