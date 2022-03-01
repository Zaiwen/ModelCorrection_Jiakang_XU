//package gr.seab.r2rml.CDMMappingRepository;
//
//import edu.unisa.ILE.FSA.EnginePortal.FDEApplication;
//import org.json.simple.JSONArray;
//import org.json.simple.JSONObject;
//
//public class SchemaTranslater {
//
////	public static void main(String[] args) {
//////		AttriMapping attriMapping = new AttriMapping();
//////		SchemaMapper schemaMapper = new SchemaMapper();
//////		attriMapping.setTargetName("promis");
//////		attriMapping.setSourceSchemaName("Bank");
//////		attriMapping.setSourceName("CDM");
//////		attriMapping.setSourceAttriName("bank_name");
//////		attriMapping.setTargetSchemaName("NDS3.csv");
//////		String filterValues = schemaMapper.buildFilter(attriMapping, "convertToSourceType");
//////		String jsonString = "{ \"query\": { \"filter\": " + filterValues
//////				+ "}, \"control\": { \"operation\": \"getAttriMapping\", \"sources\": [ \"Amir\" ] }, \"credentials\": { \"credentiallist\": [ { \"MR\": { \"username\": \"MRuser\", \"password\": \"unisa\" } } ] }}";
//////		String result = schemaMapper.callApi(jsonString, "convertToSourceType");
//////		System.out.println(result);
////
////
//////        System.out.println();
//////        System.out.println(convertToSourceType("Location", "promis"));
//////        System.out.println();
////
////        System.out.println();
////        System.out.println(convertToSourceColumn("person", "id", "promis", "persons"));
////        System.out.println();
////	}
//
//
//
//    public static String convertToSourceType(String sourceSchemaName, String targetName){
//        String sourceName = "CDM";
//        String targetSchemaName = FDEApplication.Cache.getSchemaMapping(sourceName, sourceSchemaName, targetName);
//        if (targetSchemaName != null) {
//            System.out.println("output targetSchemaName: "+targetSchemaName);
//            System.out.println();
//            return targetSchemaName;
//        } else {
//            System.out.println("output sourceSchemaName: "+sourceSchemaName);
//            System.out.println();
//            return sourceSchemaName;
//        }
//	}
//
//    public static String convertToSourceTypeMR(String sourceSchemaName, String targetName){
//
//	    String sourceName = "CDM";
//        //get targetSchemaName
//        JSONObject query = new JSONObject();
//        query.put("sourceName",sourceName);
//        query.put("sourceSchemaName",sourceSchemaName);
//        query.put("targetName",targetName);
//        JSONObject response = MRFunctions.getAttriMapping(sourceName, query, FDEApplication.MRusername, FDEApplication.MRpwd, 1);
//        JSONArray payload = (JSONArray) response.get("payload");
//        JSONObject resultJSON = new JSONObject();
//        if (payload.size()>0){
//            resultJSON = (JSONObject) payload.get(0);
//        }
//
//        String targetSchemaName = (String) resultJSON.get("targetSchemaName");
//        System.out.println();
//        System.out.println("convertToSourceType triggered: "+" Input sourceSchemaName: "+sourceSchemaName+" targetName: "+targetName + " Output targetSchemaName "+targetSchemaName);
//        if (targetSchemaName != null) {
//            System.out.println("output targetSchemaName: "+targetSchemaName);
//            System.out.println();
//            return targetSchemaName;
//        } else {
//            System.out.println("output sourceSchemaName: "+sourceSchemaName);
//            System.out.println();
//            return sourceSchemaName;
//        }
//    }
//
//    public static String convertToSourceColumn(String sourceSchemaName, String sourceAttriName, String targetName, String targetSchemaName) {
//        String sourceName = "CDM";
//        String targetAttriName = FDEApplication.Cache.getAttriMapping(sourceName, sourceSchemaName, sourceAttriName, targetName, targetSchemaName);
//        if (targetAttriName != null) {
//            System.out.println("output targetAttriName: "+targetAttriName);
//            System.out.println();
//            return targetAttriName;
//        } else {
//            System.out.println("output sourceAttriName: "+sourceAttriName);
//            System.out.println();
//            return sourceAttriName;
//        }
//    }
//
//    public static String convertToSourceColumnMR(String sourceSchemaName, String sourceAttriName, String targetName, String targetSchemaName) {
//        String sourceName = "CDM";
//        //get targetAttriName
//        JSONObject query = new JSONObject();
//        query.put("sourceName",sourceName);
//        query.put("sourceSchemaName",sourceSchemaName);
//        query.put("sourceAttriName",sourceAttriName);
//        query.put("targetName",targetName);
//        query.put("targetSchemaName",targetSchemaName);
//        JSONObject response = MRFunctions.getAttriMapping(sourceName, query, FDEApplication.MRusername, FDEApplication.MRpwd, 1);
//        JSONArray payload = (JSONArray) response.get("payload");
//        JSONObject resultJSON = new JSONObject();
//        if (payload.size()>0){
//            resultJSON = (JSONObject) payload.get(0);
//        }
//
//        String targetAttriName = (String) resultJSON.get("targetAttriName");;
//        System.out.println();
//        System.out.println("convertToSourceColumn triggered: "+" Input sourceSchemaName: "+sourceSchemaName+" sourceAttriName: "+sourceAttriName+" targetName: "+targetName+" targetSchemaName: "+targetSchemaName+ " Output targetAttriName "+targetAttriName);
//        if (targetAttriName != null) {
//            System.out.println("output targetAttriName: "+targetAttriName);
//            System.out.println();
//            return targetAttriName;
//        } else {
//            System.out.println("output sourceAttriName: "+sourceAttriName);
//            System.out.println();
//            return sourceAttriName;
//        }
//    }
//
//    public static String convertToGenericColumn(String sourceName, String sourceSchemaName, String sourceAttriName, String targetSchemaName){
//        String targetName = "CDM";
//        String targetAttriName = FDEApplication.Cache.getAttriMapping(sourceName, sourceSchemaName, sourceAttriName, targetName, targetSchemaName);
//        if (targetAttriName != null) {
//            System.out.println("output targetAttriName: "+targetAttriName);
//            System.out.println();
//            return targetAttriName;
//        } else {
//            System.out.println("output sourceAttriName: "+sourceAttriName);
//            System.out.println();
//            return sourceAttriName;
//        }
//    }
//
//    public static String convertToGenericColumnMR(String sourceName, String sourceSchemaName, String sourceAttriName, String targetSchemaName){
//        String targetName = "CDM";
//        //get targetAttriName
//        JSONObject query = new JSONObject();
//        query.put("sourceName",sourceName);
//        query.put("sourceSchemaName",sourceSchemaName);
//        query.put("sourceAttriName",sourceAttriName);
//        query.put("targetName",targetName);
//        query.put("targetSchemaName",targetSchemaName);
//        JSONObject response = MRFunctions.getAttriMapping(sourceName, query, FDEApplication.MRusername, FDEApplication.MRpwd, 1);
//        JSONArray payload = (JSONArray) response.get("payload");
//        JSONObject resultJSON = new JSONObject();
//        if (payload.size()>0){
//            resultJSON = (JSONObject) payload.get(0);
//        }
//
//        String targetAttriName = (String) resultJSON.get("targetAttriName");
//        System.out.println();
//        System.out.println("convertToGenericColumn triggered: "+" Input sourceName: "+sourceName+" sourceSchemaName: "+sourceSchemaName+" sourceAttriName: "+sourceAttriName+" targetSchemaName: "+targetSchemaName+ " Output targetAttriName "+targetAttriName);
//        if (targetAttriName != null) {
//            System.out.println("output targetAttriName: "+targetAttriName);
//            System.out.println();
//            return targetAttriName;
//        } else {
//            System.out.println("output sourceAttriName: "+sourceAttriName);
//            System.out.println();
//            return sourceAttriName;
//        }
//    }
//
//}
