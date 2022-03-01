package ESM;

import au.com.d2dcrc.yago2es.SubgraphMatcher;
import edu.isi.karma.rep.alignment.*;
import edu.isi.karma.util.RandomGUID;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import org.jgrapht.graph.DirectedWeightedMultigraph;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class ESMMatcher {


    /**convert a directed weighted multi-graph into the ESM Format*
     * @revised 19 Jan 2019
     * @param directedWeightedMultigraph directed weighted multi-graph
     *                                   @param tag tag - "sub" or "data"
     *  "SUB" or "DATA", which is a tag in an ESM vertex
     * */
    public static DirectedSparseGraph<Vertex,Edge> convertESMGraph (DirectedWeightedMultigraph<Node, LabeledLink> directedWeightedMultigraph, String tag) {

        DirectedSparseGraph<Vertex, Edge> g = new DirectedSparseGraph<>();
        Set<Node> nodeSet = directedWeightedMultigraph.vertexSet();
        Set<LabeledLink> labeledLinkSet = directedWeightedMultigraph.edgeSet();

        Map<String, Vertex> tokenToNode = new HashMap<>();//Set up a new hash map for all the vertices in the converted new graph. Key: index of vertex,
        // and the value is the vertex used in ESM Graph

        Iterator<Node> iterator = nodeSet.iterator();
        while (iterator.hasNext()) {
            Node node = iterator.next();
            String index = node.getId();//get id
            Vertex vertex_ESM = new Vertex(Integer.parseInt(index), tag);
            vertex_ESM.setLemma(node.getUri());//lemma = uri
            vertex_ESM.setGeneralizedPOS("NN");
            vertex_ESM.setCompareForm(node.getUri().concat(" nn"));
            g.addVertex(vertex_ESM);
            tokenToNode.put(index, vertex_ESM);
        }

        Iterator<LabeledLink> it = labeledLinkSet.iterator();
        while (it.hasNext()) {

            LabeledLink labeledLink = it.next();
            String sourceIndex = labeledLink.getSource().getId();
            String targetIndex = labeledLink.getTarget().getId();
            String edgeUri = labeledLink.getUri();
            Edge edge_ESM = new Edge (tokenToNode.get(sourceIndex), edgeUri, tokenToNode.get(targetIndex));
            g.addEdge(edge_ESM, tokenToNode.get(sourceIndex), tokenToNode.get(targetIndex));
        }

        return g;
    }

    /**Load serialized graph file, then get all of the sub-graph isomorphisms from pattern to data graph*
     * @param fileDirectory a directory to the pattern and data graph
     * @param subGraphFileName file name of sub-graph
     * @param graphFileName file name of data graph
     * @param startNodeSpecified if start node is specified or not
     * @param subgraphAnchorLemmas a set of anchor lemmas (label) in query graph
     * @param graphConnectingNodes a set of graph connecting nodes, each of set of graph connecting nodes correspond to an anchor in query graph
     * @from 26 Jan 2019
     * @revised 12 Feb 2019, 15 Feb 2019, 25 Feb 2019
     * @return all of the sub-graph isomorphisms
     * */
    public static List<Map<Vertex,Vertex>> getMatchResults (String fileDirectory, String subGraphFileName, String graphFileName, boolean startNodeSpecified,
                                                            Set<String> subgraphAnchorLemmas, Map<String,Set<Integer>> graphConnectingNodes) {

        DirectedWeightedMultigraph<Node, LabeledLink> bigGraph = loadDirectedWeightedMultipGraph(fileDirectory, graphFileName);/**load the data graph**/
        DirectedWeightedMultigraph<Node, LabeledLink> patternGraph = loadDirectedWeightedMultipGraph(fileDirectory, subGraphFileName);/**load the pattern graph**/
        System.out.println("The big graph and the pattern graph have been loaded! ");
        List<Map<Vertex,Vertex>> matchResults = new ArrayList<>();
        try {
            long start = System.currentTimeMillis();
            matchResults = getMatchResults(patternGraph,bigGraph,startNodeSpecified,subgraphAnchorLemmas,graphConnectingNodes);
            long elapsedTimeMills = System.currentTimeMillis() - start;
            float elapsedTimeSec = elapsedTimeMills/1000F;
            System.out.println("the elapsed time for ranking is: " + elapsedTimeSec + " s!");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return matchResults;
    }

    /**Get all of the sub-graph isomorphisms from the pattern graph to data graph*
     * @from 26 Jan 2019
     * @revised 25 Feb 2019
     * @param patternGraph pattern graph
     *@param bigGraph  data graph
     * @param startNodeSpecified if start node is specified automatically or not
     * @param subgraphAnchorLemmas a set of anchor lemmas (label) in query graph
     * @param graphConnectingNodes a set of graph connecting nodes, each of set of graph connecting nodes correspond to an anchor in query graph
     * */
    public static List<Map<Vertex,Vertex>> getMatchResults (DirectedWeightedMultigraph<Node, LabeledLink> patternGraph, DirectedWeightedMultigraph<Node, LabeledLink> bigGraph,
                                                            boolean startNodeSpecified, Set<String> subgraphAnchorLemmas, Map<String,Set<Integer>> graphConnectingNodes) throws Exception {

        DirectedSparseGraph<Vertex, Edge> esmPatternGraph = convertESMGraph(patternGraph, "PATTERN");
        DirectedSparseGraph<Vertex, Edge> esmBigGraph = convertESMGraph(bigGraph, "DATA");
        SubgraphMatcher esmMatcher = new SubgraphMatcher();

        long start = System.currentTimeMillis();
        int result = esmMatcher.isGraphIsmomorphism(esmPatternGraph, esmBigGraph);//0 means the subgraph isn't subsumed to the graph. '1' means the subgraph is subsumed to the graph
        System.out.println("result: " + result);

        if (result == 1) {
            System.gc();//force garbage collection
            ESM esm = new ESM (esmPatternGraph, esmBigGraph);

            if (startNodeSpecified) {

                String startNodeLabel = getLeastFrequentNode(patternGraph,bigGraph);//get the start node of the pattern
                System.out.println("the specified start node label is: " + startNodeLabel);

                /**Get the start node for the sub-graph, added on 2019.02.08**/
                Vertex subGraphStartNode = new Vertex();
                List<Vertex> allVerticesInPattern = new ArrayList<Vertex>(esmPatternGraph.getVertices()) ;
                Iterator iterator = allVerticesInPattern.iterator();
                while (iterator.hasNext()) {
                    Vertex vertex = (Vertex) iterator.next();
                    if (vertex.getLemma().equals(startNodeLabel)) {
                        subGraphStartNode = vertex;
                    }
                }

                /**Get all the start nodes in the data graph, added on 2019.02.08**/
                List<Vertex> graphStartNodes = new ArrayList<Vertex>();
                List<Vertex> allVerticesInData = new ArrayList<>(esmBigGraph.getVertices()) ;
                Iterator iterator1 = allVerticesInData.iterator();
                while (iterator1.hasNext()) {
                    Vertex vertex = (Vertex) iterator1.next();
                    if (vertex.getLemma().equals(startNodeLabel)) {
                        graphStartNodes.add(vertex);
                    }
                }

                esm.setSubgraphStartNode(subGraphStartNode);//Instead of the default random start node, we specify the start node for the sub-graph
                esm.setGraphStartNodes(graphStartNodes);//choosing the set of start nodes for the data graph instead of using all the nodes in the data graph as start nodes
            }

            /**added on 25 Feb 2019**/
            if (!subgraphAnchorLemmas.isEmpty()) {
                for (String subgraphAnchorLemma : subgraphAnchorLemmas) {
                    esm.addSubgraphAnchorLemma(subgraphAnchorLemma);
                }
            }

            /**added on 25 Feb 2019**/
            if (!graphConnectingNodes.isEmpty()) {
                Iterator it = graphConnectingNodes.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry pair = (Map.Entry) it.next();
                    String anchorLemma = (String) pair.getKey();
                    Set<Integer> connectingNodePosSet = (Set<Integer>) pair.getValue();
                    esm.addGraphConnectingNodes(anchorLemma, connectingNodePosSet);
                }
            }

            List<Map<Vertex, Vertex>> matchResults = esm.getSubgraphMatchingMatches();
            long elapsedTimeMills = System.currentTimeMillis() - start;
            float elapsedTimeSec = elapsedTimeMills/1000F;
            System.out.println("the elapsed time for matching is: " + elapsedTimeSec + " s!");
            if (!matchResults.isEmpty()) {
                for (Map<Vertex, Vertex> match : matchResults) {
                    System.out.println("the matching pairs are: ");
                    Iterator it = match.entrySet().iterator();
                    while (it.hasNext()) {
                        Map.Entry pair = (Map.Entry)it.next();
                        Vertex sub = (Vertex) pair.getKey();//get the node in the sub-graph
                        Vertex data = (Vertex) pair.getValue();//get the node in the data-graph
                        System.out.println("Node id in the pattern graph " + sub.getPos() + " ----- node id in the KG " + data.getPos() );
                    }
                }
                System.out.println("the # of occurrences is: " + matchResults.size());
            }
            return matchResults;

        } else {
            throw new Exception("There is not any sub-graph matching at all!");
        }
    }



    /**Search for the node which occurs least frequently in the big graph*
     * @from 9 Feb 2019
     * @param patternGraph pattern graph
     * @param dataGraph data graph
     * @return the label of a node, which occurs least in the data graph, from the pattern graph
     * */
    private static String getLeastFrequentNode (DirectedWeightedMultigraph<Node, LabeledLink> patternGraph, DirectedWeightedMultigraph<Node, LabeledLink> dataGraph) {
        String leastFreqNode = "";
        int leastFreq = Integer.MAX_VALUE;
        Set<Node> allNodesInPattern = patternGraph.vertexSet();
        Set<Node> allNodesInData = dataGraph.vertexSet();

        Iterator<Node> iterator = allNodesInPattern.iterator();
        while (iterator.hasNext()) {
            Node nodeInPatternGraph = iterator.next();
            int freq = 0;
            for (Node nodeInDataGraph : allNodesInData) {
                if (nodeInPatternGraph.getUri().equals(nodeInDataGraph.getUri())) {
                    freq++;
                }
            }
            if (freq < leastFreq) {
                leastFreq = freq;
                leastFreqNode = nodeInPatternGraph.getUri();
            }
        }

        return leastFreqNode;
    }


    /**Read a serialized file and transform it to directed weighted multiple graph which can be consumed by Karma *
     * @From 13 Jan 2019
     * @param url the file url
     * @param fileName a file to be loaded
     * @return directed weighted multi-graph
     * */
    public static DirectedWeightedMultigraph<Node, LabeledLink> loadDirectedWeightedMultipGraph (String url, String fileName) {

        DirectedWeightedMultigraph<Node, LabeledLink> directedWeightedMultigraph = new DirectedWeightedMultigraph<>(LabeledLink.class);
        String line;
        String nodeIndex;
        String nodeLabel;
        String sourceIndex;
        String targetIndex;
        String relationshipUri;

        try {
            FileReader fileReader = new FileReader(url.concat(fileName));
            BufferedReader br = new BufferedReader(fileReader);
            Map<String, Node> nodeMap = new HashMap<>();//key: node id, value: node

            while ((line = br.readLine()) != null) {
                if (line.substring(0,1).equals("t")) {
                    continue;
                } else if (line.substring(0,1).equals("v")) {
                    nodeIndex = line.split(" ")[1];//get the node id
                    nodeLabel = line.split(" ")[2];//get the node label
                    Node node = new InternalNode(nodeIndex, new Label(nodeLabel));//create a new node
                    nodeMap.put(nodeIndex, node);
                    directedWeightedMultigraph.addVertex(node);
                } else if (line.substring(0,1).equals("e")) {
                    sourceIndex = line.split(" ")[1];
                    targetIndex = line.split(" ")[2];
                    relationshipUri = line.split(" ")[3];
                    Node source = nodeMap.get(sourceIndex);//get the source
                    Node target = nodeMap.get(targetIndex);//get the target
                    LabeledLink labeledLink = new ObjectPropertyLink(new RandomGUID().toString(), new Label(relationshipUri), ObjectPropertyType.Direct);
                    directedWeightedMultigraph.addEdge(source,target,labeledLink);
                }
            }
            br.close();
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return directedWeightedMultigraph;
    }

    /**test if ESM algorithm works for us**
     * @From 14 Jan 2019
     */
    public static void main (String args []) {

        Set<String> subgraphAnchorLemmas = new HashSet<>();
        //subgraphAnchorLemmas.add("2");//2-Person
        //Set<Integer> connectingNodePosSet = new HashSet<>();
//        connectingNodePosSet.add(3);
//        connectingNodePosSet.add(7);
//        connectingNodePosSet.add(8);
        Map<String,Set<Integer>> graphConnectingNodes = new HashMap<>();
        //graphConnectingNodes.put("2",connectingNodePosSet);
        List<Map<Vertex,Vertex>> allIsomorphisms =  getMatchResults(Settings.KGEvaluationAddress, "ST6.lg", "kg20190120_N10000.lg", true, subgraphAnchorLemmas,
                graphConnectingNodes);
        System.out.println("done!");

    }
}
