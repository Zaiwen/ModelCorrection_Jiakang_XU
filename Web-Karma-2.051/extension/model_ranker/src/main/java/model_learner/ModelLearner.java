package model_learner;

import VF2.graph.VF2Graph;
import edu.isi.karma.modeling.alignment.SemanticModel;
import edu.isi.karma.modeling.alignment.learner.ModelReader;
import edu.isi.karma.modeling.alignment.learner.SortableSemanticModel;
import edu.isi.karma.modeling.research.Params;
import edu.isi.karma.rep.alignment.LabeledLink;
import edu.isi.karma.rep.alignment.Node;
import edu.isi.karma.research.modeling.ModelLearner_KnownModels4;
import org.jgrapht.graph.DirectedWeightedMultigraph;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class ModelLearner {
    public static void main(String[] args) throws Exception {

        System.out.println("ModelLearner.main");

//        System.exit(1);

//        int newSourceIndex = 0;
//        int[] trainDataIndex = {1,4,5};
//        List<SortableSemanticModel> candidateModels = ModelLearner_KnownModels4.
//                getCandidateSemanticModels(newSourceIndex,trainDataIndex);
//
//        System.out.println("We get " + candidateModels.size() + " candidate semantic models for the new source!");
//        System.out.println("============================================");
//
//        SemanticModel correctModel = ModelReader.importSemanticModelsFromJsonFiles(
//                Params.ROOT_DIR+"models-json-modified", Params.MODEL_MAIN_FILE_EXT).get(newSourceIndex);
//        DirectedWeightedMultigraph<Node, LabeledLink> correctModelGraph = correctModel.getSimpliedGraph();
//        VF2Graph correctGraph = VF2GraphAdapter.graphAdaptToVF2(correctModelGraph);
//        VF2Graph.writeIntoFile(correctGraph,"E:\\model_correction\\modelgraphs\\correct_model.lg");
//        visualizeGraphInCytoscape(correctModelGraph,"E:\\model_correction\\cytoscape\\correct_model.csv");
//
//        File file = new File("E:\\model_correction\\train\\");
//
//        if (!file.exists()){
//            file.mkdir();
//        }
//
//        int i = 0;
//        int j = 0;
//        for (SortableSemanticModel semanticModel : candidateModels) {
//            DirectedWeightedMultigraph<Node, LabeledLink> modelGraph = semanticModel.getSimpliedGraph();
//            VF2Graph graph = VF2GraphAdapter.graphAdaptToVF2(modelGraph);
//            VF2Graph.printVF2Graph(graph);
//            VF2Graph.writeIntoFile(graph,"E:\\model_correction\\modelgraphs\\model_"+(i++)+".lg");
//            visualizeGraphInCytoscape(modelGraph,"E:\\model_correction\\cytoscape\\model"+(j++)+".csv");
//        }
//
//        List<SemanticModel> semanticModels = ModelReader.importSemanticModelsFromJsonFiles(
//                Params.ROOT_DIR+"models-json-modified", Params.MODEL_MAIN_FILE_EXT);
//
//        FileWriter fw = new FileWriter("C:\\Users\\lr slxdr\\Desktop\\karma_result.txt");
//
//        int[] train = {1,4,5};
//        for (int i = 0; i < 29; i++) {
//            if(i != train[0] && i!= train[1] && i != train[2] && i != 6 && i != 22){
//                List<SortableSemanticModel> candidateSemanticModels = ModelLearner_KnownModels4.getCandidateSemanticModels(i, train);
//                fw.write("dataSource_"+(i+1)+"\n");
//                fw.write(String.join("\t", "model", "precision", "recall")+"\n");
//                int j = 0;
//                for (SortableSemanticModel model : candidateSemanticModels) {
//                    ModelEvaluation me = model.evaluate(semanticModels.get(i));
//                    fw.write(String.join("\t", "model_"+j, String.valueOf(me.getPrecision()), String.valueOf(me.getRecall()))+"\n");
//                    j++;
//                }
//            }
//        }
//
//        fw.close();

        Integer[][] train = {
                {0, 5, 11}
        };


        for (Integer[] trainDataIndex : train) {
            for (int i = 0; i < 29; i++) {
                if(i != trainDataIndex[0] && i!= trainDataIndex[1] && i != trainDataIndex[2] && i != 26){
                    int newSourceIndex = i;
                    String path = String.format("D:\\ASM\\experiment\\model_correction_20220220\\train_%d_%d_%d\\newSource_",
                            trainDataIndex[0]+1,trainDataIndex[1]+1, trainDataIndex[2]+1);
                    File expDir = new File(path+(i+1));
                    if(!expDir.exists()){
                        expDir.mkdirs();
                    }
                    learnModel(newSourceIndex,trainDataIndex,path+(i+1));
                }
            }
        }




    }


    public static void visualizeGraphInCytoscape(DirectedWeightedMultigraph<Node,LabeledLink> DiGraph, String path) throws IOException {
        FileWriter fw = new FileWriter(path);
        fw.append("source,target,edge_label\n");
        for (LabeledLink e: DiGraph.edgeSet()) {
            String source = e.getSource().getLocalId();
            source = source.substring(source.lastIndexOf("/")+1);
            String target = e.getTarget().getLocalId();
            target = target.substring(target.lastIndexOf("/")+1);
            String edgeLabel = e.getLabel().getLocalName();
            edgeLabel = edgeLabel.substring(edgeLabel.lastIndexOf("/")+1);
            String str = source + ",";
            str += target + ",";
            str += edgeLabel + "\n";
            fw.append(str);

        }
        fw.close();
    }

    public static void learnModel(int newSourceIndex, Integer[] trainDataIndex, String path) throws Exception {

        List<SortableSemanticModel> candidateModels = ModelLearner_KnownModels4.
                getCandidateSemanticModels(newSourceIndex, trainDataIndex, path);

        System.out.println("We get " + candidateModels.size() + " candidate semantic models for the new source!");
        System.out.println("============================================");

        SemanticModel correctModel = ModelReader.importSemanticModelsFromJsonFiles(
                Params.ROOT_DIR+"models-json-modified", Params.MODEL_MAIN_FILE_EXT).get(newSourceIndex);
        DirectedWeightedMultigraph<Node, LabeledLink> correctModelGraph = correctModel.getSimpliedGraph();

        String modelPath = path + "\\modelgraphs";
        String cytoscapePath = path + "\\cytoscape";

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
        visualizeGraphInCytoscape(correctModelGraph,cytoscapePath+"\\correct_model.csv");

        int i = 0;
        int j = 0;
        for (SortableSemanticModel semanticModel : candidateModels) {
            DirectedWeightedMultigraph<Node, LabeledLink> modelGraph = semanticModel.getSimpliedGraph();
            VF2Graph graph = VF2GraphAdapter.graphAdaptToVF2(modelGraph);
            VF2Graph.printVF2Graph(graph);
            VF2Graph.writeIntoFile(graph,modelPath + "\\model_"+(i++)+".lg");
            visualizeGraphInCytoscape(modelGraph,cytoscapePath + "\\model_"+(j++)+".csv");
        }



    }

}


