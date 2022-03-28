package edu.isi.karma.research.modeling;

import com.opencsv.CSVReader;
import edu.isi.karma.modeling.alignment.SemanticModel;
import edu.isi.karma.modeling.alignment.learner.ModelReader;
import edu.isi.karma.modeling.ontology.OntologyManager;
import edu.isi.karma.modeling.research.Params;
import edu.isi.karma.modeling.semantictypes.SemanticTypeColumnModel;
import edu.isi.karma.modeling.semantictypes.SemanticTypeLabel;
import edu.isi.karma.modeling.semantictypes.SemanticTypeUtil;
import edu.isi.karma.rep.alignment.ColumnNode;
import edu.isi.karma.rep.alignment.SemanticType;
import edu.isi.karma.semantictypes.typinghandler.HybridSTModelHandler;
import javafx.util.Pair;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SemanticLabeling {

    private static HybridSTModelHandler modelHandler;


    public static void main(String[] args) throws Exception {
        modelHandler = new HybridSTModelHandler("");
        modelHandler.removeAllLabels();
        modelHandler.setModelHandlerEnabled(true);
        for (int i = 0; i < 29; i++) {
            semanticLabeling(new Integer[]{1, 2, 3}, i);
        }

    }



    public static ArrayList<Pair<String, ArrayList<String>>> loadCSV(String csvFilePath) throws IOException {

        ArrayList<Pair<String, ArrayList<String>>> dataValues = new ArrayList<>();

        CSVReader csvReader = new CSVReader(new FileReader(csvFilePath));

        for (String s : csvReader.readNext()) {
            Pair<String, ArrayList<String>> pair = new Pair<>(s, new ArrayList<>());
            dataValues.add(pair);
        }

        for (String[] strings : csvReader) {
            for (int i = 0; i < strings.length; i++) {
                dataValues.get(i).getValue().add(strings[i]);
            }
        }

        return dataValues;
    }

    public static void addKnownModelForLabeling(SemanticModel model, ArrayList<Pair<String, ArrayList<String>>> trainExamples){
        for (ColumnNode cn : model.getColumnNodes()) {
            ArrayList<String> trainExample = new ArrayList<>();
            for (Pair<String, ArrayList<String>> example : trainExamples) {
                if(Objects.equals(example.getKey(), cn.getColumnName())){
                    trainExample = example.getValue();
                    break;
                }
            }
            String labelString = cn.getUserSemanticTypes().get(0).getModelLabelString();
            modelHandler.addType(labelString, trainExample);
        }

    }

    public static List<SemanticType> predictSemanticType(OntologyManager ontologyManager, ColumnNode cn, ArrayList<String> examples, int numPredictions){
        List<SemanticTypeLabel> labels = modelHandler.predictType(examples, numPredictions);
        if (labels == null){
            return null;
        }
        SemanticTypeUtil semanticTypeUtil = new SemanticTypeUtil();
        return semanticTypeUtil.getSuggestedTypes(ontologyManager, cn, new SemanticTypeColumnModel(labels));
    }



    public static void semanticLabeling(Integer[] trainIndex, int newSourceIndex) throws Exception {
        List<SemanticModel> semanticModels = ModelReader.importSemanticModelsFromJsonFiles(Params.ROOT_DIR + "models-json-20210830", Params.MODEL_MAIN_FILE_EXT);

        OntologyManager ontologyManager = new OntologyManager("");
        File oFile = new File(Params.ROOT_DIR+"ecrm_update(20190521).owl");
        ontologyManager.doImport(oFile, "UTF-8");
        ontologyManager.updateCache();

        File sourceDir = new File("D:\\ASM\\DataSets\\museum-crm\\sources-modified-20210828");


        for (Integer index : trainIndex) {
             addKnownModelForLabeling(semanticModels.get(index), loadCSV(Objects.requireNonNull(sourceDir.listFiles())[index].getAbsolutePath()));
        }

        SemanticModel newModel = semanticModels.get(newSourceIndex);

        for (ColumnNode cn : newModel.getColumnNodes()) {
            ArrayList<String> examples = new ArrayList<>();
            for (Pair<String, ArrayList<String>> example : loadCSV(Objects.requireNonNull(sourceDir.listFiles())[newSourceIndex].getAbsolutePath())) {
                if(Objects.equals(example.getKey(), cn.getColumnName())){
                    examples = example.getValue();
                    break;
                }
            }
            List<SemanticType> predictedTypes = predictSemanticType(ontologyManager, cn, examples,4);
            cn.setLearnedSemanticTypes(predictedTypes);
        }

        if (newSourceIndex < 9){
            newModel.writeJson("D:\\ASM\\DataSets\\museum-crm\\models-json-tmp\\s0"+String.valueOf(newSourceIndex+1)+".csv.model.json");
        }else {
            newModel.writeJson("D:\\ASM\\DataSets\\museum-crm\\models-json-tmp\\s"+String.valueOf(newSourceIndex+1)+".csv.model.json");
        }

    }

}
