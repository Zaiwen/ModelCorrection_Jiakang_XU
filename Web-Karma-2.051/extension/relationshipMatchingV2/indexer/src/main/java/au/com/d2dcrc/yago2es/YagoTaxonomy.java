package au.com.d2dcrc.yago2es;

/**
 * Created by Zaiwen FENG on 30/06/2017.
 */

import java.util.*;
import java.util.Map;
import java.util.Map.Entry;

/**This class contains some functions that deal with taxonomy relationship of Yago**/
public class YagoTaxonomy {

    /**Return the super class from two classes (A and B). If there is not subclass relations, return 'owl thing'*
     *
     * @param A A yago class to be compared
     * @param B A yago class to be compared
     * @param yagoTaxonomiesList a list that contains all the subclass relationships in the Yago dataset
     * @return super class of A and B
     * */

    public static String getSuperClass (String A, String B, List<YagoTtlParser.RelationValue> yagoTaxonomiesList) {

        String superClass = "";

        /**if A or B is owl:thing, then return the super class 'owl:thing' directly**/
        if((A.equals("<owl:thing>") || A.equals("<owl:Thing>")) ||
                ((B.equals("<owl:thing>")) || (B.equals("<owl:Thing>")))) {

            superClass =  "<owl:thing>";
        }

        else {

            boolean flagA = false; //if this flag is false, it means we have not found the super class yet (searching from 'A')
            boolean flagB = false; //if this flag is false, it means we have not found the super class yet (searching from 'B')

            String temp = A;

            /**Try search from the string 'A'**/
            while (!flagA) {

                for (YagoTtlParser.RelationValue relationValue : yagoTaxonomiesList) {

                    String lower = relationValue.getSubject();
                    String uppper = relationValue.getObj();

                    if (A.equals(lower)) {

                        A = uppper;

                        if (A.equals("<owl:thing>") || A.equals("<owl:Thing>")) {
                            //which means, reaching the top of the Yago taxonomy
                            superClass = "<owl:thing>";
                            flagA = true;//If flag is true, it means we have reached the top of the taxonomy, and the loop should stop
                            break;

                        }else {

                            if (A.equals(B)) {

                                superClass = B;
                                flagA = true;//If flag is true, it means we have already found the super class, and the loop should stop
                                break;
                            }else {

                                /**keep searching towards the upper direction of the taxonomy**/
                                break;
                            }
                        }
                    }
                }
            }

            /**Now, let's search from the string 'B'**/
            if (superClass.equals("<owl:thing>" )|| superClass.equals("<owl:Thing>")) {

                A = temp; //assign the original value to 'A'

                superClass = "";

                /**Try search from the string 'B'**/
                while (!flagB) {

                    for (YagoTtlParser.RelationValue relationValue : yagoTaxonomiesList) {

                        String lower = relationValue.getSubject();
                        String uppper = relationValue.getObj();

                        if (B.equals(lower)) {

                            B = uppper;

                            if (B.equals("<owl:thing>")|| B.equals("<owl:Thing>")) {
                                //which means, reaching the top of the Yago taxonomy
                                superClass = "<owl:thing>";
                                flagB = true;//If flag is true, it means we have reached the top of the taxonomy, and the loop should stop
                                break;

                            }else {

                                if (B.equals(A)) {

                                    superClass = A;
                                    flagB = true;//If flag is true, it means we have already found the super class, and the loop should stop
                                    break;
                                }else {

                                    /**keep searching towards the upper direction of the taxonomy**/
                                    break;
                                }
                            }
                        }
                    }
                }
            }

        }

        return superClass;
    }

    /**
     * Return the super class from a list of concepts. If there is not subclass relations, return 'owl thing'
     * @param conceptList a list of concepts
     * @param yagoTaxonomiesList a list that contains all the subclass relationships in the Yago dataset
     * @return super class of the list of concepts
     * **/
    public static String getSuperClass (List<String> conceptList, List<YagoTtlParser.RelationValue> yagoTaxonomiesList) {

        String superClass = "";

        if (conceptList.size() == 2) {

            superClass = getSuperClass(conceptList.get(0), conceptList.get(1), yagoTaxonomiesList);

        }

        else if (conceptList.size() > 2) {

            for (int i = 0; i < (conceptList.size() - 1); i++) {

                superClass = getSuperClass(conceptList.get(i), conceptList.get(i+1), yagoTaxonomiesList);

                if (conceptList.get(i).equals(superClass)) {

                    /**swap the i element and the (i+1) element**/
                    Collections.swap(conceptList, i, i+1);

                }

            }

        }

        return superClass;
    }

    /**Return all of the low-level annotation of an Yago instance*
     *  @param instanceName the name of instance that needs to be typed,
     *  @param filePath the path of the file of YagoSimpleType.tll
     *  @return the type of this Yago instance
     * */
    public static List<String> getYagoTypes (String instanceName, String filePath) {

        List<String> yagoTypes = new ArrayList<String>();

        /**Create a Yago parser**/
        YagoTtlParser parser = new YagoTtlParser();

        /**First of all, get the java object of all the Yago Simple Types**/
        List<YagoTtlParser.RelationValue> allYagoTypeTriples = parser.parseYagoSimpleTypes(filePath);

        Iterator<YagoTtlParser.RelationValue> iterator = allYagoTypeTriples.iterator();
        while (iterator.hasNext()) {

            YagoTtlParser.RelationValue yagoTypeTriple = iterator.next();

            /**Get the subject name of this triple**/
            String subject = yagoTypeTriple.getSubject();

            if (instanceName.equals(subject)) {

                /**Get the name of Yago type**/
                String objectName = yagoTypeTriple.getObj();

                /**Add this type into the list**/
                yagoTypes.add(objectName);
            }
        }

        return yagoTypes;
    }

    /**Get the semantic type for a graph from multiple files*
     * @param filePaths path for multiple original files of Yago typing
     * @param graphs a list of graphs that needed to be typed (Normally it should be boundary graph)
     * @param yagoTaxonomiesList comprehensive Yago taxonomy list
     * @return  graph that has been typed
     * */
    public static List<Graph> getYagoTypes (List<Graph> graphs, List<String> filePaths, List<YagoTtlParser.RelationValue> yagoTaxonomiesList) {

        /**Create a Yago parser**/
        YagoTtlParser parser = new YagoTtlParser();

        /**Set up a new list to save all of the vertices for the list of graphs**/
        List<String> instanceNameList = new ArrayList<String>();

        for (Graph graph : graphs) {

            List<Vertex> allVertex = graph.getAllVertices();

            for (Vertex vertex : allVertex) {

                String instanceName = vertex.getInstanceName();
                instanceNameList.add(instanceName);

            }
        }

        /**Remove the duplicate instance name from the list**/
        Set<String> instanceNameSet = new HashSet<>();
        instanceNameSet.addAll(instanceNameList);
        instanceNameList.clear();
        instanceNameList.addAll(instanceNameSet);

        /**Set up a hashMap to save all of the lowest- level semantic type for every instance**/
        Map<String, List<String>> conceptListMap = new HashMap<>(instanceNameList.size());
        for (String instanceName : instanceNameList) {

            conceptListMap.put(instanceName, new ArrayList<>());

        }

        /**Get the lowest-level semantic type for each instance**/
        for (String filePath : filePaths) {

            /**get the java object from part of the Yago Simple Types**/
            List<YagoTtlParser.RelationValue> partOfYagoTypeTriples = parser.parseYagoSimpleTypes(filePath);
            int i = 1;

            for (String instanceName : instanceNameList) {

                /**Get the current type list for the instance**/
                List<String> currentTypeList = conceptListMap.get(instanceName);

                Iterator<YagoTtlParser.RelationValue> iterator = partOfYagoTypeTriples.iterator();
                while (iterator.hasNext()) {

                    YagoTtlParser.RelationValue yagoTypeTriple = iterator.next();

                    /**Get the subject name of this triple**/
                    String subject = yagoTypeTriple.getSubject();

                    if (instanceName.equals(subject)) {

                        /**Get the new Yago type for the instance**/
                        String newType = yagoTypeTriple.getObj();

                        /**Add this new Type to the current type list**/
                        if (!currentTypeList.contains(newType)) {

                            currentTypeList.add(newType);
                        }
                    }
                }

                /**Set the updated type list into the hash map**/
                conceptListMap.put(instanceName, currentTypeList);
                System.out.println("we have successfully typed " + i + " instances of Yago in the low-level!");
                i++;
            }
        }


        Map<String, String> conceptMap = new HashMap<>(instanceNameList.size());

        /**Loop the whole concept list map to get the semantic type for each instance**/
        Iterator<Map.Entry<String, List<String>>> entryIterator = conceptListMap.entrySet().iterator();
        int j = 1;
        while (entryIterator.hasNext()) {

            Map.Entry<String, List<String>> entry = entryIterator.next();
            String instanceName = entry.getKey();
            List<String> yagoCoarseType = entry.getValue();

            String yagoUniqueType = "";
            if (yagoCoarseType.size() > 0) {

                yagoUniqueType = getYagoUniqueType(instanceName, yagoCoarseType, yagoTaxonomiesList);
            } else {

                yagoUniqueType = new String("<owl:thing>");
            }



            System.out.println("we have successfully typed " + j + " instances of Yago!");
            conceptMap.put(instanceName, yagoUniqueType);
            j++;
        }

        /**Last, assign label to each vertex of the graph**/
        for (Graph graph : graphs) {

            List<Vertex> allVertex = graph.getAllVertices();

            for (Vertex vertex : allVertex) {

                String instanceName = vertex.getInstanceName();
                String yagoUniqueType = conceptMap.get(instanceName);
                vertex.setData(yagoUniqueType);
            }
        }

        return graphs;
    }

    /**Return all of the low-level semantic typing of an Yago instance， unused as the Yago type triples are 13 million too huge*
     * @param instanceName the name of instance that needs to be typed,
     * @param allYagoTypeTriples all the Yago type triples
     * @return low-level Yago types for an instance
     * */
    public static List<String> getYagoTypes (String instanceName, List<YagoTtlParser.RelationValue> allYagoTypeTriples) {

        List<String> yagoTypes = new ArrayList<>();

        Iterator<YagoTtlParser.RelationValue> iterator = allYagoTypeTriples.iterator();
        while (iterator.hasNext()) {

            YagoTtlParser.RelationValue yagoTypeTriple = iterator.next();

            /**Get the subject name of this triple**/
            String subject = yagoTypeTriple.getSubject();

            if (instanceName.equals(subject)) {

                /**Get the name of Yago type**/
                String objectName = yagoTypeTriple.getObj();

                /**Add this type into the list**/
                yagoTypes.add(objectName);
            }
        }

        return yagoTypes;
    }

    /**Return the unique high-level Yago type for an instance*
     * @param instanceName the name of instance to be typed,
     * @param yagoCoarseType comprehensive Yago lowest level type list for this instance,
     * @param yagoTaxonomiesList comprehensive Yago taxonomy list,
     * @return the unique high-level semantic type
     * */
    public static String getYagoUniqueType (String instanceName, List<String> yagoCoarseType,
                                            List<YagoTtlParser.RelationValue> yagoTaxonomiesList){

        String yagoUniqueType = "";

        /**First of all, get the size of all the low-level type for this instance**/
        int sizeOfAllType = yagoCoarseType.size();

        /**Then, define an array to contain flags**/
        List<Boolean> flagList = new ArrayList<>(sizeOfAllType);
        flagList.add(true);
        for (int i = 1; i < sizeOfAllType; i++) {

            flagList.add(false);
        }

        /**Next, get the whole super class list for the first coarse type**/
        List<String> allSuperOfFirstType = getAllSuperClasses(yagoCoarseType.get(0), yagoTaxonomiesList);

        /**Then, set up the super class list for each type **/
        List<List<String>> group = new ArrayList<List<String>>(sizeOfAllType);
        for (int i = 0; i < sizeOfAllType; i++) {

            String type = yagoCoarseType.get(i);
            List<String> superClassForEachType = getAllSuperClasses(type, yagoTaxonomiesList);
            group.add(superClassForEachType);
        }

        /**Then ,compare and get the unique common super concept**/
        for (String concept: allSuperOfFirstType) {


            for (int i = 1; i < sizeOfAllType; i++) {

                /**Get super class list for each type**/
                List<String> superClassListForEachType = group.get(i);

                /**If each list contains this concept, set flag true**/
                if (superClassListForEachType.contains(concept)) {

                    flagList.set(i, true);

                }
            }

            if (!flagList.contains(false)) {

                yagoUniqueType = concept;
                break;

            }else {

                /**clear all the super class list except first column**/
                for (int i = 1; i < sizeOfAllType; i++) {

                    flagList.set(i, false);
                }
            }

        }


        return yagoUniqueType;
    }

    /**Get the path from each instance to 'owl:thing'*
     * @param concept a concept
     * @param yagoTaxonomiesList the comprehensive class list until owl:thing
     * @return the whole ordered superclass list
     * */
    public static List<String> getAllSuperClasses (String concept, List<YagoTtlParser.RelationValue> yagoTaxonomiesList){

        List<String> allSuperClasses = new ArrayList<>();
        allSuperClasses.add(concept);

        System.out.println(concept);

        boolean flag = false;

        while (!flag) {

            for(YagoTtlParser.RelationValue relationValue : yagoTaxonomiesList) {

                /**Get a double of lower class and upper class **/
                String lower = relationValue.getSubject();
                String upper = relationValue.getObj();

                if(lower.equals(concept)) {

                    concept = upper;

                    System.out.println(concept);

                    allSuperClasses.add(concept);
                    if (concept.equals("<owl:thing>")) {

                        flag = true;
                        break;
                    }

                    break;
                }
            }
        }
        return allSuperClasses;
    }
}
