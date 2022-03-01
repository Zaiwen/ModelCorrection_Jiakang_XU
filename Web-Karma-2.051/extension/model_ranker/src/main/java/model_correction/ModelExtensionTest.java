package model_correction;

import edu.isi.karma.modeling.alignment.SemanticModel;
import edu.isi.karma.modeling.alignment.learner.ModelReader;
import edu.isi.karma.modeling.research.Params;
import edu.isi.karma.rep.alignment.ColumnNode;
import edu.isi.karma.rep.alignment.SemanticType;

import java.util.*;

public class ModelExtensionTest {
    public static void main(String[] args) throws Exception {

        List<SemanticModel> models = ModelReader.importSemanticModelsFromJsonFiles(
                Params.ROOT_DIR+"models-json-modified", Params.MODEL_MAIN_FILE_EXT);
        SemanticModel model = models.get(0);
        List<ColumnNode> nodes = model.getColumnNodes();
        ArrayList<String> labels = new ArrayList<>();
        for (ColumnNode node : nodes) {
            List<SemanticType> types = node.getTopKLearnedSemanticTypes(1);
            for (SemanticType type : types) {
                String typeName = type.getDomain().getDisplayName();
                typeName = typeName.substring(typeName.lastIndexOf("/")+1);
                labels.add(typeName);
            }
        }




        System.out.println(labels);

//        Gson gson = new Gson();
//        FileWriter fw = new FileWriter("E:\\extension\\s17_label.txt");
//        fw.write(gson.toJson(labels));
//        System.out.println(gson.toJson(labels));
//        fw.close();



    }
}
