package au.edu.unisa;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

public class ObjectOfTriple {

    private String entityName = new String("");

    private List<String> codes = new ArrayList<String>();

    private List<String> nodes = new ArrayList<String>();

    private String value = new String("");

    private boolean isEntity = false;

    /**Default Constructor of 'Object'**/
    public ObjectOfTriple(){}

    /**Constructor of 'ObjectOfPredicate'.
     * Firstly, judge if a string ends with '_N***'. e.g., '_N78'*
     * For example, '_:Bank_Transaction_Record1_b7103ca278a75cad8f7d065acda0c2e80da0b7dc_N89 ends with '_N89'' should be
     * an entity in the linked data graph. However, '12350' should be a property of an entity
     * In this function, regular expression is used to validate if a string represents an entity or not
     *
     * if the result is true, it means that 'obj' represents an entity in the linked data graph
     * if the result is false, it means that 'obj' represents an attribute of an entity*/
    /**
     * @param obj object of predicate
     * **/
    public ObjectOfTriple(String obj){

        /**Get the first character of 'obj'**/
        char begin = obj.charAt(0);

        /**Get the last character of 'obj'**/
        char end = obj.charAt(obj.length()-1);

        /**judge a string if begin (end) with " (quotation mark) or not**/
        if( (!Character.toString(obj.charAt(0)).equals("\"")) && !(Character.toString(obj.charAt(obj.length()-1)).equals("\"")) ){

            /**search a string for the last occurrence of '_'**/
            int index_of_underline = obj.lastIndexOf(new String("_"));

            /**extract the sub-string between the last underline and the end of 'obj'**/
            String str_after_underline = obj.substring(index_of_underline+1,obj.length());

            /**judge if the first character of 'str_after_underline' is 'N' followed by numbers or not**/
            if(Utilities.verifyStr_beginwithN(str_after_underline)){

                setIsEntity(true);

                /**Segment the 'object' into different parts in terms of underline '_'**/
                List<String> strings = Utilities.segmentation(obj);

                /**Loop 'strings', distribute subject into different parts**/
                Iterator<String> it = strings.iterator();
                while (it.hasNext()){

                    String subStr = it.next();


                    /**The string represents the name of the subject if the string starts with a colon**/
                    if(Character.toString(subStr.charAt(0)).equals(":")){
                        /**remove the colon**/
                        subStr = subStr.substring(1);

                        setName(entityName.concat(subStr));

                    }

                    /**the string represents the code of the subject if the string contains lowercase alphabets and number from 0-9**/
                    else if(Utilities.verifyStr_40digits(subStr)){

                        codes.add(subStr);

                    }

                    /**The string represents the node of the subject if the string starts with uppercase alphabet 'N' followed by the digits*/
                    else if(Utilities.verifyStr_beginwithN(subStr)){

                        nodes.add(subStr);
                    }

                    else{

                        setName(entityName.concat("_").concat(subStr));
                    }


                }


            }
        }else if((Character.toString(obj.charAt(0)).equals("\"")) && (Character.toString(obj.charAt(obj.length()-1)).equals("\""))){

            setIsEntity(false);

            /**remove the beginning "\"" and the ending "\""**/
            String str = obj.substring(1,obj.length()-1);

            setValue(str);

        }

    }

    /**
     * @return name
     * **/
    public String getName(){

        return this.entityName;
    }

    /**
     * @param name name
     * **/
    public void setName(String name){

        this.entityName = name;
    }

    /**
     * @return a string of codes
     * **/
    public List<String> getCodes(){

        return this.codes;
    }

    /**
     * @param codes a string of codes
     * **/
    public void setCodes(List<String> codes){

        this.codes = codes;
    }

    /**
     * @return a string of nodes. Might be useless
     * **/
    public List<String> getNodes(){

        return this.nodes;
    }

    /**
     * @param nodes a string of nodes. Might be useless
     * **/
    public void setNodes(List<String> nodes){

        this.nodes = nodes;
    }

    /**
     * @return value
     * **/
    public String getValue(){

        return this.value;
    }

    /**
     * @param value value to be set
     * **/
    public void setValue(String value){

        this.value = value;
    }

    /**
     * @return if it's entity or not
     * **/
    public boolean getIsEntity(){

        return this.isEntity;
    }

    /**
     * @param isEntity bool value representing if it's entity or not
     * **/
    public void setIsEntity(boolean isEntity){

        this.isEntity = isEntity;
    }

    /**museum_crm_test function*
     * @param args input
     * */
    public static void main(String args[]){

        ObjectOfTriple objectOfPredicate = new ObjectOfTriple("_:Bank1_2bf18aad8483f26446edae4ddc7b1264eefd1d05_N26");
    }


}
