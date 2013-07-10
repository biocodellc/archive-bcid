package bcid.Renderer;

import bcid.GenericIdentifier;

import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.Map;

/**
 * jsonRenderer renders objects as JSON
 */
public class RedirectRenderer extends TextRenderer {

    public void enter(GenericIdentifier identifier) {
    }

    public void printMetadata(GenericIdentifier identifier) {
        Iterator iterator = identifier.getMetadata().entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry pairs = (Map.Entry) iterator.next();
            outputSB.append("\"" + pairs.getKey() + "\":\"" + pairs.getValue() + "\"");
            if (iterator.hasNext()) {
                outputSB.append(",");
            }
        }
        try {
            outputSB.append(identifier.getResolutionTarget());
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public void leave(GenericIdentifier identifier) {
    }

    public boolean validIdentifier(GenericIdentifier identifier)  {
        return true;
    }

}
