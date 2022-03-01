package au.com.d2dcrc.yago2es;

/**
 * Created by Zaiwen Feng on 18/04/2017
 */
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**Subject of Triple**/
public class Subject {

    private String name = new String("");

    private List<String> codes = new ArrayList<String>();

    private List<String> nodes = new ArrayList<String>();

    /**default constructor**/
    public Subject(){}

    /**Constructor of 'Subject'
     * @param string subject string from Karma output RDF**/
    public Subject(String string){

        List<String> strings = Util.segmentation(string);

        /**Loop 'strings', distribute subject into different parts**/
        Iterator<String> it = strings.iterator();
        while (it.hasNext()){

            String subStr = it.next();


            /**The string represents the name of the subject if the string starts with a colon**/
            if(Character.toString(subStr.charAt(0)).equals(":")){
                /**remove the colon**/
                subStr = subStr.substring(1);

                setName(name.concat(subStr));

            }

            /**the string represents the code of the subject if the string contains lowercase alphabets and number from 0-9**/
            else if(Util.verifyStr_40digits(subStr)){

                codes.add(subStr);

            }

            /**The string represents the node of the subject if the string starts with uppercase alphabet 'N' followed by the digits*/
            else if(Util.verifyStr_beginwithN(subStr)){

                nodes.add(subStr);
            }

            else{

                setName(name.concat("_").concat(subStr));
            }


        }


    }
    /**
     * @return name of subject
     * **/
    public String getName(){

        return name;
    }

    /**
     * @param name of subject
     * **/
    public void setName(String name){

        this.name = name;
    }

    /**
     * @return a string of codes
     * **/
    public List<String> getCodes(){

        return codes;
    }

    /**
     * @param codes a string codes
     * **/
    public void setCodes(List<String> codes){

        this.codes = codes;

    }

    /**
     * @return a list of nodes
     * **/
    public List<String> getNodes(){

        return nodes;
    }

    /**
     * @param nodes a list of nodes
     * **/
    public void setNodes(List<String> nodes){

        this.nodes = nodes;
    }



    /*test function
    *
    * */
//    public static void main(String args[]){
//
//        String str = new String("_:Person_Name1_4beb46d36a8d0d77bfa696083bfeab4b6ee156d4_N118_dcad3e2ef36ee9bd8bf15f1bbb5f0fe61a6b4625_N121");
//
//        Subject subject = new Subject(str);
//
//        System.out.println(subject.getName());
//
//        for(String code : subject.getCodes()){
//
//            System.out.println(code);
//        }
//
//        for(String node : subject.getNodes()){
//
//            System.out.println(node);
//        }
//
//
//    }

}
