package bcid;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * ResourceTypes class is a controlled list of available ResourceTypes.  This is built into code since these
 * types have rarely changed and in fact are central to so many coding operations and we don't want to rely on
 * instance level configuration control.
 * <p/>
 * ResourceTypes draw from  Dublin Core DCMI Resource Types, Dublin Darwin Core Classes, Darwin Core, Information Artifact Ontology, and ENVO
 * <p/>
 */
public class ResourceTypes {

    static ArrayList list = new ArrayList();

    // DCMI METADATA
    public static int DATASET = 1;
    public static int EVENT = 2;
    public static int IMAGE = 3;
    public static int MOVINGIMAGE = 4;
    public static int PHYSICALOBJECT = 5;
    public static int SERVICE = 6;
    public static int SOUND = 7;
    public static int TEXT = 8;

    // Dublin Core Classes
    public static int LOCATION = 9;
    public static int AGENT = 10;

    // IAO
    public static int INFORMATIONCONTENTENTITY = 11;

    // DARWIN CORE
    public static int OCCURRENCE = 12;
    public static int IDENTIFICATION = 13;
    public static int TAXON = 14;
    public static int RESOURCERELATIONSHIP = 15;
    public static int MEASUREMENTORFACT = 16;
    public static int GEOLOGICALCONTEXT = 17;

    // ENVO
    public static int BIOME = 18;
    public static int FEATURE = 19;
    public static int MATERIAL = 20;

    // Catch All
    public static int RESOURCE = 21;

    public ResourceTypes() {
        ResourceType type = null;
        // DCMI Resource Types
        list.add(new ResourceType(this.DATASET, "Dataset", "http://purl.org/dc/dcmitype/Dataset", "Data encoded in a defined structure."));
        list.add(new ResourceType(this.EVENT, "Event", "http://purl.org/dc/dcmitype/Event", "A non-persistent, time-based occurrence."));
        list.add(new ResourceType(this.IMAGE, "Image", "http://purl.org/dc/dcmitype/Image", "A visual representation other than text."));
        list.add(new ResourceType(this.MOVINGIMAGE, "MovingImage", "http://purl.org/dc/dcmitype/MovingImage", "A series of visual representations imparting an impression of motion when shown in succession."));
        list.add(new ResourceType(this.PHYSICALOBJECT, "PhysicalObject", "http://purl.org/dc/dcmitype/PhysicalObject", "An inanimate, three-dimensional object or substance."));
        list.add(new ResourceType(this.SERVICE, "Service", "http://purl.org/dc/dcmitype/Service", "A system that provides one or more functions."));
        list.add(new ResourceType(this.SOUND, "Sound", "http://purl.org/dc/dcmitype/Sound", "A resource primarily intended to be heard."));
        list.add(new ResourceType(this.TEXT, "Text", "http://purl.org/dc/dcmitype/Text", "A resource consisting primarily of words for reading."));
        // Dublin Core Classes
        list.add(new ResourceType(this.LOCATION, "Location", "http://purl.org/dc/terms/Location", "A spatial region or named place."));
        list.add(new ResourceType(this.AGENT, "Agent", "http://purl.org/dc/terms/Agent", "A resource that acts or has the power to act."));
        // IAO
        list.add(new ResourceType(this.INFORMATIONCONTENTENTITY, "InformationContentEntity", "http://purl.obolibrary.org/obo/IAO_0000030", "Examples of information content entites include journal articles, data, graphical layouts, and graphs."));
        // DARWIN CORE
        list.add(new ResourceType(this.OCCURRENCE, "Occurrence", "http://rs.tdwg.org/dwc/terms/Occurrence", "The category of information pertaining to evidence of an occurrence in nature, in a collection, or in a dataset (specimen, observation, etc.)"));
        list.add(new ResourceType(this.IDENTIFICATION, "Identification", "http://rs.tdwg.org/dwc/terms/Identification", "The category of information pertaining to taxonomic determinations (the assignment of a scientific name)."));
        list.add(new ResourceType(this.TAXON, "Taxon", "http://rs.tdwg.org/dwc/terms/Taxon", "The category of information pertaining to taxonomic names, taxon name usages, or taxon concepts."));
        list.add(new ResourceType(this.RESOURCERELATIONSHIP, "ResourceRelationship", "http://rs.tdwg.org/dwc/terms/ResourceRelationship", "The category of information pertaining to relationships between resources (instances of data records, such as Occurrences, Taxa, Locations, Events)."));
        list.add(new ResourceType(this.MEASUREMENTORFACT, "MeasurementOrFact", "http://rs.tdwg.org/dwc/terms/MeasurementOrFact", "The category of information pertaining to measurements, facts, characteristics, or assertions about a resource (instance of data record, such as Occurrence, Taxon, Location, Event)."));
        list.add(new ResourceType(this.GEOLOGICALCONTEXT, "GeologicalContext", "http://rs.tdwg.org/dwc/terms/GeologicalContext", "The category of information pertaining to a location within a geological context, such as stratigraphy."));
        // ENVO
        list.add(new ResourceType(this.BIOME, "Biome", "http://purl.obolibrary.org/obo/ENVO_00000428", "A major class of ecologically similar communities of plants, animals, and other organisms."));
        list.add(new ResourceType(this.FEATURE, "Feature", "http://purl.obolibrary.org/obo/ENVO_00002297", "An environmental feature is a prominent or distinctive aspect, quality, or characteristic of a given biome."));
        list.add(new ResourceType(this.MATERIAL, "Material", "http://purl.obolibrary.org/obo/ENVO_00010483", "Material in or on which organisms may live."));
        // Catch All
        list.add(new ResourceType(this.RESOURCE, "Resource", "http://www.w3.org/2000/01/rdf-schema#Resource", "Resource is the class of everything"));
    }

    /**
     * Return a ResourceType given an Integer
     *
     * @param typeIncrement
     * @return ResourceType
     */
    public static ResourceType get(int typeIncrement) {
        return (ResourceType) list.get(typeIncrement - 1);
    }

    public String getAllAsJSON() {
        String json = "[{";
        Iterator it = list.iterator();
        int count = 0;
        while (it.hasNext()) {
            ResourceType rt = (ResourceType) it.next();

            if (count != 0) json += ",";
            // DON'T RETURN dataset as an option here.  JSON is used to select ResourceTypes for Datasets themselves,
            // so we don't want to allow users to choose this option in any interface.
            //if (!rt.string.equalsIgnoreCase("Dataset")) {
                json += "\"" + rt.resourceType + "\":\"" + rt.string + "\"";
                count++;
            //}
        }
        json += "}]";
        return json;
    }

    public static void main(String args[]) {
        ResourceTypes rts = new ResourceTypes();
        System.out.println(rts.getAllAsJSON());
    }
}
