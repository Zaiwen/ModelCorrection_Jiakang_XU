package gr.seab.r2rml.util;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**This class describes the attribute mapping rules between the target data sources (e.g. PROMIS) and the CDM (source).
 * 6 Feb 2018.
 * @Edited on 7 Feb 2018.*
 * @revised on 25 Mar 2019
 * @author Zaiwen Feng
 * */
public class CDMMapping {

    private String mappingRuleID = "";// a unique hashcode id
    private String sourceName = "";
    private String sourceSchemaName = ""; //ontology concept.
    private String sourceAttriName = ""; //attribute of an ontology concept.
    private String targetName = ""; //database name. Could be passed from UI.
    private String targetSchemaName = ""; //name of relational table
    private String targetAttriName = ""; //name of column name of the relational table
    //private Map<String,String> relationshipMap = new HashMap<>();//Key: relationship between two entities, Value:   Name of the value of a column of a relational table. Specially, it's for relationship type in the object link table
    private String description = "";
    private String metadata = "";

    public String getMappingRuleID () {

        return this.mappingRuleID;
    }

    public void setMappingRuleID (String mappingRuleID) {

        this.mappingRuleID = mappingRuleID;
    }

    public String getSourceName () {

        return this.sourceName;
    }

    public void setSourceName (String sourceName) {

        this.sourceName = sourceName;
    }

    public String getSourceSchemaName () {

        return this.sourceSchemaName;
    }

    public void setSourceSchemaName (String sourceSchemaName) {

        this.sourceSchemaName = sourceSchemaName;
    }

    public String getSourceAttriName () {

        return this.sourceAttriName;
    }

    public void setSourceAttriName (String sourceAttriName) {

        this.sourceAttriName = sourceAttriName;
    }


    public String getTargetName () {

        return targetName;
    }

    public void setTargetName (String targetName) {

        this.targetName = targetName;
    }


    public String getTargetSchemaName () {

        return this.targetSchemaName;
    }

    public void setTargetSchemaName (String targetSchemaName) {

        this.targetSchemaName = targetSchemaName;
    }

    public String getTargetAttriName() {

        return this.targetAttriName;
    }

    public void setTargetAttriName (String targetAttriName) {

        this.targetAttriName = targetAttriName;
    }


//    /**Add a relationship mapping (For the Object link table specially)*
//     * @created 25 Mar 2019
//     * @param ontRelationship one of relationships between two entities
//     * @param dbRelationship the corresponding relation in the dbms
//     * */
//    public void addRelationshipMap (String ontRelationship, String dbRelationship) {
//        this.relationshipMap.put(ontRelationship,dbRelationship);
//    }
//
//    /**Get all the relationship mappings (For the Object link table specially)*
//     * @created 25 Mar 2019
//     *@return a map of relationships
//     * */
//    public Map<String, String> getRelationshipMap () {
//        return this.relationshipMap;
//    }

    public String getDescription () {

        return this.description;
    }

    public void setDescription (String description) {

        this.description = description;
    }

    public String getMetadata () {

        return this.metadata;
    }

    public void setMetadata (String metadata) {

        this.metadata = metadata;
    }
}
