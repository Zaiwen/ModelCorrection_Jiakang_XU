package gr.seab.r2rml.CDMMappingRepository.DataStructure;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.json.simple.JSONObject;


@JsonIgnoreProperties(ignoreUnknown = true)
public class AttriMapping {

    private String sourceName;// the name of the source on the origin side
    private String sourceSchemaName;// entity/index type/table’s name from the source side
    private String sourceAttriName;// attribute/column’s name from the source side
    private String targetName;//the name of the source on the target side
    private String targetSchemaName;// entity/index type/table’s name from the target side
    private String targetAttriName;// attribute/column’s name from the target side
    private String description;
    private JSONObject metadata;

    public void setSourceName(String sourceName) {
        this.sourceName = sourceName;
    }

    public void setSourceSchemaName(String sourceSchemaName) {
        this.sourceSchemaName = sourceSchemaName;
    }

    public void setSourceAttriName(String sourceAttriName) {
        this.sourceAttriName = sourceAttriName;
    }

    public void setTargetName(String targetName) {
        this.targetName = targetName;
    }

    public void setTargetSchemaName(String targetSchemaName) {
        this.targetSchemaName = targetSchemaName;
    }

    public void setTargetAttriName(String targetAttriName) {
        this.targetAttriName = targetAttriName;
    }

    public String getSourceName() {
        return sourceName;
    }

    public String getSourceSchemaName() {
        return sourceSchemaName;
    }

    public String getSourceAttriName() {
        return sourceAttriName;
    }

    public String getTargetName() {
        return targetName;
    }

    public String getTargetSchemaName() {
        return targetSchemaName;
    }

    public String getTargetAttriName() {
        return targetAttriName;
    }

    public String getDescription() {
        return description;
    }

    public JSONObject getMetadata() {
        return metadata;
    }

    public AttriMapping(){};

    public AttriMapping(@JsonProperty("sourceName") String sourceName,
                        @JsonProperty("sourceSchemaName") String sourceSchemaName,
                        @JsonProperty("sourceAttriName") String sourceAttriName,
                        @JsonProperty("targetName") String targetName,
                        @JsonProperty("targetSchemaName") String targetSchemaName,
                        @JsonProperty("targetAttriName") String targetAttriName,
                        @JsonProperty("description") String description,
                        @JsonProperty("metadata") JSONObject metadata) {
        this.sourceName = sourceName;
        this.sourceSchemaName = sourceSchemaName;
        this.sourceAttriName = sourceAttriName;
        this.targetName = targetName;
        this.targetSchemaName = targetSchemaName;
        this.targetAttriName = targetAttriName;
        this.description = description;
        this.metadata = metadata;
    }

    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        json.put("sourceName", sourceName);
        json.put("sourceSchemaName", sourceSchemaName);
        json.put("sourceAttriName", sourceAttriName);
        json.put("targetName", targetName);
        json.put("targetSchemaName", targetSchemaName);
        json.put("targetAttriName", targetAttriName);
        json.put("description", description);
        json.put("metadata", metadata);
        return json;
    }
}
