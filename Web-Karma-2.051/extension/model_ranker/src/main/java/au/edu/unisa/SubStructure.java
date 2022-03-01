package au.edu.unisa;

import edu.isi.karma.rep.alignment.InternalNode;
import edu.isi.karma.rep.alignment.Label;
import edu.isi.karma.rep.alignment.LabeledLink;
import edu.isi.karma.rep.alignment.Node;
import org.jgrapht.graph.DirectedWeightedMultigraph;

import java.util.HashSet;
import java.util.Set;

public class SubStructure {

    private DirectedWeightedMultigraph<Node, LabeledLink> subStructure;
    private Node limitNode;
    private Set<Node> anchorSet;

    //Constructor
    public SubStructure () {
        subStructure = new DirectedWeightedMultigraph<>(LabeledLink.class);
        limitNode = new InternalNode(new String(""), new Label(""));
        anchorSet = new HashSet<>();
    }

    public void setLimitNode (Node limitNode) {
        this.limitNode = limitNode;
    }

    public void addAnchor (Node anchor) {
        anchorSet.add(anchor);
    }

    public void setSubStructureGraph (DirectedWeightedMultigraph<Node,LabeledLink> subStructureGraph) {
        subStructure = subStructureGraph;
    }

    public Node getLimitNode () {return this.limitNode;}

    public Set<Node> getAnchorSet () {return this.anchorSet;}

    public DirectedWeightedMultigraph<Node, LabeledLink> getSubStructure() {
        return subStructure;
    }
}
