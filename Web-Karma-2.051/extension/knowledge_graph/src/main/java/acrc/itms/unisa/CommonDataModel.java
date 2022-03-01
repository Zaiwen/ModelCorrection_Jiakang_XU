package acrc.itms.unisa;

import edu.isi.karma.rep.alignment.*;
import edu.isi.karma.rep.alignment.Label;
import org.apache.commons.math3.distribution.BinomialDistribution;
import org.apache.commons.math3.distribution.UniformIntegerDistribution;
import org.jgrapht.graph.DirectedWeightedMultigraph;

import java.awt.*;
import java.util.*;
import java.util.List;

/**this class describes the common data model. Created on 22 Aug 2018**/
public class CommonDataModel {

    private Integer id;
    private DirectedWeightedMultigraph<Node,LabeledLink> cdm;//common data model
    private Map<String,Color> colorMap;
    private Map<String, Integer> nodeLabelMap;//key: string label e.g. Person, Vehicle.. value: a number
    private Map<String, Integer> edgeLabelMap;//key: string edge label e.g. visits, pays for.. value: a number
    private Map<String, Map<String,Object>> attributeValueDistribution;//key: entity type name. Value: attribute type & value distribution of that entity type

    /**constructor**/
    public CommonDataModel(int id, DirectedWeightedMultigraph<Node,LabeledLink> cdm, Map<String,Integer> nodeLabelMap, Map<String,Integer> edgeLabelMap,
    Map<String, Map<String,Object>> attributeValueDistribution){
        this.id = id;
        this.cdm = cdm;
        this.nodeLabelMap = nodeLabelMap;
        this.edgeLabelMap = edgeLabelMap;
        this.attributeValueDistribution = attributeValueDistribution;
    }

    public Integer getId() {
        return id;
    }
    public DirectedWeightedMultigraph<Node, LabeledLink> getCdm() {
        return cdm;
    }
    public void setId(int id){
        this.id = id;
    }
    public void setCdm(DirectedWeightedMultigraph<Node, LabeledLink> cdm){
        this.cdm=cdm;
    }
    public Map<String,Color> getColorMap() { return colorMap; }
    public void setColorMap(Map<String,Color> colorMap) {this.colorMap=colorMap;}
    public Map<String, Integer> getNodeLabelMap(){return this.nodeLabelMap;}
    public Map<String, Integer> getEdgeLabelMap() {return this.edgeLabelMap;}


    /**get all of the neighbouring edges of an entity type in the cdm*
     * @param entityUri an entity type in the cdm
     * @return all of the neighbouring edges of the entity type
     * @revised 25 Sep 2018
     * */
    public List<LabeledLink> getNeighbouringEdges (String entityUri) throws Exception {
        if(cdm.vertexSet().size()<1)
            throw new Exception("The CDM is empty. Knowledge graph can not be generated.");

        Node entityType=null;
        for(Node node : cdm.vertexSet()){
            if(node.getUri().equals(entityUri))
                entityType=node;
        }

        if(entityType==null)
            throw new Exception("there does not exist this entity type in the cdm!");

        Set<LabeledLink> outgoingEdges = cdm.outgoingEdgesOf(entityType);//get all the outgoing edges of this entity type
        boolean result = cdm.containsVertex(entityType);
        Set<LabeledLink> incomingEdges = cdm.incomingEdgesOf(entityType);//get all the incoming edges of this entity type
        List<LabeledLink> allNeighbouringEdges = new ArrayList<>();

        allNeighbouringEdges.addAll(outgoingEdges);// add all the outgoing edges
        allNeighbouringEdges.addAll(incomingEdges);// add all the incoming edges

        return allNeighbouringEdges;
    }


    /**generate an exemple CDM**/
    public void generateCDMExample2 () {

        DirectedWeightedMultigraph<Node,LabeledLink> cdm = new DirectedWeightedMultigraph<>(LabeledLink.class);

        Node n1 = new InternalNode("n1",new Label("Person"));
        Node n2 = new InternalNode("n2", new Label("Location"));
        Node n3 = new InternalNode("n3", new Label("Bank_account"));
        Node n5 = new InternalNode("n5", new Label("Vehicle"));
        Node n6 = new InternalNode("n6", new Label("Medicare"));
        Node n7 = new InternalNode("n7", new Label("Insurance_production"));
        Node n8 = new InternalNode("n8", new Label("Tax_office"));
        Node n9 = new InternalNode("n9", new Label("Insurance_company"));
        Node n10 = new InternalNode("n10", new Label("University"));
        Node n11 = new InternalNode("n11", new Label("Parking_lot"));
        Node n12 = new InternalNode("n12", new Label("Working_position"));
        Node n13 = new InternalNode("n13", new Label("Gym"));
        Node n14 = new InternalNode("n14", new Label("Hospital"));
        Node n15 = new InternalNode("n15", new Label("Car_leasing_company"));

        Map<String, Object> n1_attributes = new HashMap<>();
        String n1_a1 = "age";//Attribute of person
        Object n1_a1_distribution = new UniformIntegerDistribution(18,65);

        n1_attributes.put(n1_a1,n1_a1_distribution);

        this.addAttributeValueDistribution("Person",n1_attributes);


        cdm.addVertex(n1);
        cdm.addVertex(n2);
        cdm.addVertex(n3);
        cdm.addVertex(n5);
        cdm.addVertex(n6);
        cdm.addVertex(n7);
        cdm.addVertex(n8);
        cdm.addVertex(n9);
        cdm.addVertex(n10);
        cdm.addVertex(n11);
        cdm.addVertex(n12);
        cdm.addVertex(n13);
        cdm.addVertex(n14);
        cdm.addVertex(n15);

        LabeledLink l1 = new ObjectPropertyLink("l1",new Label("lives_in"),ObjectPropertyType.Direct);
        LabeledLink l2 = new ObjectPropertyLink("l2", new Label("opens"),ObjectPropertyType.Direct);
        LabeledLink l3 = new ObjectPropertyLink("l3", new Label("employs"),ObjectPropertyType.Direct);
        LabeledLink l4 = new ObjectPropertyLink("l4",new Label("owns"),ObjectPropertyType.Direct);
        LabeledLink l5 = new ObjectPropertyLink("l5",new Label("transfers_to"),ObjectPropertyType.Direct);
        LabeledLink l6 = new ObjectPropertyLink("l6", new Label("links_account_to"), ObjectPropertyType.Direct);
        LabeledLink l7 = new ObjectPropertyLink("l7",new Label("links_to"),ObjectPropertyType.Direct);
        LabeledLink l8 = new ObjectPropertyLink("l8",new Label("product_of"),ObjectPropertyType.Direct);
        LabeledLink l9 = new ObjectPropertyLink("l9",new Label("pays_tax_for"),ObjectPropertyType.Direct);
        LabeledLink l10 = new ObjectPropertyLink("l10",new Label("links_with"),ObjectPropertyType.Direct);
        LabeledLink l11 = new ObjectPropertyLink("l11",new Label("registered_in"),ObjectPropertyType.Direct);
        LabeledLink l12 = new ObjectPropertyLink("l12",new Label("sees_doctor"),ObjectPropertyType.Direct);
        LabeledLink l13 = new ObjectPropertyLink("l13",new Label("next_to"),ObjectPropertyType.Direct);
        LabeledLink l14 = new ObjectPropertyLink("l14",new Label("around"),ObjectPropertyType.Direct);
        LabeledLink l15 = new ObjectPropertyLink("l15",new Label("closes_to"),ObjectPropertyType.Direct);
        LabeledLink l16 = new ObjectPropertyLink("l16",new Label("owned_by"),ObjectPropertyType.Direct);
        LabeledLink l17 = new ObjectPropertyLink("l17",new Label("garaged_at"),ObjectPropertyType.Direct);
        LabeledLink l18 = new ObjectPropertyLink("l18",new Label("studied_in"),ObjectPropertyType.Direct);
        LabeledLink l19 = new ObjectPropertyLink("l19",new Label("is_in"),ObjectPropertyType.Direct);
        LabeledLink l20 = new ObjectPropertyLink("l20",new Label("rents"),ObjectPropertyType.Direct);
        LabeledLink l21 = new ObjectPropertyLink("l21",new Label("provides"),ObjectPropertyType.Direct);

        l1.setCardinality(new CardinalityInfo(1,4, new BinomialDistribution(3,0.5)),new CardinalityInfo(1,1));//set cardinality of l1 - Person--->lives in--->Location
        l2.setCardinality(new CardinalityInfo(1,1),new CardinalityInfo(1,4, new BinomialDistribution(3,0.5)));//l2 - person->opens->bank account
        l3.setCardinality(new CardinalityInfo(1,1),new CardinalityInfo(1,300, new BinomialDistribution(299,0.7)));//l3 university-->employs-->Person
        l4.setCardinality(new CardinalityInfo(1,2, new BinomialDistribution(1,0.5)),
                new CardinalityInfo(0,3, new BinomialDistribution(3,0.3)));//l4 person--->owns--->vehicle
        l5.setCardinality(new CardinalityInfo(1,1),new CardinalityInfo(1,300,new BinomialDistribution(299,0.7)));//l5 university--->transfers--->bank account
        l6.setCardinality(new CardinalityInfo(1,1),new CardinalityInfo(1,1000,new BinomialDistribution(999,0.5)));//l6 tax office--->link account to---->bank account
        l7.setCardinality(new CardinalityInfo(1,1),new CardinalityInfo(1,2000,new BinomialDistribution(1999,0.5)));//l7 insurance company--->link to--->bank account
        l8.setCardinality(new CardinalityInfo(1,5,new BinomialDistribution(4,0.7)),new CardinalityInfo(1,1));//l8 insurance company--->product of--->insurance company
        l9.setCardinality(new CardinalityInfo(1,1000,new BinomialDistribution(999,0.5)),new CardinalityInfo(1,1));//l9 person--->pay tax to--->tax office
        l10.setCardinality(new CardinalityInfo(1,1),new CardinalityInfo(0,1,new BinomialDistribution(1,0.5)));//l10 person--->link with--->medicare
        l11.setCardinality(new CardinalityInfo(1,100,new BinomialDistribution(99,0.7)),new CardinalityInfo(0,1,new BinomialDistribution(1,0.5)));//l11 person-->registered in---->gym
        l12.setCardinality(new CardinalityInfo(1,500,new BinomialDistribution(499,0.8)),new CardinalityInfo(1,3,new UniformIntegerDistribution(1,3)));//l12 person--->sees doctor in--->hospital
        //l13.setCardinality(new CardinalityInfo(0,1,new BinomialDistribution(1,0.9)),new CardinalityInfo(1,300,new BinomialDistribution(299,0.7)));//l13 hospital-->next to---->location
        l13.setCardinality(new CardinalityInfo(1,1),new CardinalityInfo(1,300,new BinomialDistribution(299,0.7)));//l13 hospital-->next to---->location
        //l14.setCardinality(new CardinalityInfo(0,1,new BinomialDistribution(1,0.9)),new CardinalityInfo(1,300,new BinomialDistribution(299,0.7)));//l14 car leasing company--->around--->location
        l14.setCardinality(new CardinalityInfo(1,1),new CardinalityInfo(1,300,new BinomialDistribution(299,0.7)));//l14 car leasing company--->around--->location
        l15.setCardinality(new CardinalityInfo(2,3,new BinomialDistribution(1,0.5)),new CardinalityInfo(1,300,new BinomialDistribution(299,0.7)));//l15 parking lot--->close to--->location
        l16.setCardinality(new CardinalityInfo(1,50,new BinomialDistribution(49,0.7)),new CardinalityInfo(1,1));//l16 Vehicle-->owned by--->Car leasing company
        l17.setCardinality(new CardinalityInfo(1,50,new BinomialDistribution(49,0.5)),new CardinalityInfo(1,2,new BinomialDistribution(1,0.5)));//l17 vehicle--->garaged at--->parking lot
        l18.setCardinality(new CardinalityInfo(1,300,new BinomialDistribution(299,0.7)),new CardinalityInfo(1,1));//l18 Person--->studied in---->university
        l19.setCardinality(new CardinalityInfo(1,1),new CardinalityInfo(1,1));//l19 Person--->is in---->faculty position
        l20.setCardinality(new CardinalityInfo(1,50,new BinomialDistribution(49,0.5)),new CardinalityInfo(1,3,new BinomialDistribution(2,0.5)));//l20 person--->rents---->vehicle
        l21.setCardinality(new CardinalityInfo(1,1),new CardinalityInfo(1,200,new BinomialDistribution(199,0.5)));//l21 University--->provides--->faculty position

        cdm.addEdge(n1,n2,l1);
        cdm.addEdge(n1,n3,l2);
        cdm.addEdge(n10,n1,l3);
        cdm.addEdge(n1,n5,l4);
        cdm.addEdge(n10,n3,l5);
        cdm.addEdge(n8,n3,l6);
        cdm.addEdge(n9,n3,l7);
        cdm.addEdge(n7,n9,l8);
        cdm.addEdge(n1,n8,l9);
        cdm.addEdge(n1,n6,l10);
        cdm.addEdge(n1,n13,l11);
        cdm.addEdge(n1,n14,l12);
        cdm.addEdge(n14,n2,l13);
        cdm.addEdge(n15,n2,l14);
        cdm.addEdge(n11,n2,l15);
        cdm.addEdge(n5,n15,l16);
        cdm.addEdge(n5,n11,l17);
        cdm.addEdge(n1,n10,l18);
        cdm.addEdge(n1,n12,l19);
        cdm.addEdge(n1,n5,l20);
        cdm.addEdge(n10,n12,l21);
        this.setCdm(cdm);

        Map<String, Color> colorMap = new HashMap<>();/**Begin to define the color of the entities**/
        colorMap.put("Person",Color.black);
        colorMap.put("Location",Color.blue);
        colorMap.put("Bank_account",Color.green);
        colorMap.put("Vehicle",Color.yellow);
        colorMap.put("Medicare",Color.CYAN);
        colorMap.put("Insurance_production",Color.GRAY);
        colorMap.put("Tax_office",Color.LIGHT_GRAY);
        colorMap.put("Insurance_company",Color.magenta);
        colorMap.put("University",Color.red);
        colorMap.put("Parking_lot",Color.orange);
        colorMap.put("Working_position",Color.pink);
        colorMap.put("Gym",Color.white);
        colorMap.put("Hospital",Color.darkGray);
        colorMap.put("Car_leasing_company",new Color(0,78,50));//dark green
        this.setColorMap(colorMap);
    }


    /**shuffle labels of the CDM (including edge label and node label) and attribute value distribution map*
     * @from 3 Oct 2018
     * @revised 10 Dec 2018
     * */
    public void shuffleLabels(){
        List<DirectedWeightedMultigraph<Node,LabeledLink>> graphList = new ArrayList<>();
        graphList.add(cdm);
        Utilities.shuffleLabels(graphList,edgeLabelMap,nodeLabelMap);

        Set<String> allKeys = attributeValueDistribution.keySet();
        for (String key : allKeys) {
            Map<String, Object> attributeDistribution = attributeValueDistribution.remove(key);
            Integer newKey = nodeLabelMap.get(key);
            attributeValueDistribution.put(newKey.toString(),attributeDistribution);
        }
    }

    /**generate random knowledge graph using another way. *
     * @from 11 Oct 2018
     * @revised 19 Oct 2018
     * @revised 21 Oct 2018
     *@param vp vertices pool
     *@param total expected # of vertices in KG
     *@param proportion proportion of the number of different entity types in KG. Key: entity types, Value: maximum number
     *@param relationshipOrder  ordered relationships
     *@param relationshipPropMap a map to relationship props
     *@param lazySearchTimes the times for trying to pair two nodes
     *@return constructed knowledge graph
     * @Exception if the threshold is more than # of the vertices in the expected kG, then exception will be thrown
     * */
    public KnowledgeGraph generateKG2(VerticesPool vp, int total, Map<String,Integer> proportion, List<String> relationshipOrder,
                                      Map<String, RelationshipProp> relationshipPropMap, int lazySearchTimes) throws Exception{

        Map<String, Set<Node>> classifiedNodes = new HashMap<>();//key: entity types, value: a set of all the nodes with the entity types

        KnowledgeGraph knowledgeGraph = new KnowledgeGraph(new DirectedWeightedMultigraph<>(LabeledLink.class));//generate an empty kg.
        DirectedWeightedMultigraph<Node, LabeledLink> kg = knowledgeGraph.getKg();
        Set<String> modelId = new HashSet<>();
        modelId.add("1");//model id of all the nodes in the kg should be '1'

        try{
            Map<String,Integer> assignedNumber = Utilities.assignInteger(proportion,total);//get the number to be created for each type of entity
            Iterator it = assignedNumber.entrySet().iterator();
            while (it.hasNext()){
                Map.Entry pair = (Map.Entry)it.next();
                String uri = (String) pair.getKey();//get key
                Integer number = (Integer) pair.getValue();//get value
                System.out.println("there are " + number + " " + uri + " nodes!");
                Set<Node> freshNodes = vp.getRandomNode(uri,number);//get the nodes with a specified type and number and then put into the KG
                classifiedNodes.put(uri,freshNodes);//put the nodes with a specified type into a map
                Iterator<Node> iterator = freshNodes.iterator();
                while (iterator.hasNext()) {
                    Node node = iterator.next();
                    kg.addVertex(node);//add nodes into the knowledge graph
                }
            }//all the nodes have been added to the KG

            System.out.println("Now it is time for pairing among these " +  kg.vertexSet().size() + " vertices!");
            int step=1;
            Iterator<String> iterator = relationshipOrder.iterator();
            while (iterator.hasNext()) {
                String relationship = iterator.next();
                RelationshipProp relationshipProp = relationshipPropMap.get(relationship);
                int weightOption = relationshipProp.getWeightedOption();//get the weight assigning strategy
                double percentageS = relationshipProp.getPercentageS();//Randomly get p% elements of the source
                double percentageT = relationshipProp.getPercentageT();//Randomly get p% elements of the target
                knowledgeGraph.pairEntities(relationship,this,classifiedNodes,weightOption, relationshipPropMap, lazySearchTimes, percentageS,percentageT, nodeLabelMap, edgeLabelMap, step);
                step++;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return knowledgeGraph;
    }

    /**add attribute value distribution with regard to an entity type*
     * @param entityTypeUri an entity type uri
     * @param attributeInfoMap attribute distribution with regard to an entity type
     * */
    public void addAttributeValueDistribution (String entityTypeUri, Map<String, Object> attributeInfoMap) {
        this.attributeValueDistribution.put(entityTypeUri, attributeInfoMap);
    }

    /**get attribute value distribution with regard to an entity type*
     * @param entityTypeUri an entity type uri
     * @return a map of attribute distribution with regard to an entity type
     * */
    public Map<String, Object> getAttributeValueDistribution (String entityTypeUri) {
        return this.attributeValueDistribution.get(entityTypeUri);
    }


}
