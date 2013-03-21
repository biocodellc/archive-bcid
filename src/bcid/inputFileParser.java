package bcid;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Parse an input File and construct a bcid Iterator which can be fetched
 */
public class inputFileParser {

    public ArrayList<bcid> bcidArrayList = new ArrayList();

    /**
     * Main method to demonstrate how this is used
     * @param args
     */
    public static void main(String args[]) {
        String sampleInputStringFromTextBox = "" +
                "MBIO056\thttp://biocode.berkeley.edu/specimens/MBIO56\n" +
                "56\n";
        inputFileParser parse = null;
        try {
            parse = new inputFileParser(sampleInputStringFromTextBox, ResourceTypes.EVENT);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            System.out.println("Invalid URI specified");
        }

        Iterator pi = parse.iterator();
        while (pi.hasNext()) {
            bcid b = (bcid)pi.next();
             System.out.println("sourceid = " + b.sourceID + ";webaddres = " + b.webAddress + ";resourcetype = " + b.resourceType.string);
        }

    }

    /**
     * Parse an input file and turn it into an Iterator containing bcids
     * @param inputString
     * @throws IOException
     * @throws URISyntaxException
     */
    public inputFileParser(String inputString, int resourceTypeIdentifier) throws IOException, URISyntaxException {
        BufferedReader readbuffer = new BufferedReader(new StringReader(inputString));
        String strRead;
        while ((strRead = readbuffer.readLine()) != null) {
            String sourceID = null, webAddress = null;
            String splitarray[] = strRead.split("\t");
            sourceID = splitarray[0];
            try {
                webAddress = splitarray[1];
                bcidArrayList.add(new bcid(sourceID, new URI(webAddress),resourceTypeIdentifier));
            } catch (ArrayIndexOutOfBoundsException e) {
                // ArrayIndexOutOfBounds here we just assume sourceID
                bcidArrayList.add(new bcid(sourceID,resourceTypeIdentifier));
            }
        }
    }

    /**
     * Return an iterator of bcid objects
     * @return Iterator of BCIDs
     */
    public Iterator iterator() {
        return bcidArrayList.iterator();
    }
}
