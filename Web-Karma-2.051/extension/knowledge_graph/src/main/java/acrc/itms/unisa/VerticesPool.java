package acrc.itms.unisa;

import edu.isi.karma.rep.alignment.CardinalityInfo;
import edu.isi.karma.rep.alignment.LabeledLink;
import edu.isi.karma.rep.alignment.Node;
import edu.isi.karma.util.RandomGUID;
import org.apache.commons.math3.distribution.AbstractIntegerDistribution;

import java.util.*;

/**The pool provide the initial vertices in kg**/
public class VerticesPool {

    private Set<Node> nodeSet;
    //private Map<String, Set<Node>> pool;

    //constructor
    public VerticesPool(){
        nodeSet = new HashSet<>();
    }

    /**initialize the vertices pool*
     * @revised 10 Dec 2018
     * @revised 12 Feb 2019
     * @param n the size of the pool
     * @param commonDataModel the CDM
     * @param nodeLabelMap map from string (e.g. person) to integer symbol(e.g. 12)
     * */
    public void init (int n, CommonDataModel commonDataModel, Map<String,Integer> nodeLabelMap) {
        try{
            Set<String> modelId = new HashSet<>();
            modelId.add("1");//model id of all the nodes in the kg should be '1'
            for(int i=0; i<n; i++){
                Node entityType = (Node) Utilities.getRandomElementFromSet(commonDataModel.getCdm().vertexSet());//Randomly pick an entity type from the cdm graph
                Node entity = entityType.clone();//instantiate an entity using this entity type
                initStubs(entity,commonDataModel);//initialize the stubs of the entity
                initAttributes(entity, commonDataModel, nodeLabelMap);//initialize the attributes of the entity. Added on 10 Dec 2018
                //entity.setId(new RandomGUID().toString().concat("#").concat(entity.getUri()));//assign a unique id to the new entity in the kG
                entity.setId(Integer.toString(i));//set a unique integer number as identifier. 2019.02.12
                entity.setModelIds(modelId);
                nodeSet.add(entity);//add this entity to the vertices pool
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**init the attribute of an entity based on the cdm*
     * @From 10 Dec 2018
     * @param node an entity in the knowledge graph
     * @param commonDataModel the common data model
     * @param nodeLabelMap
     * */
    public void initAttributes (Node node, CommonDataModel commonDataModel, Map<String,Integer> nodeLabelMap) {
        Map<String, Object> attributeValueDistributionMap = commonDataModel.getAttributeValueDistribution(node.getUri());//get the attribute types and its corresponding attribute value distribution defined in the CDM
        if (attributeValueDistributionMap != null) {
            Iterator it = attributeValueDistributionMap.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry)it.next();
                String attributeType = (String) pair.getKey();
                AbstractIntegerDistribution attributeDistribution = (AbstractIntegerDistribution) pair.getValue();//currently, only abstractIntegerDistribution is used! 10 Dec 2018
                int attributeValue = attributeDistribution.sample();//sample an attribute value from the distribution
                node.addAttributeValue(attributeType,attributeValue);
            }
        }
    }


    /**Init a table for saving all of the stubs of each node in the knowledge graph based on the cdm*
     * This function updates the previous function called 'initMultiplicityTable' since we will designate out-degree/in-degree for each
     * nodes in the knowledge graph according to distribution defined in the cdm.
     *@from 4 Sep 2018
     *@param node an entity in the knowledge graph.
     *@param commonDataModel the common data model.
     * */
    public void initStubs(Node node, CommonDataModel commonDataModel) throws Exception{
        String nodeUri = node.getUri();//get the Uri (Label) of the node in the knowledge graph
        List<LabeledLink> neighbouringEdges = commonDataModel.getNeighbouringEdges(nodeUri);//get all the relationship types from/to this entity type in the CDM.
        Map<String,Integer> incomingStubsMap = new HashMap<>();//create a new 'stubs' map. Key: a relationship type; Value: sampling degrees for this relationship type to this node.
        Map<String,Integer> outgoingStubMap = new HashMap<>();//create a new 'stubs' map. Key: a relationship type; Value: sampling degrees for this relationship type from this node.

        /**sample out-degree or in-degree of this relationship type according the information in the CDM**/
        for(LabeledLink labeledLink : neighbouringEdges){//iterate all kinds of relationship types
            String relationshipType = labeledLink.getUri();//get the label of each relationship type
            if(labeledLink.getSource().getUri().equals(nodeUri) &&
                    (!labeledLink.getTarget().getUri().equals(nodeUri))){//outgoing edge
                CardinalityInfo targetCardinality=labeledLink.getTargetCardinality();//get the target cardinality info of each relationship type
                int randomDegree = targetCardinality.sample();
                outgoingStubMap.put(relationshipType,randomDegree);
            }else if((!labeledLink.getSource().getUri().equals(nodeUri)) &&
                    (labeledLink.getTarget().getUri().equals(nodeUri))){//incoming edge
                CardinalityInfo sourceCardinality=labeledLink.getStartCardinality();//get the start cardinality info of each relationship type
                int randomDegree = sourceCardinality.sample();
                incomingStubsMap.put(relationshipType,randomDegree);
            }else if(labeledLink.getSource().getUri().equals(nodeUri) &&
                    labeledLink.getTarget().getUri().equals(nodeUri)){//self-loop in cdm like "is_colleague"
                CardinalityInfo sourceCardinality=labeledLink.getStartCardinality();//get the start cardinality info of each relationship type
                CardinalityInfo targetCardinality=labeledLink.getTargetCardinality();//get the target cardinality info of each relationship type
                int randomDegree = sourceCardinality.sample();
                incomingStubsMap.put(relationshipType,randomDegree);
                int randomDegree1 = targetCardinality.sample();
                outgoingStubMap.put(relationshipType,randomDegree1);
            }
            node.addStubStatus(labeledLink.getUri(),false);//added on 16/11/18. initialize the stub status. False means the stub has not been used yet.
        }

        Iterator iterator = incomingStubsMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry pair = (Map.Entry) iterator.next();
            String relationship = (String) pair.getKey();
            Integer number = (Integer) pair.getValue();
            node.addIncomingLinks(relationship,number);//put this initial incoming stub map into the entity in the knowledge graph as a property.
            node.addToCurrentInStubs(relationship, number);//put this initial incoming stub map into the entity in the knowledge graph as a property.
        }

        Iterator iterator1 = outgoingStubMap.entrySet().iterator();
        while (iterator1.hasNext()) {
            Map.Entry pair = (Map.Entry) iterator1.next();
            String relationship = (String) pair.getKey();
            Integer number = (Integer) pair.getValue();
            node.addOutgoingLinks(relationship, number);//put this initial outgoing stub map into the entity in the knowledge graph as a property.
            node.addToCurrentOutStubs(relationship, number);//put this initial outgoing stub map into the entity in the knowledge graph as a property.
        }
    }

    public void setNodeSet (Set<Node> nodeSet) {this.nodeSet = nodeSet;}

    public Set<Node> getNodeSet () {return this.nodeSet;}

    /**pick up an entity from the pool with a special label(URI), and then remove from the pool*
     * @revised 9 Oct 2018
     * @param uri expected label
     * @return random node with the expected label
     * @exception if there is not this entity, then the exception is thrown
     * */
    public Node getRandomNode (String uri) throws Exception {
        Node node = null;

        Iterator<Node> it = nodeSet.iterator();
        while (it.hasNext()){
            Node n = it.next();
            if(n.getUri().equals(uri)){
                node=n;
                it.remove();
                break;
            }
        }
        if(node == null){
            throw new Exception("there is not expected node return from the pool!");
        }
        return node;
    }

    /**pick up a certain number of entities from the pool with a specified label(URI), and then remove them from the pool*
     * @from 12 Oct 2018
     * @Revised 19 Oct 2018
     *@param uri expected label
     *@param threshold the # of vertices expected
     *@return random nodes with the expected label
     *@exception if there are not sufficient entities, then the exception is thrown
     * */
    public Set<Node> getRandomNode (String uri, int threshold) throws Exception {
        Set<Node> nodes = new HashSet<>();
        int num = 1;

        Iterator<Node> it = nodeSet.iterator();
        while (it.hasNext()) {
            Node n = it.next();
            if(n.getUri().equals(uri)){
                num++;
                nodes.add(n);
                it.remove();
            }
            if(num > threshold){
                break;
            }
        }
        if(num < threshold){
            throw new Exception("there are not enough vertices in the pool about this uri: " + uri);
        }
        return nodes;
    }
}
