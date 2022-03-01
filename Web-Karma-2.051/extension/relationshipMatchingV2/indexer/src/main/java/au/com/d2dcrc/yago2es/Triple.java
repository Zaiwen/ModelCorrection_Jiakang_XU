package au.com.d2dcrc.yago2es;

import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.parser.NxParser;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Zaiwen FENG on 6/04/2017.

 This class is used to represent a triple parsed from KARMA output RDF
 */
public class Triple {

    private Subject subject = new Subject();
    private Predicate predicate = new Predicate();
    private ObjectOfPredicate objectOfPredicate = new ObjectOfPredicate();

    public void setSubject(Subject subject){

        this.subject = subject;
    }

    public Subject getSubject(){

        return this.subject;
    }

    public void setPredicate(Predicate predicate){

        this.predicate = predicate;
    }

    public Predicate getPredicate(){

        return this.predicate;
    }

    public void setObjectOfPredicate(ObjectOfPredicate objectOfPredicate){

        this.objectOfPredicate = objectOfPredicate;
    }

    public ObjectOfPredicate getObjectOfPredicate(){

        return this.objectOfPredicate;
    }

    /**this function prints all of triples in a TURTLE file**/
    /**
     *
     * @param file_path path of this file
     */
    public static void printTriples(String file_path){
        try {

            FileInputStream is = new FileInputStream(file_path);

            NxParser nxp = new NxParser();
            nxp.parse(is);

            //System.out.println("the length of Node[] is " + nx.length);

            while(nxp.hasNext()){

                /**get a triple**/
                Node[] nx = nxp.next();

                System.out.println("Here is one of the triples");

                /**prints the subject**/
                System.out.println("subject");
                System.out.println(nx[0]);


                System.out.println("predicate");
                System.out.println(nx[1]);

                //System.out.println("\n");
                System.out.println("object");
                System.out.println(nx[2]);

                System.out.print("\n");


            }


        }catch (IOException e){

            e.printStackTrace();

        }


    }

    /**Parse a .ttl file into a set of triples object, and populate them into an array list**/
    /**
     *
     * @param file_path path of this file of Karma output
     * @return a list of triples
     */
    public static List<Triple> parseTriples(String file_path){

        List<Triple> triples = new ArrayList<Triple>();

        List<Triple> filteredTriples = new ArrayList<Triple>();

        try{

            FileInputStream is = new FileInputStream(file_path);

            NxParser nxp = new NxParser();

            nxp.parse(is);

            while(nxp.hasNext()){

                /**get a triple**/
                Node[] nx = nxp.next();
                String subject = nx[0].toString();
                String predicate = nx[1].toString();
                String object = nx[2].toString();

                /**Create subject, predicate, and object of Predicate**/
                Subject subject1 = new Subject(subject);
                Predicate predicate1 = new Predicate(predicate);
                ObjectOfPredicate objectOfPredicate1 = new ObjectOfPredicate(object);

                /**create a triple **/
                Triple triple = new Triple();
                triple.setSubject(subject1);
                triple.setPredicate(predicate1);
                triple.setObjectOfPredicate(objectOfPredicate1);

                triples.add(triple);
            }

            /**filter the triples of which the predicate are '#type'**/
            filteredTriples = filterPredicate(triples, new String("type"));


        }catch (IOException e){

            e.printStackTrace();
        }

        return filteredTriples;
    }

    /**Filter the triple in which the predicate contains a special string 'str', from a triple array list named 'triples'*
     * @param triples a list of triples
     * @param str string that must be filtered
     * @return filtered list of triples
     * */
    public static List<Triple> filterPredicate(List<Triple> triples, String str){

        List<Triple> filteredTriples = new ArrayList<Triple>();

        /**loop 'triples'**/
        Iterator<Triple> it = triples.iterator();
        while (it.hasNext()){

            Triple triple = it.next();

            if(!triple.getPredicate().getPredicate().contains(str)){

                /**add the triple to 'filteredTriples'**/
                filteredTriples.add(triple);
            }

        }

        return filteredTriples;
    }

}
