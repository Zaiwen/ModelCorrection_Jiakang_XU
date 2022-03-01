package au.com.d2dcrc.yago2es;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by Zaiwen FENG on 16/11/2017.
 * This new version aims to achieve multiple relationships between Person and City. I.e. 'diedIn' and 'wasBornIn'
 */
public class TestPrototypeByYago_V7 {

    @Test
    public void test() throws Exception {

        /**Phase 0: Prepare for the initial triples**/
        final YagoTtlParser parser = new YagoTtlParser();
        List<YagoFact> factsList = parser.parseYagoFacts("/Users/fengz/Documents/Data_Modeling/YAGO/RDF-Graph@27June17/yagoFacts_all.ttl");

        List<YagoFact> yagoFactList1 = new ArrayList<>();
        List<YagoFact> yagoFactList2 = new ArrayList<>();

        String relation1 = "directed";
        String relation2 = "wroteMusicFor";

        List<String> allRelations = new ArrayList<>();
        allRelations.add(relation1);
        allRelations.add(relation2);

        for (YagoFact yagoFact : factsList){

            if((yagoFact.getPredicate().equals(relation1)) && (yagoFactList1.size() <= 30)) {
                yagoFactList1.add(yagoFact);
            }

            else if((yagoFact.getPredicate().equals(relation2)) && (yagoFactList2.size() <= 30)) {
                yagoFactList2.add(yagoFact);
            }

        }

        /**Phase 1: Get the boundary graphs with different relationships**/

        /**create 40 boundary graphs for the relation1**/
        List<Graph> BoundaryGraphs1 = new ArrayList<>();

        for(YagoFact yagoFact : yagoFactList1) {

            Graph boundaryGraph = LinkedEntityGraph.createYagoBoundaryGraph(yagoFact,factsList,2, 5);
            BoundaryGraphs1.add(boundaryGraph);

        }

        /**Create 40 boundary graphs for the relation2**/
        List<Graph> BoundaryGraphs2 = new ArrayList<>();

        for(YagoFact yagoFact : yagoFactList2) {

            Graph boundaryGraph = LinkedEntityGraph.createYagoBoundaryGraph(yagoFact,factsList,2, 5);
            BoundaryGraphs2.add(boundaryGraph);
        }

        List<Graph> allBoundaryGraphs = new ArrayList<>();
        allBoundaryGraphs.addAll(BoundaryGraphs1);
        allBoundaryGraphs.addAll(BoundaryGraphs2);

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
        filePaths.add("/Users/fengz/Documents/Data_Modeling/YAGO/simple_taxonomy/yagoSimpleTypes_5.ttl");
        filePaths.add("/Users/fengz/Documents/Data_Modeling/YAGO/simple_taxonomy/yagoSimpleTypes_6.ttl");

        List<Graph> typedBoundaryGraphs = YagoTaxonomy.getYagoTypes(allBoundaryGraphs, filePaths, yagoTaxonomiesList);

        System.out.println("all the boundary graphs have been semantic annotated...");

        /**Phase 3 Acquire frequent sub-graph through gSpan**/
        /**numerate the labels of all the boundary graphs and save the mapping relations to a hashmap**/
        Map<String, Integer> numeratedVertexLabels =  LinkedEntityGraph.numerateVertexLables(allBoundaryGraphs);
        Map<String, Integer> numeratedEdgeLabels = LinkedEntityGraph.numerateEdgeLables(allBoundaryGraphs, allRelations);

        /**Print numberated edge label and vertex label to files, newly added on 13 Aug 2017**/
        Util.printNumeratedLabelsToFile("/Users/fengz/Project/parsemis/Yago_numeratedVertex.lg", numeratedVertexLabels);
        Util.printNumeratedLabelsToFile("/Users/fengz/Project/parsemis/Yago_numeratedEdge.lg", numeratedEdgeLabels);

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

        List<String> newEdgeInfo1 = new ArrayList<>();//For the relation1
        newEdgeInfo1.add("<wordnet_person_100007846>");
        newEdgeInfo1.add("<wordnet_movie_106613686>");//maybe unused.
        newEdgeInfo1.add(allRelations.get(0));//label of BG
        newEdgeInfo1.add("X");//common relation in BG

        List<String> newEdgeInfo2 = new ArrayList<>(); //For the relation2
        newEdgeInfo2.add("<wordnet_person_100007846>");
        newEdgeInfo2.add("<wordnet_movie_106613686>");//maybe unused.
        newEdgeInfo2.add(allRelations.get(1));////label of BG
        newEdgeInfo2.add("X");//common relation in BG

        Util.printEdgeToFile(newEdgeInfo1, "/Users/fengz/Project/parsemis/Yago_edge_RT_1.lg", numeratedVertexLabels, numeratedEdgeLabels);
        Util.printEdgeToFile(newEdgeInfo2, "/Users/fengz/Project/parsemis/Yago_edge_RT_2.lg", numeratedVertexLabels, numeratedEdgeLabels);

        /**Renumber the vertices and edges of the boundary graphs with relationship type 1**/
        Iterator<Graph> iterator = BoundaryGraphs1.iterator();
        while (iterator.hasNext()){

            Graph boundaryGraph = iterator.next();
            boundaryGraph.reOrderLabel(numeratedVertexLabels,numeratedEdgeLabels,boundaryGraph);
        }

        GSpanWrapper.writeToInputFile(BoundaryGraphs1,  "/Users/fengz/Project/parsemis/Yago_RT_1.lg");//Numerated boundary graphs for relationship type 1
        System.out.println(" Phase 3: gSpan input file for the Yago RT1 has been prepared...");

        /**Renumber the vertices and edges of the boundary graphs with relationship type 2**/
        Iterator<Graph> iterator2 = BoundaryGraphs2.iterator();
        while (iterator2.hasNext()){

            Graph boundaryGraph = iterator2.next();
            boundaryGraph.reOrderLabel(numeratedVertexLabels,numeratedEdgeLabels,boundaryGraph);
        }

        GSpanWrapper.writeToInputFile(BoundaryGraphs2,  "/Users/fengz/Project/parsemis/Yago_RT_2.lg");//Numerated boundary graphs for the relationship type 2
        System.out.println(" Phase 3: gSpan input file for the RT 2 has been prepared...");

        /******gSpan Execution!***********/




    }

}
