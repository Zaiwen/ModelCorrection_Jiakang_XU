package acrc.itms.unisa;

import edu.isi.karma.rep.alignment.LabeledLink;
import edu.isi.karma.rep.alignment.Node;
import org.jgrapht.graph.DirectedWeightedMultigraph;

/**Pattern that should be matched using Minimum image based metric*
 * @created 16 Feb 2019
 *
 * */
public class MNIPattern extends Pattern{
    private DirectedWeightedMultigraph<Node, LabeledLink> ESMPatternGraph; //To hold a pattern for ESM
    private DirectedWeightedMultigraph<Node, LabeledLink> GramiPatternGraph; //To hold a pattern for Grami
    private String jointPointLabel = "";

    public void setESMPatternGraph (DirectedWeightedMultigraph<Node,LabeledLink> ESMPatternGraph) {
        this.ESMPatternGraph = ESMPatternGraph;
    }

    public DirectedWeightedMultigraph<Node, LabeledLink> getESMPatternGraph() {
        return ESMPatternGraph;
    }

    public void setGramiPatternGraph (DirectedWeightedMultigraph<Node, LabeledLink> GramiPatternGraph) {
        this.GramiPatternGraph = GramiPatternGraph;
    }

    public DirectedWeightedMultigraph<Node, LabeledLink> getGramiPatternGraph() {
        return GramiPatternGraph;
    }

    public void setJointPointLabel (String jointPointLabel) {
        this.jointPointLabel = jointPointLabel;
    }

    public String getJointPointLabel () {
        return jointPointLabel;
    }
}
