package bcid;

import java.lang.String;

/**
 * Components used to describe individual resource types.
 */
public class ResourceType {
    public  String string;
    public  String uri;
    public  String description;
    public Integer resourceType;

    /**
     *
     * @param string String is the short description (e.g. PhysicalObject, Image, Text)
     * @param uri URI represents the URI that describes this particular resource.
     * @param description Expanded text describing what this refers to.
     */
    public ResourceType(int resourceType, String string, String uri, String description) {
        this.resourceType = resourceType;
        this.string = string;
        this.uri = uri;
        this.description = description;
    }

    /**
     * Empty resourceType
     */
    public ResourceType(int resourceType) {
            this.resourceType = resourceType;
        this.string = "spacer";
        this.uri = null;
        this.description = null;
    }
}
