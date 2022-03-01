package au.com.d2dcrc.yago2es;

import org.apache.commons.collections4.CollectionUtils;
import java.util.*;
import java.util.Map;

/**Common graph structure**/
public class Graph {

    private List<Edge> allEdges;
    private Map<String,Vertex> allVertex;//<id, vertex>
    private Map<Integer,Vertex> allIndexedVertex; //<index, vertex>, used in load graph from parsemis
    boolean isDirected = false;
    private Map<String, Integer> numeratedVertexLabels = new HashMap<String, Integer>();
    private Map<String, Integer> numeratedEdgeLabels = new HashMap<String, Integer>();
    private Map<String, Integer> vertexIdWithIndexMap = new HashMap<String, Integer>();//map the unique id to the index of vertices
    private Vertex leftAnchorPoint;//newly added
    private Vertex rightAnchorPoint;//newly added

    /**@param  isDirected the graph is directed or not
     *
     * **/
    public Graph(boolean isDirected){
        allEdges = new ArrayList<Edge>();
        allVertex = new HashMap<String,Vertex>();
        this.isDirected = isDirected;
        allIndexedVertex = new HashMap<Integer, Vertex>();
    }

    /**
     * @param allEdges all the edges
     * @param allVertexMap all the vertices
     * @param isDirected is directed or not
     * **/
    public Graph(List<Edge> allEdges, Map<String, Vertex> allVertexMap, boolean isDirected){

        this.allEdges = allEdges;
        this.allVertex = allVertexMap;
        this.isDirected = isDirected;
        allIndexedVertex = new HashMap<Integer, Vertex>();
    }

    //This works only for directed graph because for undirected graph we can end up
    //adding edges two times to allEdges
    /**
     * @param vertex the vertex to be added into the graph
     * **/
    public void addVertex(Vertex vertex){
        if(allVertex.containsKey(vertex.getId())){
            return;
        }
        allVertex.put(vertex.getId(), vertex);
//        for(Edge edge : vertex.getEdges()){
//            allEdges.add(edge);
//        }
    }

    /**used in building graph from the output of PARSEMIS**/
    /**
     * @param vertex add the vertex into the  graph
     * **/
    public void addIndexedVertex(Vertex vertex){
        if(allIndexedVertex.containsKey(vertex.getIndex())){
            return;
        }
        allIndexedVertex.put(vertex.getIndex(),vertex);
    }

    /**used in building graph from the output of PARSEMIS**/
    /**
     * @return return all the indexed vertices
     * **/
    public Map<Integer, Vertex> getAllIndexedVertex(){return allIndexedVertex;}

    //unused
    /**
     * @param id id of the vertex
     * @return the added vertex
     * **/
    public Vertex addSingleVertex(String id){
        if(allVertex.containsKey(id)){
            return allVertex.get(id);
        }
        Vertex v = new Vertex(id);
        allVertex.put(id, v);
        return v;
    }

    /**
     * @param id id of the vertex
     * @return vertex
     * **/
    public Vertex getVertex(String id){
        return allVertex.get(id);
    }

    /**get the edge via start id and end id, newly added**/
    /**
     * @param startID id of the start
     * @param endID id of end
     * @return the obtained edge
     * **/
    public Edge getEdge (String startID, String endID){

        for(Edge edge : allEdges){

            if((edge.getStartPoint().getId().equals(startID)) &&
                    (edge.getEndPoint().getId().equals(endID))){

                return edge;
            }
        }

        return null;
    }

    /**Get the edge via start id and end id, newly added for Yago**/
    /**
     * @param startID id of the start
     * @param endID id of end
     * @return the obtained edges
     * **/
    public List<Edge> getEdges (String startID, String endID) {

        List<Edge> edges = new ArrayList<>();

        for(Edge edge : allEdges){
            if((edge.getStartPoint().getId().equals(startID)) &&
                    (edge.getEndPoint().getId().equals(endID))){

                edges.add(edge);
            }


        }
        return edges;
    }

    /**Get the edge labels via start id and end id, newly added for Yago, newly added for Yago*
     *     /**
     * @param startID id of the start
     * @param endID id of end
     * @return all the edge labels on this edge
     **/
    public List<String> getEdgeLabels (String startID, String endID) {
        List<Edge> edges = this.getEdges(startID,endID);
        List<String> edgeLabels = new ArrayList<>();
        for (Edge edge : edges) {

            String edgeLabel = edge.getLabel();
            edgeLabels.add(edgeLabel);

        }
        return edgeLabels;
    }


    /**Add an edge into graph, modified on 23 July 2017**/
    /**
     *
     * @param edge to be added
     */
    public void addEdge(Edge edge){

        allEdges.add(edge);
        Vertex start = edge.getStartPoint();
        Vertex end = edge.getEndPoint();

        //start.addAdjacentVertex(edge, end);
        //end.addPreAdjacentVertex(edge,start);

    }

    /**
     * @param id1 id of the start
     * @param id2 id of the end
     * **/
    public void addEdge(String id1,String id2){
        Vertex vertex1 = null;
        if(allVertex.containsKey(id1)){
            vertex1 = allVertex.get(id1);
        }else{
            vertex1 = new Vertex(id1);
            allVertex.put(id1, vertex1);
        }
        Vertex vertex2 = null;
        if(allVertex.containsKey(id2)){
            vertex2 = allVertex.get(id2);
        }else{
            vertex2 = new Vertex(id2);
            allVertex.put(id2, vertex2);
        }

        Edge edge = new Edge(vertex1,vertex2,isDirected);
        allEdges.add(edge);
//        vertex1.addAdjacentVertex(edge, vertex2);
//        if(!isDirected){
//            vertex2.addAdjacentVertex(edge, vertex1);
//        }

    }

    /**add an directed edge into the linked entity graph with a specified label*
     *
     * @param id1 id of the start point of the edge
     * @param id2 id of the end point of the edge
     * @param id edge id
     * @param label label of added edge
     * @param isDirected directed or not
     */
    public void addEdge(String id1,String id2, String id, String label, boolean isDirected){
        Vertex vertex1 = null;
        if(allVertex.containsKey(id1)){
            vertex1 = allVertex.get(id1);
        }else{
            vertex1 = new Vertex(id1);
            allVertex.put(id1, vertex1);
        }
        Vertex vertex2 = null;
        if(allVertex.containsKey(id2)){
            vertex2 = allVertex.get(id2);
        }else{
            vertex2 = new Vertex(id2);
            allVertex.put(id2, vertex2);
        }

        Edge edge = new Edge(vertex1,vertex2,isDirected,label,id);

        allEdges.add(edge);
        vertex1.addAdjacentVertex(edge, vertex2);
        vertex2.addPreAdjacentVertex(edge,vertex1);//this row is newly added

        if(!isDirected){
            vertex2.addAdjacentVertex(edge, vertex1);
        }

    }

    /**Prepend a new vertex into an existing vertex in the graph. The added edge should be from 'v_p' to 'vertex'*
     * @param vertex the exist vertex in the graph
     * @param v_p the added vertex
     * @param edgeLabel the label of edge
     * */
    public void prependVertex(Vertex vertex, Vertex v_p, String edgeLabel) {

        /**Add 'v_p' to the graph**/
        this.addVertex(v_p);

        /**Create a new edge from 'v_p' to 'vertex'**/
        Edge edge = new Edge(v_p, vertex, true, edgeLabel, RandomStringUUID.createUUID());
        this.addEdge(edge);
        v_p.addAdjacentVertex(edge, vertex);
        vertex.addPreAdjacentVertex(edge, v_p);
    }

    /**Append a new vertex into an existing vertex in the graph. The added edge should be from 'vertex' to 'v_s'*
     * @param vertex the existed vertex in the graph
     * @param v_s the added vertex,
     * @param edgeLabel the label of the added edge
     * */
    public void appendVertex(Vertex vertex, Vertex v_s, String edgeLabel) {

        /**Add 'v_s' to the graph**/
        this.addVertex(v_s);

        /**Create a new edge from 'vertex' to 'v_s'**/
        Edge edge = new Edge(vertex, v_s, true, edgeLabel, RandomStringUUID.createUUID());
        this.addEdge(edge);
        vertex.addAdjacentVertex(edge, v_s);
        v_s.addPreAdjacentVertex(edge, vertex);
    }



    /**Remove the directed edge from the linked data graph
     * @param id1 id of the start vertex
     * @param id2 id2 id of the end vertex
     * **/
    public void removeEdge(String id1, String id2){

        Iterator<Edge> it = allEdges.iterator();
        while (it.hasNext()){

            Edge edge = it.next();
            /**get the start & end vertex of the edge**/
            Vertex start  = edge.getStartPoint();
            Vertex end = edge.getEndPoint();

            if(start.getId().equals(id1)  &&  end.getId().equals(id2)){

                start.removeAdjacentVertex(edge,end);
                end.removePreAdjacentVertex(edge,start);
                it.remove();

            }

        }


    }

    /**
     * @return all the edge of this graph
     * **/
    public List<Edge> getAllEdges(){
        return allEdges;
    }

    /**
     * @return all the vertices of this graph
     * **/
    public Collection<Vertex> getAllVertex(){
        return allVertex.values();
    }


    /**Get all the edges of the graph, used in subgraph matching**/
    /**
     *
     * @return A list containing all of vertices
     */
    public List<Vertex> getAllVertices(){

        List<Vertex> allVertices = new ArrayList<Vertex>();

        Collection<Vertex> vertexCollection = getAllVertex();
        Iterator<Vertex> it = vertexCollection.iterator();
        while (it.hasNext()){

            Vertex vertex = it.next();

            allVertices.add(vertex);
        }


        return allVertices;
    }

    /**Added on 19 Nov 2017. Unused.*
     * @param allVertices set all the vertices
     * */
    public void setAllVertices(List<Vertex> allVertices) {

        allVertex.clear();

        for (Vertex vertex : allVertices) {

            //get the label of the vertex
            int index = vertex.getIndex();
            allVertex.put(Integer.toString(index), vertex);

        }


    }

    /**
     * @param id id the vertex
     * @param label label to be set
     * **/
    public void setLabelForVertex(long id, String label){
        if(allVertex.containsKey(id)){
            Vertex vertex = allVertex.get(id);
            vertex.setData(label);
        }
    }


    /**Constructor for creating a new directed linked data graph based on Karma Ouput*
     * @param triples  RDF Triples
     * @param g initialized directed graph
     * @return Linked entity graph
     * */
    public Graph createLinkedGraph(List<Triple> triples, Graph g){

        /**Loop the triples**/
        Iterator<Triple> it = triples.iterator();

        while (it.hasNext()){

            /**Obtain one triple **/
            Triple triple = it.next();

            /**Obtain subject, predicate and object of a triple**/
            Subject subject = triple.getSubject();
            Predicate predicate = triple.getPredicate();
            ObjectOfPredicate objectOfPredicate = triple.getObjectOfPredicate();

            /**define id of entity instance that has already existed in the linked data graph, which represent the same entities instance as
             * 'subject' or 'object'**/
            String start_point = "";
            String end_point = "";



            /**Traverse all the vertices in the current linked data graph for 'subject'**/
            Collection<Vertex> vertexCollection = g.getAllVertex();
            Iterator its = vertexCollection.iterator();
            while (its.hasNext()){

                /**Get a vertex in 'vertices'**/
                Vertex vertex = (Vertex) its.next();

                /**Get the attribute & attribute value list of the vertex**/
                Map<String, String> attributes = vertex.getAttributes();

                /**Check if the entity instance that 'subject' represents has already existed in the current linked data graph or not **/
                if(isEntityInstEquals(vertex, subject)){
                    /**record id of the vertex**/
                    start_point = vertex.getId();

                }
            }

            /**if 'object' represents an entity instance rather than an attribute value**/
            if(objectOfPredicate.getIsEntity()){

                /**Traverse all the vertices in the current linked data graph for 'object'**/
                Iterator iterator = vertexCollection.iterator();
                while (iterator.hasNext()){

                    /**Get a vertex in 'vertices'**/
                    Vertex vertex = (Vertex) iterator.next();

                    /**Check if the entity instance that 'subject' represents has already existed in the current linked data graph or not **/
                    if(isEntityInstEquals(vertex, objectOfPredicate)){

                        /**record id of the vertex**/
                        end_point = vertex.getId();

                    }

                }

                /**Case I: Both start point and end point do exist in the linked data graph**/
                if((start_point.length()!=0) && (end_point.length()!=0)){

                    /**Insert an edge from 'start_point' to 'end_point'**/
                    g.addEdge(start_point,end_point, RandomStringUUID.createUUID(), predicate.getPredicate(),true);

                }

                /**Case II: Start point exists in the linked data graph. However, end point does not exist in the linked data graph**/
                else if((start_point.length()!=0) && (end_point.length()==0)){

                    /**Create a new vertex of linked data graph**/
                    Vertex vertex = new Vertex(RandomStringUUID.createUUID());

                    /**Get the label & identification code from 'object'**/
                    String label = objectOfPredicate.getName();
                    List<String> identificationCode = objectOfPredicate.getCodes();

                    /**Put the name & identification code to the newly created vertex**/
                    vertex.setData(label);
                    vertex.setIdentificationCode(identificationCode);

                    /**whether the vertex is object link one or not**/
                    if(label.startsWith("OL_")){vertex.setIsObjectLink(true);}

                    /**Add this new vertex into graph**/
                    g.addVertex(vertex);

                    /**Insert an edge from 'start_point' to 'end_point'**/
                    g.addEdge(start_point,vertex.getId(),RandomStringUUID.createUUID(),predicate.getPredicate(),true);

                }

                /**Case III: Start point does not exist in the linked data graph. However, end point exists in the linked data graph**/
                else if((start_point.length()==0) && (end_point.length()!=0)){

                    /**Create a new vertex of linked data graph**/
                    Vertex vertex = new Vertex(RandomStringUUID.createUUID());

                    /**Get the label & identification code from 'subject'**/
                    String label = subject.getName();
                    List<String> identificationCode = subject.getCodes();

                    /**Put the name & identification code to the newly created vertex**/
                    vertex.setData(label);
                    vertex.setIdentificationCode(identificationCode);

                    /**whether the vertex is object link one or not**/
                    if(label.startsWith("OL_")){vertex.setIsObjectLink(true);}

                    /**Add this new vertex into graph**/
                    g.addVertex(vertex);

                    /**Insert an edge from 'start_point' to 'end_point'**/
                    g.addEdge(vertex.getId(), end_point, RandomStringUUID.createUUID(),predicate.getPredicate(),true);
                }

                /**Case IV: Neither start point nor end point exist in the linked data graph**/
                else if ((start_point.length()==0) && (end_point.length()==0)){

                    /**Create a new vertex of linked data graph**/
                    Vertex vertex1 = new Vertex(RandomStringUUID.createUUID());

                    /**Get the label & identification code from 'subject'**/
                    String label1 = subject.getName();
                    List<String> identificationCode1 = subject.getCodes();

                    /**Put the name & identification code to the newly created vertex**/
                    vertex1.setData(label1);
                    vertex1.setIdentificationCode(identificationCode1);

                    /**whether the vertex is object link one or not**/
                    if(label1.startsWith("OL_")){vertex1.setIsObjectLink(true);}

                    /**Create a new vertex of linked data graph**/
                    Vertex vertex2 = new Vertex(RandomStringUUID.createUUID());

                    /**Get the label & identification code from 'subject'**/
                    String label2 = objectOfPredicate.getName();
                    List<String> identificationCode2 = objectOfPredicate.getCodes();

                    /**Put the name & identification code to the newly created vertex**/
                    vertex2.setData(label2);
                    vertex2.setIdentificationCode(identificationCode2);

                    /**whether the vertex is object link one or not**/
                    if(label2.startsWith("OL_")){vertex2.setIsObjectLink(true);}

                    /**Add new vertices into graph**/
                    g.addVertex(vertex1);
                    g.addVertex(vertex2);

                    /**Insert an edge from 'start_point' to 'end_point'**/
                    g.addEdge(vertex1.getId(),vertex2.getId(),RandomStringUUID.createUUID(),predicate.getPredicate(),true);
                }

            }
            /**if 'object' represents attribute values rather than an entity instance**/
            else if (!objectOfPredicate.getIsEntity()){

                /**initialize attribute name and attribute value**/
                String attributeName = "";
                String attributeValue = "";

                 /* Firstly, get the attribute value of 'object'*/
                attributeValue = objectOfPredicate.getValue();

                /**Next, get the value predicate as attribute name**/
                attributeName = predicate.getPredicate();

                Vertex vertex;

                /**Case I: start point exists in the linked data graph**/
                if(start_point.length()!=0){

                    /**Get the vertex from the linked data graph in terms of id**/
                    vertex = g.getVertex(start_point);
                }

                /**Case II: start point does not exist in the linked data graph**/
                else {

                    /**Create an new orphan vertex**/
                    vertex = new Vertex(RandomStringUUID.createUUID());
                    g.addVertex(vertex);

                    /**put the label & identification codes of 'subject' to this newly created vertex**/
                    vertex.setData(subject.getName());
                    vertex.setIdentificationCode(subject.getCodes());

                    /**whether the vertex is object link one or not**/
                    if(vertex.getLabel().startsWith("OL_")){vertex.setIsObjectLink(true);}

                }

                /**insert attribute name and value into the existed 'vertex'**/
                vertex.getAttributes().put(attributeName, attributeValue);

            }

        }

        return g;

    }

    /**Create Yago Graph, which is developed on 26 June 2017 for evaluating the prototype of relationship matching*
     * @param yagoFacts yago facts in the form of RDF
     * @param yagoRelationshipHashMap this hashmap defines the types of subject instance and object instances
     * @return yago linked entity graph
     * */
    public static Graph createYagoGraph (List<YagoFact> yagoFacts, Map<String, YagoRelationship> yagoRelationshipHashMap) {

        Long num = 0L;

        Graph graph = new Graph(true);

        /**initialize the graph**/
        Vertex initialVertex = new Vertex(RandomStringUUID.createUUID());
        initialVertex.setInstanceName("Onchiam");
        graph.addVertex(initialVertex);

        /**Loop all the yago facts**/
        Iterator<YagoFact> it = yagoFacts.iterator();

        while (it.hasNext()){

            System.out.println("Now we are dealing with the NO." + num++ +" Yago fact");

            /**Obtain one yago fact **/
            YagoFact yagoFact = it.next();

            /**Obtain subject, predicate and object of a Yago fact**/
            String subject = yagoFact.getSubject();
            String predicate = yagoFact.getPredicate();
            String object = yagoFact.getObject();

            /**Here, we need to determine the type (class) of the subject entity and the object entity**/
            YagoRelationship yagoRelationship = yagoRelationshipHashMap.get(predicate);
            String domain = yagoRelationship.getDomain();
            String range = yagoRelationship.getRange();


            /** 0 means we have not found 'subject' and 'object' in the current graph; 1 means we have already found either 'subject' or 'object' in the current graph**/
            int isSubjectExisted = 0;
            int isObjectExisted = 0;

            Vertex start = new Vertex ("temp");//temporary vertex
            Vertex end = new Vertex ("temp");////temporary vertex

            /**Traverse all the vertices in the current Yago graph**/
            Collection<Vertex> vertexCollection = graph.getAllVertex();
            Collection<Vertex> copiedVertexCollection = new ArrayList<Vertex>(vertexCollection);
            Iterator its = copiedVertexCollection.iterator();
            while (its.hasNext()){

                /**Get a vertex in 'vertices'**/
                Vertex vertex = (Vertex) its.next();

                /**Get the instance name of each vertex**/
                String instanceName = vertex.getInstanceName();

                /*Check if the entity instance that 'subject' represents has already existed in the current linked data graph or not*/
                if(instanceName.equals(subject)){

                    isSubjectExisted = 1;

                    start = vertex;

                    continue;

                }

                /**Case II: the instance name of this vertex is equal to 'object'*
                 *
                 * */
                else if (instanceName.equals(object)) {

                    isObjectExisted = 1;

                    end = vertex;

                    continue;
                }
            }

            /**Case I: both 'subject' and 'object' exist in the current Yago graph**/
            if ((isSubjectExisted == 1) && (isObjectExisted == 1)) {

                /**Add the candidate label to the start vertex if this start vertex has not the candidate label (of a special relation) yet**/
                if (!start.getCandidateLabels().contains(domain)) {

                    start.addCandidateLabels(domain);
                }

                /**Add the candidate label to the end vertex if this end vertex has not the candidate label of (a special relation) yet**/
                if (!end.getCandidateLabels().contains(range)) {

                    end.addCandidateLabels(range);
                }


                /**Insert an edge from 'start' to 'end'**/
                graph.addEdge(start.getId(), end.getId(), RandomStringUUID.createUUID(), predicate, true);
            }

            /**Case II: 'subject' exists in the current Yago graph while 'object' does not exist**/
            else if ((isSubjectExisted == 1) && (isObjectExisted == 0)) {

                /**Create a new vertex of linked data graph representing 'object'**/
                Vertex v = new Vertex(RandomStringUUID.createUUID());

                /**Put 'object' into the newly created vertex as instance name**/
                v.setInstanceName(object);

                /**Add the candidate label to the newly created ending vertex**/
                v.addCandidateLabels(range);

                /**Add this new vertex into graph**/
                graph.addVertex(v);

                /**Add the candidate label to the start vertex if this start vertex has not the candidate label (of a special relation) yet**/
                if (!start.getCandidateLabels().contains(domain)) {

                    start.addCandidateLabels(domain);
                }

                /**Insert an edge from 'subject' to 'object'**/
                graph.addEdge(start.getId(), v.getId(), RandomStringUUID.createUUID(), predicate, true);

            }

            /**Case III: 'subject' does exist in the current Yago graph while 'object' exists**/
            else if ((isSubjectExisted == 0) && (isObjectExisted == 1)) {

                /**Create a new vertex of linked data graph representing 'subject'**/
                Vertex v = new Vertex(RandomStringUUID.createUUID());

                /**Put 'subject' into newly created vertex as instance name**/
                v.setInstanceName(subject);

                /**Add the candidate label into the newly created staring vertex**/
                v.addCandidateLabels(domain);

                /**Add this new vertex into graph**/
                graph.addVertex(v);

                /**Add the candidate label to the end vertex if this end vertex has not the candidate label of (a special relation) yet**/
                if (!end.getCandidateLabels().contains(range)) {

                    end.addCandidateLabels(range);
                }

                /**Insert an edge from 'subject' to 'object'**/
                graph.addEdge(v.getId(), end.getId(), RandomStringUUID.createUUID(), predicate, true);


            }


            /**Case IV: neither 'subject' nor 'object' exists in the current Yago graph**/
            else if ((isSubjectExisted == 0) && (isObjectExisted == 0)) {

                /**Create a new vertex of linked data graph representing 'subject' and 'object'**/
                Vertex startVertex = new Vertex(RandomStringUUID.createUUID());
                Vertex endVertex = new Vertex(RandomStringUUID.createUUID());

                /**Put 'subject' and 'object' into newly created vertices as instance name**/
                startVertex.setInstanceName(subject);
                endVertex.setInstanceName(object);

                /**Add the candidate label into the staring vertex and ending vertex**/
                startVertex.addCandidateLabels(domain);
                endVertex.addCandidateLabels(range);

                /**Add these new vertices into graph**/
                graph.addVertex(startVertex);
                graph.addVertex(endVertex);

                /**Insert an edge from 'subject' to 'object'**/
                graph.addEdge(startVertex.getId(), endVertex.getId(), RandomStringUUID.createUUID(), predicate, true);
            }

        }

        return graph;

    }


    /**verify if a subject of triple and an existed vertex represent the same entity instance*
     * Criterion: 1. the subject and the vertex must have the same name(label); 2. The subject and the vertex must have the same identification code.
     * Exception: object link entity instances must always be unique
     * @param vertex a vertex in the current linked data graph
     * @param subject subject of a triple from Karma
     * @return true if 'subject' represents the same entity as the 'vertex' does
     * */
    public boolean isEntityInstEquals(Vertex vertex, Subject subject){

        boolean isEqual;

        /**Get the label of vertex**/
        String label = vertex.getLabel();

        /**Get the name of subject**/
        String name = subject.getName();

        /**Get the identification code of the vertex**/
        List<String> identificationCode = vertex.getIdentificationCode();

        /**Get the code of subject**/
        List<String> codes = subject.getCodes();

        /**Get the common list of 'identificationCode' & 'codes'**/
        ArrayList<String> commonList = new ArrayList<String> (CollectionUtils.retainAll(identificationCode,codes));

        if((commonList.size()==identificationCode.size())&&(commonList.size()==codes.size())&&(label.equals(name))){

            isEqual = true;
        }else {

            isEqual = false;
        }


        return isEqual;
    }

    /**Verify if an object from a triple and an existed vertex represent the same entity instance or not*
     * Precondition: 'objectOfPredicate' must be an entity rather than an attribute value
     * @param vertex a vertex in the current linked data graph
     * @param objectOfPredicate  object of a triple from Karma
     * @return true if 'object' represents the same entity as the 'vertex' does
     * */
    public boolean isEntityInstEquals(Vertex vertex, ObjectOfPredicate objectOfPredicate){

        boolean isEqual = false;

        /**Get the label of vertex**/
        String label = vertex.getLabel();

        /**Get the name of object of triple**/
        String name = objectOfPredicate.getName();

        /**Get the identification code of the vertex**/
        List<String> identificationCode = vertex.getIdentificationCode();

        /**Get the code of object**/
        List<String> codes = objectOfPredicate.getCodes();

        /**Get the common list of 'identificationCode' & 'codes'**/
        ArrayList<String> commonList = new ArrayList<String> (CollectionUtils.retainAll(identificationCode,codes));

        if((commonList.size()==identificationCode.size())&&(commonList.size()==codes.size())&&(label.equals(name))){

            isEqual = true;
        }else {

            isEqual = false;
        }


        return isEqual;
    }

    /**Depth first search*
     * @param root node of the grpah. DFS starts with this root node. Generally, root node is the auxiliary node of the graph
     * */
    public void dfs(Vertex root){

        //DFS uses stack data structure
        Stack<Vertex> s = new Stack<Vertex>();
        s.push(root);

        /**set this vertex visited**/
        root.setVisited(true);

        /**print this vertex**/
        root.printNode();

        while (!s.isEmpty()){

            Vertex v = (Vertex) s.peek();

            /**Get all of the unvisited child node of 'v'**/
            List<Vertex> children = v.getUnvisitedChildVertex();
            if(children.size() > 0){

                /**set all of the vertices 'visited'**/
                Iterator<Vertex> its = children.iterator();
                Vertex child = its.next();

                child.setVisited(true);
                child.printNode();
                s.push(child);
            }else {

                s.pop();
            }
        }


    }

    /**Depth first search with a certain depth, which is used in slicing linked data graph
     * @param root the start point of search
     * @param maxDepth the maximum depth of search
     * @return the search scope by 'maxDepth'
     * */
    public List<Vertex> dfs(Vertex root, int maxDepth){

        /**create a list holding all of vertices **/
        List<Vertex> vertices = new ArrayList<Vertex>();

        //DFS uses stack data structure
        Stack<Vertex> s = new Stack<Vertex>();
        s.push(root);

        /**set this vertex visited**/
        root.setVisited(true);

        /**print this vertex**/
        root.printNode();

        vertices.add(root);

        while (!s.isEmpty()){

            Vertex v = (Vertex) s.peek();

            /**Get all of the unvisited child node of 'v' within the maximum depth**/
            List<Vertex> children = v.getUnvisitedChildVertex();
            if((children.size() > 0) && (s.size() < (maxDepth+1))){

                Iterator<Vertex> its = children.iterator();
                /**Get a unvisited vertex randomly**/
                Vertex child = its.next();

                child.setVisited(true);
                child.printNode();
                s.push(child);
                vertices.add(child);
            }else {

                s.pop();
            }
        }



        return vertices;

    }

    /**Remove auxiliary vertex from the directed linked data graph. The value for the 'relationType' will be transferrd to the newly added edge*
     *
     * @param label label of auxiliary vertex to be removed
     * @param relationType attribute name representing relation type of auxiliary vertex. This attribute name will be the label of the newly added edge
     *@return the new edge after removing the object linking vertex
     */
    public List<String> removeAuxiliaryVertex(String label, String relationType){

        List<String> addedEdgeInfo = new ArrayList<String>();

        /**Get the auxiliary vertex with a specified label**/
        Iterator it = allVertex.entrySet().iterator();
        while (it.hasNext()){

            Map.Entry pair = (Map.Entry)it.next();

            Vertex vertex = (Vertex) pair.getValue();
            if (vertex.getLabel().equals(label)){

                /**get the id of pre-vertex & post-vertex of the auxiliary vertex**/
                String v_p_id = vertex.getPreAdjacentVertex().get(0).getId();
                String v_s_id = vertex.getAdjacentVertexes().get(0).getId();

                String removed_id = vertex.getId();

                /***Get the relation type of the auxiliary vertex, and put it as the label of the newly added edge**/
                Map<String, String> attributes = vertex.getAttributes();
                String relationTypeValue = attributes.get(relationType);

                /**remove the edge from 'v_p' to 'removed', the edge from 'removed' to 'v_s'**/
                removeEdge(v_p_id,removed_id);
                removeEdge(removed_id,v_s_id);

                /**remove the auxiliary vertex**/
                it.remove();

                /***Create a new edge between 'v_p' and 'v_s'**/
                addEdge(v_p_id,v_s_id,RandomStringUUID.createUUID(),relationTypeValue,true);

                /**add the new edge info into the list**/
                addedEdgeInfo.add(getVertex(v_p_id).getLabel());
                addedEdgeInfo.add(getVertex(v_s_id).getLabel());
                addedEdgeInfo.add(relationTypeValue);
            }

        }

        return addedEdgeInfo;
    }


    /**Get the Vertex-induced subgraph *
     * @param vertexList a list of vertices
     * @return induced  graph
     * */
    public Graph vertexInducedSubgraph(List<Vertex> vertexList){

        /**Initialize the graph**/
        Graph graph = new Graph(isDirected);




        /**Add 'vertexList' into 'graph'**/
        Iterator<Vertex> iterator = vertexList.iterator();
        while (iterator.hasNext()){

            Vertex vertex = iterator.next();

            graph.addVertex(vertex);


        }



        /**Clone a new vertices list**/
        List<Vertex> verticesCopies = new ArrayList<Vertex>(vertexList);

        /**Traverse every vertex in the list**/
        Iterator<Vertex> it = vertexList.iterator();
        while (it.hasNext()){

            Vertex vertex = it.next();

            /**Traverse every vertex in the list again**/
            Iterator<Vertex> its = verticesCopies.iterator();
            while (its.hasNext()){

                Vertex vertexOfG = its.next();

                if(!(vertexOfG.getId().equals(vertex.getId()))){

                    /**check if there exists an edge between 'vertex' and 'vertexOfG'**/
                    Iterator<Edge> iter = allEdges.iterator();
                    while (iter.hasNext()){

                        Edge edge = iter.next();

                        if(((edge.getStartPoint().getId().equals(vertex.getId()))  &&  (edge.getEndPoint().getId().equals(vertexOfG.getId()))) ||
                                ( (edge.getStartPoint().getId().equals(vertexOfG.getId())) && (edge.getEndPoint().getId().equals(vertex.getId())) )){

                            /**check if this edge has been visited**/
                            if(!edge.getVisited()){

                                /**add this edge into the newly created graph**/
                                graph.addEdge(edge.getStartPoint().getId(),edge.getEndPoint().getId(),edge.getId(),edge.getLabel(),
                                        edge.isDirected());
                                edge.setVisited(true);
                            }

                        }

                    }

                }

            }

        }

        return graph;

    }

    /**set all the vertices in the graph unvisited**/
    public void setAllVertexUnvisited(){

        Iterator<Map.Entry<String, Vertex>> entries = allVertex.entrySet().iterator();
        while (entries.hasNext()){

            Map.Entry<String, Vertex> entry = entries.next();
            Vertex vertex = entry.getValue();
            if(vertex.getVisited()==true){

                vertex.setVisited(false);

            }
        }

    }

    /**set all the edges in the graph unvisited**/
    public void setAllEdgesUnvisited(){

        Iterator<Edge> entries = allEdges.iterator();
        while (entries.hasNext()){

            Edge edge = entries.next();
            if(edge.getVisited()==true){

                edge.setVisited(false);
            }

        }

    }

    /**Numerate the vertex labels of the graph*
     * @return  A map.
     * Key is the non-duplicated labels for vertices of the graph
     * Value is assigned number for this label from 0,1,2,...*/
    public Map<String, Integer> numerateVertexLables(){

        /**clear the numerated label list**/
        numeratedVertexLabels.clear();

        /**create a new list to contain all of label of vertices that is not object linking**/
        List<String> functionalVertexLabel = new ArrayList<String>();
        Collection<Vertex> allVertices = getAllVertex();
        Iterator<Vertex> iterator = allVertices.iterator();
        while (iterator.hasNext()){

            Vertex vertex = iterator.next();
            if(!vertex.getIsObjectLink()){

                functionalVertexLabel.add(vertex.getLabel());

            }

        }

        /**Remove the repeated elements from 'functionalVertexLabel'**/
        Set<String> functionalVertexLabelSet = new HashSet<String>();
        functionalVertexLabelSet.addAll(functionalVertexLabel);

        /**Numerate the labels in the 'functionalVertexLabel'**/
        int vertexNumber = 0; //number for the labels of vertices
        Iterator<String> it = functionalVertexLabelSet.iterator();
        while (it.hasNext()){

            String vertexLabel = it.next();
            Integer vertexIntegerNumber = new Integer(vertexNumber);

            /**Put 'vertexLabel' and 'vertexIntegerNumber' into 'numeratedLabels'**/
            numeratedVertexLabels.put(vertexLabel,vertexIntegerNumber);
            vertexNumber++;
        }



        return numeratedVertexLabels;
    }




    /**Numerate the edge labels of the graph*
     *
     * @return A map. Key: the non-repeated labels for edges of the graph, Value: assigned number for this label from 0,1,2,...
     */
    public Map<String, Integer> numerateEdgeLables(){

        /**clear the numerated edge label list**/
        numeratedEdgeLabels.clear();

        /**create a new list to contain all of label of edges**/
        List<String> edgeLabel = new ArrayList<String>();
        Collection<Edge> allEdges = getAllEdges();
        Iterator<Edge> iterator = allEdges.iterator();
        while (iterator.hasNext()){

            Edge edge = iterator.next();
            edgeLabel.add(edge.getLabel());
        }

        /**add the label from all of object linking vertex in the linked entity graph to 'edgeLabel'**/
        Collection<Vertex> allVertices = getAllVertex();
        Iterator<Vertex> its = allVertices.iterator();
        while (its.hasNext()){

            Vertex vertex = its.next();
            if((vertex.getIsObjectLink()==true) && (vertex.getLabel().equals("OL_PL1")))//"OL_PL1" is temporally added
            {

                Map<String, String> attributesMap = vertex.getAttributes();
                edgeLabel.add(attributesMap.get("OL_PL_RT")); //"OL_PL_RT" is temporally added

            }

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

    /**Renumber the edge & vertex of graphs based on the numerated vertex label & edge label**/
    /**
     *
     * @param numeratedVertexLabels map from vertex label to a number
     * @param numeratedEdgeLabels map from edge label to a number
     * @param graph graph to be renumbered
     */
    public void reOrderLabel(Map<String,Integer> numeratedVertexLabels, Map<String,Integer> numeratedEdgeLabels, Graph graph){

        int vertexIndex= 0; //initialize the index of vertex

        vertexIdWithIndexMap.clear(); // clear the map of vertex id and index

        List<Edge> allEdges = graph.getAllEdges();

        List<Vertex> allVertex = graph.getAllVertices();

        /**assign index to each vertex**/
        Iterator<Vertex> it = allVertex.iterator();
        while (it.hasNext()){

            Vertex vertex = it.next();

            vertex.setIndex(vertexIndex);

            /**Get the unique id of vertex**/
            String id = vertex.getId();
            vertexIdWithIndexMap.put(id, Integer.valueOf(vertexIndex));

            /**numerate the vertex label**/
            vertex.setData(numeratedVertexLabels.get(vertex.getLabel()).toString());

            vertexIndex++;
        }

        /**reorder the edge**/
        Iterator<Edge> its = allEdges.iterator();
        while (its.hasNext()){

            Edge edge = its.next();

            /**Get the start point & end point of this edge**/
            Vertex start = edge.getStartPoint();
            Vertex end = edge.getEndPoint();

            /**Set the index of start & end vertex of the edge**/
            edge.setVertex1Index(start.getIndex());
            edge.setVertex2Index(end.getIndex());

            /**Set weight of the edge**/
            edge.setWeight(numeratedEdgeLabels.get(edge.getLabel()));

        }



    }

    /**Merge an external graph into big graph. This method is used in loading RDF of an external data source to the Linked Entity RDF**/
    /**
     *
     * @param graph an graphed that will be merged to the current linked entity graph
     * @return the merged graph
     */
    public Graph addGraph(Graph graph){

        Graph merged = new Graph(true);

        /**Get all of the vertices from the big graph & external graph**/
        List<Vertex> verticesInBigGraph = this.getAllVertices();
        List<Vertex> verticesInExternalGraph = graph.getAllVertices();

        /**Get all of the edges from the big graph & external graph**/
        List<Edge> edgesInBigGraph = this.getAllEdges();
        List<Edge> edgesInExternalGraph = graph.getAllEdges();

        /**Create a new Array List to contain all the overlapped vertices from 'verticesInBigGraph' and 'verticesInExternalGraph'**/
        List<Vertex> tempVertexList = new ArrayList<Vertex>();

        /**set up a hash map containing the vertex id (of the external graph) that should be replaced*
         * Key: the previous vertex id in the external graph
         * Value: the corresponding vertex id in the merged graph
         * */
        Map<String, String> tempMap = new HashMap<String, String>();

        /**Iterate each vertex in the external graph**/
        Iterator<Vertex> iterator = verticesInExternalGraph.iterator();
        while (iterator.hasNext()){

            Vertex vertexInExternalGraph = iterator.next();

            /**Iterate each vertex in the big graph**/
            Iterator<Vertex> it = verticesInBigGraph.iterator();
            while(it.hasNext()){

                Vertex vertexInBigGraph = it.next();

                boolean isEqualEntity = vertexInBigGraph.isEqualEntity(vertexInExternalGraph);

                if(isEqualEntity){
                    tempVertexList.add(vertexInExternalGraph);

                    /**add the vertex mapping between the vertex id of external graph and the vertex id of merged graph**/
                    tempMap.put(vertexInExternalGraph.getId(), vertexInBigGraph.getId());
                }


            }
        }

        /**Remove all the duplicated vertex from 'verticesInExternalGraph'**/
        verticesInExternalGraph.removeAll(tempVertexList);

        /**Then, merge it to all the vertices in big graph. After that, 'verticesInBigGraph' contains the union set of vertices from big graph and external graph**/
        verticesInBigGraph.addAll(verticesInExternalGraph);

        /**Add them to the merged graph**/
        for(Vertex vertex : verticesInBigGraph){

            merged.addVertex(vertex);
        }

        /**remove all of the adjacent and pre-adjacent vertices**/
        for(Vertex vertex : merged.getAllVertices()){

            vertex.removeAllAdjacentVertex();
            vertex.removeAllPreAjacentVertex();
        }


        /**Add all of the edges of the big graph into the merged graph**/
        Iterator<Edge> its = edgesInBigGraph.iterator();
        while (its.hasNext()){

            Edge edge = its.next();

            /**Get the id of the start point and end point**/
            String startID = edge.getStartPoint().getId();
            String endID = edge.getEndPoint().getId();
            /**add this edge to the merged graph. No need to alter the vertex id. One-to-One vertex mapping from big graph to merged graph**/
            merged.addEdge(startID, endID, edge.getId(), edge.getLabel(), true);
        }

        /**Add all of the edges of external graph into the merged graph**/
        Iterator<Edge> edgeIterator = edgesInExternalGraph.iterator();
        while(edgeIterator.hasNext()){

            Edge edge = edgeIterator.next();
            String label = edge.getLabel();
            String id = edge.getId();
            String startPointIDInExternalGraph = edge.getStartPoint().getId();
            String endPointIDInExternalGraph = edge.getEndPoint().getId();
            String startPointIDInMergedGraph = "";
            String endPointIDInMergedGraph = "";

            //adjust start point ID
            if(tempMap.containsKey(startPointIDInExternalGraph)){

                startPointIDInMergedGraph = tempMap.get(startPointIDInExternalGraph);
            }else {
                startPointIDInMergedGraph = startPointIDInExternalGraph;
            }

            //adjust end point ID
            if(tempMap.containsKey(endPointIDInExternalGraph)){

                endPointIDInMergedGraph = tempMap.get(endPointIDInExternalGraph);
            }else {
                endPointIDInMergedGraph = endPointIDInExternalGraph;
            }

            /**add this edge to the merged graph.**/
            if( (merged.getEdge(startPointIDInMergedGraph,endPointIDInMergedGraph) == null) ||
                    ((merged.getEdge(startPointIDInMergedGraph,endPointIDInMergedGraph) != null) &&
                            (!merged.getEdge(startPointIDInMergedGraph,endPointIDInMergedGraph).getLabel().equals(label))) ){

                merged.addEdge(startPointIDInMergedGraph,endPointIDInMergedGraph, id, label, true);
            }


        }


        return merged;
    }

    /**Compare two graphs. Firstly, two graphs should have the equivalent vertices with same labels.
     * Secondly, all the edges should be equivalent except 'edge1' and 'edge2'*
     * This function is abandoned from 4 Aug 2017 since the result is not correct*/
    /**
     *
     * @param graph another graph
     * @return equivalent or not
     */
    public boolean compare (Graph graph){

        boolean result = false;

        /**Get all the vertices of the two graph to be compared**/
        List<Vertex> allVertices1 = getAllVertices(); //current graph
        List<Vertex> allVertices2 = graph.getAllVertices(); //another graph

        /**Get all of the edges of the two graphs to be compared**/
        List<Edge> allEdges1 = getAllEdges();
        List<Edge> allEdges2 = graph.getAllEdges();

        /**Put all of vertex labels into lists**/
        List<String> allVertexLabels1 = new ArrayList<String>();
        for(Vertex vertex : allVertices1){

            allVertexLabels1.add(vertex.getLabel());
        }

        List<String> allVertexLabels2 = new ArrayList<String>();
        for(Vertex vertex : allVertices2){

            allVertexLabels2.add(vertex.getLabel());
        }

        if (allVertexLabels1.containsAll(allVertexLabels2) && allVertexLabels2.containsAll(allVertexLabels1)){

            /**Compare all of the edges**/

            /**Find all of the equivalent edges in 'allEdges2', which are equivalent to each edge in 'allEdge1'**/
            List<Edge> tempEdgeList1 = new ArrayList<Edge>();
            for(Edge edge1 : allEdges1){
                for (Edge edge2 : allEdges2){
                    if(edge1.compare(edge2)){ tempEdgeList1.add(edge2); }
                }
            }

            /**Find all of the equivalent edges of 'allEdges1', which are equivalent to each edge in 'allEdges2'**/
            List<Edge> tempEdgeList2 = new ArrayList<Edge>();
            for(Edge edge2 : allEdges2){
                for (Edge edge1 : allEdges1){
                    if(edge2.compare(edge1)){ tempEdgeList2.add(edge1); }
                }
            }

            if((tempEdgeList1.size()==allEdges1.size()) && (tempEdgeList2.size()==allEdges2.size())){

                result = true;
            }else{

                result = false;
            }
        }else {

            result = false;
        }


        return result;
    }

    /**Get the left anchor point. adapted on 19 Nov 2017.*
     * @return left anchor point
     * */
    public Vertex getLeftAnchorPoint () {

        if (this.leftAnchorPoint==null) {

            /**Get the first edge (which is the central edge of the boundary graph) of the field 'allEdges'**/
            Edge centralRelation = allEdges.get(0);

            this.leftAnchorPoint = centralRelation.getStartPoint();

        }

        return this.leftAnchorPoint;
    }

    /**Set the left anchor point.*
     * @param leftAnchorPoint left anchor point
     * */
    public void setLeftAnchorPoint (Vertex leftAnchorPoint) {

        this.leftAnchorPoint = leftAnchorPoint;
    }

    /**Get the right anchor point. adapted on 19 Nov 2017.*
     * @return right anchor point
     * */
    public Vertex getRightAnchorPoint () {

        if (this.rightAnchorPoint==null) {

            /**Get the first edge (which is the central edge of the boundary graph) of the field 'allEdges'**/
            Edge centralRelation = allEdges.get(0);

            this.rightAnchorPoint = centralRelation.getEndPoint();

        }
        return this.rightAnchorPoint;
    }

    /**Set the right anchor point*
     * @param rightAnchorPoint right anchor point
     * */
    public void setRightAnchorPoint (Vertex rightAnchorPoint) {

        this.rightAnchorPoint = rightAnchorPoint;
    }

    /**Sort the vertex id in an increasing number. Added on 19 Nov 2017. Unused*
     */
    public void sortVertexIdInIncreasingNumber (){

        int vertexIndex= 0; //initialize the index of vertex

        vertexIdWithIndexMap.clear(); // clear the map of vertex id and index

        List<Vertex> allVertex = this.getAllVertices();

        int length = allVertex.size();

        List<Vertex> sortedAllVertex = new ArrayList<>();

        while (vertexIndex < length) {

            for (Vertex vertex : allVertex) {

                if (vertex.getIndex()==vertexIndex) {

                    sortedAllVertex.add(vertex);

                }

            }

            vertexIndex++;
        }

        /**Set the updated vertex list to the boundary graph **/
        setAllVertices(sortedAllVertex);

        System.out.println("Finish re-sorting this boundary graph! ");

    }


}
