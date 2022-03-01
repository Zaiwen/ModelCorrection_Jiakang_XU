package au.com.d2dcrc.yago2es;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.FileNotFoundException;

/**
 * Created by Zaiwen Feng on 18/04/2017.
 *
 * This class provides some utility functionality
 *
 */
public class Util {

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

    /**Compare two hash map. if they have the equivalent keys and values, return true, or return false**/
    /**
     *
     * @param m1 the first Map
     * @param m2 the second Map
     * @return if equal, return true
     */
    public static boolean equalMaps (Map<String, String> m1, Map<String, String> m2){

        if(m1.size() != m2.size()){

            return false;
        }
        for (String key : m1.keySet()){

            if(! m1.get(key).equals(m2.get(key))){

                return false;
            }
        }
        return true;
    }

    /**Print the vertex or edge label and its corresponding number to a file*
     * @param fileName the file name and directory to be written
     * @param numeratedLabels a hash map saving numerated vertices
     * */
    public static void printNumeratedLabelsToFile (String fileName, Map<String, Integer> numeratedLabels) {


        FileWriter fw = null;
        BufferedWriter bw = null;
        try{

            fw = new FileWriter(fileName);
            bw = new BufferedWriter(fw);

            Iterator it = numeratedLabels.entrySet().iterator();
            while (it.hasNext()) {

                Map.Entry pair = (Map.Entry)it.next();
                String label = pair.getKey().toString();
                String number = pair.getValue().toString();

                String line = number.concat(" ").concat(label);
                line = line + ";\n";
                bw.write(line);
            }
        }catch (IOException ex){

            ex.printStackTrace();
        }finally {
            try{

                if(bw != null)
                    bw.close();

                if(fw != null)
                    fw.close();

            }catch (IOException ex){

                ex.printStackTrace();
            }
        }

    }

    /**Read numerated label from files, used in YAGO evaluation*
     * @param fileName a file saving numerated labels
     * @return a hash map whose key is number and value is the corresponding label
     * */
    public static Map<Integer, String> readNumeratedLabelFromFile (String fileName){

        Map<Integer, String> numeratedLabelMap = new HashMap<Integer, String>();
        try{
            String line = null;
            int number = 0;
            String label = "";

            FileReader fileReader = new FileReader(fileName);
            BufferedReader br = new BufferedReader(fileReader);

            while ((line = br.readLine()) != null){
                number = Integer.parseInt(line.split(" ")[0]);
                label = line.split(" ")[1];
                numeratedLabelMap.put(number, label);

            }


        }catch (FileNotFoundException ex){

            ex.printStackTrace();
        }catch (IOException ex){

            ex.printStackTrace();
        }
        return numeratedLabelMap;
    }


    /**Print information of an edge into a file. It might not be useful in the future. However, currently it is used to filter the graph feature set. Added on 26 May 2017**/
    /**
     *
     * @param newEdgeInfo information of the newly added edge, including label of the start, label of the end, label for the edge
     * @param fileName file to be serialized
     * @param numeratedVertexLabels the mapping table from the label of the vertex to the number
     * @param numeratedEdgeLabels the mapping table from the label of the edge to the number
     */
    public static void printEdgeToFile (List<String> newEdgeInfo, String fileName, Map<String, Integer> numeratedVertexLabels, Map<String, Integer> numeratedEdgeLabels) {

        System.out.println("DEBUG....printEdgeToFile is called...");

        BufferedWriter bw = null;
        FileWriter fw = null;

        try{

            String info = "e";

            String startLabel = newEdgeInfo.get(0);//start label
            System.out.println("startLabel" + startLabel);
            String endLabel = newEdgeInfo.get(1); //end label
            System.out.println("endLabel" + endLabel);
            String edgeLabel = newEdgeInfo.get(2); //edge label
            System.out.println("edgeLabel" + edgeLabel);
            String commonLabel = newEdgeInfo.get(3);
            System.out.println("commonLabel" + commonLabel);

            info = info.concat(" ").concat(numeratedVertexLabels.get(startLabel).toString());
            info = info.concat(" ").concat(numeratedVertexLabels.get(endLabel).toString());
            info = info.concat(" ").concat(numeratedEdgeLabels.get(edgeLabel).toString());
            info = info.concat(" ").concat(numeratedEdgeLabels.get(commonLabel).toString());//newly added

            fw = new FileWriter(fileName);
            bw = new BufferedWriter(fw);
            bw.write(info);

        }catch (IOException ex){

            ex.printStackTrace();
        }finally {
            try{

                if(bw != null)
                    bw.close();

                if(fw != null)
                    fw.close();

            }catch (IOException ex){

                ex.printStackTrace();
            }
        }

    }

    /**Read edge info from the file. It's a reverse process of the method 'printEdgeToFile'**/
    /**
     *
     * @param fileName file that saves edge information. e.g. 'e 1 3 6'. which represents label of start, label of end, label of edge respectively
     * @return an array list that saves the label of start, label of end and the label of edge respectively
     */

    public static List<Integer> readEdgeFromFile (String fileName){

        List<Integer> edgeInfoList = new ArrayList<Integer>();
        try{
            String line = null;
            int startPointLabel = 0;
            int endPointLabel = 0;
            int edgeWeight = 0;
            int commonLabel = 0;

            FileReader fileReader = new FileReader(fileName);
            BufferedReader br = new BufferedReader(fileReader);

            while ((line = br.readLine()) != null){
                if(line.substring(0, 1).equals("e")){

                    startPointLabel = Integer.parseInt(line.split(" ")[1]);
                    endPointLabel = Integer.parseInt(line.split(" ")[2]);
                    edgeWeight = Integer.parseInt(line.split(" ")[3]);
                    commonLabel = Integer.parseInt(line.split(" ")[4]);

                    edgeInfoList.add(startPointLabel);
                    edgeInfoList.add(endPointLabel);
                    edgeInfoList.add(edgeWeight);
                    edgeInfoList.add(commonLabel);
                }


            }


        }catch (FileNotFoundException ex){

            ex.printStackTrace();
        }catch (IOException ex){

            ex.printStackTrace();
        }
        return edgeInfoList;
    }

    /**
     * Write a two-dimensional array into a CSV file
     * **/
    /**
     *     *     attr1 attr2 attr3 attr4  label(optional)
     *//**  0      1     0     1      1
     *      1      0     1     1      0
     *      1      1     1     1      0
     *      0      0     1     1      0
     *      1      0     1     1      1
     *      THe source data format from pipeline
     *      attr1 0 1 1 0 1
     *      attr2 1 0 1 0 0
     *      attr3 0 1 1 1 1
     *      attr4 1 1 1 1 1
     *      label 1 0 0 0 1 **
     * @param array an two-dimensional array representing design matrix
     * @param csvFile CSV file and directory
     * @param flag 0: write training set into CSV (with 'label' column); 1: write testing set into CSV (without 'label' column)
     *                The target data format in CSV file
     * @throws Exception If writing a matrix to files fails  **/

    public static void writeMatrixToCSV (int[][] array, String csvFile, int flag) throws Exception {

        FileWriter writer = new FileWriter(csvFile);

        List<String> MatrixTitle = new ArrayList<String>();

        /**Write the first row of the CSV file - for example, fs_1, fs_2, fs_3, ... , fs_n, label**/
        if (flag == 0) {
            for (int i = 0; i < array.length - 1; i ++) {

                String string = "fs_" + i;
                MatrixTitle.add(string);
            }
            /**write the column of 'label'**/
            MatrixTitle.add("label");
        } else {

            for (int i = 0; i < array.length; i ++) {

                String string = "fs_" + i;
                MatrixTitle.add(string);
            }
        }

        CSVUtils.writeLine(writer, MatrixTitle);

        /**write all of the feature data value into CSV file**/
        for (int i = 0; i < array[0].length; i ++) {

            List<String> featureSet = new ArrayList<String>();
            for (int j = 0; j < array.length; j ++) {

                Integer featureValue = array[j][i];

                featureSet.add(featureValue.toString());
            }
            CSVUtils.writeLine(writer, featureSet);
        }

        writer.flush();
        writer.close();
    }

    /**Bubble sort*
     * @param intArray the int array to be sorted
     * */
    public static void bubbleSort (int[] intArray) {

                        /*
                 * In bubble sort, we basically traverse the array from first
                 * to array_length - 1 position and compare the element with the next one.
                 * Element is swapped with the next element if the next element is greater.
                 *
                 * Bubble sort steps are as follows.
                 *
                 * 1. Compare array[0] & array[1]
                 * 2. If array[0] > array [1] swap it.
                 * 3. Compare array[1] & array[2]
                 * 4. If array[1] > array[2] swap it.
                 * ...
                 * 5. Compare array[n-1] & array[n]
                 * 6. if [n-1] > array[n] then swap it.
                 *
                 * After this step we will have largest element at the last index.
                 *
                 * Repeat the same steps for array[1] to array[n-1]
                 *
                 */
        int n = intArray.length;
        int temp = 0;

        for (int i = 0; i < n; i++) {

            for (int j = 1; j < (n-i); j++) {

                if (intArray[j - 1] > intArray[j]) {

                    /**swap the elements**/

                        temp = intArray[j-1];
                        intArray[j-1] = intArray[j];
                        intArray[j] = temp;
                }

            }
        }

    }

    /**
     * Convert file from gSpan format to dot format, reused from DPC code
     *
     * @param gSpanFile input file
     * @param dotFile   output file
     */
    public static void GSpanToDOT(String gSpanFile, String dotFile) {
        String line;
        String vertex1, vertex2, weight;
        String edge;

        // store graph metadata
        Vector vertexes = new Vector();           //e.g: [v0, v1, v2, v3]
        HashMap labels = new HashMap();           //e.g: {v1=COP,v0=GOV,v3=REF,v2=BUS}
        ArrayList edges = new ArrayList();        //e.g: [(v0,v1), (v2,v1), (v3,v0), (v0,v1), (v0,v0), (v1,v0)]
        ArrayList weights = new ArrayList();      //edge String list e.g: [(v0,v1): 10, (v2,v1): 19, (v3,v0): 17, ...]

        try {
            FileReader fileReader = new FileReader(gSpanFile);
            BufferedReader bfr = new BufferedReader(fileReader);

            while ((line = bfr.readLine()) != null) {
                // get vertex and its label
                if (line.substring(0, 1).equals("v")) {
                    vertexes.add("v" + line.split(" ")[1]);
                    labels.put("v" + line.split(" ")[1], line.split(" ")[2]);
                }
                // get edge and its weight
                else {
                    vertex1 = "v" + line.split(" ")[1];
                    vertex2 = "v" + line.split(" ")[2];

                    edge = "(" + vertex1 + "," + vertex2 + ")";
                    weight = edge + ": " + line.split(" ")[3];
                    edges.add(edge);
                    weights.add(weight);
                }
            }
            bfr.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            // output to dot file
            FileWriter fileWriter = new FileWriter(dotFile);
            BufferedWriter bfw = new BufferedWriter(fileWriter);

            bfw.write("digraph G {\n");

            // output vertexes
            for (int i = 0; i < vertexes.size(); i++) {
                line = "\t" + vertexes.get(i) + " [label=\"" + labels.get(vertexes.get(i).toString()) + "\"];\n";
                bfw.write(line);
            }
            bfw.write("\n");

            // output edges
            for (int i = 0; i < edges.size(); i++) {
                // hardcode the substring
                vertex1 = edges.get(i).toString().substring(1, edges.get(i).toString().length() - 1).split(",")[0];
                vertex2 = edges.get(i).toString().substring(1, edges.get(i).toString().length() - 1).split(",")[1];
                line = "\t" + vertex1 + " -> " + vertex2;

                weight = weights.get(i).toString().split(":")[1];

                //weight = weights.get(i).toString().split(":")[1].substring(1, 3);
//                if (Integer.parseInt(weight) < 11) {
//                    line = line + " [color=blue, label=" + weight + "]";
//                } else {
//                    line = line + " [color=red, label=" + weight + "]";
//                }
                line = line + " [color=red, label=" + weight + "]";
                line = line + ";\n";
                bfw.write(line);
            }
            bfw.write("}");
            bfw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Convert file from gSpan format to dot format with vertex label
     *
     * @param gSpanFile input file
     * @param numeratedEdgeFile numerated edge file
     * @param  numeratedVertexFile numerated vertex file
     * @param dotFile   output file
     */
    public static void GSpanToDOT(String gSpanFile, String numeratedVertexFile, String numeratedEdgeFile, String dotFile) {
        String line;
        String vertex1, vertex2, weight;
        String edge;

        // store graph metadata
        Vector vertexes = new Vector();           //e.g: [v0, v1, v2, v3]
        HashMap labels = new HashMap();           //e.g: {v1=COP,v0=GOV,v3=REF,v2=BUS}
        ArrayList edges = new ArrayList();        //e.g: [(v0,v1), (v2,v1), (v3,v0), (v0,v1), (v0,v0), (v1,v0)]
        ArrayList weights = new ArrayList();      //edge String list e.g: [(v0,v1): 10, (v2,v1): 19, (v3,v0): 17, ...]

        /**Get the numerated label maps from the file**/
        Map<Integer, String> numeratedVertexLabel = readNumeratedLabelFromFile(numeratedVertexFile);
        Map<Integer, String> numeratedEdgeLabel = readNumeratedLabelFromFile(numeratedEdgeFile);

        try {
            FileReader fileReader = new FileReader(gSpanFile);
            BufferedReader bfr = new BufferedReader(fileReader);

            while ((line = bfr.readLine()) != null) {
                // get vertex and its label
                if (line.substring(0, 1).equals("v")) {
                    vertexes.add("v" + line.split(" ")[1]);
                    labels.put("v" + line.split(" ")[1], line.split(" ")[2]);
                }
                // get edge and its weight
                else {
                    vertex1 = "v" + line.split(" ")[1];
                    vertex2 = "v" + line.split(" ")[2];

                    edge = "(" + vertex1 + "," + vertex2 + ")";
                    weight = edge + ": " + line.split(" ")[3];
                    edges.add(edge);
                    weights.add(weight);
                }
            }
            bfr.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            // output to dot file
            FileWriter fileWriter = new FileWriter(dotFile);
            BufferedWriter bfw = new BufferedWriter(fileWriter);

            bfw.write("digraph G {\n");

            // output vertexes
            for (int i = 0; i < vertexes.size(); i++) {
                Integer numeratedLabel = Integer.valueOf(labels.get(vertexes.get(i).toString()).toString()) ;
                String actualLabel = numeratedVertexLabel.get(numeratedLabel);
                /**trim the label**/
                int size = actualLabel.split("_").length;
                if (actualLabel.split("_")[0].equals("<wordnet")) {

                    if (size == 3) {

                        actualLabel = actualLabel.split("_")[1];

                    }else if (size > 3) {

                        String tempLabel = actualLabel.split("_")[1];
                        for (int m = 2; m < (size - 1); m++) {

                            String part = actualLabel.split("_")[m];
                            tempLabel = tempLabel.concat("_").concat(part);
                        }
                        actualLabel = tempLabel;
                    }
                }else if (actualLabel.split("_")[0].equals("<wikicat")) {

                    String tempLabel = actualLabel.split("_")[1];
                    for (int m = 2; m < size; m++) {

                        String part = actualLabel.split("_")[m];
                        if (part.contains(">;")) {

                            part = part.substring(0,part.length()-2);
                        }
                        tempLabel = tempLabel.concat("_").concat(part);
                    }
                    actualLabel = tempLabel;
                }else if (actualLabel.contains("<owl:thing>")) {

                    actualLabel = actualLabel.substring(1, actualLabel.length()-2);
                }


                line = "\t" + vertexes.get(i) + " [label=\"" + actualLabel + "\"];\n";
                bfw.write(line);
            }
            bfw.write("\n");

            // output edges
            for (int i = 0; i < edges.size(); i++) {
                // hardcode the substring
                vertex1 = edges.get(i).toString().substring(1, edges.get(i).toString().length() - 1).split(",")[0];
                vertex2 = edges.get(i).toString().substring(1, edges.get(i).toString().length() - 1).split(",")[1];
                line = "\t" + vertex1 + " -> " + vertex2;

                weight = weights.get(i).toString().split(":")[1].substring(1,weights.get(i).toString().split(":")[1].length());

                String edgeLabel = numeratedEdgeLabel.get(Integer.valueOf(weight.toString()));
                if (edgeLabel.substring(0,edgeLabel.length()-1).equals("X")) {

                    line = line + " [color=red, label=" + numeratedEdgeLabel.get(Integer.valueOf(weight.toString())) + "]";
                }else {

                    line = line + " [color=blue, label=" + numeratedEdgeLabel.get(Integer.valueOf(weight.toString())) + "]";
                }

                line = line + ";\n";
                bfw.write(line);
            }
            bfw.write("}");
            bfw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
