package gr.seab.r2rml.CDMMappingRepository.DataStructure;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.json.simple.JSONObject;


@JsonIgnoreProperties(ignoreUnknown = true)
public class ValueMapping {

    private String sourceName;// the name of the source on the origin side
    private String sourceSchemaName;// entity/index type/table’s name from the source side
    private String sourceAttriName;// attribute/column’s name from the source side
    private Object sourceValue;
    private String targetName;//the name of the source on the target side
    private String targetSchemaName;// entity/index type/table’s name from the target side
    private String targetAttriName;// attribute/column’s name from the target side
    private Object targetValue;
    private String description;
    private JSONObject metadata;


    public String getSourceName() {
        return sourceName;
    }

    public String getSourceSchemaName() {
        return sourceSchemaName;
    }

    public String getSourceAttriName() {
        return sourceAttriName;
    }

    public Object getSourceValue() {
        return sourceValue;
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

    public Object getTargetValue() {
        return targetValue;
    }

    public String getDescription() {
        return description;
    }

    public JSONObject getMetadata() {
        return metadata;
    }

    public ValueMapping(@JsonProperty("sourceName") String sourceName,
                        @JsonProperty("sourceSchemaName") String sourceSchemaName,
                        @JsonProperty("sourceAttriName") String sourceAttriName,
                        @JsonProperty("sourceValue") Object sourceValue,
                        @JsonProperty("targetName") String targetName,
                        @JsonProperty("targetSchemaName") String targetSchemaName,
                        @JsonProperty("targetAttriName") String targetAttriName,
                        @JsonProperty("targetValue") Object targetValue,
                        @JsonProperty("description") String description,
                        @JsonProperty("metadata") JSONObject metadata) {
        this.sourceName = sourceName;
        this.sourceSchemaName = sourceSchemaName;
        this.sourceAttriName = sourceAttriName;
        this.sourceValue = sourceValue;
        this.targetName = targetName;
        this.targetSchemaName = targetSchemaName;
        this.targetAttriName = targetAttriName;
        this.targetValue = targetValue;
        this.description = description;
        this.metadata = metadata;
    }

    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        json.put("sourceName", sourceName);
        json.put("sourceSchemaName", sourceSchemaName);
        json.put("sourceAttriName", sourceAttriName);
        json.put("sourceValue", sourceValue);
        json.put("targetName", targetName);
        json.put("targetSchemaName", targetSchemaName);
        json.put("targetAttriName", targetAttriName);
        json.put("targetValue", targetValue);
        json.put("description", description);
        json.put("metadata", metadata);
        return json;
    }
}

