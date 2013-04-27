package bcid.Renderer;

import bcid.GenericIdentifier;

/**
 * rencererInterface defines an interface for working with rendered identifiers .
 * Can enter an object, printMetadata, and leave.
 * These methods are meant to populate a class level variable in the
 * Renderer
 */
public interface RendererInterface {

    /**
     * Enter the genericIdentifier and render any information before looking at metadata
     * @param identifier
     */
    public void enter(GenericIdentifier identifier);

    /**
     * Print an identifier's metadata
     * @param identifier
     */
    public void printMetadata(GenericIdentifier identifier);

    /**
     * Leave the object and print any relevant closing information
     * @param identifier
     */
    public void leave(GenericIdentifier identifier);

    /**
     * Need to always check the identifier and provide a consistent method for returning
     * error messages if it is bad.
     * @param identifier
     */
    public boolean validIdentifier(GenericIdentifier identifier);

}
