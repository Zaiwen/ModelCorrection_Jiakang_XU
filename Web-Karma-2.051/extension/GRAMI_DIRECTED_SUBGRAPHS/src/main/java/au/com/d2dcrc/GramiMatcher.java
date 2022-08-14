package au.com.d2dcrc;

import CSP.ConstraintGraph;
import CSP.DFSSearch;
import CSP.Variable;
import com.hp.hpl.jena.ontology.QualifiedRestriction;
import dataStructures.Graph;
import dataStructures.HPListGraph;
import dataStructures.Query;
import dataStructures.myNode;
import edu.isi.karma.rep.alignment.Node;

import java.util.*;

public class GramiMatcher {

    private List<Map<Node,Node>> isomorphismList = null;
    private Set<Integer> anchorSet = null;//designated by User
    private Graph graph = null;//data graph
    private Query qry = null;//pattern graph
    private int frequency = 0;
    private Set<Integer> limitNodeSet = null;//designated by User

    /**Constructor*
     * @created 22 Feb 2019
     * */
    public GramiMatcher () {
        isomorphismList = new ArrayList<>();
        limitNodeSet = new HashSet<>();//added on 22 Feb 2019
        anchorSet = new HashSet<>();//added on 26 Feb 2019
        frequency = 0;
    }

    /**Add anchor label to the GraMi Matcher*
     * @param anchorLabel label of anchor in pattern graph
     * @created 22 Feb 2019
     * */
    public void addAnchorSet (Integer anchorLabel) {
        anchorSet.add(anchorLabel);
    }

    /**Add label of limit node to the GraMi Matcher*
     * @created 26 Feb 2019
     * @param limitNodeLabel label of limit node
     * */
    public void addLimitNode (Integer limitNodeLabel) {
        limitNodeSet.add(limitNodeLabel);
    }

    /**Set limit node label set to GraMi Matcher*
     * @created 26 Feb 2019
     * @param limitNodeSet limit node label set of pattern graph
     * */
    public void setLimitNodeSet (Set<Integer> limitNodeSet) {
        this.limitNodeSet = limitNodeSet;
    }

    /**Set Anchor label set to GraMi Matcher*
     * @created 26 Feb 2019
     *@param anchorSet anchor label set of pattern graph
     * */
    public void setAnchorSet (Set<Integer> anchorSet) {
        this.anchorSet = anchorSet;
    }

    /**Set Query graph**/
    public void setQry (Query qry) {
        this.qry = qry;
    }

    /**Set Data Graph**/
    public void setGraph (Graph graph) {
        this.graph = graph;
    }


    /**this function is used to get the frequency that a pattern appears in a big graph*
     * @param nonCandidates so far, not very sure about the meaning, it can be null
     * @param matchingOption different scenarios for sub-graph matching
     *                       0------pattern graph: tree with unique labels
     *                       1------pattern graph: a graph
     * @return the frequency that this pattern appears in the big graph
     * @reivsed 8 Jan 2019, 22 Jan 2019, 26 Jan 2019, 22 Feb 2019
     * */
    public int getFrequency(HashMap<Integer, HashSet<Integer>> nonCandidates, int matchingOption){
        /**create a constraint graph to solve CSP**/
        int freq = 0 ;
        ConstraintGraph cg = new ConstraintGraph(graph, qry, nonCandidates);
        DFSSearch df = new DFSSearch(cg,graph.getFreqThreshold(),nonCandidates);
        try{
            if (matchingOption == 0) {
                System.out.println("MINIMUM IMAGE BASED METRICS! The pattern graph is a tree while the labels are unique...");
                df.searchExistances2();

            } else if (matchingOption == 1) {
                System.out.println("MINIMUM IMAGE BASED METRICS! The pattern is a graph rather than a tree...");
                if (!anchorSet.isEmpty()) {
                    df.setAnchorLabel(anchorSet);// added on 22 Feb 2019
                }
                if (!limitNodeSet.isEmpty()) {
                    df.setLimitNodeLabel(limitNodeSet);// added on 26 Feb 2019
                }
                df.searchExistances();
            }
            freq=df.getFrequencyOfPattern();
            System.out.println("the frequency of this pattern is: " + freq);
            this.isomorphismList = df.getIsomorphismList();
            System.out.println("the are totally " + isomorphismList.size() + " subgraph isomorphisms detected by GraMi!");

        }catch (Exception e) {
            e.printStackTrace();
        }

        return freq;
    }

    /**Get all the sub-graph isomorphisms*
     * @from 26 Jan 2019
     * */
    public List<Map<Node,Node>> getIsomorphismList () {return isomorphismList;}

    /**a matching example is given. The pattern is a graph*
     * @latest revise 8 Jan 2019
     * */
    public static void example2 () {
        /**Create a big graph. The ID of this graph is 100, and the frequency threshold is???.**/
        Graph singleGraph = new Graph(100,Integer.MAX_VALUE);

        /**Add nodes into the big graph**/
        int index_u0 = singleGraph.addNode(2);
        int index_u1 = singleGraph.addNode(1);
        int index_u2 = singleGraph.addNode(3);
        int index_u3 = singleGraph.addNode(2);
        int index_u4 = singleGraph.addNode(1);
        int index_u5 = singleGraph.addNode(1);
        int index_u6 = singleGraph.addNode(2);
        int index_u7 = singleGraph.addNode(3);

        /**create new nodes. **/
        myNode u0 = new myNode(index_u0,2);
        myNode u1 = new myNode(index_u1,1);
        myNode u2 = new myNode(index_u2,3);
        myNode u3 = new myNode(index_u3,2);
        myNode u4 = new myNode(index_u4,1);
        myNode u5 = new myNode(index_u5,1);
        myNode u6 = new myNode(index_u6,2);
        myNode u7 = new myNode(index_u7,3);


        /**add reachable nodes for each node in big graph. 19 Dec 2018**/
        u1.addreachableNode(u0,10);
        u2.addreachableNode(u1,30);
        u2.addreachableNode(u0,20);

        u2.addreachableNode(u3,20);
        u2.addreachableNode(u4,30);
        u4.addreachableNode(u3,10);

        u2.addreachableNode(u5,30);
        u2.addreachableNode(u6,20);
        u5.addreachableNode(u6,10);

        u7.addreachableNode(u0,20);
        u7.addreachableNode(u1,30);

        /***add myNode to the graph. 19 Dec 2018.**/
        singleGraph.addNode(u0);
        singleGraph.addNode(u1);
        singleGraph.addNode(u2);
        singleGraph.addNode(u3);
        singleGraph.addNode(u4);
        singleGraph.addNode(u5);
        singleGraph.addNode(u6);
        singleGraph.addNode(u7);


        /**add edges into the big graph**/
        singleGraph.addEdge(index_u1,index_u0,10);
        singleGraph.addEdge(index_u2,index_u1,30);
        singleGraph.addEdge(index_u2,index_u0,20);
        singleGraph.addEdge(index_u2,index_u3,20);
        singleGraph.addEdge(index_u2,index_u4,30);
        singleGraph.addEdge(index_u4,index_u3,10);
        singleGraph.addEdge(index_u2,index_u5,30);
        singleGraph.addEdge(index_u2,index_u6,20);
        singleGraph.addEdge(index_u5,index_u6,10);
        singleGraph.addEdge(index_u7,index_u0,20);
        singleGraph.addEdge(index_u7,index_u1,30);

        /**set the frequent nodes by label of the big graph**/
        HashMap<Integer, HashMap<Integer,myNode>> freqNodesByLabel = new HashMap<Integer, HashMap<Integer, myNode>>();
        HashMap<Integer,myNode> one = new HashMap<Integer, myNode>();
        one.put(1,u1);
        one.put(4,u4);
        one.put(5,u5);
        freqNodesByLabel.put(1,one);
        HashMap<Integer,myNode> two = new HashMap<Integer, myNode>();
        two.put(0,u0);
        two.put(3,u3);
        two.put(6,u6);
        freqNodesByLabel.put(2,two);
        HashMap<Integer,myNode> three = new HashMap<Integer,myNode>();
        three.put(2,u2);
        three.put(7,u7);
        freqNodesByLabel.put(3,three);
        singleGraph.setFreqNodesByLabel(freqNodesByLabel);


        /**Begin to create HPListGraph**/
        HPListGraph hpListGraph = new HPListGraph("query");
        /**add nodes into the hpListGraph**/
        int v1 = hpListGraph.addNodeIndex(1);
        int v2 = hpListGraph.addNodeIndex(3);
        int v3 = hpListGraph.addNodeIndex(2);

        /**add edges into the hplistGraph**/
        hpListGraph.addEdgeIndex(v2,v1,30,1);/**not very clear the meaning of label and direction**/
        hpListGraph.addEdgeIndex(v1,v3,10,1);
        hpListGraph.addEdgeIndex(v2,v3,20,1);

        Query q = new Query((HPListGraph<Integer,Double>)hpListGraph);/**create a new query**/

        HashMap<Integer, HashSet<Integer>> nonCandidates = new HashMap<Integer, HashSet<Integer>>();//create a hashmap to save pruned variables
        GramiMatcher gramiMatcher = new GramiMatcher();
        gramiMatcher.setGraph(singleGraph);
        gramiMatcher.setQry(q);
        gramiMatcher.addLimitNode(3);
        gramiMatcher.addAnchorSet(1);
        gramiMatcher.getFrequency(nonCandidates, 1);//the pattern graph is a graph rather than a tree

    }


    /**print all of the correspondence*
     *
     * @from 14 Feb 2019
     * */
    public static void printMatchedNode (List<Map<Node, Node>> matchedResults) {

        for (int i = 0; i < matchedResults.size(); i++) {
            Map<Node, Node> mp = matchedResults.get(i);
            System.out.println("The No: " + i + " graph embedding is: ");
            Iterator it = mp.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry) it.next();
                Node nodeInPattern = (Node) pair.getKey();
                Node nodeInDataGraph = (Node) pair.getValue();
                System.out.println("node: " + nodeInPattern.getId() + " in pattern graph ----- node: " + nodeInDataGraph.getId() + " in data graph");
            }
        }
    }



    /**a matching example is given. The pattern is a directed graph*
     * @latest check 8 Jan 2019
     * */
    public static void example3 () {
        /**Create a big graph. The ID of this graph is 100, and the frequency threshold is???.**/
        Graph singleGraph = new Graph(100,Integer.MAX_VALUE);

        /**Add nodes into the big graph**/
        int index_u0 = singleGraph.addNode(2);
        int index_u1 = singleGraph.addNode(1);
        int index_u2 = singleGraph.addNode(3);
        int index_u3 = singleGraph.addNode(2);
        int index_u4 = singleGraph.addNode(1);
        int index_u5 = singleGraph.addNode(1);
        int index_u6 = singleGraph.addNode(2);
        int index_u7 = singleGraph.addNode(3);

        /**create new nodes. **/
        myNode u0 = new myNode(index_u0,2);
        myNode u1 = new myNode(index_u1,1);
        myNode u2 = new myNode(index_u2,3);
        myNode u3 = new myNode(index_u3,2);
        myNode u4 = new myNode(index_u4,1);
        myNode u5 = new myNode(index_u5,1);
        myNode u6 = new myNode(index_u6,2);
        myNode u7 = new myNode(index_u7,3);


        /**add reachable nodes for each node in big graph. 19 Dec 2018**/
        u1.addreachableNode(u0,10);
        u2.addreachableNode(u1,30);
        u2.addreachableNode(u0,20);

        u2.addreachableNode(u3,20);
        u2.addreachableNode(u4,30);
        u4.addreachableNode(u3,10);

        u2.addreachableNode(u5,30);
        u2.addreachableNode(u6,20);
        u5.addreachableNode(u6,10);

        u7.addreachableNode(u0,20);
        u7.addreachableNode(u1,30);

        /***add myNode to the graph. 19 Dec 2018.**/
        singleGraph.addNode(u0);
        singleGraph.addNode(u1);
        singleGraph.addNode(u2);
        singleGraph.addNode(u3);
        singleGraph.addNode(u4);
        singleGraph.addNode(u5);
        singleGraph.addNode(u6);
        singleGraph.addNode(u7);


        /**add edges into the big graph**/
        singleGraph.addEdge(index_u1,index_u0,10);
        singleGraph.addEdge(index_u2,index_u1,30);
        singleGraph.addEdge(index_u2,index_u0,20);
        singleGraph.addEdge(index_u2,index_u3,20);
        singleGraph.addEdge(index_u2,index_u4,30);
        singleGraph.addEdge(index_u4,index_u3,10);
        singleGraph.addEdge(index_u2,index_u5,30);
        singleGraph.addEdge(index_u2,index_u6,20);
        singleGraph.addEdge(index_u5,index_u6,10);
        singleGraph.addEdge(index_u7,index_u0,20);
        singleGraph.addEdge(index_u7,index_u1,30);

        /**set the frequent nodes by label of the big graph**/
        HashMap<Integer, HashMap<Integer,myNode>> freqNodesByLabel = new HashMap<Integer, HashMap<Integer, myNode>>();
        HashMap<Integer,myNode> one = new HashMap<Integer, myNode>();
        one.put(1,u1);
        one.put(4,u4);
        one.put(5,u5);
        freqNodesByLabel.put(1,one);
        HashMap<Integer,myNode> two = new HashMap<Integer, myNode>();
        two.put(0,u0);
        two.put(3,u3);
        two.put(6,u6);
        freqNodesByLabel.put(2,two);
        HashMap<Integer,myNode> three = new HashMap<Integer,myNode>();
        three.put(2,u2);
        three.put(7,u7);
        freqNodesByLabel.put(3,three);
        singleGraph.setFreqNodesByLabel(freqNodesByLabel);


        /**Begin to create HPListGraph**/
        HPListGraph hpListGraph = new HPListGraph("query");
        /**add nodes into the hpListGraph**/
        int v1 = hpListGraph.addNodeIndex(1);
        int v2 = hpListGraph.addNodeIndex(3);
        int v3 = hpListGraph.addNodeIndex(2);

        /**add edges into the hplistGraph**/
        hpListGraph.addEdgeIndex(v2,v1,30,1);/**not very clear the meaning of label and direction**/
        //hpListGraph.addEdgeIndex(v1,v3,10,1);
        hpListGraph.addEdgeIndex(v2,v3,20,1);

        Query q = new Query((HPListGraph<Integer,Double>)hpListGraph);/**create a new query**/

        HashMap<Integer, HashSet<Integer>> nonCandidates = new HashMap<Integer, HashSet<Integer>>();//create a hashmap to save pruned variables
        GramiMatcher gramiMatcher = new GramiMatcher();
        gramiMatcher.setGraph(singleGraph);
        gramiMatcher.setQry(q);
        gramiMatcher.getFrequency(nonCandidates, 0);//the pattern graph is a tree

    }

    /**museum_crm_test for adaption GraMi to a sub-graph isomorphism matcher*
     * @from 8 Feb 2019
     * */
    public static void main (String args[]){
        example2();
    }
}
