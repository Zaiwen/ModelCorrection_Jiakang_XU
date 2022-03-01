package gr.seab.r2rml.util;

import com.fasterxml.jackson.databind.util.JSONPObject;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import gr.seab.r2rml.CDMMappingRepository.MongoFunctions;
import gr.seab.r2rml.entities.LogicalTableMapping;
import gr.seab.r2rml.entities.PredicateObjectMap;
import org.bson.Document;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import java.util.LinkedList;

public class TestR2RMLParser
{
    public static void main (String args[]) {

//        String r2rmlAbsoluteFilePath = "/Users/fengz/Documents/Data_Value_Mapping/R2RML/r2rml_example(17Jan18)/NDS3.csv-model.ttl";
//
//        String r2rmlPropFileAbsPath = "/Users/fengz/Documents/Data_Value_Mapping/R2RML/r2rml_example(17Jan18)/NDS3.csv-model.ttl";
//
//        String inputModelPath = "/Users/fengz/Documents/Data_Value_Mapping/Karma(from_9_Nov_2016)/RDF-Graph@16Jan18/NDS3.csv";

//        String r2rmlAbsoluteFilePath = "/Users/fengz/Project/Web-KarmaV3.2/r2rml-parser/src/main/resources/NDS3.csv-model.ttl";

        String r2rmlAbsoluteFilePath = "/Users/fengz/GitHub/DataMatching/Web-Karma-2.051/doc/r2rml_test_file/location_E.ttl";
        String r2rmlPropFileAbsPath = "/Users/fengz/GitHub/DataMatching/Web-Karma-2.051/extension/r2rml-parser/r2rml.properties";
        String inputModelPath = "/Users/fengz/GitHub/DataMatching/Web-Karma-2.051/extension/r2rml-parser/dspace/edm-empty.rdf";
        String applicationContext = "/Users/fengz/GitHub/DataMatching/Web-Karma-2.051/extension/r2rml-parser/src/main/resources/app-context.xml";
        String owlFilePath = "/Users/fengz/Documents/Data_Multiple_Schema_Matching/Karma-data-set/RDF-Graph@14Mar19/CDM.owl";//added on 26 Mar 2019
        String applicationContextLocation = "";
        LinkedList<LogicalTableMapping> logicalTableMappings = R2RMLUtil.parseR2RML(r2rmlAbsoluteFilePath, r2rmlPropFileAbsPath, inputModelPath, applicationContextLocation);
        System.out.println("Get the logical table mappings...");
        List<CDMMapping> cdmMappings = R2RMLUtil.normalise(logicalTableMappings);
        JSONObject jsonDocument = R2RMLUtil.createJSON_Forward(cdmMappings,owlFilePath, new ArrayList<>());
        System.out.println(jsonDocument);

        System.out.println("Now begin to upload this JSON object to MangoDB!");
        MongoClient client = MongoFunctions.getClient("mongodb://MRuser:unisa@103.61.226.39:27017/MR");
        MongoCollection<Document> collection = MongoFunctions.getCollection("MR", "Zaiwen.mapping.rules", client);
        boolean result = MongoFunctions.insertDocument(jsonDocument, "MR", "Zaiwen.mapping.rules", client);
        if (result) {
            System.out.println("JSON object has been successfully uploaded!");
            System.out.println("All the documents in this collection is: " + collection.count());
        } else {
            System.out.println("JSON object has NOT been successfully uploaded!");
        }

//        System.out.println("Preparing for POST it to the server");
//        String url = "http://103.61.226.11:8091/mr";
//        System.out.println("The URL of server is: " + url);
//        R2RMLUtil.netClientPost(url, jsonDocument);

        client.close();

    }



}
