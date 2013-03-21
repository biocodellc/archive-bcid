package bcid.testData;

import java.net.URISyntaxException;
import java.util.ArrayList;

/**
 * Construct a sample dataset in an ArrayList to use for testing
 */
public class testDataSet extends ArrayList {
    public testDataSet() {
        try {
            this.add(new bcid.testData.testDataRow("http://biocode.berkeley.edu/specimens/MBIO1000","UMC:Molluscs:9592", bcid.ResourceTypes.PHYSICALOBJECT));
            this.add(new bcid.testData.testDataRow("http://biocode.berkeley.edu/specimens/MBIO1400","UMC:Molluscs:18544   ", bcid.ResourceTypes.PHYSICALOBJECT));

            //this.add(new testDataRow("http://biocode.berkeley.edu/specimens/MBIO1000","MBIO1000", ResourceTypes.PHYSICALOBJECT));
            //this.add(new testDataRow("http://biocode.berkeley.edu/specimens/MBIO1400","MBIO1400", ResourceTypes.PHYSICALOBJECT));
            //this.add(new bcid.testData.testDataRow("http://biocode.berkeley.edu/events/66","CM91", bcid.ResourceTypes.EVENT));
            //this.add(new testDataRow("http://biocode.berkeley.edu/events/88","CM125-126", ResourceTypes.EVENT));
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }
}
