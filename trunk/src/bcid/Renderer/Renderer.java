package bcid.Renderer;

import bcid.GenericIdentifier;

/**
 * Abstract class Renderer implements the visitor methods
 * and controls all renderer subClasses for rendering bcids
 */
public abstract class Renderer implements RendererInterface {
    protected StringBuilder outputSB;

    /**
     * render an Identifier
     *
     * @param identifier
     * @return
     */
    public String renderIdentifier(GenericIdentifier identifier)  {
        outputSB = new StringBuilder();

        if (validIdentifier(identifier)) {
            enter(identifier);
            printMetadata(identifier);
            leave(identifier);
            return outputSB.toString();
        }else {
            return outputSB.toString();
        }
    }


}
