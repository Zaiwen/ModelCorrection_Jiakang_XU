package au.com.d2dcrc.yago2es;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Zaiwen FENG on 30/06/2017.
 */
public class TestParsingYagoTaxonomies {

    public void testParsingYagoTaxonomies () throws Exception {

        final YagoTtlParser parser = new YagoTtlParser();

        /**Get info for Yago relationship types**/
        List<YagoTtlParser.RelationValue> yagoTaxonomiesList = parser.parseYagoTaxonomy("/Users/fengz/Documents/Data_Modeling/YAGO/simple_taxonomy/yagoTaxonomy.ttl");

        //String superClass = YagoTaxonomy.getSuperClass("<wordnet_football_player_110101634>","<wordnet_person_100007846>",  yagoTaxonomiesList);

        String superClass = YagoTaxonomy.getSuperClass("<yagoLegalActorGeo>","<yagoPermanentlyLocatedEntity>",  yagoTaxonomiesList);

        System.out.println("The super class is: " + superClass);
    }

    public void testGettingSuperClass () throws Exception {

        final YagoTtlParser parser = new YagoTtlParser();

        /**Get info for Yago relationship types**/
        List<YagoTtlParser.RelationValue> yagoTaxonomiesList = parser.parseYagoTaxonomy("/Users/fengz/Documents/Data_Modeling/YAGO/simple_taxonomy/yagoTaxonomy.ttl");

        List<String> concepts = new ArrayList<>();

        concepts.add("<owl:Thing>");
        concepts.add("<yagoPermanentlyLocatedEntity>");

        String superClass = YagoTaxonomy.getSuperClass(concepts, yagoTaxonomiesList);

        System.out.println("The super class is: " + superClass);

    }


    public void testGetAllSuperClass () throws Exception {

        final YagoTtlParser parser = new YagoTtlParser();

        /**Get info for Yago relationship types**/
        List<YagoTtlParser.RelationValue> yagoTaxonomiesList = parser.parseYagoTaxonomy("/Users/fengz/Documents/Data_Modeling/YAGO/simple_taxonomy/yagoTaxonomy.ttl");

        String concept = "<wikicat_Beninese_Roman_Catholic_archbishops>";

        List<String> allSuperClass = YagoTaxonomy.getAllSuperClasses(concept, yagoTaxonomiesList);

        for (String superClass : allSuperClass) {

            System.out.println(superClass);
        }
    }

    @Test
    public void testGetYagoUniqueType () throws Exception {

        String uniqueType = "";

        final YagoTtlParser parser = new YagoTtlParser();

        List<String> allTypeList = new ArrayList<>();
        List<String> typeList1 = YagoTaxonomy.getYagoTypes("Princeton_Theological_Seminary", "/Users/fengz/Documents/Data_Modeling/YAGO/simple_taxonomy/yagoSimpleTypes_1.ttl");
        List<String> typeList2 = YagoTaxonomy.getYagoTypes("Princeton_Theological_Seminary", "/Users/fengz/Documents/Data_Modeling/YAGO/simple_taxonomy/yagoSimpleTypes_2.ttl");
        List<String> typeList3 = YagoTaxonomy.getYagoTypes("Princeton_Theological_Seminary", "/Users/fengz/Documents/Data_Modeling/YAGO/simple_taxonomy/yagoSimpleTypes_3.ttl");
        allTypeList.addAll(typeList1);
        allTypeList.addAll(typeList2);
        allTypeList.addAll(typeList3);

                /**Get info for Yago relationship types**/
        List<YagoTtlParser.RelationValue> yagoTaxonomiesList = parser.parseYagoTaxonomy("/Users/fengz/Documents/Data_Modeling/YAGO/simple_taxonomy/yagoTaxonomy.ttl");

        uniqueType = YagoTaxonomy.getYagoUniqueType("Princeton_Theological_Seminary", allTypeList, yagoTaxonomiesList);


        System.out.println(uniqueType);
    }


}
