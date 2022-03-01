package au.com.d2dcrc.yago2es;

/**
 * Created by Zaiwen FENG on 28/06/2017.
 */

/**This class aims to describe the relationship types in Yago dataset, including its type, domain and range**/
public class YagoRelationship {

    private String relationName = "";
    private String type = "";
    private String domain = "";
    private String range = "";

    /**Default Constructor**/
    public YagoRelationship () {}

    /**Constructor*
     * @param relationName name of relationship
     * @param type type
     * @param domain domain
     * @param range range
     * */
    public YagoRelationship (String relationName, String type, String domain, String range) {

        this.relationName = relationName;
        this.type = type;
        this.domain = domain;
        this.range = range;

    }

    public void setRelationName (String relationName) {

        this.relationName = relationName;
    }

    public String getRelationName () {

        return relationName;
    }

    public void setType (String type) {

        this.type = type;
    }

    public String getType () {

        return this.type;
    }

    public void setDomain (String domain) {

        this.domain = domain;
    }

    public String getDomain () {

        return this.domain;
    }

    public void setRange (String range) {

        this.range = range;
    }

    public String getRange () {

        return this.range;
    }

}
