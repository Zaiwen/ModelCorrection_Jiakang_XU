package gr.seab.r2rml.CDMMappingRepository.DataStructure;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

@JsonIgnoreProperties(ignoreUnknown = true)
public class EntityType {

    private String entityName;
    private String description;
    private JSONArray sources;
    private JSONObject metadata;

    public String getEntityName() {
        return entityName;
    }

    public String getDescription() {
        return description;
    }

    public JSONArray getSources() {
        return sources;
    }

    public JSONObject getMetadata() {
        return metadata;
    }


    public EntityType(@JsonProperty("entityName") String entityName, @JsonProperty("description") String description,
                      @JsonProperty("sources") JSONArray sources, @JsonProperty("metadata") JSONObject metadata) {
        this.entityName = entityName;
        this.description = description;
        this.sources = sources;
        this.metadata = metadata;
    }

    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        json.put("entityName", entityName);
        json.put("description", description);
        json.put("sources", sources);
        json.put("metadata", metadata);
        return json;
    }
}
