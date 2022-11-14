package model_learner;

import VF2.graph.LGGraph;
import edu.isi.karma.modeling.alignment.SemanticModel;
import edu.isi.karma.modeling.alignment.learner.ModelReader;
import edu.isi.karma.modeling.alignment.learner.SortableSemanticModel;
import edu.isi.karma.modeling.research.Params;
import edu.isi.karma.rep.alignment.*;
import edu.isi.karma.research.modeling.ModelLearner_KnownModels4;
import org.jgrapht.graph.DirectedWeightedMultigraph;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.List;

public class ModelLearner {

    public static FileWriter fw;


    public static void main(String[] args) throws Exception {

//        test1();
//        test2();
        semanticTypeStat();

//        System.exit(1);

//        Integer[][] train = {
//                {8, 10},
//        };
//
//
//        for (Integer[] trainDataIndex : train) {
//            String path = String.format("C:\\D_Drive\\ASM\\experiment\\exp_20220831\\train_%d_%d___1",
//                    trainDataIndex[0]+1, trainDataIndex[1]+1);
//
//            File dir = new File(path);
//            if (!dir.exists()){
//                dir.mkdirs();
//            }
//
//            String resPath = String.format("C:\\D_Drive\\ASM\\experiment\\exp_20220831\\train_%d_%d___1\\(%d,%d)result.csv",
//                    trainDataIndex[0]+1, trainDataIndex[1]+1,
//                    trainDataIndex[0]+1, trainDataIndex[1]+1);
//            File resFile = new File(resPath);
//            fw = new FileWriter(resFile);
//            fw.write(String.join(",", "data source", "precision: karma", "recall: karma", "running time(s): karma")+"\n");
//
//            for (int i = 0; i < 29; i++) {
//                if(i != trainDataIndex[0] && i!= trainDataIndex[1] ){
//                    String outputPath = String.format("C:\\D_Drive\\ASM\\experiment\\exp_20220831\\train_%d_%d___1\\newSource_",
//                            trainDataIndex[0]+1,trainDataIndex[1]+1);
//
//                    File expDir = new File(outputPath+(i+1));
//                    if(!expDir.exists()){
//                        expDir.mkdirs();
//                    }
//
//                    learnModel(i,trainDataIndex,outputPath+(i+1));
//                }
//            }
//            fw.close();
//
//        }


    }

    public static String learnedTypesStr(List<SemanticType> learnedTypes){
        StringBuilder str = new StringBuilder();
        boolean flag = false;
        if (learnedTypes != null && learnedTypes.size() > 1 &&
                learnedTypes.get(0).getConfidenceScore() / learnedTypes.get(1).getConfidenceScore() > 5){
            flag = true;
        }
        for (SemanticType learnedType : learnedTypes) {
//            System.out.println(learnedType.getType());
            String candidateType = learnedType.getDomain().getLocalName();
            candidateType = candidateType.substring(candidateType.lastIndexOf("/")+1);
            if (candidateType.contains("#")){
                candidateType = candidateType.substring(candidateType.lastIndexOf("#") + 1);
            }
            String candidateProperty = learnedType.getType().getLocalName();
            candidateProperty = candidateProperty.contains("#")?candidateProperty.substring(candidateProperty.lastIndexOf("#")+1)
                    :candidateProperty.substring(candidateProperty.lastIndexOf("/")+1);

//            System.out.println(candidateType + " " + candidateProperty + " " + learnedType.getConfidenceScore());
            Double score = learnedType.getConfidenceScore();
            if (score < 0.05){
                break;
            }
            str.append(String.join(" ", candidateType, candidateProperty, score.toString(), "\t"));
            if (flag){
                break;
            }
        }
        return String.valueOf(str);
    }


    public static void writeCSV(DirectedWeightedMultigraph<Node,LabeledLink> DiGraph, String path) throws IOException {
        FileWriter fw = new FileWriter(path);
//        fw.append("source,target,edge_label\n");
//
//        for (LabeledLink e: DiGraph.edgeSet()) {
//
//            String source = e.getSource().getLocalId();
//            source = source.substring(source.lastIndexOf("/")+1);
//            String target = e.getTarget().getLocalId();
//            target = target.substring(target.lastIndexOf("/")+1);
//            String edgeLabel = e.getLabel().getLocalName();
//            edgeLabel = edgeLabel.substring(edgeLabel.lastIndexOf("/")+1);
//            String str = source + ",";
//            str += target + ",";
//            str += edgeLabel + "\n";
//            fw.append(str);
//
//        }

        fw.append(String.join(",","source", "target", "edge_label", "target_node_type", "learned_types", "\n"));

        for (LabeledLink e: DiGraph.edgeSet()) {

            if (e.getTarget() instanceof InternalNode) {
                String source = e.getSource().getLocalId();
                source = source.substring(source.lastIndexOf("/") + 1);
                if (source.contains("#")){
                    source = source.substring(source.lastIndexOf("#") + 1);
                }
                String target = e.getTarget().getLocalId();
                target = target.substring(target.lastIndexOf("/") + 1);
                if (target.contains("#")){
                    target = target.substring(target.lastIndexOf("#") + 1);
                }
                String edgeLabel = e.getLabel().getLocalName();
                edgeLabel = edgeLabel.substring(edgeLabel.lastIndexOf("/") + 1);
                String str = String.join(",", source, target, edgeLabel, "InternalNode", "", "\n");
                fw.append(str);
            }else if(e.getTarget() instanceof ColumnNode) {
                String source = e.getSource().getLocalId();
                source = source.substring(source.lastIndexOf("/") + 1);
                if (source.contains("#")){
                    source = source.substring(source.lastIndexOf("#") + 1);
                }
                ColumnNode targetNode = (ColumnNode) e.getTarget();
                String target = targetNode.getColumnName();
                target = target.substring(target.lastIndexOf("/") + 1);
                String edgeLabel = e.getLabel().getLocalName();
                edgeLabel = edgeLabel.contains("#")? edgeLabel.substring(edgeLabel.lastIndexOf("#") + 1):
                        edgeLabel.substring(edgeLabel.lastIndexOf("/") + 1);
                String learnedTypes = targetNode.getLearnedSemanticTypes() == null?"":learnedTypesStr(targetNode.getLearnedSemanticTypes());
                String str = String.join(",", source, target, edgeLabel, "columnNode", learnedTypes, "\n");
                fw.append(str);
            }

        }

        fw.close();
    }

    public static void learnModel(int newSourceIndex, Integer[] trainDataIndex, String outputPath) throws Exception {

        Long start = System.currentTimeMillis();
        List<SortableSemanticModel> candidateModels = ModelLearner_KnownModels4.
                getCandidateSemanticModels(newSourceIndex, trainDataIndex, outputPath);
        double runTime = (System.currentTimeMillis() - start) / 1000f;
        double precision = 0;
        double recall=0;



        if (candidateModels.size() > 0) {

            SemanticModel candidateModel = candidateModels.get(0);


//            System.out.println("We get " + candidateModels.size() + " candidate semantic models for the new source!");
//            System.out.println("============================================");

            SemanticModel correctModel = ModelReader.importSemanticModelsFromJsonFiles(
                    Params.ROOT_DIR + "models-json-tmp", Params.MODEL_MAIN_FILE_EXT).get(newSourceIndex);
            DirectedWeightedMultigraph<Node, LabeledLink> correctModelGraph = correctModel.getSimpliedGraph();

//            String modelPath = outputPath + "\\modelgraphs";
            String cytoscapePath = outputPath + "\\cytoscape";

//            File modelDir = new File(modelPath);
            File cytoscapeDir = new File(cytoscapePath);

//            if (!modelDir.exists()) {
//                modelDir.mkdir();
//            }

            if (!cytoscapeDir.exists()) {
                cytoscapeDir.mkdir();
            }

            LGGraph correctGraph = VF2GraphAdapter.graphAdaptToVF2(correctModelGraph);
//        LGGraph.writeIntoFile(correctGraph,modelPath+"\\correct_model.lg");
            writeCSV(correctModel.getGraph(), cytoscapePath + "\\correct_model.csv");

//        int i = 0;
//        int j = 0;
//        for (SortableSemanticModel semanticModel : candidateModels) {
//            DirectedWeightedMultigraph<Node, LabeledLink> modelGraph = semanticModel.getSimpliedGraph();
//            System.out.println(semanticModel.getGraph());
//            LGGraph graph = VF2GraphAdapter.graphAdaptToVF2(modelGraph);
//            LGGraph.printVF2Graph(graph);
//            LGGraph.writeIntoFile(graph,modelPath + "\\model_"+(i++)+".lg");
//            visualizeGraphInCytoscape(modelGraph,cytoscapePath + "\\model_"+(j++)+".csv");
//        }

//            DirectedWeightedMultigraph<Node, LabeledLink> modelGraph = candidateModel.getSimpliedGraph();
//            LGGraph graph = VF2GraphAdapter.graphAdaptToVF2(modelGraph);
//        LGGraph.printVF2Graph(graph);

//        LGGraph.writeIntoFile(graph,modelPath + "\\candidate_model.lg");
            writeCSV(candidateModel.getGraph(), cytoscapePath + "\\candidate_model.csv");

            precision = candidateModel.evaluate(correctModel).getPrecision();
            recall = candidateModel.evaluate(correctModel).getRecall();

            fw.append(String.join(",", "s"+(newSourceIndex+1)+".csv", String.valueOf(precision), String.valueOf(recall), String.valueOf(runTime))).append("\n");
        }
    }


    public static void learnModel(int newSourceIndex, String outputPath) throws Exception {

        Long start = System.currentTimeMillis();
        List<SortableSemanticModel> candidateModels = ModelLearner_KnownModels4.
                getCandidateSemanticModels(newSourceIndex, outputPath);
        double runTime = (System.currentTimeMillis() - start) / 1000f;
        DecimalFormat twoDForm = new DecimalFormat("#.##");
        runTime = Double.parseDouble(twoDForm.format(runTime));
        double precision = 0;
        double recall=0;

        if (candidateModels.size() > 0) {

            SemanticModel candidateModel = candidateModels.get(0);


//            System.out.println("We get " + candidateModels.size() + " candidate semantic models for the new source!");
//            System.out.println("============================================");

            SemanticModel correctModel = ModelReader.importSemanticModelsFromJsonFiles(
                    Params.ROOT_DIR + "models-json-tmp", Params.MODEL_MAIN_FILE_EXT).get(newSourceIndex);
            DirectedWeightedMultigraph<Node, LabeledLink> correctModelGraph = correctModel.getSimpliedGraph();

//            String modelPath = outputPath + "\\modelgraphs";
            String cytoscapePath = outputPath + "\\cytoscape";

//            File modelDir = new File(modelPath);
            File cytoscapeDir = new File(cytoscapePath);

//            if (!modelDir.exists()) {
//                modelDir.mkdir();
//            }

            if (!cytoscapeDir.exists()) {
                cytoscapeDir.mkdir();
            }

            LGGraph correctGraph = VF2GraphAdapter.graphAdaptToVF2(correctModelGraph);
//        LGGraph.writeIntoFile(correctGraph,modelPath+"\\correct_model.lg");
            writeCSV(correctModel.getGraph(), cytoscapePath + "\\correct_model.csv");

//        int i = 0;
//        int j = 0;
//        for (SortableSemanticModel semanticModel : candidateModels) {
//            DirectedWeightedMultigraph<Node, LabeledLink> modelGraph = semanticModel.getSimpliedGraph();
//            System.out.println(semanticModel.getGraph());
//            LGGraph graph = VF2GraphAdapter.graphAdaptToVF2(modelGraph);
//            LGGraph.printVF2Graph(graph);
//            LGGraph.writeIntoFile(graph,modelPath + "\\model_"+(i++)+".lg");
//            visualizeGraphInCytoscape(modelGraph,cytoscapePath + "\\model_"+(j++)+".csv");
//        }

//            DirectedWeightedMultigraph<Node, LabeledLink> modelGraph = candidateModel.getSimpliedGraph();
//            LGGraph graph = VF2GraphAdapter.graphAdaptToVF2(modelGraph);
//        LGGraph.printVF2Graph(graph);

//        LGGraph.writeIntoFile(graph,modelPath + "\\candidate_model.lg");
            writeCSV(candidateModel.getGraph(), cytoscapePath + "\\candidate_model.csv");

            precision = candidateModel.evaluate(correctModel).getPrecision();
            recall = candidateModel.evaluate(correctModel).getRecall();

            fw.append(String.join(",", "s"+(newSourceIndex+1)+".csv", String.valueOf(precision), String.valueOf(recall), String.valueOf(runTime))).append("\n");
        }
    }

    public static void semanticTypeStat() throws Exception {
        List<SemanticModel> correctModels = ModelReader.importSemanticModelsFromJsonFiles(
                Params.ROOT_DIR+"models-json-tmp", Params.MODEL_MAIN_FILE_EXT);

        FileWriter fw = new FileWriter("C:\\D_Drive\\ASM\\experiment\\tmp\\semanticType__"+Params.DATASET_NAME+".csv");

        fw.write(String.join(",", "data_source", "column_name", "correct_type", "candidate_types")+"\n");

        for (SemanticModel model : correctModels) {

            DirectedWeightedMultigraph<Node, LabeledLink> graph = model.getGraph();
            for (Node node : graph.vertexSet()) {
                if (node instanceof ColumnNode){
                    String columnName = ((ColumnNode) node).getColumnName();
                    String correctTypeName = ((ColumnNode) node).getUserSemanticTypes().get(0).getDomain().getSimplifiedDisplayName();
                    String correctDataPropertyName = ((ColumnNode) node).getUserSemanticTypes().get(0).getType().getSimplifiedDisplayName();
                    String correctType = correctTypeName+";"+correctDataPropertyName;
                    String learnedTypes = "[";
                    List<SemanticType> learnedSemanticTypes = ((ColumnNode) node).getLearnedSemanticTypes();
                    if (learnedSemanticTypes!=null){
                        for (SemanticType semanticType : learnedSemanticTypes) {
                            learnedTypes += semanticType.getDomain().getSimplifiedDisplayName();
                            learnedTypes += ";";
                            learnedTypes += semanticType.getType().getSimplifiedDisplayName();
                            learnedTypes += ";";
                            learnedTypes += semanticType.getConfidenceScore();
                            learnedTypes += "\t";
                        }
                    }
                    learnedTypes += "]";
                    fw.write(String.join(",", model.getName(), columnName, correctType, learnedTypes)+"\n");
                }
            }
        }

        fw.close();
    }

    public static void test1() throws Exception {
//        List<SemanticModel> correctModels = ModelReader.importSemanticModelsFromJsonFiles(
//                Params.ROOT_DIR+"models-json-tmp", Params.MODEL_MAIN_FILE_EXT);
//
//        FileWriter fw = new FileWriter("C:\\D_Drive\\ASM\\experiment\\semanticType.csv");
//
//        fw.write(String.join(",", "data_source", "column_name", "correct_type", "candidate_types")+"\n");
//
//        for (SemanticModel model : correctModels) {
//            DirectedWeightedMultigraph<Node, LabeledLink> graph = model.getGraph();
//            for (Node node : graph.vertexSet()) {
//                if (node instanceof ColumnNode){
//                    String columnName = ((ColumnNode) node).getColumnName();
//                    String correctTypeName = ((ColumnNode) node).getUserSemanticTypes().get(0).getDomain().getSimplifiedDisplayName();
//                    String correctDataPropertyName = ((ColumnNode) node).getUserSemanticTypes().get(0).getType().getSimplifiedDisplayName();
//                    String correctType = correctTypeName+";"+correctDataPropertyName;
//                    String learnedTypes = "[";
//                    List<SemanticType> learnedSemanticTypes = ((ColumnNode) node).getLearnedSemanticTypes();
//                    if (learnedSemanticTypes!=null){
//                        for (SemanticType semanticType : learnedSemanticTypes) {
//                            learnedTypes += semanticType.getDomain().getSimplifiedDisplayName();
//                            learnedTypes += ";";
//                            learnedTypes += semanticType.getType().getSimplifiedDisplayName();
//                            learnedTypes += ";";
//                            learnedTypes += semanticType.getConfidenceScore();
//                            learnedTypes += "\t";
//                        }
//                    }
//                    learnedTypes += "]";
//                    fw.write(String.join(",", model.getName(), columnName, correctType, learnedTypes)+"\n");
//                }
//            }
//        }
//
//        fw.close();


        String path = "C:\\D_Drive\\ASM\\experiment\\exp_20220920\\";

        File dir = new File(path);
        if (!dir.exists()){
            dir.mkdirs();
        }

        String resPath = "C:\\D_Drive\\ASM\\experiment\\exp_20220920\\result.csv";
        File resFile = new File(resPath);
        fw = new FileWriter(resFile);
        fw.write(String.join(",", "data source", "precision: karma", "recall: karma", "running time(s): karma")+"\n");

        for (int i = 0; i < 15; i++) {
//                if(i!=0){
//                    continue;
//                }
            String outputPath = "C:\\D_Drive\\ASM\\experiment\\exp_20220920\\newSource_";
            File expDir = new File(outputPath+(i+1));
            if(!expDir.exists()){
                expDir.mkdirs();
            }

            learnModel(i,outputPath+(i+1));

        }
        fw.close();
    }

    public static void test2() throws Exception{
        Integer[][] train = {
                {0, 5, 11},
        };


        for (Integer[] trainDataIndex : train) {
            String path = String.format("C:\\D_Drive\\ASM\\experiment\\exp_20220916\\train_%d_%d_%d___1",
                    trainDataIndex[0]+1, trainDataIndex[1]+1, trainDataIndex[2]+1);

            File dir = new File(path);
            if (!dir.exists()){
                dir.mkdirs();
            }

            String resPath = String.format("C:\\D_Drive\\ASM\\experiment\\exp_20220916\\train_%d_%d_%d___1\\result.csv",
                    trainDataIndex[0]+1, trainDataIndex[1]+1, trainDataIndex[2]+1);
            File resFile = new File(resPath);
            fw = new FileWriter(resFile);
            fw.write(String.join(",", "data source", "precision: karma", "recall: karma", "running time(s): karma")+"\n");

            for (int i = 0; i < 29; i++) {
                if(i != trainDataIndex[0] && i!= trainDataIndex[1] && i != trainDataIndex[2] && i != 27){
                    String outputPath = String.format("C:\\D_Drive\\ASM\\experiment\\exp_20220916\\train_%d_%d_%d___1\\newSource_",
                            trainDataIndex[0]+1,trainDataIndex[1]+1, trainDataIndex[2]+1);

                    File expDir = new File(outputPath+(i+1));
                    if(!expDir.exists()){
                        expDir.mkdirs();
                    }

                    learnModel(i,trainDataIndex,outputPath+(i+1));
                }
            }
            fw.close();

        }
    }

}


