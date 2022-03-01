package gr.seab.r2rml.util;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.util.*;

import com.fasterxml.jackson.databind.util.JSONPObject;
import gr.seab.r2rml.SimpleRelationshipMatch.RelationshipMatcher;
import gr.seab.r2rml.entities.PredicateObjectMap;
import org.apache.jena.atlas.json.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import gr.seab.r2rml.beans.Main;
import gr.seab.r2rml.beans.Parser;
import gr.seab.r2rml.entities.LogicalTableMapping;
import gr.seab.r2rml.entities.MappingDocument;

import java.net.URL;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

/**
 * This class implements the util methods of R2RML processing.
 *
 * @author Mahfuzul Amin
 * @author Zaiwen Feng
 */
public class R2RMLUtil {
	private static final Logger log = LoggerFactory.getLogger(Main.class);

	private static Properties properties = new Properties();

	public static LinkedList<LogicalTableMapping> parseR2RML(String r2rmlAbsoluteFilePath, String r2rmlPropFileAbsPath, String inputModelPath, String applicationContextLocation) {
		LinkedList<LogicalTableMapping> mappingList = null;
		try {
			log.info("R2RML absolute file path is " + r2rmlAbsoluteFilePath);
			properties.load(new FileInputStream(r2rmlPropFileAbsPath));
			properties.setProperty("mapping.file", r2rmlAbsoluteFilePath);
			properties.setProperty("input.model", inputModelPath);

			ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("app-context.xml");

			Parser parser = (Parser) context.getBean("parser");
			parser.setProperties(properties);

			MappingDocument mappingDocument = parser.parse(true);
			log.info("R2RML document is parsed.");

			context.close();

			mappingList = mappingDocument.getLogicalTableMappings();

		} catch (Exception e) {
			log.error("Error occurred while parsing R2RML file.", e);
		}

		return mappingList;
	}

	/**Parse a R2RML file via the absolute path of r2rml file. Then print out all the mappings between the relational table and CDM*
	 * @author Zaiwen FENG
	 * @param r2rmlAbsoluteFilePath : the absolute file path of R2RML file
	 * @return a list of mappings between the relational tables and CDM
	 * might be useless.
	 * */
	public static LinkedList<LogicalTableMapping> printOutMappings (String r2rmlAbsoluteFilePath) {

		String r2rmlPropFileAbsPath = "/Users/fengz/Project/Web-KarmaV3.2/r2rml-parser/r2rml.properties";

		String inputModelPath = "/Users/fengz/Project/Web-KarmaV3.2/r2rml-parser/dspace/edm-empty.rdf";

		String applicationContext = "/Users/fengz/Project/Web-KarmaV3.2/r2rml-parser/src/main/resources/app-context.xml";

		LinkedList<LogicalTableMapping> logicalTableMappings =  parseR2RML (r2rmlAbsoluteFilePath, r2rmlPropFileAbsPath, inputModelPath, applicationContext);



		System.out.println("Get the logical table mappings...");

		return logicalTableMappings;
	}

	/**Normalize all the mapping rules from a list of logical table mappings*
     * @created Jan 2018
     * @edited 20 Mar 2019
     * */
	public static List<CDMMapping> normalise (LinkedList<LogicalTableMapping> logicalTableMappings) {

		List<CDMMapping> cdmMappings = new ArrayList<CDMMapping>();

		Iterator<LogicalTableMapping> it = logicalTableMappings.iterator();

		while (it.hasNext()) {

			LogicalTableMapping logicalTableMapping = it.next();

			/**Get a list of ontology entity names of CDM in this mapping rule**/
			ArrayList<String> ontologyEntityNames = logicalTableMapping.getSubjectMap().getClassUris();

			/**Normally, there is only one ontology entity name of CDM in a mapping rule**/
			String ontologyEntityName = ontologyEntityNames.get(0);
			ontologyEntityName = ontologyEntityName.substring(ontologyEntityName.lastIndexOf("#")+1);//trim

			/**Get the data source name.**/
			String dataSourceTableName = logicalTableMapping.getDatabseTableName();
			if (dataSourceTableName.indexOf(".") >= 0) {//if the character '.' is present in data source table name.
                dataSourceTableName = dataSourceTableName.substring(0,dataSourceTableName.lastIndexOf("."));//trim
            }

			/**Get a map of predicate object map. One ontology entity might correspond to many mapping rules.**/
			ArrayList<PredicateObjectMap> predicateObjectMaps = logicalTableMapping.getPredicateObjectMaps();

			for (PredicateObjectMap pom : predicateObjectMaps) {

				ArrayList<String> ontologyAttributes = pom.getPredicates();

				/**Normally, we get the first predicate as the ontology attribute**/
				String ontologyAttribute = ontologyAttributes.get(0);
				ontologyAttribute = ontologyAttribute.substring(ontologyAttribute.lastIndexOf("#")+1);//trim

				if (pom.getObjectColumn() != null) {

					String dataSourceAttribute = pom.getObjectColumn();

					/**********Generate a new object of CDM Mapping Rules*************/
					CDMMapping cdmMapping = new CDMMapping();

					UUID uniquekey = UUID.randomUUID();

					cdmMapping.setMappingRuleID(uniquekey.toString()); //set unique key
					cdmMapping.setSourceName("CDM");
					cdmMapping.setSourceSchemaName(ontologyEntityName);
					cdmMapping.setSourceAttriName(ontologyAttribute);
					cdmMapping.setTargetName("CaseManagementSystem");
					cdmMapping.setTargetSchemaName(dataSourceTableName);
					cdmMapping.setTargetAttriName(dataSourceAttribute);
					cdmMapping.setDescription("");
					cdmMapping.setMetadata("");

					cdmMappings.add(cdmMapping);
					/*****************************************************************/

				} else { continue; }

			}
		}
		return cdmMappings;
	}

	/**convert java bean of CDM mapping to JSON object (Forward. i.e. source: CDM, target: a specified data source).*
	 * @param cdmMappingList a list of java bean representing cdm mapping rules.
     * @param owlFilePath path to the CDM ontology
     * @param tableType type of table, general one or object link table?
	 * @return the corresponding Json Object
     * @Created Jan 2018
     * @Revised 20,25,27 Mar 2019
	 * */
	public static JSONObject createJSON_Forward (List<CDMMapping> cdmMappingList, String owlFilePath, List<String> tableType) {

	    tableType.add("GENERAL");//Default

		JSONObject cdmAttriMappingObj_Forward = new JSONObject();

		String ontPrefix = "http://www.semanticweb.org/fengz/ontologies/2019/2/CDM-ontology-20190314#";

		/**1). Create a list of json object (i.e.documentArray) for payload (cdm mapping rule)**/
		JSONArray mappingRulesArray = new JSONArray();

		Iterator<CDMMapping> iterator = cdmMappingList.iterator();
		while (iterator.hasNext()) {

			CDMMapping cdmMapping = iterator.next();

			//create a mapping rule JSON object, corresponds to normalized mapping rules. The source is CDM, the target is a specified data source.
			JSONObject documentJsonObj_Forward = new JSONObject();

			String sourceName_Forward = cdmMapping.getSourceName(); //should be 'CDM'.
			String sourceSchemaName_Forward = cdmMapping.getSourceSchemaName();
			String sourceAttriName_Forward = cdmMapping.getSourceAttriName();
			String targetName_Forward = cdmMapping.getTargetName(); //could be passed from UI. In the current stage, it is 'PROMIS'
			String targetSchemaName_Forward = cdmMapping.getTargetSchemaName();
			String targetAttriName_Forward = cdmMapping.getTargetAttriName();

			documentJsonObj_Forward.put("sourceName", sourceName_Forward);
			documentJsonObj_Forward.put("sourceSchemaName", sourceSchemaName_Forward);//entity
			documentJsonObj_Forward.put("sourceAttriName", sourceAttriName_Forward);//data property
			documentJsonObj_Forward.put("targetName", targetName_Forward);//database name
			documentJsonObj_Forward.put("targetSchemaName", targetSchemaName_Forward);//table name
			documentJsonObj_Forward.put("targetAttriName", targetAttriName_Forward);//attribute name
			documentJsonObj_Forward.put("description", "");//Currently, it is empty
			documentJsonObj_Forward.put("metadata", "");//Currently, it is empty

            //If the target attribute name is named 'relationship_type' in an object link table, we will figure out relationship mapping for it. 2019.03.25
            JSONArray ja = new JSONArray();//create an array of JSON object for holding relationship mappings.
            if (targetAttriName_Forward.equals("relationship_type")) {

                tableType.clear();
                tableType.add("OBJECTLINK");//added 2019.03.27

                //get the multiple relationship types from Data Source
                List<String> dbRelationTypeList = RelationshipMatcher.extractRTFromOBJTable(targetName_Forward,targetSchemaName_Forward,targetAttriName_Forward);

                //get the multiple relationship types from CDM ontology
                List<String> ontRelationTypeList = RelationshipMatcher.extractEnumeratedDatatype(owlFilePath, sourceAttriName_Forward);

                //get the mappings
                Map<String,String> relationshipMap = RelationshipMatcher.getRTMappings(ontRelationTypeList,dbRelationTypeList);

				Iterator it = relationshipMap.entrySet().iterator();
				while (it.hasNext()) {
					Map.Entry pair = (Map.Entry) it.next();
					String ontRelation = (String) pair.getKey();
					String dbRelation = (String) pair.getValue();

					JSONObject rtMappingJsonObj = new JSONObject();//Create a new JSON Object. The source is CDM, the target is a specified data source.
                    rtMappingJsonObj.put("composite_entity_RT",ontRelation);
					rtMappingJsonObj.put("object_link_table_RT",dbRelation);
					ja.add(rtMappingJsonObj);
				}
                documentJsonObj_Forward.put("relationshipMap", ja);
            }

			mappingRulesArray.add(documentJsonObj_Forward);
		}

		/**(2). Create 'filter' json object.**/
		JSONObject filterJsonObj_Forward = new JSONObject();
		filterJsonObj_Forward.put("documentArray", mappingRulesArray);

		/**3). Create 'query' json object, and add it to 'cdmAttriMappingObj'.**/
		JSONObject queryJsonObj_Forward = new JSONObject();
		queryJsonObj_Forward.put("filter", filterJsonObj_Forward);
		cdmAttriMappingObj_Forward.put("query", queryJsonObj_Forward);

		/**4). Create 'control' json object, and add it to 'cdmAttriMappingObj'.**/
		JSONObject controlJsonObj_Forward = new JSONObject();
		controlJsonObj_Forward.put("operation", "insertAttriMapping");
		List<String> sourceList = new ArrayList<String>();
		sourceList.add("CDM");
		controlJsonObj_Forward.put("sources", sourceList);
		cdmAttriMappingObj_Forward.put("control", controlJsonObj_Forward);

		/**5). Create 'credentials' json object, and add it to 'cdmAttriMappingObj'.**/
		JSONObject userAccount = new JSONObject();
		userAccount.put("username", "MRuser");
		userAccount.put("password", "unisa");
		JSONObject MR = new JSONObject();
		MR.put("MR",userAccount);
		List<JSONObject> credentialList = new ArrayList<JSONObject>();
		credentialList.add(MR);
		JSONObject credentialsJsonObj = new JSONObject();
		credentialsJsonObj.put("credentiallist", credentialList);

		cdmAttriMappingObj_Forward.put("credentials", credentialsJsonObj);
		return cdmAttriMappingObj_Forward;
	}

//	/**convert java bean of CDM mapping to JSON object (Reverse. i.e. source: a specified data source, target: CDM).*
//	 * @param cdmMappingList a list of java bean representing cdm mapping rules.
//	 * @return the corresponding JSON Object
//	 * */
//	public static JSONObject createJSON_Reverse (List<CDMMapping> cdmMappingList) {
//
//		JSONObject cdmAttriMappingObj_Reverse = new JSONObject();
//
//		/**1). Create a list of json object (i.e.documentArray) for payload (cdm mapping rule)**/
//		List<JSONObject> mappingRulesList_Reverse = new ArrayList<JSONObject>();
//
//		Iterator<CDMMapping> iterator = cdmMappingList.iterator();
//		while (iterator.hasNext()) {
//
//			CDMMapping cdmMapping = iterator.next();
//
//			//create another JSON object that corresponds to normalized mapping rules. However, the source is a specified data source, the target is CDM.
//			JSONObject documentJsonObj_Reverse = new JSONObject();
//
//			String sourceName_Reverse = cdmMapping.getTargetName(); //should be 'promis', etc..
//			String sourceSchemaName_Reverse = cdmMapping.getTargetSchemaName();
//			String sourceAttriName_Reverse = cdmMapping.getTargetAttriName();
//			String targetName_Reverse = cdmMapping.getSourceName(); //it is 'CDM'
//			String targetSchemaName_Reverse = cdmMapping.getSourceSchemaName();
//			String targetAttriName_Reverse = cdmMapping.getSourceAttriName();
//			String description_Reverse = cdmMapping.getDescription();
//			String metadata_Reverse = cdmMapping.getMetadata();
//
//			documentJsonObj_Reverse.put("sourceName", sourceName_Reverse);
//			documentJsonObj_Reverse.put("sourceSchemaName", sourceSchemaName_Reverse);
//			documentJsonObj_Reverse.put("sourceAttriName", sourceAttriName_Reverse);
//			documentJsonObj_Reverse.put("targetName", targetName_Reverse);
//			documentJsonObj_Reverse.put("targetSchemaName", targetSchemaName_Reverse);
//			documentJsonObj_Reverse.put("targetAttriName", targetAttriName_Reverse);
//			documentJsonObj_Reverse.put("description", "");//Currently, it is empty.
//			documentJsonObj_Reverse.put("metadata", "");//Currently, it is empty.
//
//			mappingRulesList_Reverse.add(documentJsonObj_Reverse);
//
//		}
//
//		/**(2). Create 'filter' json object.**/
//		JSONObject filterJsonObj_Reverse = new JSONObject();
//		filterJsonObj_Reverse.put("documentArray", mappingRulesList_Reverse);
//
//		/**3). Create 'query' json object, and add it to 'cdmAttriMappingObj'.**/
//		JSONObject queryJsonObj_Reverse = new JSONObject();
//		queryJsonObj_Reverse.put("filter", filterJsonObj_Reverse);
//		cdmAttriMappingObj_Reverse.put("query", queryJsonObj_Reverse);
//
//		/**4). Create 'control' json object, and add it to 'cdmAttriMappingObj'.**/
//		JSONObject controlJsonObj_Reverse = new JSONObject();
//		controlJsonObj_Reverse.put("operation", "insertAttriMapping");
//		List<String> sourceList = new ArrayList<String>();
//		sourceList.add("promis");
//		controlJsonObj_Reverse.put("sources", sourceList);
//		cdmAttriMappingObj_Reverse.put("control", controlJsonObj_Reverse);
//
//		/**5). Create 'credentials' json object, and add it to 'cdmAttriMappingObj'.**/
//		JSONObject userAccount = new JSONObject();
//		userAccount.put("username", "MRuser");
//		userAccount.put("password", "unisa");
//		JSONObject MR = new JSONObject();
//		MR.put("MR",userAccount);
//		List<JSONObject> credentialList = new ArrayList<JSONObject>();
//		credentialList.add(MR);
//		JSONObject credentialsJsonObj = new JSONObject();
//		credentialsJsonObj.put("credentiallist", credentialList);
//
//		cdmAttriMappingObj_Reverse.put("credentials", credentialsJsonObj);
//
//		return cdmAttriMappingObj_Reverse;
//	}

	/**POST a string to a defined REST API*
	 * @comments: might be useless for demo. 2019.03.19
	 *
	 * */
	public static int netClientPost (String APIURL, String content) {


		int responseCode = 0 ;
		try {

			URL url = new URL(APIURL);
			HttpURLConnection conn = (HttpURLConnection)url.openConnection();
			conn.setDoOutput(true);
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Content-Type", "application/json");

			OutputStream os = conn.getOutputStream();
			os.write(content.getBytes());
			os.flush();

			responseCode = conn.getResponseCode();

			if ((responseCode != HttpURLConnection.HTTP_CREATED) && (responseCode != HttpURLConnection.HTTP_OK)  ) {

				throw new RuntimeException("Failed : HTTP error code: " + conn.getResponseCode());

			} else {

				System.out.println("The HTTP code is: " + responseCode);
			}

			BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));

			String output;
			System.out.println("Output from Server ... \n");
			while ((output = br.readLine()) != null) {
				System.out.println(output);
			}
			conn.disconnect();

		} catch (MalformedURLException e) {

			e.printStackTrace();

		} catch (IOException e) {

			e.printStackTrace();
		}
		return responseCode;
	}

}
