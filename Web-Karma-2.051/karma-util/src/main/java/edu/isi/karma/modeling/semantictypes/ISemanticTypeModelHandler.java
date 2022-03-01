package edu.isi.karma.modeling.semantictypes;

import java.util.List;


public interface ISemanticTypeModelHandler {
	boolean addType(String label, List<String> examples);
	List<SemanticTypeLabel> predictType(List<String> examples, int numPredictions);
	boolean removeAllLabels();
	boolean readModelFromFile(String filepath);
	void setModelHandlerEnabled(boolean enabled);
	boolean readModelFromFile(String filepath, boolean isNumeric);
}
