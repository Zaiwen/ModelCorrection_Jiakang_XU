package acrc.itms.unisa;

import edu.isi.karma.util.RandomGUID;
import jnr.ffi.annotations.Direct;
import org.jgrapht.Graph;
import org.jgrapht.VertexFactory;
import org.jgrapht.generate.RandomGraphGenerator;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedWeightedMultigraph;
import org.jgrapht.graph.SimpleGraph;

import edu.isi.karma.rep.alignment.Node;
import edu.isi.karma.rep.alignment.LabeledLink;
import edu.isi.karma.rep.alignment.ObjectPropertyLink;
import edu.isi.karma.rep.alignment.Label;
import edu.isi.karma.rep.alignment.ObjectPropertyType;
import edu.isi.karma.rep.alignment.InternalNode;


import java.util.*;


/**Generates a random graph.*
 * 2 Aug 2018
 * */
public class GraphGeneration {

    /**this function is to create a random graph with a certain vertices and edges*
     *@unused unused because it might create a graph that is not completely connected
     * @param numOfVertex: the number of vertices
     * @param numOfEdge: the number of edges
     * */
    public static Graph<Integer,DefaultEdge> createSmallRandomGraph (int numOfVertex, int numOfEdge){

        RandomGraphGenerator<Integer,DefaultEdge> randomGen = new RandomGraphGenerator<Integer, DefaultEdge>(numOfVertex,numOfEdge);
        Graph<Integer,DefaultEdge> randomGraph = new SimpleGraph<>(DefaultEdge.class);
        VertexFactory<Integer> factory = new VertexFactory<Integer>(){
            int gid;

            @Override
            public Integer createVertex() {
                return gid++;
            }
        };
        randomGen.generateGraph(randomGraph,factory,null);

        /**remove orphan vertex**/
        Set<Integer> nodes = randomGraph.vertexSet();
        Set<DefaultEdge> edges = randomGraph.edgeSet();
        Set<String> allEdgePoints = new HashSet<>();
        Iterator<DefaultEdge> iterator = edges.iterator();
        while (iterator.hasNext()){
            DefaultEdge defaultEdge = iterator.next();
            String edgeString = defaultEdge.toString();
            String start = edgeString.substring(edgeString.indexOf("(")+1,edgeString.indexOf(":")-1);
            String end = edgeString.substring(edgeString.indexOf(":")+2,edgeString.indexOf(")"));
            allEdgePoints.add(start);
            allEdgePoints.add(end);
        }
        Integer[] nodeArray = nodes.toArray(new Integer[nodes.size()]);//change the set called 'nodes' to an array
        for(Integer node : nodeArray){
            String nodeString = String.valueOf(node);
            if(!allEdgePoints.contains(nodeString)){
                randomGraph.removeVertex(node);
            }
        }

        System.out.println("A pattern graph with " + numOfVertex + " vertices and " + numOfEdge + " edges has been created.");
        return randomGraph;

    }

    /**this function is used to create a random graph with a certain number of vertices*
     * @param numOfVertex the number of vertices. Let's say 'N'
     * @return a random graph. the number of edges is: N(N-1)/2
     * */
    public static Graph<Integer, DefaultEdge> createSmallRandomGraph2(int numOfVertex){
        Graph<Integer,DefaultEdge> randomGraph = new SimpleGraph<>(DefaultEdge.class);//create an empty graph
        for(int i=0; i < numOfVertex; i++){
            randomGraph.addVertex(i);//add node to the graph
        }
        for(int i=0; i < (numOfVertex-1); i++){
            for(int j=(i+1); j<numOfVertex; j++){
                randomGraph.addEdge(i,j);//add edge to the graph
            }
        }
        int numOfEdge = randomGraph.edgeSet().size();
        System.out.println("A pattern graph with " + numOfVertex + " vertices and " + numOfEdge + " edges has been created.");
        return randomGraph;
    }

    /**this function is used to create a small directed weighted graph.*
     * @param numOfVertex number of Vertices in this graph
     * @param nodeLabelSet the set of labels, which is used to label nodes in this graph
     * */
    public static DirectedWeightedMultigraph<Node, LabeledLink> createLabeledGraph(int numOfVertex, Set<String> nodeLabelSet){

        /**create an initial random graph**/
        Graph<Integer, DefaultEdge> initialGraph = createSmallRandomGraph2(numOfVertex);

        /**deal with the set of node label**/
        String [] nodeLabelArray = new String[nodeLabelSet.size()];
        int c = 0;
        for(String x : nodeLabelSet) nodeLabelArray[c++] = x;

        /**Get all of vertices and edges of this graph**/
        Set<Integer> allNodes = initialGraph.vertexSet();
        Set<DefaultEdge> allEdges = initialGraph.edgeSet();

        int total = allNodes.size();
        Random random = new Random();

        /**put all of the nodes into a hash map, indexed by the node id**/
        Map<String,Node> nodeMap = new HashMap<>();

        DirectedWeightedMultigraph<Node,LabeledLink> graph = new DirectedWeightedMultigraph<>(LabeledLink.class);

        /**Assign a random label on each node**/
        Iterator<Integer> iterator = allNodes.iterator();
        while (iterator.hasNext()){

            Integer index = iterator.next();
            /**generate a random number pointing to a random label in the array **/
            Integer i = random.nextInt(nodeLabelArray.length);
            /**Get the random label**/
            Label label = new Label(nodeLabelArray[i]);
            /**Build a node, where the index and the random label are assigned**/
            Node node = new InternalNode(index.toString(),label);
            /**put this node into the indexed node map**/
            nodeMap.put(index.toString(),node);
            /**add this new node to the directed weighted graph**/
            graph.addVertex(node);
        }

        /**deal with all of the edges in the initial graph**/
        Iterator<DefaultEdge> it = allEdges.iterator();
        while (it.hasNext()){
            DefaultEdge defaultEdge = it.next();
            String edgeString = defaultEdge.toString();
            String start = edgeString.substring(edgeString.indexOf("(")+1,edgeString.indexOf(":")-1);
            String end = edgeString.substring(edgeString.indexOf(":")+2,edgeString.indexOf(")"));
            /**create a new labeled link, and put it into the graph**/
            Label label = new Label("1");//only set the url as 1
            LabeledLink labeledLink = new ObjectPropertyLink(new RandomGUID().toString(),label,ObjectPropertyType.Direct);
            graph.addEdge(nodeMap.get(start),nodeMap.get(end),labeledLink);
        }
        return graph;
    }

    /**deep clone a directed weighed multiple graph*
     * @param initialGraph initial directed weighed multiple graph
     * @return a copied directed weighed multiple graph
     * @note maybe useless
     * */
    public static Graph<Node,LabeledLink> deepClone (Graph<Node,LabeledLink> initialGraph){

        Graph<Node,LabeledLink> copiedGraph = new DirectedWeightedMultigraph<>(LabeledLink.class);
        /**Get all of the vertices and edges from the original graph**/
        Set<Node> allNodes = initialGraph.vertexSet();
        Set<LabeledLink> allEdges = initialGraph.edgeSet();

        /**put all of the nodes of the copied graph into a hash map, indexed by the node id**/
        Map<String,Node> nodeMap = new HashMap<>();

        /**copy all the nodes of the original graph, and put it into the copied graph**/
        for(Node node : allNodes){
            Node copiedNode = node.clone();//deep clone
            copiedGraph.addVertex(copiedNode);
            /**put this copied node into the indexed node map**/
            nodeMap.put(node.getId(),node);
        }

        /**copy all the edges of the original graph**/
        for(LabeledLink labeledLink : allEdges){
            LabeledLink copiedLink = labeledLink.clone();//deep clone
            String startIndex = labeledLink.getSource().getId();
            String endIndex = labeledLink.getTarget().getId();
            copiedGraph.addEdge(nodeMap.get(startIndex),nodeMap.get(endIndex),copiedLink);
        }

        return copiedGraph;
    }

    /**deep clone a directed weighed multiple graph. At the same time, renumber the node of a graph*
     *
     * @param initialGraph the initial graph to be copied and renumbered
     * @param startNum the staring number of the new graph. if the node numbering of a graph is 0,1,2,3...,
     *                 after renumbering, the new node numbering of this graph is x, x+1,x+2,x+3,....(x is the start number)
     * */
    public static Graph<Node,LabeledLink> deepClone (Graph<Node,LabeledLink> initialGraph, int startNum){

        Graph<Node,LabeledLink> copiedGraph = new DirectedWeightedMultigraph<>(LabeledLink.class);
        /**Get all of the vertices and edges from the original graph**/
        Set<Node> allNodes = initialGraph.vertexSet();
        Set<LabeledLink> allEdges = initialGraph.edgeSet();

        /**create a hash map to store the old node index and the corresponding updated node index**/
        Map<String,Node> nodeIndexMap = new HashMap<>();//<Key,Value> Key is the old node index. Value is the updated node
        /**loop all the nodes of this graph, and renumber them.**/
        for(Node node : allNodes){
            Node copiedNode = node.clone();//deep clone the node
            String currentIndex = node.getId();//get the current index(id)
            copiedNode.setId(String.valueOf(startNum));//set the updated index(id) on the copied node
            nodeIndexMap.put(currentIndex,copiedNode);//<Key,Value> Key is the old node index. Value is the copied node with updated index(id)
            copiedGraph.addVertex(copiedNode);//get this copied node inserted into copied graph
            startNum++;
        }

        /**copy all the edges of the original graph, and renumber all the edges of this graph**/
        for(LabeledLink labeledLink : allEdges){
            Label label = labeledLink.getLabel();
            //String id = labeledLink.getId();
            LabeledLink copiedLink = new ObjectPropertyLink(new RandomGUID().toString(),label,ObjectPropertyType.Direct);
            String initialStartIndex = labeledLink.getSource().getId();//get the initial start point
            String initialEndIndex = labeledLink.getTarget().getId();//get the initial end point
            Node start = nodeIndexMap.get(initialStartIndex);
            Node end = nodeIndexMap.get(initialEndIndex);
            copiedGraph.addEdge(start,end,copiedLink);//get this copied link inserted into copied graph
        }

        return copiedGraph;
    }

    /**add random edges between two substructures(sub-graphs) in a big graph, making the big graph growing*
     * @param sub1 sub-structure within a big graph
     * @param sub2 another sub-structure within a big graph
     * @param bigGraph big graph
     * @param m the number of random edges added to the big graph between substructures
     * @precondition the big graph should contain sub1 and sub2 in advance
     * @problme there might be some multiple edges generated, but with very low possibilities
     * */
    public static void addRandomLinks (Graph<Node,LabeledLink> sub1, Graph<Node,LabeledLink> sub2, Graph<Node,LabeledLink> bigGraph,int m) throws Exception{

        /**get all the nodes and edges from 'sub1' and 'sub2'**/
        Set<Node> sub1Nodes = sub1.vertexSet();
        Set<Node> sub2Nodes = sub2.vertexSet();

        /**Convert set1 to an array**/
        Node [] sub1NodeArray = new Node[sub1Nodes.size()];
        int c = 0;
        for(Node x : sub1Nodes) sub1NodeArray[c++] = x;

        /**Convert set2 to an array**/
        Node [] sub2NodeArray = new Node[sub2Nodes.size()];
        int c2 = 0;
        for(Node y : sub2Nodes) sub2NodeArray[c2++] = y;

        Random random = new Random();

        /**avoid m is too many**/
        if(m>sub1NodeArray.length) {
            throw new Exception();
        }

        for(int i=0;i<m;i++){
            /**Fetch a random node from 'sub1Nodes' and 'sub2Nodes'**/
            Integer index1 = random.nextInt(sub1NodeArray.length);
            Integer index2 = random.nextInt(sub2NodeArray.length);
            Node node1 = sub1NodeArray[index1];//get a random node from sub1
            Node node2 = sub2NodeArray[index2];//get a random node from sub2

            /**create a link between node 1 and 2**/
            Label label = new Label("1");//only set the url as 1
            LabeledLink labeledLink = new ObjectPropertyLink(new RandomGUID().toString(),label,ObjectPropertyType.Direct);
            int direction = random.nextInt(2);
            if(direction==0){
                bigGraph.addEdge(node1,node2,labeledLink);
            }else {
                bigGraph.addEdge(node2,node1,labeledLink);
            }
        }
    }

    /**create a (random) big graph based on a seed(pattern) graph*
     *@param seed a pattern graph
     *@param frequency the times that the pattern will be reproduced
     *@param m the number of random edges added to the big graph between substructures
     *@return a big graph
     * */
    public static DirectedWeightedMultigraph<Node,LabeledLink> createBigGraph(Graph<Node,LabeledLink> seed, int frequency, int m){
        /**create an empty graph**/
        DirectedWeightedMultigraph<Node,LabeledLink> bigGraph = new DirectedWeightedMultigraph<>(LabeledLink.class);

        /**copy the small graph into a certain times, and add copies into the big graph**/
        List<Graph<Node,LabeledLink>> seedList = new ArrayList<>();
        int i;
        int startIdentifier=0;
        int seedNodeNum=seed.vertexSet().size();
        for(i=0; i<frequency; i++){
            Graph<Node, LabeledLink> copy = deepClone(seed,startIdentifier);
            startIdentifier=startIdentifier+seedNodeNum;
            Set<Node> allNodes = copy.vertexSet();
            for(Node node : allNodes){
                bigGraph.addVertex(node);//add nodes to the big graph
            }
            Set<LabeledLink> allEdges = copy.edgeSet();
            for(LabeledLink labeledLink : allEdges){
                Node start = labeledLink.getSource();
                Node end = labeledLink.getTarget();
                bigGraph.addEdge(start,end,labeledLink);
            }
            seedList.add(copy);
        }
        System.out.println("The initial pattern graph has been copied " + i + " times.");

        /**add random edges between sub-structures**/
        try{
            for(int j=0;j<frequency-1;j++){
                for(int k=j+1;k<frequency;k++){
                    addRandomLinks(seedList.get(j),seedList.get(k),bigGraph,m);
                }
            }
        }catch (Exception e){
            System.err.println(e);
        }
        System.out.println("random edges have been added to the big graph.");
        int nodeNumBigG=bigGraph.vertexSet().size();
        int edgeNumBigG=bigGraph.edgeSet().size();
        System.out.println("the number of node: " + nodeNumBigG + " , the number of edges: " + edgeNumBigG);
        return bigGraph;

    }

    /**create a (random) big graph based on a number of seed(pattern) graphs*
     * @param seedMap a map of pattern graphs.
     *                Key: a substructure,
     *                Value: the times that this substructure will be reproduced
     * @param m the number of random edges added to the big graph between substructures(i.e., any two seeds)
     * @return a big graph
     * */
    public static DirectedWeightedMultigraph<Node,LabeledLink> createBigGraph(Map<DirectedWeightedMultigraph<Node,LabeledLink>,Integer> seedMap, int m) throws Exception{

        /**create an empty graph**/
        DirectedWeightedMultigraph<Node,LabeledLink> bigGraph = new DirectedWeightedMultigraph<>(LabeledLink.class);

        /**copy the small graph into a certain times, and add copies into the big graph**/
        List<Graph<Node,LabeledLink>> subStructureList = new ArrayList<>();//create a list to save all these sub-structures
        int startIdentifier=0;
        int index=0;//index of a substructure(i.e. steiner tree)
        Iterator<Map.Entry<DirectedWeightedMultigraph<Node,LabeledLink>,Integer>> iterator = seedMap.entrySet().iterator();
        while (iterator.hasNext()){
            index++;
            Map.Entry<DirectedWeightedMultigraph<Node,LabeledLink>,Integer> entry = iterator.next();
            DirectedWeightedMultigraph<Node,LabeledLink> seed = entry.getKey();//get a seed
            Integer frequency = entry.getValue();//get frequency of the seed
            int seedNodeNum = seed.vertexSet().size();//get the number of vertices of this seed
            /**copy the seed for a certain number of times**/
            for(int i=0; i<frequency; i++){
                Graph<Node,LabeledLink> copy = deepClone(seed,startIdentifier);
                startIdentifier=startIdentifier+seedNodeNum;
                Set<Node> allNodes = copy.vertexSet();
                for(Node node : allNodes){
                    bigGraph.addVertex(node);//add nodes to the big graph
                }
                Set<LabeledLink> allEdges = copy.edgeSet();
                for(LabeledLink labeledLink : allEdges){
                    Node start = labeledLink.getSource();
                    Node target = labeledLink.getTarget();
                    bigGraph.addEdge(start,target,labeledLink);
                }
                subStructureList.add(copy);
            }
            System.out.println("the " + index + " substructure has been copied for " + frequency + " times!");
        }

        /**add random edges between different sub-structures**/
        int subStructureNum = subStructureList.size();
        try{
            for(int j=0;j<subStructureNum;j++){
                for(int k=j+1; k<subStructureNum; k++){
                    addRandomLinks(subStructureList.get(j),subStructureList.get(k),bigGraph,m);
                }
            }
        }catch (Exception e){
            System.err.println(e);
        }
        int nodeNumBigG=bigGraph.vertexSet().size();
        int edgeNumBigG=bigGraph.edgeSet().size();
        System.out.println("the number of node: " + nodeNumBigG + " , the number of edges: " + edgeNumBigG);

        return bigGraph;
    }



//    /**create a random knowledge graph based on the Common data model*
//     * @param cdm the common data model
//     * @param threshold the maximum number of nodes in the knowledge graph
//     * @return a random knowledge graph based on cdm
//     * */
//    public static DirectedWeightedMultigraph<Node,LabeledLink> generateKG (DirectedWeightedMultigraph<Node,LabeledLink> cdm, int threshold) throws  Exception
//    {
//
//        if(cdm.vertexSet().size()<1)
//            throw new Exception("The CDM is empty. Knowledge graph can not be generated.");
//
//        DirectedWeightedMultigraph<Node,LabeledLink> knowledgeGraph = new DirectedWeightedMultigraph<>(LabeledLink.class);//create an empty knowledge graph
//
//        Node initial = (Node) getRandomElementFromSet(cdm.vertexSet());//Randomly pick a node from the cdm graph
//        knowledgeGraph.addVertex(initial);//add this node into knowledge graph
//        Set<Node> nodeSet = knowledgeGraph.vertexSet();//a set to save all the nodes in the knowledge graph.
//
//        while(knowledgeGraph.vertexSet().size() < threshold){
//
//            Node v = (Node)getRandomElementFromSet(nodeSet);//get a node randomly from the current knowledge graph
//            Set<LabeledLink> outgoingEdges = knowledgeGraph.outgoingEdgesOf(v);//get all the outgoing edges of v
//            Set<LabeledLink> incomingEdges = knowledgeGraph.incomingEdgesOf(v);//get all the incoming edges of v
//            Set<LabeledLink> allNeighbouringEdges = new HashSet<>();
//            allNeighbouringEdges.addAll(outgoingEdges);// add all the outgoing edges
//            allNeighbouringEdges.addAll(incomingEdges);// add all the incoming edges
//            LabeledLink randomLink = (LabeledLink)getRandomElementFromSet(allNeighbouringEdges);//get a random neighbouring edge randomly from the current vertex
//
//            /**case 1: randomly pick-up link is an incoming edge to the node**/
//
//
//        }
//
//        return knowledgeGraph;
//    }

//    /**Prepend a node to an existing node in the directed weighted multiple graph. 20 Aug 2018*
//     * @param graph knowledge graph
//     * @param node an existing node to be prepended
//     * @param newNode a prepending node
//     * @param relationshipType relationship type on the link pointing from the new node to the existing node
//     *
//     * */
//    public static void prependNodeToG(DirectedWeightedMultigraph<Node,LabeledLink> graph, Node node, Node newNode, String relationshipType){
//        graph.addVertex(newNode);//add this vertex into the graph
//        Label label = new Label(relationshipType);//create the label on the new edge
//        LabeledLink labeledLink = new ObjectPropertyLink(new RandomGUID().toString(),label,ObjectPropertyType.Direct);//create a new label
//        graph.addEdge(newNode,node,labeledLink);//add this new edge into the knowledge graph
//    }
//
//    /**Append a node to an existing node in a directed weighed multiple graph. 22 Aug 2018.*
//     * @param graph knowledge graph
//     * @param node an existing node to be appended
//     * @param newNode a appending node
//     * @param relationshipType relationship type on the link pointing form the existing node to the new added node
//     * */
//    public static void appendNodeToG(DirectedWeightedMultigraph<Node,LabeledLink> graph, Node node, Node newNode, String relationshipType){
//        graph.addVertex(newNode);//add this vertex into the graph
//        Label label = new Label(relationshipType);//create the label on the new edge
//        LabeledLink labeledLink = new ObjectPropertyLink(new RandomGUID().toString(),label,ObjectPropertyType.Direct);//create a new label
//        graph.addEdge(node,newNode,labeledLink);//add this new edge into the knowledge graph
//    }

//    /**generate a random integer value between 0 and a specified range**/
//    public static int getRandomIntegerBetweenRange(int max){
//        Random rand = new Random();
//        int rand_int = rand.nextInt(max+1);//generate a random integers from 0 to max
//        return rand_int;
//    }
//
//    /**extract an element randomly from a set**/
//    public static Object getRandomElementFromSet(Set hashSet) throws Exception{
//
//        if(hashSet.size()<1){
//            throw new Exception("the set is empty. No element can be extracted from it.");
//        }
//
//        Object randomElement = null;
//        int randomIndex = getRandomIntegerBetweenRange(hashSet.size());//get a random number
//        int i = 0;
//        for(Object object : hashSet){
//            if(i == randomIndex)
//                randomElement = object;
//        }
//        return randomElement;
//    }

    /**museum_crm_test function**/
    public static void main(String args[]){


        Set<String> nodeLabelSet = new HashSet<>();
        nodeLabelSet.add("10");
        nodeLabelSet.add("20");
        nodeLabelSet.add("30");
        nodeLabelSet.add("40");
        nodeLabelSet.add("50");
        int nodeNumber = 50;
        Graph<Node, LabeledLink> seed = createLabeledGraph(nodeNumber,nodeLabelSet);
        Graph<Node,LabeledLink> bigGraph = createBigGraph(seed,200,20);
        System.out.println("Done.");

    }

}
