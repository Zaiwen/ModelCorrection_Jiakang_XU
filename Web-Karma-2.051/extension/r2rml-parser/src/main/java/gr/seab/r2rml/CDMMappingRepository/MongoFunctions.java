package gr.seab.r2rml.CDMMappingRepository;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import com.mongodb.util.JSON;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.util.ArrayList;
import java.util.List;

import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Updates.*;

public class MongoFunctions {

    public static void main(String[] args) {
        MongoClient client = getClient("mongodb://MRuser:unisa@103.61.226.39:27017/MR");
        //MongoCollection<Document> collection = getCollection("MR", "Zaiwen.attributeMapping.rules", client);
        MongoCollection<Document> collection = getCollection("MR", "Zaiwen", client);
        System.out.println("All the documents in this collection is: " + collection.count());

        /**Get all of the document from the collection**/
        JSONArray results = getDocuments(null, collection, 0);
        System.out.println(results.toJSONString());

//        JSONObject j = new JSONObject();
//        j.put("test2",0);
//        JSONArray ja = new JSONArray();
//        ja.add(j);
//        insertDocuments(ja,collection);

//        deleteDocuments("{test1:0}", collection);

//        JSONObject matchEntityType = new JSONObject();
//        JSONArray sources = new JSONArray();
//        sources.add("es");
//        matchEntityType.put("sources", sources);
//
//        String sourceName = "promis";
//        JSONArray outSources = (JSONArray) matchEntityType.get("sources");
//        if (!outSources.contains(sourceName)) {
//            String filterString = matchEntityType.toString();
//            System.out.println("filterString" + filterString);
//            outSources.add(sourceName);
//            String updateString = matchEntityType.toString();
//            System.out.println("updateString" + updateString);
//        }
//        boolean result = createCollection("MR", "test", "{x:1}", client);
//        System.out.println(result);
//        boolean result = dropCollection("MR", "test", client);
//        System.out.println(result);

        client.close();
    }

    public static MongoClient getClient(String connectionString) {
//        MongoClientURI connectionString = new MongoClientURI("mongodb://MRuser:unisa@130.220.210.130:27017/MR");
        MongoClientURI connectionURI = new MongoClientURI(connectionString);
        MongoClient client = new MongoClient(connectionURI);
        return client;
    }

    public static MongoCollection<Document> getCollection(String dbName, String collectionName, MongoClient client) {
        MongoDatabase database = client.getDatabase(dbName);
        MongoCollection<Document> collection = database.getCollection(collectionName);
        return collection;
    }

    public static boolean createCollection(String dbName, String collectionName, String firstDocument,
                                           MongoClient client) {
        MongoCollection<Document> collection = getCollection(dbName, collectionName, client);

        if (collection.count() > 0) {
            return false;
        } else {
            try {
                Document doc = Document.parse(firstDocument);
                collection.insertOne(doc);

            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }

    public static boolean dropCollection(String dbName, String collectionName, MongoClient client) {
        MongoCollection<Document> collection = getCollection(dbName, collectionName, client);

        try {
            collection.drop();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static boolean insertDocuments(JSONArray documentJSONArray, MongoCollection<Document> collection) {

        List<Document> documents = new ArrayList();

        for (int i = 0; i < documentJSONArray.size(); i++) {
            JSONObject documentJSONObject = (JSONObject) documentJSONArray.get(i);
            Document document = Document.parse(documentJSONObject.toJSONString());
            documents.add(document);
        }

        try {
            collection.insertMany(documents);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public static boolean insertDocuments(JSONArray documentJSONArray, String dbName, String collectionName,
                                          MongoClient client) {
        return insertDocuments(documentJSONArray, getCollection(dbName, collectionName, client));
    }

    public static boolean insertDocument(JSONObject document, String dbName, String collectionName,
                                         MongoClient client) {
        JSONArray documentJSONArray = new JSONArray();
        documentJSONArray.add(document);
        return insertDocuments(documentJSONArray, getCollection(dbName, collectionName, client));
    }

    public static JSONArray getDocuments(String filterString, MongoCollection<Document> collection, int limit) {

        JSONArray results = new JSONArray();
        JSONParser parser = new JSONParser();

        Bson bson = (Bson) JSON.parse(filterString);
        if (bson == null) {
            bson = (Bson) JSON.parse("{}");
        }

        MongoCursor<Document> cursor;
        if (limit == 0){
            cursor = collection.find(bson).iterator();
        } else {
            cursor = collection.find(bson).limit(limit).iterator();
        }

        try {
            while (cursor.hasNext()) {
                JSONObject result = (JSONObject) parser.parse(cursor.next().toJson());
                System.out.println(result.toString());
                results.add(result);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            cursor.close();
        }

        return results;
    }

    public static JSONArray getDocuments(String filterString, String dbName, String collectionName,
                                         MongoClient client, int limit) {
        return getDocuments(filterString, getCollection(dbName, collectionName, client), limit);
    }

    public static UpdateResult updateDocuments(String filterString, String updateString,
                                               MongoCollection<Document> collection) {
        Bson filterBson = (Bson) JSON.parse(filterString);
        Bson updateBson = (Bson) JSON.parse(updateString);

        if (filterBson == null) {
            filterBson = (Bson) JSON.parse("{}");
        }
        UpdateResult ur = null;
        try {
            ur = collection.updateMany(filterBson, updateBson);
            System.out.println(ur.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ur;
    }

    public static UpdateResult updateDocuments(String filterString, String updateString, String dbName,
                                               String collectionName, MongoClient client) {
        return updateDocuments(filterString, updateString, getCollection(dbName, collectionName, client));
    }

    public static DeleteResult deleteDocuments(String filterString, MongoCollection<Document> collection) {
        Bson filterBson = (Bson) JSON.parse(filterString);
        if (filterBson == null) {
            filterBson = (Bson) JSON.parse("{}");
        }
        DeleteResult dr = null;
        try {
            dr = collection.deleteMany(filterBson);
            System.out.println(dr.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dr;
    }

    public static DeleteResult deleteDocuments(String filterString, String dbName, String collectionName,
                                               MongoClient client) {
        return deleteDocuments(filterString, getCollection(dbName, collectionName, client));
    }


}
