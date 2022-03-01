package au.com.d2dcrc.yago2es;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by Zaiwen Feng on 5/05/2017.
 */
public class Main {

    /**
     *
     * @param args input args
     */
    public static void main(String args[]){

        /**initialize a list saving frequent subgraphs**/
        List<Graph> frequentSubgraphs = new ArrayList<Graph>();
        int matrix[][];

        /**--------Phase 0: Create Linked Entity Graph based on output Karma RDF--------------------**/
        Graph linkedGraph = LinkedEntityGraph.create
                ("/Users/fengz/Documents/Data_Modeling/Karma(from_9_Nov_2016)/RDF-Graph@30May17/big_graph.ttl");

        /**remove the object linking vertex with label 'OL_BA1' and attribute name 'OL_BA_RT'**/
        //linkedGraph.removeAuxiliaryVertex("OL_BA1","OL_BA_RT");//unused
        System.out.println("Phase 1: Linked entity graph for big graph has been created...");

        Graph externalGraph = LinkedEntityGraph.create("/Users/fengz/Documents/Data_Modeling/Karma(from_9_Nov_2016)/RDF-Graph@30May17/external_source.ttl");

        System.out.println("Phase 0: Linked entity graph for external graph has been created...");

        /**--------Phase 1: Merge the external linked data **/
        Graph merged = linkedGraph.addGraph(externalGraph);
        System.out.println("Phase 1: Linked entity graph has been merged...");

        /**-----------Phase 2: Slice the whole linked entity graph to boundary graphs with a specified relationship-----**/

        /**numerate the labels of the linked data graph and save the mapping relations to a hashmap**/
        Map<String, Integer> numeratedVertexLabels =  merged.numerateVertexLables();
        Map<String, Integer> numeratedEdgeLabels = merged.numerateEdgeLables();


        /**a specified relationship, for example, "rent house in", "visits", or "lives in" between person and location**/
        List<Graph> boundaryGraphs = LinkedEntityGraph.slicingLinkedGraph(merged,"OL_PL1","OL_PL_RT","Lives_in", 2);//with relationship 'Lives_in'
        List<Graph> boundaryGraphs1 = LinkedEntityGraph.slicingLinkedGraph(merged, "OL_PL1", "OL_PL_RT", "Have_dinner_at", 2); //with the relationship 'Have_dinner_at' - non 'Lives_in'
        List<Graph> boundaryGraphs_Unlabeled = LinkedEntityGraph.slicingLinkedGraph(merged, "OL_PL1", "OL_PL_RT", "unknown", 2);

        List<String> newEdgeInfo = new ArrayList<String>();//for the edge "Lives_in"
        List<String> newEdgeInfo1 = new ArrayList<String>(); //for the edge "Have_dinner_at" - non "Lives_in"
        List<String> newEdgeInfo_unknown = new ArrayList<String>(); // for the edge "unknown" - need to be predicted

        /**Remove the object linking type from the boundary graphs with relationship 'Lives_in'**/
        for(Graph boundaryGraph : boundaryGraphs){
            newEdgeInfo = boundaryGraph.removeAuxiliaryVertex("OL_PL1", "OL_PL_RT");
        }

        /**Remove the object linking type from the boundary graphs with relationship 'Having_dinner_at'*/
        for(Graph boundaryGraph1 : boundaryGraphs1){
            newEdgeInfo1 = boundaryGraph1.removeAuxiliaryVertex("OL_PL1", "OL_PL_RT");
        }

        /**Remove the object linking type from the boundary graphs with relationship 'unknown'. Unlabeled testing data is created**/
        for(Graph boundaryGraph_Unlabeled : boundaryGraphs_Unlabeled){
            newEdgeInfo_unknown = boundaryGraph_Unlabeled.removeAuxiliaryVertex("OL_PL1", "OL_PL_RT");
        }

        Util.printEdgeToFile(newEdgeInfo, "/Users/fengz/Project/parsemis/edge_RT.lg", numeratedVertexLabels, numeratedEdgeLabels);
        Util.printEdgeToFile(newEdgeInfo1, "/Users/fengz/Project/parsemis/edge_non_RT.lg", numeratedVertexLabels, numeratedEdgeLabels);
        Util.printEdgeToFile(newEdgeInfo_unknown, "/Users/fengz/Project/parsemis/edge_unknown_RT.lg", numeratedVertexLabels, numeratedEdgeLabels);

        System.out.println("Phase 2: slicing linked graph to boundary graphs is finished...");

        /**-----------Phase 3: Acquire Frequent subgraphs through gSpan-------------------*
         * Possible IMPROVEMENT: call gSpan pSystem.out.println("Phase 3: Frequent subgraphs of these boundary graphs has been acquired...");ackage here?
         * */
        /**Renumber the vertices and edges of the boundary graphs with relationship 'Lives_in'**/
        Iterator<Graph> iterator = boundaryGraphs.iterator();
        while (iterator.hasNext()){

            Graph boundaryGraph = iterator.next();
            boundaryGraph.reOrderLabel(numeratedVertexLabels,numeratedEdgeLabels,boundaryGraph);
        }

        GSpanWrapper.writeToInputFile(boundaryGraphs,  "/Users/fengz/Project/parsemis/RT.lg");//boundary graphs for relationship type "lives_in"
        System.out.println("Phase 3: gSpan input file for the RT has been prepared...");

        /**Renumber the vertices and edges of the boundary graphs with relationship 'Having_dinner_at'**/
        Iterator<Graph> iterator1 = boundaryGraphs1.iterator();
        while (iterator1.hasNext()){

            Graph boundaryGraph1 = iterator1.next();
            boundaryGraph1.reOrderLabel(numeratedVertexLabels,numeratedEdgeLabels,boundaryGraph1);
        }

        GSpanWrapper.writeToInputFile(boundaryGraphs1,  "/Users/fengz/Project/parsemis/non-RT.lg");//boundary graphs for relationship type "Having_dinner_at"
        System.out.println("Phase 3: gSpan input file for the non-RT has been prepared...");

        /**Renumber the vertices and edges of the testing data**/
        Iterator<Graph> iterator_for_testingData = boundaryGraphs_Unlabeled.iterator();
        while(iterator_for_testingData.hasNext()){

            Graph boundaryGraph_Unlabeled = iterator_for_testingData.next();
            boundaryGraph_Unlabeled.reOrderLabel(numeratedVertexLabels, numeratedEdgeLabels, boundaryGraph_Unlabeled);
        }
        GSpanWrapper.writeToInputFile(boundaryGraphs_Unlabeled, "/Users/fengz/Project/parsemis/unknown-RT.lg");
        System.out.println("Phase 3: boundary graphs for unknown-RT has been prepared...");

        /******gSpan Execution!***********/




    }


}
