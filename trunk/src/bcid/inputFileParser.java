package bcid;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Parse an input File and construct an element Iterator which can be fetched
 */
public class inputFileParser {

    public ArrayList<bcid> elementArrayList = new ArrayList();

    /**
     * Main method to demonstrate how this is used
     * @param args
     */
    public static void main(String args[]) {

        /*
        String sampleInputStringFromTextBox = "" +

                "MBIO056\thttp://biocode.berkeley.edu/specimens/MBIO56\n" +
                "56\n";
        inputFileParser parse = null;
        try {
            parse = new inputFileParser(sampleInputStringFromTextBox, DATASET_ID);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            System.out.println("Invalid URI specified");
        }

        Iterator pi = parse.iterator();
        while (pi.hasNext()) {
            element b = (element)pi.next();
             System.out.println("sourceid = " + b.sourceID + ";webaddres = " + b.webAddress );
        }
        */

    }

    /**
     * Parse an input file and turn it into an Iterator containing elements
     * @param inputString
     * @throws IOException
     * @throws URISyntaxException
     */
    public inputFileParser(String inputString, Integer dataset_id) throws IOException, URISyntaxException {
        BufferedReader readbuffer = new BufferedReader(new StringReader(inputString));
        String strRead;
        while ((strRead = readbuffer.readLine()) != null) {
            String sourceID = null, webAddress = null;
            String splitarray[] = strRead.split("\t");
            sourceID = splitarray[0];
            try {
                webAddress = splitarray[1];
                elementArrayList.add(new bcid(sourceID, new URI(webAddress),dataset_id));
            } catch (ArrayIndexOutOfBoundsException e) {
                // ArrayIndexOutOfBounds here we just assume sourceID
                elementArrayList.add(new bcid(sourceID,dataset_id));
            }
        }
    }

    /**
     * Return an iterator of element objects
     * @return Iterator of BCIDs
     */
    public Iterator iterator() {
        return elementArrayList.iterator();
    }
}
