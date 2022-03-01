package au.com.d2dcrc.yago2es;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * Created by Zaiwen Feng on 16/05/2017.
 */
public class Main_Phase4 {

    /**@param  args input args
     * **/
    public static void main(String args[]){

        /**----------Phase 4: Minimize feature set---------------------**/
        FeatureSelector featureSelector = new FeatureSelector();
        /**Get all of the frequent sub-graphs of boundary graphs with 'RT'**/
        List<Graph> allFrequentGraphs = GSpanWrapper.loadGraph("/Users/fengz/Project/parsemis/RT_subgraphs.txt");
        /**Get the info of the edge that needs to be contained in the graph**/
        List<Integer> edgeInfo = Util.readEdgeFromFile("/Users/fengz/Project/parsemis/edge_RT.lg");
        /**Get the weight info of the edge with RT**/
        int weight_RT = edgeInfo.get(2);
        /**Next, we just extract the sub-graphs that contains the edge with relationship type**/
        List<Graph> usefulFrequentGraphs = featureSelector.removeUselessFrequentGraph(allFrequentGraphs, edgeInfo);

        /**Get all frequent sub-graphs of boundary graphs with 'non-RT'**/
        List<Graph> allFrequentGraphs_non_RT = GSpanWrapper.loadGraph("/Users/fengz/Project/parsemis/non-RT_subgraphs.txt");
        /**Get the info of the edge that needs to be contained in the graph**/
        List<Integer> edgeInfo_non_RT = Util.readEdgeFromFile("/Users/fengz/Project/parsemis/edge_non_RT.lg");
        /**Get the weight info of the edge with non-RT**/
        int weight_non_RT = edgeInfo_non_RT.get(2);
        /**Next, we just extract the sub-graphs that contains the edge with relationship type**/

        List<Graph> usefulFrequentGraphs_non_RT = featureSelector.removeUselessFrequentGraph(allFrequentGraphs_non_RT, edgeInfo_non_RT);

        List<Graph> usefulFrequentGraphs_RT = new ArrayList<Graph>(usefulFrequentGraphs);
        for(Graph usefulFrequentGraph : usefulFrequentGraphs){
            /**Get all the edges from a useful frequent graph with RT**/
            List<Edge> allEdges = usefulFrequentGraph.getAllEdges();
            for(Edge edge : allEdges){
                /**replace the weight by using "X"**/
                if(edge.getWeight()==weight_RT){edge.setWeight(65532);} //65532 has no meaning
            }

        }


        for(Graph usefulFrequentGraph_non_RT : usefulFrequentGraphs_non_RT){
            /**Get all the edges from a useful frequent graph with non-RT**/
            List<Edge> allEdges = usefulFrequentGraph_non_RT.getAllEdges();
            for(Edge edge : allEdges){
                /**replace the weight by using weight of RT**/
                if(edge.getWeight()==weight_non_RT){edge.setWeight(65532); } //65532 has no meaning

            }
        }

        List<Graph> commmonFeatureSet = new ArrayList<Graph>();

        /**Get the common set of 'usefulFrequentGraphs' and 'usefulFrequentGraphs_non_RT'**/
        Iterator<Graph> iterator = usefulFrequentGraphs.iterator();
        while (iterator.hasNext()){
            Graph graph_with_RT = iterator.next();

            for(Graph usefulFrequentGraph_non_RT : usefulFrequentGraphs_non_RT){

                boolean result = graph_with_RT.compare(usefulFrequentGraph_non_RT);

                if(result){

                    iterator.remove();
                    break;
                }
            }
        }

        List<Graph> discriminativeFeatureSet = new ArrayList<Graph>(usefulFrequentGraphs);

        /**----------Phase 5: Match each subgraph to a boundary graph with a specified relationship---------------------**/
        /**Load boundary graphs**/
        List<Graph> boundaryGraphs_RT = GSpanWrapper.loadGraph("/Users/fengz/Project/parsemis/RT.lg");
        List<Graph> boundaryGraphs_non_RT = GSpanWrapper.loadGraph("/Users/fengz/Project/parsemis/non-RT.lg");
        List<Graph> boundaryGraphs_unknown_RT = GSpanWrapper.loadGraph("/Users/fengz/Project/parsemis/unknown-RT.lg");

        /**Create a hashmap to contain boundary graphs (Positive & Negative training set) and its corresponding label**/
        Map<Graph, Integer> boundaryGraphs_RT_Map = new HashMap<Graph, Integer>();
        Map<Graph, Integer> boundaryGraphs_non_RT_Map = new HashMap<Graph, Integer>();

        /**replace the weight of the edge 'RT' using '65532'**/
        for(Graph boundaryGraph : boundaryGraphs_RT){

            List<Edge> allEdges = boundaryGraph.getAllEdges();
            for(Edge edge : allEdges){
                if(edge.getWeight()==weight_RT) {edge.setWeight(65532);}
            }
            boundaryGraphs_RT_Map.put(boundaryGraph, weight_RT);//weight_RT will be the label for the boundary graph
        }

        /**replace the weight of the edge 'RT' using '65532'**/
        for(Graph boundaryGraph : boundaryGraphs_non_RT){

            List<Edge> allEdges = boundaryGraph.getAllEdges();
            for(Edge edge : allEdges){
                if(edge.getWeight()==weight_non_RT){edge.setWeight(65532); }

            }
            boundaryGraphs_non_RT_Map.put(boundaryGraph, weight_non_RT);
        }

        /**Get the info of the edge that needs to be contained in the testing data graph**/
        List<Integer> edgeInfo_unknown_RT = Util.readEdgeFromFile("/Users/fengz/Project/parsemis/edge_unknown_RT.lg");
        /**Get the weight info of the edge with unknown-RT**/
        int weight_unknown_RT = edgeInfo_unknown_RT.get(2);

        /**replace the weight of the edge 'RT' using '65532' in the testing data graph**/
        for(Graph boundaryGraph : boundaryGraphs_unknown_RT){

            List<Edge> allEdges = boundaryGraph.getAllEdges();
            for(Edge edge : allEdges){
                if(edge.getWeight()==weight_unknown_RT){
                    edge.setWeight(65532);
                }
            }
        }

//        List<Graph> allBoundaryGraphs = new ArrayList<Graph>();
//        allBoundaryGraphs.addAll(boundaryGraphs_RT);
//        allBoundaryGraphs.addAll(boundaryGraphs_non_RT);

        Map<Graph, Integer> allLabeledBoundaryGraphs = new HashMap<Graph, Integer>();
        allLabeledBoundaryGraphs.putAll(boundaryGraphs_RT_Map);
        allLabeledBoundaryGraphs.putAll(boundaryGraphs_non_RT_Map);

        /**Create a new subgraph matcher for training**/
        SubgraphMatcher matcher = new SubgraphMatcher(allLabeledBoundaryGraphs, discriminativeFeatureSet);

        List<Graph> subGraphs = matcher.getSubgraphs();
        Map<Graph, Integer> labeledGraphs = matcher.getLabeledGraphs();

        int[][] trainingMatrix = matcher.acquireMatrix(subGraphs, labeledGraphs);

        /**Create a new subgraph matcher for testing**/
        SubgraphMatcher matcher1 = new SubgraphMatcher(boundaryGraphs_unknown_RT, discriminativeFeatureSet);

        List<Graph> subGraphs1 = matcher1.getSubgraphs();
        List<Graph> graphs1 = matcher1.getGraphs();

        int[][] testingMatrix = matcher1.acquireMatrix(subGraphs1, graphs1);

        /**Write training matrix to csv file**/
        try {

            Util.writeMatrixToCSV(trainingMatrix, "/Users/fengz/Documents/Data_Modeling/Karma(from_9_Nov_2016)/RDF-Graph@30May17/trainingMatrix.csv", 0);
            Util.writeMatrixToCSV(testingMatrix, "/Users/fengz/Documents/Data_Modeling/Karma(from_9_Nov_2016)/RDF-Graph@30May17/testingMatrix.csv", 1);
        }catch (Exception ex){

            ex.printStackTrace();
        }

    }
}
