package bcid;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * The Identifier class encapsulates all types of identifiers that we deal with in the BCID system
 */
public abstract class GenericIdentifier implements GenericIdentifierInterface {

    /* The identifier itself */
    public URI identifier;
     /* All identifiers ave the Attribution 3.0 Unported CC License Applied */
    public final String rights = "http://creativecommons.org/licenses/by/3.0/";

    public String resolverTargetPrefix = "http://biscicol.org/bcid/";
    public String resolverMetadataPrefix = "http://biscicol.org/bcid/id/";


    /**
     * The resolution target for this identifier
     *
     * @return
     * @throws URISyntaxException
     */
    public URI getResolutionTarget() throws URISyntaxException {
        return new URI(resolverTargetPrefix);
    }

    /**
     * The metadata target for this identifier
     *
     * @return
     * @throws URISyntaxException
     */
    public URI getMetadataTarget() throws URISyntaxException {
        return new URI(resolverMetadataPrefix);
    }
}
