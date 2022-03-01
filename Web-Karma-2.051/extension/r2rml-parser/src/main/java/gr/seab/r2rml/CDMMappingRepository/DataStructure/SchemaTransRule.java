package gr.seab.r2rml.CDMMappingRepository.DataStructure;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.json.simple.JSONObject;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SchemaTransRule {

    private String sourceName;// the name of the source on the origin side
    private String targetName;//the name of the source on the target side
    private String roleName;// the name of the role
    private String description;
    private String content;// the role itself in the format of a string
    private JSONObject metadata;

    public String getSourceName() {
        return sourceName;
    }

    public String getTargetName() {
        return targetName;
    }

    public String getRoleName() {
        return roleName;
    }

    public String getDescription() {
        return description;
    }

    public String getContent() {
        return content;
    }

    public JSONObject getMetadata() {
        return metadata;
    }

    public SchemaTransRule(@JsonProperty("sourceName") String sourceName,
                           @JsonProperty("targetName") String targetName,
                           @JsonProperty("roleName") String roleName, @JsonProperty("description") String description,
                           @JsonProperty("content") String content, @JsonProperty("metadata") JSONObject metadata) {
        this.sourceName = sourceName;
        this.description = description;
        this.targetName = targetName;
        this.roleName = roleName;
        this.content = content;
        this.metadata = metadata;
    }

    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        json.put("sourceName", sourceName);
        json.put("description", description);
        json.put("targetName", targetName);
        json.put("roleName", roleName);
        json.put("content", content);
        json.put("metadata", metadata);
        return json;
    }

}
