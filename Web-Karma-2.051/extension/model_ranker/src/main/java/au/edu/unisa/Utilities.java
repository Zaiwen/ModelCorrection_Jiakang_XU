package au.edu.unisa;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utilities {

    /**print map**/
    public static void printMap(Map mp){
        Iterator it = mp.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            System.out.println(pair.getKey() + " = " + pair.getValue());
        }
    }

    /**bubble sort*
     * @create 7 Mar 2019
     * */
    public static int bubbleSort (int arr[]) {
        int min = 0;
        int n = arr.length;
        for (int i = 0; i < n-1; i++) {
            for (int j = 0; j < n-i-1; j++) {
                if (arr[j] > arr[j+1]) {
                    //swap arr[j+1] and arr[i]
                    int temp = arr[j];
                    arr[j] = arr[j+1];
                    arr[j+1] = temp;
                }
            }
        }
        min = arr[0];
        return min;
    }

    /**check if two sets are the same or not*
     * @created 18 April 2019
     *@param set1
     * @param set2
     * @return if set 1 is equal to set 2, return true.
     * */
    public static boolean isEqualSet (Set<?> set1, Set<?> set2) {

        if (set1 == null || set2 == null) {
            return false;
        }

        if (set1.size() != set2.size()) {
            return false;
        }

        return set1.containsAll(set2);
    }

    /**check if two hash maps are the same or not*
     * @created 18 April 2019
     *
     * */
    public static boolean isEqualMap (Map<?,?> map1, Map<?,?> map2) {

        if (map1 == null || map2 == null) {
            return false;
        }

        if (map1.size() != map2.size()) {
            return false;
        }

        //added on 30 April 2019
        if ((map1.size()==0) || (map2.size()==0)) {
            return false;
        }

        if (Utilities.isEqualSet(map1.keySet(),map2.keySet())) {
            //loop the first hash map
            Iterator<?> iterator = map1.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry pair = (Map.Entry)iterator.next();
                Object key = pair.getKey();
                Object value1 = pair.getValue();
                Object value2 = map2.get(key);
                if (value1.equals(value2)) {
                    continue;
                }else {
                    return false;
                }
            }
            return true;

        } else {
            return false;
        }

    }

    /**check if a string is numeric or not*
     * @from 6 May 2019
     * */
    public static boolean isNumeric (String string) {
        try {

            Double.parseDouble(string);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }

    }

    public static void test1 () {
        Map<String, String> asiaCaptial1 = new HashMap<>();
        asiaCaptial1.put("Japan", "Tokyo");
        asiaCaptial1.put("South Korea", "Seoul");
        asiaCaptial1.put("China", "Beijing");

        Map<String, String> asiaCapital2 = new HashMap<>();
        asiaCapital2.put("South Korea", "Seoul");
        asiaCapital2.put("Japan", "Tokyo");
        asiaCapital2.put("China", "Beijing");

        System.out.println(isEqualMap(asiaCaptial1,asiaCapital2));
    }

    /**verify if a string is 40 digits and just contains Lowercase alphabets and 0-9 numbers
     * for example, if the string is '2bf18aad8483f26446edae4ddc7b1264eefd1d05', it should return true*
     * However, if the string is 'Person' or 'Name1', it should return false value
     * @param string a string to be verified
     * @return true if the string is 40 digits and just contains Lowercase alphabets and 0-9 numbers
     * */
    public static boolean verifyStr_40digits(String string){

        boolean b = false;

        Pattern pattern = Pattern.compile("^(?=.*[a-z])(?=.*[0-9])[a-z0-9]{40}$");

        Matcher matcher = pattern.matcher(string);

        b= matcher.find();

        return b;
    }

    /**verify if a string begins with 'N' followed by numbers*
     * @param string a string to be verified
     * @return true if the string begins with 'N' followed by numbers
     * */
    public static boolean verifyStr_beginwithN(String string){

        boolean b = false;

        /**judge if the first character of 'str_after_underline' is 'N' or not**/
        String front_part = string.substring(0,1);

        if(front_part.equals(new String("N"))){

            String rear_part = string.substring(1,string.length());

            /**judge if 'rear_part' is numeric or not using regular expression**/
            String regex = "[0-9]+";

            if(rear_part.matches(regex)){

                b = true;

            }

        }


        return b;
    }

    /**String segmentation with underline '_'*
     * @param string string to be segmented
     * @return a list of segmented strings
     * */
    public static List<String> segmentation (String string){

        List<String> subStrings = new ArrayList<String>();

        /**Parse a string and convert a string into a string array with underline as delimiter**/
        String [] strings = string.split("_");

        for(String str : strings){

            /**Remove empty string**/
            if(str.equals("")){

                continue;
            }

            subStrings.add(str);


        }


        return subStrings;
    }



    public static void main (String args[]) {
        test1();
    }

}
