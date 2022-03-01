package au.com.d2dcrc.yago2es;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by Zaiwen FENG on 1/08/2017.
 */
public class TestPrototypeByYago_V4 {

    @Test
    public void test() throws Exception {

        /**Phase 0: Prepare for the initial triples**/
        final YagoTtlParser parser = new YagoTtlParser();
        List<YagoFact> factsList = parser.parseYagoFacts("/Users/fengz/Documents/Data_Modeling/YAGO/RDF-Graph@27June17/yagoFacts_all.ttl");

        List<YagoFact> positiveFacts = new ArrayList<>();
        List<YagoFact> negativeFacts = new ArrayList<>();

        String positiveRelation = "influences";
        String negativeRelation = "hasAcademicAdvisor";

        for (YagoFact yagoFact : factsList){

            if((yagoFact.getPredicate().equals(positiveRelation)) && (positiveFacts.size() <= 39)) {
                positiveFacts.add(yagoFact);
            }

            else if((yagoFact.getPredicate().equals(negativeRelation)) && (negativeFacts.size() <= 39)) {
                negativeFacts.add(yagoFact);
            }
        }

        /**Phase 1: Get the boundary graphs with relationships 'influences' and 'isMarriedTo'**/

        /**create positive boundary graphs +40**/
        List<Graph> positiveBoundaryGraphs = new ArrayList<>();

        for(YagoFact yagoFact : positiveFacts) {

            Graph boundaryGraph = LinkedEntityGraph.createYagoBoundaryGraph(yagoFact,factsList,2, 10);
            positiveBoundaryGraphs.add(boundaryGraph);

        }

        /**Create negative boundary graphs -40**/
        List<Graph> negativeBoundaryGraphs = new ArrayList<>();

        for(YagoFact yagoFact : negativeFacts) {

            Graph boundaryGraph = LinkedEntityGraph.createYagoBoundaryGraph(yagoFact,factsList,2, 10);
            negativeBoundaryGraphs.add(boundaryGraph);
        }

        List<Graph> allBoundaryGraphs = new ArrayList<>();
        allBoundaryGraphs.addAll(positiveBoundaryGraphs);
        allBoundaryGraphs.addAll(negativeBoundaryGraphs);

        System.out.println("Boundary graphs has been generated! ");

        /**Phase 2: Semantic typing Yago instances**/

        /**Then specially, in terms of yagoSchema.ttl, each entities must be added label name based on instance name. This procedure is special for using Yago data**/
        /**Phase 2.0: Get the taxonomy tree of Yago & relationship schema type of Yago**/
        List<YagoTtlParser.RelationValue> yagoTaxonomiesList = parser.parseYagoTaxonomy("/Users/fengz/Documents/Data_Modeling/YAGO/simple_taxonomy/yagoTaxonomy.ttl");

        List<String> filePaths = new ArrayList<>();
        filePaths.add("/Users/fengz/Documents/Data_Modeling/YAGO/simple_taxonomy/yagoSimpleTypes_1.ttl");
        filePaths.add("/Users/fengz/Documents/Data_Modeling/YAGO/simple_taxonomy/yagoSimpleTypes_2.ttl");
        filePaths.add("/Users/fengz/Documents/Data_Modeling/YAGO/simple_taxonomy/yagoSimpleTypes_3.ttl");
        filePaths.add("/Users/fengz/Documents/Data_Modeling/YAGO/simple_taxonomy/yagoSimpleTypes_4.ttl");

        List<Graph> typedBoundaryGraphs = YagoTaxonomy.getYagoTypes(allBoundaryGraphs, filePaths, yagoTaxonomiesList);

        System.out.println("all the boundary graphs have been semantic annotated...");

        /**Phase 3 Acquire frequent sub-graph through gSpan**/
        /**numerate the labels of all the boundary graphs and save the mapping relations to a hashmap**/
        Map<String, Integer> numeratedVertexLabels =  LinkedEntityGraph.numerateVertexLables(allBoundaryGraphs);
        Map<String, Integer> numeratedEdgeLabels = LinkedEntityGraph.numerateEdgeLables(allBoundaryGraphs, positiveRelation, negativeRelation);

        Iterator<Map.Entry<String, Integer>> numeratedVertexIterator = numeratedVertexLabels.entrySet().iterator();
        while (numeratedVertexIterator.hasNext()) {
            Map.Entry<String, Integer> entry = numeratedVertexIterator.next();
            System.out.println("Key = " + entry.getKey() + ", Value= " +entry.getValue());
        }

        Iterator<Map.Entry<String, Integer>> numeratedEdgeIterator = numeratedEdgeLabels.entrySet().iterator();
        while (numeratedEdgeIterator.hasNext()) {
            Map.Entry<String, Integer> entry = numeratedEdgeIterator.next();
            System.out.println("Key = " + entry.getKey() + ", Value= " +entry.getValue());
        }


        System.out.println("vertices and edges numerated...");

        List<String> newEdgeInfo = new ArrayList<>();//For the edge 'isMarriedTo'
        newEdgeInfo.add("<wordnet_person_100007846>");
        newEdgeInfo.add("<wordnet_person_100007846>");
        newEdgeInfo.add(positiveRelation);

        List<String> newEdgeInfo1 = new ArrayList<>(); //For the edge 'influences'
        newEdgeInfo1.add("<wordnet_person_100007846>");
        newEdgeInfo1.add("<wordnet_person_100007846>");
        newEdgeInfo1.add(negativeRelation);

        Util.printEdgeToFile(newEdgeInfo, "/Users/fengz/Project/parsemis/Yago_edge_RT.lg", numeratedVertexLabels, numeratedEdgeLabels);
        Util.printEdgeToFile(newEdgeInfo1, "/Users/fengz/Project/parsemis/Yago_edge_non_RT.lg", numeratedVertexLabels, numeratedEdgeLabels);

        /**Renumber the vertices and edges of the boundary graphs with relationship 'influences**/
        Iterator<Graph> iterator = positiveBoundaryGraphs.iterator();
        while (iterator.hasNext()){

            Graph boundaryGraph = iterator.next();
            boundaryGraph.reOrderLabel(numeratedVertexLabels,numeratedEdgeLabels,boundaryGraph);
        }

        GSpanWrapper.writeToInputFile(positiveBoundaryGraphs,  "/Users/fengz/Project/parsemis/Yago_RT.lg");//Numerated boundary graphs for relationship type "influences"
        System.out.println("Phase 3: gSpan input file for the Yago RT has been prepared...");

        /**Renumber the vertices and edges of the boundary graphs with relationship 'isMarriedTo'**/
        Iterator<Graph> iterator1 = negativeBoundaryGraphs.iterator();
        while (iterator1.hasNext()){

            Graph boundaryGraph1 = iterator1.next();
            boundaryGraph1.reOrderLabel(numeratedVertexLabels,numeratedEdgeLabels,boundaryGraph1);
        }

        GSpanWrapper.writeToInputFile(negativeBoundaryGraphs,  "/Users/fengz/Project/parsemis/Yago_non_RT.lg");//Numerated boundary graphs for the relationship type 'isMarriedTo'
        System.out.println("Phase 3: gSpan input file for the non-RT has been prepared...");

        /******gSpan Execution!***********/




    }

}
