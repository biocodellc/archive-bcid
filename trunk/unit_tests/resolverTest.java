package unit_tests;

import bcid.resolver;
import com.sun.jersey.server.wadl.WadlGenerator;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * Test the BCID Resolver
 */
public class resolverTest {
    resolver r;

    /**
     * Test the Resolver by passing in various arks with the expected output
     * @throws Exception
     */
    @Test
    public void resolverTest() throws Exception {

        run("ark:/87286/U264c82d19-6562-4174-a5ea-e342eae353e8",
                "http://biscicol.org/id/metadata/ark:/21547/U264c82d19-6562-4174-a5ea-e342eae353e8",
                "suffixpassthrough = 1; no webAddress specified; has a SourceID");

        run("ark:/21547/R2MBIO56",
                "http://biocode.berkeley.edu/specimens/MBIO56",
                "suffixPassthrough = 1; webaddress specified; has a SourceID");

        run("ark:/21547/R2",
                "http://biscicol.org/id/metadata/ark:/21547/R2",
                "suffixPassthrough = 1; webaddress specified; no SourceID");

        run("ark:/21547/W2",
                "http://biscicol.org/id/metadata/ark:/21547/W2",
                "suffixPassthrough = 0; no webaddress specified; no SourceID");

        run("ark:/21547/Gk2",
                "http://biscicol.org:3030/ds?graph=urn:uuid:77806834-a34f-499a-a29f-aaac51e6c9f8",
                "suffixPassthrough = 0; webaddress specified; no SourceID");

        run("ark:/21547/Gk2FOO",
                "http://biscicol.org:3030/ds?graph=urn:uuid:77806834-a34f-499a-a29f-aaac51e6c9f8FOO",
                "suffixPassthrough = 0; webaddress specified;  sourceID specified (still pass it through");

    }

    /**
     * Run the various resolver tests
     * @param input
     * @param expected
     * @param message
     * @throws Exception
     */
    public void run(String input, String expected, String message) throws Exception {
        resolver r = new resolver(input);
        String actual = r.resolveARK().toString();
        if (actual.equals(expected))
            assertTrue(true);
        else
            assertTrue(message +" (" + input + ") != (" + expected+")", false);
    }
}
