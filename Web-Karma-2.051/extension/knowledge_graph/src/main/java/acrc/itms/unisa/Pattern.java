package acrc.itms.unisa;

/**pattern that induces a specified relationship type*
 * @created 16 Feb 2019
 * */
public class Pattern {
    private Integer id;
    private String inducedRT;//induced relationship type, e.g. transfer to
    private String type; //type of pattern. So far, Unique cycling dependency, cycling dependency, stub correlated...

    public void setId (Integer id) {
        this.id = id;
    }

    public void setPatternType (String type) {
        this.type = type;
    }

    public void setInducedRT (String inducedRT) {this.inducedRT = inducedRT; }

    public enum patternType {
        UNIQUE_CYCLING_DEPENDENCY,
        CYCLING_DEPENDENCY;

    }

    public String getType () {
        return type;
    }


}
