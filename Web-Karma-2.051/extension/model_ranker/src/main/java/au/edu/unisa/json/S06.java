package au.edu.unisa.json;

public class S06 {

    /**ingest s06**/
    public static void ingest () {

        JSON2MysqlAdaptor json2MysqlAdaptor = new JSON2MysqlAdaptor();//build a constructor
        json2MysqlAdaptor.addJsonProp("Title");
        json2MysqlAdaptor.addJsonProp("Artist");
        json2MysqlAdaptor.addJsonProp("CopyAfter");
        json2MysqlAdaptor.addJsonProp("Copyright");
        json2MysqlAdaptor.addJsonProp("Classification");
        json2MysqlAdaptor.addJsonProp("Medium");
        json2MysqlAdaptor.addJsonProp("Dimension");
        json2MysqlAdaptor.addJsonProp("Owner");
        json2MysqlAdaptor.addJsonProp("AquisitionDate");
        json2MysqlAdaptor.addJsonProp("Image");
        json2MysqlAdaptor.addJsonProp("Description");
        json2MysqlAdaptor.addJsonProp("DateOfWork");
        json2MysqlAdaptor.addJsonProp("CreditLine");
        json2MysqlAdaptor.addJsonProp("Ref");
        json2MysqlAdaptor.addJsonProp("ArtistBornDiedDate");
        json2MysqlAdaptor.addJsonProp("CopyAfterDate");
        json2MysqlAdaptor.addJsonProp("Sitter");
        json2MysqlAdaptor.addJsonProp("SitterBornDiedDate");
        json2MysqlAdaptor.addJsonProp("Keywords");

        json2MysqlAdaptor.addDBColumnNameList("id");
        json2MysqlAdaptor.addDBColumnNameList("birth_date");
        json2MysqlAdaptor.addDBColumnNameList("death_date");
        json2MysqlAdaptor.addDBColumnNameList("title");
        json2MysqlAdaptor.addDBColumnNameList("artist");
        json2MysqlAdaptor.addDBColumnNameList("copy_after");
        json2MysqlAdaptor.addDBColumnNameList("copyright");
        json2MysqlAdaptor.addDBColumnNameList("classification");
        json2MysqlAdaptor.addDBColumnNameList("medium");
        json2MysqlAdaptor.addDBColumnNameList("dimension");
        json2MysqlAdaptor.addDBColumnNameList("owner");
        json2MysqlAdaptor.addDBColumnNameList("aquisition_date");
        json2MysqlAdaptor.addDBColumnNameList("image");
        json2MysqlAdaptor.addDBColumnNameList("description");
        json2MysqlAdaptor.addDBColumnNameList("date_of_work");
        json2MysqlAdaptor.addDBColumnNameList("credit_line");
        json2MysqlAdaptor.addDBColumnNameList("reference");
        json2MysqlAdaptor.addDBColumnNameList("copy_after_date");
        json2MysqlAdaptor.addDBColumnNameList("sitter");
        json2MysqlAdaptor.addDBColumnNameList("sitter_born_death_date");
        json2MysqlAdaptor.addDBColumnNameList("keywords");




    }




}
