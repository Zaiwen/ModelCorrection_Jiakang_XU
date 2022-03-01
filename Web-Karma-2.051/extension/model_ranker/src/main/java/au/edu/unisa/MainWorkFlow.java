//package au.edu.unisa;
//
//import ESM.ESM;
//import ESM.Edge;
//import ESM.Vertex;
//import acrc.itms.unisa.CommonDataModel;
//import ESM.ESMMatcher;
//import acrc.itms.unisa.Utilities;
//import au.com.d2dcrc.GramiMatcher;
//import au.com.d2dcrc.yago2es.SubgraphMatcher;
//import dataStructures.HPListGraph;
//import dataStructures.Query;
//import edu.isi.karma.modeling.alignment.learner.SortableSemanticModel;
//import edu.isi.karma.rep.alignment.LabeledLink;
//import edu.isi.karma.rep.alignment.Node;
//import edu.isi.karma.research.modeling.ModelLearner_KnownModels4;
//import edu.uci.ics.jung.graph.DirectedSparseGraph;
//import org.jgrapht.graph.DirectedWeightedMultigraph;
//
//import java.util.*;
//
///**This class will build the main process for ranking the semantic models of the new data source*
// * /**main process of rank of semantic models for multiple-tables .
// * This main process had been demoed on 21 Aug 2018 D2D roadmap meeting*
// * @From 15 Aug 2018
// * @author Zaiwen FENG
// * */
//
//public class MainWorkFlow {
//    public static void rank (String matcher) throws Exception {
//
//        try {
//            /**1. Read the synthetic knowledge graph**/
//            CommonDataModel commonDataModel = new CommonDataModel(1,new DirectedWeightedMultigraph<>(LabeledLink.class), new HashMap<>(), new HashMap<>(), new HashMap<>());
//            commonDataModel.generateCDMExample2();//generate an example cdm
//            commonDataModel.shuffleLabels();//shuffle the edge label and node label of the CDM
//            Map<String, Integer> nodeLabelMap = commonDataModel.getNodeLabelMap();//key: node label in the cdm, value: a number
//            Map<String, Integer> edgeLabelMap = commonDataModel.getEdgeLabelMap();//key: edge label in the cdm, value: a number
//            Utilities.printMap(commonDataModel.getNodeLabelMap());//print all the node labels
//            Utilities.printMap(commonDataModel.getEdgeLabelMap());//print out all the edge labels
//            System.out.println("CDM is successfully built...");
//            DirectedWeightedMultigraph<Node, LabeledLink> bigGraph = ESMMatcher.loadDirectedWeightedMultipGraph(Settings.SerializedKGFileAddress,"kg20190113_2000N.lg");
//            System.out.println("the synthetic knowledge graph has been loaded from the disk...");
//
//
//            /**2. Get the candidate Steiner trees with regard to the new data source**/
//            List<SortableSemanticModel> candidateSemanticModels = ModelLearner_KnownModels4.getCandidateSemanticModels();
//            System.out.println("So far we have got " + candidateSemanticModels.size() + " candidate semantic models! ");
//            List<DirectedWeightedMultigraph<Node,LabeledLink>> seedList = new ArrayList<>();
//            for (int i = 0; i < candidateSemanticModels.size(); i++) {
//                System.out.println("candidate " + i + " " + candidateSemanticModels.get(i).getRankingDetails());
//                DirectedWeightedMultigraph<Node,LabeledLink> candidateSteinerTree = candidateSemanticModels.get(i).getSimpliedGraph();//only get internal nodes and object property link.
//                seedList.add(candidateSteinerTree);
//            }
//
//            List<SteinerTree> steinerTrees = new ArrayList<>();
//            for (DirectedWeightedMultigraph<Node,LabeledLink> seed : seedList){
//                acrc.itms.unisa.Utilities.shuffleIds(seed);/**Here we shuffle the ids (including node id & edge id) of all the candidate Steiner tree**/
//                SteinerTree steinerTree = new SteinerTree(seed);
//                steinerTree.resetLabels(nodeLabelMap,edgeLabelMap);/**Here we reset the labels (including node label and edge label) of all the candidate Steiner trees according to the correspondence defined beforehand**/
//                steinerTrees.add(steinerTree);//added on 20 Feb 2019
//            }
//            System.out.println("candidate semantic trees are generated. All the node and edge labels have been reset.");
//
//
//            /**3. Get the appearance of each candidate Steiner tree.**/
//            long start = System.currentTimeMillis();
//            if (matcher.equals(Settings.Matcher.GraMi.toString())) {
//                dataStructures.Graph singleGraph = new dataStructures.Graph(5000,Integer.MAX_VALUE);/**Create a big graph, the ID is 5000**/
//                dataStructures.Graph.loadDirectedWeightedMultigraph(bigGraph, singleGraph);/**Load 'bigGraph'(DirectedWeighedMultiGraph) into it**/
//                singleGraph.printFreqNodes();//print frequent nodes
//
//                int index=0;
//                for (SteinerTree st : steinerTrees) {
//                    System.out.println("now we get the No." + index + " Steiner tree!");
//                    index++;
//                    HPListGraph hpListGraph = new HPListGraph("query");/**create pattern graph**/
//                    HPListGraph.loadDirectedWeightedMultiGraph(st.getTree(),hpListGraph);
//                    System.out.println("the pattern is: \n" + hpListGraph);//print the pattern graph
//                    Query q = new Query((HPListGraph<Integer,Double>)hpListGraph);/**create a query**/
//                    HashMap<Integer, HashSet<Integer>> nonCandidates = new HashMap<>();//create a hashmap to save pruned variables
//                    GramiMatcher gramiMatcher = new GramiMatcher();
//                    gramiMatcher.setGraph(singleGraph);
//                    gramiMatcher.setQry(q);
//                    gramiMatcher.getFrequency(nonCandidates,0);//the pattern graph is a tree --- so the 4th parameter is 0
//                    System.out.println("\n");
//                }
//            } else if (matcher.equals(Settings.Matcher.ESM.toString())) {
//
//                DirectedSparseGraph<Vertex, Edge> esmDataGraph = ESMMatcher.convertESMGraph (bigGraph, "DATA");//convert it to the ESM graph
//                SubgraphMatcher subgraphMatcher = new SubgraphMatcher();
//                int index = 0;
//                for (SteinerTree st : steinerTrees) {
//                    System.out.println("Now we get the No. " + index + " Steiner tree! ");
//                    DirectedSparseGraph<Vertex, Edge> esmPatternGraph = ESMMatcher.convertESMGraph(st.getTree(), "PATTERN");//convert to an ESM graph
//                    System.out.println("Now begin to match the No. " + index + " Steiner tree with the data graph! " );
//                    ESM esm = new ESM (esmPatternGraph, esmDataGraph);
//                    int result = subgraphMatcher.isGraphIsmomorphism(esmPatternGraph,esmDataGraph);
//                    if (result == 1) {
//                        System.out.println("isomorphism to the No. " + index + " Steiner tree has been detected!");
//                        ESM esmMatcher = new ESM (esmPatternGraph, esmDataGraph);
//                        List<Map<Vertex,Vertex>> matchResults = esmMatcher.getSubgraphMatchingMatches();
//                        System.out.println("the frequency of the No. " + index + " is: " + matchResults.size());
//                    } else {
//                        throw new Exception("isomorphism to the No. " + index + " Steiner tree has not been detected!");
//                    }
//                    index++;
//                }
//            }
//            long elapsedTimeMills = System.currentTimeMillis() - start;
//            float elapsedTimeSec = elapsedTimeMills/1000F;
//            System.out.println("the elapsed time for ranking is: " + elapsedTimeSec + " s!");
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    public static void main (String args[]) {
//        try {
//            rank(Settings.Matcher.GraMi.toString());
//        }catch (Exception ex) {
//            ex.printStackTrace();
//        }
//    }
//
//}
