package au.edu.unisa;

import ESM.ESM;
import ESM.ESMMatcher;
import ESM.Edge;
import ESM.Vertex;
import au.com.d2dcrc.GramiMatcher;
import au.com.d2dcrc.yago2es.SubgraphMatcher;
import au.com.d2dcrc.yago2es.Util;
import dataStructures.HPListGraph;
import dataStructures.Query;
import edu.isi.karma.modeling.alignment.learner.SortableSemanticModel;
import edu.isi.karma.rep.alignment.LabeledLink;
import edu.isi.karma.rep.alignment.Node;
import edu.isi.karma.research.modeling.ModelLearner_KnownModels4;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import org.jgrapht.graph.DirectedWeightedMultigraph;

import java.util.*;

/**Rank data source of a museum data**/
public class RankMuseumDataModel {


    private static Map<String, Integer> nodeLabelMap = new HashMap<>();//key: node label in the KG, value: a number
    private static Map<String, Integer> edgeLabelMap = new HashMap<>();//key: edge label in the KG, value: a number


    /**Load the pre-made knowledge graph *
     * @param KGFileUrl kg file path
     * @param kgFileName kg file name
     * @return directed graph
     * @from 6 May 2019
     * @revised 6 June 2019
     * */

    public static DirectedWeightedMultigraph<Node, LabeledLink> loadMuseumKG (String KGFileUrl, String kgFileName) {

        //String kgName = "kg20190504_M_10000N.lg";
        DirectedWeightedMultigraph<Node, LabeledLink> bigGraph = ESMMatcher.loadDirectedWeightedMultipGraph(KGFileUrl,kgFileName);
        System.out.println("The museum knowledge graph " + kgFileName + " has already been loaded into memory!");
        System.out.println("# TOTAL N OF KG: " + bigGraph.vertexSet().size());
        System.out.println("# TOTAL E OF KG: " + bigGraph.edgeSet().size());
        return bigGraph;
    }


    /**load node label map map*
     *@from 6 May 2019
     * */
    public static Map<Integer, String> loadTypeMap (String pathName, String fileName) {

        Map<Integer, String> typeMap = Util.readNumeratedLabelFromFile(pathName.concat(fileName));
        System.out.println("type map has been loaded! ");

        //clean value
        Iterator it = typeMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            Integer entityNumber = (Integer) pair.getKey();
            String entityType = (String) pair.getValue();
            if ((entityNumber == 0) && (entityType.length() <= 1 )) {
                typeMap.put(entityNumber,entityType.substring(0,entityType.length()-1));//update value
            } else  {
                typeMap.put(entityNumber,entityType.substring(32,entityType.length()-1));//update value
            }

        }

        Utilities.printMap(typeMap);
        return typeMap;
    }


    /**Get all of candidate Steiner tree of a data source from museum data*
     * @from 5 May 2019
     * @param shuffle shuffle node or edge label in the Steiner tree or not
     * */
    public static List<SteinerTree> getCandidateSteinerTree (boolean shuffle) {

        List<SteinerTree> steinerTrees = new ArrayList<>();
        try {
            List<SortableSemanticModel> candidateSemanticModels = ModelLearner_KnownModels4.getCandidateSemanticModels(0, new Integer[]{1, 2, 3});
            System.out.println("So far we have got " + candidateSemanticModels.size() + " candidate semantic models! ");
            List<DirectedWeightedMultigraph<Node,LabeledLink>> seedList = new ArrayList<>();
            for (int i = 0; i < candidateSemanticModels.size(); i++) {
                System.out.println("candidate " + i + " " + candidateSemanticModels.get(i).getRankingDetails());
                DirectedWeightedMultigraph<Node,LabeledLink> candidateSteinerTree = candidateSemanticModels.get(i).getSimpliedGraph();//only get internal nodes and object property link.

                //trim labels of nodes and edges of Steiner tree. Like, trim the prefix of 'E48_Place_Name'
                for (Node node : candidateSteinerTree.vertexSet()) {
                    if (node.getUri().substring(0,4).equals("http")) {

                        String originalNodeLabel = node.getUri();
                        String trimedNodeLabel = originalNodeLabel.substring(32,originalNodeLabel.length());
                        node.setLabel(trimedNodeLabel);//update node label as trimmed
                    }
                }

                for (LabeledLink labeledLink : candidateSteinerTree.edgeSet()) {
                    if (labeledLink.getUri().substring(0,4).equals("http")) {
                        String originalEdgeLabel = labeledLink.getUri();
                        String trimedEdgeLabel = originalEdgeLabel.substring(32,originalEdgeLabel.length());
                        labeledLink.setUri(trimedEdgeLabel);//update edge label as trimmed
                    }

                }
                seedList.add(candidateSteinerTree);
            }

            if (shuffle) {
                for (DirectedWeightedMultigraph<Node,LabeledLink> seed : seedList){
                    acrc.itms.unisa.Utilities.shuffleIds(seed);/**Here we shuffle the ids (including node id & edge id) of all the candidate Steiner tree**/
                    SteinerTree steinerTree = new SteinerTree(seed);
                    steinerTree.resetLabels(nodeLabelMap,edgeLabelMap);/**Here we reset the labels (including node label and edge label) of all the candidate Steiner trees according to the correspondence defined beforehand**/
                    steinerTrees.add(steinerTree);//added on 20 Feb 2019
                }
            }

            System.out.println("candidate semantic trees are generated. All the node and edge labels have been reset.");

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return steinerTrees;
    }

    public static void setNodeLabelMap (Map<Integer, String> entityTypeMap) {
        Iterator it = entityTypeMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            Integer key = (Integer) pair.getKey();
            String value = (String) pair.getValue();
            nodeLabelMap.put(value,key);
        }
    }

    public static void setEdgeLabelMap (Map<Integer, String> relationshipTypeMap) {
        Iterator it = relationshipTypeMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            Integer key = (Integer) pair.getKey();
            String value = (String) pair.getValue();
            edgeLabelMap.put(value, key);
        }
    }

    /**ESM Matcher**/
    public static void esmMatch (DirectedWeightedMultigraph<Node,LabeledLink> bigGraph, List<SteinerTree> steinerTrees) throws Exception {
        long start = System.currentTimeMillis();
        DirectedSparseGraph<Vertex, Edge> esmDataGraph = ESMMatcher.convertESMGraph (bigGraph, "DATA");//convert it to the ESM graph
        SubgraphMatcher subgraphMatcher = new SubgraphMatcher();
        int index = 0;
        for (SteinerTree st : steinerTrees) {
            System.out.println("Now we get the No. " + index + " Steiner tree! ");
            DirectedSparseGraph<Vertex, Edge> esmPatternGraph = ESMMatcher.convertESMGraph(st.getTree(), "PATTERN");//convert to an ESM graph
            System.out.println("Now begin to match the No. " + index + " Steiner tree with the data graph! " );
            ESM esm = new ESM (esmPatternGraph, esmDataGraph);
            int result = subgraphMatcher.isGraphIsmomorphism(esmPatternGraph,esmDataGraph);
            if (result == 1) {
                System.out.println("isomorphism to the No. " + index + " Steiner tree has been detected!");
                ESM esmMatcher = new ESM (esmPatternGraph, esmDataGraph);
                List<Map<Vertex,Vertex>> matchResults = esmMatcher.getSubgraphMatchingMatches();
                System.out.println("the frequency of the No. " + index + " is: " + matchResults.size());
            } else {
                throw new Exception("isomorphism to the No. " + index + " Steiner tree has not been detected!");
            }
            index++;
        }
        long elapsedTimeMills = System.currentTimeMillis() - start;
        float elapsedTimeSec = elapsedTimeMills/1000F;
        System.out.println("the elapsed time for ranking is: " + elapsedTimeSec + " s!");
    }

    /**GraMi Matcher**/
    public static void gramiMatch (DirectedWeightedMultigraph<Node,LabeledLink> bigGraph, List<SteinerTree> st)throws Exception {

        Map<SteinerTree, Integer> freqMap = new HashMap<>();
        long start = System.currentTimeMillis();
        dataStructures.Graph singleGraph = new dataStructures.Graph(5000,Integer.MAX_VALUE);/**Create a big graph, the ID is 5000**/
        dataStructures.Graph.loadDirectedWeightedMultigraph(bigGraph, singleGraph);/**Load 'bigGraph'(DirectedWeighedMultiGraph) into it**/
        singleGraph.printFreqNodes();//print frequent nodes

        int index=0;
        for (SteinerTree steinerTree : st) {
            System.out.println("now we get the No." + index + " Steiner tree!");
            index++;
            HPListGraph hpListGraph = new HPListGraph("query");/**create pattern graph**/
            HPListGraph.loadDirectedWeightedMultiGraph(steinerTree.getTree(), hpListGraph);
            System.out.println("the pattern is: \n" + hpListGraph);//print the pattern graph
            Query q = new Query((HPListGraph<Integer, Double>) hpListGraph);/**create a query**/
            HashMap<Integer, HashSet<Integer>> nonCandidates = new HashMap<>();//create a hashmap to save pruned variables
            GramiMatcher gramiMatcher = new GramiMatcher();
            gramiMatcher.setGraph(singleGraph);
            gramiMatcher.setQry(q);
            int freq = gramiMatcher.getFrequency(nonCandidates, 1);//the pattern graph is a tree. But, the label might not be unique
            freqMap.put(steinerTree, freq);

        }
        System.out.println("\n");
        long elapsedTimeMills = System.currentTimeMillis() - start;
        float elapsedTimeSec = elapsedTimeMills/1000F;
        System.out.println("the elapsed time for ranking is: " + elapsedTimeSec + " s!");

        Iterator iterator = freqMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry pair = (Map.Entry) iterator.next();
            Integer freq = (Integer) pair.getValue();
            System.out.println("FREQUENCY IS: " + freq);
            if (freq == 0) {
                SteinerTree steinerTree = (SteinerTree) pair.getKey();
                DirectedWeightedMultigraph<Node,LabeledLink> graph  = steinerTree.getTree();
                Set<Node> nodeSet = graph.vertexSet();
                for (Node node : nodeSet) {
                    System.out.println(node.getId() + " : " + node.getUri());

                }
                Set<LabeledLink> labeledLinkSet = graph.edgeSet();
                for (LabeledLink labeledLink : labeledLinkSet) {
                    System.out.println(labeledLink.getSource().getId() + " " + labeledLink.getTarget().getId() + " " + labeledLink.getUri() );

                }
            }
        }

    }

    /**museum_crm_test the match between a steiner tree and big graph**/
    public static void matchTest1 () {

        //load big graph
        DirectedWeightedMultigraph<Node, LabeledLink> bigGraph = loadMuseumKG(Settings.SerializedMusemumKGFileAddress, "kg.lg");
//        Map<Integer, String> entityTypeMap = loadTypeMap(Settings.SerializedMusemumKGFileAddress, "entity_type_map.lg");
//        Map<Integer, String> relationshipMap = loadTypeMap(Settings.SerializedMusemumKGFileAddress, "relationship_type_map.lg");
//        setNodeLabelMap(entityTypeMap);
//        setEdgeLabelMap(relationshipMap);

        //load steiner tree
        DirectedWeightedMultigraph<Node, LabeledLink> pattern = loadMuseumKG(Settings.SerializedMusemumKGFileAddress, "pattern.lg");
        SteinerTree steinerTree = new SteinerTree(pattern);
        List<SteinerTree> steinerTreeList = new ArrayList<>();
        steinerTreeList.add(steinerTree);

        try {
            gramiMatch(bigGraph, steinerTreeList);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    /**rank a data source from museum data*
     * @from 5 May 2019
     *Step: 1). Get all the candidate Steiner trees
     * 2). Load Knowledge graph
     *3). Use pattern graph match to get frequency
     * */
    public static void getModelFrequency () {


        DirectedWeightedMultigraph<Node, LabeledLink> bigGraph = loadMuseumKG(Settings.SerializedMusemumKGFileAddress, "kg.lg");
        Map<Integer, String> entityTypeMap = loadTypeMap(Settings.SerializedMusemumKGFileAddress, "entity_type_map.lg");
        Map<Integer, String> relationshipMap = loadTypeMap(Settings.SerializedMusemumKGFileAddress, "relationship_type_map.lg");
        setNodeLabelMap(entityTypeMap);
        setEdgeLabelMap(relationshipMap);
        List<SteinerTree> steinerTrees = getCandidateSteinerTree(true);
        System.out.println(steinerTrees.size() + " Steiner trees' labels has been converted into numbers! :-)");
        try {
            gramiMatch(bigGraph,steinerTrees);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**Load the pre-made knowledge graph *
     * @from 6 May 2019
     * */

    public static DirectedWeightedMultigraph<Node, LabeledLink> loadSyntheticKG (String kgFileName) {

        //String kgName = "kg20190504_M_10000N.lg";
        DirectedWeightedMultigraph<Node, LabeledLink> graph = ESMMatcher.loadDirectedWeightedMultipGraph(Settings.SerializedKGFileAddress,kgFileName);
        System.out.println("The synthetic knowledge graph " + kgFileName + " has already been loaded into memory!");
        System.out.println("# TOTAL N OF KG: " + graph.vertexSet().size());
        System.out.println("# TOTAL E OF KG: " + graph.edgeSet().size());
        return graph;
    }

    /**rank model by synthetic knowledge graph*
     * @from 10 May 2019
     * */
    public static void getModelFrequency_Synthetic_KG () {
        DirectedWeightedMultigraph<Node, LabeledLink> bigGraph =
                loadSyntheticKG("kg20190116_2000N.lg");

        DirectedWeightedMultigraph<Node, LabeledLink> pattern1 = loadSyntheticKG("ST1.lg");
        DirectedWeightedMultigraph<Node, LabeledLink> pattern2 = loadSyntheticKG("ST2.lg");
        DirectedWeightedMultigraph<Node, LabeledLink> pattern3 = loadSyntheticKG("ST3.lg");
        DirectedWeightedMultigraph<Node, LabeledLink> pattern4 = loadSyntheticKG("ST4.lg");

        SteinerTree steinerTree1 = new SteinerTree(pattern1);
        SteinerTree steinerTree2 = new SteinerTree(pattern2);
        SteinerTree steinerTree3 = new SteinerTree(pattern3);
        SteinerTree steinerTree4 = new SteinerTree(pattern4);
        List<SteinerTree> steinerTreeList = new ArrayList<>();
        steinerTreeList.add(steinerTree1);
        steinerTreeList.add(steinerTree2);
        steinerTreeList.add(steinerTree3);
        steinerTreeList.add(steinerTree4);
        try {
            Set<String> subgraphAnchorLemmas = new HashSet<>();
            Map<String,Set<Integer>> graphConnectingNodes = new HashMap<>();
            ESMMatcher.getMatchResults(Settings.IntegratedAddress, "ST1.lg", "kg20190116_2000N.lg",true,subgraphAnchorLemmas,
                    graphConnectingNodes);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }



    public static void main (String args[]) {
        getCandidateSteinerTree(false);
    }
}
