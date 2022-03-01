package au.com.d2dcrc.yago2es;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Zaiwen FENG on 30/06/2017.
 */
public class TestParsingYagoTypes {


    public void testParsingYagoTypes () throws Exception {

        final YagoTtlParser parser = new YagoTtlParser();

        /**Get info for Yago relationship types**/
        List<YagoTtlParser.RelationValue> yagoTypesList = parser.parseYagoSimpleTypes("/Users/fengz/Documents/Data_Modeling/YAGO/simple_taxonomy/yagoSimpleTypes.ttl");

        System.out.println("done!");
    }

    @Test
    public void testGetYagoTypes () throws Exception {

        List<String> yagoTypes = new ArrayList<>();

        List<String> yagoTypes1 = YagoTaxonomy.getYagoTypes("Princeton_Theological_Seminary" ,"/Users/fengz/Documents/Data_Modeling/YAGO/simple_taxonomy/yagoSimpleTypes_1.ttl");
        List<String> yagoTypes2 = YagoTaxonomy.getYagoTypes("Princeton_Theological_Seminary" ,"/Users/fengz/Documents/Data_Modeling/YAGO/simple_taxonomy/yagoSimpleTypes_2.ttl");
        List<String> yagoTypes3 = YagoTaxonomy.getYagoTypes("Princeton_Theological_Seminary" ,"/Users/fengz/Documents/Data_Modeling/YAGO/simple_taxonomy/yagoSimpleTypes_3.ttl");

        yagoTypes.addAll(yagoTypes1);
        yagoTypes.addAll(yagoTypes2);
        yagoTypes.addAll(yagoTypes3);

        for (String yagoType : yagoTypes) {


            System.out.println(yagoType);
        }



    }

    public void testGetYagoTypes1 () throws Exception {

        List<String> yagoTypes = YagoTaxonomy.getYagoTypes("Endtroducing....." ,"/Users/fengz/Documents/Data_Modeling/YAGO/simple_taxonomy/yagoSimpleTypes.ttl");

        for (String yagoType : yagoTypes) {


            System.out.println(yagoType);
        }



    }

}
