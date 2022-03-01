package au.com.d2dcrc.yago2es;

import org.junit.Test;

import java.util.*;

/**
 * Created by Zaiwen FENG on 26/06/2017.
 */
public class TestPrototypeByYago {


    public void testCanParseWithoutError() throws Exception {


        /**--------Phase 0: Create Linked Entity Graph based on Yago triples--------------------**/
        final YagoTtlParser parser = new YagoTtlParser();

        /**Get info for Yago relationship types**/
        Map<String, YagoRelationship> yagoRelationshipHashMap = parser.parseYagoSchema("/Users/fengz/Documents/Data_Modeling/YAGO/RDF-Graph@27June17/yagoSchema.ttl");

        List<YagoFact> yagofacts = parser.parseYagoFacts("/Users/fengz/Documents/Data_Modeling/YAGO/RDF-Graph@27June17/yagoFacts_10K.ttl");

        /**create Yago graph**/
        Graph yagoGraph = Graph.createYagoGraph(yagofacts, yagoRelationshipHashMap);

        /**Check all the vertices with more than 1 candidate labels in YagoGraph**/
        List<Vertex> allVertices = yagoGraph.getAllVertices();
        List<Vertex> allVerticesWithMultiplecandidateLables = new ArrayList<>();
        for (Vertex vertex : allVertices) {

            List<String> candidateLabels = vertex.getCandidateLabels();



            /**The scenario that the number of candidate labels is more than 1.**/
            if (candidateLabels.size() > 1) {

                allVerticesWithMultiplecandidateLables.add(vertex);
            }else if (candidateLabels.size() == 1) {

                /**The scenario that the number of candidate labels is 1. **/
                String label = candidateLabels.get(0);
                vertex.setData(label);

            }

        }

        /**Then specially, in terms of yagoSchema.ttl, each entities must be added label name based on instance name. This procedure is special for using Yago data**/
        /**Step1: Get the taxonomy tree of Yago**/
        List<YagoTtlParser.RelationValue> yagoTaxonomiesList = parser.parseYagoTaxonomy("/Users/fengz/Documents/Data_Modeling/YAGO/simple_taxonomy/yagoTaxonomy.ttl");

        /**Step2: Deal with vertices with multiple candidate labels**/
        /**Note: Here is our current rules for typing Yago entities (vertices). 1) if 'wordnet_' and 'yago_' both exist, filter 'yago_'
         * and get the top concept of 'wordnet_'; 2) If only 'wordnet_' exists, get the top concept of all; 3) If only 'yago_' exist, get the
         * top concept of 'yago_'**/
        for (Vertex vertex : allVerticesWithMultiplecandidateLables) {

            /**Get all the candidate label of this vertex**/
            List<String> candidateLabels = vertex.getCandidateLabels();

            List<String> tempListStartingWithWordNet = new ArrayList<>();

            /**Then fetch all the labels starting with '<wordnet'**/
            for (String candidateLabel : candidateLabels) {

                if (candidateLabel.substring(0, 8).equals("<wordnet")) {

                    tempListStartingWithWordNet.add(candidateLabel);

                }

            }

            /**Scenario 1  -- All the candidate labels start with '<wordnet'*
             * /**Scenario 2 -- Some candidate labels start with '<wordnet' and some other candidate labels start with <yago**
             * */
            if ((tempListStartingWithWordNet.size() <= candidateLabels.size()) && (tempListStartingWithWordNet.size() != 0)) {

                if (tempListStartingWithWordNet.size() == 1) {

                    String superClass = tempListStartingWithWordNet.get(0);
                    vertex.setData(superClass);

                }else {

                    String superClass = YagoTaxonomy.getSuperClass(tempListStartingWithWordNet, yagoTaxonomiesList);
                    vertex.setData(superClass);// assign to the vertex as the label

                }

            }
            /**Scenario 3 -- All the candidate labels start with '<yago'**/
            else if (tempListStartingWithWordNet.size() == 0) {

                String superClass = YagoTaxonomy.getSuperClass(candidateLabels, yagoTaxonomiesList);
                vertex.setData(superClass);
            }

        }

        System.out.println("Phase 1: Yago graph has been created...");


        /**--------Phase 1: Merge the external linked data **/
        System.out.println("Phase 1: No need to merge at all");

        /**-----------Phase 2: Slice the whole linked entity graph to boundary graphs with a specified relationship-----**/
        /**numerate the labels of the linked data graph and save the mapping relations to a hashmap**/
        Map<String, Integer> numeratedVertexLabels =  yagoGraph.numerateVertexLables();
        Map<String, Integer> numeratedEdgeLabels = yagoGraph.numerateEdgeLables();

        System.out.println("vertices and edges numerated...");

        /**a specified relationship, for example, "influences", or "isMarriedTo" between person and person**/
        List<Graph> boundaryGraphs = LinkedEntityGraph.slicingLinkedGraph(yagoGraph, "isMarriedTo", 2); //boundary graphs with relationship 'isMarriedTo'
        List<Graph> boundaryGraphs1 = LinkedEntityGraph.slicingLinkedGraph(yagoGraph, "influences", 2); //boundary graphs with relationship 'influences'

        List<String> newEdgeInfo = new ArrayList<>();//For the edge 'isMarriedTo'
        newEdgeInfo.add("<wordnet_person_100007846>");
        newEdgeInfo.add("<wordnet_person_100007846>");
        newEdgeInfo.add("isMarriedTo");

        List<String> newEdgeInfo1 = new ArrayList<>(); //For the edge 'influences'
        newEdgeInfo1.add("<wordnet_person_100007846>");
        newEdgeInfo1.add("<wordnet_person_100007846>");
        newEdgeInfo1.add("influences");

        Util.printEdgeToFile(newEdgeInfo, "/Users/fengz/Project/parsemis/Yago_edge_RT.lg", numeratedVertexLabels, numeratedEdgeLabels);
        Util.printEdgeToFile(newEdgeInfo1, "/Users/fengz/Project/parsemis/Yago_edge_non_RT.lg", numeratedVertexLabels, numeratedEdgeLabels);

        System.out.println("Phase 2: slicing linked graph to boundary graphs is finished...");

        /**
         * -----------Phase 3: Acquire Frequent subgraphs through gSpan-------------------
         * **/
        /**Renumber the vertices and edges of the boundary graphs with relationship 'isMarriedTo'**/
        Iterator<Graph> iterator = boundaryGraphs.iterator();
        while (iterator.hasNext()){

            Graph boundaryGraph = iterator.next();
            boundaryGraph.reOrderLabel(numeratedVertexLabels,numeratedEdgeLabels,boundaryGraph);
        }

        GSpanWrapper.writeToInputFile(boundaryGraphs,  "/Users/fengz/Project/parsemis/Yago_RT.lg");//Numerated boundary graphs for relationship type "influences"
        System.out.println("Phase 3: gSpan input file for the Yago RT (e.g. 'influences') has been prepared...");

        /**Renumber the vertices and edges of the boundary graphs with relationship 'influences'**/
        Iterator<Graph> iterator1 = boundaryGraphs1.iterator();
        while (iterator1.hasNext()){

            Graph boundaryGraph1 = iterator1.next();
            boundaryGraph1.reOrderLabel(numeratedVertexLabels,numeratedEdgeLabels,boundaryGraph1);
        }

        GSpanWrapper.writeToInputFile(boundaryGraphs1,  "/Users/fengz/Project/parsemis/Yago_non_RT.lg");//Numerated boundary graphs for the relationship type 'isMarriedTo'
        System.out.println("Phase 3: gSpan input file for the non-RT (e.g. 'isMarriedTo') has been prepared...");

        /******gSpan Execution!***********/


    }
}
