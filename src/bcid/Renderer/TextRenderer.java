package bcid.Renderer;

import bcid.GenericIdentifier;

import java.util.Iterator;
import java.util.Map;

/**
 * textRenderer renders object results as Text
 */
public class TextRenderer extends Renderer {

    public void enter(GenericIdentifier identifier) {
        outputSB.append("***" + identifier.getClass().getSimpleName() + "***\n");
    }

    public void printMetadata(GenericIdentifier identifier) {
        Iterator iterator = identifier.getMetadata().entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry pairs = (Map.Entry) iterator.next();
            outputSB.append(pairs.getKey() + "=" + pairs.getValue() + "\n");
        }
    }

    public void leave(GenericIdentifier identifier) {
    }

    public boolean validIdentifier(GenericIdentifier identifier) {
        if (identifier == null) {
            outputSB.append("identifier is null");
            return false;
        } else {
            return true;
        }

    }
}
