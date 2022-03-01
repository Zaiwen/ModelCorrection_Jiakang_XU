package au.com.d2dcrc.yago2es;

import org.junit.Test;

import java.util.*;

/**
 * Created by Zaiwen Feng on 4/08/2017.
 * The version aims to achieve multiple distinction of relationships
 */
public class TestPrototypeByYago_Phase4_V3 {

    @Test
    public void TestPrototype () throws Exception {

        /**----------Phase 4: Minimize feature set---------------------**/
        FeatureSelector featureSelector = new FeatureSelector();
        /**Get all of the frequent sub-graphs of boundary graphs with 'RT'**/
        List<Graph> allFrequentGraphs_RT1 = GSpanWrapper.loadGraph("/Users/fengz/Project/parsemis/Yago_RT_1_subgraphs.txt");
        /**Get the info of the edge that needs to be contained in the graph**/
        List<Integer> edgeInfo_RT_1 = Util.readEdgeFromFile("/Users/fengz/Project/parsemis/Yago_edge_RT_1.lg");
        /**Get the weight info of the edge with RT**/
        int weight_RT1 = edgeInfo_RT_1.get(2);
        /**Next, we just extract the sub-graphs that contains starting point and end point of the edge with relationship type**/
        //List<Graph> usefulFrequentGraphs_RT1 = featureSelector.removeUselessFrequentGraph3(allFrequentGraphs_RT1, edgeInfo_RT_1);
        List<Graph> usefulFrequentGraphs_RT1 = allFrequentGraphs_RT1;

        /**Get all of the frequent sub-graphs of boundary graphs with 'RT2'**/
        List<Graph> allFrequentGraphs_RT2 = GSpanWrapper.loadGraph("/Users/fengz/Project/parsemis/Yago_RT_2_subgraphs.txt");
        /**Get the info of the edge that needs to be contained in the graph**/
        List<Integer> edgeInfo_RT_2 = Util.readEdgeFromFile("/Users/fengz/Project/parsemis/Yago_edge_RT_2.lg");
        /**Get the weight info of the edge with RT**/
        int weight_RT2 = edgeInfo_RT_2.get(2);
        /**Next, we just extract the sub-graphs that contains the starting point and the end point of the edge with relationship type**/
        //List<Graph> usefulFrequentGraphs_RT2 = featureSelector.removeUselessFrequentGraph3(allFrequentGraphs_RT2, edgeInfo_RT_2);
        List<Graph> usefulFrequentGraphs_RT2 = allFrequentGraphs_RT2;

        /**Get all of the frequent sub-graphs of boundary graphs with 'RT3'**/
        List<Graph> allFrequentGraphs_RT3 = GSpanWrapper.loadGraph("/Users/fengz/Project/parsemis/Yago_RT_3_subgraphs.txt");
        /**Get the info of the edge that needs to be contained in the graph**/
        List<Integer> edgeInfo_RT_3 = Util.readEdgeFromFile("/Users/fengz/Project/parsemis/Yago_edge_RT_3.lg");
        /**Get the weight info of the edge with RT**/
        int weight_RT3 = edgeInfo_RT_3.get(2);
        /**Next, we just extract the sub-graphs that contains the starting point and the end point of the edge with relationship type**/
        //List<Graph> usefulFrequentGraphs_RT3 = featureSelector.removeUselessFrequentGraph3(allFrequentGraphs_RT3, edgeInfo_RT_3);
        List<Graph> usefulFrequentGraphs_RT3 = allFrequentGraphs_RT3;

        /**Get all of the frequent sub-graphs of boundary graphs with 'RT4'**/
        List<Graph> allFrequentGraphs_RT4 = GSpanWrapper.loadGraph("/Users/fengz/Project/parsemis/Yago_RT_4_subgraphs.txt");
        /**Get the info of the edge that needs to be contained in the graph**/
        List<Integer> edgeInfo_RT_4 = Util.readEdgeFromFile("/Users/fengz/Project/parsemis/Yago_edge_RT_4.lg");
        /**Get the weight info of the edge with RT**/
        int weight_RT4 = edgeInfo_RT_4.get(2);
        /**Next, we just extract the sub-graphs that contains the starting point and the end point of the edge with relationship type**/
        //List<Graph> usefulFrequentGraphs_RT3 = featureSelector.removeUselessFrequentGraph3(allFrequentGraphs_RT3, edgeInfo_RT_3);
        List<Graph> usefulFrequentGraphs_RT4 = allFrequentGraphs_RT4;


        /**Build a subgraph matcher to compare two graphs**/
        SubgraphMatcher subgraphMatcher = new SubgraphMatcher();

        List<Graph> union1 = subgraphMatcher.getUnionOfGraphSet(usefulFrequentGraphs_RT1, usefulFrequentGraphs_RT2);
        List<Graph> union2 = subgraphMatcher.getUnionOfGraphSet(union1, usefulFrequentGraphs_RT3);
        List<Graph> union3 = subgraphMatcher.getUnionOfGraphSet(union2, usefulFrequentGraphs_RT4);


//        List<Graph> commmonFeatureSet = new ArrayList<Graph>();//not useful, but for debugging
//        List<Graph> usefulFrequentGraphs_RT1_cy = new ArrayList<>(usefulFrequentGraphs_RT1);
//        List<Graph> usefulFrequentGraphs_RT2_cy = new ArrayList<>(usefulFrequentGraphs_RT2);


//        /**Get the common set of 'usefulFrequentGraphs' and 'usefulFrequentGraphs_non_RT'**/
//        for (Graph usefulFrequentGraph_RT1 : usefulFrequentGraphs_RT1) {
//
//            for (Graph usefulFrequentGraph_RT2 : usefulFrequentGraphs_RT2) {
//
//                if (subgraphMatcher.compare(usefulFrequentGraph_RT1, usefulFrequentGraph_RT2)) {
//
//                    commmonFeatureSet.add(usefulFrequentGraph_RT1);
//                }
//
//            }
//        }
//
//                 /* A-B
//         * **/
//        Iterator<Graph> iterator = usefulFrequentGraphs_RT1.iterator();
//        while (iterator.hasNext()){
//            Graph graph_with_RT = iterator.next();
//
//            for(Graph commonGraph : commmonFeatureSet){
//
//                boolean result = subgraphMatcher.compare(graph_with_RT, commonGraph);
//
//                if(result){
//
//                    iterator.remove();
//                    break;
//                }
//            }
//        }
//
//        /**B-A**/
//        Iterator<Graph> iterator1 = usefulFrequentGraphs_RT2_cy.iterator();
//        while (iterator1.hasNext()){
//
//            Graph graph_with_non_RT = iterator1.next();
//
//            for (Graph commmonGraph : commmonFeatureSet) {
//
//                boolean result = subgraphMatcher.compare(graph_with_non_RT, commmonGraph);
//
//                if (result) {
//
//                    iterator1.remove();
//                    break;
//                }
//            }
//        }

//         /* A-B
//         * **/
//        Iterator<Graph> iterator = usefulFrequentGraphs_RT.iterator();
//        while (iterator.hasNext()){
//            Graph graph_with_RT = iterator.next();
//
//            for(Graph usefulFrequentGraph_non_RT : usefulFrequentGraphs_non_RT){
//
//                boolean result = graph_with_RT.compare(usefulFrequentGraph_non_RT);
//
//                if(result){
//
//                    iterator.remove();
//                    break;
//                }
//            }
//        }
//
//        /**B-A**/
//        Iterator<Graph> iterator1 = usefulFrequentGraphs_non_RT_cy.iterator();
//        while (iterator1.hasNext()){
//
//            Graph graph_with_non_RT = iterator1.next();
//
//            for (Graph usefulFrequentGraph_RT : usefulFrequentGraphs_RT_cy) {
//
//                boolean result = graph_with_non_RT.compare(usefulFrequentGraph_RT);
//
//                if (result) {
//
//                    iterator1.remove();
//                    break;
//                }
//            }
//        }


//
//        List<Graph> discriminativeFeatureSet_positive = new ArrayList<Graph>(usefulFrequentGraphs_RT1);
//        List<Graph> discriminativeFeatureSet_negative = new ArrayList<Graph>(usefulFrequentGraphs_RT2_cy);
//        List<Graph> discriminativeFeatureSet = new ArrayList<>(discriminativeFeatureSet_positive);
//        discriminativeFeatureSet.addAll(discriminativeFeatureSet_negative);

        /**filter out the graph with the number vertex is more than 11 and the number of vertex is more than 10 **/
//        Iterator<Graph> its = discriminativeFeatureSet.iterator();
//        while (its.hasNext()) {
//
//            Graph graph = its.next();
//            if (graph.getAllEdges().size() > 6) {
//
//                its.remove();
//            }
//        }

        System.out.println("Discriminative feature set has been created...");

        /**----------Phase 5: Match each subgraph to a boundary graph with a specified relationship---------------------**/
        /**Load boundary graphs**/
        List<Graph> boundaryGraphs_RT1 = GSpanWrapper.loadGraph("/Users/fengz/Project/parsemis/Yago_RT_1.lg");
        List<Graph> boundaryGraphs_RT2 = GSpanWrapper.loadGraph("/Users/fengz/Project/parsemis/Yago_RT_2.lg");
        List<Graph> boundaryGraphs_RT3 = GSpanWrapper.loadGraph("/Users/fengz/Project/parsemis/Yago_RT_3.lg");
        List<Graph> boundaryGraphs_RT4 = GSpanWrapper.loadGraph("/Users/fengz/Project/parsemis/Yago_RT_4.lg");

        /**Create a hashmap to contain boundary graphs (Positive & Negative training set) and its corresponding label**/
        Map<Graph, Integer> boundaryGraphs_RT1_Map = new HashMap<Graph, Integer>();
        Map<Graph, Integer> boundaryGraphs_RT2_Map = new HashMap<Graph, Integer>();
        Map<Graph, Integer> boundaryGraphs_RT3_Map = new HashMap<Graph, Integer>();
        Map<Graph, Integer> boundaryGraphs_RT4_Map = new HashMap<Graph, Integer>();

        for(Graph boundaryGraph : boundaryGraphs_RT1){

            boundaryGraphs_RT1_Map.put(boundaryGraph, weight_RT1);//weight_RT1 will be the label for the boundary graph
        }

        for(Graph boundaryGraph : boundaryGraphs_RT2){

            boundaryGraphs_RT2_Map.put(boundaryGraph, weight_RT2);//weight_RT2 will be the label for the boundary graph
        }

        for(Graph boundaryGraph : boundaryGraphs_RT3){

            boundaryGraphs_RT3_Map.put(boundaryGraph, weight_RT3);//weight_RT3 will be the label for the boundary graph
        }

        for(Graph boundaryGraph : boundaryGraphs_RT4){

            boundaryGraphs_RT4_Map.put(boundaryGraph, weight_RT4);//weight_RT4 will be the label for the boundary graph
        }

        Map<Graph, Integer> allLabeledBoundaryGraphs = new HashMap<Graph, Integer>();
        allLabeledBoundaryGraphs.putAll(boundaryGraphs_RT1_Map);
        allLabeledBoundaryGraphs.putAll(boundaryGraphs_RT2_Map);
        allLabeledBoundaryGraphs.putAll(boundaryGraphs_RT3_Map);
        allLabeledBoundaryGraphs.putAll(boundaryGraphs_RT4_Map);

        /**Create a new subgraph matcher for training**/
        SubgraphMatcher matcher = new SubgraphMatcher(allLabeledBoundaryGraphs, union3 );

        List<Graph> subGraphs = matcher.getSubgraphs();
        Map<Graph, Integer> labeledGraphs = matcher.getLabeledGraphs();

        int[][] trainingMatrix = matcher.acquireMatrix(subGraphs, labeledGraphs);

        /**Write training matrix to csv file**/
        try {

            Util.writeMatrixToCSV(trainingMatrix, "/Users/fengz/Documents/Data_Modeling/YAGO/RDF-Graph@27June17/trainingMatrix.csv", 0);
        }catch (Exception ex){

            ex.printStackTrace();
        }

        System.out.println("Subgraph matching ends...");
        System.out.println(union3.size() + " discriminative features are used.");
    }
}
