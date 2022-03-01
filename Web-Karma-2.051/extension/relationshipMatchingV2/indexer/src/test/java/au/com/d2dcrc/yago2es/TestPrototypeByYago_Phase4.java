package au.com.d2dcrc.yago2es;

import org.junit.Test;

import java.util.*;

/**
 * Created by Zaiwen Feng on 17/07/2017.
 */
public class TestPrototypeByYago_Phase4 {

    @Test
    public void TestPrototype () throws Exception {

        /**----------Phase 4: Minimize feature set---------------------**/
        FeatureSelector featureSelector = new FeatureSelector();
        /**Get all of the frequent sub-graphs of boundary graphs with 'RT'**/
        List<Graph> allFrequentGraphs = GSpanWrapper.loadGraph("/Users/fengz/Project/parsemis/Yago_RT_subgraphs.txt");
        /**Get the info of the edge that needs to be contained in the graph**/
        List<Integer> edgeInfo = Util.readEdgeFromFile("/Users/fengz/Project/parsemis/Yago_edge_RT.lg");
        /**Get the weight info of the edge with RT**/
        int weight_RT = edgeInfo.get(2);
        /**Next, we just extract the sub-graphs that contains the edge with relationship type**/
        List<Graph> usefulFrequentGraphs_RT = featureSelector.removeUselessFrequentGraph(allFrequentGraphs, edgeInfo);

        /**Get all frequent sub-graphs of boundary graphs with 'non-RT'**/
        List<Graph> allFrequentGraphs_non_RT = GSpanWrapper.loadGraph("/Users/fengz/Project/parsemis/Yago_non_RT_subgraphs.txt");
        /**Get the info of the edge that needs to be contained in the graph**/
        List<Integer> edgeInfo_non_RT = Util.readEdgeFromFile("/Users/fengz/Project/parsemis/Yago_edge_non_RT.lg");
        /**Get the weight info of the edge with non-RT**/
        int weight_non_RT = edgeInfo_non_RT.get(2);
        /**Next, we just extract the sub-graphs that contains the edge with relationship type**/

        List<Graph> usefulFrequentGraphs_non_RT = featureSelector.removeUselessFrequentGraph(allFrequentGraphs_non_RT, edgeInfo_non_RT);

        List<Graph> commmonFeatureSet = new ArrayList<Graph>();//not useful, but for debugging
        List<Graph> usefulFrequentGraphs_RT_cy = new ArrayList<>(usefulFrequentGraphs_RT);
        List<Graph> usefulFrequentGraphs_non_RT_cy = new ArrayList<>(usefulFrequentGraphs_non_RT);

        /**Get the common set of 'usefulFrequentGraphs' and 'usefulFrequentGraphs_non_RT'**/
        for (Graph usefulFrequentGraph_RT : usefulFrequentGraphs_RT) {

            for (Graph usefulFrequentGraph_non_RT : usefulFrequentGraphs_non_RT) {

                if (usefulFrequentGraph_RT.compare(usefulFrequentGraph_non_RT)) {

                    commmonFeatureSet.add(usefulFrequentGraph_RT);
                }

            }
        }

                 /* A-B
         * **/
        Iterator<Graph> iterator = usefulFrequentGraphs_RT.iterator();
        while (iterator.hasNext()){
            Graph graph_with_RT = iterator.next();

            for(Graph commonGraph : commmonFeatureSet){

                boolean result = graph_with_RT.compare(commonGraph);

                if(result){

                    iterator.remove();
                    break;
                }
            }
        }

        /**B-A**/
        Iterator<Graph> iterator1 = usefulFrequentGraphs_non_RT_cy.iterator();
        while (iterator1.hasNext()){

            Graph graph_with_non_RT = iterator1.next();

            for (Graph usefulFrequentGraph_RT : usefulFrequentGraphs_RT_cy) {

                boolean result = graph_with_non_RT.compare(usefulFrequentGraph_RT);

                if (result) {

                    iterator1.remove();
                    break;
                }
            }
        }

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



        List<Graph> discriminativeFeatureSet_positive = new ArrayList<Graph>(usefulFrequentGraphs_RT);
        List<Graph> discriminativeFeatureSet_negative = new ArrayList<Graph>(usefulFrequentGraphs_non_RT_cy);
        List<Graph> discriminativeFeatureSet = new ArrayList<>(discriminativeFeatureSet_positive);
        discriminativeFeatureSet.addAll(discriminativeFeatureSet_negative);

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
        List<Graph> boundaryGraphs_RT = GSpanWrapper.loadGraph("/Users/fengz/Project/parsemis/Yago_RT.lg");
        List<Graph> boundaryGraphs_non_RT = GSpanWrapper.loadGraph("/Users/fengz/Project/parsemis/Yago_non_RT.lg");

        /**Create a hashmap to contain boundary graphs (Positive & Negative training set) and its corresponding label**/
        Map<Graph, Integer> boundaryGraphs_RT_Map = new HashMap<Graph, Integer>();
        Map<Graph, Integer> boundaryGraphs_non_RT_Map = new HashMap<Graph, Integer>();

        for(Graph boundaryGraph : boundaryGraphs_RT){

            boundaryGraphs_RT_Map.put(boundaryGraph, weight_RT);//weight_RT will be the label for the boundary graph
        }

        /**replace the weight of the edge 'RT' using '65532'**/
        for(Graph boundaryGraph : boundaryGraphs_non_RT){

            boundaryGraphs_non_RT_Map.put(boundaryGraph, weight_non_RT);
        }

        Map<Graph, Integer> allLabeledBoundaryGraphs = new HashMap<Graph, Integer>();
        allLabeledBoundaryGraphs.putAll(boundaryGraphs_RT_Map);
        allLabeledBoundaryGraphs.putAll(boundaryGraphs_non_RT_Map);

        /**Create a new subgraph matcher for training**/
        SubgraphMatcher matcher = new SubgraphMatcher(allLabeledBoundaryGraphs, discriminativeFeatureSet);

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
    }
}
