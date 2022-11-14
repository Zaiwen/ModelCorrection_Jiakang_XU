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


    private static final HybridSTModelHandler modelHandler = new HybridSTModelHandler("");


    public static void main(String[] args) throws Exception {


//        System.exit(1);
        removeKnownModels();
//        System.exit(1);
//        semanticLabeling(new Integer[]{1, 4, 5}, 0);
        for (int i = 0; i < 15; i++) {
            if (i != 12){
                continue;
            }

//            System.out.println(i);
            semanticLabeling(new Integer[]{0, 1, 5, 6, 7, 12, 13, 14}, i);
//            semanticLabeling(i);

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
                if (strings[i].length() > 0){
                    dataValues.get(i).getValue().add(strings[i]);
                }

//                dataValues.get(i).getValue().add(strings[i]);
            }
        }



        return dataValues;
    }


    public static void addKnownModelsForLabeling(SemanticModel model, ArrayList<Pair<String, ArrayList<String>>> trainExamples){
        for (ColumnNode cn : model.getColumnNodes()) {
            ArrayList<String> trainExample = new ArrayList<>();
            for (Pair<String, ArrayList<String>> example : trainExamples) {
                if(Objects.equals(example.getKey(), cn.getColumnName())){
                    trainExample = example.getValue();
                    break;
                }
            }
            String labelString = cn.getUserSemanticTypes().get(0).getModelLabelString();
            boolean savingSuccessful = modelHandler.addType(labelString, trainExample);
            if (!savingSuccessful){
                System.out.println("fail to add a type!!!");
            }
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



    public static void semanticLabeling(int newSourceIndex) throws Exception {
        removeKnownModels();
//        List<SemanticModel> semanticModels = ModelReader.importSemanticModelsFromJsonFiles(Params.ROOT_DIR + "models-json-20220522", Params.MODEL_MAIN_FILE_EXT);
        List<SemanticModel> semanticModels = ModelReader.importSemanticModelsFromJsonFiles(Params.ROOT_DIR + "models-json_20220920", Params.MODEL_MAIN_FILE_EXT);
//        List<SemanticModel> semanticModels = ModelReader.importSemanticModelsFromJsonFiles(Params.ROOT_DIR + "models-json-20220801", Params.MODEL_MAIN_FILE_EXT);

        OntologyManager ontologyManager = new OntologyManager("");
//        File oFile = new File(Params.ROOT_DIR+"ecrm_update(20190521).owl");
        File oFile = new File(Params.ROOT_DIR+"weapon.owl");
//        File oFile = new File(Params.ROOT_DIR+"edm.owl");


        ontologyManager.doImport(oFile, "UTF-8");
        ontologyManager.updateCache();

//        File sourceDir = new File(Params.ROOT_DIR+"sources-modified-20210828");
        File sourceDir = new File(Params.ROOT_DIR+"sources-modified");
        SemanticModel newModel = semanticModels.get(newSourceIndex);

        for (int i = 0; i < Objects.requireNonNull(sourceDir.listFiles()).length; i++) {
            if(i != newSourceIndex){
                addKnownModelsForLabeling(semanticModels.get(i), loadCSV(Objects.requireNonNull(sourceDir.listFiles())[i].getAbsolutePath()));
            }
//            addKnownModelsForLabeling(semanticModels.get(i), loadCSV(Objects.requireNonNull(sourceDir.listFiles())[i].getAbsolutePath()));
        }




        for (ColumnNode cn : newModel.getColumnNodes()) {
            ArrayList<String> examples = new ArrayList<>();
            for (Pair<String, ArrayList<String>> example : loadCSV(Objects.requireNonNull(sourceDir.listFiles())[newSourceIndex].getAbsolutePath())) {
                if(Objects.equals(example.getKey(), cn.getColumnName())){
                    examples = example.getValue();
                    break;
                }
            }
            List<SemanticType> predictedTypes = predictSemanticType(ontologyManager, cn, examples,4);
            cn.setLearnedSemanticTypes(null);
//            System.out.println(predictedTypes);
            if (!(predictedTypes == null || predictedTypes.isEmpty() || predictedTypes.get(0).getConfidenceScore() == 0)) {
                cn.setLearnedSemanticTypes(predictedTypes);
            }
        }

        newModel.writeJson(Params.ROOT_DIR + "models-json-tmp\\" + newModel.getName() + ".model.json");

//        if (newSourceIndex < 9){
//            newModel.writeJson(Params.ROOT_DIR + "models-json-tmp\\s0"+String.valueOf(newSourceIndex+1)+".csv.model.json");
//        }else {
//            newModel.writeJson(Params.ROOT_DIR + "models-json-tmp\\s"+String.valueOf(newSourceIndex+1)+".csv.model.json");
//        }

        System.out.println(newModel.getName());


    }


    public static void semanticLabeling(Integer[] trainIndex, int newSourceIndex) throws Exception {

        removeKnownModels();
        List<SemanticModel> semanticModels = ModelReader.importSemanticModelsFromJsonFiles(Params.ROOT_DIR + "models-json_20220920", Params.MODEL_MAIN_FILE_EXT);
//        List<SemanticModel> semanticModels = ModelReader.importSemanticModelsFromJsonFiles(Params.ROOT_DIR + "models-json-20220801", Params.MODEL_MAIN_FILE_EXT);
//        List<SemanticModel> semanticModels = ModelReader.importSemanticModelsFromJsonFiles(Params.ROOT_DIR + "models-json-20220522", Params.MODEL_MAIN_FILE_EXT);

        OntologyManager ontologyManager = new OntologyManager("");
//        File oFile = new File(Params.ROOT_DIR+"edm.owl");
        File oFile = new File(Params.ROOT_DIR+"weapon.owl");

        ontologyManager.doImport(oFile, "UTF-8");
        ontologyManager.updateCache();


        File sourceDir = new File(Params.ROOT_DIR+"\\sources-modified");


        for (Integer index : trainIndex) {
             addKnownModelsForLabeling(semanticModels.get(index), loadCSV(Objects.requireNonNull(sourceDir.listFiles())[index].getAbsolutePath()));
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

//            System.out.println(predictedTypes);
            if (!(predictedTypes == null || predictedTypes.isEmpty() || predictedTypes.get(0).getConfidenceScore() == 0)) {
                cn.setLearnedSemanticTypes(predictedTypes);
            }
        }


        newModel.writeJson(Params.ROOT_DIR + "models-json-tmp\\" + newModel.getName() + ".model.json");

//        if (newSourceIndex < 9){
//            newModel.writeJson(Params.ROOT_DIR + "models-json-tmp\\s0"+String.valueOf(newSourceIndex+1)+".csv.model.json");
//        }else {
//            newModel.writeJson(Params.ROOT_DIR + "models-json-tmp\\s"+String.valueOf(newSourceIndex+1)+".csv.model.json");
//        }

        System.out.println(newModel.getName());


    }

    public static void removeKnownModels(){
        System.out.println(modelHandler.removeAllLabels());
    }

}
