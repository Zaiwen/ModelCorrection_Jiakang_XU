package au.com.d2dcrc.yago2es;

import org.junit.Test;

import java.util.*;

/**
 * Created by Zaiwen Feng on 5/08/2017.
 * The version aims to achieve multiple distinction of relationships. For 4 different relationships, i.e. influences, hasAcademicAdvisor, isMarriedTo, hasChild between person and person.
 */
public class TestPrototypeByYago_Phase4_V4 {

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

        /**begin to time**/
        long start = System.currentTimeMillis();

        /**Build a subgraph matcher to compare two graphs**/
        SubgraphMatcher subgraphMatcher = new SubgraphMatcher();

        /**Build discriminative feature set**/
        List<Graph> dfs = new ArrayList<Graph>();

        /**First, get the dfs for the 1st relationship boundary graph**/
        List<Graph> union_R2_R3_R4 = subgraphMatcher.getUnionOfGraphSet(usefulFrequentGraphs_RT2, usefulFrequentGraphs_RT3, usefulFrequentGraphs_RT4);
        List<Graph> dfs1 = subgraphMatcher.getSubstractSet(usefulFrequentGraphs_RT1, union_R2_R3_R4);

        /**get the dfs for the 2nd relationship boundary graph**/
        List<Graph> union_R1_R3_R4 = subgraphMatcher.getUnionOfGraphSet(usefulFrequentGraphs_RT1, usefulFrequentGraphs_RT3, usefulFrequentGraphs_RT4);
        List<Graph> dfs2 = subgraphMatcher.getSubstractSet(usefulFrequentGraphs_RT2, union_R1_R3_R4);

        /**get the dfs for the 3rd relationship boundary graph**/
        List<Graph> union_R1_R2_R4 = subgraphMatcher.getUnionOfGraphSet(usefulFrequentGraphs_RT1, usefulFrequentGraphs_RT2, usefulFrequentGraphs_RT4);
        List<Graph> dfs3 = subgraphMatcher.getSubstractSet(usefulFrequentGraphs_RT3, union_R1_R2_R4);


        /**get the dfs for the 4th relationship boundary graph**/
        List<Graph> union_R1_R2_R3 = subgraphMatcher.getUnionOfGraphSet(usefulFrequentGraphs_RT1, usefulFrequentGraphs_RT2, usefulFrequentGraphs_RT3);
        List<Graph> dfs4 = subgraphMatcher.getSubstractSet(usefulFrequentGraphs_RT4, union_R1_R2_R3);

        dfs.addAll(dfs1);
        dfs.addAll(dfs2);
        dfs.addAll(dfs3);
        dfs.addAll(dfs4);

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
        SubgraphMatcher matcher = new SubgraphMatcher(allLabeledBoundaryGraphs, dfs );

        List<Graph> subGraphs = matcher.getSubgraphs();
        Map<Graph, Integer> labeledGraphs = matcher.getLabeledGraphs();

        int[][] trainingMatrix = matcher.acquireMatrix(subGraphs, labeledGraphs);

        /**end the timing**/
        long elapsedTimeMillis = System.currentTimeMillis() - start;
        float elapsedTimeSec = elapsedTimeMillis/1000F;

        /**Write training matrix to csv file**/
        try {

            Util.writeMatrixToCSV(trainingMatrix, "/Users/fengz/Documents/Data_Modeling/YAGO/RDF-Graph@27June17/trainingMatrix.csv", 0);
        }catch (Exception ex){

            ex.printStackTrace();
        }

        System.out.println("Subgraph matching ends...");
        System.out.println(dfs.size() + " discriminative features are used.");
        System.out.println("total time consumption is: " + elapsedTimeSec + "s.");
    }
}
