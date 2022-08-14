package au.edu.unisa;

import ESM.ESMMatcher;
import ESM.Vertex;
import acrc.itms.unisa.KnowledgeGraph;
import acrc.itms.unisa.MixedMatcher;
import edu.isi.karma.rep.alignment.LabeledLink;
import edu.isi.karma.rep.alignment.Node;
import org.jgrapht.graph.DirectedWeightedMultigraph;

import java.util.*;

/**integrated Subgraph Matcher*
 * @created 26 Feb 2019
 *1. Determine the limit nodes and anchors
 *2. Use modified GraMi
 *3. Use modifed ESM
 *4. Join the results
 * */
public class IntegratedSubgraphMatcher {

    private MixedMatcher mixedMatcher = null;
    private SteinerTree steinerTree = null;
    private KnowledgeGraph knowledgeGraph = null;
    private int frequency = 0;
    private List<Map<String, Integer>> isomorphismList = null;
    private Set<Node> limitNodeSet = null;//all of the limit nodes in pattern graph
    private Set<Node> anchorSet = null;//all of the anchor nodes in pattern graph
    private Map<String,Set<Integer>> graphConnectingNode = null;//Key: Anchors in pattern graph, value: legal mapping nodes (connecting nodes) for each anchor

    /**Constructor*
     * @created 26 Feb 2019
     * @param query Steiner tree
     * @param data knowledage graph
     * */
    public IntegratedSubgraphMatcher (SteinerTree query, KnowledgeGraph data) {
        steinerTree = query;
        knowledgeGraph = data;
        mixedMatcher = new MixedMatcher();
        isomorphismList = new ArrayList<>();
        limitNodeSet = new HashSet<>();
        anchorSet = new HashSet<>();
        graphConnectingNode = new HashMap<>();
    }


    /**Get all the isomorphisms between the Steiner tree and the Knowledge graph*
     *@created 26 Feb 2019
     * */
    public void getIsomorphismList () {
        DirectedWeightedMultigraph<Node, LabeledLink> query = steinerTree.getTree();//get Query graph
        DirectedWeightedMultigraph<Node, LabeledLink> dataGraph = knowledgeGraph.getKg();//get Data graph
        Set<Node> limitNodeSet = steinerTree.getLimitNodes(1,4,knowledgeGraph);//get limit nodes of pattern graph
        System.out.println("Limit nodes in pattern graph has been detected!");
        for (Node limitNode : limitNodeSet) {
            System.out.println("the id of limit node is: " + limitNode.getId() + " the label of limit node is: " + limitNode.getUri());
        }
        System.out.println("");
        Set<SubStructure> esmSubStructures = steinerTree.decompose(limitNodeSet);//get the part that needs to be exactly matched
        System.out.println("ESM Sub-structures from pattern graph have been detected!");
        int index = 0;
        for (SubStructure esmSubStructure : esmSubStructures) {
            System.out.println("this is the " + index + " ESM sub-structure.");
            Node limitNode = esmSubStructure.getLimitNode();// get the limit node of this sub-structure
            this.limitNodeSet.add(limitNode);//put this node into limit node set of pattern graph
            Set<Node> anchors = esmSubStructure.getAnchorSet();// get the anchors of this sub-structure
            this.anchorSet.addAll(anchors);//put all the anchors into anchor node set of pattern graph
        }
        System.out.println("All the anchors and limit nodes in pattern graph have been detected!");

        System.out.println("Now begin to conduct (modified) MNI-based sub-graph matching, given limit nodes and anchors!");
        Set<Integer> limitNodeLabelSet = new HashSet<>();
        Set<Integer> anchorLabelSet = new HashSet<>();
        for (Node limitNode : limitNodeSet) {
            limitNodeLabelSet.add(Integer.parseInt(limitNode.getUri()));
        }
        for (Node anchor : anchorSet) {
            anchorLabelSet.add(Integer.parseInt(anchor.getUri()));
        }
        try {
            List<Map<Node,Node>> mniMatches = mixedMatcher.loadGramiMatcher(query,dataGraph,1,1,limitNodeLabelSet,anchorLabelSet);
            System.out.println("Now we get " + mniMatches.size() + " matches by using modified GraMi!");

            graphConnectingNode = getConnectingNodeMap(mniMatches);
            System.out.println("Now we get legal connecting nodes for " + graphConnectingNode.size() + " anchor(s) in pattern graph!");

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**Get all the isomorphisms between the Steiner tree and the Knowledge graph*
     * @created 6 Mar 2019
     * @revised on 13 Mar 2019 NOTE: new decomposition strategy using functional dependency is applied
     * */
    public void count () {

        DirectedWeightedMultigraph<Node, LabeledLink> dataGraph = knowledgeGraph.getKg();//get Data graph

        if (steinerTree.checkNeighboringRelation()) {//first, check if neighboring relationship follows FD or not
            /**Init the Adjacent List of the Steiner tree**/
            System.out.println("Now initialize the Adjacent list of Steiner tree! ");
            steinerTree.init();

            /**Now, begin to decompose the pattern graph (Steiner tree)**/
            try {
                steinerTree.fdDecompose(0);//DFS search from node00
                System.out.println(steinerTree.getAnchors().size() + " anchors have been detected!");
                System.out.println(steinerTree.getAtomicSubStructures().size() + " atomic substructures have been detected!");

                steinerTree.decompose();//get sub-structures
                System.out.println(steinerTree.getSubStructures().size() + " substructures have been found! ");

            } catch (Exception ex) {
                ex.printStackTrace();
            }


            Set<DirectedWeightedMultigraph<Node,LabeledLink>> subStructures = steinerTree.getSubStructures();
            List<List<Map<Vertex,Vertex>>> allMappingsList = new ArrayList<>();

            try {

                System.out.println("Begin to match sub-structures with data graph!");
                for (DirectedWeightedMultigraph<Node,LabeledLink> subGraph : subStructures) {
                    Set<String> subgraphAnchorLables = new HashSet<>();
                    Map<String,Set<Integer>> graphConnectingNode = new HashMap<>();
                    List<Map<Vertex, Vertex>> allMappings = ESMMatcher.getMatchResults(subGraph,dataGraph,true,subgraphAnchorLables,graphConnectingNode);
                    allMappingsList.add(allMappings);
                }

                System.out.println("begin to join the results!");
                int min = 0;
                int[] countArray = new int[allMappingsList.size()];
                int i = 0;
                for (List<Map<Vertex,Vertex>> mapList : allMappingsList) {
                    countArray[i] = mapList.size();
                }
                min = Utilities.bubbleSort(countArray);
                System.out.println("the counting result is: " + min );

            } catch (Exception ex) {
                ex.printStackTrace();
            }

        } else {
            System.out.println("This Steiner tree is filtered out since it does not satisfy neighboring relation constraint for FD!");
        }

    }

    /**Call AC-MNI sub-graph matcher to get the frequency in big graph*
     * from 20 June 2019
     * @param pattern pattern graph
     *@param bigGraph data graph
     *@param limitNodeSet limit node set// maybe it is not needed??
     * @param anchorSet anchor node set
     * */
    public int count2 (DirectedWeightedMultigraph<Node,LabeledLink> pattern, DirectedWeightedMultigraph<Node,LabeledLink> bigGraph, Set<Integer> limitNodeSet, Set<Integer> anchorSet) {

        int freq = 0;

        if (steinerTree.checkNeighboringRelation()) {//first, check if neighboring relationship follows FD or not
            /**Init the Adjacent List of the Steiner tree**/
            System.out.println("Now initialize the Adjacent list of Steiner tree! ");
            steinerTree.init();


            try {
                //begin to call MNI-based sub-graph matcher
                MixedMatcher mixedMatcher = new MixedMatcher();
                long start = System.currentTimeMillis();

                List<Map<Node,Node>> embeddings = mixedMatcher.loadGramiMatcher(pattern,bigGraph, 1, 1, limitNodeSet, anchorSet);//graph mode, AC-MNI
                freq = embeddings.size();

                long elapsedTimeMillis = System.currentTimeMillis() - start;
                float elapsedTimeSec = elapsedTimeMillis/1000F;
                System.out.println("time: " + elapsedTimeSec);

            } catch (Exception ex) {
                ex.printStackTrace();
            }

        } else {
            System.out.println("This Steiner tree is filtered out since it does not satisfy neighboring relation constraint for FD!");
        }
        return freq;
    }


    /**Join two set of isomorphisms *
     * @from 6 Mar 2019
     * @param result1 sub-graph isomorphism list returned by ESM
     * @param result2 another sub-graph isomorphism list returned by ESM
     * @param matchingLabel matching label for inner join
     * @return joined graphs
     * */
    public List<Map<String,Integer>> joinEmbeddedGraphs (List<Map<Vertex,Vertex>> result1, List<Map<Vertex, Vertex>> result2, String matchingLabel) {

        MixedMatcher mixedMatcher = new MixedMatcher();
        List<Map<String,Integer>> results = new ArrayList<>();
        int freq = 0;
        for (Map<Vertex, Vertex> esmMap1 : result1) {
            for (Map<Vertex, Vertex> esmMap2: result2) {
                Map<String,Integer> esmIsomorphismIndex1 = mixedMatcher.convertESMIsomorphism(esmMap1);//key:label, value:id
                Map<String,Integer> esmIsomorphismIndex2 = mixedMatcher.convertESMIsomorphism(esmMap2);//key:label, value:id
                int i = esmIsomorphismIndex1.get(matchingLabel).intValue();
                int j = esmIsomorphismIndex2.get(matchingLabel).intValue();
                if (i == j) {
//                    Map<String,Integer> joinedGraph = new HashMap<>();
//                    joinedGraph.putAll(esmIsomorphismIndex);
//                    joinedGraph.putAll(gramiIsomorphismIndex);//merge two maps ignoring duplicate keys!
                    freq ++;
                    //results.add(joinedGraph);
                }
            }
        }
        System.out.println("there are " + freq + " joined embedded graphs in the data graph found!");
        return results;
    }



    /**Filter out some impossible connecting nodes (mapping nodes of the anchors), and get all the legal connectors for ESM Sub-graph Matcher*
     * @create 28 Feb 2019
     * @param mniMatches matches returned by modified GraMi
     *@return legal connecting nodes for each anchor
     * */
    public Map<String, Set<Integer>> getConnectingNodeMap (List<Map<Node,Node>> mniMatches) {

        Map<String, Set<Integer>> connectingNodeMap = new HashMap<>();
        for (Node anchor : anchorSet) {
            String anchorLabel = anchor.getUri();//get the label of anchor
            System.out.println("the anchor label is: " + anchorLabel);
            String anchorID = anchor.getId();//get anchor ID

            Set<Integer> nodeIDSetInDataGraph = new HashSet<>();//id of all the nodes corresponding to an anchor

            for (Map<Node,Node> mniMatch : mniMatches) {
                Iterator it = mniMatch.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry pair = (Map.Entry) it.next();
                    Node nodeInPattern = (Node) pair.getKey();
                    Node nodeInDataGraph = (Node) pair.getValue();
                    if (nodeInPattern.getId().equals(anchorID)) {
                        String nodeIDInDataGraph = nodeInDataGraph.getId();//get the ID of the corresponding node in data graph
                        System.out.println("the legal corresponding connector ID is: " + nodeIDInDataGraph);
                        nodeIDSetInDataGraph.add(Integer.parseInt(nodeIDInDataGraph));
                    }
                }
            }
            connectingNodeMap.put(anchorLabel,nodeIDSetInDataGraph);
        }
        return connectingNodeMap;
    }

    /**museum_crm_test the AC-MNI matcher*
     * @from 26 Feb 2019
     * */
    public static void main (String args[]) {
        DirectedWeightedMultigraph<Node, LabeledLink> bigGraph = ESMMatcher.loadDirectedWeightedMultipGraph(Settings.KGEvaluationAddress, "kg20190115_N7000.lg");/**load the data graph**/
        DirectedWeightedMultigraph<Node, LabeledLink> patternGraph = ESMMatcher.loadDirectedWeightedMultipGraph(Settings.KGEvaluationAddress, "ST1.lg");//load the pattern graph
        SteinerTree tree = new SteinerTree(patternGraph);
        tree.addFD("5","7");//add FD to Steiner tree
        tree.addFD("13","7");//add FD to Steiner tree
        KnowledgeGraph knowledgeGraph = new KnowledgeGraph(bigGraph);
        IntegratedSubgraphMatcher matcher = new IntegratedSubgraphMatcher(tree, knowledgeGraph);


        Set<Integer> limitNodeSet = new HashSet<>();
        limitNodeSet.add(7); //label of PL is 7
        Set<Integer> anchorSet = new HashSet<>();
        anchorSet.add(12);//label of P is 12

        int freq = matcher.count2(patternGraph, bigGraph, limitNodeSet, anchorSet);

        System.out.println("according to AC-MIN, the freq is: " + freq);

    }

}
