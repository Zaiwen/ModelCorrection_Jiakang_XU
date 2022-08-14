package acrc.itms.unisa;

import au.com.d2dcrc.GramiMatcher;
import dataStructures.HPListGraph;
import dataStructures.Query;
import edu.isi.karma.rep.alignment.LabeledLink;
import edu.isi.karma.rep.alignment.Node;
import org.jgrapht.graph.DirectedWeightedMultigraph;

import java.util.*;

/**museum_crm_test CSP-based exact subgraph matcher based on a big random graph. 17 Aug 2018**/
public class TestFrequency3 {

    public static void main(String args[]){
        Set<String> nodeLabelSet = new HashSet<String>();
        nodeLabelSet.add("10");
        nodeLabelSet.add("20");
        nodeLabelSet.add("30");
        nodeLabelSet.add("40");
        nodeLabelSet.add("50");
        int nodeNumber = 22;
        DirectedWeightedMultigraph<Node, LabeledLink> seed = GraphGeneration.createLabeledGraph(nodeNumber,nodeLabelSet);
        DirectedWeightedMultigraph<Node,LabeledLink> bigGraph = GraphGeneration.createBigGraph(seed,21,2);

        /**Create a big graph. The ID of this graph is 8674, and the frequency threshold is 1.**/
        dataStructures.Graph singleGraph = new dataStructures.Graph(8674,1);
        /**Load 'bigGraph'(DirectedWeighedMultiGraph) into it**/
        dataStructures.Graph.loadDirectedWeightedMultigraph(bigGraph, singleGraph);

        /**create pattern graph**/
        HPListGraph hpListGraph = new HPListGraph("query");
        HPListGraph.loadDirectedWeightedMultiGraph(seed,hpListGraph);
        System.out.println("the pattern is: \n" + hpListGraph);
        Query q = new Query((HPListGraph<Integer,Double>)hpListGraph);/**create a new query**/

        HashMap<Integer, HashSet<Integer>> nonCandidates = new HashMap<>();//create a hashmap to save pruned variables
        GramiMatcher gramiMatcher = new GramiMatcher();
        gramiMatcher.setGraph(singleGraph);
        gramiMatcher.setQry(q);
        gramiMatcher.getFrequency(nonCandidates, 1);//1 or 2 have not been determined. 8 Jan 2019

        System.out.println("museum_crm_test done.");



    }

}
