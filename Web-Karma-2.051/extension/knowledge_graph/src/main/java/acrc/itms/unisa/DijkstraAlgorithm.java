package acrc.itms.unisa;

import edu.isi.karma.rep.alignment.InternalNode;
import edu.isi.karma.rep.alignment.Label;
import edu.isi.karma.rep.alignment.LabeledLink;
import edu.isi.karma.rep.alignment.Node;
import org.jgrapht.graph.DirectedWeightedMultigraph;

import java.util.*;


public class DijkstraAlgorithm {

    /**Compares the actual distance with the newly calculated one**/
    private static void calculateMinimumDistance(Node evaluationNode, Integer edgeWeigh, Node sourceNode) {
        Integer sourceDistance = sourceNode.getDistance();
        if (sourceDistance + edgeWeigh < evaluationNode.getDistance()) {
            evaluationNode.setDistance(sourceDistance + edgeWeigh);
            LinkedList<Node> shortestPath = new LinkedList<>(sourceNode.getShortestPath());
            shortestPath.add(sourceNode);
            evaluationNode.setShortestPath(shortestPath);
        }
    }

    /**Returns the node with the lowest distance from the unsettled nodes set**/
    private static Node getLowestDistanceNode(Set<Node> unsettledNodes){
        Node lowestDistanceNode = null;
        int lowestDistance = Integer.MAX_VALUE;
        for (Node node : unsettledNodes) {
            int nodeDistance = node.getDistance();
            if(nodeDistance < lowestDistance){
                lowestDistance = nodeDistance;
                lowestDistanceNode = node;
            }
        }
        return lowestDistanceNode;
    }

    /**Find the shortest path between the source and all of the other nodes in the graph*
     * @param graph graph to be searched
     * @param source the beginning node
     * @return
     * */
    public static DirectedWeightedMultigraph<Node,LabeledLink> calculateShortestPathFromSource(DirectedWeightedMultigraph<Node, LabeledLink> graph, Node source){

        source.setDistance(0);

        Set<Node> settledNodes = new HashSet<>();
        Set<Node> unsettledNodes = new HashSet<>();

        unsettledNodes.add(source);

        while (unsettledNodes.size() != 0) {
            Node currentNode = getLowestDistanceNode(unsettledNodes);//get the node from the unsettled node set with the lowest distance
            unsettledNodes.remove(currentNode);
            for (Map.Entry<Node, Integer> adjacencyPair : currentNode.getAdjacentNodes().entrySet()) {
                Node adjacentNode = adjacencyPair.getKey();
                Integer edgeWeight = adjacencyPair.getValue();
                if (!settledNodes.contains(adjacentNode)) {
                    calculateMinimumDistance(adjacentNode,edgeWeight,currentNode);
                    unsettledNodes.add(adjacentNode);
                }
            }
            settledNodes.add(currentNode);
        }
        return graph;
    }

    /**Find the shortest path between two nodes in the graph*
     * @param source the beginning node
     * @param target the targeting node
     * @revised 7 Dec 2018
     * @precondition the distance of the source should be 0, the distance of other nodes in the graph should be the maximum
     * @return
     * */
    public static List<Node> calculateShortestPathFromSource(Node source, Node target){
        source.setDistance(0);

        Set<Node> settledNodes = new HashSet<>();
        Set<Node> unsettledNodes = new HashSet<>();
        List<Node> shortestPathNodes = new ArrayList<>();//save the nodes between the source and the target on the shortest path (if the shortest path exists).

        unsettledNodes.add(source);

        while (unsettledNodes.size() != 0) {
            Node currentNode = getLowestDistanceNode(unsettledNodes);//get the node from the unsettled node set with the lowest distance
            unsettledNodes.remove(currentNode);
            if(currentNode.getId().equals(target.getId())){//means that the current node is the target node
                shortestPathNodes = currentNode.getShortestPath();
                break;//if the current node is the target node, then it is not necessary to explore further
            }else {
                for (Map.Entry<Node, Integer> adjacencyPair : currentNode.getAdjacentNodes().entrySet()) {
                    Node adjacentNode = adjacencyPair.getKey();
                    Integer edgeWeight = adjacencyPair.getValue();
                    if (!settledNodes.contains(adjacentNode)) {
                        calculateMinimumDistance(adjacentNode,edgeWeight,currentNode);
                        unsettledNodes.add(adjacentNode);
                    }
                }
            }
            settledNodes.add(currentNode);
        }
        if (!shortestPathNodes.isEmpty()) {// if shortest path exists
            shortestPathNodes.add(target);//add the last node in the part. 24 Oct 2018
        }

        return shortestPathNodes;
    }

    public static void printNodesPath(List<Node> shortestPath) {
        int index = 0;
        if((shortestPath.size() == 0) || (shortestPath.size() == 1)){
            System.out.println("there is not shortest path that could be found!");
        }else if(shortestPath.size() == 2){
            System.out.println("A shortest path is found. However, it is not legal since it means there has already been an edge between the source and the target!");
        }else {
            System.out.println("The shortest path has been found!");
            for(Node node : shortestPath){
                System.out.println(index + " " + node.getId());
                index++;
            }
        }
    }

//    public static void printEdgesOnPath(List<LabeledLink> labeledLinks){
//        if(!labeledLinks.isEmpty()){
//            System.out.println("now begin to print edges: ");
//            for(LabeledLink labeledLink : labeledLinks){
//                System.out.println("the start point is " + labeledLink.getSource().getId() + " the target point is: " + labeledLink.getTarget().getId());
//            }
//        }     else {
//
//            System.out.println("Since there are not edges on the path, there is not print out!");
//        }
//
//    }

//    /**init the KG to a new round of shortest path search**/
//    public static void init (DirectedWeightedMultigraph<Node, LabeledLink> directedWeightedMultigraph) {
//        Set<Node> nodeSet = directedWeightedMultigraph.vertexSet();
//        for (Node node : nodeSet) {
//            node.setDistance(Integer.MAX_VALUE);
//            List<Node> nodeList = node.getShortestPath();//get the shortest path for each node
//            nodeList.clear();//clear
//            node.setShortestPath(nodeList);//re-set
//        }
//    }


    public static void main(String args[]){

        
        Node nodeA = new InternalNode("B75", new Label("12"));
        Node nodeB = new InternalNode("DF9", new Label("7"));
        Node nodeC = new InternalNode("A2E", new Label("3"));

        nodeB.addDestination(nodeA, 1);
        nodeA.addDestination(nodeB, 1);
        nodeB.addDestination(nodeC, 1);
        nodeC.addDestination(nodeB, 1);

        DirectedWeightedMultigraph<Node, LabeledLink> graph = new DirectedWeightedMultigraph<>(LabeledLink.class);

        graph.addVertex(nodeA);
        graph.addVertex(nodeB);
        graph.addVertex(nodeC);

//        Node node1 = new InternalNode("node1", new Label("U"));
//        Node node2 = new InternalNode("node2", new Label("BA"));
//        Node node3 = new InternalNode("node3", new Label("P"));
//        Node node4 = new InternalNode("node4", new Label("BA"));
//        Node node5 = new InternalNode("node5", new Label("BA"));
//
//        Node node6 = new InternalNode("node6", new Label("P"));
//        Node node7 = new InternalNode("node7", new Label("BA"));
//
//        Node node8 = new InternalNode("node8", new Label("P"));
//        Node node9 = new InternalNode("node9", new Label("BA"));
//        Node node10 = new InternalNode("node10", new Label("U"));
//
//        //node1.addDestination(node2,1);
//        node1.addDestination(node3,1);
//        //node1.addDestination(node4,1);
//        //node1.addDestination(node5,1);
//        node1.addDestination(node6,1);
//        node1.addDestination(node7,1);
//        node1.addDestination(node8,1);
//        node1.addDestination(node9,1);
//
//        //node2.addDestination(node1,1);
//        node2.addDestination(node3,1);
//
//        node3.addDestination(node1,1);
//        node3.addDestination(node2,1);
//        node3.addDestination(node4,1);
//        node3.addDestination(node5,1);
//
//        //node4.addDestination(node1,1);
//        node4.addDestination(node3,1);
//
//        //node5.addDestination(node1,1);
//        node5.addDestination(node3,1);
//
//        node6.addDestination(node1,1);
//        node6.addDestination(node7,1);
//
//        node7.addDestination(node1,1);
//        node7.addDestination(node6,1);
//
//        node8.addDestination(node1,1);
//        node8.addDestination(node9,1);
//
//        node9.addDestination(node1,1);
//        node9.addDestination(node8,1);
//
//        DirectedWeightedMultigraph<Node, LabeledLink> graph = new DirectedWeightedMultigraph<>(LabeledLink.class);
//
//        graph.addVertex(node1);
//        graph.addVertex(node2);
//        graph.addVertex(node3);
//        graph.addVertex(node4);
//        graph.addVertex(node5);
//        graph.addVertex(node6);
//        graph.addVertex(node7);
//        graph.addVertex(node8);
//        graph.addVertex(node9);
//        graph.addVertex(node10);

        //int distance = 0;
        List<Node> shortestPath = calculateShortestPathFromSource(nodeA,nodeC);
        //List<LabeledLink> links = showEdgesOnPath(graph, shortestPath);//get all of the edges on the path
        System.out.println("the length of 'shortestPath' is " + shortestPath.size());
        try{
            printNodesPath(shortestPath);
            System.out.println("the shortest distance is: " + nodeC.getDistance());
        }catch (Exception e){
            e.printStackTrace();
        }
        System.out.println("computing is done........");


    }


}
