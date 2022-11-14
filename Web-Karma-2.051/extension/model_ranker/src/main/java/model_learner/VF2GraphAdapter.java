package model_learner;


import VF2.graph.Edge;
import edu.isi.karma.rep.alignment.LabeledLink;
import edu.isi.karma.rep.alignment.Node;
import VF2.algorithm.Pair;
import VF2.graph.LGGraph;
import org.jgrapht.graph.DirectedWeightedMultigraph;

import java.io.FileNotFoundException;
import java.util.*;

public class VF2GraphAdapter {

    public static LGGraph graphAdaptToVF2(DirectedWeightedMultigraph<Node, LabeledLink> dg) throws FileNotFoundException {

        LGGraph LGGraph = new LGGraph();
        Set<Node> nodes = dg.vertexSet();
        LabelDir.getLabel();

        HashMap<Node,Integer> nodeIds = new HashMap<>();
        HashMap<LabeledLink, Pair<Integer,Integer>> edgeIds = new HashMap<>();

        int i = 0;
        int defaultLabel = -1;

        for (Node node : nodes) {
            nodeIds.put(node,i++);
        }

        for (Map.Entry<Node, Integer> entry : nodeIds.entrySet()) {
            Node node = entry.getKey();
            Integer nodeId = entry.getValue();
            String nodeLabel = node.getLabel().getLocalName();
//            System.out.println(nodeLabel);
            nodeLabel = nodeLabel.substring(nodeLabel.lastIndexOf("/")+1);
//            if(!nodeLabel.startsWith("E")){
//                nodeLabel = nodeLabel.substring(nodeLabel.indexOf('E'));
//            }

            LGGraph.addNode(nodeId, LabelDir.NodeLabel.getOrDefault(nodeLabel, defaultLabel));

//            System.out.println(nodeLabel+": "+LabelDir.NodeLabel.getOrDefault(nodeLabel, defaultLabel));
        }
//        System.out.println(LGGraph.nodes.size());
//        System.out.println();

        for (Node node1 : nodes) {
            for (Node node2 : nodes) {
                if (dg.containsEdge(node1,node2)){
                    LabeledLink edge = (LabeledLink) dg.getEdge(node1,node2);
                    Pair<Integer,Integer> edgePair = new Pair<>(nodeIds.get(node1),nodeIds.get(node2));
                    edgeIds.put(edge,edgePair);
                }else if (dg.containsEdge(node2,node1)){
                    LabeledLink edge = (LabeledLink) dg.getEdge(node2,node1);
                    Pair<Integer,Integer> edgePair = new Pair<>(nodeIds.get(node2),nodeIds.get(node1));
                    edgeIds.put(edge,edgePair);
                }
            }
        }

        for (Map.Entry<LabeledLink, Pair<Integer, Integer>> entry : edgeIds.entrySet()) {
            LabeledLink edge = entry.getKey();
            Pair<Integer,Integer> edgePair = entry.getValue();
            String edgeLabel = edge.getLabel().getLocalName();
            int sourceId = edgePair.getKey();
            int targetId = edgePair.getValue();

//            System.out.println(edgeLabel);
            edgeLabel = edgeLabel.substring(edgeLabel.lastIndexOf("/")+1);
//            if (!edgeLabel.startsWith("P")){
//                edgeLabel = edgeLabel.substring(edgeLabel.indexOf('P'));
//            }

            LGGraph.addEdge(sourceId,targetId,LabelDir.EdgeLabel.getOrDefault(edgeLabel,defaultLabel));
//            System.out.println(edgeLabel+": "+LabelDir.EdgeLabel.getOrDefault(edgeLabel, defaultLabel));

        }
//        System.out.println(LGGraph.edges.size());


        return LGGraph;
    }

    public static DirectedWeightedMultigraph<Node, LabeledLink> LgGraphToDWG(LGGraph lgGraph){

        for (VF2.graph.Node node : lgGraph.nodes) {

        }

        for (Edge edge : lgGraph.edges) {

        }

        return null;
    }


    public static void main(String[] args) {
        ArrayList<String> list = new ArrayList<>();
        list.add("b");
        list.add("a");
        list.add("c");
        list.add("d");
        Collections.sort(list);
        "a".compareTo("b");
        for (String s : list) {
            System.out.println(s);
        }
    }
}


