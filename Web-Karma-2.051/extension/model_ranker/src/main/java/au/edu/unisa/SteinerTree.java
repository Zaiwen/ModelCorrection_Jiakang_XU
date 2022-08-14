package au.edu.unisa;

import ESM.ESMMatcher;
import ESM.Edge;
import ESM.Vertex;
import acrc.itms.unisa.KnowledgeGraph;
import edu.isi.karma.rep.alignment.*;
import edu.isi.karma.util.RandomGUID;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import org.jgrapht.graph.DirectedWeightedMultigraph;

import java.util.*;

/**Steiner trees produced from Karma*
 *@from Jan 2019
 * @revised on 20 Feb 2019
 * */


public class SteinerTree{

    private Set<Node> limitNodes = null;//may be useless
    private Set<Node> anchors = null;
    private DirectedWeightedMultigraph<Node,LabeledLink> tree = null;
    private Set<DirectedWeightedMultigraph<Node,LabeledLink>> subStructures = null;
    private Map<Integer, LinkedList<Integer>> adj = null;  //list for adjacent node representation. key: Node ID, Value: all of its adjacent nodes. Added on 1 Mar 2019
    private Map<Integer,Boolean> visited = null;//Record which node has been visited in a round of DFS. Key: node ID, value: false - not visited
    private List<DirectedWeightedMultigraph<Node,LabeledLink>> atomStructures = null;//Record the sub-structures satisfying RULE2. Added on 12 Mar 2019.
    private Set<FD> fdSet = null;//Record all the functional dependencies on the Steiner tree

    /**
     * Constructor.
     * @created 20 Feb 2019
     */
    public SteinerTree(){
        limitNodes = new HashSet<>();
        anchors = new HashSet<>();
        tree = new DirectedWeightedMultigraph<Node, LabeledLink>(LabeledLink.class);
        subStructures = new HashSet<>();
    }

    /**Constructor*
     * @created 20 Feb 2019
     * @param tree concrete tree
     * */
    public SteinerTree (DirectedWeightedMultigraph<Node,LabeledLink> tree) {
        this.tree = tree;
        limitNodes = new HashSet<>();
        anchors = new HashSet<>();
        subStructures = new HashSet<>();
        atomStructures = new ArrayList<>();
        fdSet = new HashSet<>();
    }

    /**reset the node/edge labels of the Steiner tree into numbers (0,1,2,3,...) according to the definition of the cdm (e.g.)**
     * @param edgeLabelMap Pre-defined correspondence. Key:string. The initial edge label, Value:numeric edge label from 0 (like, 0,1,2,...)
     * @param nodeLabelMap Pre-defined correspondence. Key:string. The initial node label, Value:numeric node label from 0 (like, 0,1,2,...)
     * @from 9 Jan 2019
     * @revised 6 May 2019
     * */
    public void resetLabels(Map<String,Integer> nodeLabelMap, Map<String,Integer> edgeLabelMap){

        Set<LabeledLink> edgeSet = tree.edgeSet();//get all of the edges of the Steiner tree
        for(LabeledLink labeledLink : edgeSet){
            Label linkLabel = labeledLink.getLabel();//get label
            String trimedLinkUri = linkLabel.getUri().replaceFirst(Settings.prefix, "");//trim the prefix produced by the Protege
            Integer newUri = edgeLabelMap.get(trimedLinkUri);//get new uri
            linkLabel.setUri(newUri.toString());//update uri
        }

        Set<Node> nodeSet = tree.vertexSet();//get all of the nodes of the graph
        for(Node node : nodeSet){
            Label label = node.getLabel();//get label
            String nodeUri = label.getUri();//get uri
            String trimedNodeUri = nodeUri.replaceFirst(Settings.prefix, "");//trim the prefix produced by the protege
            if (!Utilities.isNumeric(trimedNodeUri)) {//the condition is added on 6 May 2019
                Integer newNodeUri = nodeLabelMap.get(trimedNodeUri);
                label.setUri(newNodeUri.toString());
            }
        }
    }

    /**Get the tree*
     * @created 20 Feb 2019
     * */
    public DirectedWeightedMultigraph<Node, LabeledLink> getTree() {
        return tree;
    }

    /**add a sub-structure for ESM matching*
     * @created 21 Feb 2019
     * @param subStructure sub-structure containing limit node
     * */
    public void addSubStructure (DirectedWeightedMultigraph<Node, LabeledLink> subStructure) {
        this.subStructures.add(subStructure);
    }

    /**get all of the subStructures*
     * @created 13 Mar 2019
     * @return all of the substructures
     * */
    public Set<DirectedWeightedMultigraph<Node,LabeledLink>> getSubStructures () {
        return this.subStructures;
    }


    /**add an anchor to the anchor set*
     * @created 21 Feb 2019
     * @param anchor an anchor to be added into Steiner tree
     * */
    public void addAnchor (Node anchor) {
        this.anchors.add(anchor);
    }

    /**Get all of the anchors of Steiner tree*
     * @created 13 Mar 2019
     * @return all of the anchors of the Steiner tree
     * */
    public Set<Node> getAnchors () {
        return anchors;
    }

    /**add an limit node to the limit node set*
     * @created 21 Feb 2019
     * @param limitNode a limit node to be added into
     * */
    public void addLimitNode (Node limitNode) {
        this.limitNodes.add(limitNode);
    }

    /**add a functional dependency to the Steiner tree*
     * @create 12 Mar 2019
     * @param fd a new functional dependency to be added
     * */
    public void addFD (FD fd) {
        this.fdSet.add(fd);
    }

    /**add a functional dependency to the Steiner tree*
     * @created 13 Mar 2019
     * @param determinantLabel label of determinant in Steiner tree
     * @param dependentLabel label of dependent in Steiner tree
     * */
    public void addFD (String determinantLabel, String dependentLabel) {
        FD fd = new FD (determinantLabel, dependentLabel);
        this.fdSet.add(fd);
    }

    /**Get all of the Atomic Substructures*
     * @created 13 Mar 2019
     * @return all of the atomic sub-structures of Steiner tree
     * */
    public List<DirectedWeightedMultigraph<Node,LabeledLink>> getAtomicSubStructures () {
        return atomStructures;
    }

    /**Decompose pattern graph (Steiner tree) into a set of sub-structures, which are the inputs of ESM*
     * @created 13 Mar 2019 (should be extended in the future)
     *@return a couple of substructures
     * */
    public void decompose () {
        Set<DirectedWeightedMultigraph<Node, LabeledLink>> candidateSSSet= new HashSet<>();
        DirectedWeightedMultigraph<Node, LabeledLink> atomicSS = atomStructures.get(0);
        if (!anchors.isEmpty()) {
            Node anchor = null;
            for (Node node : anchors) {
                anchor = node;
                break;
            }

            Set<LabeledLink> outgoingEdges = tree.outgoingEdgesOf(anchor);//get all the outgoing edges
            Set<LabeledLink> incomingEdges = tree.incomingEdgesOf(anchor);//get all the incoming edges

            Iterator<LabeledLink> it1 = outgoingEdges.iterator();
            while (it1.hasNext()) {
                LabeledLink labeledLink = it1.next();
                Node target = labeledLink.getTarget();
                DirectedWeightedMultigraph<Node,LabeledLink> candidate = new DirectedWeightedMultigraph<>(LabeledLink.class);//create a new sub-structure around the limit node
                candidate.addVertex(anchor);
                candidate.addVertex(target);//add this node to the sub-structure graph
                candidate.addEdge(anchor,target,labeledLink);//add this edge to the sub-structure graph
                candidateSSSet.add(candidate);
            }

            Iterator<LabeledLink> it2 = incomingEdges.iterator();
            while (it2.hasNext()) {
                LabeledLink labeledLink = it2.next();
                Node source = labeledLink.getSource();
                DirectedWeightedMultigraph<Node,LabeledLink> candidate = new DirectedWeightedMultigraph<>(LabeledLink.class);//create a new sub-structure around the limit node
                candidate.addVertex(source);//add this node to the sub-structure graph
                candidate.addVertex(anchor);
                candidate.addEdge(source,anchor,labeledLink);//add this edge to the sub-structure graph
                candidateSSSet.add(candidate);
            }


            //check if candidate is subsumed by atomic graph
            for (DirectedWeightedMultigraph<Node,LabeledLink> candidate : candidateSSSet) {
                SubStructure subStructure = new SubStructure();

                DirectedSparseGraph<Vertex,Edge> subGraph =  ESMMatcher.convertESMGraph(candidate,"subGraph");
                DirectedSparseGraph<Vertex,Edge> graph = ESMMatcher.convertESMGraph(atomicSS,"graph");
                ESM.ESM esm = new ESM.ESM(subGraph,graph);//build subgraph matcher
                boolean result = esm.isSubgraphIsomorphism();
                if (result) {
                    subStructures.add(atomicSS);
                } else {
                    subStructures.add(candidate);
                }
            }
        } else {
            subStructures.add(atomicSS);
        }

    }

    /**Decompose pattern graph (Steiner tree) into a set of sub-structures, which are the inputs of ESM*
     * @created 20 Feb 2019
     * @param limitNodes a set of limit nodes
     * @return a set of ESM sub-structures
     * */
    public Set<SubStructure> decompose (Set<Node> limitNodes) {
        Set<SubStructure> subStructures = new HashSet<>();

        Iterator<Node> iterator = limitNodes.iterator();
        while (iterator.hasNext()) {
            Node limitNode = iterator.next();
            DirectedWeightedMultigraph<Node,LabeledLink> subStructureGraph = new DirectedWeightedMultigraph<>(LabeledLink.class);//create a new sub-structure around the limit node
            SubStructure subStructure = new SubStructure();//create a new ESM sub-structure
            subStructure.setLimitNode(limitNode);//set the limit node
            subStructureGraph.addVertex(limitNode);//add limit node to the sub-structure graph
            this.addLimitNode(limitNode);//add this limit node to this Steiner tree

            Set<LabeledLink> outgoingEdges = tree.outgoingEdgesOf(limitNode);//get all the outgoing edges
            Set<LabeledLink> incomingEdges = tree.incomingEdgesOf(limitNode);//get all the incoming edges

            Iterator<LabeledLink> it1 = outgoingEdges.iterator();
            while (it1.hasNext()) {
                LabeledLink labeledLink = it1.next();
                Node anchor = labeledLink.getTarget();
                subStructure.addAnchor(anchor);//add this anchor to the sub-structure graph
                this.addAnchor(anchor);//add this anchor node to the Steiner tree
                subStructureGraph.addVertex(anchor);//add this node to the sub-structure graph
                subStructureGraph.addEdge(limitNode,anchor,labeledLink);//add this edge to the sub-structure graph
            }

            Iterator<LabeledLink> it2 = incomingEdges.iterator();
            while (it2.hasNext()) {
                LabeledLink labeledLink = it2.next();
                Node anchor = labeledLink.getSource();
                subStructure.addAnchor(anchor);//add this anchor to the sub-structure graph
                this.addAnchor(anchor);//add this anchor node to the Steiner tree
                subStructureGraph.addVertex(anchor);//add this node to the sub-structure graph
                subStructureGraph.addEdge(anchor,limitNode,labeledLink);//add this edge to the sub-structure graph
            }

            subStructure.setSubStructureGraph(subStructureGraph);
            subStructures.add(subStructure);//add this sub-structure into the set
        }//end outer while
        return subStructures;
    }

    /**Decompose Steiner tree by anchor point*
     * @param anchor anchor point in Steiner tree
     * @return  two sub-structures
     * @create 5 Mar 2019
     * */
    public Set<SubStructure> decompose (Node anchor) {
        Set<SubStructure> subStructures = new HashSet<>();
        Set<Node> dfsStartPoint = new HashSet<>();
        Set<LabeledLink> toBeRemoved = new HashSet<>();

        //remove the anchor and its neighbouring edges from Steiner tree
        Set<LabeledLink> outgoingEdges = tree.outgoingEdgesOf(anchor);//get all the outgoing edges
        Set<LabeledLink> incomingEdges = tree.incomingEdgesOf(anchor);//get all the incoming edges

        //Iterator all the outgoing edges
        for (LabeledLink labeledLink : outgoingEdges) {
            Node target = labeledLink.getTarget();//get target
            dfsStartPoint.add(target);
            toBeRemoved.add(labeledLink);
        }

        //Iterator all the incoming edges
        for (LabeledLink labeledLink : incomingEdges) {
            Node source = labeledLink.getSource();
            dfsStartPoint.add(source);
            toBeRemoved.add(labeledLink);
        }

        //remove the anchor point and all of its neighbouring edges
        for (LabeledLink labeledLink : toBeRemoved) {
            tree.removeEdge(labeledLink);
        }
        tree.removeVertex(anchor);

        //construct a index for each node in the tree (although the tree might not be connected now)
        Set<Node> nodeSet = tree.vertexSet();
        Map<Integer, Node> nodeMap = new HashMap<>();
        for (Node node : nodeSet) {
            nodeMap.put(Integer.parseInt(node.getId()),node);
        }

        //for every DFS start point, conduct DFS search
        try {
            for (Node node : dfsStartPoint) {
                this.init();
                System.out.println("Now the start node is: " + node.getId());
                List<Integer> traversedNodeIDList = DFS(Integer.parseInt(node.getId()));
                List<Node> traversedNodeList = new ArrayList<>();
                DirectedWeightedMultigraph<Node, LabeledLink> subGraph = new DirectedWeightedMultigraph<Node, LabeledLink>(LabeledLink.class);
                SubStructure subStructure = new SubStructure();
                subStructure.setSubStructureGraph(subGraph);
                Set<LabeledLink> existedLabeledLinkSet = new HashSet<>();//hold all the labeled links in a sub-structure

                //recover the nodes of sub-structure
                for (Integer traversedNodeID : traversedNodeIDList) {
                    System.out.println("the current id is: " + traversedNodeID);
                    Node currentNode = nodeMap.get(traversedNodeID);
                    subGraph.addVertex(currentNode);//add current node to sub-structure
                    traversedNodeList.add(currentNode);
                }

                //add the anchor to sub-structure finally
                subGraph.addVertex(anchor);

                //recover all the edge s of sub-structure
                for (Node traversedNode : traversedNodeList) {
                    Set<LabeledLink> labeledLinks = tree.edgesOf(traversedNode);//get all the labeled link neighbouring this node

                    //check every labeled link exist in the current labeled link set. If no, add it into current labeled link set
                    for (LabeledLink labeledLink : labeledLinks) {
                        Node target = labeledLink.getTarget();
                        Node source = labeledLink.getSource();

                        boolean flag = true;//true - will add, false - will not add
                        for (LabeledLink labeledLink1 : existedLabeledLinkSet) {
                            Node target1 = labeledLink1.getTarget();
                            Node source1 = labeledLink1.getSource();

                            //if this labeled link has existed int the exist link set, do not add it.
                            if (target.getId().equals(target1.getId())) {
                                if (source.getId().equals(source1.getId())) {
                                    flag = false;
                                }
                            }
                        }

                        if (flag) {
                            subGraph.addEdge(source, target, labeledLink);//add this edge to the sub-structure
                        }
                    }
                }

                //add the node between the anchor and its neighbouring nodes
                for (LabeledLink labeledLink : toBeRemoved) {
                    Node start = labeledLink.getSource();
                    Node target = labeledLink.getTarget();
                    if (start.getId().equals(anchor.getId())) {
                        if (target.getId().equals(node.getId())) {
                            subGraph.addEdge(start,target,labeledLink);//add this edge to the sub-structure
                        }
                    } else if (start.getId().equals(node.getId())) {
                        if (target.getId().equals(anchor.getId())) {
                            subGraph.addEdge(start,target,labeledLink);//add this edge to the sub-structure
                        }
                    }
                }


                subStructures.add(subStructure);//add it to the set of substructures
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        int index = 0;
        for (SubStructure subStructure : subStructures) {
            System.out.println("this is the " + index + " sub-structure!");
            System.out.println("there are " + subStructure.getSubStructure().vertexSet().size() + " nodes!");
            System.out.println("there are " + subStructure.getSubStructure().edgeSet().size() + " edges!");
        }
        return subStructures;
    }


    /**Get a set of limit nodes*
     * @created 20 Feb 2019
     *@param numberThreshold threshold for the number of the node in kg if the node is a limit node
     *@param degreeThreshold threshold for the degree of the node in kg if the node is a limit node
     *@param knowledgeGraph knowledge graph
     *@return a set of limit nodes
     * */
    public Set<Node> getLimitNodes (int numberThreshold, int degreeThreshold, KnowledgeGraph knowledgeGraph) {
        Set<Node> limitNodes = new HashSet<>();
        //DirectedWeightedMultigraph<Node, LabeledLink> bigGraph = knowledgeGraph.getKg();//get big graph

        /**loop every entity in the Steiner tree**/
        Set<Node> allNodes = tree.vertexSet();
        Iterator<Node> iterator = allNodes.iterator();
        while (iterator.hasNext()) {
            Node nodeInPattern = iterator.next();
            int freq = knowledgeGraph.getFrequency(nodeInPattern.getUri());//Get the frequency
            int degree = knowledgeGraph.getDegree(nodeInPattern.getUri());//Get the degree mean
            if (freq <= numberThreshold) {
                if (degree >= degreeThreshold) {
                    limitNodes.add(nodeInPattern);
                }
            }
        }
        return limitNodes;
    }

    /**fill in an adjacent list representation, for DFS
     * @NOTE: Here we do not consider the direction of edges
     * *
     * @create 1 Mar 2019
     * @revissed  5 Mar 2019
     * */
    public void init () {
        Set<Node> allNodeSet = tree.vertexSet();//get all the nodes
        Set<LabeledLink> allLinkSet = tree.edgeSet();//get all the edges

        adj = new HashMap<>();
        visited = new HashMap<>();

        //Mark all the vertices as not visited, and construct the adjacent list for each node
        for (Node node : allNodeSet) {
            visited.put(Integer.parseInt(node.getId()), false);
            LinkedList<Integer> adjacentNodeIDList = new LinkedList<>();
            for (LabeledLink labeledLink : allLinkSet) {
                Node source = labeledLink.getSource();
                Node target = labeledLink.getTarget();
                if (node.getId().equals(source.getId())) {
                    Node end = labeledLink.getTarget();
                    adjacentNodeIDList.add(Integer.parseInt(end.getId()));
                } else if (node.getId().equals(target.getId())) {
                    Node start = labeledLink.getSource();
                    adjacentNodeIDList.add(Integer.parseInt(start.getId()));
                }
            }
            adj.put(Integer.parseInt(node.getId()),adjacentNodeIDList);
        }
    }


    /**A function used by DFS*
     * @Adatpted from address: https://www.geeksforgeeks.org/depth-first-search-or-dfs-for-a-graph/
     * @create 1 Mar 2019
     * @param v the current vertex
     *@param visited an array recording if a node is visited or not
     *@param visitedNode all the visited nodes
     * */
    public void DFSUtil (int v, Map<Integer,Boolean> visited, List<Integer> visitedNode) {

        /**Mark the current node as visited and print it**/
        visited.put(v, true);
        System.out.print(v + " ");
        visitedNode.add(v);

        //Recur for all the vertices adjacent to this vertex
        Iterator<Integer> i = adj.get(v).listIterator();
        while (i.hasNext()) {
            int n = i.next();
            if (!visited.get(n)) {
                DFSUtil(n, visited, visitedNode);
            }
        }
    }

    /**The function to do DFS traversal. It uses recursive DFSUtil()*
     * @adapt https://www.geeksforgeeks.org/depth-first-search-or-dfs-for-a-graph/
     * @created 1 Mar 2019
     * @param v id of the start node
     * */
    public List<Integer> DFS (int v) throws Exception{

        //if the start node does not exist in the tree, throw exception
        if (!visited.keySet().contains(v)) {
            throw new Exception("This start node does not exist in the Steiner tree!");
        }

        List<Integer> visitedNodeList = new ArrayList<>();

        /**Call the recursive helper function to print DFS traversal**/
        DFSUtil(v, visited, visitedNodeList);
        return visitedNodeList;
    }



    /**find all the connected components in the Steiner tree*
     * @created 4 Mar 2019
     * @return all of the connected components from the Steiner tree
     * */
    public List<Set<Node>> getConnectedComponent () {
        List<Set<Node>> classfiedNodeSet = new ArrayList<>();
        Set<Node> nodeSet = tree.vertexSet();
        Map<Node,Boolean> nodeBooleanMap = new HashMap<>();//true: this node has been classified; false: this node hasn't been classified into a group
        for (Node node : nodeSet) {
            nodeBooleanMap.put(node, false);//Initially, all the nodes have not been classified
        }

        //get connected components
        Node randomStartNode = null;
        while (true) {
            Boolean allAreClassified = true;

            //get a random unclassified node from Steiner tree
            Iterator it = nodeBooleanMap.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry) it.next();
                //Node node = (Node) pair.getKey();
                Boolean classified = (Boolean) pair.getValue();
                if (!classified) {
                    allAreClassified = false;
                    randomStartNode = (Node) pair.getKey();//get a random unclassified node
                    break;
                }
            }

            if (allAreClassified) {
                break;
            }

            Set<Node> allNodeSetInSubStructure = new HashSet<>();

            //get all the connected nodes from the random start node
            try {
                List<Integer> traversedNodeIDList = DFS(Integer.parseInt(randomStartNode.getId()));

                //change these Node status as visited
                Iterator iterator = nodeBooleanMap.entrySet().iterator();
                while (iterator.hasNext()) {
                    Map.Entry pair = (Map.Entry) iterator.next();
                    Node node = (Node) pair.getKey();
                    Boolean classified = (Boolean) pair.getValue();
                    if (traversedNodeIDList.contains(Integer.parseInt(node.getId()))) {
                        nodeBooleanMap.put(node,true);//update value in node boolean map
                        allNodeSetInSubStructure.add(node);
                    }
                }

                classfiedNodeSet.add(allNodeSetInSubStructure);
            } catch (Exception ex) {

                ex.printStackTrace();
            }


        }
        return classfiedNodeSet;
    }

    /**Get a list of nodes from Steiner tree according to its id*
     * @param nList a list of node id
     *@return a list of nodes
     * */
    public List<Node> getNodeListByID (List<Integer> nList) {
        List<Node> nodeList = new ArrayList<>();
        Set<Node> allNodes = tree.vertexSet();

        //set up a hash map. key: node id, value: node
        Map<Integer, Node> nodeMap = new HashMap<>();
        for (Node node : allNodes) {
            String nodeID = node.getId();//get id of node
            nodeMap.put(Integer.parseInt(nodeID), node);
        }

        for (Integer nodeID : nList) {
            Node node = nodeMap.get(nodeID);//get the node
            nodeList.add(node);
        }

        return nodeList;
    }

    /**
     * Rule for functional dependency based Steiner tree decomposition
     *Rule 1: For a pattern graph with 3 entity types A, B, C, if B determines A, and C determines A,
     * this pattern graph will be partitioned into two sub-structures,
     * the one is A-B, and the other one is A-C.
     * @create 13 Mar 2019
     * @param B entity in Steiner tree
     * @param A entity in Steiner tree
     * @param C entity in Steiner tree
     *          @return an anchor is found
     * **/
    public boolean findAnchors (Node B, Node A, Node C) {
        boolean result = false;
        if (isFDExist(B,A)) {
            if (isFDExist(C,A)) {
                result = true;//it means that A is anchor
                addAnchor(A);//add A into anchor list
            }
        }
        return result;
    }

    /**Rule for functional dependency based Steiner tree decomposition*
     * Rule 2: For a pattern graph with 3 entity types A, B, C, if B determines C, this pattern graph is atomic and cannot be partitioned.
     *@param A an entity in Steiner tree
     * @param B an entity in Steiner tree
     * @param C an entity in Steiner tree
     *          @return an atomic substructure is found
     *          @create 13 Mar 2019
     * */
    public boolean findAtomicSubStructure (Node A, Node B, Node C) {
        boolean result = false;
        if (!isFDExist(A,B)) {
            if (isFDExist(B,C)) {
                result = true;//it means that A,B,C constitutes an atomic substructure
            }
        }

        //if it is an atomic substructure, build it
        if (result) {
            DirectedWeightedMultigraph<Node, LabeledLink> atomSS = new DirectedWeightedMultigraph<Node, LabeledLink>(LabeledLink.class);

            atomSS.addVertex(A);
            atomSS.addVertex(B);
            atomSS.addVertex(C);

            //add the edge between A and B
            LabeledLink labeledLinkAB = tree.getEdge(A,B);
            if (labeledLinkAB == null) {
                labeledLinkAB = tree.getEdge(B,A);
                atomSS.addEdge(B,A,labeledLinkAB);
            } else {
                atomSS.addEdge(A,B,labeledLinkAB);
            }

            //add the edge between B and C
            LabeledLink labeledLinkBC = tree.getEdge(B,C);
            if (labeledLinkBC == null) {
                labeledLinkBC = tree.getEdge(C,B);
                atomSS.addEdge(C,B,labeledLinkBC);
            } else {
                atomSS.addEdge(B,C,labeledLinkBC);
            }

            atomStructures.add(atomSS);//add this atomic substructure to the list
        }
        return result;
    }


    /**check if there is functional dependency between node A and B *
     *@param A determinant
     *@param B dependent
     * @return if there exist functional dependent from A to B, return true. Other, return false
     * */
    public boolean isFDExist (Node A, Node B) {
        boolean result = false;

        //loop all the functional dependency of Steiner tree
        for (FD fd : fdSet) {
            String determinantLabel = fd.getDeterminant();
            String dependdentLabel = fd.getDependent();
            if (A.getUri().equals(determinantLabel)) {
                if (B.getUri().equals(dependdentLabel)) {
                    result = true;
                }
            }
        }
        return result;
    }


    /**Rule 3: If there exist a functional dependency 𝐴→𝐵, then 𝐴 should be neighbouring to 𝐵 in the semantic model
     * @return if A is neighboring B, return true. Otherwise, return false.
     **/
    public boolean checkNeighboringRelation () {
        boolean finalResult = true;//assume Steiner tree complies this rule
        boolean result[] = new boolean[fdSet.size()];
        int i = 0;
        for (FD fd : fdSet) {
            String determinantLabel = fd.getDeterminant();
            String dependentLabel = fd.getDependent();

            //
            Set<Node> allNodes = tree.vertexSet();

            //set up a hash map. key: node label (uri), value: node
            Map<String, Node> nodeMap = new HashMap<>();
            for (Node node : allNodes) {
                String nodeUri = node.getUri();//get id of node
                nodeMap.put(nodeUri, node);
            }

            //Get determinant and dependent
            Node determinant = nodeMap.get(determinantLabel);
            Node dependent = nodeMap.get(dependentLabel);

            //check if there is a direct link between determinant and dependent
            LabeledLink labeledLink = tree.getEdge(determinant,dependent);
            if (labeledLink == null) {
                labeledLink = tree.getEdge(dependent,determinant);
                if (labeledLink != null) {
                    result[i] = true;
                } else {
                    result[i] = false;
                }
            }else {
                result[i] = true;
            }
            i++;
        }

        for (int j = 0; j < result.length; j++) {

            if (!result[j]) {

                finalResult = false;
            }
        }

        return finalResult;
    }



    /**decomposition of Steiner tree using functional dependency*
     *@create 13 Mar 2019
     *@param startPointID ID of the starting point for DFS
     * */
    public void fdDecompose (int startPointID) {
        //DFS
        try {
            List<Integer> nList = this.DFS(startPointID);//start from node 00.
            List<Node> nodeList = this.getNodeListByID(nList);
            System.out.println("All of the nodes have been sorted by DFS!");

            //iterate all the nodes according to DFS order
            for (int i = 0; i < (nodeList.size()-2); i++) {
                Node A = nodeList.get(i);
                Node B = nodeList.get(i+1);
                Node C = nodeList.get(i+2);

                //try to find atomic substructure
                findAtomicSubStructure(A,B,C);

                //try to detect anchor point
                findAnchors(A,B,C);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**check if a graph is connected or not**/


    /**museum_crm_test decomposition function *
     * @from 21 Feb 2019
     * */
    public static void main (String args[]) {
        DirectedWeightedMultigraph<Node,LabeledLink> graph = new DirectedWeightedMultigraph<>(LabeledLink.class);
//        Node node00 = new InternalNode("00",new Label("A"));
//        Node node01 = new InternalNode("01",new Label("B"));
//        Node node02 = new InternalNode("02",new Label("C"));
//        Node node03 = new InternalNode("03", new Label("D"));
//        Node node04 = new InternalNode("04", new Label("E"));
//        Node node05 = new InternalNode("05", new Label("F"));
//        Node node06 = new InternalNode("06", new Label("G"));
//        Node node07 = new InternalNode("07", new Label("H"));
//        Node node08 = new InternalNode("08", new Label("I"));
//        Node node09 = new InternalNode("09", new Label("J"));
//        Node node10 = new InternalNode("10", new Label("K"));
//        Node node11 = new InternalNode("11", new Label("L"));
//        Node node12 = new InternalNode("12", new Label("M"));
//        Node node13 = new InternalNode("13", new Label("N"));
//        Node node14 = new InternalNode("14", new Label("O"));
//        Node node15 = new InternalNode("15", new Label("P"));
//        Node node16 = new InternalNode("16", new Label("Q"));
//        Node node17 = new InternalNode("17", new Label("R"));
//        Node node18 = new InternalNode("18", new Label("S"));
//
//        graph.addVertex(node00);
//        graph.addVertex(node01);
//        graph.addVertex(node02);
//        graph.addVertex(node03);
//        graph.addVertex(node04);
//        graph.addVertex(node05);
//        graph.addVertex(node06);
//        graph.addVertex(node07);
//        graph.addVertex(node08);
//        graph.addVertex(node09);
//        graph.addVertex(node10);
//        graph.addVertex(node11);
//        graph.addVertex(node12);
//        graph.addVertex(node13);
//        graph.addVertex(node14);
//        graph.addVertex(node15);
//        graph.addVertex(node15);
//        graph.addVertex(node16);
//        graph.addVertex(node17);
//        graph.addVertex(node18);
//
//
//        LabeledLink l01 = new ObjectPropertyLink(new RandomGUID().toString(), new Label("1"), ObjectPropertyType.Direct);//create a new link
//        LabeledLink l02 = new ObjectPropertyLink(new RandomGUID().toString(), new Label("1"), ObjectPropertyType.Direct);//create a new link
//        LabeledLink l03 = new ObjectPropertyLink(new RandomGUID().toString(), new Label("1"), ObjectPropertyType.Direct);//create a new link
//        LabeledLink l04 = new ObjectPropertyLink(new RandomGUID().toString(), new Label("1"), ObjectPropertyType.Direct);//create a new link
//        LabeledLink l05 = new ObjectPropertyLink(new RandomGUID().toString(), new Label("1"), ObjectPropertyType.Direct);//create a new link
//        LabeledLink l06 = new ObjectPropertyLink(new RandomGUID().toString(), new Label("1"), ObjectPropertyType.Direct);//create a new link
//        LabeledLink l07 = new ObjectPropertyLink(new RandomGUID().toString(), new Label("1"), ObjectPropertyType.Direct);//create a new link
//        LabeledLink l08 = new ObjectPropertyLink(new RandomGUID().toString(), new Label("1"), ObjectPropertyType.Direct);//create a new link
//        LabeledLink l09 = new ObjectPropertyLink(new RandomGUID().toString(), new Label("1"), ObjectPropertyType.Direct);//create a new link
//        LabeledLink l10 = new ObjectPropertyLink(new RandomGUID().toString(), new Label("1"), ObjectPropertyType.Direct);//create a new link
//        LabeledLink l11 = new ObjectPropertyLink(new RandomGUID().toString(), new Label("1"), ObjectPropertyType.Direct);//create a new link
//        LabeledLink l12 = new ObjectPropertyLink(new RandomGUID().toString(), new Label("1"), ObjectPropertyType.Direct);//create a new link
//        LabeledLink l13 = new ObjectPropertyLink(new RandomGUID().toString(), new Label("1"), ObjectPropertyType.Direct);//create a new link
//        LabeledLink l14 = new ObjectPropertyLink(new RandomGUID().toString(), new Label("1"), ObjectPropertyType.Direct);//create a new link
//        LabeledLink l15 = new ObjectPropertyLink(new RandomGUID().toString(), new Label("1"), ObjectPropertyType.Direct);//create a new link
//        LabeledLink l16 = new ObjectPropertyLink(new RandomGUID().toString(), new Label("1"), ObjectPropertyType.Direct);//create a new link
//        LabeledLink l17 = new ObjectPropertyLink(new RandomGUID().toString(), new Label("1"), ObjectPropertyType.Direct);//create a new link
//        LabeledLink l18 = new ObjectPropertyLink(new RandomGUID().toString(), new Label("1"), ObjectPropertyType.Direct);//create a new link
//
//        graph.addEdge(node00,node01,l01);
//        graph.addEdge(node00,node02,l02);
//        graph.addEdge(node01,node03,l03);
//        graph.addEdge(node01,node04,l04);
//        graph.addEdge(node02,node05,l05);
//        graph.addEdge(node02,node06,l06);
//        graph.addEdge(node03,node07,l07);
//        graph.addEdge(node03,node08,l08);
//        graph.addEdge(node04,node09,l09);
//        graph.addEdge(node04,node10,l10);
//        graph.addEdge(node05,node11,l11);
//        graph.addEdge(node05,node12,l12);
//        graph.addEdge(node06,node13,l13);
//        graph.addEdge(node06,node14,l14);
//        graph.addEdge(node08,node15,l15);
//        graph.addEdge(node08,node16,l16);
//        graph.addEdge(node12,node17,l17);
//        graph.addEdge(node12,node18,l18);

        Node node00 = new InternalNode("00",new Label("P"));
        Node node01 = new InternalNode("01",new Label("V"));
        Node node02 = new InternalNode("02",new Label("PL"));
        Node node03 = new InternalNode("03",new Label("L"));

        graph.addVertex(node00);
        graph.addVertex(node01);
        graph.addVertex(node02);
        graph.addVertex(node03);

        LabeledLink l01 = new ObjectPropertyLink(new RandomGUID().toString(), new Label("1"), ObjectPropertyType.Direct);//create a new link
        LabeledLink l02 = new ObjectPropertyLink(new RandomGUID().toString(), new Label("2"), ObjectPropertyType.Direct);
        LabeledLink l03 = new ObjectPropertyLink(new RandomGUID().toString(), new Label("3"), ObjectPropertyType.Direct);

        graph.addEdge(node01, node00, l01);
        graph.addEdge(node01, node02, l02);
        graph.addEdge(node02, node03, l03);

        SteinerTree steinerTree = new SteinerTree(graph);

        /**Init the Adjacent List of the Steiner tree**/
        System.out.println("Now initialize the Adjacent list of Steiner tree! ");
        steinerTree.init();

        //put some functional dependencies on the Steiner tree
        FD fd1 = new FD ("V","PL");//that is, Vehicle -> Parking Lot
        FD fd2 = new FD ("L","PL");//that is, Location -> Parking Lot
        steinerTree.addFD(fd1);//add functional dependency to the Steiner tree
        steinerTree.addFD(fd2);//add functional dependency to the Steiner tree

        int i = 0;
        System.out.println("Following is the Depth First Traversal " + " starting form vertex: " + i);

        try {

            boolean result = steinerTree.checkNeighboringRelation();
            System.out.println(result);
            if (result) {
                System.out.println("this steiner tree complies with functional dependency based neighboring relationship rule!");
                steinerTree.fdDecompose(0);//DFS search from node00
                System.out.println(steinerTree.anchors.size() + " anchors have been detected!");
                System.out.println(steinerTree.atomStructures.size() + " atomic substructures have been detected!");

                steinerTree.decompose();//get sub-structures
                System.out.println(steinerTree.subStructures.size() + " substructures have been found! ");
            } else {
                System.out.println("this steiner tree does not comply with functional dependency based neighboring relationship rule!");
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }


        System.out.println("done!");
    }

}
