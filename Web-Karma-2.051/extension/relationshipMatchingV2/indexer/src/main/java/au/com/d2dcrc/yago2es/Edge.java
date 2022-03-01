package au.com.d2dcrc.yago2es;

/**
 * Created by Zaiwen FENG on 5/04/2017.
 */
public class Edge {

    private boolean isDirected = false;
    private Vertex vertex1; //start point
    private Vertex vertex2; //end point
    private int vertex1Index; //id for the start point, only used in frequent subgraph mining & graph matching
    private int vertex2Index; //id for the end point, only used in frequent subgraph mining & graph matching
    private boolean visited = false; // just for some special algorithms,e.g. vertex-induce subgraph
    private String id;/**id of the edge, which is the unique identification of this edge**/
    private String label;/**label for an edge**/
    private int weight; /**numeric label - correspond to the text label saved in 'label'**/

    /**Constructor used in the phase of subgraph matching**/
    /**
     *
     * @param vertex1 start point
     * @param vertex2 end point
     * @param weight number representing edge label, for example, 0 represents "visits"
     * @param id unique id of this edge
     */
    Edge(Vertex vertex1, Vertex vertex2, int weight, String id){
        this.vertex1 = vertex1;
        this.vertex2 = vertex2;
        this.weight = weight;
        this.id = id;
    }


    /**vertex1 is start and vertex2 is end point if the edge is directed**/
    Edge(Vertex vertex1, Vertex vertex2,boolean isDirected){
        this.vertex1 = vertex1;
        this.vertex2 = vertex2;
        this.isDirected = isDirected;
    }


    /**
     * create an edge with a label and id*
     /**
     * @param vertex1 start point of the edge
     * @param vertex2 end point of the edge
     * @param isDirected direction of edge
     * @param label edge label
     * @param id edge id
     */
    Edge(Vertex vertex1, Vertex vertex2, boolean isDirected, String label, String id){
        this.vertex1 = vertex1;
        this.vertex2 = vertex2;
        this.isDirected = isDirected;
        this.label = label;
        this.id = id;
    }

    /**start point and end point setter**/
    /**
     *
     * @param start start vertex of edge
     * @param end end vertex of edge
     */
    public void setVertices(Vertex start, Vertex end){
        this.vertex1 = start;
        this.vertex2 = end;
    }

    /**Get the start point of the edge**/
    /**
     *
     * @return the start vertex of edge
     */
    public Vertex getStartPoint() {
        return vertex1;
    }

    /**Get the end point of the edge**/
    /**
     *
     * @return the end vertex of edge
     */
    public Vertex getEndPoint(){
        return vertex2;
    }

    /**
     * @param vertex1Id index of start vertex, it's used in frequent subgraph mining and graph matching.
     */
    public void setVertex1Index(int vertex1Id){

        this.vertex1Index = vertex1Id;
    }

    /**
     *
     * @param vertex2Id  index of end vertex. It's used in frequent subgraph mining and graph matching
     */
    public void setVertex2Index(int vertex2Id){

        this.vertex2Index = vertex2Id;
    }


    /**Edge label getter**/
    /**
     *
     * @return label of edge
     */
    public String getLabel(){
        return  this.label;
    }

    /**Get the label of start and end vertex*
     * @return label of two vertices and edge
     * */
    public String getVerticesLabel() {
        return "(" + vertex1.getLabel().toString() + "," +
                vertex2.getLabel().toString() + "): " + label;
    }

    /**print edge content**/
    /**
     *
     * @return edge content
     */
    public String toString() {
        return "(" + vertex1.toString() + "," + vertex2.toString() + "): " + label;
    }

    public String getId() {
        return this.id;
    }

    public boolean isDirected(){
        return isDirected;
    }

    public int getWeight() {return weight;}

    public void setWeight(int weight){

        this.weight = weight;
    }

    public int getStartIndex() {return vertex1Index; }

    public int getEndIndex () {return vertex2Index; }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((vertex1 == null) ? 0 : vertex1.hashCode());
        result = prime * result + ((vertex2 == null) ? 0 : vertex2.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Edge other = (Edge) obj;
        if (vertex1 == null) {
            if (other.vertex1 != null)
                return false;
        } else if (!vertex1.equals(other.vertex1))
            return false;
        if (vertex2 == null) {
            if (other.vertex2 != null)
                return false;
        } else if (!vertex2.equals(other.vertex2))
            return false;
        return true;
    }

    /**check if two edges are same - with the same vertices labels and edge label*
     * It is used in comparing two graphs equity in the phase of minimizing feature set
     * @param edge the edge to be compared
     * @return comparing result
     * */
    public boolean compare(Edge edge){

        boolean result = false;

        /**Get the label of start point**/
        Vertex start = edge.getStartPoint();
        String startPointLabel = start.getLabel();

        /**Get the label of end point**/
        Vertex end = edge.getEndPoint();
        String endPointLabel = end.getLabel();

        /**Get the label of the edge**/
        Integer weight = edge.getWeight();

        if((vertex1.getLabel().equals(startPointLabel)) && (vertex2.getLabel().equals(endPointLabel)) &&
                (Integer.valueOf(this.weight).equals(weight))){
            return true;

        }else {
            return false;
        }
    }


    public void setVisited(boolean visited){

        this.visited = visited;
    }

    public boolean getVisited (){

        return visited;
    }

    /**Compare two edges by their start & end point**/
    /**
     *
     * @param edge the edge to be compared
     * @return -1 if e is larger
     * 0 if they are the same
     * 1 if e is smaller
     */
    public int compareTo(Edge edge){

        if (this.vertex1.getLabel().compareTo(edge.vertex2.getLabel()) < 0){
            return -1;
        } else if (this.vertex1.getLabel().compareTo(edge.vertex1.getLabel()) == 0) {

            if (this.vertex2.getLabel().compareTo(edge.vertex2.getLabel()) < 0) {
                return -1;
            }

            // DO NOT REARRANGE THE EDGE ORDER BY WEIGHT ??? What's mean???
            else if (this.vertex2.getLabel().compareTo(edge.vertex2.getLabel()) == 0) {
                return 0;
            } else {

                return 1;
            }
        } else {
            return 1;
        }
    }
}
