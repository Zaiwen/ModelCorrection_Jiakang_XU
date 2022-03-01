package au.edu.unisa;

import edu.isi.karma.rep.alignment.*;
import org.jgrapht.graph.DirectedWeightedMultigraph;

import java.util.List;
import java.util.Set;

public class Test {

    public static void main (String args[]) {

        DirectedWeightedMultigraph<Node, LabeledLink> graph = new DirectedWeightedMultigraph<>(LabeledLink.class);
        Node v0 = new InternalNode("0", new Label("A"));
        Node v1 = new InternalNode("1", new Label("A"));
        Node v2 = new InternalNode("2", new Label("A"));
        Node v3 = new InternalNode("3", new Label("A"));
        Node v4 = new InternalNode("4", new Label("A"));
        LabeledLink e0 = new ObjectPropertyLink("0", new Label("B"), ObjectPropertyType.Direct);
        LabeledLink e1 = new ObjectPropertyLink("1", new Label("B"), ObjectPropertyType.Direct);
        LabeledLink e2 = new ObjectPropertyLink("2", new Label("B"), ObjectPropertyType.Direct);

        graph.addVertex(v0);
        graph.addVertex(v1);
        graph.addVertex(v2);
        graph.addVertex(v3);
        graph.addVertex(v4);
        graph.addEdge(v0,v1,e0);
        graph.addEdge(v2,v3,e1);
        graph.addEdge(v3,v4,e2);

        System.out.println("There are totally " + graph.vertexSet().size() + " nodes!");
        System.out.println("There are totally " + graph.edgeSet().size() + " edges!");


        System.out.println("Now begin to check connectivity!");
        KG kg = new KG(graph);
        /**Init the Adjacent List of the Steiner tree**/
        System.out.println("Now initialize the Adjacent list of Steiner tree! ");
        kg.init();
        List<DirectedWeightedMultigraph<Node,LabeledLink>> connectedComponents = kg.getConnectedComponent();
        System.out.println("# of connected component is: " + connectedComponents.size());

    }
}
