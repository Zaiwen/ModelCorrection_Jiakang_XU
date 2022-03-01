package acrc.itms.unisa;


import edu.isi.karma.rep.alignment.*;
import org.jgrapht.graph.DirectedWeightedMultigraph;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**This class is used to generate all of the patterns needed in our case study*
 * @from 11 Feb 2019
 * */
public class PatternFactory {

    private Map<String, List<StructuralPattern>> patternMap = new HashMap<>();

    /**add structural pattern**/
    public void addStruturalPattern (String relationshipType, List<StructuralPattern> structuralPatterns) {
        patternMap.put(relationshipType, structuralPatterns);
    }

    /**get all of structural patterns with regard ta a relationship**/
    public List<StructuralPattern> getStructuralPattern (String relationship) {
        List<StructuralPattern> structuralPatterns = patternMap.get(relationship);
        return structuralPatterns;
    }

    /**generate Pattern A ---- for the relationship "transfers to "*
     * If a person is employed by the university, and the person opens a certain number of bank accounts,
     * the university will transfer salary to one of these bank accounts that the person opens.
     * PATTERN DESCRIPTION:
     * UNIVERSITY EMPLOYS A PERSON, WHO OPENS SOME BANK ACCOUNTS,
     * UNIVERSITY WILL TRANSFER MONEY TO ONE OF THEM
     * PATTERN TYPE:
     * UNIQUE CYCLING DEPENDENCY
     *@from 11 Feb 2019
     * @revised 16 Feb 2019
     * @param nodeLabelMap string label --- > integer label, which is defined in the CDM
     * @param edgeLabelMap string label --- > integer label, which is defined in the CDM
     * @return pattern in regard to "transfers to"
     * */
    public MNIPattern generatePattern_C1 (Map<String, Integer> nodeLabelMap, Map<String,Integer> edgeLabelMap) {
        MNIPattern pattern = new MNIPattern();
        pattern.setId(1);// set the id ---- 1
        pattern.setPatternType(Pattern.patternType.UNIQUE_CYCLING_DEPENDENCY.toString());
        pattern.setInducedRT("transfers_to");//set induced relationship type

        DirectedWeightedMultigraph<Node,LabeledLink> graph_esm = new DirectedWeightedMultigraph<Node, LabeledLink>(LabeledLink.class);
        Node n1 = new InternalNode("0",new Label(nodeLabelMap.get("Person").toString()));
        Node n2 = new InternalNode("1", new Label(nodeLabelMap.get("University").toString()));
        LabeledLink l1 = new ObjectPropertyLink("0", new Label(edgeLabelMap.get("employs").toString()),ObjectPropertyType.Direct);
        graph_esm.addVertex(n1);
        graph_esm.addVertex(n2);
        graph_esm.addEdge(n2, n1, l1);
        pattern.setESMPatternGraph(graph_esm);//set pattern graph

        DirectedWeightedMultigraph<Node,LabeledLink> graph_grami = new DirectedWeightedMultigraph<>(LabeledLink.class);
        Node n3 = new InternalNode("3",new Label(nodeLabelMap.get("Person").toString()));
        Node n4 = new InternalNode("4", new Label(nodeLabelMap.get("Bank_account").toString()));
        graph_grami.addVertex(n3);
        graph_grami.addVertex(n4);
        LabeledLink l2 = new ObjectPropertyLink("1", new Label(edgeLabelMap.get("opens").toString()),ObjectPropertyType.Direct);
        graph_grami.addEdge(n3, n4, l2);
        pattern.setGramiPatternGraph(graph_grami);

        pattern.setJointPointLabel(nodeLabelMap.get("Person").toString());
        return pattern;
    }
}
