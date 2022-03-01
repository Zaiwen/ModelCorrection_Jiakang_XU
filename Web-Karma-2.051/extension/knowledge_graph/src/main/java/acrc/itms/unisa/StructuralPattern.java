package acrc.itms.unisa;

import edu.isi.karma.rep.alignment.LabeledLink;
import edu.isi.karma.rep.alignment.Node;
import org.jgrapht.graph.DirectedWeightedMultigraph;

/**This class describes the structural pattern which are expected to be imported to the KG*
 * @from 11 Feb 2019
 * */
public class StructuralPattern extends Pattern{

    private DirectedWeightedMultigraph<Node, LabeledLink> patternGraph; //To hold a structural pattern

    public void setPatternGraph (DirectedWeightedMultigraph<Node, LabeledLink> patternGraph) { this.patternGraph = patternGraph; }

    public DirectedWeightedMultigraph<Node, LabeledLink> getPatternGraph() {
        return patternGraph;
    }
}
