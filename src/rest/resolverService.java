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
 * Resolver Service Call, returning JSON representation of results containing identifier metadata.
 * One can parse the JSON result for "Error" for non-responses, bad identifiers.
 * This service should be open to ALL and not require user authentication.
 */
@Path("resolver")
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
     * Users just passes in the dataset
     * @param scheme
     * @param naan
     * @param shoulder
     * @return
     */
    @GET
    @Path("/{scheme}/{naan}/{shoulder}")
    @Produces(MediaType.APPLICATION_JSON)
    public String run(@PathParam("scheme") String scheme,
                      @PathParam("naan") String naan,
                      @PathParam("shoulder") String shoulder
                      ) {
        return run(scheme, naan, shoulder, "");
    }

    /**
     * User passes in the dataset + identifier
     * @param scheme
     * @param naan
     * @param shoulder
     * @param identifier
     * @return
     */
    @GET
    @Path("/{scheme}/{naan}/{shoulder}/{identifier}")
    @Produces(MediaType.APPLICATION_JSON)
    public String run(@PathParam("scheme") String scheme,
                      @PathParam("naan") String naan,
                      @PathParam("shoulder") String shoulder,
                      @PathParam("identifier") String identifier) {

        // decode this identifier
        String bcid = scheme + "/" + naan + "/" + shoulder + "/" + identifier;

        // Initialize variables
        SettingsManager sm = SettingsManager.getInstance();
        EZIDService ezidAccount = new EZIDService();
        // Setup ezid account/login information

        try {
            sm.loadProperties();
            ezidAccount.login(sm.retrieveValue("eziduser"), sm.retrieveValue("ezidpass"));
            return resolverResults(ezidAccount, bcid);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return "[{\"Error\":\"Unable to load properties file on server: " + e.getMessage() + "\"}]";
        } catch (EZIDException e) {
            e.printStackTrace();
            return  "[{\"Error\":\"Exception accessing EZID Service\"}]";
        } catch (Exception e) {
            e.printStackTrace();
            return "[{\"Error\":\"Identifier " + bcid + ", may be badly formed\"}]";
        }
    }

    private String resolverResults(EZIDService ezidService, String identifier) throws Exception {
        resolver r = new resolver(identifier);
        return r.resolveAll(ezidService);
        //return new bcid.resolver(identifier).resolveAll(ezidService);
    }
}
