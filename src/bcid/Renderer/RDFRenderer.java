package bcid.Renderer;

import bcid.GenericIdentifier;

import java.util.Iterator;
import java.util.Map;

/**
 * textRenderer renders object results as Text
 */
public class RDFRenderer extends Renderer {
    String about;
    StringBuilder properties;
    public void enter(GenericIdentifier identifier) {
        outputSB.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"\n" +
                "                 xmlns:rdfs=\"http://www.w3.org/2000/01/rdf-schema#\"\n" +
                "                 xmlns:dcterms=\"http://purl.org/dc/terms/\"\n" +
                "                 xmlns:dwc=\"http://rs.tdwg.org/dwc/terms/\"\n" +
                "                 xmlns:dsw=\"http://purl.org/dsw/\"\n" +
                "                 xmlns:mrtg=\"http://rs.tdwg.org/mrtg/trunk/RDF/mrtg.n3#\"\n" +
                "                 xmlns:xmp=\"http://ns.adobe.com/xap/1.0/\"\n" +
                "                 xmlns:foaf=\"http://xmlns.com/foaf/0.1/\"\n" +
                "                 >");
    }

    public void printMetadata(GenericIdentifier identifier) {
        Iterator iterator = identifier.getMetadata().entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry pairs = (Map.Entry) iterator.next();
            String bcidKey = (String)pairs.getKey();
            if (bcidKey.equalsIgnoreCase("ark")) {
                about = "<rdf:Description rdf:about=\"" + pairs.getValue() + "\">\n";
            }  else if (bcidKey.equalsIgnoreCase("what"))
                properties.append("<rdf:type rdf:resource=\"" + pairs.getValue() + "\" />");
            else {
                properties.append("<prefix:property>" + pairs.getValue()+ "</prefix:collectionCode>");
            }
        }
    }

    public void leave(GenericIdentifier identifier) {
        outputSB.append(about);
        outputSB.append(properties.toString());
        outputSB.append("</rdf:Description>");
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
