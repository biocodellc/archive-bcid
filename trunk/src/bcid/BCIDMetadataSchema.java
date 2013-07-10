package bcid;

import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.Map;

/**
 * Metadata Schema for Describing an identifier --
 * These are the metadata elements building blocks that we can use to express this identifier either via RDF or HTML
 * and consequently forms the basis of what the "outside" world sees about the identifiers.
 * This class is used by Renderers to structure content.
 */
public class BCIDMetadataSchema {
    // Core Elements for rendering
    public metadataElement about = null;
    public metadataElement resource = null;
    public metadataElement dcCreator = null;
    public metadataElement dcTitle = null;
    public metadataElement dcDate = null;
    public metadataElement dcRights = null;
    public metadataElement dcIsPartOf = null;
    public metadataElement dcSource = null;
    public metadataElement dcMediator = null;
    public metadataElement dcHasVersion = null;
    public metadataElement bscSuffixPassthrough = null;


    public GenericIdentifier identifier;

    public BCIDMetadataSchema() {
    }

    public void BCIDMetadataInit(GenericIdentifier identifier) {
        this.identifier = identifier;
        String ark = null;
        Iterator iterator = identifier.getMetadata().entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry pairs = (Map.Entry) iterator.next();
            String bcidKey = (String) pairs.getKey();
            try {
                if (bcidKey.equalsIgnoreCase("ark")) {
                    ark = pairs.getValue().toString();
                    about = new metadataElement("rdf:Description", identifier.resolverTargetPrefix + ark, "Identifier Resolver");
                } else if (bcidKey.equalsIgnoreCase("what")) {
                    resource = new metadataElement("rdf:type", pairs.getValue().toString(), "What");
                } else if (bcidKey.equalsIgnoreCase("when")) {
                    dcDate = new metadataElement("dc:date", pairs.getValue().toString(), "Date last updated");
                } else if (bcidKey.equalsIgnoreCase("who")) {
                    dcCreator = new metadataElement("dc:creator", pairs.getValue().toString(), "Creator");
                } else if (bcidKey.equalsIgnoreCase("title")) {
                    dcTitle = new metadataElement("dc:title", pairs.getValue().toString(), "Title");
                } else if (bcidKey.equalsIgnoreCase("sourceID")) {
                    dcSource = new metadataElement("dc:source", pairs.getValue().toString(), "Source ID");
                } else if (bcidKey.equalsIgnoreCase("rights")) {
                    dcRights = new metadataElement("dcterms:rights", pairs.getValue().toString(), "Rights");
                } else if (bcidKey.equalsIgnoreCase("doi")) {
                    // Create mapping here for DOI if it only shows the prefix
                    String doi = pairs.getValue().toString().replace("doi:", "http://dx.doi.org/");
                    dcIsPartOf = new metadataElement("dcterms:isPartOf", doi, "Dataset DOI");
                } else if (bcidKey.equalsIgnoreCase("webaddress")) {
                    dcHasVersion = new metadataElement("dcterms:hasVersion", pairs.getValue().toString(), "Has Version (Redirection target)");
                } else if (bcidKey.equalsIgnoreCase("datasetsSuffixPassThrough")) {
                    bscSuffixPassthrough = new metadataElement("bsc:suffixPassthrough", pairs.getValue().toString(), "Supports suffixPassthrough");
                }
            } catch (NullPointerException e) {
                e.getMessage();
            }
        }
        if (ark != null) {
            try {
                dcMediator = new metadataElement("dcterms:mediator", identifier.getMetadataTarget().toString(), "Metadata mediator");
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * A convenience class for holding metadata elements
     */
    public final class metadataElement {
        private String key;
        private String value;
        private String description;

        public metadataElement(String key, String value, String description) {
            this.key = key;
            this.value = value;
            this.description = description;
        }

        public String getKey() {
            return key;
        }

        /**
         * Replace prefixes with fully qualified URL's
         *
         * @return
         */
        public String getFullKey() {
            String tempKey = key;
            tempKey = tempKey.replace("dc:", "http://purl.org/dc/elements/1.1/");
            tempKey = tempKey.replace("dcterms:", "http://purl.org/dc/terms/");
            tempKey = tempKey.replace("rdfs:", "http://www.w3.org/2000/01/rdf-schema#");
            tempKey = tempKey.replace("rdf:", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
            tempKey = tempKey.replace("bsc:", "http://biscicol.org/terms/index.html#");
            return tempKey;
        }

        public String getValue() {
            return value;
        }

        public String setValue(String value) {
            String old = this.value;
            this.value = value;
            return old;
        }

        public String getDescription() {
            return description;
        }
    }
}
