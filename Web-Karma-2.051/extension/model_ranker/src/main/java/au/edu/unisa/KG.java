package au.edu.unisa;

import acrc.itms.unisa.GephiUtilities;
import au.com.d2dcrc.yago2es.*;
import edu.isi.karma.rep.alignment.*;
import edu.isi.karma.rep.alignment.Label;
import org.jgrapht.graph.DirectedWeightedMultigraph;
import org.semanticweb.yars.nx.parser.NxParser;

import java.awt.*;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.List;

/**it represents American Art Museums Knowledge Graph**/
public class KG {

    private DirectedWeightedMultigraph<Node,LabeledLink> kg = null;
    private Map<Integer, LinkedList<Integer>> adj = null;  //list for adjacent node representation. key: Node ID, Value: all of its adjacent nodes. Added on 1 Mar 2019
    private Map<Integer,Boolean> visited = null;//Record which node has been visited in a round of DFS. Key: node ID, value: false - not visited
    private Map<String,Integer> numeratedVertexLabels = new HashMap<>();//key: unique vertex label, value: unique number from 0
    private Map<String,Integer> numeratedEdgeLabels = new HashMap<>();//key:unique edge label, value: unique number from 0

    public KG (DirectedWeightedMultigraph<Node,LabeledLink> kg) {
        this.kg = kg;
    }

    /**fill in an adjacent list representation, for DFS
     * Initialization for checking connectivity
     * @NOTE: Here we do not consider the direction of edges
     * *
     * @create 1 Mar 2019
     * @revised  10 April 2019
     * */
    public void init () {
        Set<Node> allNodeSet = kg.vertexSet();//get all the nodes
        Set<LabeledLink> allLinkSet = kg.edgeSet();//get all the edges
        Map<String, Integer> nodeIDMap = new HashMap<>();//Key: old node id, e.g. abc-ace... Value: new node id: 0,1,2...

        //here, re-order all the id of nodes, from 0.
        Integer newNodeID = 0;
        Iterator<Node> iterator1 = allNodeSet.iterator();
        while (iterator1.hasNext()) {
            Node node = iterator1.next();
            String oldNodeID = node.getId();
            node.setId(newNodeID.toString());
            nodeIDMap.put(oldNodeID, newNodeID);
            newNodeID++;
        }

        adj = new HashMap<>();
        visited = new HashMap<>();

        //Mark all the vertices as not visited, and construct the adjacent list for each node
        for (Node node : allNodeSet) {
            visited.put(Integer.parseInt(node.getId()), false);
            LinkedList<Integer> adjacentNodeIDList = new LinkedList<>();
            for (LabeledLink labeledLink : allLinkSet) {
                Node source = labeledLink.getSource();
                Node target = labeledLink.getTarget();
                if (node.getId().equals(source.getId())) {
                    Node end = labeledLink.getTarget();
                    adjacentNodeIDList.add(Integer.parseInt(end.getId()));
                } else if (node.getId().equals(target.getId())) {
                    Node start = labeledLink.getSource();
                    adjacentNodeIDList.add(Integer.parseInt(start.getId()));
                }
            }
            adj.put(Integer.parseInt(node.getId()),adjacentNodeIDList);
        }
    }


    /**Re-order a graph making an unique id (like: abcde-edfsdf-fwsd) for each node *
     * @created 1 May 2019
     *
     * */
    public void reOrderGraphNode () {
        Set<Node> allNodeSet = kg.vertexSet();//get all the nodes
        Set<LabeledLink> allLinkSet = kg.edgeSet();//get all the edges
        Map<String, String> nodeIDMap = new HashMap<>();//Key: old node id, e.g. abc-ace... Value: new node id: 0,1,2...

        //here, re-order all the id of nodes, given a unique id
        Iterator<Node> iterator1 = allNodeSet.iterator();
        while (iterator1.hasNext()) {
            Node node = iterator1.next();
            String oldNodeID = node.getId();
            String newNodeID = RandomStringUUID.createUUID();
            node.setId(newNodeID);
            nodeIDMap.put(oldNodeID, newNodeID);
        }

    }



    /**A function used by DFS*
     * @Adatpted from address: https://www.geeksforgeeks.org/depth-first-search-or-dfs-for-a-graph/
     * @create 1 Mar 2019
     * @param v the current vertex
     *@param visited an array recording if a node is visited or not
     *@param visitedNode all the visited nodes
     * */
    public void DFSUtil (int v, Map<Integer,Boolean> visited, List<Integer> visitedNode) {

        /**Mark the current node as visited and print it**/
        visited.put(v, true);
        System.out.print(v + " ");
        visitedNode.add(v);

        //Recur for all the vertices adjacent to this vertex
        Iterator<Integer> i = adj.get(v).listIterator();
        while (i.hasNext()) {
            int n = i.next();
            if (!visited.get(n)) {
                DFSUtil(n, visited, visitedNode);
            }
        }
    }

    /**The function to do DFS traversal. It uses recursive DFSUtil()*
     * @adapt https://www.geeksforgeeks.org/depth-first-search-or-dfs-for-a-graph/
     * @created 1 Mar 2019
     * @param v id of the start node
     * */
    public List<Integer> DFS (int v) throws Exception{

        //if the start node does not exist in the tree, throw exception
        if (!visited.keySet().contains(v)) {
            throw new Exception("This start node does not exist in the Steiner tree!");
        }

        List<Integer> visitedNodeList = new ArrayList<>();

        /**Call the recursive helper function to print DFS traversal**/
        DFSUtil(v, visited, visitedNodeList);
        return visitedNodeList;
    }



    /**find all the connected components in the Steiner tree*
     * @created 4 Mar 2019
     * @revised 1 May 2019
     * @return all of the connected components from the Steiner tree
     * */
    public List<DirectedWeightedMultigraph<Node,LabeledLink>> getConnectedComponent () {
        List<Set<Node>> classfiedNodeSet = new ArrayList<>();
        List<DirectedWeightedMultigraph<Node, LabeledLink>> directedWeightedMultigraphList = new ArrayList<>();//to hold connected graphs
        Set<Node> nodeSet = kg.vertexSet();
        Map<Node,Boolean> nodeBooleanMap = new HashMap<>();//true: this node has been classified; false: this node hasn't been classified into a group
        for (Node node : nodeSet) {
            nodeBooleanMap.put(node, false);//Initially, all the nodes have not been classified
        }

        //get connected components
        Node randomStartNode = null;
        while (true) {
            Boolean allAreClassified = true;

            //get a random unclassified node from Steiner tree
            Iterator it = nodeBooleanMap.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry) it.next();
                //Node node = (Node) pair.getKey();
                Boolean classified = (Boolean) pair.getValue();
                if (!classified) {
                    allAreClassified = false;
                    randomStartNode = (Node) pair.getKey();//get a random unclassified node
                    break;
                }
            }

            if (allAreClassified) {
                break;
            }

            Set<Node> allNodeSetInSubStructure = new HashSet<>();

            //get all the connected nodes from the random start node
            try {
                List<Integer> traversedNodeIDList = DFS(Integer.parseInt(randomStartNode.getId()));

                //change these Node status as visited
                Iterator iterator = nodeBooleanMap.entrySet().iterator();
                while (iterator.hasNext()) {
                    Map.Entry pair = (Map.Entry) iterator.next();
                    Node node = (Node) pair.getKey();
                    Boolean classified = (Boolean) pair.getValue();
                    if (traversedNodeIDList.contains(Integer.parseInt(node.getId()))) {
                        nodeBooleanMap.put(node,true);//update value in node boolean map
                        allNodeSetInSubStructure.add(node);
                    }
                }

                //recover the whole connected graph
                DirectedWeightedMultigraph<Node,LabeledLink> connectedG = new DirectedWeightedMultigraph<>(LabeledLink.class);
                for (Node node : allNodeSetInSubStructure) {
                    connectedG.addVertex(node);
                }

                Set<LabeledLink> edgeSet = kg.edgeSet();
                for (LabeledLink labeledLink : edgeSet) {

                    Node source = labeledLink.getSource();
                    Node target = labeledLink.getTarget();

                    if (allNodeSetInSubStructure.contains(source) && allNodeSetInSubStructure.contains(target)) {
                        connectedG.addEdge(source,target,labeledLink);
                    }
                }

                classfiedNodeSet.add(allNodeSetInSubStructure);
                directedWeightedMultigraphList.add(connectedG);
            } catch (Exception ex) {

                ex.printStackTrace();
            }
        }


        return directedWeightedMultigraphList;
    }


    /**Compare two entities in the two different knowledge graph, and judge if they are the same entity (entity resolution)
     *We just use a simple Rule-based Entity Resolution method to resolve entities, described as follows:
     *if any two records are similar on all of the properties and their type is the same, then they refer to the same entity
     * @from 12 April 2019
     * @revised 5 June 2019
     *@param entity1 1st entity
     *@param entity2 2nd entity
     * @return if entity1 is actually the same as 2nd entity, return true. Otherwise, return false.
     * **/
    public static boolean  entityResolution (Node entity1, Node entity2) {
        boolean result = false;
        String entityType1 = entity1.getUri();//get entity URI
        String entityType2 = entity2.getUri();//get entity URI

        if (!entityType1.equals(entityType2)) {
            result = false;
        } else {

            Map<String, Object> attributeValueMap1 = entity1.getAttributeValueMap();
            Map<String, Object> attributeValueMap2 = entity2.getAttributeValueMap();

            //compare two attribute value map the same or not
            if (Utilities.isEqualMap(attributeValueMap1,attributeValueMap2)) {
                result = true;
            } else {
                result = false;
            }

        }
        return result;
    }

    /**get the graph*
     * @created 12 April 2019
     * */
    public DirectedWeightedMultigraph<Node, LabeledLink> getKg() {
        return kg;
    }

    /**set the graph*
     * @created 1 May 2019
     * */
    public void setKg (DirectedWeightedMultigraph<Node,LabeledLink> labeledLinkDirectedWeightedMultigraph) {
        this.kg = labeledLinkDirectedWeightedMultigraph;
    }
    /**Merge two Museum knowledge graphs*
     * NOTE: either of knowledge graphs, is allowed to be unconnected
     * @created 12 April 2019
     * @revised 30 April 2019
     * @param kg graph to be merged
     * @return merged graph
     * */
    public DirectedWeightedMultigraph<Node,LabeledLink> merge (KG kg) {

        //remove orphan nodes
        this.clearOrphanNode();
        kg.clearOrphanNode();

        this.reOrderGraphNode();
        kg.reOrderGraphNode();

        Set<Node> nodeSet1 = this.kg.vertexSet();//get all the entities
        Set<Node> nodeSet2 = kg.getKg().vertexSet();//get all the entities

        Map<Node,Node> nodeNodeMap = new HashMap<>();//record all the same entities from g1 and g2

        //get duplicated entities from g1 and g2
        for (Node node1 : nodeSet1) {

            for (Node node2 : nodeSet2) {

                boolean result = entityResolution(node1,node2);
                if (result) {
                    nodeNodeMap.put(node1,node2);
                }
            }
        }

        Set<Node> duplicatedNodeG1 = nodeNodeMap.keySet();
        System.out.println();
        System.out.println("# of the same entities: " + nodeNodeMap.size());

        //print out all of the same entities from two graphs
        Iterator it = nodeNodeMap.entrySet().iterator();
        while (it.hasNext()) {

            Map.Entry pair = (Map.Entry) it.next();
            Node node1 = (Node) pair.getKey();
            Node node2 = (Node) pair.getValue();
            System.out.println("the mapping is: " + node1.getUri() + " -------- " + node2.getUri());
        }

        //create and enrich the merged graph
        DirectedWeightedMultigraph<Node, LabeledLink> mergedGraph = new DirectedWeightedMultigraph<>(LabeledLink.class);

        //add the nodes from the 2nd graph
        for (Node node : nodeSet2) {
            mergedGraph.addVertex(node);
        }

        //add the edges from the 2nd graph
        Set<LabeledLink> edgeSet2 = kg.getKg().edgeSet();
        for (LabeledLink labeledLink : edgeSet2) {
            Node source = labeledLink.getSource();
            Node target = labeledLink.getTarget();
            mergedGraph.addEdge(source,target,labeledLink);
        }

        //add the nodes from the 1st graph, but avoid to add the duplicated (the same) entities
        for (Node node : nodeSet1) {

            if (!duplicatedNodeG1.contains(node)) {//check if it is not the similar entity
                boolean r = mergedGraph.addVertex(node);
            }
        }

        //add edges from  the 1st graph
        Set<LabeledLink> edgeSet1  = this.kg.edgeSet();
        for (LabeledLink labeledLink : edgeSet1) {
            Node source = labeledLink.getSource();
            Node target = labeledLink.getTarget();
            String edgeUri = labeledLink.getUri();//uri of the edge to be merged

            Node updatedSource = null;
            if (duplicatedNodeG1.contains(source)) {
                updatedSource = nodeNodeMap.get(source);
            } else {
                updatedSource = source;
            }

            Node updatedTarget = null;
            if (duplicatedNodeG1.contains(target)) {
                updatedTarget = nodeNodeMap.get(target);
            } else {
                updatedTarget = target;
            }

            LabeledLink updatedLink = new ObjectPropertyLink(labeledLink.getId(),new Label(edgeUri), ObjectPropertyType.Direct);//create a new link

            //check if there exist an edge in the current graph with the same label
            LabeledLink link = mergedGraph.getEdge(updatedSource,updatedTarget);
            if (link == null) {
                mergedGraph.addEdge(updatedSource,updatedTarget,updatedLink);
            } else if ((link != null) && (!link.getUri().equals(edgeUri))) {
                mergedGraph.addEdge(updatedSource,updatedTarget,updatedLink);
            }
        }

        return mergedGraph;
    }


    /**clear orphan nodes in knowledge graphs*
     * @from 1 May 2019
     * */
    public void clearOrphanNode (){

        this.init();//install special data structure to DFS
        List<DirectedWeightedMultigraph<Node,LabeledLink>> connectedGraphList = getConnectedComponent();

        Set<Node> toBeRemoved  = new HashSet<>();//to hold orphan nodes

        for (DirectedWeightedMultigraph<Node, LabeledLink> connectedGraph : connectedGraphList) {

            Set<Node> nodeSet = connectedGraph.vertexSet();
            if (nodeSet.size() == 1) {
                toBeRemoved.addAll(nodeSet);
            }
        }

        DirectedWeightedMultigraph<Node, LabeledLink> graph = getKg();
        DirectedWeightedMultigraph<Node, LabeledLink> updatedGraph = new DirectedWeightedMultigraph<>(LabeledLink.class);

        for (Node node : graph.vertexSet()) {
            if (!toBeRemoved.contains(node)) {
                updatedGraph.addVertex(node);
            }
        }

        for (LabeledLink labeledLink : graph.edgeSet()) {

            Node source = labeledLink.getSource();
            Node target = labeledLink.getTarget();
            updatedGraph.addEdge(source,target,labeledLink);
        }

        this.setKg(updatedGraph);
    }

    /**Conceptualize knowledge graph*
     * lift the label of each entity (e.g. E21_Person1) from instance level to type level (e.g. E21_Person)
     *@from April 2019
     *@revised 6 June 2019
     * */
    public void conceptualize () {

        DirectedWeightedMultigraph<Node,LabeledLink> graph = this.getKg();
        Set<Node> nodeSet = graph.vertexSet();
        Iterator<Node> iterator = nodeSet.iterator();
        while (iterator.hasNext()) {
            Node node = iterator.next();
            String uri = node.getUri();
            String entityType = uri.substring(0, uri.length()-1);
            node.setEntityType(entityType);
        }
    }

    /**Numerate the vertex labels of the graph*
     * @revised 6 June 2019
     * @return  A map.
     * Key is the non-duplicated labels for vertices of the graph
     * Value is assigned number for this label from 0,1,2,...*
    *  adapt work in 2017   */
    public Map<String, Integer> numerateVertexLables() {

        /**clear the numerated label list**/
        numeratedVertexLabels.clear();

        /**create a new list to contain all of label of vertices **/
        List<String> functionalVertexLabel = new ArrayList<String>();
        Set<Node> allVertices = kg.vertexSet();
        Iterator<Node> iterator = allVertices.iterator();
        while (iterator.hasNext()) {

            Node node = iterator.next();
            functionalVertexLabel.add(node.getEntityType());//get entity type
        }

        /**Remove the repeated elements from 'functionalVertexLabel'**/
        Set<String> functionalVertexLabelSet = new HashSet<String>();
        functionalVertexLabelSet.addAll(functionalVertexLabel);

        /**Numerate the labels in the 'functionalVertexLabel'**/
        int vertexNumber = 0; //number for the labels of vertices
        Iterator<String> it = functionalVertexLabelSet.iterator();
        while (it.hasNext()) {

            String vertexLabel = it.next();
            Integer vertexIntegerNumber = new Integer(vertexNumber);

            /**Put 'vertexLabel' and 'vertexIntegerNumber' into 'numeratedLabels'**/
            numeratedVertexLabels.put(vertexLabel, vertexIntegerNumber);
            vertexNumber++;
        }
        return numeratedVertexLabels;
    }

    /**Numerate the edge labels of the graph*
     *
     * @return A map. Key: the non-repeated labels for edges of the graph,
     * Value: assigned number for this label from 0,1,2,...
     * adapt my work on relationship disambiguation in 2017
     */
    public Map<String, Integer> numerateEdgeLables(){

        /**clear the numerated edge label list**/
        numeratedEdgeLabels.clear();

        /**create a new list to contain all of label of edges**/
        List<String> edgeLabel = new ArrayList<String>();
        Set<LabeledLink> allEdges = kg.edgeSet();
        Iterator<LabeledLink> iterator = allEdges.iterator();
        while (iterator.hasNext()){

            LabeledLink labeledLink = iterator.next();
            edgeLabel.add(labeledLink.getUri());
        }


        /**Remove the repeated label of edges**/
        Set<String> edgeLabelSet = new HashSet<String>();
        edgeLabelSet.addAll(edgeLabel);

        /**Numerate the labels**/
        int edgeNumber = 0; //number for the labels of vertices
        Iterator<String> it = edgeLabelSet.iterator();
        while (it.hasNext()){

            String label = it.next();
            Integer edgeIntegerNumber = new Integer(edgeNumber);

            /**Put 'label' and 'edgeIntegerNumber' into 'numeratedLabels'**/
            numeratedEdgeLabels.put(label,edgeIntegerNumber);
            edgeNumber++;
        }

        return numeratedEdgeLabels;
    }


    /***serialize knowledge graph**/
    /**
     *
     * @adapt my work on relationship disambiguation in 2017
     * @param fileName the file (including directory) that is written to
     */
    public void serialize(String fileName){

        BufferedWriter bw = null;
        FileWriter fw = null;

        try{
            /**create a list to contain all the data **/
            List<String> data = new ArrayList<String>();

            /**print the title of each graph, e.g. "t # 0", "t # 1", "t # 2", ......**/
            String title = "t".concat(" ").concat("#").concat(" ").concat(String.valueOf(1).concat("\n"));
            data.add(title);
            System.out.print(title);

            /**Get all the vertices of boundary graph**/
            Set<Node> allVertex = kg.vertexSet();

            /**Loop all the vertices of this boundary graph**/
            Iterator<Node> it = allVertex.iterator();

            while (it.hasNext()){

                Node node = it.next();

                /**Get the numeric label of this vertex**/
                String entityType = node.getEntityType();

                String vertexIdWithLabel = "v".concat(" ").concat(String.valueOf(node.getId())).concat(" ").concat(numeratedVertexLabels.get(entityType).toString()).concat("\n");

                /**print each vertex with id and its label, which is represented as a number as well. e.g. "v 1 0"s**/
                data.add(vertexIdWithLabel);
                //System.out.print(vertexIdWithLabel);

            }

            /**Get all the edges of knowledge graph**/
            Set<LabeledLink> allEdges = kg.edgeSet();

            /**Loop all the edges of this knowledge graph**/
            Iterator<LabeledLink> its = allEdges.iterator();

            while (its.hasNext()){

                LabeledLink labeledLink = its.next();

                /**Get the numeric label of this edge**/
                String relationshipType = labeledLink.getUri();

                /**Get the numerated start id & end id of this edge**/
                String start = labeledLink.getSource().getId();
                String target = labeledLink.getTarget().getId();

                String edgeWithLabel = "e".concat(" ").concat(start).concat(" ").concat(target).
                        concat(" ").concat(numeratedEdgeLabels.get(relationshipType).toString()).concat("\n");
                data.add(edgeWithLabel);
                /**print each edge with start and end and its label, which is represented as a number as well.
                 * e.g. "e 0 1 3"**/
                //System.out.print(edgeWithLabel);
            }


            fw = new FileWriter(fileName);
            bw = new BufferedWriter(fw);

            Iterator<String> stringIterator = data.iterator();
            while (stringIterator.hasNext()){

                String string = stringIterator.next();
                bw.write(string);
            }


            System.out.print("Serialization Done!");

        }catch (IOException e){

            e.printStackTrace();

        }catch (NullPointerException e){

            e.printStackTrace();
        }finally {

            try{

                if(bw != null)
                    bw.close();

                if(fw != null)
                    fw.close();

            }catch (IOException ex){

                ex.printStackTrace();
            }
        }


    }


    /**Parse the rough Smithsonian American Art Museum N3 file*
     * Might be useless because we do not use rough Museum rdf any more. 4 June 2019.
     * @from 6 April 2019
     * @param filePath
     * @return list of entities
     * @revised 30 April 2019
     * */
    public static DirectedWeightedMultigraph<Node, LabeledLink> parseSAAMRDF (String filePath) {

        DirectedWeightedMultigraph<edu.isi.karma.rep.alignment.Node,LabeledLink> graph = new DirectedWeightedMultigraph<>(LabeledLink.class);

//        Set<String> entitySet = new HashSet<>();
//        List<YagoFact> allFacts = new ArrayList<>();
//        try {
//
//            FileInputStream is = new FileInputStream(filePath);
//            NxParser nxp = new NxParser();
//            nxp.parse(is);
//            while (nxp.hasNext()) {
//
//                org.semanticweb.yars.nx.Node[] nx = nxp.next();/**Get a triple**/
//                String subject = nx[0].toString();
//                subject = trim(subject);
//                entitySet.add(subject);
//                String predicate = nx[1].toString();
//                predicate = trim(predicate);
//                String obj = nx[2].toString();
//                obj = trim(obj);
//                //System.out.println(subject + "   " + predicate + "   " + obj);
//
//                YagoFact fact = new YagoFact(subject,predicate,obj);
//                allFacts.add(fact);
//            }
//
//            System.out.println("there are totally " + entitySet.size() + " distinct entities.");
//        }catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        //create all of the nodes in the knowledge graph. Each node represents a entity
//        Set<edu.isi.karma.rep.alignment.Node> nodeSet = new HashSet<>();
//        Map<String, Node> nodeMap = new HashMap<>();
//        for (String subject : entitySet) {
//            edu.isi.karma.rep.alignment.Node node = new edu.isi.karma.rep.alignment.InternalNode(RandomStringUUID.createUUID(), new Label(subject));
//            nodeSet.add(node);
//            nodeMap.put(subject, node);
//            graph.addVertex(node);//add this node to the graph
//        }
//
//        for (YagoFact fact : allFacts) {
//
//            String subject = fact.getSubject();
//            String predicate = fact.getPredicate();
//            String object = fact.getObject();
//
//            //Begin to link different entities, with an object link relationship, like: "http://www.cidoc-crm.org/cidoc-crm/P43_has_dimension "
//            if (entitySet.contains(subject) && entitySet.contains(object)) {
//                edu.isi.karma.rep.alignment.Node source = nodeMap.get(subject);
//                edu.isi.karma.rep.alignment.Node target = nodeMap.get(object);
//                //will create an edge between the source and the target
//                LabeledLink link = new ObjectPropertyLink(RandomStringUUID.createUUID(), new Label(predicate), ObjectPropertyType.Direct);
//                graph.addEdge(source, target, link);//add this edge to the graph
//            } else {
//                //if the predicate is "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", then save the 'object' as 'entity type'
//                if (predicate.equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#type")) {
//
//                    //iterate all the entities existing in the knowledge graph
//                    for (edu.isi.karma.rep.alignment.Node entity : nodeSet) {
//                        if (entity.getUri().equals(subject)) {
//                            entity.setEntityType(object);//assign entity type to each entity
//                        }
//                    }
//                    //if the predicate is not "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", but the 'object' is not in the set of 'subject', it is the attribute of an entity
//                } else {
//
//                    edu.isi.karma.rep.alignment.Node source = nodeMap.get(subject);
//                    source.addAttributeValue(predicate, object);
//                }
//            }
//
//        }
//
//        System.out.println("All of the entities with their types have been determined!");
//        System.out.println("There are totally " + graph.vertexSet().size() + " nodes!");
//        System.out.println("There are totally " + graph.edgeSet().size() + " edges!");



        return graph;
    }


    /**Detect 3 different datas from SMMA data*
     * @from 30 April 2019
     *pattern 1: starts with _: e.g. "_:crm_E54_Dimension1_6c8d57007697917591051856e6b1f14ac6168281_N15"
     *pattern 2: embraced by "<>" e.g. "<http://erlangen-crm.org/current/P3_has_note>"
     *pattern 3: embraced by "" e.g. "120 x 600 inches"
     * @param str a string be be trimmed
     * @return trimmed string
     * */
    public static String trim (String str) {
        String trimmed = "";

        char c = str.charAt(0);//return the first character of string

        if (c == '_') {
            trimmed = str.substring(2, str.length());
        } else if (c == '<') {
            trimmed = str.substring(1, str.length()-1);
        } else if (c == '"') {
            trimmed = str.substring(1, str.length()-1);
        }
        return trimmed;
    }


    /**Extract entity type from identifier string*
     * e.g. extract 'E35_Title1' from 'E35_Title1_976a176f91e777e7fee5eece6c04013bc042e47f_N24'
     *@from 5 June 2019
     *@param identifier original string
     *@return a returned entity type
     * */
    private static String extractEntityType (String identifier) {

        int length = identifier.length();
        String trimed = "";
        int index = 0;
        for (int i = length-1 ; i >= 0; i--) {
            char c = identifier.charAt(i);
            if (c == '_') {
                index++;
            }
            if (index == 2) {
                trimed = identifier.substring(0,i);
                break;
            }


        }

        return trimed;
    }




    /**Parse modified Museum RDF files*
     * @from 4 June 2019
     * @param filePath file url and name
     * @return directed graph (NOTE: graph might not be connected)
     * */
    public static DirectedWeightedMultigraph<Node, LabeledLink> parseSAAMRDF2 (String filePath) {

        DirectedWeightedMultigraph<edu.isi.karma.rep.alignment.Node,LabeledLink> graph = new DirectedWeightedMultigraph<>(LabeledLink.class);

        Set<String> entitySet = new HashSet<>();
        List<YagoFact> allFacts = new ArrayList<>();
        try {

            FileInputStream is = new FileInputStream(filePath);
            NxParser nxp = new NxParser();
            nxp.parse(is);
            while (nxp.hasNext()) {

                org.semanticweb.yars.nx.Node[] nx = nxp.next();/**Get a triple**/
                String subject = nx[0].toString();
                subject = trim(subject);
                entitySet.add(subject);
                String predicate = nx[1].toString();
                predicate = trim(predicate);
                String obj = nx[2].toString();
                obj = trim(obj);
                //System.out.println(subject + "   " + predicate + "   " + obj);
                YagoFact fact = new YagoFact(subject,predicate,obj);
                allFacts.add(fact);
            }

            System.out.println("there are totally " + entitySet.size() + " distinct entities.");
        }catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("There are " + allFacts.size() + " triples!");

        /**First, fold the type and attribute to the entity. And, remove the triples like:*
         * E12_Production1_d12f899b141d4d232a0c1951897b0cb41ea00650_N1016   http://www.w3.org/2000/01/rdf-schema#label   Henry James, Sr.
         * E35_Title1_d12f899b141d4d232a0c1951897b0cb41ea00650_N1014   http://www.w3.org/1999/02/22-rdf-syntax-ns#type   http://erlangen-crm.org/current/E35_Title
         * */
        Set<edu.isi.karma.rep.alignment.Node> nodeSet = new HashSet<>();//save all the entities
        Map<String, Node> nodeMap = new HashMap<>();//Key: Id the entity, like: E35_Title1_d12f899b141d4d232a0c1951897b0cb41ea00650_N1014; Value: its entity
        Iterator<YagoFact> iterator = allFacts.iterator();
        while (iterator.hasNext()) {

            YagoFact fact = iterator.next();
            String subject = fact.getSubject();
            String predicate = fact.getPredicate();
            String object = fact.getObject();

            if (predicate.equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#type")) {
                edu.isi.karma.rep.alignment.Node node = new edu.isi.karma.rep.alignment.InternalNode(subject, new Label(extractEntityType(subject)));
                nodeSet.add(node);
                //graph.addVertex(node);//add this node to the graph
                nodeMap.put(subject,node);
                iterator.remove();
            }
        }
        System.out.println("There are " + allFacts.size() + " triples after removing type-triples!");

        /**Then, fold the attribute to the entities**/
        Set<String> entityURISet = nodeMap.keySet();
        Iterator<YagoFact> iterator1 = allFacts.iterator();
        while (iterator1.hasNext()) {
            YagoFact fact = iterator1.next();
            String subject = fact.getSubject();
            String predicate = fact.getPredicate();
            String object = fact.getObject();

            System.out.println(subject + "   " + predicate + "   " + object);
            if (!entityURISet.contains(object)) {//if the predicate is not "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", and the 'object' is not in the set of 'subject', then it is the attribute of an entity
                edu.isi.karma.rep.alignment.Node entity = nodeMap.get(subject);//Get the entity
                entity.addAttributeValue(predicate, object);
                iterator1.remove();
            }

        }
        System.out.println("There are " + allFacts.size() + " triples after folding attributes!");

        /**Next, resolve the same entities. Like: if two E67_Birth_123456 and E67_234567 has the same Birth_URL, then merge them**/
        Map<String,List<String>> equalEntityMap = new HashMap<>();//key: a node, value: all the equal entities
        for (edu.isi.karma.rep.alignment.Node node : nodeSet) {
            List<String> equlaEntities = new ArrayList<>();
            for (edu.isi.karma.rep.alignment.Node eachNode : nodeSet) {

                if (node.getId().equals(eachNode.getId())) {
                    continue;//avoid the same entity
                } else {
                    boolean isEqual = entityResolution(node,eachNode);
                    if (isEqual) {
                        String id = eachNode.getId();
                        equlaEntities.add(id);
                    }
                }
            }
            equalEntityMap.put(node.getId(),equlaEntities);
        }

        /**Next, add directed links to the knowledge graph**/
        Iterator<YagoFact> iterator2 = allFacts.iterator();
        while (iterator2.hasNext()) {
            YagoFact fact = iterator2.next();
            String subject = fact.getSubject();
            String predicate = fact.getPredicate();
            String object = fact.getObject();

            Node source = nodeMap.get(subject);//Get source node
            Node target = nodeMap.get(object);//Get target node
            LabeledLink link = new ObjectPropertyLink(RandomStringUUID.createUUID(), new Label(predicate), ObjectPropertyType.Direct);//create link between source and target

            List<String> equalNodeIdToSource = equalEntityMap.get(source.getId());
            List<String> equalNodeIdToTarget= equalEntityMap.get(target.getId());
            Node adjustedSource = null;
            Node adjustedTarget = null;

            Set<Node> currentNodes = graph.vertexSet();
            //get the equivalent node to source in current knowledge graph
            if (!equalNodeIdToSource.isEmpty()){
                for (Node node : currentNodes) {
                    if (equalNodeIdToSource.contains(node.getId())) {
                        adjustedSource = node;
                    }
                }
            }

            //get the equivalent node to target in current knowledge graph
            if (!equalNodeIdToTarget.isEmpty()) {
                for (Node node : currentNodes) {
                    if (equalNodeIdToTarget.contains(node.getId())) {
                        adjustedTarget = node;
                    }
                }
            }

            //now add nodes and links to graph
            if (adjustedSource == null && adjustedTarget == null) {//case 1: both source and target are new
                graph.addVertex(source);
                graph.addVertex(target);
                graph.addEdge(source, target, link);//add this edge to the graph
                source.addOutgoingLinks(predicate,0);//0 has no meaning here
                target.addIncomingLinks(predicate,0);//0 has no meaning here
            } else if (adjustedSource != null && adjustedTarget == null) {//case 2: source has existed, target is new
                graph.addVertex(target);
                graph.addEdge(adjustedSource, target, link);
                adjustedSource.addOutgoingLinks(predicate,0);
                target.addIncomingLinks(predicate,0);
            } else if (adjustedSource == null && adjustedTarget != null) {//case 3: source is new, target has existed in the graph
                graph.addVertex(source);
                graph.addEdge(source, adjustedTarget, link);
                source.addOutgoingLinks(predicate,0);
                adjustedTarget.addIncomingLinks(predicate,0);
            } else if (adjustedSource != null && adjustedTarget != null) {//case 4: both source and target have existed
                graph.addEdge(adjustedSource,adjustedTarget, link);
                source.addOutgoingLinks(predicate,0);
                target.addIncomingLinks(predicate,0);
            }
        }

        System.out.println("All of the entities with their types have been determined!");
        System.out.println("There are totally " + graph.vertexSet().size() + " nodes!");
        System.out.println("There are totally " + graph.edgeSet().size() + " edges!");
        return graph;


    }


    /**merge two, three, or more museum RDF files*
     * @from 2 May 2019
     *
     * */
    public static void mergeAll () {


        DirectedWeightedMultigraph<edu.isi.karma.rep.alignment.Node, LabeledLink> initialGraph = parseSAAMRDF(Settings.MUSEUM_CRM_RDF_Address.concat(Settings.MUSEUM_CRM_RDF_S01));
        KG initialKG = new KG(initialGraph);

        List<String> fileList = new ArrayList<>();
//        fileList.add(Settings.MUSEUM_CRM_RDF_S02);
//        fileList.add(Settings.MUSEUM_CRM_RDF_S03);
//        fileList.add(Settings.MUSEUM_CRM_RDF_S04);
//        fileList.add(Settings.MUSEUM_CRM_RDF_S05);
//        fileList.add(Settings.MUSEUM_CRM_RDF_S06);
        fileList.add(Settings.MUSEUM_CRM_RDF_S07);
//        fileList.add(Settings.MUSEUM_CRM_RDF_S08);
//        fileList.add(Settings.MUSEUM_CRM_RDF_S09);
//        fileList.add(Settings.MUSEUM_CRM_RDF_S10);
//        fileList.add(Settings.MUSEUM_CRM_RDF_S11);
//        fileList.add(Settings.MUSEUM_CRM_RDF_S12);
//        fileList.add(Settings.MUSEUM_CRM_RDF_S13);
//        fileList.add(Settings.MUSEUM_CRM_RDF_S14);
//        fileList.add(Settings.MUSEUM_CRM_RDF_S15);
//        fileList.add(Settings.MUSEUM_CRM_RDF_S16);
//        fileList.add(Settings.MUSEUM_CRM_RDF_S17);
//        fileList.add(Settings.MUSEUM_CRM_RDF_S18);
//        fileList.add(Settings.MUSEUM_CRM_RDF_S19);
//        fileList.add(Settings.MUSEUM_CRM_RDF_S20);
//        fileList.add(Settings.MUSEUM_CRM_RDF_S21);
//        fileList.add(Settings.MUSEUM_CRM_RDF_S23);
//        fileList.add(Settings.MUSEUM_CRM_RDF_S24);
//        fileList.add(Settings.MUSEUM_CRM_RDF_S25);
//        fileList.add(Settings.MUSEUM_CRM_RDF_S26);
//        fileList.add(Settings.MUSEUM_CRM_RDF_S27);
//        fileList.add(Settings.MUSEUM_CRM_RDF_S28);
//        fileList.add(Settings.MUSEUM_CRM_RDF_S29);

        for (int i = 0; i < fileList.size(); i++) {

            DirectedWeightedMultigraph<edu.isi.karma.rep.alignment.Node, LabeledLink> graph = parseSAAMRDF(Settings.MUSEUM_CRM_RDF_Address.concat(fileList.get(i)));//get the i rdf file
            KG addedKG = new KG(graph);
            DirectedWeightedMultigraph<Node, LabeledLink> mergedGraph = initialKG.merge(addedKG);
            KG mergedKG = new KG(mergedGraph);
            initialKG = mergedKG;
            System.out.println("# nodes is: " + mergedGraph.vertexSet().size());
            System.out.println("# edges is: " + mergedGraph.edgeSet().size());

        }

        initialKG.init();
        int n = initialKG.getConnectedComponent().size();
        System.out.println();
        System.out.println("there are " + n + " components");
        initialKG.conceptualize();
        System.out.println("conceptualization finished!");
        initialKG.numerateVertexLables();
        initialKG.numerateEdgeLables();
        Util.printNumeratedLabelsToFile(Settings.MUSEUM_KG_ADDRESS.concat("entity_type_map.lg"), initialKG.numeratedVertexLabels);
        Util.printNumeratedLabelsToFile(Settings.MUSEUM_KG_ADDRESS.concat("relationship_type_map.lg"), initialKG.numeratedEdgeLabels);
        System.out.println("numerate edges and nodes done! ");
        initialKG.serialize(Settings.MUSEUM_KG_ADDRESS.concat("kg.lg"));

    }


    public static void vizMuseumKG (String kgUrl, String kgFileName, String kgVizUrl, String kgVizFileName) {

        //build color map
        Map<String,Color> colorMap = new HashMap<>();
        colorMap.put("",Color.black);
        colorMap.put("E48_Place_Name",Color.black);
        colorMap.put("E52_Time-Span",Color.blue);
        colorMap.put("E33_Linguistic_Object",Color.green);
        colorMap.put("E38_Image",Color.yellow);
        colorMap.put("E69_Death",Color.CYAN);
        colorMap.put("E8_Acquisition",Color.GRAY);
        colorMap.put("E74_Group",Color.LIGHT_GRAY);
        colorMap.put("E78_Collection",Color.magenta);
        colorMap.put("E82_Actor_Appellation",Color.red);
        colorMap.put("E35_Title",Color.orange);
        colorMap.put("E55_Type",Color.pink);
        colorMap.put("E34_Inscription",Color.white);
        colorMap.put("E22_Man-Made_Object",Color.darkGray);
        colorMap.put("E44_Place_Appellation",new Color(0,78,50));//dark green
        colorMap.put("E54_Dimension",Color.darkGray);
        colorMap.put("E12_Production",Color.darkGray);
        colorMap.put("E67_Birth",Color.red);
        colorMap.put("E21_Person",Color.black);
        colorMap.put("E39_Actor",Color.darkGray);
        colorMap.put("E30_Right",Color.darkGray);
        colorMap.put("E40_Legal_Body",Color.darkGray);
        colorMap.put("E57_Material",Color.darkGray);
        colorMap.put("E53_Place",Color.darkGray);
        colorMap.put("E31_Document",Color.darkGray);

        DirectedWeightedMultigraph<Node, LabeledLink> bigGraph = RankMuseumDataModel.loadMuseumKG(kgUrl, kgFileName);

        GephiUtilities.convertToGephi(bigGraph, kgVizUrl, kgVizFileName, colorMap);
    }

    /**Test parsing two Museum RDFs, and then merge them together, and next serialize the knowledge graph**/
    public static void test1 () {
        DirectedWeightedMultigraph<edu.isi.karma.rep.alignment.Node, LabeledLink> graph1 = parseSAAMRDF(Settings.MUSEUM_CRM_RDF_Address.concat(Settings.MUSEUM_CRM_RDF_S02));
        DirectedWeightedMultigraph<edu.isi.karma.rep.alignment.Node, LabeledLink> graph2 = parseSAAMRDF(Settings.MUSEUM_CRM_RDF_Address.concat(Settings.MUSEUM_CRM_RDF_S03));
        KG kg1 = new KG(graph1);
        KG kg2 = new KG(graph2);
        DirectedWeightedMultigraph<Node, LabeledLink> merged = kg1.merge(kg2);
        System.out.println("# nodes is: " + merged.vertexSet().size());
        System.out.println("# edges is: " + merged.edgeSet().size());
        KG kg = new KG(merged);
        kg.init();
        int n = kg.getConnectedComponent().size();
        System.out.println();
        System.out.println("there are " + n + " components");
        kg.conceptualize();
        System.out.println("conceptualization finished!");
        kg.numerateVertexLables();
        kg.numerateEdgeLables();
        Util.printNumeratedLabelsToFile(Settings.MUSEUM_KG_ADDRESS.concat("entity_type_map.lg"), kg.numeratedVertexLabels);
        Util.printNumeratedLabelsToFile(Settings.MUSEUM_KG_ADDRESS.concat("relationship_type_map.lg"), kg.numeratedEdgeLabels);
        System.out.println("numerate edges and nodes done! ");
        kg.serialize(Settings.MUSEUM_KG_ADDRESS.concat("kg.lg"));
    }

    /**validate a rough Museum graph is connected or not*
     *
     * */
    //connected or not
    public static void test2 () {

        DirectedWeightedMultigraph<edu.isi.karma.rep.alignment.Node, LabeledLink> graph =
                parseSAAMRDF2("/Users/fengz/Documents/Data_Multiple_Schema_Matching/Karma-data-set/RDF-Graph@5May19/museum-crm/rdf-modified/s18-s-indianapolis-artists.nt");
        KG kg = new KG(graph);
        kg.init();
        List<DirectedWeightedMultigraph<Node, LabeledLink>> component = kg.getConnectedComponent();
        int n = component.size();
        System.out.println();
        System.out.println("there are " + n + " components");
        kg.conceptualize();
        System.out.println("conceptualization finished!");
        kg.numerateVertexLables();
        kg.numerateEdgeLables();
        Util.printNumeratedLabelsToFile(Settings.MUSEUM_KG_ADDRESS.concat("entity_type_map.lg"), kg.numeratedVertexLabels);
        Util.printNumeratedLabelsToFile(Settings.MUSEUM_KG_ADDRESS.concat("relationship_type_map.lg"), kg.numeratedEdgeLabels);
        System.out.println("numerate edges and nodes done! ");
        kg.serialize(Settings.MUSEUM_KG_ADDRESS.concat("kg.lg"));
        String kgUrl = Settings.SerializedMusemumKGFileAddress;
        String kgFileName = "kg.lg";
        String kgVizUrl = Settings.SerializedMusemumKGGephiFileAddress;
        String kgVizFileName = "kg20190507_M_932N.gexf";
        vizMuseumKG(kgUrl,kgFileName,kgVizUrl,kgVizFileName);
    }


    /**compute the how many connected sub-structures that a rough Museum RDF file contains *
     * and then filter out all the orphan nodes.
     * Might be useless because we do not use rough Museum rdf any more. 4 June 2019.
     * */
    public static void test3() {
        DirectedWeightedMultigraph<edu.isi.karma.rep.alignment.Node, LabeledLink> graph = parseSAAMRDF(Settings.MUSEUM_CRM_RDF_Address.concat(Settings.MUSEUM_CRM_RDF_S22));
        KG kg = new KG(graph);
        System.out.println("Now initialize the Adjacent list of KG! ");
        kg.init();
        List<DirectedWeightedMultigraph<edu.isi.karma.rep.alignment.Node,LabeledLink>> components = kg.getConnectedComponent();
        System.out.println();
        System.out.println("# of connected components: " + components.size());

        for (DirectedWeightedMultigraph<edu.isi.karma.rep.alignment.Node,LabeledLink> component : components) {

            System.out.println("this component contains: ");
            System.out.println("# of nodes: " + component.vertexSet().size());
            System.out.println("# of edges: " + component.edgeSet().size());
            if (component.vertexSet().size() == 1) {
                Set<edu.isi.karma.rep.alignment.Node> nodes = component.vertexSet();
                for (edu.isi.karma.rep.alignment.Node node : nodes) {
                    System.out.println("node uri is: " + node.getUri());
                }

            }
        }
    }

    public static void main (String args[]) {

        test2();
    }


}
