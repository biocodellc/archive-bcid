package bcid;

import java.net.URI;

/**
 * the Identifier class encapsulates all types of identifiers.
 * It implements Iterable, which is meant to iterate through any metadata fields associated with
 * a particular identifier.  It is up to each identifier class to assign metadata fields
 */
public abstract class GenericIdentifier implements GenericIdentifierInterface {

    /**
     * The identifier itself
     */
    protected URI identifier;

}
