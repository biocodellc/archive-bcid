package bcid.Renderer;

import bcid.GenericIdentifier;

import java.util.Iterator;
import java.util.Map;

/**
 * HTMLTableRenderer renders object results as HTMLTable
 */
public class HTMLTableRenderer extends Renderer {
    String ark = null;
    StringBuilder runningSB = new StringBuilder();

    public void enter(GenericIdentifier identifier) {
        runningSB.append("<table>");
    }

    public void printMetadata(GenericIdentifier identifier) {
        Iterator iterator = identifier.getMetadata().entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry pairs = (Map.Entry) iterator.next();

            if (pairs.getKey().toString().equalsIgnoreCase("datasetsPrefix")) {
                ark = pairs.getValue().toString();
            }
            runningSB.append("<tr><td>" + pairs.getKey() + "</td><td>" + pairs.getValue() + "</td></tr>\n");
        }
    }

    public void leave(GenericIdentifier identifier) {
        runningSB.append("</table>");
        if (ark != null) {
            outputSB.append("<h2>" + ark + "</h2>");
        }
        outputSB.append(runningSB);
    }

    public boolean validIdentifier(GenericIdentifier identifier) {
        if (identifier == null) {
            outputSB.append("<h2>Unable to find identifier</h2>");
            return false;
        } else {
            return true;
        }

    }
}
