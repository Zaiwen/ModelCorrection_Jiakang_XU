package au.edu.unisa.json;


import au.edu.unisa.Settings;
//import com.google.gson.JsonElement;
//import com.google.gson.JsonObject;
import jnr.ffi.annotations.In;
import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;

/**This class is responsible for transforming json museum data into mysql database*
 * @from 27 April 2020
 * @revised 30 April, 1 May, 2020
 * */
public class JSON2MysqlAdaptor {

    private List<String> jsonPropList = new ArrayList<>();//Corresponding to all the json properties in databases
    private List<String> dbColumnNameList = new ArrayList<>();//Corresponding to all the column names in database

    /**add a json prop into "jsonPropList"**/
    public void addJsonProp (String jsonProp) {
        jsonPropList.add(jsonProp);
    }

    /**add a db column name into "dbColumnNameList"**/
    public void addDBColumnNameList (String dbColumnName) {dbColumnNameList.add(dbColumnName); }

    /**transformer that ingests data from a json file to mysql database*
     *
     * @param jsonFileName name of a json file which needs to be ingested
     * @param tableName name of table in the database of museum
     * @param birthDeathDate_propName the property name representing birth and death date in json file
     * @from 27 April 2020, 2 May 2020
     * */
    public void transform (String jsonFileName, String tableName, String birthDeathDate_propName) throws JSON2MysqlException {

        if (jsonPropList.size() == 0) {

            new JSON2MysqlException("json property list has NOT been prepared! ");
        }

        if (dbColumnNameList.size() == 0) {

            new JSON2MysqlException("db column name list has NOT been prepared! ");
        }

        //JSON Parser object to parse Json DS file
        JSONParser jsonParser = new JSONParser();
        try {
            JSONArray array = (JSONArray) jsonParser.parse(new FileReader(Settings.MUSEUM_CRM_DS_DIR.concat(jsonFileName)));
            System.out.println(array);

            System.out.println("Begin to link mysql database...");

            /**begin to connect mysql...**/
            String myDriver = "com.mysql.cj.jdbc.Driver";
            String myUrl = "jdbc:mysql:// localhost:3306/";
            Class.forName(myDriver);
            String dbName = "Museum?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC";
            String dbUserName = "root";
            //String dbPassword = "123456Whu";/**the password on the Mac at home**/
            String dbPassword = "MAcri"; /** the password on the workstation in the office**/
            Connection conn = DriverManager.getConnection(myUrl + dbName, dbUserName, dbPassword);

            System.out.println("begin to delete all the previous data in mysql db...");
            /**delete any previous records..**/
            PreparedStatement st = conn.prepareStatement("delete from " + tableName);//firstly, delete all the existing rows
            st.executeUpdate();
            st.close();

            int count = 1;
            /**iterator over all the elements in JSONArray**/
            Iterator<JSONObject> iterator = array.iterator();
            while (iterator.hasNext()) {
                JSONObject jsonElement = iterator.next();//one json_object is like: {A=a; B=b, C=c,...}

                /**build a hash map, whose key is a json property (like: "Birth"), and the value is the value of the json property**/
                LinkedHashMap<String,String> jsonObjMap = new LinkedHashMap<>();

                System.out.println("----------------------------------");
                System.out.println("-------------ELEMENT  "  + count  + "---------------------");

                /**iterate over all the json properties**/
                Iterator<String> iterator1 = jsonPropList.iterator();
                while (iterator1.hasNext()) {

                    String jsonPropName = iterator1.next();//get a json property, like: "BirthDeathDate"
                    String jsonPropValue = (String) jsonElement.get(jsonPropName); //get the value, like "1849-1936"

                    jsonObjMap.put(jsonPropName,jsonPropValue);

                }

                System.out.println("Splitting Birth&Death Date value into BirthDate and DeathDate...");
                String birthDate = "";
                String deathDate = "";
                String birth_and_death_date = jsonObjMap.get(birthDeathDate_propName);

                if (birth_and_death_date == null) {
                    new JSON2MysqlException("Exception: birth and death date is NOT catched...");
                }

                /**split birth and death date**/
                if (birth_and_death_date.length()==9) {//like: 1900-1990
                    birthDate = birth_and_death_date.substring(0,4);
                    deathDate = birth_and_death_date.substring(5,9);
                }else if (birth_and_death_date.length()==5) {//like: 1931-
                    birthDate = birth_and_death_date.substring(0,4);

                }

                LinkedHashMap dbRow = new LinkedHashMap();//to store a row of data in mysql. Key: database column names, Value: a row of data
                dbRow.put(dbColumnNameList.get(0),Integer.valueOf(count).toString());
                dbRow.put(dbColumnNameList.get(1), birthDate);
                dbRow.put(dbColumnNameList.get(2), deathDate);

                /**remove the element which the key is "BirthDeathDate" from "jsonObjMap"**/
                jsonObjMap.remove(birthDeathDate_propName);

                int k = 3;//put json property value to the "dbRow" (from the 4th property of database, because 1st is id, the 2rd one is birth date and the 3rd one is death date)
                /**Preparing a hash map to store the record into mysql db...**/
                Iterator iterator2 = jsonObjMap.entrySet().iterator();
                while (iterator2.hasNext()) {
                    Map.Entry pair = (Map.Entry)iterator2.next();
                    String jsonProp = (String) pair.getKey();
                    String jsonValue = (String) pair.getValue();
                    dbRow.put(dbColumnNameList.get(k),jsonValue);
                    k++;
                }

                String sub_query_1 = "insert into " + tableName + " (";
                String sub_query_2 = ") values (";
                String sub_query_3 = ")";

                /**print the data to be filled into mysql...**/
                Iterator iterator3 = dbRow.entrySet().iterator();
                while (iterator3.hasNext()) {
                    Map.Entry pair = (Map.Entry) iterator3.next();
                    String columnName = (String) pair.getKey();
                    String columnValue = (String) pair.getValue();
                    System.out.println(columnName + " = " + columnValue);
                    sub_query_1 = sub_query_1.concat(columnName + ", ");
                    sub_query_2 = sub_query_2.concat("?, ");
                }
                String query = sub_query_1.concat(sub_query_2).concat(sub_query_3);

                /**filter out a comma and a space before ")"**/
                query = query.replace(", )", ")");

                System.out.println("The query sentence is: " + query);

                /**create mysql insert prepared statement**/
                int m = 1;
                PreparedStatement preparedStatement = conn.prepareStatement(query);
                Iterator iterator4 = dbRow.entrySet().iterator();
                while (iterator4.hasNext()) {
                    Map.Entry pair = (Map.Entry) iterator4.next();
                    String columnValue = (String) pair.getValue();
                    if (m == 1) {
                        preparedStatement.setInt(m, Integer.parseInt(columnValue));//the 1st column - "id"
                        System.out.println(Integer.parseInt(columnValue));
                    } else {
                        preparedStatement.setString(m, columnValue);//the other columns
                    }
                    m++;
                }

                System.out.println("begin to insert this json element into mysql db...");

                /**execute the prepared statement**/
                preparedStatement.execute();

                System.out.println("finish inserting " + count + " ELEMENT");
                System.out.println("----------------------------------");

                count++;
                preparedStatement.close();
            }

            conn.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (SQLException ex) {
            ex.printStackTrace();
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        }

    }


    class JSON2MysqlException extends Throwable{
        JSON2MysqlException(String string) {
            System.out.println(string);

        }

    }






}
