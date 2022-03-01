package model_learner;

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
import java.util.List;

public class ModelLearner1 {
    public static void main(String[] args) throws Exception {
        int newSourceIndex = 12;
        Integer[] trainDataIndex = {0,1,2};
        List<SortableSemanticModel> candidateModels = ModelLearner_KnownModels4.
                getCandidateSemanticModels(newSourceIndex,trainDataIndex);
        System.out.println("We get " + candidateModels.size() + " candidate semantic models for the new source!");
        System.out.println("============================================");

        File graphs = new File("C:\\Users\\Dell\\Desktop\\model_graphs0");
        LabelDir.getLabel();

        if (!graphs.exists()){
            graphs.mkdir();
        }

        SemanticModel correctModel = ModelReader.importSemanticModelsFromJsonFiles(Params.ROOT_DIR+"models-json-modified",
                Params.MODEL_MAIN_FILE_EXT).get(0);

        DirectedWeightedMultigraph<Node, LabeledLink> correctModelGraph = correctModel.getSimpliedGraph();


        File cmFile = new File(graphs,"correct_model.txt");
        FileWriter fw = new FileWriter(cmFile);
        fw.append("source\ttarget\tedge_label\n");
        for (LabeledLink e: correctModelGraph.edgeSet()) {
            String source = e.getSource().getLocalId();
            source = source.substring(source.lastIndexOf('E'));
            String target = e.getTarget().getLocalId();
            target = target.substring(target.lastIndexOf('E'));
            String edgeLabel = e.getLabel().getLocalName();
            edgeLabel = edgeLabel.substring(edgeLabel.lastIndexOf('P'));

            String str = source + "\t";
            str += target + "\t";
            str += edgeLabel + "\n";
            fw.append(str);
        }

        fw.close();

        int i = 0;
        for (SortableSemanticModel model : candidateModels) {
            DirectedWeightedMultigraph<Node, LabeledLink> modelGraph = model.getSimpliedGraph();

            String graphFileName = "model_" + i + ".txt";
            File graphFile = new File(graphs,graphFileName);
            FileWriter fw1 = new FileWriter(graphFile);

            fw1.append("source\ttarget\tedge_label\n");
            for(LabeledLink e:modelGraph.edgeSet()){
                String str1 = e.getSource().getLocalId() + "\t";
                str1 += e.getTarget().getLocalId() + "\t";
                str1 += e.getLabel().getLocalName() + "\n";
                fw1.append(str1);

            }

            fw1.close();

            i++;
        }
    }
}
