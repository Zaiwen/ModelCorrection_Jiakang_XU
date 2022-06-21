package model_learner;

import VF2.graph.LGGraph;
import edu.isi.karma.modeling.alignment.ModelEvaluation;
import edu.isi.karma.modeling.alignment.SemanticModel;
import edu.isi.karma.modeling.alignment.learner.ModelReader;
import edu.isi.karma.modeling.alignment.learner.SortableSemanticModel;
import edu.isi.karma.modeling.research.Params;
import edu.isi.karma.research.modeling.ModelLearner_KnownModels4;
import VF2.algorithm.VF2;
import org.jgrapht.graph.DirectedWeightedMultigraph;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

import static edu.isi.karma.modeling.research.Params.ROOT_DIR;
import static VF2.graph.LGGraph.printVF2Graph;



public class ModelRanker1 {
    public static void main(String[] args) throws Exception {


        File result = new File("C:\\Users\\Dell\\Desktop\\model_ranker_exp\\result3.txt");

        FileWriter fw = new FileWriter(result);

        fw.append("model\tmap_frq\ttime\tprecision\trecall\n");
        fw.flush();

        List<SortableSemanticModel> candidateModels = ModelLearner_KnownModels4.
                getCandidateSemanticModels(0,new Integer[] {1,2,3});


        File knowledgeGraphFile = new File(ROOT_DIR + "museum20200906.lg");
        LGGraph knowledgeGraph = LGGraph.loadGraphSetFromFile(knowledgeGraphFile);


        SemanticModel correctModel = ModelReader.importSemanticModelsFromJsonFiles(Params.ROOT_DIR+"models-json-modified",
                Params.MODEL_MAIN_FILE_EXT).get(19);

        DirectedWeightedMultigraph correctModelGraph = correctModel.getSimpliedGraph();
        LGGraph correctLGGraph = VF2GraphAdapter.graphAdaptToVF2(correctModelGraph);

        printVF2Graph(correctLGGraph);

        long start0 = System.currentTimeMillis();
        VF2 v = new VF2();
        ArrayList mappings0 = v.matchGraphSetWithQuery(knowledgeGraph, correctLGGraph);
        int mapFrq0 = mappings0.size();
//        int mapFrq0 = 10000000;
        double runtime0 = (System.currentTimeMillis() - start0)/1000.0;
        fw.append("correct_model\t"+mapFrq0+'\t'+runtime0+"\t1.00\t1.00\n");
        fw.flush();


        int i = 0;
        for (SortableSemanticModel model : candidateModels) {
            ModelEvaluation me = model.evaluate(correctModel);

            DirectedWeightedMultigraph modelGraph = model.getSimpliedGraph();

            LGGraph modelLGGraph = VF2GraphAdapter.graphAdaptToVF2(modelGraph);

            long start = System.currentTimeMillis();

            printVF2Graph(modelLGGraph);
            VF2 vf2 = new VF2();
            ArrayList mappings = vf2.matchGraphSetWithQuery(knowledgeGraph, modelLGGraph);
            int mapFrq = mappings.size();
//            int mapFrq = 10000000;
            double precision = me.getPrecision();
            double recall = me.getRecall();
            double runtime = (System.currentTimeMillis() - start)/1000.0;
            fw.append("model_"+i+'\t'+mapFrq+'\t'+runtime+'\t'+precision+'\t'+recall+'\n');
            fw.flush();
            i++;
        }

        fw.close();

    }



}
