package unit_tests;

import bcid.dataGroupMinter;
import bcid.database;
import bcid.resolver;
import org.junit.Test;
import util.SettingsManager;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Test DataGroup minting
 */
public class dataGroupMinterTest {
    // Initialize settings manager
    SettingsManager sm = SettingsManager.getInstance();


    @Test
    public void testMinter() throws Exception {
        sm.loadProperties();

        // Create a Dataset
        database db = null;
        db = new database();

        // Check for remote-user
        Integer user_id = db.getUserId("demo");
        db.close();

        // Mint the data group
        dataGroupMinter minter = null;
        minter = new dataGroupMinter(false, true);

        minter.mint(
                new Integer(sm.retrieveValue("bcidNAAN")),
                user_id,
                "urn:Test",
                null,
                "http://biocode.berkeley.edu/specimens/",
                null,
                "TEST minter");
        minter.close();
        String datasetPrefix = minter.getPrefix();

        // Test that the prefix was created
        if (datasetPrefix != null && !datasetPrefix.equals("")) {
            assertTrue(true);
        } else {
            assertTrue("No prefix or NULL prefix returned!", false);
        }

        // Test that some identifier was created and works with the resolution + suffixPassthrough Resolver
        resolver r = new resolver(datasetPrefix + "MBIO56");
        String actual = r.resolveARK().toString();
        r.close();
        String expected = "http://biocode.berkeley.edu/specimens/MBIO56";
        if (actual.equals(expected))
            assertTrue(true);
        else
            assertFalse("resolution service isssues with our new identifier", false);
    }
}
