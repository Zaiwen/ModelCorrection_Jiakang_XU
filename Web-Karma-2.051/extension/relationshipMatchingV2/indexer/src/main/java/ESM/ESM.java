/*
 Copyright (c) 2012, Regents of the University of Colorado
 All rights reserved.

 Redistribution and use in source and binary forms, with or without modification,
 are permitted provided that the following conditions are met:

 * Redistributions of source code must retain the above copyright notice, this
    list of conditions and the following disclaimer.

 * Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.

 * Neither the name of the University of Colorado nor the names of its
    contributors may be used to endorse or promote products derived from this
    software without specific prior written permission.

 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR
 ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package ESM;

import java.util.*;

import edu.uci.ics.jung.graph.DirectedGraph;

/**
 * <p>The Exact Subgraph Matching (ESM) algorithm Java implementation for subgraph isomorphism problems</p>
 *
 *
 * <p>The subgraph isomorphism problem is NP-hard. We designed a simple exact subgraph matching (ESM)
 * algorithm for dependency graphs using a backtracking approach. This algorithm is designed to process
 * dependency graphs (directed graphs, direction from governor token to dependent token) from dependency
 * parsers for biological relation and event extraction. It has been demonstrated to be very efficient
 * in these biological information extraction applications.</p>
 *
 *
 * <p>The total worst-case algorithm complexity is O(<i>n</i>^2 * <i>k</i>^<i>n</i>) where n is the number of vertices and
 * k is the vertex degree (number of adjacent edges). The best-case algorithm complexity is O(n^3 * k^2). </p>
 *
 * <p>For more details about our ESM algorithm, the analysis on time complexity, and the different
 * applications of the algorithm, see the section "Related Publications" in the README file for the
 * complete list of our ESM-related publications. </p>
 *
 * <p>This ESM implementation also provides a function to determine the graph isomorphism between two graphs.
 * </p>
 *
 * @author Implemented by Haibin Liu and Tested by Philippe Thomas
 *
 */
public class ESM {
    /** subgraph to be matched (normally smaller graph) */
    DirectedGraph<Vertex, Edge> subgraph = null;
    /** graph to be matched (normally bigger graph) */
    DirectedGraph<Vertex, Edge> graph = null;
    /** startnode of subgraph for matching (from which subgraph node to start the matching process) */
    private Vertex subgraphStartNode = null;
    /** a set of startnodes of graph for matching */
    private List<Vertex> graphStartNodes = null;
    /** a set of anchor labels in the subgraph, each anchor corresponds to a list of connecting nodes in the data graph*
     * @created 25 Feb 2019
     * @Key: lemma of an anchor
     * @Value: the corresponding connecting nodes
     * */
    private Map<String,Set<Integer>> graphConnectingNodes = null;
    /** a set of anchors in the query graph*
     * @created 25 Feb 2019
     * */
    private Set<String> subgraphAnchorLemmas = null;

    /**
     * Constructor to initialize the subgraph and graph and
     * specify the start node of the subgraph and the set of start nodes of the graph
     * @param subgraph : subgraph (supposed to be smaller)
     * @param graph : graph (supposed to be bigger)
     */
    public ESM (DirectedGraph<Vertex, Edge> subgraph, DirectedGraph<Vertex, Edge> graph) {
        this.graph = graph;
        this.subgraph = subgraph;
        //set the startnode of subgraph
        subgraphStartNode = getRandomStartNode( new ArrayList<Vertex>(subgraph.getVertices()) );
        graphStartNodes = new ArrayList<Vertex>(graph.getVertices());
        graphConnectingNodes = new HashMap<>();//added on 25 Feb 2019
        subgraphAnchorLemmas = new HashSet<>();//added on 25 Feb 2019
    }

    /**Add a new anchor of query graph, and at the same time, add its corresponding nodes (connecting nodes) of the data graph*
     * @create 25 Feb 2019
     *@param anchorLemma anchor pos (ID) in the query graph
     *@param connectingNodePosSet a set of connecting node pos with the same label as the anchor
     * */
    public void addGraphConnectingNodes (String anchorLemma, Set<Integer> connectingNodePosSet) {
        graphConnectingNodes.put(anchorLemma,connectingNodePosSet);
    }

    /**Get a set of connecting nodes from data graph, according to the label of an anchor in the query graph*
     *@create 25 Feb 2019
     * @param anchorLemma anchor lemma (label) in the query graph
     * @return a set of connecting node pos with the same label as the anchor
     * */
    public Set<Integer> getGraphConnectingNodes (String anchorLemma) {
        Set<Integer> connectingNodes = graphConnectingNodes.get(anchorLemma);
        return connectingNodes;
    }

    /**Get all of the anchor label*
     * @create 25 Fev 2019
     * @return a set of sub-graph anchors
     * */
    public Set<String> getSubgraphAnchorLemma () {
        return subgraphAnchorLemmas;
    }

    /**add an anchor label*
     * @created 25 Feb 2019
     * @param subgraphAnchorLemma an anchor of subgraph
     * */
    public void addSubgraphAnchorLemma (String subgraphAnchorLemma) {
        this.subgraphAnchorLemmas.add(subgraphAnchorLemma);
    }

    /**
     * randomly choose the start node of the subgraph (default setting)
     * @param subgraphNodes
     * @return start node of the subgraph
     */
    private Vertex getRandomStartNode( List<Vertex> subgraphNodes) {
        //Create random class object
        Random random = new Random();
        //Generate a random number (index) with the size of the list being the maximum
        int randomNumber = random.nextInt(subgraphNodes.size());
        return subgraphNodes.get(randomNumber);
    }

    /**
     * Instead of the default random start node, users can specify the start node for the subgraph
     * @param vertex : user-specified start node
     */
    public void setSubgraphStartNode (Vertex vertex) {
        subgraphStartNode = vertex;
    }

    /**
     * The default setting for choosing the set of start nodes for the graph
     * is use all nodes in the graph as start nodes. However, users can set the set of start
     * nodes for the graph to specify which specific set of nodes they want to use to compare
     * with the subgraph start node. This will narrow down the search by avoiding to match
     * the subgraph start node with every graph node. Consequently, more efficient.
     * @param vertices : user-specified set of start nodes
     */
    public void setGraphStartNodes (List<Vertex> vertices) {
        graphStartNodes = vertices;
    }

    /**
     * Retrieve specific matchings between the subgraph and the graph
     * matching result is store in a map. the key is the node in the subgraph and
     * the value is the injective matching node in the graph
     * Since a subgraph can match different places of a graph,
     * we record all the matchings by putting each matching into a List
     * @return a list of matchings between two graphs
     */
    public List<Map<Vertex, Vertex>> getSubgraphMatchingMatches() {
        List<Map<Vertex, Vertex>> matches = null;
        if(!isSubgraphSmaller()) {
            System.err.println("The size of the subgraph: " +
                    subgraph.getVertexCount() + " is bigger than the size of the graph " +
                    graph.getVertexCount() + ". Please check.");
            return matches;
        }

        for(int i = 0; i < graphStartNodes.size(); i++) {//Line 7 of Algorithm 2
            Map<Vertex, Vertex> subgraphToGraph = new HashMap<Vertex, Vertex>();
            Map<Vertex, Vertex> graphToSubgraph = new HashMap<Vertex, Vertex>();
            List<Map<Vertex, Vertex>> total = new ArrayList<Map<Vertex, Vertex>>();
            List<Vertex> toMatch = Arrays.asList(subgraphStartNode, graphStartNodes.get(i));

            if(matchNodeForAllMatches(toMatch, subgraphToGraph, graphToSubgraph, subgraph, graph, total)) {//Line 10 of Algorithm 2
                if(matches == null) {
                    matches = new ArrayList<Map<Vertex, Vertex>>(total);
                    continue;
                }
                for(Map<Vertex, Vertex> m : total) {
                    boolean flag = true;
                    for(Map<Vertex, Vertex> n : matches) {
                        if( equalMaps(m, n) )
                            flag = false;
                    }
                    if(flag) matches.add(m);
                }
            }
        }
        return matches;
    }

    /**
     * Check if two maps are equal
     * @param m1 : first map
     * @param m2 : second map
     * @return true of false
     */
    private boolean equalMaps(Map<Vertex,Vertex> m1, Map<Vertex,Vertex> m2) {
        if (m1.size() != m2.size())
            return false;
        for (Vertex key: m1.keySet())
            if (!m1.get(key).equals(m2.get(key)))
                return false;
        return true;
    }

    /**
     * The main recursive function for subgraph isomorphism
     * this function helps retrieve all possible matchings between two graphs because
     * a subgraph can have multiple matchings with a graph
     * @param toMatchs : nodes to be matched in two graphs
     * @param subgraphToGraphs : map to record mapping from the subgraph to the graph
     * @param graphToSubgraphs : map to record mapping from the graph to the subgraph
     * @param subgraph : the input subgraph
     * @param graph : the input graph
     * @param total : store all possible matchings between two graphs
     * @return boolean to indicate if the matching is successful or not
     */
    private boolean matchNodeForAllMatches(List<Vertex> toMatchs, Map<Vertex, Vertex> subgraphToGraphs, Map<Vertex, Vertex> graphToSubgraphs,
                                           DirectedGraph<Vertex, Edge> subgraph, DirectedGraph<Vertex, Edge> graph, List<Map<Vertex, Vertex>> total) {
        //generate local copies
        List<Vertex> toMatch = new ArrayList<Vertex>(toMatchs);
        Map<Vertex, Vertex> subgraphToGraph = new HashMap<Vertex, Vertex>(subgraphToGraphs);
        Map<Vertex, Vertex> graphToSubgraph = new HashMap<Vertex, Vertex>(graphToSubgraphs);

        boolean failure = false;
        boolean success = true;
        while( toMatch.size() != 0 ) {//Line 2 of Algorithm 3
            Vertex noder = toMatch.remove(0);
            Vertex nodes = toMatch.remove(0);

            //this is the place to check whether they can be matched
            if(subgraphToGraph.containsKey(noder) && !graphToSubgraph.containsKey(nodes))
                return failure;
            if(!subgraphToGraph.containsKey(noder) && graphToSubgraph.containsKey(nodes))
                return failure;
            if(subgraphToGraph.containsKey(noder) && graphToSubgraph.containsKey(nodes)) {
                if(subgraphToGraph.get(noder).equals(nodes) && graphToSubgraph.get(nodes).equals(noder)) {
                    //do nothing
                }
                else
                    return failure;
            }

            // Here we can make checks whether noder and nodes should match
            if(!matchNodeContent(noder, nodes))
                return failure;

            //record the injective match
            subgraphToGraph.put(noder, nodes);
            graphToSubgraph.put(nodes, noder);

            //one direction match (as governor)
            for( Edge e : subgraph.getOutEdges(noder)) {//Line 10 of Algorithm 3
                Vertex r = e.getDependent();
                List<Vertex> candidateNodes = new ArrayList<Vertex>();
                for(Edge s : graph.getOutEdges(nodes) ) {//Line 12 of Algorithm 3
                    if(e.getLabel().equals(s.getLabel()))//Line 14
                    {
                        Vertex dependentNode = s.getDependent();//added on 25 Feb 2019

                        /**Here, check every dependent node if it is in the domain of designated set. 2019.02.25**/
                        Integer dependentNodePos = dependentNode.getPos();//get the pos
                        String dependentNodeLemma = dependentNode.getLemma();//get the lemma
                        if (subgraphAnchorLemmas.contains(dependentNodeLemma)) {//it means that this dependent node might be a connecting node
                            Set<Integer> allConnectingNodePosSet = graphConnectingNodes.get(dependentNodeLemma);//get all the connecting nodes with this lemma
                            if (allConnectingNodePosSet.contains(dependentNodePos)) {
                                candidateNodes.add(dependentNode);//Line 15
                            }
                        } else {//it means that this dependent node is not a connecting node
                            candidateNodes.add(dependentNode);//Line 15
                        }
                    }
                }
                boolean flag = false;
                boolean terminate = false;
                for(Vertex s : candidateNodes) {//Line 16 of Algorithm 3
                    if(subgraphToGraph.containsKey(r) && ! graphToSubgraph.containsKey(s))
                        continue;
                    if(!subgraphToGraph.containsKey(r) && graphToSubgraph.containsKey(s))
                        continue;
                    if(subgraphToGraph.containsKey(r) && graphToSubgraph.containsKey(s)) {
                        if(subgraphToGraph.get(r).equals(s) && graphToSubgraph.get(s).equals(r)) {
                            terminate = true;
                            break;
                        }
                        else continue;
                    }
                    List<Vertex> toMatchTemp = new ArrayList<Vertex>(toMatch);
                    toMatchTemp.add(noder); toMatchTemp.add(nodes);//Line 21 of Algorithm 3
                    toMatchTemp.add(r); toMatchTemp.add(s);//Line 21 of Algorithm 3
                    if(matchNodeForAllMatches(toMatchTemp, subgraphToGraph, graphToSubgraph, subgraph, graph, total)) {
                        flag = true;
                    }
                }
                if(terminate) continue;
                if(flag) return success;

                return failure;
            }

            //the other direction match (as dependent)
            for( Edge e : subgraph.getInEdges(noder)) {
                Vertex r = e.getGovernor();
                List<Vertex> candidateNodes = new ArrayList<Vertex>();
                for(Edge s : graph.getInEdges(nodes) ) {
                    if(e.getLabel().equals(s.getLabel()))
                        candidateNodes.add(s.getGovernor());
                }
                boolean flag = false;
                boolean terminate = false;
                for(Vertex s : candidateNodes) {
                    if(subgraphToGraph.containsKey(r) && ! graphToSubgraph.containsKey(s))
                        continue;
                    if(!subgraphToGraph.containsKey(r) && graphToSubgraph.containsKey(s))
                        continue;
                    if(subgraphToGraph.containsKey(r) && graphToSubgraph.containsKey(s)) {
                        if(subgraphToGraph.get(r).equals(s) && graphToSubgraph.get(s).equals(r)) {
                            terminate = true;
                            break;
                        }
                        else continue;
                    }
                    List<Vertex> toMatchTemp = new ArrayList<Vertex>(toMatch);
                    toMatchTemp.add(noder); toMatchTemp.add(nodes);
                    toMatchTemp.add(r); toMatchTemp.add(s);
                    if(matchNodeForAllMatches(toMatchTemp, subgraphToGraph, graphToSubgraph, subgraph, graph, total))
                        flag = true;
                }
                if(terminate) continue;
                if(flag) return success;

                return failure;
            }

        }

        //success return
        total.add(subgraphToGraph);
        return success;
    }

    /**
     * Check if the input subgraph is subsumed by the input graph
     * @return true or false
     */
    public boolean isSubgraphIsomorphism() {
        boolean isSubgraphIsomorphism = false;
        if(!isSubgraphSmaller()) {
            System.err.println("The size of the subgraph: " +
                    subgraph.getVertexCount() + " is bigger the size of the graph " +
                    graph.getVertexCount() + ". Please check.");
            return isSubgraphIsomorphism;
        }
        for(int i = 0; i < graphStartNodes.size(); i++) {
            Map<Vertex, Vertex> subgraphToGraph = new HashMap<Vertex, Vertex>();
            Map<Vertex, Vertex> graphToSubgraph = new HashMap<Vertex, Vertex>();
            List<Vertex> toMatch = Arrays.asList(subgraphStartNode, graphStartNodes.get(i));

            if(matchNodeForSingleMatch(toMatch, subgraphToGraph, graphToSubgraph, subgraph, graph)) {
                isSubgraphIsomorphism = true;
                break;
            }
        }
        return isSubgraphIsomorphism;
    }

    /**
     * Provide an additional function to determine if two graphs are isomorphic to each other
     * based on the fact that if two graphs are sub-graph isomorphic to each other, then they are
     * isomorphic to each other
     * @return true or false
     */
    public boolean isGraphIsomorphism() {
        boolean isGraphIsomorphism = false;
        boolean isSubgraphIsomorphicToGraph = false;
        boolean isgraphIsomorphicToSubgraph = false;

        if(!isGraphSizeSame()) {
            System.err.println("The size of the subgraph: " +
                    subgraph.getVertexCount() + " is not same as the size of the graph " +
                    graph.getVertexCount() + ". Please check.");
            return isGraphIsomorphism;
        }
        //subgraph against graph
        for(int i = 0; i < graphStartNodes.size(); i++) {
            Map<Vertex, Vertex> subgraphToGraph = new HashMap<Vertex, Vertex>();
            Map<Vertex, Vertex> graphToSubgraph = new HashMap<Vertex, Vertex>();
            List<Vertex> toMatch = Arrays.asList(subgraphStartNode, graphStartNodes.get(i));
            if(matchNodeForSingleMatch(toMatch, subgraphToGraph, graphToSubgraph, subgraph, graph)) {
                isSubgraphIsomorphicToGraph = true;
                break;
            }
        }

        //graph against subgraph
        //reset the startnode(s)
        subgraphStartNode = getRandomStartNode( new ArrayList<Vertex>(graph.getVertices()) );
        graphStartNodes = new ArrayList<Vertex>(subgraph.getVertices());
        for(int i = 0; i < graphStartNodes.size(); i++) {
            Map<Vertex, Vertex> subgraphToGraph = new HashMap<Vertex, Vertex>();
            Map<Vertex, Vertex> graphToSubgraph = new HashMap<Vertex, Vertex>();

            List<Vertex> toMatch = Arrays.asList(subgraphStartNode, graphStartNodes.get(i));
            if(matchNodeForSingleMatch(toMatch, subgraphToGraph, graphToSubgraph, graph, subgraph)) {
                isgraphIsomorphicToSubgraph = true;
                break;
            }
        }

        if(isSubgraphIsomorphicToGraph && isgraphIsomorphicToSubgraph)
            isGraphIsomorphism = true;

        //set the startnode(s) back
        subgraphStartNode = getRandomStartNode( new ArrayList<Vertex>(subgraph.getVertices()) );
        graphStartNodes = new ArrayList<Vertex>(graph.getVertices());

        return isGraphIsomorphism;
    }

    /**
     * The main recursive function for subgraph isomorphism
     * this function only retrieve one possible matchings between two graphs.
     * As long as it finds an isomorphic subgraph, it returns.
     * Thus, this function is used to determine the subgraph isomorphism, instead of
     * retrieving all possible matchings between two graphs. Therefore, faster.
     * @param toMatchs : nodes to be matched in two graphs
     * @param subgraphToGraphs : map to record mapping from the subgraph to the graph
     * @param graphToSubgraphs : map to record mapping from the graph to the subgraph
     * @param subgraph : the input subgraph
     * @param graph : the input graph
     * @return boolean to indicate if the subgraph isomorphism exists or not
     */
    private boolean matchNodeForSingleMatch(List<Vertex> toMatchs, Map<Vertex, Vertex> subgraphToGraphs, Map<Vertex, Vertex> graphToSubgraphs,
                                            DirectedGraph<Vertex, Edge> subgraph, DirectedGraph<Vertex, Edge> graph) {
        //generate local copies
        List<Vertex> toMatch = new ArrayList<Vertex>(toMatchs);
        Map<Vertex, Vertex> subgraphToGraph = new HashMap<Vertex, Vertex>(subgraphToGraphs);
        Map<Vertex, Vertex> graphToSubgraph = new HashMap<Vertex, Vertex>(graphToSubgraphs);

        boolean failure = false;
        boolean success = true;
        while( toMatch.size() != 0 ) {
            Vertex noder = toMatch.remove(0);
            Vertex nodes = toMatch.remove(0);
            //System.out.println("before " + noder.getToken() + " -> " + nodes.getToken());
            //this is the place to check whether they can be matched
            if(subgraphToGraph.containsKey(noder) && !graphToSubgraph.containsKey(nodes))
                return failure;
            if(!subgraphToGraph.containsKey(noder) && graphToSubgraph.containsKey(nodes))
                return failure;
            if(subgraphToGraph.containsKey(noder) && graphToSubgraph.containsKey(nodes)) {
                if(subgraphToGraph.get(noder).equals(nodes) && graphToSubgraph.get(nodes).equals(noder)) {
                    //do nothing
                }
                else
                    return failure;
            }

            // Here we can make checks whether noder and nodes should match
            if(!matchNodeContent(noder, nodes))
                return failure;

            //record the injective match
            subgraphToGraph.put(noder, nodes);
            graphToSubgraph.put(nodes, noder);
            //System.out.println("after " + noder.getToken() + " -> " + nodes.getToken());

            //one direction match (as governor)
            for( Edge e : subgraph.getOutEdges(noder)) {
                Vertex r = e.getDependent();
                List<Vertex> candidateNodes = new ArrayList<Vertex>();
                for(Edge s : graph.getOutEdges(nodes) ) {
                    if(e.getLabel().equals(s.getLabel()))
                        candidateNodes.add(s.getDependent());
                }
                boolean terminate = false;
                for(Vertex s : candidateNodes) {
                    if(subgraphToGraph.containsKey(r) && ! graphToSubgraph.containsKey(s))
                        continue;
                    if(!subgraphToGraph.containsKey(r) && graphToSubgraph.containsKey(s))
                        continue;
                    if(subgraphToGraph.containsKey(r) && graphToSubgraph.containsKey(s)) {
                        if(subgraphToGraph.get(r).equals(s) && graphToSubgraph.get(s).equals(r)) {
                            terminate = true;
                            break;
                        }
                        else continue;
                    }
                    List<Vertex> toMatchTemp = new ArrayList<Vertex>(toMatch);
                    toMatchTemp.add(noder); toMatchTemp.add(nodes);
                    toMatchTemp.add(r); toMatchTemp.add(s);
                    if(matchNodeForSingleMatch(toMatchTemp, subgraphToGraph, graphToSubgraph, subgraph, graph)) {
                        subgraphToGraphs = new HashMap<Vertex, Vertex>(subgraphToGraph);
                        graphToSubgraphs = new HashMap<Vertex, Vertex>(graphToSubgraph);
                        return success;
                    }
                }
                if(terminate) continue;

                return failure;
            }

            //the other direction match (as dependent)
            for( Edge e : subgraph.getInEdges(noder)) {
                Vertex r = e.getGovernor();
                List<Vertex> candidateNodes = new ArrayList<Vertex>();
                for(Edge s : graph.getInEdges(nodes) ) {
                    if(e.getLabel().equals(s.getLabel()))
                        candidateNodes.add(s.getGovernor());
                }
                boolean terminate = false;
                for(Vertex s : candidateNodes) {
                    if(subgraphToGraph.containsKey(r) && ! graphToSubgraph.containsKey(s))
                        continue;
                    if(!subgraphToGraph.containsKey(r) && graphToSubgraph.containsKey(s))
                        continue;
                    if(subgraphToGraph.containsKey(r) && graphToSubgraph.containsKey(s)) {
                        if(subgraphToGraph.get(r).equals(s) && graphToSubgraph.get(s).equals(r)) {
                            terminate = true;
                            break;
                        }
                        else continue;
                    }
                    List<Vertex> toMatchTemp = new ArrayList<Vertex>(toMatch);
                    toMatchTemp.add(noder); toMatchTemp.add(nodes);
                    toMatchTemp.add(r); toMatchTemp.add(s);
                    if(matchNodeForSingleMatch(toMatchTemp, subgraphToGraph, graphToSubgraph, subgraph, graph)) {
                        subgraphToGraphs = new HashMap<Vertex, Vertex>(subgraphToGraph);
                        graphToSubgraphs = new HashMap<Vertex, Vertex>(graphToSubgraph);
                        return success;
                    }
                }
                if(terminate) continue;

                return failure;
            }

        }

        //success return
        subgraphToGraphs = new HashMap<Vertex, Vertex>(subgraphToGraph);
        graphToSubgraphs = new HashMap<Vertex, Vertex>(graphToSubgraph);
        //for(Entry<Vertex, Vertex> entry : subgraphToGraphs.entrySet())
        //	System.out.println(entry.getKey().getToken() + " -> " + entry.getValue().getToken());
        return success;
    }

    /**
     * Determine if two nodes from two graphs should match with each other or not
     * Current implementation check the compareForm in each node, which includes
     * the generalized POS tag and the lemma information computed by the BioLemmatizer
     * @param noder : node in the subgraph
     * @param nodes : node in the graph
     * @return true of false
     */
    private boolean matchNodeContent(Vertex noder, Vertex nodes) {
        boolean canMatch = false;
        // the matching criteria can be extended,
        // i.e., word can be lemma, and tag can be generalized tag
        // ontological resources can be also imported here for node matching

        if( noder.getCompareForm().equals(nodes.getCompareForm())){
            canMatch = true;
        }
        return canMatch;
    }

    /**
     * Sanity check if the specified subgraph is smaller or equal to the specified graph
     * This function is called first when determining the subgraph isomorphism
     * @return true or false
     */
    private Boolean isSubgraphSmaller() {
        return subgraph.getVertexCount() <= graph.getVertexCount();
    }

    /**
     * Sanity check if the specified subgraph is equal to the specified graph
     * This function is called first when determining the graph isomorphism
     * @return true or false
     */
    private Boolean isGraphSizeSame() {
        return subgraph.getVertexCount() == graph.getVertexCount();
    }

}

