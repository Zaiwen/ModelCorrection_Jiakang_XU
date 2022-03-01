package au.com.d2dcrc.yago2es;

import org.elasticsearch.index.fielddata.RamAccountingTermsEnum;

import java.util.*;

/**
 * Created by Zaiwen Feng on 5/05/2017.
 *
 * This class is used to create and slice linked entity graph
 */
public class LinkedEntityGraph {

    /**Create linked data graph**/
    /**
     *
     * @return Linked entity graph
     */
    public static Graph create(){

        String file_path = "/Users/fengz/Documents/Data_Modeling/Karma(from_9_Nov_2016)/RDF-Graph@20April17/linked_data_example.ttl";

        Triple.printTriples(file_path);

        List<Triple> result = Triple.parseTriples(file_path);

        Graph linkedGraph = new Graph(true);

        linkedGraph.createLinkedGraph(result,linkedGraph);

        return linkedGraph;

    }

    /**Create a linked data graph from a specified turtle file
     * @param fileURL turtle formatted RDF file
     * @return Linked entity graph
     */
    public static Graph create(String fileURL){

        String file_path = fileURL;

        Triple.printTriples(file_path);

        List<Triple> result = Triple.parseTriples(file_path);

        Graph linkedGraph = new Graph(true);

        linkedGraph.createLinkedGraph(result,linkedGraph);

        return linkedGraph;

    }


    /**Load RDF of an external data source to the Linked Entity RDF**/
    /**
     *
     * @param bigGraph Current LOD graph
     * @param externalGraph linked entity graph for the external structured data source
     * @return Merged linked entity graph
     */
//    public static Graph merge (Graph bigGraph, Graph externalGraph) {
//
//        /**-------Merge the vertices in the external linked entity graphs into the big (current) linked entity graph------**/
//        /**Iterate each vertex in the external linked entity graph**/
//        Iterator<Vertex> it = externalGraph.getAllVertices().iterator();
//        while (it.hasNext()){
//
//            Vertex vertexInExternalGraph = it.next();
//
//            /**Iterate each vertex in the big (current) linked entity graph**/
//            Iterator<Vertex> iterator = bigGraph.getAllVertices().iterator();
//            while (iterator.hasNext()){
//
//                Vertex vertexInBigGraph = iterator.next();
//
//                /**Compare vertex in the external linked entity graph with each vertex in big graph**/
//                /**Condition1: the labels of two vertices should be equivalent**/
//                if(vertexInExternalGraph.getLabel().equals(vertexInBigGraph.getLabel())){
//
//                    Map<String, String> attributesMap1 = vertexInExternalGraph.getAttributes();
//                    Map<String, String> attributesMap2 = vertexInBigGraph.getAttributes();
//
//                    /**Condition2: all of the attributes should be equivalent**/
//                    if (Util.equalMaps(attributesMap1, attributesMap2)){
//
//                        /**below**Merge 'vertexInExternalGraph' to 'vertexInBigGraph' IMPORTANT!!!**/
//                        List<Edge> outgoingEdges = vertexInExternalGraph.getOutgoingEdges();
//                        List<Vertex> adjacentVertex = vertexInExternalGraph.getAdjacentVertexes();
//                        List<Edge> incomingEdges = vertexInExternalGraph.getIncomingEdges();
//                        List<Vertex> preAdjacentVertex = vertexInExternalGraph.getPreAdjacentVertex();
//
//
//
//
//
//                        /**above**Merge 'vertexInExternalGraph' to 'vertexInBigGraph' IMPORTANT!!!**/
//                    }
//                }
//
//
//            }
//        }
//
//
//
//
//
//        return bigGraph;
//    }

    /**Create boundary graphs from an linked entity graph. Search starts from the (object link) vertices with a special attribute value*
     /**
     *
     * @param linkedGraph linked entity graph to be sliced
     * @param rootLabel label of object linking vertex that graph search will begin with, for example, OL_PL
     * @param attributeName name of attribute that contains relationship between recognized entity instances, for example, OL_PL_RT (To be removed)
     * @param attributeValue relationship between recognized entity instances, for example, buy_car_at
     * @param maxLength maximum searching length that far from the child or parent of root object linking vertex
     * @return boundary graphs
     */
    public static List<Graph> slicingLinkedGraph(Graph linkedGraph, String rootLabel, String attributeName, String attributeValue, int maxLength){

        /**Create a list to save boundary graph**/
        List<Graph> boundaryGraphs = new ArrayList<Graph>();

        /**Get all the object linking vertex with label 'OL_PL1' and the attribute value 'Buy_car_at' for attribute name 'OL_PL_RT'
         * and put them into a HashMap<Key, Value>**/
        Map<Vertex, Vertex> edgeWithRT = new HashMap<Vertex, Vertex>();

        Iterator<Vertex> iterator = linkedGraph.getAllVertex().iterator();
        while(iterator.hasNext()){

            Vertex vertex = iterator.next();

            if((vertex.getIsObjectLink()==true) && (vertex.getLabel().equals(rootLabel))){

                Map<String,String> attributes = vertex.getAttributes();

                if(attributes.get(attributeName).equals(attributeValue)){

                    Vertex root = vertex;

                    /**Get the parent and child of this root**/
                    List<Vertex> children = root.getAdjacentVertexes();
                    List<Vertex> parents = root.getPreAdjacentVertex();

                    Vertex child = children.get(0);//Value
                    Vertex parent = parents.get(0);//Key

                    /**Key: parent of the root; Value: child of the root**/
                    edgeWithRT.put(parent,child);

                }

            }
        }

        /**Remove the object linking vertex with 'OL_PL1' and attribute name 'OL_PL_RT'**/
        //linkedGraph.removeAuxiliaryVertex(rootLabel,attributeName);//root vertex shouldn't be removed from linked entity graph from 24 May 2017


        /**Loop the edge with a specified Relationship Type**/
        Iterator iterator1 = edgeWithRT.entrySet().iterator();
        while (iterator1.hasNext()){

            Map.Entry pair = (Map.Entry)iterator1.next();
            Vertex parent = (Vertex)pair.getKey();
            Vertex child = (Vertex)pair.getValue();

            /**set all of the vertices & edges in the linked data graph unvisited**/
            linkedGraph.setAllVertexUnvisited();
            linkedGraph.setAllEdgesUnvisited();
            List<Vertex> results1 = linkedGraph.dfs(child,maxLength);

            /**set all of the vertices & edges in the linked data graph unvisited**/
            linkedGraph.setAllVertexUnvisited();
            linkedGraph.setAllEdgesUnvisited();
            List<Vertex> results2 = linkedGraph.dfs(parent,maxLength);

            /**merge**/
            results1.removeAll(results2);
            results1.addAll(results2);

            //create a new vertex list
            List<Vertex> vertexList = new ArrayList<Vertex>();

            /**clone each vertex in 'result1' returned from dfs algorithm, but remove 'outgoingEdges', 'adjacentVertex', 'incomingEdges' and 'preAjacentVertex' of each vertex**/
            Iterator<Vertex> iterator2 = results1.iterator();
            while (iterator2.hasNext()){

                Vertex v = iterator2.next();

                Vertex clonedVertex = new Vertex(v.getId());

                /**clone properties of vertex from **/
                clonedVertex.setData(v.getLabel());
                clonedVertex.setAttributes(v.getAttributes());
                clonedVertex.setIdentificationCode(v.getIdentificationCode());
                clonedVertex.setIsObjectLink(v.getIsObjectLink());
                clonedVertex.setVisited(v.getVisited());
                clonedVertex.setLength(v.getLength());

                /**set empty attributes for 'outgoingEdges', 'adjacentVertex', 'incomingEdges' and 'preAjacentVertex'**/
                clonedVertex.setPreAdjacentVertex(new ArrayList<Vertex>());
                clonedVertex.setIncomingEdges(new ArrayList<Edge>());
                clonedVertex.setAdjacentVertex(new ArrayList<Vertex>());
                clonedVertex.setOutgoingEdges(new ArrayList<Edge>());

                vertexList.add(clonedVertex);
            }

            /**create vertex-induced subgraph (boundary graph)**/
            Graph g = linkedGraph.vertexInducedSubgraph(vertexList);

            boundaryGraphs.add(g);
        }
        return boundaryGraphs;
    }

    /**Create boundary graphs from an linked entity graph. Search starts from the edges with a special attribute value
     * This function is used on Yago dataset
     *  @param linkedGraph linked entity graph to be sliced,
     *  @param edgeLabel the label of a special edge. Slicing boundary graphs is at the center of this edge,
     *  @param maxLength maximum searching length that far from the starting point and ending point of this edge,
     *  @return boundary graphs
     * **/
    public static List<Graph> slicingLinkedGraph(Graph linkedGraph, String edgeLabel, int maxLength) {

        /**Create a list to save boundary graph**/
        List<Graph> boundaryGraphs = new ArrayList<Graph>();

        /**'edgeWithRT' are the edges on which the label is specified, for example, 'visits', 'shopping_at'**/
        Map<Vertex, Vertex> edgeWithRT = new HashMap<Vertex, Vertex>();

        Iterator<Edge> iterator = linkedGraph.getAllEdges().iterator();
        while (iterator.hasNext()) {

            Edge edge = iterator.next();

            if (edge.getLabel().equals(edgeLabel)) {

                /**Get the starting point and ending point of the edge**/
                Vertex start = edge.getStartPoint();
                Vertex end = edge.getEndPoint();

                /**Key: start of this edge; Value: end of this edge**/
                edgeWithRT.put(start,end);
            }


        }

        int i = 1;
        /**Loop the edge with a specified Relationship Type**/
        Iterator iterator1 = edgeWithRT.entrySet().iterator();
        while (iterator1.hasNext()){



            Map.Entry pair = (Map.Entry)iterator1.next();
            Vertex parent = (Vertex)pair.getKey();
            Vertex child = (Vertex)pair.getValue();

            /**set all of the vertices & edges in the linked data graph unvisited**/
            linkedGraph.setAllVertexUnvisited();
            linkedGraph.setAllEdgesUnvisited();
            /**Get all the vertices withtin the scope**/
            List<Vertex> results1 = linkedGraph.dfs(child,maxLength);

            /**set all of the vertices & edges in the linked data graph unvisited**/
            linkedGraph.setAllVertexUnvisited();
            linkedGraph.setAllEdgesUnvisited();
            /**Get all the vertices within the scope**/
            List<Vertex> results2 = linkedGraph.dfs(parent,maxLength);

            /**merge**/
            results1.removeAll(results2);
            results1.addAll(results2);

            //create a new vertex list
            List<Vertex> vertexList = new ArrayList<Vertex>();

            /**clone each vertex in 'result1' returned from dfs algorithm, but remove 'outgoingEdges', 'adjacentVertex', 'incomingEdges' and 'preAjacentVertex' of each vertex**/
            Iterator<Vertex> iterator2 = results1.iterator();
            while (iterator2.hasNext()){

                Vertex v = iterator2.next();

                Vertex clonedVertex = new Vertex(v.getId());

                /**clone properties of vertex from **/
                clonedVertex.setData(v.getLabel());
                clonedVertex.setAttributes(v.getAttributes());
                clonedVertex.setIdentificationCode(v.getIdentificationCode());
                clonedVertex.setIsObjectLink(v.getIsObjectLink());
                clonedVertex.setVisited(v.getVisited());
                clonedVertex.setLength(v.getLength());

                /**set empty attributes for 'outgoingEdges', 'adjacentVertex', 'incomingEdges' and 'preAjacentVertex'**/
                clonedVertex.setPreAdjacentVertex(new ArrayList<Vertex>());
                clonedVertex.setIncomingEdges(new ArrayList<Edge>());
                clonedVertex.setAdjacentVertex(new ArrayList<Vertex>());
                clonedVertex.setOutgoingEdges(new ArrayList<Edge>());

                vertexList.add(clonedVertex);
            }

            /**create vertex-induced subgraph (boundary graph)**/
            Graph g = linkedGraph.vertexInducedSubgraph(vertexList);
            System.out.println("We have created " + i + " boundary graph. ");

            i++;



            //if (i > 18) {break;}

            boundaryGraphs.add(g);
        }


        return boundaryGraphs;
    }


    /**Create a Yago boundary graph in terms of yago Lists*
     * @param yagoFact yago fact
     * @param  factsList fact list
     * @param maxLength max length
     * @param maxDegree max degree
     * @return  a graph
     * */
    public static Graph createYagoBoundaryGraph (YagoFact yagoFact, List<YagoFact> factsList, int maxLength, int maxDegree) {

        Graph boundaryGraph = new Graph(true);

        /**Get the initial graph based on yagoFact**/
        String subject = yagoFact.getSubject();
        String predicate = yagoFact.getPredicate();
        String object = yagoFact.getObject();

        Vertex start = new Vertex(RandomStringUUID.createUUID());
        start.setInstanceName(subject);//assign instance name to start point
        boundaryGraph.addVertex(start);

        Vertex end = new Vertex(RandomStringUUID.createUUID());
        end.setInstanceName(object);//assign instance name to end point
        boundaryGraph.addVertex(end);

        Edge edge = new Edge(start,end,true,"X",RandomStringUUID.createUUID());
        boundaryGraph.addEdge(edge);
        start.addAdjacentVertex(edge, end);
        end.addPreAdjacentVertex(edge,start);

        boundaryGraph.setLeftAnchorPoint(start);//set the left anchor point
        boundaryGraph.setRightAnchorPoint(end);//set the right anchor point

        /**Extend the boundary graph in terms of 'maxLength'**/
        for (int i = 1; i <= maxLength; i++) {

            System.out.println("The No. " + i + " hop");
            extendYagoBoundaryGraph(boundaryGraph, factsList, maxDegree);
        }



        return boundaryGraph;


    }

    /**Create a Yago boundary graph in terms of yagoFacts.ttl*
     * @param yagoFact the initial central fact of this boundary graph.
     * @param filePath the file path for yagoFacts.ttl,
     * @param maxLength the maximum length for searching, for example, 2 or 3.
     * @param maxDegree the maximum degree of the vertices in the graph. This parameter is used to control scale of the boundary graph
     * @return boundary graph at the center of the yagoFact
     * */
    public static Graph createYagoBoundaryGraph (YagoFact yagoFact, String filePath, int maxLength, int maxDegree) {

        Graph boundaryGraph = new Graph(true);

        /**Load all of the Yago facts into an array list**/
        final YagoTtlParser parser = new YagoTtlParser();
        List<YagoFact> factsList = parser.parseYagoFacts(filePath);

        /**Get the initial graph based on yagoFact**/
        String subject = yagoFact.getSubject();
        String predicate = yagoFact.getPredicate();
        String object = yagoFact.getObject();

        Vertex start = new Vertex(RandomStringUUID.createUUID());
        start.setInstanceName(subject);//assign instance name to start point
        boundaryGraph.addVertex(start);

        Vertex end = new Vertex(RandomStringUUID.createUUID());
        end.setInstanceName(object);//assign instance name to end point
        boundaryGraph.addVertex(end);

        Edge edge = new Edge(start,end,true,predicate,RandomStringUUID.createUUID());
        boundaryGraph.addEdge(edge);
        start.addAdjacentVertex(edge, end);
        end.addPreAdjacentVertex(edge,start);

        /**Extend the boundary graph in terms of 'maxLength'**/
        for (int i = 1; i <= maxLength; i++) {

            System.out.println("The No. " + i + " hop");
            extendYagoBoundaryGraph(boundaryGraph, factsList, maxDegree);
        }



        return boundaryGraph;
    }

    /**Evolve the Yago boundary graph in terms of yagoFacts.ttl for ONE hop*
     * @param initialBG the initial boundary graph
     * @param yagoFactList the comprehensive list of yago facts
     * @param maxDegree the maximum degree of the vertices in the graph. This parameter is used to control scale of the boundary graph
     * @return the evolved graph
     * */
    public static Graph extendYagoBoundaryGraph (Graph initialBG, List<YagoFact> yagoFactList, int maxDegree) {

        /**Get all of the unvisited vertices of the initial BG**/
        List<Vertex> allVertices = initialBG.getAllVertices();

        int i = 1;

        Iterator<Vertex> iterator = allVertices.iterator();
        while (iterator.hasNext()){

            System.out.println("Searching incoming and outgonging edges for the No. " + i + " vertex");

            Vertex vertex = iterator.next();
            //if this vertex has not been visited
            if (vertex.getVisited() == false) {

               /**Get the instance name of this vertex**/
                String instanceName = vertex.getInstanceName();//instance name for a vertex is unique

                System.out.println("The instance name of current vertex is: " + instanceName);

                List<YagoFact> allPersonsWinPulitzer = new ArrayList<>();//just for test

                /**Get all of the pre-adjacent and adjacent vertices of this vertex* */
                for (YagoFact yagoFact : yagoFactList) {

                    String subject = yagoFact.getSubject();
                    String predicate = yagoFact.getPredicate();
                    String object = yagoFact.getObject();

                    /**Filter out some useless facts**/
                    if ((!predicate.equals("hasGender"))) {

                        /**If the newly added vertex is pre-adjacent to this vertex, then prepend the newly added vertex to this vertex**/
                        if (object.equals(instanceName)) {

                            boolean flag = false;//false means 'v_p' does not exist in the current boundary graph

                            /**If 'v_p' exists in the existing boundary graph, then just add an edge between 'v_p' and 'vertex'**/
                            List<Vertex> allExistedVertices = initialBG.getAllVertices();
                            for (Vertex existedVertex : allExistedVertices) {

                                if (existedVertex.getInstanceName().equals(subject)) {

                                    flag = true;//true means 'v_p' exists in the current boundary graph

                                    /**If there is not current relationship type named 'predicate' or 'X' on the current edge, then add a new edge
                                     * with the relation type named 'predicate'**/
                                    if ((initialBG.getEdges(existedVertex.getId(), vertex.getId()).size() == 0) ||
                                            ((initialBG.getEdges(existedVertex.getId(), vertex.getId()).size() > 0) &&
                                                    (!initialBG.getEdgeLabels(existedVertex.getId(), vertex.getId()).contains(predicate)) &&
                                                    (!initialBG.getEdgeLabels(existedVertex.getId(), vertex.getId()).contains("X")))){

                                        /**create a new edge between 'v_p' and 'vertex'**/
                                        Edge edge = new Edge(existedVertex,vertex,true,predicate,RandomStringUUID.createUUID());
                                        initialBG.addEdge(edge);


                                    }
                                }


                            }

                            /**If 'v_p' does not exist in the exists in the existing boundary graph, then append a new vertex to 'vertex'**/
                            if(!flag) {

                                /**Create a new vertex. The instance name of this vertex is equal to 'subject'**/
                                Vertex v_p = new Vertex(RandomStringUUID.createUUID());
                                v_p.setInstanceName(subject);

                                /**Prepend 'v_p' to 'vertex', the label of the edge is equal to 'predicate'**/
                                initialBG.prependVertex(vertex, v_p, predicate);
                            }

                            allPersonsWinPulitzer.add(yagoFact);//just for test

                        }
                        /**If the newly added vertex is adjacent to this vertex, then append that newly added vertex to this vertex**/
                        else if (subject.equals(instanceName)) {

                            boolean flag = false;//false means 'v_s' does not exist in the current boundary graph

                            /**If 'v_s' already exists in the current boundary graph, then just add an edge between 'vertex' and 'v_s'**/
                            List<Vertex> allExistedVertices = initialBG.getAllVertices();
                            for (Vertex existedVertex : allExistedVertices) {

                                if (existedVertex.getInstanceName().equals(object)) {

                                    flag = true; //true means 'v_s' exists in the current boundary graph

                                    /**If there is not current relationship type named 'predicate' or 'X' on the current edge, then add a new edge
                                     * with the relation type named 'predicate'**/
                                    if ((initialBG.getEdges(vertex.getId(),existedVertex.getId()).size() == 0) ||
                                            ((initialBG.getEdges(vertex.getId(),existedVertex.getId()).size() > 0) &&
                                                    (!initialBG.getEdgeLabels(vertex.getId(),existedVertex.getId()).contains(predicate)) &&
                                                    (!initialBG.getEdgeLabels(vertex.getId(), existedVertex.getId()).contains("X")))){

                                        /**create a new edge between 'vertex' and 'v_s'**/
                                        Edge edge = new Edge(vertex,existedVertex,true,predicate,RandomStringUUID.createUUID());
                                        initialBG.addEdge(edge);


                                    }

                                }
                            }

                            /**If 'v_p' does not exist in the exists in the existing boundary graph, then append a new vertex to 'vertex'**/
                            if(!flag) {

                                /**Create a new vertex. The instance name of this vertex is equal to 'object'**/
                                Vertex v_s = new Vertex(RandomStringUUID.createUUID());
                                v_s.setInstanceName(object);

                                /**Append 'v_s' to 'vertex', the label of the edge is equal to 'predicate'**/
                                initialBG.appendVertex(vertex,v_s,predicate);
                            }

                            allPersonsWinPulitzer.add(yagoFact);//just for test

                        }

                        /**filter out some vertex with more than 300 incoming/outging edges **/
                        if ((vertex.getIncomingEdges().size() + vertex.getOutgoingEdges().size()) > maxDegree) {

                            break;
                        }

                    }

                }
            }

            /**Set this vertex visited**/
            vertex.setVisited(true);

            /**Computing all the incoming edges of this vertex**/
            int numberOfIncomingEdges = vertex.getIncomingEdges().size();

            /**Computing all the outgoing edges of this vertex**/
            int numberOfOutgoingEdges = vertex.getOutgoingEdges().size();

            System.out.println("the number of incoming edges is: " + numberOfIncomingEdges);
            System.out.println("the number of outgoing edges is: " + numberOfOutgoingEdges);

            i++;
        }
        return initialBG;
    }

    /**Numerate the vertex labels of a set of boundary graphs, used in Yago
     * @param graphs a set of graphs (boundary graphs)*
     * @return  A map.
     * Key is the non-duplicated labels for vertices of the graph
     * Value is assigned number for this label from 0,1,2,...*/
    public static Map<String, Integer> numerateVertexLables(List<Graph> graphs){

        Map<String, Integer> numeratedVertexLabels = new HashMap<>();

        /**create a new list to contain the labels of all the vertices**/
        List<String> allVertexLabel = new ArrayList<String>();

        for(Graph graph : graphs) {

            Collection<Vertex> allVertices = graph.getAllVertex();
            Iterator<Vertex> iterator = allVertices.iterator();
            while (iterator.hasNext()){

                Vertex vertex = iterator.next();
                allVertexLabel.add(vertex.getLabel());

            }

        }

        /**Remove the repeated elements from 'functionalVertexLabel'**/
        Set<String> allVertexLabelSet = new HashSet<String>();
        allVertexLabelSet.addAll(allVertexLabel);

        /**Numerate the labels in the 'allVertexLabel'**/
        int vertexNumber = 0; //number for the labels of vertices
        Iterator<String> it = allVertexLabelSet.iterator();
        while (it.hasNext()){

            String vertexLabel = it.next();
            Integer vertexIntegerNumber = new Integer(vertexNumber);

            /**Put 'vertexLabel' and 'vertexIntegerNumber' into 'numeratedLabels'**/
            numeratedVertexLabels.put(vertexLabel,vertexIntegerNumber);
            vertexNumber++;
        }



        return numeratedVertexLabels;
    }


    /**Numerate the edge labels of a set of graphs, used in Yago*
     *@param graphs a set of boundary graphs
     *@param positiveRelationship positive relation
     *@param negativeRelationship negative relation
     * @return A map. Key: the non-repeated labels for edges of the graph, Value: assigned number for this label from 0,1,2,...
     */
    public static Map<String, Integer> numerateEdgeLables(List<Graph> graphs, String positiveRelationship, String negativeRelationship){

        Map<String, Integer> numeratedEdgeLabels = new HashMap<>();

        /**create a new list to contain all of label of edges**/
        List<String> edgeLabel = new ArrayList<String>();

        for(Graph graph : graphs) {

            Collection<Edge> allEdges = graph.getAllEdges();
            Iterator<Edge> iterator = allEdges.iterator();
            while (iterator.hasNext()){

                Edge edge = iterator.next();
                edgeLabel.add(edge.getLabel());
            }


        }

        edgeLabel.add(positiveRelationship);//newly added
        edgeLabel.add(negativeRelationship);//newly added

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

    /**Numerate the edge labels of a set of graphs, used in Yago*
     *@param graphs a set of boundary graphs
     *@param relationships central relationships in the boundary graphs
     * @return A map. Key: the non-repeated labels for edges of the graph, Value: assigned number for this label from 0,1,2,...
     */
    public static Map<String, Integer> numerateEdgeLables(List<Graph> graphs, List<String> relationships){

        Map<String, Integer> numeratedEdgeLabels = new HashMap<>();

        /**create a new list to contain all of label of edges**/
        List<String> edgeLabel = new ArrayList<String>();

        for(Graph graph : graphs) {

            Collection<Edge> allEdges = graph.getAllEdges();
            Iterator<Edge> iterator = allEdges.iterator();
            while (iterator.hasNext()){

                Edge edge = iterator.next();
                edgeLabel.add(edge.getLabel());
            }


        }

        edgeLabel.addAll(relationships);

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

}

