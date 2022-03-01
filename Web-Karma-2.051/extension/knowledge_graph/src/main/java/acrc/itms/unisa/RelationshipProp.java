package acrc.itms.unisa;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**This class defines all the necessary parameters for generating KG with regard to a specified relationship type, e.g. lives in, works at,...*
 * From Oct 2018
 * */
public class RelationshipProp {

    private String name;//name of relationship type
    private Map<String, Double> coefficients = new HashMap<>();//BY Wolfgang
    private int weightedOption;//0 or 1. 0 means EQUAL, while 1 means INCREMENTAL for the weight paired between entities (e.g. University and BA)
    private double percentageS = 1.0;//if percentageS is equal p, we will pick up p percentage of source to pair with this relationship type
    private double percentageT = 1.0;//if percentageS is equal p, we will pick up p percentage of target to pair with this relationship type

    /**All the dependent patterns which are needed for inducing this relationship type**/
    private List<Pattern> dependentPatterns = new ArrayList<>();


    public void setName(String name){this.name = name;}

    public String getName(){return this.name;}

    public void setCoefficient(Map<String, Double> coefficients){this.coefficients = coefficients;}

    public Map<String, Double> getCoefficients(){return this.coefficients;}

    public void addCoefficient(String relationName, Double weight) {
        coefficients.put(relationName, weight);
    }

    public void setWeightedOption(int weightedOption){this.weightedOption = weightedOption;}

    public int getWeightedOption(){return this.weightedOption;}

    public void setPercentageS(double percentageS){this.percentageS = percentageS;}//added on 14 Nov 2018

    public double getPercentageS(){return this.percentageS;}//added on 14 Nov 2018

    public void setPercentageT(double percentageT){this.percentageT = percentageT;}//added on 14 Nov 2018

    public double getPercentageT(){return this.percentageT;}//added on 14 Nov 2018

    /**Get all the dependent patterns with regard to this relationship type*
     * @from 11 Feb 2019
     * @return all of the dependent patterns with regard to this relationship type
     * */
    public List<Pattern> getDependentPatterns () {
        return this.dependentPatterns;
    }

    /**Add a structural pattern with regard to this relationship type*
     * @param pattern the dependent pattern to be added
     * @from 11 Feb 2019
     * */
    public void addDependentPattern (Pattern pattern) {
        this.dependentPatterns.add(pattern);
    }

}
