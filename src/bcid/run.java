package bcid;

import edu.ucsb.nceas.ezid.EZIDException;
import edu.ucsb.nceas.ezid.EZIDService;
import net.sf.json.JSONArray;
import util.SettingsManager;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.Exception;
import java.lang.String;
import java.lang.System;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.UUID;


/**
 * This is a class used for running/testing Identifier creation methods.
 */
public class run {
    // a testData file to use for various tests in this class
    ArrayList testDatafile;

    public run() {
        // Set this to the TEST dataset
        Integer dataset_id = 1;

        // Create test data
        System.out.println("\nConstructing Test ArrayList of LocalId's ...");
        String sampleInputStringFromTextBox = "" +
                "MBIO056\thttp://biocode.berkeley.edu/specimens/MBIO56\n" +
                "56\n" +
                "urn:uuid:1234-abcd-5678-efgh-9012-ijkl\n" +
                "38543e40-665f-11e2-89f3-001f29e2923c";

        try {
            testDatafile = new inputFileParser(sampleInputStringFromTextBox,dataset_id).elementArrayList;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        System.out.println("  Successfully created test dataset");
    }

    /**
     * This method runs through all the permutations for working with identifiers, and
     * provides examples for their common use.
     *
     * @param ezidAccount
     * @throws Exception
     */
    private void runBiSciColIdentifierTools(EZIDService ezidAccount, Integer naan, Integer who) throws Exception {


        // Create a minter object to use for all these samples
        System.out.println("\nCreating a minter object by passing in an existing Dataset Prefix ...");
        // Creation method Example #1, no arguments is just the default test case ark:/99999/fk4
        // minterBCID minter = new minterBCID();
        // Creation method Example #2, provide a NAAN & a user, which automatically creates a new shoulder
        // minterBCID minter = new minterBCID("87286",1);
        // Creation method Example #3, pass in an existing prefix and use that
        elementMinter minter = new elementMinter(87286, "C2", false, true);
        System.out.println("  prefix=" + minter.prefix);
        System.out.println("  datasets_id=" + minter.getDatasets_id());

        // Create a bcid for localId without Suffix passthrough
        System.out.println("\nCreate bcids WITHOUT suffix passthrough from test dataset ...");
        String datasetIdentifier = minter.mintList(testDatafile);
        System.out.println("  datasetIdentifier = " + datasetIdentifier);
        Iterator ezids1It = minter.getIdentifiers(datasetIdentifier).iterator();
        while (ezids1It.hasNext()) {
            System.out.println("  " + ezids1It.next());
        }
        System.out.println("  cleaning up ...");
        minter.deleteLoadedSetUUID(datasetIdentifier);

        // Create a bcid for each localId's  with Suffix passthrough
        System.out.println("\nCreate bcids WITH suffix passthrough from test dataset ...");
        System.out.println("  datasetIdentifier = " + datasetIdentifier);
        datasetIdentifier = minter.mintList(testDatafile);
        Iterator ezids2It = minter.getIdentifiers(datasetIdentifier).iterator();
        while (ezids2It.hasNext()) {
            System.out.println("  " + ezids2It.next());
        }
        System.out.println("  cleaning up ...");
        minter.deleteLoadedSetUUID(datasetIdentifier);

        // Create a bunch of bcids
        int num = 10;
        System.out.println("\nCreate " + num + " bcids (a reserved slot in our database) ...");
        datasetIdentifier = minter.createBCIDs(num, new URI("http://purl.org/dc/dcmitype/PhysicalObject"));
        System.out.println("  datasetIdentifier = " + datasetIdentifier);
        Iterator guidsIt = minter.getIdentifiers(datasetIdentifier).iterator();
        while (guidsIt.hasNext()) {
            System.out.println("  " + guidsIt.next());
        }
        System.out.println("  cleaning up (deleting these identifiers cause we're just testing here) ...");
        minter.deleteLoadedSetUUID(datasetIdentifier);

        // Create bcids from Given uuids with Suffix Passthrough
        System.out.println("\nCreate test uuid dataset ...");
        String uuidInputStringFromTextBox = "" +
                UUID.randomUUID() + "\thttp://biocode.berkeley.edu/specimens/MBIO57\n" +
                UUID.randomUUID() + "\n" +
                UUID.randomUUID();
        ArrayList localUUIDs = new inputFileParser(uuidInputStringFromTextBox, 1).elementArrayList;
        System.out.println("  Successfully created test uuid dataset");

        // Create a bcid for each localId's
        System.out.println("\nCreating bcids with uuid suffix passthrough from test uuid dataset (FSU case) ...");
        datasetIdentifier = minter.mintList(localUUIDs);
        System.out.println("  datasetIdentifier = " + datasetIdentifier);
        Iterator uuidsIt = minter.getIdentifiers(datasetIdentifier).iterator();
        while (uuidsIt.hasNext()) {
            System.out.println("  " + uuidsIt.next());
        }

        // Return information about an identifier (resolver service)
        //ark:/87286/C2
        //ark:/87286/C2eee9cd5b-fd7d-40f9-acc6-362770e7bfde
        //1. Look in the identifiers table and resolve there.
        //2. Look in the datasets table

        // Create bcids from Given uuids with Suffix Passthrough
        System.out.println("\nCreate a second test uuid dataset ...");
        String uuidInputStringFromTextBox2 = "" +
                UUID.randomUUID() + "\thttp://biocode.berkeley.edu/specimens/MBIO57\n" +
                UUID.randomUUID() + "\n" +
                UUID.randomUUID();
        ArrayList localUUIDs2 = new inputFileParser(uuidInputStringFromTextBox2, 1).elementArrayList;
        System.out.println("  Successfully created test uuid dataset #2");

        // Create a bcid for each localId's
        System.out.println("\nCreating bcidS WITHOUT uuid suffix passthrough from test uuid dataset #2 ...");
        datasetIdentifier = minter.mintList(localUUIDs2);
        Iterator uuidsIt2 = minter.getIdentifiers(datasetIdentifier).iterator();
        while (uuidsIt2.hasNext()) {
            System.out.println("  " + uuidsIt2.next());
        }

        // TODO: Send URL Link to results
        // Scan Identifiers table and create ezids where ezidRequest = true && ezidMade = false
        System.out.println("\nBatch create ezids for Identifiers ...");
        System.out.println("  OPTION disabled here for now; we cannot scale for all requests, but we want to maintain them.");
        //manageEZIDs creator = new manageEZIDs();
        //creator.createEZIDs(ezidAccount);

        // Scan Datasets table and create ezids where ezidRequest = true && ezidMade = false
        System.out.println("\nBatch create ezids for Datasets ...");
        manageEZID creator = new manageEZID();
        creator.createDatasetsEZIDs(ezidAccount);
        System.out.println(" Scanned datasets table and created any dataset ezids");

        // Force an update on an individual bcid
        System.out.println("\nUpdate a single ezid metadata record for id = " + minter.getDatasets_id() + "  ...");
        creator.updateDatasetsEZID(ezidAccount, minter.getDatasets_id());

        // Create a Dataset
        /* System.out.println("\nCreate a new dataset object:");
        dataset dataset = new dataset(false);
        dataset.mint(naan,  who, new ResourceTypes().RESOURCE, null, "http://www.google.com/", "this is a test");
        System.out.println("  Created " + dataset.prefix);
        dataset.close();
        */

        // Close connection
        minter.close();
    }

    private static void resolverResults(EZIDService ezidService, String identifier) {
        try {
            resolver r = new resolver(identifier);
            System.out.println("Attempting to resolve " + identifier);
            System.out.println(r.resolveAllAsJSON(ezidService));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Test the Resolution Services
     *
     * @param ezidService
     */
    public static void resolver(EZIDService ezidService) {
        resolverResults(ezidService, "ark:/87286/C2/AOkI");
        resolverResults(ezidService, "ark:/87286/C2/64c82d19-6562-4174-a5ea-e342eae353e8");
        resolverResults(ezidService, "ark:/87286/C2/Foo");
        resolverResults(ezidService, "ark:/87286/Cddfdf2");
    }

    /**
     * Test creating a bunch of BCIDs as if it were being called from a REST service
     * @param sm
     * @param user_id
     * @param suffixPassthrough
     * @throws Exception
     */
    private void runBCIDCreatorService(EZIDService ezidAccount, SettingsManager sm, int user_id, Boolean suffixPassthrough) throws Exception {
        new Integer(sm.retrieveValue("bcidNAAN"));

        // Create a minter object
        elementMinter minter = new elementMinter(true,suffixPassthrough);
        minter.mint(new Integer(sm.retrieveValue("bcidNAAN")), user_id, ResourceTypes.SOUND, null, null, "Test Title");

        // Mint a list of identifiers
        String datasetUUID = minter.mintList(testDatafile);

        // Return the list of identifiers that were made here
        System.out.println(JSONArray.fromObject(minter.getIdentifiers(datasetUUID)).toString());


                    // Force an update on an individual bcid
       // System.out.println("\nUpdate a single ezid metadata record for id = " + 3 + "  ...");
      //  manageEZID me = new manageEZID();
      //  me.updateDatasetsEZID(ezidAccount, 3);

        // What does a client want in return?
        /*
        Dataset level data:
        ArkID, Type, title
        The created identifiers

         */
    }

    public static void main(String[] args) {

            run r = new run();
            // Initialize variables
            SettingsManager sm = SettingsManager.getInstance();
            EZIDService ezidAccount = new EZIDService();

            try {
                // Setup ezid account/login information
                sm.loadProperties();
                ezidAccount.login(sm.retrieveValue("eziduser"), sm.retrieveValue("ezidpass"));
                // Go through the processes of DOI assignment, bcid creation, ezid creation
                //runAllServices(ds, ezidAccount);

                // GetMyGUID, service #1
                // Pass in a long list of local identifiers, and mint bcids
                //r.runBiSciColIdentifierTools(ezidAccount, new Integer(sm.retrieveValue("bcidNAAN")), 1);

                // create BCIDs with suffixPassthrough for user_id =1
                r.runBCIDCreatorService(ezidAccount,sm, 1, false);



                //resolver(ezidAccount);

            } catch (EZIDException e) {
                e.printStackTrace();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

}
