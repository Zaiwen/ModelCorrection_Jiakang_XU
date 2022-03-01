package edu.isi.karma.research.modeling;


import edu.isi.karma.modeling.alignment.LinkIdFactory;
import edu.isi.karma.rep.alignment.*;
import org.jgrapht.graph.DirectedWeightedMultigraph;

import java.util.HashSet;
import java.util.Set;

/**
 * The class is responsible for addling a domain ontology (http://www.cidoc-crm.org/html/5.0.4/cidoc-crm.html#_Toc310250811)
 *into assignment graph G.
 *We use the domain ontology to find all the paths that relate the current class nodes in the alignment graph G.
 *
 *Algorithm 4 - Add Ontology Paths to G
 *
 *@Author Zaiwen FENG
 * @created 26 May 2019
 * **/
public class DomainOntology {


    private DirectedWeightedMultigraph<Node, LabeledLink> ontologyGraph = new DirectedWeightedMultigraph<>(LabeledLink.class);

    public DirectedWeightedMultigraph<Node, LabeledLink> getOntologyGraph() {
        return ontologyGraph;
    }

    public void setOntologyGraph(DirectedWeightedMultigraph<Node, LabeledLink> ontologyGraph) {
        this.ontologyGraph = ontologyGraph;
    }

    public static DomainOntology loadMuseumOntology () {

        DirectedWeightedMultigraph<Node,LabeledLink> museumOntGraph = new DirectedWeightedMultigraph<>(LabeledLink.class);


        Set<String> modelIds = new HashSet<>();
        modelIds.add("s09-s-18-artists.json");

        Node n1 = new InternalNode("http://erlangen-crm.org/current/E22_Man-Made_Object1", new Label("http://erlangen-crm.org/current/E22_Man-Made_Object"));
        n1.setModelIds(modelIds);
        Node n2 = new InternalNode("http://erlangen-crm.org/current/E12_Production1", new Label("http://erlangen-crm.org/current/E12_Production"));
        n2.setModelIds(modelIds);
        Node n3 = new InternalNode("http://erlangen-crm.org/current/E35_Title1", new Label("http://erlangen-crm.org/current/E35_Title"));
        n3.setModelIds(modelIds);
        Node n4 = new InternalNode("http://erlangen-crm.org/current/E54_Dimension1", new Label("http://erlangen-crm.org/current/E54_Dimension"));
        n4.setModelIds(modelIds);
        Node n5 = new InternalNode("http://erlangen-crm.org/current/E52_Time-Span3", new Label("http://erlangen-crm.org/current/E52_Time-Span"));
        n5.setModelIds(modelIds);
        Node n6 = new InternalNode("http://erlangen-crm.org/current/E55_Type1", new Label("http://erlangen-crm.org/current/E55_Type"));
        n6.setModelIds(modelIds);
        Node n7 = new InternalNode("http://erlangen-crm.org/current/E39_Actor1", new Label("http://erlangen-crm.org/current/E39_Actor"));
        n7.setModelIds(modelIds);
        Node n8 = new InternalNode("http://erlangen-crm.org/current/E21_Person1", new Label("http://erlangen-crm.org/current/E21_Person"));//added on 29 May 2019
        n8.setModelIds(modelIds);

        museumOntGraph.addVertex(n1);
        museumOntGraph.addVertex(n2);
        museumOntGraph.addVertex(n3);
        museumOntGraph.addVertex(n4);
        museumOntGraph.addVertex(n5);
        museumOntGraph.addVertex(n6);
        museumOntGraph.addVertex(n7);
        museumOntGraph.addVertex(n8);

        /**set pattern model ids. 1 June 2019**/
        Set<String> patternIds = new HashSet<>();
        patternIds.add("s09-s-18-artists.json");


        LabeledLink e1 = new ObjectPropertyLink(LinkIdFactory.
                getLinkId("http://erlangen-crm.org/current/E22_Man-Made_Object1---http://erlangen-crm.org/current/P108i_was_produced_by---http://erlangen-crm.org/current/E12_Production1", n1.getId(), n2.getId()),
                new Label("http://erlangen-crm.org/current/P108i_was_produced_by"), ObjectPropertyType.Direct);
        e1.setModelIds(patternIds);
        LabeledLink e2 = new ObjectPropertyLink(LinkIdFactory.
                getLinkId("http://erlangen-crm.org/current/E22_Man-Made_Object1---http://erlangen-crm.org/current/P102_has_title---http://erlangen-crm.org/current/E35_Title1", n1.getId(), n3.getId()),
                new Label("http://erlangen-crm.org/current/P102_has_title"), ObjectPropertyType.Direct);
        e2.setModelIds(patternIds);
        LabeledLink e3 = new ObjectPropertyLink(LinkIdFactory.
                getLinkId("http://erlangen-crm.org/current/E22_Man-Made_Object1---http://erlangen-crm.org/current/P43_has_dimension---http://erlangen-crm.org/current/E54_Dimension1", n1.getId(), n4.getId()),
                new Label("http://erlangen-crm.org/current/P43_has_dimension"), ObjectPropertyType.Direct);
        e3.setModelIds(patternIds);
        LabeledLink e4 = new ObjectPropertyLink(LinkIdFactory.
                getLinkId("http://erlangen-crm.org/current/E12_Production1---http://erlangen-crm.org/current/P4_has_time-span---http://erlangen-crm.org/current/E52_Time-Span3", n2.getId(), n5.getId()),
                new Label("http://erlangen-crm.org/current/P4_has_time-span"), ObjectPropertyType.Direct);
        e4.setModelIds(patternIds);
        LabeledLink e5 = new ObjectPropertyLink(LinkIdFactory.
                getLinkId("http://erlangen-crm.org/current/E12_Production1---http://erlangen-crm.org/current/P32_used_general_technique---http://erlangen-crm.org/current/E55_Type1", n2.getId(), n6.getId()),
                new Label("http://erlangen-crm.org/current/P32_used_general_technique"), ObjectPropertyType.Direct);
        e5.setModelIds(patternIds);
        LabeledLink e6 = new ObjectPropertyLink(LinkIdFactory.
                getLinkId("http://erlangen-crm.org/current/E12_Production1---http://erlangen-crm.org/current/P14_carried_out_by---http://erlangen-crm.org/current/E39_Actor1", n2.getId(), n7.getId()),
                new Label("http://erlangen-crm.org/current/P14_carried_out_by"), ObjectPropertyType.Direct);
        e6.setModelIds(patternIds);
        LabeledLink e7 = new ObjectPropertyLink(LinkIdFactory.
                getLinkId("http://erlangen-crm.org/current/E21_Person1---http://www.w3.org/2000/01/rdf-schema#subClassOf---http://erlangen-crm.org/current/E39_Actor1", n8.getId(), n7.getId()),
                new Label("http://www.w3.org/2000/01/rdf-schema#subClassOf"), ObjectPropertyType.Direct);//added on 29 May 2019
        e7.setModelIds(patternIds);

        museumOntGraph.addEdge(n1,n2,e1);
        museumOntGraph.addEdge(n1,n3,e2);
        museumOntGraph.addEdge(n1,n4,e3);
        museumOntGraph.addEdge(n2,n5,e4);
        museumOntGraph.addEdge(n2,n6,e5);
        museumOntGraph.addEdge(n2,n7,e6);
        museumOntGraph.addEdge(n8,n7,e7);//added on 29 May 2019

        DomainOntology domainOntology = new DomainOntology();
        domainOntology.setOntologyGraph(museumOntGraph);
        return domainOntology;
    }

}
