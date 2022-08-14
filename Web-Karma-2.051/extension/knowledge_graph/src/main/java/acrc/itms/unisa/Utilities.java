package acrc.itms.unisa;

import com.google.common.collect.Iterables;
import org.jgrapht.graph.DirectedWeightedMultigraph;
import edu.isi.karma.rep.alignment.Node;
import edu.isi.karma.rep.alignment.LabeledLink;
import edu.isi.karma.rep.alignment.ObjectPropertyLink;
import edu.isi.karma.rep.alignment.Label;
import edu.isi.karma.rep.alignment.ObjectPropertyType;
import edu.isi.karma.rep.alignment.InternalNode;

import java.util.*;

public class Utilities {

    /**generate a random integer value between 0 and a specified range, i.e. [0,max)**/
    public static int getRandomIntegerBetweenRange(int max){
        Random rand = new Random();
        int rand_int = rand.nextInt(max);//generate a random integers from 0 to max
        return rand_int;
    }

    /**extract an element randomly from a set (with a uniform distribution)**/
    public static Object getRandomElementFromSet(Set hashSet) throws Exception{

        if(hashSet.size()<1){
            throw new Exception("the set is empty. No element can be extracted from it.");
        }

        Object randomElement = null;
        int randomIndex = getRandomIntegerBetweenRange(hashSet.size());//get a random number
        int i = 0;
        for(Object object : hashSet){
            if(i == randomIndex){
                randomElement = object;
                break;
            }
                i++;
        }
        return randomElement;
    }

    /**Remove elements from a set where the id of elements are known *
     * @param hashSet a hash set
     * @param filteredSet a set for the ids of all elements to be filtered
     * */
    public static void removeElementsFromSet(Set<Node> hashSet, Set<String> filteredSet) {
        for(Iterator<Node> iterator = hashSet.iterator(); iterator.hasNext();) {
            Node node=iterator.next();//get the node
            String nodeId=node.getId();//get the id of this node
            for(String filteredNodeId : filteredSet){//loop all of the elements in the set to be filtered
                if(nodeId.equals(filteredNodeId))
                    iterator.remove();
            }
        }
    }

   /**Remove an element from a knowledge graph node set where the id of the element is known*
    * @param hashSet a hash set
    * @param removedNodeId id of the node to be removed
    * */
   public static void removeElementFromSet(Set<Node> hashSet, String removedNodeId){
       for(Iterator<Node> iterator = hashSet.iterator(); iterator.hasNext();) {
           Node node=iterator.next();//get the node
           String nodeId=node.getId();//get the id of this node
           if(nodeId.equals(removedNodeId)){
               iterator.remove();
           }
       }
   }

    /**Convert a Set into an Array*
     * @param nodeSet a set of nodes
     * @return an array of nodes
     * */
    public static Node[] convertSetToArray(Set<Node> nodeSet){
        Node[] array=new Node[nodeSet.size()];//allocate memory for node array
        int i=0;
        for(Node node : nodeSet)
            array[i++]=node;
        System.out.println("a set of nodes has been converted to an array.");
        return array;
    }


    /**Shuffle the labels of all the directed weighted multiple graphs from a string (e.g. "http://unisa.edu.au/.../#Tax_Office1") to an integer (e.g. "60")*
     * @param graphList a list of graphs
     * @param edgeLabelMap Enrich a map with the correspondence. Key:string. The initial edge label, Value:numeric edge label from 0 (like, 0,1,2,...)
     * @param nodeLabelMap Enrich a map with the correspondence. Key:string. The initial node label, Value:numeric node label from 0 (like, 0,1,2,...)
     * */
    public static void shuffleLabels(List<DirectedWeightedMultigraph<Node, LabeledLink>> graphList, Map<String,Integer> edgeLabelMap, Map<String,Integer> nodeLabelMap){

        Set<String> edgeLabelSet = new HashSet<String>();//create an empty set for saving edge labels
        //Map<String, Integer> edgeLabelMap = new HashMap<String, Integer>();//create a map.<Key,Value>. Key:string edge label, Value:numeric edge label from 0 (like, 0,1,2,...)
        Set<String> nodeLabelSet = new HashSet<String>();//create an empty set for saving node labels
        //Map<String, Integer> nodeLabelMap = new HashMap<String, Integer>();//create a map .<Key,Value>. Key:string node label, Value:numeric node label from 0 (like, 0,1,2,...)
        for(DirectedWeightedMultigraph<Node, LabeledLink> directedWeightedMultigraph : graphList){

            Set<LabeledLink> edgeSet = directedWeightedMultigraph.edgeSet();//get all of the edges of the graph
            for(LabeledLink labeledLink : edgeSet){
                String label = labeledLink.getLabel().getUri();//get label
                edgeLabelSet.add(label);
            }

            Set<Node> nodeSet = directedWeightedMultigraph.vertexSet();//get all of the nodes of the graph
            for(Node node : nodeSet){
                String label = node.getUri();//get label
                nodeLabelSet.add(label);
            }
        }
        /**shuffle the labels**/
        int i=0;
        for(String label : edgeLabelSet){
            edgeLabelMap.put(label,i);
            i++;
        }
        int j=0;
        for(String label : nodeLabelSet){
            nodeLabelMap.put(label,j);
            j++;
        }
        /**reset the labels of the graph**/
        for(DirectedWeightedMultigraph<Node,LabeledLink> directedWeightedMultigraph : graphList){

            Set<LabeledLink> edgeSet = directedWeightedMultigraph.edgeSet();
            for(LabeledLink labeledLink : edgeSet){
                Label label = labeledLink.getLabel();//get label
                Integer number = edgeLabelMap.get(label.getUri());//get the corresponding number
                label.setUri(number.toString());//reset the label
            }

            Set<Node> nodeSet = directedWeightedMultigraph.vertexSet();
            for(Node node : nodeSet){
                Label label = node.getLabel();//get label
                if(nodeLabelSet.contains(label.getUri())){
                    Integer number = nodeLabelMap.get(label.getUri());
                    label.setUri(number.toString());//reset the label
                }
            }
        }

    }

    /**Shuffle the label of a directed weighted multiple graph by the existing correspondence. *
     *@param graph a graph of which the node labels and edge labels should be shuffled.
     *@param edgeLabelMap the existing edge correspondence
     *@param nodeLabelMap the existing node correspondence
     *@Exception the existing correspondence doesn't cover node label or edge label
     * @from 19 Sep 2018
     * */
    public static void shuffleLabelByExistingMaps (DirectedWeightedMultigraph<Node,LabeledLink> graph, Map<String,Integer> edgeLabelMap, Map<String,Integer> nodeLabelMap) throws Exception{
        Set<LabeledLink> edgeSet = graph.edgeSet();
        for(LabeledLink labeledLink : edgeSet){
            Label label = labeledLink.getLabel();//get label
            Integer number = edgeLabelMap.get(label.getUri());//get the corresponding number
            if(number != null){
                label.setUri(number.toString());//reset the label
            } else {
                throw new Exception("the label " + label + " is not covered by the existing edge correspondence!");
            }
        }

        Set<Node> nodeSet = graph.vertexSet();
        for(Node node : nodeSet){
            Label label = node.getLabel();//get label
            Integer number = nodeLabelMap.get(label.getUri());
            if(number != null){
                label.setUri(number.toString());//reset the label
            }else {
                throw new Exception("the label " + label + " is not covered by the existing node correspondence!");
            }
        }
    }



    /**shuffle the node id of a directed weighed multiple graph*
     * @param directedWeightedMultigraph a directed weighed multiple graph which the ids need shuffling
     * */
    public static void shuffleIds(DirectedWeightedMultigraph<Node, LabeledLink> directedWeightedMultigraph){

        Set<Node> nodeSet = directedWeightedMultigraph.vertexSet();
        /**create a map . <Key,Value>. Key:the original node id, Value:new id numbered from 0 (like, 0,1,2,...)**/
        Map<String, Integer> nodeLabelMap = new HashMap<>();//
        int index=0;//define the starting node id after renumbering
        for(Node node : nodeSet){
            String originalId = node.getId();//get the original node id
            nodeLabelMap.put(originalId,index);
            node.setId(String.valueOf(index));//set a new id to this node
            index++;
        }
    }

    /**shuffle the node & edge of id of a directed weighted multiple graph, while creating a new directed weighted multiple graph*
     * @from 2 Oct 2018
     *@param oldGraph the graph to be shuffled
     *@param newGraph the graph after shuffle
     * */
    public static void shuffleIds(DirectedWeightedMultigraph<Node, LabeledLink> oldGraph,
                                  DirectedWeightedMultigraph<Node,LabeledLink> newGraph){
        Set<Node> nodeSet = oldGraph.vertexSet();
        Map<Node, Node> nodeMap = new HashMap<>();//create a map . <Key,Value>. Key:the original node, Value:new node which id numbered from 0 (like, 0,1,2,...)
        int index = 0;//define the starting node id after renumbering
        for(Node node : nodeSet){
            String uri = node.getUri();//get the node uri in the old graph
            //String uri = node.getId();//update it to debug. 25 Oct 2018. For debugging
            Node n = new InternalNode(String.valueOf(index),new Label(uri));
            newGraph.addVertex(n);//add the new node into the new graph
            index++;
            nodeMap.put(node,n);
        }

        Set<LabeledLink> edgeSet = oldGraph.edgeSet();
        int linkIndex = 0;//define the starting edge id after renumbering
        for(LabeledLink labeledLink : edgeSet) {
            String linkUri = labeledLink.getUri();//get the edge uri in the new graph
            LabeledLink newLink = new ObjectPropertyLink(String.valueOf(linkIndex),new Label(linkUri),ObjectPropertyType.Direct);//create a new link
            Node oldSource = labeledLink.getSource();//get the old source
            Node oldTarget = labeledLink.getTarget();//get the old target
            Node newSource = nodeMap.get(oldSource);//get the new source
            Node newTarget = nodeMap.get(oldTarget);//get the new target
            newGraph.addEdge(newSource,newTarget,newLink);//add the new link into the new graph
            linkIndex++;
        }
    }

    /**import more entities to balance the fraction of different types. Should be called during each layer of search*
     * @from 5 Oct 2018
     * @param fraction expected proportion for each type of entity types
     * @param nodeNumberMap actual number of each entity types
     * @return expected number of each entity types
     * */
    public static Map<String, Integer> fitProportion(Map<String,Integer> fraction, Map<String,Integer> nodeNumberMap) throws Exception{
        int[] fractionVector = new int[fraction.size()];
        int[] nodeNumberArray = new int[fraction.size()];
        String[] nodeUriArray = new String[fraction.size()];
        int i=0;
        Iterator it = fraction.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            String uri = (String)pair.getKey();
            int value = (Integer) pair.getValue();
            int numberByUri = nodeNumberMap.get(uri);
            fractionVector[i]= value;//fraction value
            nodeNumberArray[i]=numberByUri;//node number of each entity type currently
            nodeUriArray[i]=uri;
            i++;
        }


        /**Firstly, try each element in 'nodeNumberArray[i]' as anchor**/
        int[] tempResult = new int[fraction.size()];//save a temporary result
        int[] addedNumber = new int[fraction.size()];//initialize the added number. Principle. 1)Avoid removing any nodes, 2)minimize # of nodes to be added.
        for (int j=0; j<nodeNumberArray.length; j++){

            boolean continueOuterLoop = false;
            if(nodeNumberArray[j]<1){
                continue;//if the number of entities is less than 1, it is impossible to be an anchor
            }else {
                //anchorIndex = j;
                double anchorProportion=(double)nodeNumberArray[j]/(double)fractionVector[j];//basic proportion potentially
                for(int k=0; k<fractionVector.length; k++){
                    tempResult[k]=(int)(fractionVector[k]*anchorProportion);
                    if(tempResult[k]<1){
                        if(nodeNumberArray[k]>1){
                            continueOuterLoop=true;
                            break;//break the inner loop, which means this anchor index is not possible
                        }
                        tempResult[k]=1;//the minimal number of 'tempResult[k]' is 1

                    }else if(tempResult[k]<nodeNumberArray[k]){
                        continueOuterLoop=true;
                        break;//break the inner loop, which means this anchor index is not possible
                    }
                }
                if(continueOuterLoop){
                    continue;
                }
            }
            /**now suppose each element in 'tempResult' is filled with a non negative number**/
            int cost = 0;//initialize the cost. 1)Avoid removing any nodes, 2)minimize # of nodes to be added.
            for(int p=0; p<tempResult.length; p++){
                if(tempResult[p]<nodeNumberArray[p]){
                    throw new Exception("It means that some nodes needs to be removed from the current KG, which is not allowed!");
                }else {
                    cost=cost + (tempResult[p]-nodeNumberArray[p]);
                }
            }
            addedNumber[j]=cost;//we prefer lower cost. But, if cost is equal to 0, it means impossible
        }

        /**Then, which anchor is what we need? We prefer lower cost. But, if cost is equal to 0, it means impossible**/
        int anchor = 0;
        int cost = 2147483647;
        for(int l=0; l<addedNumber.length; l++){
            if(addedNumber[l]==0){
                continue;
            }else {
                if(addedNumber[l]<cost){
                    cost=addedNumber[l];
                    anchor=l;
                }
            }
        }

        /**next, get the number vector that we expect**/
        int[] expect = new int[fraction.size()];
        double basic=(double)nodeNumberArray[anchor]/(double)fractionVector[anchor];
        for(int m=0; m<expect.length; m++){
            if(m==anchor){
                expect[m]=nodeNumberArray[m];
            }else {
                expect[m]=(int)(fractionVector[m]*basic);
                if(expect[m]<1){
                    expect[m]=1;//'1' is the minimal
                }

            }
        }
        int index=0;
        Map<String, Integer> expectMap = new HashMap<>();
        while (index<fraction.size()) {
            expectMap.put(nodeUriArray[index],expect[index]);
            index++;
        }
        return expectMap;
    }

//    /**Shuffling elements of set*
//     * @param original a set
//     * @return a shuffled list
//     * */
//    public static List<String> shuffleElementInSet(Set<String> original){
//        List<String> list = new ArrayList<>(original);
//        Collections.shuffle(list);
//        return list;
//    }

    /**Assign an Integer according to a predefined proportion. Given a predefined proportion and the total number, we will give the # of each type of entity types*
     *@param proportion predefined proportion for each entity type, such as Vehicle, Person, ...
     *@param total the total # of expected graph.
     *@return the reassigned number
     * @Exception if the value in 'proportion' is less than 1, then throw an exception. Do not support "Person, 1", "University, 0.05"....
     * @from 12 Oct 2018
     * */
    public static Map<String,Integer> assignInteger(Map<String,Integer> proportion, int total) throws Exception{
        Map<String,Integer> assignedInteger = new HashMap<>();
        int sum = 0;
        Iterator iterator = proportion.entrySet().iterator();
        while (iterator.hasNext()){
            Map.Entry pair = (Map.Entry)iterator.next();
            Integer number = (Integer) pair.getValue();//get value
            if(number < 1){
                throw new Exception("The value in proportion vector can not be less than 1!");
            }else {
                sum= sum+number;
            }
        }

        Iterator it = proportion.entrySet().iterator();
        while (it.hasNext()){
            Map.Entry pair = (Map.Entry)it.next();
            String uri = (String) pair.getKey();//get key
            Integer number = (Integer) pair.getValue();//get value
            Integer actualNumber = (int)(((double)number * (double)total)/(double)sum)+1;
            assignedInteger.put(uri,actualNumber);
        }
        return assignedInteger;
    }

    /**print map**/
    public static void printMap(Map mp){
        Iterator it = mp.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            System.out.println(pair.getKey() + " = " + pair.getValue());
        }
    }

    //museum_crm_test
    public static void main(String args[]){

        double n = Math.random();
        System.out.println(n);

    }

    /**Given a set, randomly pick up p% elements from a set**/
    public static Set<Node> randomPart (Set<Node> target, double percentage) {
        Set<Node> part = new HashSet<>();
        int p = (int) (percentage*target.size());
        Iterable<Node> partOfCandidates = Iterables.limit(target, p);//randomly pick up some of elements of the sets
        Iterator<Node> iterator = partOfCandidates.iterator();
        while (iterator.hasNext()) {
            Node node = iterator.next();
            part.add(node);
        }
        return part;
    }
}
