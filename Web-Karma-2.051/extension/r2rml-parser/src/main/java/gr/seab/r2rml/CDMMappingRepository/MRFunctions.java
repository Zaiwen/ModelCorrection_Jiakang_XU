//package gr.seab.r2rml.CDMMappingRepository;
//
//import com.fasterxml.jackson.databind.DeserializationFeature;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.mongodb.MongoClient;
//import com.mongodb.client.result.UpdateResult;
////import edu.unisa.ILE.FSA.EnginePortal.FDEApplication;
////import edu.unisa.ILE.FSA.EnginePortal.JSONFunctions;
////import edu.unisa.ILE.FSA.InternalDataStructure.ControlSpec;
////import edu.unisa.ILE.FSA.InternalDataStructure.QuerySpec;
////import edu.unisa.ILE.FSA.InternalDataStructure.UserAccessSpec;
//import org.json.simple.JSONArray;
//import org.json.simple.JSONObject;
//import org.json.simple.parser.JSONParser;
//import org.json.simple.parser.ParseException;
//
//public class MRFunctions {
//
//    public static JSONObject conduct(ControlSpec cs, QuerySpec qs, UserAccessSpec uas) {
//        System.out.println("ControlSpec: " + cs);
//        System.out.println("QuerySpec: " + qs);
//        System.out.println("UserAccessSpec: " + uas);
//        JSONObject result = new JSONObject();
//        JSONParser parser = new JSONParser();
//
//        //cs attributes
//        String operation = cs.getOperation();//must not be null
//
//        String sourceName = null;
//        if (cs.getSources() != null && cs.getSources().size() > 0) {
//            sourceName = (String) cs.getSources().get(0);
//        }
//
//        //qs attributes
//        JSONObject filter_spec = null;
//        if (qs != null && qs.getFILTER_SPEC() != null) {
//            try {
//                filter_spec = (JSONObject) parser.parse(qs.getFILTER_SPEC().toString());
//            } catch (ParseException e) {
//                e.printStackTrace();
//            }
//        }
//
//        //uas attributes
//        JSONArray credentialList;
//        JSONObject MRcredential = null; //must not be null
//        try {
//            credentialList = (JSONArray) parser.parse(uas.getCredentialList().toString());
//            for (int i = 0; i < credentialList.size(); i++) {
//                JSONObject credential = (JSONObject) credentialList.get(i);
//                if (credential.containsKey("MR")) {
//                    MRcredential = (JSONObject) credential.get("MR");
//                    break;
//                }
//            }
//        } catch (ParseException e) {
//            e.printStackTrace();
//        }
//
//        if (MRcredential != null) {
//            String username = (String) MRcredential.get("username");
//            String password = (String) MRcredential.get("password");
//
//            try {
//                switch (operation) {
//                    case "addSource":// changes in MR.sources and MR.entityTypes
//                        result = addSource(filter_spec, username, password);
//                        break;
//                    case "getSource":
//                        result = getSource(sourceName, username, password);
//                        break;
//                    case "updateSource":// changes in MR.sources and MR.entityTypes
////                        result = findEntities(types, criteria, username, password);
//                        break;
//                    case "deleteSource":// changes in MR.sources and MR.entityTypes
////                        result = findEntities(types, criteria, username, password);
//                        break;
//                    case "listAllSources":
//                        result = listAllSources(username, password);
//                        break;
//                    case "listEntityTypes":
//                        result = listEntityTypes(sourceName, username, password);
//                        break;
//
//                    case "addEntityType":
//                        result = addEntityType(filter_spec, username, password);
//                        break;
//                    case "getEntityType":
//                        result = getEntityType(filter_spec, username, password);
//                        break;
//                    case "updateEntityType":
////                        result = findEntities(types, criteria, username, password);
//                        break;
//                    case "deleteEntityType":
////                        result = findEntities(types, criteria, username, password);
//                        break;
//                    case "listAllEntityTypes()":
//                        result = listAllEntityTypes(username, password);
//                        break;
//
//                    case "insertR2RML":
////                        result = findEntities(types, criteria, username, password);
//                        break;
//                    case "getR2RML":
////                        result = findEntities(types, criteria, username, password);
//                        break;
//                    case "updateR2RML":
////                        result = findEntities(types, criteria, username, password);
//                        break;
//                    case "deleteR2RML":
////                        result = findEntities(types, criteria, username, password);
//                        break;
//
//                    case "insertSchemaMapping":
////                        result = findEntities(types, criteria, username, password);
//                        break;
//                    case "getSchemaMapping":
////                        result = findEntities(types, criteria, username, password);
//                        break;
//                    case "updateSchemaMapping":
////                        result = findEntities(types, criteria, username, password);
//                        break;
//                    case "deleteSchemaMapping":
////                        result = findEntities(types, criteria, username, password);
//                        break;
//
//                    case "insertAttriMapping":
//                        result = insertAttriMapping(sourceName, filter_spec, username, password);
//                        break;
//                    case "getAttriMapping":
//                        result = getAttriMapping(sourceName, filter_spec, username, password, 0);
//                        break;
//                    case "updateAttriMapping":
////                        result = findEntities(types, criteria, username, password);
//                        break;
//                    case "deleteAttriMapping":
////                        result = findEntities(types, criteria, username, password);
//                        break;
//
//                    case "insertSchemaTransRules":
//                        result = insertSchemaTransRules(sourceName, filter_spec, username, password);
//                        break;
//                    case "getSchemaTransRules":
//                        result = getSchemaTransRules(sourceName, filter_spec, username, password, 0);
//                        break;
//                    case "updateSchemaTransRules":
////                        result = findEntities(types, criteria, username, password);
//                        break;
//                    case "deleteSchemaTransRules":
////                        result = findEntities(types, criteria, username, password);
//                        break;
//
//                    case "insertValueMapping":
////                        result = findEntities(types, criteria, username, password);
//                        break;
//                    case "findValueMapping":
////                        result = findEntities(types, criteria, username, password);
//                        break;
//                    case "updateValueMapping":
////                        result = findEntities(types, criteria, username, password);
//                        break;
//                    case "deleteValueMapping":
////                        result = findEntities(types, criteria, username, password);
//                        break;
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//        return result;
//    }
//
//    //sources collection related functions
//    private static JSONObject addSource(JSONObject filter_spec, String username,
//                                        String password) {
//        boolean success = false;
//        //create URL
//        String URL = createURL(username, password);
//        String collectionName = "sources";
//        String dbName = "MR";
//
//        // changes in MR.sources and MR.entityTypes
//        // attribute matching and create filteredSource
//        ObjectMapper mapper = new ObjectMapper();
//        mapper.enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);
//        Source source = JSONFunctions.JSON_match(mapper, Source.class, filter_spec);
//        JSONObject filteredSource = source.toJson();
//
//        // get source name
//        String sourceName = (String) filteredSource.get("sourceName");
//        if (sourceName != null) {
//            // connect connection
//            MongoClient client = MongoFunctions.getClient(URL);
//            // fist check if it exist already
//            JSONArray
//                matchSources =
//                MongoFunctions.getDocuments("{sourceName:\"" + sourceName + "\"}", dbName, collectionName, client, 0);
//            if (matchSources.size() == 0) {
//                //insert new document to MR.sources
//                MongoFunctions.insertDocument(filteredSource, dbName, collectionName, client);
//
//                //update entityTypes in MR.entityTypes
//                JSONArray entityTypes = source.getEntityTypes();
//                //for each entityType in collection entityTypes, two attributes will be updated,
//                //which are entityName and sources.
//                for (int i = 0; i < entityTypes.size(); i++) {
//                    String entityName = (String) entityTypes.get(i);
//                    //First, check if the entityType exists in entityTypes collection
//                    JSONArray
//                        matchEntityTypes =
//                        MongoFunctions
//                            .getDocuments("{entityName:\"" + entityName + "\"}", dbName, "entityTypes", client, 0);
//                    if (matchEntityTypes.size() == 0) {
//                        //if not, create a new entityType named entityName with one source and insert
//                        JSONArray sources = new JSONArray();
//                        sources.add(sourceName);
//                        EntityType entityType = new EntityType(entityName, "", sources, new JSONObject());
//                        MongoFunctions.insertDocument(entityType.toJson(), dbName, "entityTypes", client);
//                        success = true;
//                    } else {
//                        //if yes, update existing entityType by upserting the sources JSONArray
//                        JSONObject matchEntityType = (JSONObject) matchEntityTypes.get(0);
//                        JSONArray sources = (JSONArray) matchEntityType.get("sources");
//                        if (!sources.contains(sourceName)) {
//                            String filterString = "{entityName:\"" + entityName + "\"}";
////                            System.out.println(filterString);
//                            sources.add(sourceName);
//                            String updateString = "{$set:{sources:" + sources + "}}";
////                            System.out.println(updateString);
//                            UpdateResult
//                                ur =
//                                MongoFunctions
//                                    .updateDocuments(filterString, updateString, dbName, "entityTypes", client);
////                            System.out.println("updated number:" + ur.getModifiedCount());
//                        }
//                        success = true;
//                    }
//                }
//            }
//            //close connection
//            client.close();
//        }
//
//        //create response
//        JSONObject result = new JSONObject();
//        result.put("acknowledge", success);
//        return result;
//    }
//
//    public static JSONObject getSource(String sourceName, String username,
//                                       String password) {
//
//        //create filterString
//        String filterString = null;
//        if (sourceName != null) {
//            filterString = "{sourceName:\"" + sourceName + "\"}";
//        }
//        //create URL
//        String URL = createURL(username, password);
//        String collectionName = "sources";
//        String dbName = "MR";
//        //connect connection
//        MongoClient client = MongoFunctions.getClient(URL);
//        //get documents
//        JSONArray sources = MongoFunctions.getDocuments(filterString, dbName, collectionName, client, 0);
//        //close connection
//        client.close();
//        //extract payload
//        if (sources.size() > 0) {
//            JSONObject source = (JSONObject) sources.get(0);//get source should return a single source
//            JSONArray payload = new JSONArray();
//            payload.add(source);
//            JSONObject result = new JSONObject();
//            result.put("acknowledge", "true");
//            result.put("payload", payload);
//            return result;
//        } else {
//            JSONObject result = new JSONObject();
//            result.put("acknowledge", "false");
//            result.put("exception", "cannot find source" + sourceName);
//            return result;
//        }
//    }
//
//    private static JSONObject listAllSources(String username,
//                                             String password) {
//        //create URL
//        String URL = createURL(username, password);
//        String collectionName = "sources";
//        String dbName = "MR";
//        //connect
//        MongoClient client = MongoFunctions.getClient(URL);
//        //get documents
//        JSONArray sources = MongoFunctions.getDocuments(null, dbName, collectionName, client, 0);
//        //close connection
//        client.close();
//
//        //generate result
//        JSONArray payload = new JSONArray();
//        for (int i = 0; i < sources.size(); i++) {
//            JSONObject source = (JSONObject) sources.get(i);
//            String sourceName = (String) source.get("sourceName");
//            payload.add(sourceName);
//        }
//        JSONObject result = new JSONObject();
//        result.put("acknowledge", "true");
//        result.put("payload", payload);
//        return result;
//    }
//
//    private static JSONObject listEntityTypes(String sourceName, String username,
//                                              String password) {
//        //create filterString
//        String filterString = null;
//        if (sourceName != null) {
//            filterString = "{sourceName:\"" + sourceName + "\"}";
//        }
//        //create URL
//        String URL = createURL(username, password);
//        String collectionName = "sources";
//        String dbName = "MR";
//        //connect connection
//        MongoClient client = MongoFunctions.getClient(URL);
//        //get documents
//        JSONArray sources = MongoFunctions.getDocuments(filterString, dbName, collectionName, client, 0);
//        //close connection
//        client.close();
//        //extract payload
//        if (sources.size() > 0) {
//            JSONObject source = (JSONObject) sources.get(0);//only one source should be retrieved
//            JSONArray payload = (JSONArray) source.get("entityTypes");
//            JSONObject result = new JSONObject();
//            result.put("acknowledge", "true");
//            result.put("payload", payload);
//            return result;
//        } else {
//            JSONObject result = new JSONObject();
//            result.put("acknowledge", "false");
//            result.put("exception", "cannot find source" + sourceName);
//            return result;
//        }
//    }
//
//    //entityTypes collection related functions
//
//    private static JSONObject addEntityType(JSONObject filter_spec, String username,
//                                            String password) {
//        return null;
//    }
//
//    private static JSONObject getEntityType(JSONObject filter_spec, String username,
//                                            String password) {
//        //parse filter
//        String filterString = null;
//        if (filter_spec != null) {
//            filterString = filter_spec.toJSONString(); //{entityName:xxx}
//        }
//        //create URL
//        String URL = createURL(username, password);
//        String collectionName = "entityTypes";
//        String dbName = "MR";
//        //connect connection
//        MongoClient client = MongoFunctions.getClient(URL);
//        //get documents
//        JSONArray entityTypes = MongoFunctions.getDocuments(filterString, dbName, collectionName, client, 0);
//        //close connection
//        client.close();
//        //extract payload
//        if (entityTypes.size() > 0) {
//            JSONObject entityType = (JSONObject) entityTypes.get(0);//should return a single entity
//            JSONArray payload = new JSONArray();
//            payload.add(entityType);
//            JSONObject result = new JSONObject();
//            result.put("acknowledge", "true");
//            result.put("payload", payload);
//            return result;
//        } else {
//            JSONObject result = new JSONObject();
//            result.put("acknowledge", "false");
//            result.put("exception", "cannot find entity type" + entityTypes);
//            return result;
//        }
//    }
//
//    private static JSONObject listAllEntityTypes(String username,
//                                                 String password) {
//        System.out.println();
//
//        System.out.println("listAllEntityTypes triggered");
//
//        System.out.println();
//        //create URL
//        String URL = createURL(username, password);
//        String collectionName = "entityTypes";
//        String dbName = "MR";
//        //connect
//        MongoClient client = MongoFunctions.getClient(URL);
//        //get documents
//        JSONArray entityTypes = MongoFunctions.getDocuments(null, dbName, collectionName, client, 0);
//        //close connection
//        client.close();
//
//        //generate result
//        JSONArray payload = new JSONArray();
//        for (int i = 0; i < entityTypes.size(); i++) {
//            JSONObject entityType = (JSONObject) entityTypes.get(i);
//            String entityName = (String) entityType.get("entityName");
//            payload.add(entityName);
//        }
//        JSONObject result = new JSONObject();
//        result.put("acknowledge", "true");
//        result.put("payload", payload);
//        return result;
//    }
//
//    //attriMapping collection related functions
//
//    private static JSONObject insertAttriMapping(String sourceName, JSONObject filter_spec, String username,
//                                                 String password) {
//        //filter input array, match attributes and create the filtered documentArray
//        JSONArray inputArray = (JSONArray) filter_spec.get("documentArray");
//        JSONArray filteredArray = new JSONArray();
//        ObjectMapper mapper = new ObjectMapper();
//        mapper.enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);
//        for (int i = 0; i < inputArray.size(); i++) {
//            AttriMapping mapping = JSONFunctions.JSON_match(mapper, AttriMapping.class, (JSONObject) inputArray.get(i));
//            JSONObject document = mapping.toJson();
//            filteredArray.add(document);
//        }
//        //create URL
//        String URL = createURL(username, password);
//        String collectionName = sourceName + ".attriMapping";
//        String dbName = "MR";
//        //connect connection
//        MongoClient client = MongoFunctions.getClient(URL);
//        //insert documents
//        boolean success = MongoFunctions.insertDocuments(filteredArray, dbName, collectionName, client);
//        //close connection
//        client.close();
//        //create response
//        JSONObject result = new JSONObject();
//        result.put("acknowledge", success);
//        return result;
//    }
//
//    public static JSONObject getAttriMapping(String sourceName, JSONObject filter_spec, String username,
//                                              String password, int limit) {
//        //parse filter
//        String filterString = null;
//        if (filter_spec != null) {
//            filterString = filter_spec.toJSONString();
//        }
//        //create URL
//        String URL = createURL(username, password);
//        String collectionName = sourceName + ".attriMapping";
//        String dbName = "MR";
//        //connect connection
//        MongoClient client = MongoFunctions.getClient(URL);
//        //insert documents
//        JSONArray payload = MongoFunctions.getDocuments(filterString, dbName, collectionName, client, limit);
//        //close connection
//        client.close();
//        //create response
//        JSONObject result = new JSONObject();
//        result.put("acknowledge", "true");
//        result.put("payload", payload);
//        return result;
//    }
//
//    //schemaTransRules collection related functions
//
//    private static JSONObject insertSchemaTransRules(String sourceName, JSONObject filter_spec, String username,
//                                                     String password) {
//        //filter input array, match attributes and create the filtered documentArray
//        JSONArray inputArray = (JSONArray) filter_spec.get("documentArray");
//        JSONArray filteredArray = new JSONArray();
//        ObjectMapper mapper = new ObjectMapper();
//        mapper.enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);
//        for (int i = 0; i < inputArray.size(); i++) {
//            SchemaTransRule
//                rule =
//                JSONFunctions.JSON_match(mapper, SchemaTransRule.class, (JSONObject) inputArray.get(i));
//            JSONObject document = rule.toJson();
//            filteredArray.add(document);
//        }
//        //create URL
//        String URL = createURL(username, password);
//        String collectionName = sourceName + ".schemaTransRules";
//        String dbName = "MR";
//        //connect connection
//        MongoClient client = MongoFunctions.getClient(URL);
//        //insert documents
//        boolean success = MongoFunctions.insertDocuments(filteredArray, dbName, collectionName, client);
//        //close connection
//        client.close();
//        //create response
//        JSONObject result = new JSONObject();
//        result.put("acknowledge", success);
//        return result;
//    }
//
//    private static JSONObject getSchemaTransRules(String sourceName, JSONObject filter_spec, String username,
//                                                  String password, int limit) {
//        //parse filter
//        String filterString = null;
//        if (filter_spec != null) {
//            filterString = filter_spec.toJSONString();
//        }
//        //create URL
//        String URL = createURL(username, password);
//        String collectionName = sourceName + ".schemaTransRules";
//        String dbName = "MR";
//        //connect connection
//        MongoClient client = MongoFunctions.getClient(URL);
//        //get documents
//        JSONArray payload = MongoFunctions.getDocuments(filterString, dbName, collectionName, client, limit);
//        //close connection
//        client.close();
//        //create response
//        JSONObject result = new JSONObject();
//        result.put("acknowledge", "true");
//        result.put("payload", payload);
//        return result;
//    }
//
//    public static String createURL(String username, String password) {
//        return "mongodb://" + username + ":" + password + "@" + FDEApplication.MongoDBURL;
//    }
//
//
//}
