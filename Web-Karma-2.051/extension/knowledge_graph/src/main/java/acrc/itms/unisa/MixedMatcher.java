package acrc.itms.unisa;

import ESM.ESMMatcher;
import ESM.Vertex;
import au.com.d2dcrc.GramiMatcher;
import dataStructures.HPListGraph;
import dataStructures.Query;
import edu.isi.karma.rep.alignment.LabeledLink;
import edu.isi.karma.rep.alignment.Node;
import org.jgrapht.graph.DirectedWeightedMultigraph;

import java.util.*;

public class MixedMatcher {


    /**AC-MNI Matcher adapted from GraMi*
     * @revised 14 Feb 2019
     * @param fileDirectory directory to the pattern graph and data graph
     * @param subGraphFileName file name of pattern graph
     * @param graphFileName file name of data graph
     * @param matchingOption 0-tree mode, 1-graph mode
     * @param constrained 0-do not consider anchor constrained match, 1- anchor constrained match
     * @param limitNodeSet limit node label set in pattern graph
     * @param anchorNodeSet anchor label set in pattern graph
     * @return mappings
     * */
    public List<Map<Node,Node>> loadGramiMatcher (String fileDirectory, String subGraphFileName, String graphFileName, int matchingOption,
                                                  int constrained, Set<Integer> limitNodeSet, Set<Integer> anchorNodeSet) {

        DirectedWeightedMultigraph<Node, LabeledLink> bigGraph = ESMMatcher.loadDirectedWeightedMultipGraph(fileDirectory, graphFileName);/**load the data graph**/
        DirectedWeightedMultigraph<Node, LabeledLink> patternGraph = ESMMatcher.loadDirectedWeightedMultipGraph(fileDirectory, subGraphFileName);//load the pattern graph
        List<Map<Node,Node>> matchedResults = new ArrayList<>();
        try {
            matchedResults = loadGramiMatcher(patternGraph, bigGraph, matchingOption, constrained, limitNodeSet, anchorNodeSet);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return matchedResults;
    }


    /**AC-MNI Matcher adapted from GraMi*
     * @created 19 Jan 2019
     * @revised 17,26 Feb 2019
     * @param patternGraph pattern graph
     * @param bigGraph data graph
     * @param matchingOption 0-tree mode, 1-graph mode
     * @param constrained 0-do not consider anchor constrained match, 1- anchor constrained match (AC-MNI)
     * @param limitNodeSet limit node label set in pattern graph
     * @param anchorNodeSet anchor label set in pattern graph
     * @return node mappings
     * */
    public List<Map<Node,Node>> loadGramiMatcher (DirectedWeightedMultigraph<Node,LabeledLink> patternGraph, DirectedWeightedMultigraph<Node,LabeledLink> bigGraph,
                                                  int matchingOption, int constrained, Set<Integer> limitNodeSet, Set<Integer> anchorNodeSet) throws Exception {

        dataStructures.Graph singleGraph = new dataStructures.Graph(5000,Integer.MAX_VALUE);/**Create a big graph, the ID is 5000**/
        Map<Integer, Integer> correspondences = dataStructures.Graph.loadDirectedWeightedMultigraph(bigGraph, singleGraph);/**Load 'bigGraph'(DirectedWeighedMultiGraph) into it**/
        singleGraph.printFreqNodes();//print frequent nodes

        HPListGraph hpListGraph = new HPListGraph("query");/**create pattern graph**/
        HPListGraph.loadDirectedWeightedMultiGraph(patternGraph,hpListGraph);//convert to GraMi Graph
        Query q = new Query((HPListGraph<Integer,Double>)hpListGraph);/**create a query**/
        HashMap<Integer, HashSet<Integer>> nonCandidates = new HashMap<>();//create a hashmap to save pruned variables

        long start = System.currentTimeMillis();
        GramiMatcher gramiMatcher = new GramiMatcher();
        gramiMatcher.setGraph(singleGraph);
        gramiMatcher.setQry(q);
        if (constrained == 1) {
            gramiMatcher.setLimitNodeSet(limitNodeSet);
            gramiMatcher.setAnchorSet(anchorNodeSet);
        } else {
            //the limit node set and anchor set will be empty for the GraMi Matcher!

        }
        gramiMatcher.getFrequency(nonCandidates,matchingOption);
        List<Map<Node, Node>> matchedResults = gramiMatcher.getIsomorphismList();//get all of the isomorphisms
        if (matchingOption == 1) {

            long elapsedTimeMills = System.currentTimeMillis() - start;
            float elapsedTimeSec = elapsedTimeMills/1000F;

            /**update the id of node in big graph. added 17 Feb 2019**/
            for (Map<Node,Node> matchedResult : matchedResults) {
                Iterator it = matchedResult.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry pair = (Map.Entry)it.next();
                    Node nodeInDataGraph = (Node) pair.getValue();
                    Integer indexInGraMiGraph = Integer.parseInt(nodeInDataGraph.getId());
                    Integer originalID = correspondences.get(indexInGraMiGraph);
                    nodeInDataGraph.setId(originalID.toString());//update id
                }
            }

            System.out.println("the elapsed time for ranking is: " + elapsedTimeSec + " s!");
            GramiMatcher.printMatchedNode(matchedResults);//print all of mappings
        } else {
            new Exception("We do not provide isomorphism list because the matching option is not 1!");
        }

        return matchedResults;
    }



    /**Join two lists *
     * @from 26 Jan 2019
     * @param esm sub-graph isomorphism list returned by ESM
     * @param grami sub-graph isomorphism list returned by Grami
     * @param matchingLabel matching label for inner join
     * @return joined graphs
     * */
    public List<Map<String,Integer>> joinEmbeddedGraphs (List<Map<Vertex,Vertex>> esm, List<Map<Node,Node>> grami, String matchingLabel) {

        List<Map<String,Integer>> results = new ArrayList<>();
        int freq = 0;
        for (Map<Vertex, Vertex> esmMap : esm) {
            for (Map<Node, Node> gramiMap: grami) {
                Map<String,Integer> esmIsomorphismIndex = convertESMIsomorphism(esmMap);//key:label, value:id
                Map<String,Integer> gramiIsomorphismIndex = convertGramiIsomorphism(gramiMap);//key:label, value:id
                int i = esmIsomorphismIndex.get(matchingLabel).intValue();
                int j = gramiIsomorphismIndex.get(matchingLabel).intValue();
                if (i == j) {
                    Map<String,Integer> joinedGraph = new HashMap<>();
                    joinedGraph.putAll(esmIsomorphismIndex);
                    joinedGraph.putAll(gramiIsomorphismIndex);//merge two maps ignoring duplicate keys!
                    freq ++;
                    results.add(joinedGraph);
                }
            }
        }
        System.out.println("there are " + freq + " joined embedded graphs in the data graph found!");
        return results;
    }

    /**convert a sub-graph isomorphism of ESM to a map, where*
     * Key: label of a node of data graph in this sub-graph isomorphism, Value: id of a node of data graph in this sub-graph isomorphism
     *@from 27 Jan 2019
     *@param esmIsomorphism a sub-graph isomorphism returned by ESM
     *@return a map where label of a node of data graph in this sub-graph isomorphism, Value: id of a node of data graph in this sub-graph isomorphism
     * */
    public Map<String,Integer> convertESMIsomorphism (Map<Vertex,Vertex> esmIsomorphism) {
        Map<String,Integer> embeddedNodeIndex = new HashMap<>();
        Iterator it = esmIsomorphism.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            Vertex embeddedNode = (Vertex) pair.getValue();
            String embeddedNodeLabel = embeddedNode.getLemma();
            int embeddedNodeId = embeddedNode.getPos();
            embeddedNodeIndex.put(embeddedNodeLabel,embeddedNodeId);
        }
        return embeddedNodeIndex;
    }

    /**Convert a sub-graph isomorphism of Grami to a map, where*
     * Key: label of a node of data graph in this sub-graph isomorphism (embedded graph), Value: id of a node of data graph in this sub-graph isomorphism(embedded graph)
     * @from 27 Jan 2019
     * @param gramiIsomorphism a sub-graph isomorphism returned by Grami
     * @return a map where label of a node of data graph in this sub-graph isomorphism, Value: id of a node of data graph in this sub-graph isomorphism
     * */
    private Map<String,Integer> convertGramiIsomorphism (Map<Node,Node> gramiIsomorphism) {
        Map<String, Integer> embeddedNodeIndex = new HashMap<>();
        Iterator it = gramiIsomorphism.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            Node embeddedNode = (Node) pair.getValue();
            String embeddedNodeLabel = embeddedNode.getUri();
            Integer embeddedNodeId = Integer.parseInt(embeddedNode.getId());
            embeddedNodeIndex.put(embeddedNodeLabel, embeddedNodeId);
        }
        return embeddedNodeIndex;
    }


    /**Mixed sub-graph matcher*
     * @from 28 Jan 2019
     *@param directory a directory to data graph file and all of the pattern files
     *@param dataGraphFileName file name of data graph
     *@param esmPatternFileName file name of pattern for esm
     *@param gramiPatternFileName file name of pattern for grami
     *@param matchingLabel label of the joint node
     *@return all of the correspondences
     * */
    public List<Map<String,Integer>> join (String directory, String dataGraphFileName, String esmPatternFileName, String gramiPatternFileName,
                                           String matchingLabel) {

        List<Map<String,Integer>> joinedResults = new ArrayList<>();
        try {
            long start = System.currentTimeMillis();
            List<Map<Vertex,Vertex>> esmIsomorphismList = ESMMatcher.getMatchResults(directory, esmPatternFileName, dataGraphFileName, true, new HashSet<>(), new HashMap<>());//specify start node
            List<Map<Node,Node>> gramiIsomorphismList = loadGramiMatcher(directory,gramiPatternFileName,dataGraphFileName,1, 0, new HashSet<>(), new HashSet<>());
            joinedResults = joinEmbeddedGraphs(esmIsomorphismList, gramiIsomorphismList, matchingLabel);
            long elapsedTimeMills = System.currentTimeMillis() - start;
            float elapsedTimeSec = elapsedTimeMills/1000F;
            System.out.println("the ranking time for integrated matcher is: " + elapsedTimeSec + " s!");
            return joinedResults;
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return joinedResults;
    }

    /**Mixed sub-graph matcher*
     *@created 16 Feb 2019
     * @param esmPatternGraph pattern graph as input of ESM
     * @param gramiPatternGraph pattern graph as input of Grami
     * @param bigGraph data graph
     * @param jointPointLabel label of the joint point. NOTE currently only consider one joint point
     * @return all of the correspondences
     * */
    public List<Map<String, Integer>> join (DirectedWeightedMultigraph<Node,LabeledLink> bigGraph,DirectedWeightedMultigraph<Node,LabeledLink> esmPatternGraph,
                                            DirectedWeightedMultigraph<Node,LabeledLink> gramiPatternGraph,
                                            String jointPointLabel) {

        List<Map<String,Integer>> joinedResults = new ArrayList<>();
        try {
            List<Map<Vertex, Vertex>> esmIsomorphismList = ESMMatcher.getMatchResults(esmPatternGraph,bigGraph,true, new HashSet<>(), new HashMap<>());//start node will be specified.
            List<Map<Node,Node>> gramiIsomorphismList = loadGramiMatcher(gramiPatternGraph,bigGraph, 1, 0, new HashSet<>(), new HashSet<>());
            joinedResults = joinEmbeddedGraphs(esmIsomorphismList, gramiIsomorphismList, jointPointLabel);
            System.out.println("There are " + joinedResults.size() + " embedded graphs!");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return joinedResults;
    }


    public static void main (String args[]) {
        MixedMatcher matcher = new MixedMatcher();
        Set<Integer> limitNodeSet = new HashSet<>();
        limitNodeSet.add(1);
        Set<Integer> anchorSet = new HashSet<>();
        anchorSet.add(0);
        matcher.loadGramiMatcher(Settings.IntegratedAddress,"pattern20190226.lg" ,"kg20190226.lg", 1, 1, limitNodeSet, anchorSet);

//        matcher.join(Settings.IntegratedAddress, "kg20190115_N4000.lg","test_esm.lg","test_grami.lg", "12");
//        ESMMatcher.getMatchResults(Settings.IntegratedAddress,"ST4.lg","kg20190113_N2000.lg",true);
    }
}
