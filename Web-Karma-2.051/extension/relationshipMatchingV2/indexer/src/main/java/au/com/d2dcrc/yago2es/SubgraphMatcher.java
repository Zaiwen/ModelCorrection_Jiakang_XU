package au.com.d2dcrc.yago2es;

import ESM.ESM;
import ESM.Vertex;
import ESM.Edge;
import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * Created by Zaiwen FENG on 9/05/2017.
 */



public class SubgraphMatcher {
    private List<Graph> graphs;
    private List<Graph> subgraphs;
    private Map<Graph, Integer> labeledGraphs;

    private String graphFile;
    private String subGraphFile;

    int[][] matrix;

    /**Default Constructor**/
    public SubgraphMatcher () {}

    /**Constructor of this subgraph matcher**/
    /**
     *
     * @param graphFile file for these boundary graphs with specified relationships between two recognized entity instances
     * @param featureSetFile file for frequent mined from a bundle of boundary graphs with a specified relationship. e.g. with relationship "rent house" between a person and a location
     */
    public SubgraphMatcher(String graphFile, String featureSetFile){

        this.graphFile = graphFile;
        this.subGraphFile = featureSetFile;

        graphs = GSpanWrapper.loadGraph(graphFile);
        subgraphs = GSpanWrapper.loadGraph(featureSetFile);

    }

    /**Constructor*
     *@param graphs all of boundary graphs
     *@param featureGraphs discriminative feature set (DFS)*/
    public SubgraphMatcher(List<Graph> graphs, List<Graph> featureGraphs){

        this.graphs = graphs;
        this.subgraphs = featureGraphs;
    }

    /**Constructor*
     * @param labeledGraphs all of boundary graphs
     * @param featureGraphs discriminative feature set (DFS)
     * */
    public SubgraphMatcher(Map<Graph, Integer> labeledGraphs, List<Graph> featureGraphs) {

        this.labeledGraphs = labeledGraphs;
        this.subgraphs = featureGraphs;
    }

    public List<Graph> getGraphs(){return graphs;}

    public void setGraphs(List<Graph> graphs) {this.graphs = graphs;}

    public List<Graph> getSubgraphs() {return subgraphs;}

    public void setSubgraphs(List<Graph> subgraphs) {this.subgraphs = subgraphs;}

    public Map<Graph, Integer> getLabeledGraphs() {return labeledGraphs;}

    public void setLabeledGraphs(Map<Graph, Integer> labeledGraphs) {this.labeledGraphs = labeledGraphs;}

    /**Match all subgraphs to each boundary graph, set 1 if it's subgraph, otherwise 0
     * Acquire Discriminative Feature Set (DFS) for these boundary graphs**/
    /**
     *
     * @param subgraphs frequent subgraphs
     * @param graphs boundary graphs
     * @return design matrix
     */
    public int[][] acquireMatrix(List<Graph> subgraphs, List<Graph> graphs) {
        //graphs = GSpanWrapper.loadGraph(graphFile);
        //subgraphs = GSpanWrapper.loadGraph(subGraphFile);

        matrix = new int[subgraphs.size()][graphs.size()];
        int counter = 0; //??
        String tempString;  //??
        ArrayList<String> featureData = new ArrayList<String>(); //??

        int value = 0;

        for (int i = 0; i < subgraphs.size(); i++) {

            tempString = counter + ": ,";

            for (int j = 0; j < graphs.size(); j++) {

                DirectedSparseGraph<Vertex,Edge> subgraph = convertESMGraph(subgraphs.get(i));
                DirectedSparseGraph<Vertex, Edge> graph = convertESMGraph(graphs.get(j));

                /**set 1 if it's the sub graph**/
                value = isGraphIsmomorphism(subgraph, graph);
                matrix[i][j] = value;

            }
        }
        return matrix;
    }

    /**Match all subgraphs to each boundary graph, set 1 if it's subgraph, otherwise 0, together with label*
     * this method is used in code matrix based on training set*/
    /**
     *
     * @param subgraphs Minimized graph discriminative graph feature set
     * @param labeledGraphsMap Key: Boundary graph; Value: weight on the edge with labeled relationship type, for example: 6-visits, 5-having dinner at
     * @return design matrix for label
     */
    public int[][] acquireMatrix(List<Graph> subgraphs, Map<Graph, Integer> labeledGraphsMap) {

        /**Split 'labeledGraphsMap' into two array lists**/
        List<Graph> keys = new ArrayList<Graph>(labeledGraphsMap.keySet());
        List<Integer> values = new ArrayList<Integer>(labeledGraphsMap.values());

        matrix = new int[subgraphs.size()+1][labeledGraphsMap.size()];
        int value = 0;

        /**Fill the feature value into design matrix**/
        for (int i = 0; i < subgraphs.size(); i++) {

            for (int j = 0; j < keys.size(); j++) {

                DirectedSparseGraph<Vertex,Edge> subgraph = convertESMGraph(subgraphs.get(i));
                DirectedSparseGraph<Vertex, Edge> graph = convertESMGraph(keys.get(j));

                /**set 1 if it's the sub graph**/
                value = isGraphIsmomorphism(subgraph, graph);
                matrix[i][j] = value;
                System.out.println("the subgraph " + i + " has been matched to the graph " + j + " !");
            }

        }

        /**Fill the label into design matrix**/
        for (int j = 0; j < values.size(); j++) {

            matrix[subgraphs.size()][j] = values.get(j);
        }
        return matrix;
    }

    /**Compare if two graphs are the same
     * @param graph1 graph to be compared
     * @param graph2 graph to be compared
     * @return true means two graphs are same, false means two graphs are different
     * */
    public boolean compare (Graph graph1, Graph graph2) {

        boolean result = false;
        DirectedSparseGraph<Vertex, Edge> source = convertESMGraph(graph1);
        DirectedSparseGraph<Vertex, Edge> target = convertESMGraph(graph2);
        /**set 1 if source is the sub graph of target**/
        int positive = isGraphIsmomorphism(source, target);
        /**set 1 if target is the sub graph of source**/
        int negative = isGraphIsmomorphism(target, source);

        if ( (positive == 1 ) && (negative == 1)) {

            result = true;
        }


        return result;
    }

    /**Suppose there are two graph sets, get the union of these two sets*
     * @param dfs1 Precondition: dfs1 must not contain duplicated elements
     * @param dfs2 Precondition: dfs2 must not contain duplicated elements
     * @return union of dfs1 and dfs2
     * */
    public List<Graph> getUnionOfGraphSet (List<Graph> dfs1, List<Graph> dfs2) {

        List<Graph> union = new ArrayList<Graph>();

        /**firstly, get the intersection of two sets**/
        List<Graph> intersection = getIntersectionOfGraphSet(dfs1,dfs2);

        union.addAll(dfs1);
        union.addAll(dfs2);//Note: Now union might contain some duplicated elements!

        /**subtract the intersection from union**/
        Iterator<Graph> iterator = union.iterator();
        while (iterator.hasNext()) {

            Graph graph = iterator.next();

            for (Graph dfs : intersection) {

                if (compare(graph, dfs)) {

                    iterator.remove();

                    break;//added on 23 Nov 2017.

                }

            }
        }

        union.addAll(intersection);

        return union;
    }

    /**Suppose there are three discriminative feature set (graph set), get the union set of these three graph sets*
     * @param dfs1 Precondition: dfs1 must not contain duplicated elements
     * @param dfs2 Precondition: dfs2 must not contain duplicated elements
     * @param dfs3 Precondition: dfs3 must not contain duplicated elements
     * @return union of dfs 1,2,3
     * */
    public List<Graph> getUnionOfGraphSet (List<Graph> dfs1, List<Graph> dfs2, List<Graph> dfs3) {

        List<Graph> union = getUnionOfGraphSet(dfs1, dfs2);

        List<Graph> union1 = getUnionOfGraphSet(union, dfs3);

        return union1;
    }

    /**Suppose there are two discriminative feature set, get the intersection of these two graph sets*
     * @param dfs1 Precondition: dfs1 must not contain duplicated elements
     * @param dfs2 Precondition : dfs2 must not contain duplicated elements
     * @return intersection of dfs1 and dfs2
     * */
    public List<Graph> getIntersectionOfGraphSet (List<Graph> dfs1, List<Graph> dfs2) {

        List<Graph> intersection = new ArrayList<Graph>();

        for (Graph dfs : dfs1) {



                for (Graph anotherDfs : dfs2) {



                        if (compare(anotherDfs, dfs)) {

                            intersection.add(anotherDfs);
                        }


                }

        }

        return intersection;
    }

    /**Given graph set A and graph set B, get A-B*
     * @param dfs1 Precondition: dfs1 must not contain duplicated elements
     * @param dfs2 Precondition: dfs2 must not contain duplicated elements
     * @return dfs1-dfs2
     * */
    public List<Graph> getSubstractSet (List<Graph> dfs1, List<Graph> dfs2) {

        List<Graph> substract = new ArrayList<>();
        substract.addAll(dfs1);

        /**first, get the intersection of dfs1 and dfs2**/
        List<Graph> intersection = getIntersectionOfGraphSet(dfs1, dfs2);

        Iterator<Graph> it = substract.iterator();
        while (it.hasNext())
        {
            Graph graph = it.next();

            for (Graph dfs : intersection) {

                if (compare(graph, dfs)) {

                    it.remove();

                    break;//added on 16 Nov 2017

                }

            }

        }

        return substract;
    }


    /**judge if subgraph is subsumed to the graph**/
    /**
     *
     * @param subgraph graph used in the ESM algorithm
     * @param graph graph used in the ESM algorithm
     * @return 0 or 1. 0 means the subgraph isn't subsumed to the graph. '1' means the subgraph is subsumed to the graph
     * @comment it might be used in the semantic model ranking @ 14 Jan 2019
     */
    public int isGraphIsmomorphism(DirectedGraph subgraph, DirectedGraph graph) {

        int i = 0; //the fault is that subgraph is not subsumed to the graph
        boolean b = false;
        ESM esm  = new ESM(subgraph, graph); //graph1-subgraph; graph2-graph
        b = esm.isSubgraphIsomorphism();

        if(b == false){
            i = 0;
        }else {
            i = 1;
        }
        return i;
    }



    /**Convert a graph into an adjacent matrix, which is essentially a two-dimensional array*
     * unused*/
    /**
     *
     * @param graph the graph to be converted
     * @return the corresponding matrix of the graph
     */
    public AdjacentMatrix graphToMatrix(Graph graph) {

        return new AdjacentMatrix(graph);
    }

    /**Convert vertex in 'au.edu.unisa' into the vertex used in Exact Subgraph Matching (ESM) Algorithm*
     * unused*/
    /**
     *
     * @param v vertex defined in au.edu.unisa.Vertex
     * @return vertex used in Exact Subgraph Matching (ESM) Algorithm
     */
    public Vertex convertESMVertex(au.com.d2dcrc.yago2es.Vertex v){

        Vertex vertex = new Vertex();

        //lemma = label
        vertex.setLemma(v.getLabel());

        /**unused, however, it must be defined in order to invoke ESM algorithm**/
        vertex.setGeneralizedPOS("NN");

        /**unused, however, it must be defined in order to invoke ESM algorithm**/
        vertex.setCompareForm(v.getLabel().concat(" nn"));

        return vertex;
    }

    /**Convert graph in 'au.edu.unisa' into the graph used in Exact Subgraph Matching (ESM) Algorithm**/
    /**
     *
     * @param graph boundary graph and frequent sub-graph
     * @return graph used in the Exact Subgraph Matching (ESM) Algorithm
     */
    public DirectedSparseGraph<Vertex,Edge> convertESMGraph(Graph graph) {

        DirectedSparseGraph<Vertex,Edge> g = new DirectedSparseGraph<Vertex, Edge>();
        List<au.com.d2dcrc.yago2es.Vertex> allVertices = graph.getAllVertices();/**Get all the vertices & Edges from the graph in 'au.edu.unisa'**/
        List<au.com.d2dcrc.yago2es.Edge> allEdges = graph.getAllEdges();


        Map<Integer, Vertex> tokenToNode = new HashMap<Integer, Vertex>();/**Set up a new hash map for all the vertices. The key is the index of vertex, and the value is the vertex used in ESM**/
        Iterator<au.com.d2dcrc.yago2es.Vertex> iterator = allVertices.iterator();
        while (iterator.hasNext()){

            au.com.d2dcrc.yago2es.Vertex vertex = iterator.next();
            int index = vertex.getIndex();

            /**Create a new vertex, and fill field to this vertex**/
            Vertex vertex_ESM = new Vertex();
            vertex_ESM.setLemma(vertex.getLabel());//lemma = label
            vertex_ESM.setGeneralizedPOS("NN"); //"NN" is not meaningful here
            vertex_ESM.setCompareForm(vertex.getLabel().concat( " nn"));

            /**add this vertex to graph & hashmap 'tokenToNode'**/
            g.addVertex(vertex_ESM);
            tokenToNode.put(new Integer(index), vertex_ESM);
        }

        /**Convert all of the edges**/
        Iterator<au.com.d2dcrc.yago2es.Edge> it = allEdges.iterator();
        while (it.hasNext()){

            au.com.d2dcrc.yago2es.Edge edge = it.next();

            /**Get the index of start, label and the index of end**/
            int startIndex = edge.getStartIndex();
            int endIndex = edge.getEndIndex();
            int weight = edge.getWeight();

            /**Create a new edge used in ESM**/
            Edge edge_ESM = new Edge(tokenToNode.get(startIndex), String.valueOf(weight), tokenToNode.get(endIndex));
            g.addEdge(edge_ESM, tokenToNode.get(startIndex), tokenToNode.get(endIndex));
        }

        return g;
    }

    /**load knowledge graph and pattern from local machine disk for subgraph matching.
     * @from 24 Sep 2018
     * @param fileName file name to be loaded
     * **/
    public static List<Graph> loadGraph(String fileName){

        List<Graph> graphs = new ArrayList<Graph>();
        Graph graph = new Graph(true);

        String line = null;
        int vertexIndex = 0;
        String vertexLabel = null;
        int startPointIndex = 0;
        int endPointIndex = 0;
        int edgeWeight = 0;

        // load the graph file and store those graphs
        try {
            FileReader fileReader = new FileReader(fileName);
            BufferedReader br = new BufferedReader(fileReader);

            while ((line = br.readLine()) != null) {
                if (line.substring(0, 1).equals("t")) {
                    graphs.add(graph);
                    // initialize Graph graph
                    graph = new Graph(true);
                }

                // match vertex
                else if (line.substring(0, 1).equals("v")) {
                    vertexIndex = Integer.parseInt(line.split(" ")[1]);
                    vertexLabel = line.split(" ")[2];
                    au.com.d2dcrc.yago2es.Vertex vertex = new au.com.d2dcrc.yago2es.Vertex(vertexIndex,vertexLabel, RandomStringUUID.createUUID());
                    graph.addVertex(vertex);
                    graph.addIndexedVertex(vertex);

                } else if (line.substring(0, 1).equals("e")) {
                    Map<Integer, au.com.d2dcrc.yago2es.Vertex> allIndexedVertex = graph.getAllIndexedVertex();
                    startPointIndex = Integer.parseInt(line.split(" ")[1]);
                    endPointIndex = Integer.parseInt(line.split(" ")[2]);
                    au.com.d2dcrc.yago2es.Vertex start = allIndexedVertex.get(startPointIndex);
                    au.com.d2dcrc.yago2es.Vertex end = allIndexedVertex.get(endPointIndex);
                    double originalWeight = Double.parseDouble(line.split(" ")[3]);
                    edgeWeight =(int) originalWeight;
                    au.com.d2dcrc.yago2es.Edge edge = new au.com.d2dcrc.yago2es.Edge(start, end, edgeWeight, RandomStringUUID.createUUID());
                    edge.setVertex1Index(startPointIndex);
                    edge.setVertex2Index(endPointIndex);
                    graph.addEdge(edge);
                }
            }
            // remember to append the last one
            graphs.add(graph);
            // remove the first element since it's nothing
            graphs.remove(0);
            br.close();
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return graphs;




    }


    /**just for test subgraph matching. *
     * @from 24 Sep 2018
     * */
    public static void main(String args[]){
        List<Graph> graphs = loadGraph("/Users/fengz/Project/Web-KarmaV3.5/graph-matching-stuff/test01.txt");
        Graph largeGraph = graphs.get(0);
        Graph pattern = graphs.get(1);
        System.out.println("large graph and pattern have been imported...");

        SubgraphMatcher subgraphMatcher = new SubgraphMatcher();
        DirectedSparseGraph<Vertex,Edge> graph = subgraphMatcher.convertESMGraph(largeGraph);
        DirectedSparseGraph<Vertex,Edge> sub = subgraphMatcher.convertESMGraph(pattern);
        System.out.println("conversion finished..");

        int match = subgraphMatcher.isGraphIsmomorphism(sub,graph);
        System.out.println(match);
    }
}
