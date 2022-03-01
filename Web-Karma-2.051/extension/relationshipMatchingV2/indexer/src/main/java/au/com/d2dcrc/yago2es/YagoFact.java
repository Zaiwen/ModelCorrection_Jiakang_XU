package au.com.d2dcrc.yago2es;
/**
 * Created by Zaiwen FENG on 26/06/2017.
 */
/**Each object of this class represents an entites**/
public class YagoFact {

    //private String id;
    private String subject;
    private String predicate;
    private String object;


    /**Default Constructor of Yago Fact*
     * @param subject subject of the fact
     * @param predicate predict of the fact
     * @param object object of the fact
     * */
    public YagoFact (String subject, String predicate, String object){

        this.subject = subject;
        this.predicate = predicate;
        this.object = object;
    }


    public void setSubject (String subject) {

        this.subject = subject;
    }

    public String getSubject () {

        return this.subject;
    }

    public void setPredicate (String predicate) {

        this.predicate = predicate;
    }

    public String getPredicate () {

        return this.predicate;
    }

    public void setObject (String object) {

        this.object = object;
    }

    public String getObject () {

        return this.object;
    }


}
