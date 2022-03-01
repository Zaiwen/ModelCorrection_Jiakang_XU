package au.com.d2dcrc.yago2es;

/**
 * Created by Zaiwen FENG on 5/04/2017.
 */

import org.apache.commons.collections4.CollectionUtils;

import java.util.*;

/**Basic data structure for vertex**/
public class Vertex {

    /**Unique Id for the vertex**/
    String id;

    /**label for the vertex, it's class name, for example, 'Person' **/
    private String label;
    /**For Yago data, each vertex represents an instance, and each vertex has instance name.
     * However, Karma output RDF has not this kind of instance name.**/
    private String instanceName; //this variable is optional

    /**Record all of the candidate labels. This variable is used in Yago data.**/
    private List<String> candidateLabels = new ArrayList<String>();

    private List<Edge> outgoingEdges = new ArrayList<Edge>();//outgoing edges
    private List<Vertex> adjacentVertex = new ArrayList<Vertex>(); //post vertex

    /**Add pre vertex list and incoming edge list**/
    private List<Edge> incomingEdges = new ArrayList<Edge>();//incoming
    private List<Vertex> preAdjacentVertex = new ArrayList<Vertex>();//pre vertex

    /**A map to store all the attributes and attributes value of a vertex**/
    private Map<String, String> attributes = new HashMap<String, String>();

    /**A list to save identification string of an entity instance, e.g. "519908316d0a1e7d4a4c9c9f983c9e70c6c7481d"*
     * The code comes from Karma*/
    private List<String> identificationCode = new ArrayList<String>();

    /**is object linking entity instance or not**/
    private boolean isObjectLink = false;

    /**Used to flag if the vertex is visited or not**/
    private boolean visited = false; // used in some special algorithms, e.g. dfs

    /**Used to record the distance to the root**/
    private int length = 0;

    /**index for a vertex. It's used in frequent subgraph matching & graph matching*
     * Note that index is not unique.*/
    private int index = 0;

    Vertex(String id){
        this.id = id;
    }

    public boolean getVisited(){

        return this.visited;
    }

    /**Constructor for initialize the class field. This function is used in feature selection**/
    /**
     *
     * @param index index of the vertex
     * @param label label of the vertex
     * @param id id of the vertex
     */
    public Vertex(int index, String label, String id){
        this.index = index;
        this.label = label;
        this.id = id;
    }

    public void setVisited(boolean visited){

        this.visited = visited;
    }

    public int getLength(){

        return length;
    }

    public void setLength(int length){

        this.length = length;
    }

    public String getId(){
        return id;
    }

    public void setId(String id){this.id = id;}

    public void setData(String label){
        this.label = label;
    }

    public String getLabel(){
        return label;
    }


    public void setAttributes(Map<String,String> attributes){

        this.attributes = attributes;
    }

    public Map<String, String> getAttributes(){

        return this.attributes;
    }

    public void setIdentificationCode(List<String> identificationCode){

        this.identificationCode = identificationCode;
    }

    public List<String> getIdentificationCode(){

        return identificationCode;
    }

    public void setIsObjectLink(boolean isObjectLink){

        this.isObjectLink = isObjectLink;
    }

    public boolean getIsObjectLink(){

        return this.isObjectLink;
    }

    public void setInstanceName (String instanceName) {

        this.instanceName = instanceName;
    }

    public String getInstanceName () {

        return this.instanceName;
    }

    /**newly added on 29 June 17*
     * @param candidateLabels candidate labels only used in the evaluation on Yago*/
    public void setCandidateLabels (List<String> candidateLabels) {

        this.candidateLabels = candidateLabels;
    }

    /**newly added on 29 June 17*
     * @param candidateLabel candidate labels only used in the evaluation on Yago
     * */
    public void addCandidateLabels (String candidateLabel) {

        candidateLabels.add(candidateLabel);
    }

    /**newly added on 29 June 17*
     * @return a list of candidate labels
     * */
    public List<String> getCandidateLabels () {

        return candidateLabels;
    }

    /**
     * @param e edge between the original vertex and the added vertex
     * @param v added vertex
     * **/
    public void addAdjacentVertex(Edge e, Vertex v){
        outgoingEdges.add(e);
        adjacentVertex.add(v);
    }

    /**remove adjacent vertex*
     * @param e edge to be removed
     * @param v vertex to be removed
     * */
    public void removeAdjacentVertex(Edge e, Vertex v){

        outgoingEdges.remove(e);
        adjacentVertex.remove(v);
    }

    /**remove all the adjacent vertices**/
    public void removeAllAdjacentVertex(){

        outgoingEdges.removeAll(outgoingEdges);
        adjacentVertex.removeAll(adjacentVertex);
    }

    /**
     * @param v the vertex to be added
     * @param e the edge to be added
     * **/
    public void addPreAdjacentVertex(Edge e, Vertex v){
        incomingEdges.add(e);
        preAdjacentVertex.add(v);
    }

    /**remove pre-adjacent vertex*
     * @param v  the vertex to be removed
     * @param e the edge to be removed
     * */
    public void removePreAdjacentVertex(Edge e, Vertex v){
        incomingEdges.remove(e);
        preAdjacentVertex.remove(v);
    }

    /**remove all the pre-adjacent vertices**/
    public void removeAllPreAjacentVertex(){

        incomingEdges.removeAll(incomingEdges);
        preAdjacentVertex.removeAll(preAdjacentVertex);
    }

    public int getIndex(){

        return index;
    }

    public void setIndex(int index){

        this.index = index;
    }

    /**print the vertex content, used in subgraph matching*
     * @return the returned string
     * */
    public String toString(){
        return "v" + String.valueOf(id);
    }

    public List<Vertex> getAdjacentVertexes(){return adjacentVertex;}

    public void setAdjacentVertex(List<Vertex> adjacentVertex){ this.adjacentVertex = adjacentVertex; }

    public List<Edge> getOutgoingEdges(){return outgoingEdges; }

    public void setOutgoingEdges(List<Edge> outgoingEdges){ this.outgoingEdges = outgoingEdges;}

    public List<Vertex> getPreAdjacentVertex() {return preAdjacentVertex;}

    public void setPreAdjacentVertex(List<Vertex> preAdjacentVertex){ this.preAdjacentVertex = preAdjacentVertex; }

    public List<Edge> getIncomingEdges() { return  incomingEdges; }

    public void setIncomingEdges(List<Edge> incomingEdges) {this.incomingEdges = incomingEdges; }

    /**Get all the unvisited vertices of a vertex*
     * @return a list of unvisited child vertices
     * */
    public List<Vertex> getUnvisitedChildVertex(){
        List<Vertex> unvisited = new ArrayList<Vertex>();

        List<Vertex> adjacentVertices = this.getAdjacentVertexes();
        List<Vertex> preAdjacentVertices = this.getPreAdjacentVertex();

        /**combine two lists**/
        List<Vertex> newList = new ArrayList<Vertex>(adjacentVertices);
        newList.addAll(preAdjacentVertices);

        /**Get any unvisited vertex from the list**/
        Iterator<Vertex> it = newList.iterator();
        while (it.hasNext()){

            Vertex vertex = it.next();

            if(vertex.visited == false){

                unvisited.add(vertex);
            }

        }

        return unvisited;
    }

    public int getDegree(){
        return outgoingEdges.size();
    }

    /**print this vertex**/
    public void printNode(){

        System.out.println(label);
    }

    /**Compare a vertex (an entity) with another vertex (entity)**/
    /**The principle is to compare all of attributes and see if they are equivalent
     * If all of attributes are equivalent, then these two entities are equivalent
     * @param vertex another vertex (entity)
     * @return equal or not
     */
    public boolean isEqualEntity (Vertex vertex){

        boolean isEqual = false;

        /**Get all of attributes of this entity & another entity**/
        Map<String, String> attributes1 = this.getAttributes();
        Map<String, String> attributes2 = vertex.getAttributes();

        /**Compare these two hash map**/
        isEqual = Util.equalMaps(attributes1, attributes2);

        return isEqual;
    }

}
