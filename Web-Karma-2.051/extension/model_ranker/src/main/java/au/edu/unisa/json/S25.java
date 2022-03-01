package au.edu.unisa.json;

import au.edu.unisa.Settings;
import au.edu.unisa.json.JSON2MysqlAdaptor;

public class S25 {

    /**a new implementation for ingesting data s25*
     * @from 27 April 2020
     * */
    public static void ingest () {

        JSON2MysqlAdaptor json2MysqlAdaptor = new JSON2MysqlAdaptor();//construct an adaptor
        json2MysqlAdaptor.addJsonProp("BirthDeathDate");//value needs to be split
        json2MysqlAdaptor.addJsonProp("Subtype_of_Art");
        json2MysqlAdaptor.addJsonProp("Type_of_Art");
        json2MysqlAdaptor.addJsonProp("Title");
        json2MysqlAdaptor.addJsonProp("Date_made");
        json2MysqlAdaptor.addJsonProp("Signed");
        json2MysqlAdaptor.addJsonProp("Accessories");
        json2MysqlAdaptor.addJsonProp("Credit");
        json2MysqlAdaptor.addJsonProp("ArtistName");
        json2MysqlAdaptor.addJsonProp("Keywords");
        json2MysqlAdaptor.addJsonProp("Inscription");
        json2MysqlAdaptor.addJsonProp("Accession_id");
        json2MysqlAdaptor.addJsonProp("Pic_URL");
        json2MysqlAdaptor.addJsonProp("Dimensions");

        json2MysqlAdaptor.addDBColumnNameList("id");
        json2MysqlAdaptor.addDBColumnNameList("birth_date");//split birth date
        json2MysqlAdaptor.addDBColumnNameList("death_date");//split death date
        json2MysqlAdaptor.addDBColumnNameList("subtype_of_art");
        json2MysqlAdaptor.addDBColumnNameList("type_of_art");
        json2MysqlAdaptor.addDBColumnNameList("title");
        json2MysqlAdaptor.addDBColumnNameList("date_made");
        json2MysqlAdaptor.addDBColumnNameList("signed");
        json2MysqlAdaptor.addDBColumnNameList("accessories");
        json2MysqlAdaptor.addDBColumnNameList("credit");
        json2MysqlAdaptor.addDBColumnNameList("artist_name");
        json2MysqlAdaptor.addDBColumnNameList("key_words");
        json2MysqlAdaptor.addDBColumnNameList("inscription");
        json2MysqlAdaptor.addDBColumnNameList("accession_id");
        json2MysqlAdaptor.addDBColumnNameList("pic_url");
        json2MysqlAdaptor.addDBColumnNameList("dimensions");

        json2MysqlAdaptor.addDBColumnNameList("person_uri(fake)");
        json2MysqlAdaptor.addDBColumnNameList("birth_uri(fake)");
        json2MysqlAdaptor.addDBColumnNameList("death_date(fake)");



        try {
            json2MysqlAdaptor.transform(Settings.MUSEUM_CRM_DS_S25, "s25", "BirthDeathDate");
        } catch (JSON2MysqlAdaptor.JSON2MysqlException ex) {

            ex.printStackTrace();

        }
    }



    public static void main (String args[]) {
        ingest();
    }
}
