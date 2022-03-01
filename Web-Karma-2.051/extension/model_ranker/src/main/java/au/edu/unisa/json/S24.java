package au.edu.unisa.json;

import au.edu.unisa.Settings;
import au.edu.unisa.json.JSON2MysqlAdaptor;

public class S24 {

    public static void ingest () {

        JSON2MysqlAdaptor json2MysqlAdaptor = new JSON2MysqlAdaptor();
        json2MysqlAdaptor.addJsonProp("access");
        json2MysqlAdaptor.addJsonProp("dim");
        json2MysqlAdaptor.addJsonProp("artist_period");
        json2MysqlAdaptor.addJsonProp("made");
        json2MysqlAdaptor.addJsonProp("prov");
        json2MysqlAdaptor.addJsonProp("title");
        json2MysqlAdaptor.addJsonProp("nationality");
        json2MysqlAdaptor.addJsonProp("artist");
        json2MysqlAdaptor.addJsonProp("image_url");

        json2MysqlAdaptor.addDBColumnNameList("id");
        json2MysqlAdaptor.addDBColumnNameList("birth_date");
        json2MysqlAdaptor.addDBColumnNameList("death_date");
        json2MysqlAdaptor.addDBColumnNameList("access");
        json2MysqlAdaptor.addDBColumnNameList("dimension");
        json2MysqlAdaptor.addDBColumnNameList("made");
        json2MysqlAdaptor.addDBColumnNameList("provider");
        json2MysqlAdaptor.addDBColumnNameList("title");
        json2MysqlAdaptor.addDBColumnNameList("nationality");
        json2MysqlAdaptor.addDBColumnNameList("artist");
        json2MysqlAdaptor.addDBColumnNameList("image_url");

        try {
            json2MysqlAdaptor.transform(Settings.MUSEUM_CRM_DS_S24,"s24", "artist_period");

        } catch (JSON2MysqlAdaptor.JSON2MysqlException ex) {

            ex.printStackTrace();
        }


    }

    public static void main (String args[]) {
        ingest();
    }



}
