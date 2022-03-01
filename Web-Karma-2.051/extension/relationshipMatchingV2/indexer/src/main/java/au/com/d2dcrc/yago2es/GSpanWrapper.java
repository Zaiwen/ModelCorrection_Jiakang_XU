package au.com.d2dcrc.yago2es;


import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.FileNotFoundException;

/**
 * Created by Zaiwen Feng on 5/05/2017.
 */
public class GSpanWrapper {

    /**write the input of Parsemis, Unused
     /**
     *
     * @param boundaryGraphs -a list of boundary graphs that has not be reordered;
     * @param numeratedVertexLabels -a map of numerated vertex labels;
     * @param numeratedEdgeLabels -numeratedEdgeLabels: a map of numerated edge labels;
     * @param fileName -the file (including directory) that is written to
     */
    public static void writeToInputFile(List<Graph> boundaryGraphs, Map<String, Integer> numeratedVertexLabels,
                                        Map<String, Integer> numeratedEdgeLabels, String fileName){

        BufferedWriter bw = null;
        FileWriter fw = null;

        try{
            /**create a list to contain all the data **/
            List<String> data = new ArrayList<String>();

            /**Loop each boundary graph**/
            int graphID = 0;
            Iterator<Graph> graphIterator = boundaryGraphs.iterator();
            while (graphIterator.hasNext()){

                Graph boundaryGraph = graphIterator.next();

                /**create a new hashmap to map vertex unique id to newly numerated id**/
                Map<String, Integer> vertexIdMap = new HashMap<String, Integer>();

                /**print the title of each graph, e.g. "t # 0", "t # 1", "t # 2", ......**/
                String title = "t".concat(" ").concat("#").concat(" ").concat(String.valueOf(graphID).concat("\n"));
                data.add(title);
                System.out.print(title);

                /**Get all the vertices of boundary graph**/
                Collection<Vertex> allVertex = boundaryGraph.getAllVertex();

                /**Loop all the vertices of this boundary graph**/
                Iterator<Vertex> it = allVertex.iterator();

                int vertexID = 0;//vertex id is the 2nd column of the gSpan input

                while (it.hasNext()){

                    Vertex vertex = it.next();

                    /**Get the label of this vertex**/
                    String label = vertex.getLabel();

                    /**Get the id of this vertex, and add the mapping relation to 'vertexIdMap'**/
                    vertexIdMap.put(vertex.getId(),vertexID);

                    /**convert label to the number based on 'numeratedVertexLabels'**/
                    Integer labelID = numeratedVertexLabels.get(label);

                    String vertexIdWithLabel = "v".concat(" ").concat(String.valueOf(vertexID)).concat(" ").concat(labelID.toString()).concat("\n");

                    /**print each vertex with id and its label, which is represented as a number as well. e.g. "v 1 0"s**/
                    data.add(vertexIdWithLabel);
                    System.out.print(vertexIdWithLabel);

                    vertexID++;
                }

                /**Get all the edges of boundary graph**/
                Collection<Edge> allEdges = boundaryGraph.getAllEdges();

                /**Loop all the edges of this boundary graph**/
                Iterator<Edge> its = allEdges.iterator();

                while (its.hasNext()){

                    Edge edge = its.next();

                    /**Get the label of this edge**/
                    String label = edge.getLabel();

                    /**convert label to the number based on 'numeratedVertexLabels'**/
                    Integer labelID = numeratedEdgeLabels.get(label);

                    if(labelID == null){

                        throw new NullPointerException("Undetected edge label...");

                    }

                    /**Get the numerated start id & end id of this edge**/
                    Integer start = vertexIdMap.get(edge.getStartPoint().getId());
                    Integer end = vertexIdMap.get(edge.getEndPoint().getId());

                    String edgeWithLabel = "e".concat(" ").concat(start.toString()).concat(" ").concat(end.toString()).
                            concat(" ").concat(labelID.toString()).concat("\n");
                    data.add(edgeWithLabel);
                    /**print each edge with start and end and its label, which is represented as a number as well.
                     * e.g. "e 0 1 3"**/
                    System.out.print(edgeWithLabel);
                }


                graphID++;
            }


            fw = new FileWriter(fileName);
            bw = new BufferedWriter(fw);

            Iterator<String> stringIterator = data.iterator();
            while (stringIterator.hasNext()){

                String string = stringIterator.next();
                bw.write(string);
            }


            System.out.print("Done");

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

    /***write the input of parsemis**/
    /**
     *
     * @param boundaryGraphs a list of boundary graphs that has been renumbered;
     * @param fileName the file (including directory) that is written to
     */
    public static void writeToInputFile(List<Graph> boundaryGraphs,  String fileName){

        BufferedWriter bw = null;
        FileWriter fw = null;

        try{
            /**create a list to contain all the data **/
            List<String> data = new ArrayList<String>();

            /**Loop each boundary graph**/
            int graphID = 0;
            Iterator<Graph> graphIterator = boundaryGraphs.iterator();
            while (graphIterator.hasNext()){

                Graph boundaryGraph = graphIterator.next();

                /**print the title of each graph, e.g. "t # 0", "t # 1", "t # 2", ......**/
                String title = "t".concat(" ").concat("#").concat(" ").concat(String.valueOf(graphID).concat("\n"));
                data.add(title);
                System.out.print(title);

                /**Get all the vertices of boundary graph**/
                Collection<Vertex> allVertex = boundaryGraph.getAllVertex();

                /**Loop all the vertices of this boundary graph**/
                Iterator<Vertex> it = allVertex.iterator();

                while (it.hasNext()){

                    Vertex vertex = it.next();

                    /**Get the numeric label of this vertex**/
                    String label = vertex.getLabel();

                    String vertexIdWithLabel = "v".concat(" ").concat(String.valueOf(vertex.getIndex())).concat(" ").concat(label).concat("\n");

                    /**print each vertex with id and its label, which is represented as a number as well. e.g. "v 1 0"s**/
                    data.add(vertexIdWithLabel);
                    //System.out.print(vertexIdWithLabel);

                }

                /**Get all the edges of boundary graph**/
                Collection<Edge> allEdges = boundaryGraph.getAllEdges();

                /**Loop all the edges of this boundary graph**/
                Iterator<Edge> its = allEdges.iterator();

                while (its.hasNext()){

                    Edge edge = its.next();

                    /**Get the numeric label of this edge**/
                    int weight = edge.getWeight();

                    /**Get the numerated start id & end id of this edge**/
                    Integer start = edge.getStartPoint().getIndex();
                    Integer end = edge.getEndPoint().getIndex();

                    String edgeWithLabel = "e".concat(" ").concat(start.toString()).concat(" ").concat(end.toString()).
                            concat(" ").concat(String.valueOf(weight)).concat("\n");
                    data.add(edgeWithLabel);
                    /**print each edge with start and end and its label, which is represented as a number as well.
                     * e.g. "e 0 1 3"**/
                    //System.out.print(edgeWithLabel);
                }


                graphID++;
            }


            fw = new FileWriter(fileName);
            bw = new BufferedWriter(fw);

            Iterator<String> stringIterator = data.iterator();
            while (stringIterator.hasNext()){

                String string = stringIterator.next();
                bw.write(string);
            }


            System.out.print("Done");

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

    /***write the input of parsemis.The difference between this method and the above method 'writeToInputFile' is that
     * This method will ensure that the vertices is sort by index rather than id.  Added on 20 Nov 2017. **/
    /**
     *
     * @param boundaryGraphs a list of boundary graphs of which the vertices have been sorted.;
     * @param fileName the file (including directory) that is written to
     */
    public static void writeIndexedGraphToInputFile(List<Graph> boundaryGraphs,  String fileName){

        BufferedWriter bw = null;
        FileWriter fw = null;

        try{
            /**create a list to contain all the data **/
            List<String> data = new ArrayList<String>();

            /**Loop each boundary graph**/
            int graphID = 0;
            Iterator<Graph> graphIterator = boundaryGraphs.iterator();
            while (graphIterator.hasNext()){

                Graph boundaryGraph = graphIterator.next();

                /**print the title of each graph, e.g. "t # 0", "t # 1", "t # 2", ......**/
                String title = "t".concat(" ").concat("#").concat(" ").concat(String.valueOf(graphID).concat("\n"));
                data.add(title);
                System.out.print(title);

                /**Get all the vertices of boundary graph. The vertices are sorted by the index of vertex**/
                Map<Integer, Vertex> allIndexedVertex = boundaryGraph.getAllIndexedVertex();

                /**Loop all the vertices of this boundary graph**/
                Iterator it = allIndexedVertex.entrySet().iterator();

                while (it.hasNext()){

                    Map.Entry pair = (Map.Entry)it.next();

                    Vertex vertex = (Vertex) pair.getValue();

                    /**Get the numeric label of this vertex**/
                    String label = vertex.getLabel();

                    String vertexIdWithLabel = "v".concat(" ").concat(String.valueOf(vertex.getIndex())).concat(" ").concat(label).concat("\n");

                    /**print each vertex with id and its label, which is represented as a number as well. e.g. "v 1 0"s**/
                    data.add(vertexIdWithLabel);
                    //System.out.print(vertexIdWithLabel);

                }

                /**Get all the edges of boundary graph**/
                Collection<Edge> allEdges = boundaryGraph.getAllEdges();

                /**Loop all the edges of this boundary graph**/
                Iterator<Edge> its = allEdges.iterator();

                while (its.hasNext()){

                    Edge edge = its.next();

                    /**Get the numeric label of this edge**/
                    int weight = edge.getWeight();

                    /**Get the numerated start id & end id of this edge**/
                    Integer start = edge.getStartPoint().getIndex();
                    Integer end = edge.getEndPoint().getIndex();

                    String edgeWithLabel = "e".concat(" ").concat(start.toString()).concat(" ").concat(end.toString()).
                            concat(" ").concat(String.valueOf(weight)).concat("\n");
                    data.add(edgeWithLabel);
                    /**print each edge with start and end and its label, which is represented as a number as well.
                     * e.g. "e 0 1 3"**/
                    //System.out.print(edgeWithLabel);
                }


                graphID++;
            }


            fw = new FileWriter(fileName);
            bw = new BufferedWriter(fw);

            Iterator<String> stringIterator = data.iterator();
            while (stringIterator.hasNext()){

                String string = stringIterator.next();
                bw.write(string);
            }


            System.out.print("Done");

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

    /**Read the output of Parsemis (i.e. frequent subgraphs), and create the corresponding graphs**/
    /**
     * @param fileName output of Parsemis
     * @return corresponding frequent sub-graphs
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
                    Vertex vertex = new Vertex(vertexIndex,vertexLabel, RandomStringUUID.createUUID());
                    graph.addVertex(vertex);
                    graph.addIndexedVertex(vertex);

                } else if (line.substring(0, 1).equals("e")) {
                    Map<Integer,Vertex> allIndexedVertex = graph.getAllIndexedVertex();
                    startPointIndex = Integer.parseInt(line.split(" ")[1]);
                    endPointIndex = Integer.parseInt(line.split(" ")[2]);
                    Vertex start = allIndexedVertex.get(startPointIndex);
                    Vertex end = allIndexedVertex.get(endPointIndex);
                    edgeWeight = Integer.parseInt(line.split(" ")[3]);
                    Edge edge = new Edge(start, end, edgeWeight, RandomStringUUID.createUUID());
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


}