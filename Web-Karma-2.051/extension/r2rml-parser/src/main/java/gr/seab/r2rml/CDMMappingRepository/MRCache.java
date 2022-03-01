package gr.seab.r2rml.CDMMappingRepository;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import org.bson.Document;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.sql.*;

public class MRCache {

    public static Connection connection;

//    public static void main(String[] args) {
//
//            MRCache cache = new MRCache("/Users/Shared/OneDrive/FederatedDataPlatformProject/Workspace/IntellijGit/federated-data-platform/FederationEngine/test.db", false);
////            cache.createAttriMappingTable("promis");
////            cache.createAttriMappingTable("CDM");
////            cache.syncAttriMapping("CDM", "MRuser", "unisa");
////            cache.syncAttriMapping("promis", "MRuser", "unisa");
////            System.out.println(cache.getAttriMapping("CDM","person","id","promis","persons"));
//            System.out.println(cache.getSchemaMapping("CDM","person","promis"));
//    }


    public MRCache(String filePath, Boolean inMemory) {
        try {
            connection = connect(filePath, inMemory);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("MRCache successfully connected!");
    }

    public void close() throws Exception {
        connection.close();
    }


    //memory db will cease to exist when the connection is closed, so make sure the connection is available all the time.
    public Connection connect(String filePath, Boolean inMemory) throws Exception {
        // SQLite connection string
        String url;
        if (inMemory) {
            url = "jdbc:sqlite::memory:";
        } else {
            url = "jdbc:sqlite:" + filePath;
        }
        Connection conn = DriverManager.getConnection(url);
        return conn;
    }



    public void createAttriMappingTable(String sourceName) {
        // SQL statement for creating a new table
        String tableName = sourceName + "AttriMapping";
        String sql = "CREATE TABLE IF NOT EXISTS " + tableName + " (\n"
                     + "	sourceName text,\n"
                     + "	sourceSchemaName text,\n"
                     + "	sourceAttriName text,\n"
                     + "	targetName text,\n"
                     + "	targetSchemaName text,\n"
                     + "	targetAttriName text,\n"
                     + "	metadata text,\n"
                     + "	description text\n"
                     + ");";
        try (Statement stmt = connection.createStatement()) {
            // create a new table
            stmt.execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

//    public void syncAttriMapping(String sourceName, String username, String password) {
//        //get data from collection
//        String collectionName = sourceName + ".attriMapping";
//        String url = MRFunctions.createURL(username, password);
//        MongoClient client = MongoFunctions.getClient(url);
//        MongoCollection<Document> collection = MongoFunctions.getCollection("MR", collectionName, client);
//        JSONArray results = MongoFunctions.getDocuments(null, collection, 0);
//        for (int i = 0; i < results.size(); i++) {
//            JSONObject result = (JSONObject) results.get(i);
//            //parse data from each document
//            sourceName = (String) result.get("sourceName");
//            String sourceSchemaName = (String) result.get("sourceSchemaName");
//            String sourceAttriName = (String) result.get("sourceAttriName");
//            String targetName = (String) result.get("targetName");
//            String targetSchemaName = (String) result.get("targetSchemaName");
//            String targetAttriName = (String) result.get("targetAttriName");
//            String metadata = (String) result.get("metadata");
//            String description = (String) result.get("description");
//            //insert into cache table
//            insertAttriMapping(sourceName, sourceSchemaName, sourceAttriName, targetName,
//                               targetSchemaName, targetAttriName, metadata,
//                               description);
//        }
//
//        //insert into cache table
//    }

    public void insertAttriMapping(String sourceName, String sourceSchemaName, String sourceAttriName,
                                   String targetName, String targetSchemaName, String targetAttriName, String metadata,
                                   String description) {
        String tableName = sourceName + "AttriMapping";
        String
            sql =
            "INSERT INTO " + tableName
            + "(sourceName,sourceSchemaName,sourceAttriName,targetName,targetSchemaName,targetAttriName,metadata,description) VALUES(?,?,?,?,?,?,?,?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, sourceName);
            pstmt.setString(2, sourceSchemaName);
            pstmt.setString(3, sourceAttriName);
            pstmt.setString(4, targetName);
            pstmt.setString(5, targetSchemaName);
            pstmt.setString(6, targetAttriName);
            pstmt.setString(7, metadata);
            pstmt.setString(8, description);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public String getAttriMapping(String sourceName, String sourceSchemaName, String sourceAttriName, String targetName,
                                  String targetSchemaName) {
        String tableName = sourceName + "AttriMapping";
        String targetAttriName = null;
        String sql = "select * from '" + tableName + "' where "
                     + "sourceName='" + sourceName + "' and "
                     + "sourceSchemaName='" + sourceSchemaName + "' and "
                     + "sourceAttriName='" + sourceAttriName + "' and "
                     + "targetName='" + targetName + "' and "
                     + "targetSchemaName='" + targetSchemaName + "' limit 1;";
        System.out.println(sql);
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                targetAttriName = rs.getString("targetAttriName");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return targetAttriName;
    }

    public String getSchemaMapping(String sourceName, String sourceSchemaName, String targetName){
        String tableName = sourceName + "AttriMapping";
        String targetSchemaName = null;
        String sql = "select * from '" + tableName + "' where "
                     + "sourceName='" + sourceName + "' and "
                     + "sourceSchemaName='" + sourceSchemaName + "' and "
                     + "targetName='" + targetName + "' limit 1;";
        System.out.println(sql);
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                targetSchemaName = rs.getString("targetSchemaName");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return targetSchemaName;
    }
}

