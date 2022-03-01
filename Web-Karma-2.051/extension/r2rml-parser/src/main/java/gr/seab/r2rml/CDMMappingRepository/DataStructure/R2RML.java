package gr.seab.r2rml.CDMMappingRepository.DataStructure;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.json.simple.JSONObject;

@JsonIgnoreProperties(ignoreUnknown = true)
public class R2RML {

    private String mappingKey;//the key to identify the R2RML file, ex. entityName+tableNames
    private String description;
    private String R2RML; // The actual file in base64 format
    private JSONObject metadata;

    public String getMappingKey() {
        return mappingKey;
    }

    public String getDescription() {
        return description;
    }

    public String getR2RML() {
        return R2RML;
    }

    public JSONObject getMetadata() {
        return metadata;
    }

    public R2RML(@JsonProperty("mappingKey") String mappingKey, @JsonProperty("description") String description,
                 @JsonProperty("R2RML") String R2RML, @JsonProperty("metadata") JSONObject metadata) {
        this.mappingKey = mappingKey;
        this.description = description;
        this.R2RML = R2RML;
        this.metadata = metadata;
    }

    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        json.put("mappingKey", mappingKey);
        json.put("description", description);
        json.put("R2RML", R2RML);
        json.put("metadata", metadata);
        return json;
    }
}
