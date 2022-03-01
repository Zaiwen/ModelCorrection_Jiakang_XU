package gr.seab.r2rml.CDMMappingRepository.DataStructure;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.json.simple.JSONObject;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SchemaMapping {
    private String sourceName;// the name of the source on the origin side
    private String sourceSchemaName;// entity/index type/table’s name from the source side
    private String targetName;//the name of the source on the target side
    private String targetSchemaName;// entity/index type/table’s name from the target side
    private String description;
    private JSONObject metadata;

    public String getSourceName() {
        return sourceName;
    }
    public void setSourceName(String sourceName) {
        this.sourceName = sourceName;
    }
    public String getSourceSchemaName() {
        return sourceSchemaName;
    }
    public void setSourceSchemaName(String sourceSchemaName) {
        this.sourceSchemaName = sourceSchemaName;
    }
    public String getTargetName() {
        return targetName;
    }
    public void setTargetName(String targetName) {
        this.targetName = targetName;
    }
    public String getTargetSchemaName() {
        return targetSchemaName;
    }
    public void setTargetSchemaName(String targetSchemaName) {
        this.targetSchemaName = targetSchemaName;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public JSONObject getMetadata() {
        return metadata;
    }
    public void setMetadata(JSONObject metadata) {
        this.metadata = metadata;
    }

    public SchemaMapping(@JsonProperty("sourceName") String sourceName,
                        @JsonProperty("sourceSchemaName") String sourceSchemaName,
                        @JsonProperty("targetName") String targetName,
                        @JsonProperty("targetSchemaName") String targetSchemaName,
                        @JsonProperty("description") String description,
                        @JsonProperty("metadata") JSONObject metadata) {
        this.sourceName = sourceName;
        this.sourceSchemaName = sourceSchemaName;
        this.targetName = targetName;
        this.targetSchemaName = targetSchemaName;
        this.description = description;
        this.metadata = metadata;
    }

    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        json.put("sourceName", sourceName);
        json.put("sourceSchemaName", sourceSchemaName);
        json.put("targetName", targetName);
        json.put("targetSchemaName", targetSchemaName);
        json.put("description", description);
        json.put("metadata", metadata);
        return json;
    }
}
