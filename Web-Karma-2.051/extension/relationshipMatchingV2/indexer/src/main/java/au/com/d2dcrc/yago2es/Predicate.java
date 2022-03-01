package au.com.d2dcrc.yago2es;

/**
 * Created by Zaiwen Feng on 18/04/2017.
 */

/**Predicate of the Triple**/

public class Predicate {

    private String prefix = new String("");

    private String predicate = new String("");

    /**
     * constructor
     * **/
    public Predicate(){}

    /**Construction function of 'Predicate'
     * @param string predicate string from Karma output RDF**/

    public Predicate(String string){

        /**remove the angle bracket**/
        string = string.substring(1, string.length()-1);

        /**get the front part of string before sharp '#'**/
        String front_part = string.substring(0,string.indexOf('#'));

        /**get the rear part of a string after sharp '#'**/
        String rear_part = string.substring(string.indexOf('#')+1, string.length());

        setPrefix(front_part);

        setPredicate(rear_part);

    }

    /**
     * @return prefix
     * **/
    public String getPrefix(){

        return prefix;
    }

    /**
     * @param prefix prefix to be set
     * **/
    public void setPrefix(String prefix){

        this.prefix = prefix;
    }

    /**
     * @return predicate
     * **/
    public String getPredicate(){

        return predicate;
    }

    /**
     * @param predicate predicate to be set
     * **/
    public void setPredicate(String predicate){

        this.predicate = predicate;
    }

    /**test function*
     * @param args input args
     * */
    public static void main (String args[]){

        String str = new String("<http://www.w3.org/1999/02/22-rdf-syntax-ns#type>");

        str = str.substring(1, str.length()-1);

        System.out.println(str);

        String str1 = str.substring(0,str.indexOf('#'));

        String str2 = str.substring(str.indexOf('#')+1, str.length());

        System.out.println(str1);

        System.out.println(str2);

    }

}
