/*******************************************************************************
 * This new version is to combine Steiner tree algorithm with subgraph matching algorithm in GraMi. 14 August 2018.
 ******************************************************************************/

package edu.isi.karma.research.modeling;


import edu.isi.karma.config.ModelingConfiguration;
import edu.isi.karma.config.ModelingConfigurationRegistry;
import edu.isi.karma.modeling.alignment.*;
import edu.isi.karma.modeling.alignment.learner.*;
import edu.isi.karma.modeling.ontology.OntologyManager;
import edu.isi.karma.modeling.research.Params;
import edu.isi.karma.rep.alignment.*;
import edu.isi.karma.rep.alignment.SemanticType.Origin;
import edu.isi.karma.util.RandomGUID;
import edu.isi.karma.webserver.ContextParametersRegistry;
import edu.isi.karma.webserver.ServletContextParameterMap;
import org.jgrapht.graph.AsUndirectedGraph;
import org.jgrapht.graph.DirectedWeightedMultigraph;
import org.jgrapht.graph.WeightedMultigraph;
import org.python.core.PyFunction;
import org.python.core.PyInteger;
import org.python.core.PyList;
import org.python.google.common.collect.Lists;
import org.python.util.PythonInterpreter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.*;

public class ModelLearner_KnownModels4 {

    private static Logger logger = LoggerFactory.getLogger(ModelLearner_KnownModels4.class);
    private static OntologyManager ontologyManager = null;
    private static List<SemanticModel> semanticModels = null;
    private GraphBuilder graphBuilder = null;
    private NodeIdFactory nodeIdFactory = null;
    private List<Node> steinerNodes = null;
    private PrintWriter resultFile;



    public ModelLearner_KnownModels4(OntologyManager ontologyManager,
                                     List<Node> steinerNodes) throws FileNotFoundException {
        if (ontologyManager == null ||
                steinerNodes == null ||
                steinerNodes.isEmpty()) {
            logger.error("cannot instanciate model learner!");
            return;
        }
        GraphBuilder gb = ModelLearningGraph.getInstance(ontologyManager, ModelLearningGraphType.Compact).getGraphBuilder();
        this.ontologyManager = ontologyManager;
        this.steinerNodes = steinerNodes;
        this.graphBuilder = cloneGraphBuilder(gb); // create a copy of the graph builder
        this.nodeIdFactory = this.graphBuilder.getNodeIdFactory();

    }

    public ModelLearner_KnownModels4(GraphBuilder graphBuilder,
                                     List<Node> steinerNodes) throws FileNotFoundException {
        if (graphBuilder == null ||
                steinerNodes == null ||
                steinerNodes.isEmpty()) {
            logger.error("cannot instanciate model learner!");
            return;
        }
        this.ontologyManager = graphBuilder.getOntologyManager();
        this.steinerNodes = steinerNodes;
        this.graphBuilder = cloneGraphBuilder(graphBuilder); // create a copy of the graph builder
        this.nodeIdFactory = this.graphBuilder.getNodeIdFactory();
    }

    /**set Steiner nodes in this model learner. 8 June 2018.**/
    public void setSteinerNodes (List<Node> steinerNodes) {
        this.steinerNodes.clear();

        Iterator<Node> iterator = steinerNodes.iterator();
        while (iterator.hasNext()) {

            Node node = iterator.next();

            this.steinerNodes.add(node);


        }

        //steinerNodes = steinerNodes;

    }

    /**get Steiner nodes in this model learner. 8 June 2018.**/
    public List<Node> getSteinerNodes () {

        return this.steinerNodes;
    }

    private GraphBuilder cloneGraphBuilder(GraphBuilder graphBuilder) {

        GraphBuilder clonedGraphBuilder = null;
        if (graphBuilder == null || graphBuilder.getGraph() == null) {
            clonedGraphBuilder = new GraphBuilderTopK(this.ontologyManager, false);
        } else {
            clonedGraphBuilder = new GraphBuilderTopK(this.ontologyManager, graphBuilder.getGraph());
        }
        return clonedGraphBuilder;
    }

    /**Compute the Steiner tree for a single table. 17 May 2018.**/
    public List<SortableSemanticModel> hypothesize(boolean useCorrectTypes, int numberOfCandidates) throws Exception {

        ModelingConfiguration modelingConfiguration = ModelingConfigurationRegistry.getInstance().getModelingConfiguration(ontologyManager.getContextId());
        List<SortableSemanticModel> sortableSemanticModels = new ArrayList<SortableSemanticModel>();

        /**It is used to save the mapping relations between the column nodes in new source and merged graph. 8 June 2018**/
        Map<ColumnNode, ColumnNode> mappingToSourceColumns = new HashMap<ColumnNode, ColumnNode>();

        /**It is used to save the steiner nodes in the merged graph. 8 June 2018.**/
        List<ColumnNode> columnNodes = new LinkedList<ColumnNode>();

        /**The column nodes in the new source is saved in 'steinerNodes'. 8 June 2018.**/
        for (Node n : steinerNodes)
            if (n instanceof ColumnNode) {
                ColumnNode c = (ColumnNode)n;
                columnNodes.add(c);/**Here, we get the steiner node in the merged graph. 8 June 2018.**/
                mappingToSourceColumns.put(c, c);//what's mean for this code???
            }

        for (Node n : steinerNodes) {
            if (n instanceof ColumnNode) {
                ColumnNode steinerNode = (ColumnNode)n;
                List<SemanticType> candidateSemanticTypes = getCandidateSteinerSets(steinerNode, useCorrectTypes, numberOfCandidates);
                addSteinerNodeToTheGraph(steinerNode, candidateSemanticTypes);
            }
        }


        Set<Node> sn = new HashSet<Node>(steinerNodes);
        List<DirectedWeightedMultigraph<Node, LabeledLink>> topKSteinerTrees;
        if (this.graphBuilder instanceof GraphBuilderTopK) {

            /**Get the top k Steiner Trees. 22 May 2018.**/
            int k = modelingConfiguration.getTopKSteinerTree();

            topKSteinerTrees =  ((GraphBuilderTopK)this.graphBuilder).getTopKSteinerTrees(sn, k, null, null, false);
        }
        else
        {
            topKSteinerTrees = new LinkedList<DirectedWeightedMultigraph<Node, LabeledLink>>();
            SteinerTree steinerTree = new SteinerTree(
                    new AsUndirectedGraph<Node, DefaultLink>(this.graphBuilder.getGraph()), Lists.newLinkedList(sn));
            WeightedMultigraph<Node, DefaultLink> t = steinerTree.getDefaultSteinerTree();
            TreePostProcess treePostProcess = new TreePostProcess(this.graphBuilder, t);
            if (treePostProcess.getTree() != null)
                topKSteinerTrees.add(treePostProcess.getTree());
        }

//		System.out.println(GraphUtil.labeledGraphToString(treePostProcess.getTree()));

//		logger.info("END ...");

        for (DirectedWeightedMultigraph<Node, LabeledLink> tree: topKSteinerTrees) {
            if (tree != null) {
//					System.out.println();
                SemanticModel sm = new SemanticModel(new RandomGUID().toString(),
                        tree,
                        columnNodes,
                        mappingToSourceColumns
                );
                SortableSemanticModel sortableSemanticModel =
                        new SortableSemanticModel(sm, false);
                sortableSemanticModels.add(sortableSemanticModel);

//					System.out.println(GraphUtil.labeledGraphToString(sm.getGraph()));
//					System.out.println(sortableSemanticModel.getLinkCoherence().printCoherenceList());
            }
        }

        Collections.sort(sortableSemanticModels);
        int count = Math.min(sortableSemanticModels.size(), modelingConfiguration.getNumCandidateMappings());
        logger.info("results are ready ...");
//		sortableSemanticModels.get(0).print();
        return sortableSemanticModels.subList(0, count);

    }

    private List<SemanticType> getCandidateSteinerSets(ColumnNode steinerNode, boolean useCorrectTypes, int numberOfCandidates) {

        if (steinerNode == null)
            return null;

        List<SemanticType> candidateSemanticTypes = null;

        if (!useCorrectTypes) {
            candidateSemanticTypes = steinerNode.getTopKLearnedSemanticTypes(numberOfCandidates);
        } else if (steinerNode.getSemanticTypeStatus() == ColumnSemanticTypeStatus.UserAssigned) {
            candidateSemanticTypes = steinerNode.getUserSemanticTypes();
        }

        if (candidateSemanticTypes == null) {
            logger.error("No candidate semantic type found for the column " + steinerNode.getColumnName());
            return null;
        }

        return candidateSemanticTypes;
    }


    private void addSteinerNodeToTheGraph(ColumnNode steinerNode, List<SemanticType> semanticTypes) {

        if (!this.graphBuilder.addNode(steinerNode)) return ;

        if (semanticTypes == null) {
            logger.error("semantic type is null.");
            return;
        }

        for (SemanticType semanticType : semanticTypes) {

            if (semanticType == null) {
                logger.error("semantic type is null.");
                continue;

            }
            if (semanticType.getDomain() == null) {
                logger.error("semantic type does not have any domain");
                continue;
            }

            if (semanticType.getType() == null) {
                logger.error("semantic type does not have any link");
                continue;
            }

            String domainUri = semanticType.getDomain().getUri();
            String propertyUri = semanticType.getType().getUri();
            Double confidence = semanticType.getConfidenceScore();
            Origin origin = semanticType.getOrigin();

            logger.debug("semantic type: " + domainUri + "|" + propertyUri + "|" + confidence + "|" + origin);

            Set<Node> nodesWithSameUriOfDomain = this.graphBuilder.getUriToNodesMap().get(domainUri);
            if (nodesWithSameUriOfDomain == null || nodesWithSameUriOfDomain.isEmpty()) {
                String nodeId = nodeIdFactory.getNodeId(domainUri);
                Node source = new InternalNode(nodeId, new Label(domainUri));
                if (!this.graphBuilder.addNodeAndUpdate(source, null)) continue;
                String linkId = LinkIdFactory.getLinkId(propertyUri, source.getId(), steinerNode.getId());
                LabeledLink link = new DataPropertyLink(linkId, new Label(propertyUri));
                if (!this.graphBuilder.addLink(source, steinerNode, link)) continue;;
            } else {
                for (Node source : nodesWithSameUriOfDomain) {
                    String linkId = LinkIdFactory.getLinkId(propertyUri, source.getId(), steinerNode.getId());
                    LabeledLink link = new DataPropertyLink(linkId, new Label(propertyUri));
                    if (!this.graphBuilder.addLink(source, steinerNode, link)) continue;;
                }
            }

        }
    }

    private static double roundDecimals(double d, int k) {
        String format = "";
        for (int i = 0; i < k; i++) format += "#";
        DecimalFormat DForm = new DecimalFormat("#." + format);
        return Double.valueOf(DForm.format(d));
    }

    @SuppressWarnings("unused")
    private static void getStatistics1(List<SemanticModel> semanticModels) {
        for (int i = 0; i < semanticModels.size(); i++) {
            SemanticModel source = semanticModels.get(i);
            int attributeCount = source.getColumnNodes().size();
            int nodeCount = source.getGraph().vertexSet().size();
            int linkCount = source.getGraph().edgeSet().size();
            int datanodeCount = 0;
            int classNodeCount = 0;
            for (Node n : source.getGraph().vertexSet()) {
                if (n instanceof InternalNode) classNodeCount++;
                if (n instanceof ColumnNode) datanodeCount++;
            }
            System.out.println(attributeCount + "\t" + nodeCount + "\t" + linkCount + "\t" + classNodeCount + "\t" + datanodeCount);

            List<ColumnNode> columnNodes = source.getColumnNodes();
            getStatistics2(columnNodes);

        }
    }

    private static void getStatistics2(List<ColumnNode> columnNodes) {

        if (columnNodes == null)
            return;

        int numberOfAttributesWhoseTypeIsFirstCRFType = 0;
        int numberOfAttributesWhoseTypeIsInCRFTypes = 0;
        for (ColumnNode cn : columnNodes) {
            List<SemanticType> userSemanticTypes = cn.getUserSemanticTypes();
            List<SemanticType> top4Suggestions = cn.getTopKLearnedSemanticTypes(4);

            for (int i = 0; i < top4Suggestions.size(); i++) {
                SemanticType st = top4Suggestions.get(i);
                if (userSemanticTypes != null) {
                    for (SemanticType t : userSemanticTypes) {
                        if (st.getModelLabelString().equalsIgnoreCase(t.getModelLabelString())) {
                            if (i == 0) numberOfAttributesWhoseTypeIsFirstCRFType ++;
                            numberOfAttributesWhoseTypeIsInCRFTypes ++;
                            i = top4Suggestions.size();
                            break;
                        }
                    }
                }
            }

        }

        System.out.println(numberOfAttributesWhoseTypeIsInCRFTypes + "\t" + numberOfAttributesWhoseTypeIsFirstCRFType);
//		System.out.println(columnNodes.size() + "\t" + numberOfAttributesWhoseTypeIsInCRFTypes + "\t" + numberOfAttributesWhoseTypeIsFirstCRFType);

//		System.out.println("totalNumberOfAttributes: " + columnNodes.size());
//		System.out.println("numberOfAttributesWhoseTypeIsInCRFTypes: " + numberOfAttributesWhoseTypeIsInCRFTypes);
//		System.out.println("numberOfAttributesWhoseTypeIsFirstCRFType:" + numberOfAttributesWhoseTypeIsFirstCRFType);
    }

    /**Get candidate semantic models for new data source.
     * @return candidate semantic models (Steiner trees) for the new data source.
     * */
    public static List<SortableSemanticModel> getCandidateSemanticModels(int newSourceIndex, Integer[] trainDataIndex) throws Exception {

        ServletContextParameterMap contextParameters = ContextParametersRegistry.getInstance().getDefault();

//        String inputPath = Params.INPUT_DIR;
//        String graphPath = Params.GRAPHS_DIR;

        /**Import all of the semantic models, including new data source and training data source, into Karma. 14 Aug 2018**/
        if(semanticModels == null) {
            semanticModels = ModelReader.importSemanticModelsFromJsonFiles(Params.ROOT_DIR + "models-json-modified", Params.MODEL_MAIN_FILE_EXT);
        }

        List<SemanticModel> trainingData = new ArrayList<SemanticModel>();

        /**begin to import the common data model (CDM)**/
        if(ontologyManager == null) {
            ontologyManager = new OntologyManager(contextParameters.getId());
//            File oFile = new File(Params.ROOT_DIR + "weapon.owl");
        File oFile = new File(Params.ROOT_DIR+"ecrm_update(20190521).owl");
//        File ff = new File(Params.ROOT_DIR+"preloaded-ontologies");
//        File[] files = ff.listFiles();
//        for (File f : files) {
//
//            if (!f.getName().endsWith(".ttl")) {/**filter out ".DS_Store" file in the directory of 'preloaded-ontologies'**/
//                continue;
//            }
//            ontologyManager.doImport(f, "UTF-8");
//        }
            ontologyManager.doImport(oFile, "UTF-8");
            ontologyManager.updateCache();
        }


        ModelLearningGraph modelLearningGraph = null;

        ModelLearner_KnownModels4 modelLearner;

        boolean iterativeEvaluation = false;
        boolean useCorrectType = true;
        boolean randomModel = false;

        int numberOfCandidates = 1;
        int numberOfKnownModels;
        String filePath = Params.RESULTS_DIR + "temp/";
        String filename = "";
        filename += "results";
        filename += useCorrectType ? "-correct types":"-k=" + numberOfCandidates;
        filename += randomModel ? "-random":"";
        filename += iterativeEvaluation ? "-iterative":"";
        filename += ".csv";

//        PrintWriter resultFileIterative = null;

//        StringBuffer[] resultsArray = null;

//        if (iterativeEvaluation) {
//            resultFileIterative = new PrintWriter(new File(filePath + filename));
//            resultsArray = new StringBuffer[semanticModels.size() + 2];
//            for (int i = 0; i < resultsArray.length; i++) {
//                resultsArray[i] = new StringBuffer();
//            }
//        } else {
////            resultFile = new PrintWriter(new File(filePath + filename));
////            resultFile.println("source \t p \t r \t t \n");
//        }


        /**Get the semantic model of the new data source**/
        SemanticModel newSource = semanticModels.get(newSourceIndex);

        logger.info("======================================================");
        logger.info(newSource.getName() + "(#attributes:" + newSource.getColumnNodes().size() + ")");
        System.out.println(newSource.getName() + "(#attributes:" + newSource.getColumnNodes().size() + ")");
        logger.info("======================================================");

//        numberOfKnownModels = iterativeEvaluation ? 0 : semanticModels.size() - 1;

//        if (iterativeEvaluation) {
//            if (resultsArray[0].length() > 0)	resultsArray[0].append(" \t ");
//            resultsArray[0].append(newSource.getName() + "(" + newSource.getColumnNodes().size() + ")" + "\t" + " " + "\t" + " ");
//            if (resultsArray[1].length() > 0)	resultsArray[1].append(" \t ");
//            resultsArray[1].append("p \t r \t t");
//        }

        /**collect all of the training data set**/
//        for(int j=0; j < semanticModels.size(); j++){
//            if (j != newSourceIndex) {
//                trainingData.add(semanticModels.get(j));
//            }
//        }

//        int[] trainDataIndex = {1,2,3};
        numberOfKnownModels = trainDataIndex.length;
        for (int i = 0; i < trainDataIndex.length; i++) {
            trainingData.add(semanticModels.get(trainDataIndex[i]));
        }


        /**Until now, the 'userSemanticTypes' and 'learnedSemanticTypes' are still null. 18 June 2018.**/
        modelLearningGraph = (ModelLearningGraphCompact)ModelLearningGraph.getEmptyInstance(ontologyManager, ModelLearningGraphType.Compact);

        SemanticModel correctModel = newSource;

        /**Get the Steiner nodes (boxes) from the new source. 8 June 2018.**/
        List<ColumnNode> columnNodes = correctModel.getColumnNodes();
        //				if (useCorrectType && numberOfCRFCandidates > 1)
        //					updateCrfSemanticTypesForResearchEvaluation(columnNodes);


        List<Node> steinerNodes = new LinkedList<Node>(columnNodes);//get steiner node. very important. 19 Aug 2018



        modelLearner = new ModelLearner_KnownModels4(ontologyManager, steinerNodes);

        long start = System.currentTimeMillis();

//        String graphName = !iterativeEvaluation?
//                graphPath + semanticModels.get(newSourceIndex).getName() + Params.GRAPH_JSON_FILE_EXT :
//                graphPath + semanticModels.get(newSourceIndex).getName() + ".knownModels=" + numberOfKnownModels + Params.GRAPH_JSON_FILE_EXT;

//        if (randomModel) {
//            modelLearner = new ModelLearner_KnownModels4(new GraphBuilder(ontologyManager, false), steinerNodes);
//        } else if (new File(graphName).exists()) {
//            // read graph from file
//            try {
//                logger.info("loading the graph ...");
//                DirectedWeightedMultigraph<Node, DefaultLink> graph = GraphUtil.importJson(graphName);
//                modelLearner.graphBuilder = new GraphBuilderTopK(ontologyManager, graph);
//                modelLearner.nodeIdFactory = modelLearner.graphBuilder.getNodeIdFactory();
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        } else {
//            logger.info("Begin to building semantic model of the new source ...");
//
            for (SemanticModel sm : trainingData)
                modelLearningGraph.addModelAndUpdate(sm, PatternWeightSystem.JWSPaperFormula);
//
            modelLearner.graphBuilder = modelLearningGraph.getGraphBuilder();
            modelLearner.nodeIdFactory = modelLearner.graphBuilder.getNodeIdFactory();
//
//            /**print alignment graph**/
////            DirectedWeightedMultigraph<Node , DefaultLink> currentGraph = modelLearner.graphBuilder.getGraph();
////            printDirectedWeightedMultigraph(currentGraph);//print the graph
////            System.out.println("# of nodes in model learning graph: " + currentGraph.vertexSet().size());
////            System.out.println("# of links in model learning graph: " +currentGraph.edgeSet().size());
//
//            // save graph to file
//            try {
////						GraphUtil.exportJson(modelLearningGraph.getGraphBuilder().getGraph(), graphName);
//                /**Visualize the merged graph (model learning graph). 18 June 2018.**/
//                GraphVizUtil.exportJGraphToGraphviz(modelLearner.graphBuilder.getGraph(),
//                        "test20190531_alignment_without_steiner_n",
//                        true,
//                        GraphVizLabelType.LocalId,
//                        GraphVizLabelType.LocalUri,
//                        false,
//                        true,
//                        graphName + ".dot");
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }

        /**Compute Steiner tree for a single table. 17 May 2018. //Generating and Ranking Semantic Models (using karma's metrics) **/
        List<SortableSemanticModel> hypothesisList = modelLearner.hypothesize(useCorrectType, numberOfCandidates);

        long elapsedTimeMillis = System.currentTimeMillis() - start;
        float elapsedTimeSec = elapsedTimeMillis/1000F;

        System.out.println("time: " + elapsedTimeSec);

        int cutoff = 20;//ModelingConfiguration.getMaxCandidateModels();
        List<SortableSemanticModel> topHypotheses = null;
        if (hypothesisList != null) {
            topHypotheses = hypothesisList.size() > cutoff ?
                    hypothesisList.subList(0, cutoff) :
                    hypothesisList;
        }

        Map<String, SemanticModel> models = new TreeMap<String, SemanticModel>();

        // export to json
        //				if (topHypotheses != null)
        //					for (int k = 0; k < topHypotheses.size() && k < 3; k++) {
        //						String fileExt = null;
        //						if (k == 0) fileExt = Params.MODEL_RANK1_FILE_EXT;
        //						else if (k == 1) fileExt = Params.MODEL_RANK2_FILE_EXT;
        //						else if (k == 2) fileExt = Params.MODEL_RANK3_FILE_EXT;
        //						SortableSemanticModel m = topHypotheses.get(k);
        //						new SemanticModel(m).writeJson(Params.MODEL_DIR +
        //								newSource.getName() + fileExt);
        //					}

        ModelEvaluation me;
        models.put("1-correct model", correctModel);
        if (topHypotheses != null) {
            for (int k = 0; k < topHypotheses.size(); k++) {

                SortableSemanticModel m = topHypotheses.get(k);

                me = m.evaluate(correctModel);

                String label = "candidate " + k + "\n" +
//								(m.getSteinerNodes() == null ? "" : m.getSteinerNodes().getScoreDetailsString()) +
                        "link coherence:" + (m.getLinkCoherence() == null ? "" : m.getLinkCoherence().getCoherenceValue()) + "\n";
                label += (m.getSteinerNodes() == null || m.getSteinerNodes().getCoherence() == null) ?
                        "" : "node coherence:" + m.getSteinerNodes().getCoherence().getCoherenceValue() + "\n";
                label += "confidence:" + m.getConfidenceScore() + "\n";
                label += m.getSteinerNodes() == null ? "" : "mapping score:" + m.getSteinerNodes().getScore() + "\n";
                label +=
                        "cost:" + roundDecimals(m.getCost(), 6) + "\n" +
                                //								"-distance:" + me.getDistance() +
                                "-precision:" + me.getPrecision() +
                                "-recall:" + me.getRecall();

                models.put(label, m);

                if (k == 0) { // first rank model
                    System.out.println("number of known models: " + numberOfKnownModels +
                            ", precision: " + me.getPrecision() +
                            ", recall: " + me.getRecall() +
                            ", time: " + elapsedTimeSec);
                    logger.info("number of known models: " + numberOfKnownModels +
                            ", precision: " + me.getPrecision() +
                            ", recall: " + me.getRecall() +
                            ", time: " + elapsedTimeSec);
//							resultFile.println("number of known models \t precision \t recall");
//							resultFile.println(numberOfKnownModels + "\t" + me.getPrecision() + "\t" + me.getRecall());
                    String s = "";
                    if (iterativeEvaluation) {
//                        if (resultsArray[numberOfKnownModels + 2].length() > 0)
//                            resultsArray[numberOfKnownModels + 2].append(" \t ");
//                        resultsArray[numberOfKnownModels + 2].append(s);
                    } else {
                        s = newSource.getName() + "," + me.getPrecision() + "," + me.getRecall() + "," + elapsedTimeSec;
                        System.out.println(s);
//                        resultFile.println(s);
                    }

//					resultFile.println(me.getPrecision() + "\t" + me.getRecall() + "\t" + elapsedTimeSec);
                }
            }
        }



        String outputPath = Params.OUTPUT_DIR;
        String outName = !iterativeEvaluation?
                outputPath + semanticModels.get(newSourceIndex).getName() + Params.GRAPHVIS_OUT_DETAILS_FILE_EXT :
                outputPath + semanticModels.get(newSourceIndex).getName() + ".knownModels=" + numberOfKnownModels + Params.GRAPHVIS_OUT_DETAILS_FILE_EXT;

        GraphVizUtil.exportSemanticModelsToGraphviz(
                models,
                newSource.getName(),
                outName,
                GraphVizLabelType.LocalId,
                GraphVizLabelType.LocalUri,
                true,
                true);

//	    resultFile.println("=======================================================");
//        if (iterativeEvaluation) {
//            for (StringBuffer s : resultsArray)
//                resultFileIterative.println(s.toString());
//            resultFileIterative.close();
//        } else {
//            resultFile.close();
//        }
        return hypothesisList;

    }

    public static List<SortableSemanticModel> getCandidateSemanticModels(int newSourceIndex, Integer[] trainDataIndex, String outputPath) throws Exception {


        ServletContextParameterMap contextParameters = ContextParametersRegistry.getInstance().getDefault();

//        String inputPath = Params.INPUT_DIR;
//        String graphPath = Params.GRAPHS_DIR;

        /**Import all of the semantic models, including new data source and training data source, into Karma. 14 Aug 2018**/
        if(semanticModels == null) {
            semanticModels = ModelReader.importSemanticModelsFromJsonFiles(Params.ROOT_DIR + "models-json-tmp", Params.MODEL_MAIN_FILE_EXT);
        }


        List<SemanticModel> trainingData = new ArrayList<SemanticModel>();

        /**begin to import the common data model (CDM)**/
        if(ontologyManager == null) {
            ontologyManager = new OntologyManager(contextParameters.getId());
//            File oFile = new File(Params.ROOT_DIR + "weapon.owl");
            File oFile = new File(Params.ROOT_DIR+"ecrm_update(20190521).owl");
//        File ff = new File(Params.ROOT_DIR+"preloaded-ontologies");
//        File[] files = ff.listFiles();
//        for (File f : files) {
//
//            if (!f.getName().endsWith(".ttl")) {/**filter out ".DS_Store" file in the directory of 'preloaded-ontologies'**/
//                continue;
//            }
//            ontologyManager.doImport(f, "UTF-8");
//        }
            ontologyManager.doImport(oFile, "UTF-8");
            ontologyManager.updateCache();
        }


        ModelLearningGraph modelLearningGraph = null;

        ModelLearner_KnownModels4 modelLearner;

        boolean iterativeEvaluation = false;
        boolean useCorrectType = true;
        boolean randomModel = false;

        int numberOfCandidates = 1;
        int numberOfKnownModels;
        String filePath = Params.RESULTS_DIR + "temp/";
        String filename = "";
        filename += "results";
        filename += useCorrectType ? "-correct types":"-k=" + numberOfCandidates;
        filename += randomModel ? "-random":"";
        filename += iterativeEvaluation ? "-iterative":"";
        filename += ".csv";

//        PrintWriter resultFileIterative = null;
//        StringBuffer[] resultsArray = null;
//        if (iterativeEvaluation) {
//            resultFileIterative = new PrintWriter(new File(filePath + filename));
//            resultsArray = new StringBuffer[semanticModels.size() + 2];
//            for (int i = 0; i < resultsArray.length; i++) {
//                resultsArray[i] = new StringBuffer();
//            }
//        } else {
////            resultFile = new PrintWriter(new File(filePath + filename));
////            resultFile.println("source \t p \t r \t t \n");
//        }


        /**Get the semantic model of the new data source**/
        SemanticModel newSource = semanticModels.get(newSourceIndex);

        logger.info("======================================================");
        logger.info(newSource.getName() + "(#attributes:" + newSource.getColumnNodes().size() + ")");
        System.out.println(newSource.getName() + "(#attributes:" + newSource.getColumnNodes().size() + ")");
        logger.info("======================================================");

//        numberOfKnownModels = iterativeEvaluation ? 0 : semanticModels.size() - 1;

//        if (iterativeEvaluation) {
//            if (resultsArray[0].length() > 0)	resultsArray[0].append(" \t ");
//            resultsArray[0].append(newSource.getName() + "(" + newSource.getColumnNodes().size() + ")" + "\t" + " " + "\t" + " ");
//            if (resultsArray[1].length() > 0)	resultsArray[1].append(" \t ");
//            resultsArray[1].append("p \t r \t t");
//        }

        /**collect all of the training data set**/
//        for(int j=0; j < semanticModels.size(); j++){
//            if (j != newSourceIndex) {
//                trainingData.add(semanticModels.get(j));
//            }
//        }

//        int[] trainDataIndex = {1,2,3};
        numberOfKnownModels = trainDataIndex.length;
        for (int i = 0; i < trainDataIndex.length; i++) {
            trainingData.add(semanticModels.get(trainDataIndex[i]));
        }


        /**Until now, the 'userSemanticTypes' and 'learnedSemanticTypes' are still null. 18 June 2018.**/
        modelLearningGraph = (ModelLearningGraphCompact)ModelLearningGraph.getEmptyInstance(ontologyManager, ModelLearningGraphType.Compact);

        SemanticModel correctModel = newSource;

        /**Get the Steiner nodes (boxes) from the new source. 8 June 2018.**/
        List<ColumnNode> columnNodes = correctModel.getColumnNodes();
        //				if (useCorrectType && numberOfCRFCandidates > 1)
        //					updateCrfSemanticTypesForResearchEvaluation(columnNodes);


        List<Node> steinerNodes = new LinkedList<Node>(columnNodes);//get steiner node. very important. 19 Aug 2018



        modelLearner = new ModelLearner_KnownModels4(ontologyManager, steinerNodes);

        long start = System.currentTimeMillis();

//        String graphName = !iterativeEvaluation?
//                graphPath + semanticModels.get(newSourceIndex).getName() + Params.GRAPH_JSON_FILE_EXT :
//                graphPath + semanticModels.get(newSourceIndex).getName() + ".knownModels=" + numberOfKnownModels + Params.GRAPH_JSON_FILE_EXT;

//        if (randomModel) {
//            modelLearner = new ModelLearner_KnownModels4(new GraphBuilder(ontologyManager, false), steinerNodes);
//        } else if (new File(graphName).exists()) {
//            // read graph from file
//            try {
//                logger.info("loading the graph ...");
//                DirectedWeightedMultigraph<Node, DefaultLink> graph = GraphUtil.importJson(graphName);
//                modelLearner.graphBuilder = new GraphBuilderTopK(ontologyManager, graph);
//                modelLearner.nodeIdFactory = modelLearner.graphBuilder.getNodeIdFactory();
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        } else {
//            logger.info("Begin to building semantic model of the new source ...");
//
        for (SemanticModel sm : trainingData)
            modelLearningGraph.addModelAndUpdate(sm, PatternWeightSystem.JWSPaperFormula);
//
        modelLearner.graphBuilder = modelLearningGraph.getGraphBuilder();
        modelLearner.nodeIdFactory = modelLearner.graphBuilder.getNodeIdFactory();
//
//            /**print alignment graph**/
////            DirectedWeightedMultigraph<Node , DefaultLink> currentGraph = modelLearner.graphBuilder.getGraph();
////            printDirectedWeightedMultigraph(currentGraph);//print the graph
////            System.out.println("# of nodes in model learning graph: " + currentGraph.vertexSet().size());
////            System.out.println("# of links in model learning graph: " +currentGraph.edgeSet().size());
//
//            // save graph to file
//            try {
////						GraphUtil.exportJson(modelLearningGraph.getGraphBuilder().getGraph(), graphName);
//                /**Visualize the merged graph (model learning graph). 18 June 2018.**/
//                GraphVizUtil.exportJGraphToGraphviz(modelLearner.graphBuilder.getGraph(),
//                        "test20190531_alignment_without_steiner_n",
//                        true,
//                        GraphVizLabelType.LocalId,
//                        GraphVizLabelType.LocalUri,
//                        false,
//                        true,
//                        graphName + ".dot");
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }

        /**Compute Steiner tree for a single table. 17 May 2018. //Generating and Ranking Semantic Models (using karma's metrics) **/
        List<SortableSemanticModel> hypothesisList = modelLearner.hypothesize(useCorrectType, numberOfCandidates);

        long elapsedTimeMillis = System.currentTimeMillis() - start;
        float elapsedTimeSec = elapsedTimeMillis/1000F;

        System.out.println("time: " + elapsedTimeSec);

        int cutoff = 20;//ModelingConfiguration.getMaxCandidateModels();
        List<SortableSemanticModel> topHypotheses = null;
        if (hypothesisList != null) {
            topHypotheses = hypothesisList.size() > cutoff ?
                    hypothesisList.subList(0, cutoff) :
                    hypothesisList;
        }

        Map<String, SemanticModel> models = new TreeMap<String, SemanticModel>();

        // export to json
        //				if (topHypotheses != null)
        //					for (int k = 0; k < topHypotheses.size() && k < 3; k++) {
        //						String fileExt = null;
        //						if (k == 0) fileExt = Params.MODEL_RANK1_FILE_EXT;
        //						else if (k == 1) fileExt = Params.MODEL_RANK2_FILE_EXT;
        //						else if (k == 2) fileExt = Params.MODEL_RANK3_FILE_EXT;
        //						SortableSemanticModel m = topHypotheses.get(k);
        //						new SemanticModel(m).writeJson(Params.MODEL_DIR +
        //								newSource.getName() + fileExt);
        //					}

        ModelEvaluation me;
        models.put("1-correct model", correctModel);
        if (topHypotheses != null) {
            for (int k = 0; k < topHypotheses.size(); k++) {

                SortableSemanticModel m = topHypotheses.get(k);

                me = m.evaluate(correctModel);

                String label = "candidate " + k + "\n" +
//								(m.getSteinerNodes() == null ? "" : m.getSteinerNodes().getScoreDetailsString()) +
                        "link coherence:" + (m.getLinkCoherence() == null ? "" : m.getLinkCoherence().getCoherenceValue()) + "\n";
                label += (m.getSteinerNodes() == null || m.getSteinerNodes().getCoherence() == null) ?
                        "" : "node coherence:" + m.getSteinerNodes().getCoherence().getCoherenceValue() + "\n";
                label += "confidence:" + m.getConfidenceScore() + "\n";
                label += m.getSteinerNodes() == null ? "" : "mapping score:" + m.getSteinerNodes().getScore() + "\n";
                label +=
                        "cost:" + roundDecimals(m.getCost(), 6) + "\n" +
                                //								"-distance:" + me.getDistance() +
                                "-precision:" + me.getPrecision() +
                                "-recall:" + me.getRecall();

                models.put(label, m);

                if (k == 0) { // first rank model
                    System.out.println("number of known models: " + numberOfKnownModels +
                            ", precision: " + me.getPrecision() +
                            ", recall: " + me.getRecall() +
                            ", time: " + elapsedTimeSec);
                    logger.info("number of known models: " + numberOfKnownModels +
                            ", precision: " + me.getPrecision() +
                            ", recall: " + me.getRecall() +
                            ", time: " + elapsedTimeSec);
//							resultFile.println("number of known models \t precision \t recall");
//							resultFile.println(numberOfKnownModels + "\t" + me.getPrecision() + "\t" + me.getRecall());
                    String s = "";
                    if (iterativeEvaluation) {
//                        if (resultsArray[numberOfKnownModels + 2].length() > 0)
//                            resultsArray[numberOfKnownModels + 2].append(" \t ");
//                        resultsArray[numberOfKnownModels + 2].append(s);
                    } else {
                        s = newSource.getName() + "," + me.getPrecision() + "," + me.getRecall() + "," + elapsedTimeSec;
                        System.out.println(s);
//                        resultFile.println(s);
                    }

//					resultFile.println(me.getPrecision() + "\t" + me.getRecall() + "\t" + elapsedTimeSec);
                }
            }
        }



        String outName = !iterativeEvaluation?
                outputPath + "\\" + semanticModels.get(newSourceIndex).getName() + Params.GRAPHVIS_OUT_DETAILS_FILE_EXT :
                outputPath + "\\" + semanticModels.get(newSourceIndex).getName() + ".knownModels=" + numberOfKnownModels + Params.GRAPHVIS_OUT_DETAILS_FILE_EXT;

        GraphVizUtil.exportSemanticModelsToGraphviz(
                models,
                newSource.getName(),
                outName,
                GraphVizLabelType.LocalId,
                GraphVizLabelType.LocalUri,
                true,
                true);

//	    resultFile.println("=======================================================");
//        if (iterativeEvaluation) {
//            for (StringBuffer s : resultsArray)
//                resultFileIterative.println(s.toString());
//            resultFileIterative.close();
//        } else {
//            resultFile.close();
//        }
        return hypothesisList;

    }

    /**Get a node from knowledge graph*
     *@start from 28 May 2019
     * @param nodeUri
     * @param graph
     * @return node that named that URI
     * */
    public static Node getNodeFromDirectedWeightedMultigraph (String nodeUri, DirectedWeightedMultigraph<Node,DefaultLink> graph) {

        Node node = null;
        Set<Node> nodeSet = graph.vertexSet();
        for (Node node1 : nodeSet) {

            if (node1.getUri().equals(nodeUri)) {
                node = node1;
            }
        }
        return node;
    }

    /**Print graph*
     * @start from 29 May 2019
     * */
    public static void printDirectedWeightedMultigraph (DirectedWeightedMultigraph<Node,DefaultLink> graph) {
        Set<Node> nodeSet = graph.vertexSet();
        Set<DefaultLink> defaultLinks = graph.edgeSet();
        for (Node node : nodeSet) {
            String uri = node.getUri();
            System.out.println("Node URI is: " + uri);
        }


        for (DefaultLink defaultLink : defaultLinks) {
            String linkUri = defaultLink.getUri();
            Node source = defaultLink.getSource();
            Node target = defaultLink.getTarget();
            System.out.println("Edge URI is: " + linkUri + ", the source URI is: " + source.getUri() + ", the target URI is: " + target.getUri());
            //System.out.println("Edge ID is: " + defaultLink.getId() + ", the source ID is: " + source.getId() + ", the target ID is: " + target.getId());
        }



    }





    /**just for test . 14 Aug 2018.**/
    public static void main(String[] args) throws Exception {

//        List<SortableSemanticModel> result = getCandidateSemanticModels(2);
//        System.out.println("We get " + result.size() + " candidate semantic models for the new source!");

//        List<SortableSemanticModel> candidateSemanticModels = getCandidateSemanticModels(11, new int[]{0,1,2});
//        candidateSemanticModels.get(0).getAccuracy();
//        System.out.println(candidateSemanticModels.size());
        semanticModels = null;
        ontologyManager = null;


        SemanticModel candidateModel = getCandidateSemanticModels(11, new Integer[]{1, 4, 5}).get(0);


        // Leave One Out
//        for (int i = 0; i < 15; i++) {
//            int[] trainIndex = LOO(i);
//            List<SortableSemanticModel> candidateSemanticModels = getCandidateSemanticModels(i, trainIndex);
//
//        }
//        resultFile.close();

        // numbers of known models = 2
//        for (int i = 0; i < 15; i++) {
//            int[] trainIndex = randomTrainIndex(i, 2, i);
//            getCandidateSemanticModels(i, trainIndex);
//        }
//        resultFile.close();
        // numbers of known models = 6
//        for (int i = 0; i < 15; i++) {
//            int[] trainIndex = randomTrainIndex(i, 10, i);
//            getCandidateSemanticModels(i, trainIndex);
//        }
//        resultFile.close();
        // numbers of known models = 10
        System.exit(1);
        for (int i = 0; i < 15; i++) {
            Integer[] trainIndex = randomTrainIndex(i, 10, i);
            getCandidateSemanticModels(i, trainIndex);
        }
//        resultFile.close();

    }

    public static int[] LOO(int i){
        ArrayList<Integer> list = new ArrayList<>();


        for (int j = 0; j < 15; j++) {
            list.add(j);
        }

        list.remove(i);
        int[] arr = new int[14];
        for (int j = 0; j < 14; j++) {
            arr[j] = list.get(j);
        }

        return arr;
    }

    public static Integer[] randomTrainIndex(int testIndex, int numbersOfKnownModels, int seed){
        PythonInterpreter interpreter = new PythonInterpreter();
        interpreter.execfile("C:\\D_Drive\\ASM\\DataMatchingMaster\\randomTrainIndex.py");
        PyFunction pyFunction = interpreter.get("randomTrainIndex", PyFunction.class);
        PyList pyList = (PyList) pyFunction.__call__(new PyInteger(testIndex), new PyInteger(numbersOfKnownModels), new PyInteger(seed));
        pyList.sort();
        Integer[] arr = new Integer[numbersOfKnownModels];
        for (int j = 0; j < numbersOfKnownModels; j++) {
            arr[j] = (int) pyList.get(j);
        }
        return arr;
    }

}