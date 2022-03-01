package acrc.itms.unisa;

import ESM.ESMMatcher;
import ESM.Vertex;
import acrc.itms.unisa.CommonDataModel;
import acrc.itms.unisa.DijkstraAlgorithm;
import edu.isi.karma.rep.alignment.*;
import edu.isi.karma.util.RandomGUID;
import org.jgrapht.graph.DirectedWeightedMultigraph;

import java.io.*;
import java.nio.Buffer;
import java.util.*;

/**This class describes a knowledge graph**/
public class KnowledgeGraph{

    private DirectedWeightedMultigraph<Node,LabeledLink> kg;
    private Map<String, RelationshipProp> relationshipPropMap = new HashMap<>();

    public KnowledgeGraph(DirectedWeightedMultigraph<Node,LabeledLink> kg){
        this.kg = kg;
    }

    public void setKg(DirectedWeightedMultigraph<Node,LabeledLink> kg){this.kg = kg;}

    public DirectedWeightedMultigraph<Node, LabeledLink> getKg() {
        return kg;
    }

    public void setRelationshipPropMap(Map<String, RelationshipProp> relationshipPropMap){this.relationshipPropMap = relationshipPropMap;}

    public Map<String, RelationshipProp> getRelationshipPropMap(){return this.relationshipPropMap;}


    /**pair an entity with another entity in the knowledge graph with a specified relationship type. If there already exists a link
     *  between two entities, then throw exception
     * @param source a node existed in the knowledge graph. The node has some stubs needing to find slots.
     * @param target a node existed in the knowledge graph. The node has some slots which might be inserted from source.
     * @param relationshipType a relationship type that is expected to be on the new link
     * @param weightOption weight strategy. 0: equal, 1: incremental
     * @param p the probability to link the source and the target
     * @return true if pairing successfully. Otherwise, it will return false
     * */
    public boolean addLink(Node source, Node target, String relationshipType, int weightOption, double p) {
        LabeledLink labeledLink=kg.getEdge(source,target);//Firstly, check if there already exists a link from source to target
        boolean result = false;//default output
        if (labeledLink != null){
            result = false;//there exist an link already between source and target
        }else{
            Map<String,Integer> outStubs=source.getCurrentOutStubs();//get all of the current out_stubs of the source
            Map<String, Integer> inStubs=target.getCurrentInStubs();//get all of the current in_stubs of the target
            int numOfOutStubs = source.getOutgoingLinks().get(relationshipType);//get the original # of out-stubs of the source
            int numOfInStubs = target.getIncomingLinks().get(relationshipType);//get the original # of in-stubs of the target
            List<String> sourceStubList=stubMapToList(outStubs);//convert to a list of out-stubs
            List<String> targetStubList=stubMapToList(inStubs);//convert to a list of in-stubs
            try{
                if(!sourceStubList.isEmpty() && sourceStubList.contains(relationshipType)){//the source contains an empty stub equal to the expected relationship type
                    if(!targetStubList.isEmpty() && targetStubList.contains(relationshipType)){//the target contains an empty stub equal to the expected relationship type

                        double randomNumber = Math.random();//create a random number between 0 and 1

                        if(randomNumber < p){
                            LabeledLink newLink = new ObjectPropertyLink(new RandomGUID().toString(), new Label(relationshipType), ObjectPropertyType.Direct);//create a new link
                            kg.addEdge(source,target,newLink);//add this new link to the knowledge graph

                            source.addDestination(target,1);//for Dijkstra algorithm
                            target.addDestination(source,1);//for Dijkstra algorithm

                            LabeledLink addedLabeledLink = kg.getEdge(source, target);//get the edge that is added just now

                            source.decreaseOutStubs(relationshipType);//decrease the out-stub about this relationship type
                            target.decreaseInStubs(relationshipType);//decrease in-stub of the target entity

                            result = true;//meaning pair successfully
                            System.out.println("the node " + source.getId() + " has been successfully paired with " + target.getId() + " with the relationship type " + relationshipType);

                            if(weightOption == 0){//equal option
                                System.out.println("weightOption is " + weightOption);
                                kg.setEdgeWeight(addedLabeledLink,1);//set the weight 1 to the new edge
                                System.out.println("the weight is " + 1);
                            }else if (weightOption == 1){//incremental option
                                System.out.println("weightOption is " + weightOption);
                                if((numOfOutStubs > 1)  && (numOfInStubs == 1)){//like  Person--opens--BA
                                    System.out.println("# of the original out-stubs is " + numOfOutStubs);
                                    System.out.println("# of the current out-stubs is " + source.getCurrentOutStubs().get(relationshipType));
                                    int weight = numOfOutStubs - source.getCurrentOutStubs().get(relationshipType);
                                    kg.setEdgeWeight(addedLabeledLink,weight);
                                    System.out.println("the assigned weight is " + weight);
                                }else if ((numOfOutStubs == 1) && (numOfInStubs > 1)){//like Person---lives in---Location
                                    System.out.println("# of the original in-stubs is " + numOfInStubs);
                                    System.out.println("# of the current in-stubs is " + target.getCurrentInStubs().get(relationshipType));
                                    int weight = numOfInStubs - target.getCurrentInStubs().get(relationshipType);
                                    kg.setEdgeWeight(addedLabeledLink,weight);
                                    System.out.println("the assigned weight is " + weight);
                                }
                            }
                        }
                    }else {
                        result = false;
                        System.out.println("pair fails because there are not enough stubs in the target side!");
                    }
                }else {
                    result = false;
                    System.out.println("pair fails because there are not enough stubs in the source side!");
                }
            }catch (Exception e){
                System.err.println(e);
            }
        }
        return result;
    }



    /**Given the order of relationships types, e.g. lives in, employs, and a number of orphan vertices with empty stubs, pair these vertices with a certain order of relationships*
     * @param relationshipType a specified relationship type
     * @param commonDataModel the cdm
     * @param classifiedNodes the list of all the nodes classified by a specified type in KG
     * @param weightOption strategy for weight assignment
     * @param relationProps relationship props
     * @param threshold the search will stop after fails over times of threshold
     * @param percentageS pick up p of the source entities when pairing
     * @param percentageT pick up p of the target entities when pairing
     * @param nodeLabelMap key: string label e.g. Person, Vehicle.. value: a number
     * @param edgeLabelMap key: string edge label e.g. visits, pays for.. value: a number
     *                     @param step
     * @Exception if there is not this relationship in the CDM, then an exception is thrown
     * @from 17 Oct 2018
     * @revised 14 Nov 2018
     * */
    public void pairEntities (String relationshipType, CommonDataModel commonDataModel, Map<String,Set<Node>> classifiedNodes,
                              int weightOption, Map<String, RelationshipProp> relationProps, int threshold, double percentageS, double percentageT,
                              Map<String,Integer> nodeLabelMap, Map<String,Integer> edgeLabelMap, int step) throws Exception {
        Set<LabeledLink> relationships =  commonDataModel.getCdm().edgeSet();//get all of the relationship types in the CDM
        LabeledLink relationship = null;
        for(LabeledLink link : relationships){
            if(link.getUri().equals(relationshipType)){
                relationship = link;
            }
        }
        if(relationship == null){
            throw new Exception("this relationship " + relationshipType + " does not exist in the cdm!");
        }else {
            String sourceUri = relationship.getSource().getUri();//get the uri of the source
            String targetUri = relationship.getTarget().getUri();//get the uri of the target
            Set<Node> sourceCandidates = classifiedNodes.get(sourceUri);// get all of the candidates of source
            Set<Node> targetCandidates = classifiedNodes.get(targetUri);//get all of the candidate of target

            Set<Node> sourceSet = new HashSet<>();
            if(percentageS==1.0){
                sourceSet.addAll(sourceCandidates);//copied set - source
            }else{
                sourceSet = Utilities.randomPart(sourceCandidates,percentageS);//random part of the original set - source
            }

            Set<Node> targetSet = new HashSet<>();
            if(percentageT==1.0){
                targetSet.addAll(targetCandidates);//copied set - target
            }else {
                targetSet = Utilities.randomPart(targetCandidates,percentageT);//random part of the original set - target
            }

            RelationshipProp relationshipProp = relationProps.get(relationshipType);// get the relationship prop
            List<Pattern> patterns = relationshipProp.getDependentPatterns();//get all the dependent structural patterns
            MixedMatcher mixedMatcher = new MixedMatcher();//build an integrated subgraph matcher

            if (!patterns.isEmpty()) {
                /**Get the structural pattern**/
                Pattern pattern = patterns.get(0);//Currently, assume that there is only one structural pattern with regard to this relationship type
                if (pattern.getType().equals(Pattern.patternType.UNIQUE_CYCLING_DEPENDENCY.toString())) {
                    MNIPattern pattern1 = (MNIPattern) pattern;//downcasting
                    DirectedWeightedMultigraph<Node, LabeledLink> esmPatternGraph = pattern1.getESMPatternGraph();
                    DirectedWeightedMultigraph<Node, LabeledLink> gramiPatternGraph = pattern1.getGramiPatternGraph();
                    String jointPointLabel = pattern1.getJointPointLabel();
                    List<Map<String, Integer>> matchedResults = mixedMatcher.join(this.kg, esmPatternGraph, gramiPatternGraph, jointPointLabel);
                    System.out.println("We find " + matchedResults + " results for the pattern of the relationship type " + relationship.getUri());

                    //addLink(randomSource, randomTarget, relationshipType, weightOption, linkProbability);
                } else {
                    /**Get all of the sub-graph isomorphisms with regard to this pattern in the KG**/
                    //List<Map<Vertex, Vertex>> matchedResults = ESMMatcher.getMatchResults(patternGraph, this.kg, true);//start node is specified
                }
            } else {
                int continuousFailingTimes=0;

                while (!sourceSet.isEmpty() && !targetSet.isEmpty()) {
                    Node randomSource = (Node) Utilities.getRandomElementFromSet(sourceSet);//pick up a random source entity
                    Node randomTarget = (Node) Utilities.getRandomElementFromSet(targetSet);//pick up a random target entity



                    double linkProbability = getLinkProbability2(relationshipType, randomSource, randomTarget, nodeLabelMap, edgeLabelMap);//get the link probability between randomSource and randomTarget
                    System.out.println("the link probability is: " + linkProbability);

                    /**try to pair the random source with the random target. The result will be returned*/
                    boolean result = addLink(randomSource, randomTarget, relationshipType, weightOption, linkProbability);

                    if(result){//if pair successfully
                        continuousFailingTimes=0;
                        if(isStubTypeEmpty(randomSource,relationshipType)){//check # of stubs about this relationship
                            sourceSet.remove(randomSource);//if randomSource runs out of their stubs, then remove it from sourceSet*
                        }
                        if(isStubTypeEmpty(randomTarget,relationshipType)){//check # of stubs about this relationship
                            targetSet.remove(randomTarget);//if randomTarget runs out of their stubs, then remove it from targetSet*
                        }
                    }else{
                        continuousFailingTimes++;
                        System.out.println("the node " + randomSource.getId() + " has failed to pair with " + randomTarget.getId());
                        if(continuousFailingTimes > threshold){//lazy search
                            System.out.println("Pairing fails too many times! Break!");
                            break;
                        }

                    }
                }
            }

            System.out.println("Now there are totally " + kg.vertexSet().size() + " vertices!");
            System.out.println("Now there are totally " + kg.edgeSet().size() + " edges!");

            /**Remove the orphan nodes THAT HAS NOT BEEN PAIRED during this round. **/

//            Set<Node> toBeRemoved = new HashSet<>();
//            Set<Node> nodes = kg.vertexSet();
//            Iterator<Node> iterator = nodes.iterator();
//            while (iterator.hasNext()) {
//                Node node = iterator.next();
//                if(this.getNeighbouringEdges(node).isEmpty()){
//                    if(node.getUri().equals(sourceUri) || node.getUri().equals(targetUri)){
//                        if (node.getUri().equals(nodeLabelMap.get("Vehicle").toString())  && (step == 13)){//important! added on 13 Dec 2018. if orphan 'vehicle' nodes are deleted, then no 'vehicle' could be used to pair 'car leasing company'
//                            //do nothing - 'Vehicle' is exempted
//                        } else {
//                            toBeRemoved.add(node);
//                        }
//                    }
//                }
//            }
//
//            Iterator<Node> iterator2 = toBeRemoved.iterator();
//            while (iterator2.hasNext()) {
//                Node node = iterator2.next();
//                kg.removeVertex(node);//remove the node from KG
//                String uri = node.getUri();//get the uri of this node
//                Set<Node> nodeSet = classifiedNodes.get(uri);//get the node set in the 'classifiedNodes'
//                nodeSet.remove(node);
//            }
//            System.out.println("Now there are totally " + kg.vertexSet().size() + " vertices after clearing the orphan nodes!");
        }
    }

    /**Here we set the RULES of probability between two entities instances, i.e. a source and a target paired with a certain relationship type*
     * @From 14 Nov 2018
     * @revised 4 Dec 2018
     *@param relationType relationship type to be added between the source and the target
     *@param source source
     *@param target target
     *@param nodeLabelMap //key: string label e.g. Person, Vehicle.. value: a number
     *@param edgeLabelMap key: string edge label e.g. visits, pays for.. value: a number
     *@return the dependent probability between the source and the target
     * */
    public static double getLinkProbability2(String relationType,  Node source, Node target, Map<String,Integer> nodeLabelMap, Map<String, Integer> edgeLabelMap) throws Exception{
        double probability = 0.0;
        double distance = 0.0;

        if(relationType.equals(edgeLabelMap.get("lives_in").toString())){
            probability = 1.0;
        }else if(relationType.equals(edgeLabelMap.get("employs").toString())){
            probability = 1.0;
        }else if(relationType.equals(edgeLabelMap.get("studied_in").toString())){
            if (source.getStubStatus(edgeLabelMap.get("employs").toString())) {//filter
                //it means that this person has already been employed by a university. do nothing. Linking probability is 0.0
            } else {
                if ((int)source.getAttributeValue("age") < 40) {
                    probability = 1.0;
                }
            }
        }else if (relationType.equals(edgeLabelMap.get("opens").toString())) {
            probability = 1.0;
        }else if (relationType.equals(edgeLabelMap.get("links_with").toString())) {
            probability = 1.0;
        }else if (relationType.equals(edgeLabelMap.get("provides").toString())) {
            probability = 1.0; //no constraint
        }else if (relationType.equals(edgeLabelMap.get("is_in").toString())) {
//            if (links.size() == 2) {
//                for (LabeledLink link : links) {//compute the distance between the source and the target
//                    String uri = link.getUri();//get uri of the relationship type
//                    Double coefficient = coefficients.get(uri);//get the coefficient of this uri
//                    distance = distance + coefficient;
//                }
//                if (distance == 2.0) {//rule model
//                    probability = 1.0;
//                } else {
//                    probability = 0.0;
//                }
//            }else {
//                //do nothing
//            }
        }else if (relationType.equals(edgeLabelMap.get("transfers_to").toString())) {//15-transfers to
//            if (links.size() == 2) {
//                for (LabeledLink link : links) {//compute the distance between the source and the target
//                    String uri = link.getUri();//get uri of the relationship type
//                    double weight = link.getWeight();//get the weight of this link, maybe 1,2,3...
//                    Double coefficient = coefficients.get(uri);//get the coefficient of this uri
//                    distance = distance + coefficient*weight;
//                }
//                if (distance == 2.0) {//rule model
//                    probability = 1.0;
//                } else {
//                    probability = 0.0;
//                }
//            } else {
//                //do nothing
//            }
        } else if (relationType.equals(edgeLabelMap.get("pays_tax_for").toString())) {
            if (source.getStubStatus(edgeLabelMap.get("employs").toString())) {//if a person is employed by the university, then the person should pay for tax office
                probability = 1.0;
            }
        } else if (relationType.equals(edgeLabelMap.get("links_account_to").toString())) {
//            if (links.size() == 2) {
//                for (LabeledLink link : links) {
//                    String uri = link.getUri();//get uri of the relationship type
//                    Double coefficient = coefficients.get(uri);//get the coefficient of this uri
//                    distance = distance +coefficient;
//                }
//                if (distance == 2.0) {
//                    //probability = 1.0;
//                    if (target.getStubStatus(edgeLabelMap.get("transfers_to").toString())) {//if a university transfer to this bank account
//                        probability = 1.0;
//                    }
//                } else {
//                    probability = 0.0;
//                }
//            } else {
//                //do nothing
//            }
        } else if (relationType.equals(edgeLabelMap.get("product_of").toString())) {
            probability = 1.0;
        } else if (relationType.equals(edgeLabelMap.get("links_to").toString())) {
            probability = 1.0;
        } else if (relationType.equals(edgeLabelMap.get("owns").toString())) {
            if ((int)source.getAttributeValue("age") > 18) {
                probability = 1.0;
            }
        } else if (relationType.equals(edgeLabelMap.get("closes_to").toString())) {
            probability = 1.0;
        } else if (relationType.equals(edgeLabelMap.get("garaged_at").toString())) {
//            if (links.size() == 3) {
//                for (LabeledLink link : links) {
//                    String uri = link.getUri();//get uri of the relationship type
//                    Double weight = link.getWeight();
//                    System.out.println("TEST! The weight is " + weight);
//                    Double coefficient = coefficients.get(uri);//get the coefficient of this uri
//                    System.out.println("TEST! The coefficient is " + coefficient);
//                    distance = distance + coefficient*weight;
//                }
//                System.out.println("TEST! The distance between location and parking lot is: " +distance);
//                if (distance == 3.0) {
//                    probability = 1.0;
//                } else {
//                    probability = 0.0;
//                }
//            } else {
//                //do nothing
//            }
        } else if (relationType.equals(edgeLabelMap.get("owned_by").toString())) {
            if (!source.getStubStatus(edgeLabelMap.get("owns").toString())) {//filter
                probability = 1.0;
            }
        } else if (relationType.equals(edgeLabelMap.get("around").toString())) {
            probability = 1.0;
        } else if (relationType.equals(edgeLabelMap.get("rents").toString())) {
//            if (links.size() == 3) {
//                for (LabeledLink link : links) {
//                    String uri = link.getUri();//get uri of the relationship type
//                    Double coefficient = coefficients.get(uri);//get the coefficient of this uri
//                    distance = distance +coefficient;
//                }
//                if (distance == 3.0) {
//                    probability = 1.0;
//                } else {
//                    probability = 0.0;
//                }
//            } else {
//                //do nothing
//            }
        } else if (relationType.equals(edgeLabelMap.get("registered_in").toString())) {
            probability = 1.0;
        } else if (relationType.equals(edgeLabelMap.get("next_to").toString())) {
            probability = 1.0;
        } else if (relationType.equals(edgeLabelMap.get("sees_doctor").toString())) {
//            if (links.size() == 2) {
//                for (LabeledLink link : links) {
//                    String uri = link.getUri();//get uri of the relationship type
//                    Double coefficient = coefficients.get(uri);//get the coefficient of this uri
//                    distance = distance +coefficient;
//                }
//                if (distance == 2.0) {
//                    probability = 1.0;
//                } else {
//                    probability = 0.0;
//                }
//            } else {
//                //do nothing
//            }
        }

        return probability;
    }



    /**convert a 'stub' hash map into a 'stub' list*
     *@param stubMap a map saving stubs of a node in the knowledge graph
     *@return  a list of stubs
     * */
    public List<String> stubMapToList(Map<String,Integer> stubMap){
        List<String> stubList=new ArrayList<>();
        Iterator it=stubMap.entrySet().iterator();
        while(it.hasNext()){
            Map.Entry pair = (Map.Entry)it.next();
            String relation = (String)pair.getKey();//get a specified relationship type
            Integer frequency = (Integer)pair.getValue();//get the number of this relationship type
            if(frequency==0){//there is not this kind of stub
                continue;
            }else {//yes we have this kind of stub
                for(int i=0; i<frequency; i++)
                    stubList.add(relation);
            }
        }
        return stubList;
    }

    /**Check if a stub map is empty or not. I.e., the stubs for all the relationships*
     * a stub map of an entity in the knowledge graph, whatever incoming or outgoing stubs
     * @param stubMap a map of stubs
     * @return true: empty false:not empty
     * */
    public boolean isStubMapEmpty(Map<String,Integer> stubMap){
        boolean result=true;//empty is default
        Iterator it=stubMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            Integer frequency = (Integer) pair.getValue();
            if(frequency!=0){
                result=false;
                break;
            }
        }
        return result;
    }

    /***Check if stubs for a special relationship empty or not*
     * @param node a node in KG
     * @param relationshipType a kind of relationship
     * @return true: empty false: not empty
     * @From 19 Oct 2018
     * */
    public boolean isStubTypeEmpty (Node node, String relationshipType) {
        boolean result;
        Map<String,Integer> stubMap = node.getAllCurrentStubs();//get all of the current stubs of this entity
        Integer frequency = stubMap.get(relationshipType);//get # of the current stubs for a specified relationship
        if(frequency == 0){
            result = true;
        }else {
            result = false;
        }
        return result;
    }

//    /**Check if one stub for a specified relationship used or not*
//     *@From 16 Nov 2018
//     * @param node a node in KG
//     *
//     * */
//    public boolean isStubTypeUsed (Node node, String relationshipType) {
//        boolean result;
//        Map<String, Integer> currentStubMap = node.getAllCurrentStubs();//get all of the current stubs of the entity
//        Integer currentNumber = currentStubMap.get(relationshipType);//get the # of the current stubs for a specified relationship
//        Map<String, Integer> originalStubMap = node.getAllOriginalStubs();//get all of the original stubs of the entity
//        Integer originalNumber = currentStubMap.get(relationshipType);//get the # of the original stubs of a specified relationship type
//        if (currentNumber < originalNumber){
//            result = true;
//        }else {
//            result = false;
//        }
//
//        return result;
//    }

    /**Get all of the neighbouring links of an entity in KG*
     * from 26 Sep 2018
     * @param entity an entity in the knowledge graph
     * @return all of the neighbouring edges of this entity
     * */
    public List<LabeledLink> getNeighbouringEdges(Node entity){
        List<LabeledLink> allNeighbouringLinks = new ArrayList<>();
        Set<LabeledLink> outgoingEdges = kg.outgoingEdgesOf(entity);//get all the outgoing edges of this entity
        Set<LabeledLink> incomingEdges = kg.incomingEdgesOf(entity);//get all the incoming edges of this entity
        allNeighbouringLinks.addAll(outgoingEdges);
        allNeighbouringLinks.addAll(incomingEdges);
        return allNeighbouringLinks;
    }


    /**show all of the edges on the shortest path, given a sequence of nodes on the path*
     * @from 24 Oct 2018
     * */
    public List<LabeledLink> showEdgesOnPath (List<Node> nodes) throws Exception{
        List<LabeledLink> links = new ArrayList<>();
        System.out.println("Now begin to get the edges on the path!");
        for(int i = 0; i < (nodes.size() - 1); i++){
            Node source = nodes.get(i);
            Node target = nodes.get(i+1);
            LabeledLink link = kg.getEdge(source, target);
            if(link != null){
                System.out.println("the uri of the edge from " + link.getSource().getId() + " to " + link.getTarget().getId() + " is: " + link.getUri());
                links.add(link);
            }else {
                LabeledLink reverseLink = kg.getEdge(target, source);
                System.out.println("the uri of the edge from " + reverseLink.getSource().getId() + " to " + reverseLink.getTarget().getId() + " is: " + reverseLink.getUri());
                links.add(reverseLink);
            }

        }
        System.out.println("# of edges on the path is: " + links.size());
        return links;
    }


    /**clear all of the orphan nodes in KG*
     * @from 10 Oct 2018
     * */
    public void clearOrphanNode(){

        Set<Node> nodeSet = kg.vertexSet();
        List<Node> nodeList = new LinkedList<>(nodeSet);
        int num=0;
        Iterator<Node> iterator = nodeList.iterator();
        while(iterator.hasNext()){
            Node node = iterator.next();
            List<LabeledLink> labeledLinks = getNeighbouringEdges(node);
            if(labeledLinks.size()==0){
                num++;
                kg.removeVertex(node);
            }
        }


        System.out.println(num + " nodes in KG are removed!");

    }

    /**Set the distance of every node in the KG INFINITY in order to get Dijkstra algorithm run correctly*
     *@From 7 Dec 2018
     * */
    public void setInfiniteDistance () {
        Set<Node> allNodes = kg.vertexSet();//get all of the vertices of the KG
        Iterator<Node> it = allNodes.iterator();
        while (it.hasNext()) {
            Node node = it.next();
            node.setDistance(Integer.MAX_VALUE);
            List<Node> nodeList = node.getShortestPath();
            nodeList.clear();//reset the initial shortest path zero for a new round of Dijkstra search
            node.setShortestPath(nodeList);
        }
    }

    /**Serialize a knowledge graph. Write a directed weighed multiple graph into a format like this:
     * # t 1
     * v 0 1
     * v 1 12
     * ..
     * e 0 14 8
     * ...
     * *
     * @from 12 Jan 2019
     * @param bigGraph a generated directed weighted multiple graph which is the common graph structure produced by Karma
     * @param fileName a file name for the serialized graph
     * */
    public void serializeKG (DirectedWeightedMultigraph<Node, LabeledLink> bigGraph, String fileName) {

        BufferedWriter bw = null;
        FileWriter fw = null;

        List<String> data = new ArrayList<>();//create a list containing all of the data
        try {

            String title = "t".concat(" ").concat("#").concat(" ").concat("1").concat("\n");
            data.add(title);

            Set<Node> nodeSet = bigGraph.vertexSet();
            Iterator<Node> it = nodeSet.iterator();
            while (it.hasNext()) {
                Node node = it.next();
                String nodeId = node.getId();
                String nodeUri = node.getUri();
                String eachNodeRow = "v".concat(" ").concat(nodeId).concat(" ").concat(nodeUri).concat("\n");
                data.add(eachNodeRow);
            }

            Set<LabeledLink> labeledLinkSet = bigGraph.edgeSet();
            Iterator<LabeledLink> iterator = labeledLinkSet.iterator();
            while (iterator.hasNext()) {
                LabeledLink labeledLink = iterator.next();
                String sourceNodeId = labeledLink.getSource().getId();
                String targetNodeId = labeledLink.getTarget().getId();
                String edgeUri = labeledLink.getUri();
                String eachEdgeRow = "e".concat(" ").concat(sourceNodeId).concat(" ").concat(targetNodeId).concat(" ").concat(edgeUri).concat("\n");
                data.add(eachEdgeRow);
            }

            /**Begin to serialize...**/
            fw = new FileWriter(Settings.SerializedKGFileAddress.concat(fileName));
            bw = new BufferedWriter(fw);

            Iterator<String> iterator1 = data.iterator();
            while (iterator1.hasNext()) {
                String row = iterator1.next();
                bw.write(row);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (NullPointerException ex) {
            ex.printStackTrace();
        } finally {
            try {
                if (bw != null) {
                    bw.close();
                }
                if (fw != null) {
                    fw.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    /**Get the frequency of a node with regard to a specified label in knowledge graph*
     *@created 21 Feb 2019
     *@param uri uri
     *@return # of nodes with the uri
     * */
    public int getFrequency (String uri) {
        Set<Node> nodeSet = kg.vertexSet();
        int freq = 0;
        Iterator<Node> iterator = nodeSet.iterator();
        while (iterator.hasNext()) {
            Node node = iterator.next();

            if (node.getUri().equals(uri)) {
                freq++;
            }
        }
        return freq;
    }

    /**Get the (mean of) degrees of a node with regard to a specified label in knowledge graph*
     * @created 21 Feb 2019
     * @param uri of a node
     * @return average degree of this node in the knowledge graph
     * */
    public int getDegree (String uri) {

        int degreeMean = 0;
        Set<Integer> degrees = new HashSet<>();//To hold the degree of each matched node of a specified entity type
        Set<Node> nodeSet = kg.vertexSet();
        Iterator<Node> iterator = nodeSet.iterator();
        while (iterator.hasNext()) {
            Node node = iterator.next();
            if (node.getUri().equals(uri)) {

                Set<LabeledLink> neighbours = kg.edgesOf(node);
                int degree = neighbours.size();
                degrees.add(degree);
            }

        }
        /**Get the average **/
        int sum = 0;
        for (Integer degree : degrees) {
            sum = sum + degree;
        }
        degreeMean = sum/degrees.size();

        return degreeMean;
    }

}




