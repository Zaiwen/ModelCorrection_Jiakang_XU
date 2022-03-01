package gr.seab.r2rml.CDMMappingRepository.DataStructure;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Source {

    private String sourceName;
    private String description;
    private String sourceType; // ex., elasticsearch, postgresql, mongodb.
    private JSONObject accessInfo; // host, port, protocol, url, etc
    private JSONArray entityTypes; // entityTypes that is maintained by the source
    private JSONObject metadata;

    public String getSourceName() {
        return sourceName;
    }

    public String getDescription() {
        return description;
    }

    public String getSourceType() {
        return sourceType;
    }

    public JSONObject getAccessInfo() {
        return accessInfo;
    }

    public JSONArray getEntityTypes() {
        return entityTypes;
    }

    public JSONObject getMetadata() {
        return metadata;
    }

    public Source(@JsonProperty("sourceName") String sourceName, @JsonProperty("description") String description,
                  @JsonProperty("sourceType") String sourceType, @JsonProperty("accessInfo") JSONObject accessInfo,
                  @JsonProperty("entityTypes") JSONArray entityTypes, @JsonProperty("metadata") JSONObject metadata) {
        this.sourceName = sourceName;
        this.description = description;
        this.sourceType = sourceType;
        this.accessInfo = accessInfo;
        this.entityTypes = entityTypes;
        this.metadata = metadata;
    }

    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        json.put("sourceName", sourceName);
        json.put("description", description);
        json.put("sourceType", sourceType);
        json.put("accessInfo", accessInfo);
        json.put("entityTypes", entityTypes);
        json.put("metadata", metadata);
        return json;
    }
}
