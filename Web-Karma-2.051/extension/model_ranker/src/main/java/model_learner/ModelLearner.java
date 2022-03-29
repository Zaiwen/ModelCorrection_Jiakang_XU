package model_learner;

import VF2.graph.VF2Graph;
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
import java.util.List;

public class ModelLearner {
    public static void main(String[] args) throws Exception {

//        List<SemanticModel> models = ModelReader.importSemanticModelsFromJsonFiles(
//                Params.ROOT_DIR+"models-json-tmp", Params.MODEL_MAIN_FILE_EXT);
//
//        for (SemanticModel model : models) {
//            if (model.getName().endsWith("csv")) {
//                writeCSV(model.getSimpliedGraph(), "E:\\correct_models\\" + model.getName());
//            }
////            System.out.println(model.getName());
//        }
//
//
//

//        System.exit(1);

        Integer[][] train = {
                {0, 9, 20}
        };


        for (Integer[] trainDataIndex : train) {
            for (int i = 0; i < 29; i++) {
                if(i != trainDataIndex[0] && i!= trainDataIndex[1] && i != trainDataIndex[2] && i != 26){
                    String outputPath = String.format("D:\\ASM\\experiment\\exp_20220322\\train_%d_%d_%d\\newSource_",
                            trainDataIndex[0]+1,trainDataIndex[1]+1, trainDataIndex[2]+1);
                    File expDir = new File(outputPath+(i+1));
                    if(!expDir.exists()){
                        expDir.mkdirs();
                    }
                    learnModel(i,trainDataIndex,outputPath+(i+1));
                }
            }
        }




    }

    public static String learnedTypesStr(List<SemanticType> learnedTypes){
        StringBuilder str = new StringBuilder();
        for (SemanticType learnedType : learnedTypes) {
//            System.out.println(learnedType.getType());
            String candidateType = learnedType.getDomain().getLocalName();
            candidateType = candidateType.substring(candidateType.lastIndexOf("/")+1);
            String candidateProperty = learnedType.getType().getLocalName();
            candidateProperty = candidateProperty.contains("#")?candidateProperty.substring(candidateProperty.lastIndexOf("#")+1)
                    :candidateProperty.substring(candidateProperty.lastIndexOf("/")+1);
//            System.out.println(candidateType + " " + candidateProperty + " " + learnedType.getConfidenceScore());
            str.append(String.join(" ", candidateType, candidateProperty, learnedType.getConfidenceScore().toString(), "\t"));
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
                String target = e.getTarget().getLocalId();
                target = target.substring(target.lastIndexOf("/") + 1);
                String edgeLabel = e.getLabel().getLocalName();
                edgeLabel = edgeLabel.substring(edgeLabel.lastIndexOf("/") + 1);
                String str = String.join(",", source, target, edgeLabel, "InternalNode", "null", "\n");
                fw.append(str);
            }else if(e.getTarget() instanceof ColumnNode) {
                String source = e.getSource().getLocalId();
                source = source.substring(source.lastIndexOf("/") + 1);
                ColumnNode targetNode = (ColumnNode) e.getTarget();
                String target = targetNode.getColumnName();
                target = target.substring(target.lastIndexOf("/") + 1);
                String edgeLabel = e.getLabel().getLocalName();
                edgeLabel = edgeLabel.contains("#")? edgeLabel.substring(edgeLabel.lastIndexOf("#") + 1):
                        edgeLabel.substring(edgeLabel.lastIndexOf("/") + 1);
                String learnedTypes = targetNode.getLearnedSemanticTypes() == null?"null":learnedTypesStr(targetNode.getLearnedSemanticTypes());
                String str = String.join(",", source, target, edgeLabel, "columnNode", learnedTypes, "\n");
                fw.append(str);
            }

        }

        fw.close();
    }

    public static void learnModel(int newSourceIndex, Integer[] trainDataIndex, String outputPath) throws Exception {

        List<SortableSemanticModel> candidateModels = ModelLearner_KnownModels4.
                getCandidateSemanticModels(newSourceIndex, trainDataIndex, outputPath);

        SemanticModel candidateModel = candidateModels.get(0);

        System.out.println("We get " + candidateModels.size() + " candidate semantic models for the new source!");
        System.out.println("============================================");

        SemanticModel correctModel = ModelReader.importSemanticModelsFromJsonFiles(
                Params.ROOT_DIR+"models-json-tmp", Params.MODEL_MAIN_FILE_EXT).get(newSourceIndex);
        DirectedWeightedMultigraph<Node, LabeledLink> correctModelGraph = correctModel.getSimpliedGraph();

        String modelPath = outputPath + "\\modelgraphs";
        String cytoscapePath = outputPath + "\\cytoscape";

        File modelDir = new File(modelPath);
        File cytoscapeDir = new File(cytoscapePath);

        if (!modelDir.exists()){
            modelDir.mkdir();
        }

        if (!cytoscapeDir.exists()){
            cytoscapeDir.mkdir();
        }

        VF2Graph correctGraph = VF2GraphAdapter.graphAdaptToVF2(correctModelGraph);
        VF2Graph.writeIntoFile(correctGraph,modelPath+"\\correct_model.lg");
        writeCSV(correctModel.getGraph(),cytoscapePath+"\\correct_model.csv");

//        int i = 0;
//        int j = 0;
//        for (SortableSemanticModel semanticModel : candidateModels) {
//            DirectedWeightedMultigraph<Node, LabeledLink> modelGraph = semanticModel.getSimpliedGraph();
//            System.out.println(semanticModel.getGraph());
//            VF2Graph graph = VF2GraphAdapter.graphAdaptToVF2(modelGraph);
//            VF2Graph.printVF2Graph(graph);
//            VF2Graph.writeIntoFile(graph,modelPath + "\\model_"+(i++)+".lg");
//            visualizeGraphInCytoscape(modelGraph,cytoscapePath + "\\model_"+(j++)+".csv");
//        }

        DirectedWeightedMultigraph<Node, LabeledLink> modelGraph = candidateModel.getSimpliedGraph();
        VF2Graph graph = VF2GraphAdapter.graphAdaptToVF2(modelGraph);
//        VF2Graph.printVF2Graph(graph);

        VF2Graph.writeIntoFile(graph,modelPath + "\\candidate_model.lg");
        writeCSV(candidateModel.getGraph(),cytoscapePath + "\\candidate_model.csv");

    }

}


