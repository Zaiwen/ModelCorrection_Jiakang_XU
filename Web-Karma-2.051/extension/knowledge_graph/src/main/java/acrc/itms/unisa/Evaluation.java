package acrc.itms.unisa;

import au.com.d2dcrc.GramiMatcher;
import dataStructures.HPListGraph;
import dataStructures.Query;
import edu.isi.karma.rep.alignment.*;
import org.jgrapht.graph.DirectedWeightedMultigraph;

import java.util.*;

public class Evaluation {


    /**generate an example pattern*
     * @From 16 Dec 2018
     * Note: A cycling dependency, i.e. If a person is employed by the university, and the person opens a certain number of bank accounts,
     * there must be a bank account and only one of the accounts linked with the university.
     * */
    public static DirectedWeightedMultigraph<Node,LabeledLink> cyclingPattern1(){
        DirectedWeightedMultigraph<Node,LabeledLink> pattern = new DirectedWeightedMultigraph<Node,LabeledLink>(LabeledLink.class);
        Node n1 = new InternalNode("0",new Label("12"));
        Node n2 = new InternalNode("1", new Label("6"));
        Node n3 = new InternalNode("2", new Label("7"));
        pattern.addVertex(n1);
        pattern.addVertex(n2);
        pattern.addVertex(n3);

        //LabeledLink l1 = new ObjectPropertyLink("0",new Label("16"),ObjectPropertyType.Direct);
        LabeledLink l2 = new ObjectPropertyLink("1",new Label("12"),ObjectPropertyType.Direct);
        LabeledLink l3 = new ObjectPropertyLink("2",new Label("15"), ObjectPropertyType.Direct);

        //pattern.addEdge(n1,n2,l1);
        pattern.addEdge(n3,n2,l3);
        pattern.addEdge(n3,n1,l2);
        return pattern;
    }


    /***Evaluate the # of patterns occurring in the KG*
     * @From 16 Dec 2018
     * */
    public static void main (String args[]) {

        try {
            CommonDataModel commonDataModel = new CommonDataModel(1,new DirectedWeightedMultigraph<>(LabeledLink.class), new HashMap<>(), new HashMap<>(), new HashMap<>());
            commonDataModel.generateCDMExample2();//generate an example cdm
            commonDataModel.shuffleLabels();//shuffle the edge label and node label of the CDM
            Map<String, Integer> nodeLabelMap = commonDataModel.getNodeLabelMap();
            Map<String, Integer> edgeLabelMap = commonDataModel.getEdgeLabelMap();
            Utilities.printMap(commonDataModel.getNodeLabelMap());//print all the node labels
            Utilities.printMap(commonDataModel.getEdgeLabelMap());//print out all the edge labels
            System.out.println("CDM is successfully built...");
            DirectedWeightedMultigraph<Node, LabeledLink> kg = Generator.KGExample2(300, commonDataModel, nodeLabelMap, edgeLabelMap);//create a kg based on the CDM
            dataStructures.Graph singleGraph = new dataStructures.Graph(5555,0);/**Create a big graph. The ID of this graph is 5555, and the frequency threshold is 0.**/
            dataStructures.Graph.loadDirectedWeightedMultigraph(kg, singleGraph);/**Load 'bigGraph'(DirectedWeighedMultiGraph) into it**/


            HPListGraph hpListGraph = new HPListGraph("pattern");/**create pattern graph**/
            DirectedWeightedMultigraph<Node,LabeledLink> pattern = cyclingPattern1();
            HPListGraph.loadDirectedWeightedMultiGraph(pattern,hpListGraph);
            System.out.println("the pattern is: \n" + hpListGraph);
            Query q = new Query((HPListGraph<Integer,Double>)hpListGraph);/**create a new query**/

            HashMap<Integer, HashSet<Integer>> nonCandidates = new HashMap<>();//create a hashmap to save pruned variables
            GramiMatcher gramiMatcher = new GramiMatcher();
            gramiMatcher.setGraph(singleGraph);
            gramiMatcher.setQry(q);
            gramiMatcher.getFrequency(nonCandidates, 1);

            System.out.println("museum_crm_test done.");

        } catch (Exception e) {
            e.printStackTrace();
        }




    }

}
