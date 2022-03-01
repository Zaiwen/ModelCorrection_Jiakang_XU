package ESM;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Before;
import org.junit.Test;

import edu.ucdenver.ccp.nlp.biolemmatizer.BioLemmatizer;
import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.DirectedSparseGraph;

/**
 * <br>Unit test cases to test the implementation of the Exact Subgraph Matching (ESM) algorithm</br>
 * <br></br>
 * @author Tested by Philippe Thomas
 * </br>
 *
 */


public class ESMTest {

    private DirectedGraph subgraph = null;
    private DirectedGraph graph = null;
    private ESM test;

    /** load BioLemmatizer */
    public static BioLemmatizer bioLemmatizer = new BioLemmatizer();

    /**test this class**/
    public static void main(String args[]){

        ESMTest test = new ESMTest();
        test.setUpClass();



    }




    @Before
    public void setUpClass() {
        String r = "nn(BIO_Entity1-14/NNP, Cells-17/NNS); nn(BIO_Entity1-14/NNP, BIO_Entity2-19/NNP)";
        subgraph = createGraph(r);
		/*
		for(Object v : subgraph.getVertices()) {
			Vertex x = (Vertex) v;
			System.out.println(x.getToken());
		}*/
        String s = "amod(BIO_Entity1-14/NNP, -Producing-15/JJ); nn(BIO_Entity1-14/NNP, Cells-17/NNS); nn(BIO_Entity1-14/NNP, BIO_Entity2-19/NNP); " +
                "nn(BIO_Entity1-14/NNP, BIO_Entity3-29/NNP); nn(BIO_Entity1-14/NNP, BIO_Entity4-39/NNP)";
        graph = createGraph(s);
        test = new ESM(subgraph, graph);
    }

    /**
     * Create graphs from dependency representation
     * @param r : input dependency representation separated by ";"
     * @return created dependency graph
     */
    public static DirectedGraph createGraph(String r) {
        DirectedSparseGraph<Vertex,Edge> graph = new DirectedSparseGraph<Vertex,Edge>();
        Map<String, Vertex> tokenToNode = new HashMap<String, Vertex>();
        /** dr: a single dependency representation */
        for ( String dr : r.split("\\s*;\\s*") ) {
            if ( ! dr.matches("^\\S+\\(\\S+\\s*,\\s*\\S+\\)\\s*$") )
                throw new RuntimeException("The dependency representation: "
                        + dr + " is not valid. Please check.");
            Matcher md = Pattern.compile("^(\\S+)\\((\\S+)\\s*,\\s*(\\S+)\\)\\s*$").matcher(dr);
            md.find();
            String label = md.group(1);
            String g = md.group(2);
            String d = md.group(3);

            Vertex gov;
            if(!tokenToNode.containsKey(g)) {
                gov = new Vertex(g);
                graph.addVertex(gov);
                tokenToNode.put(g, gov);
            }
            else { gov = tokenToNode.get(g); }

            Vertex dep;
            if(!tokenToNode.containsKey(d)) {
                dep = new Vertex(d);
                graph.addVertex(dep);
                tokenToNode.put(d, dep);
            }
            else { dep = tokenToNode.get(d); }

            Edge govToDep = new Edge(gov, label, dep);
            graph.addEdge(govToDep, gov, dep);
        }

        /** generates lemma for each node */
        generateLemmas(graph.getVertices());

        return graph;
    }

    /**
     * Generate lemma and generalized POS tag for each node in the graph
     * and set the correponding fields in the nodes for the node comparison process
     * @param nodes : nodes for which the lemma and generalized POS will be generated
     */
    public static void generateLemmas(Collection<Vertex> nodes) {
        try {
            for(Vertex node : nodes) {
                String lemma = node.getWord();
                lemma = lemma.replaceAll("-", "");
                if(lemma.matches("^BIO_Entity\\d*$")) {
                    lemma = "BIO_Entity";
                }
                else lemma = bioLemmatizer.lemmatizeByLexiconAndRules(lemma, node.getTag()).lemmasToString();
                lemma = lemma.replaceAll("\\d", "");
                node.setLemma(lemma.toLowerCase());

                String[] nounTags = {"NNS", "NNP", "NNPS", "NN"};
                String[] adjectiveTags = {"JJR", "JJS", "JJ"};
                //String[] adverbTags = {"RBR", "RBS", "RB"};
                String[] verbTags = {"VBD", "VBP", "VBZ", "VB", "VBG"};

                String tag = node.getTag();
                if(Arrays.asList(nounTags).contains(tag))
                    tag = "NN";
                else if(Arrays.asList(adjectiveTags).contains(tag))
                    tag = "JJ";
                    //else if(Arrays.asList(adverbTags).contains(tag))
                    //	tag = "RB";
                else if(Arrays.asList(verbTags).contains(tag))
                    tag = "VB";

                //set compare form
                node.setCompareForm( (lemma + " " + tag).toLowerCase() );
                node.setGeneralizedPOS(tag);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * Checking if two identical graphs are isomporphic, independent of token-Id and independent of edge ordering
     */
    @Test
    public void isGraphIsmomorphismTest(){
        String g1 = "nn(type-2/NN, Th1/Th2-1/NN); nsubj(cytokines-3/NNS, type-2/NN); nn(patients-7/NNS, hepatitis-5/NN); nn(patients-7/NNS, B-6/NN); prep_in(cytokines-3/NNS, patients-7/NNS); partmod(patients-7/NNS, treated-8/VBN); prep_with(treated-8/VBN, PROTEIN0-10/NN)";
        String g2 = "nn(type-20/NN, Th1/Th2-11/NN); partmod(patients-7/NNS, treated-8/VBN); prep_in(cytokines-3/NNS, patients-7/NNS); nn(patients-7/NNS, B-6/NN); nn(patients-7/NNS, hepatitis-5/NN); nsubj(cytokines-3/NNS, type-20/NN); prep_with(treated-8/VBN, PROTEIN0-10/NN)";


        DirectedGraph<Vertex,Edge> graph1 = createGraph(g1);
        DirectedGraph<Vertex,Edge> graph2 = createGraph(g2);

        ESM esm  = new ESM(graph1, graph2);
        assertEquals(true, esm.isGraphIsomorphism());
        assertEquals(true, esm.isSubgraphIsomorphism());
    }

    /**
     * Checking if two partially isomorphic graphs are isomorphic
     * Replaced dependency 'nn' with 'nns'
     */
    @Test
    public void isNotGraphIsmomorphismTest1(){
        String g1 = "nns(type-2/NN, Th1/Th2-1/NN); nsubj(cytokines-3/NNS, type-2/NN); nn(patients-7/NNS, hepatitis-5/NN); nn(patients-7/NNS, B-6/NN); prep_in(cytokines-3/NNS, patients-7/NNS); partmod(patients-7/NNS, treated-8/VBN); prep_with(treated-8/VBN, PROTEIN0-10/NN)";
        String g2 = "nn(type-20/NN, Th1/Th2-11/NN); partmod(patients-7/NNS, treated-8/VBN); prep_in(cytokines-3/NNS, patients-7/NNS); nn(patients-7/NNS, B-6/NN); nn(patients-7/NNS, hepatitis-5/NN); nsubj(cytokines-3/NNS, type-20/NN); prep_with(treated-8/VBN, PROTEIN0-10/NN)";


        DirectedGraph<Vertex,Edge> graph1 = createGraph(g1);
        DirectedGraph<Vertex,Edge> graph2 = createGraph(g2);

        ESM esm  = new ESM(graph1, graph2);
        assertEquals(false, esm.isGraphIsomorphism());
    }

    /**
     * Checking if two partially isomorphic graphs are isomorphic
     * Replaced 'Th1/Th2' with 'Th2/T1'
     */
    @Test
    public void isNotGraphIsmomorphismTest2(){
        String g1 = "nn(type-2/NN, Th1/Th2-1/NN); nsubj(cytokines-3/NNS, type-2/NN); nn(patients-7/NNS, hepatitis-5/NN); nn(patients-7/NNS, B-6/NN); prep_in(cytokines-3/NNS, patients-7/NNS); partmod(patients-7/NNS, treated-8/VBN); prep_with(treated-8/VBN, PROTEIN0-10/NN)";
        String g2 = "nn(type-20/NN, Th2/T1-11/NN); partmod(patients-7/NNS, treated-8/VBN); prep_in(cytokines-3/NNS, patients-7/NNS); nn(patients-7/NNS, B-6/NN); nn(patients-7/NNS, hepatitis-5/NN); nsubj(cytokines-3/NNS, type-20/NN); prep_with(treated-8/VBN, PROTEIN0-10/NN)";


        DirectedGraph<Vertex,Edge> graph1 = createGraph(g1);
        DirectedGraph<Vertex,Edge> graph2 = createGraph(g2);

        ESM esm  = new ESM(graph1, graph2);
        assertEquals(false, esm.isGraphIsomorphism());
    }


    /**
     * Checking subgraph isomorphism capabilities with one simple pattern  'conj_and(PROTEIN123-1/NNS, PROTEIN234-2/NNS)'
     * Note that Token, ID is again different and the POS tag has been changed from 'NN' to 'NNS'
     * Also 'PROTEIN0' has been replaced with 'PROTEIN123' and 'PROTEIN1' with 'PROTEIN234'
     */
    @Test
    public void isSubgraphIsmorphismTest1(){
        String g1 = "conj_and(PROTEIN0-39/NN, PROTEIN1-41/NN); nsubj(responsible-43/JJ, PROTEIN1-41/NN); cop(responsible-43/JJ, were-42/VBD); ccomp(revealed-37/VBD, responsible-43/JJ); nn(production-46/NN, PROTEIN2-45/NN); prep_for(responsible-43/JJ, production-46/NN); det(phase-50/NN, the-48/DT); amod(phase-50/NN, exponential-49/JJ); prep_during(responsible-43/JJ, phase-50/NN); prep_of(phase-50/NN, growth-52/NN); det(absence-55/NN, the-54/DT); prep_in(growth-52/NN, absence-55/NN); prep_of(absence-55/NN, KinA-57/NN); conj_and(KinA-57/NN, KinB-59/NN); prep_of(absence-55/NN, KinB-59/NN)";
        String p1 = "conj_and(PROTEIN123-1/NNS, PROTEIN234-2/NNS)";

        DirectedGraph<Vertex,Edge> graph = createGraph(g1);
        DirectedGraph<Vertex,Edge> pattern = createGraph(p1);

        ESM esm  = new ESM(pattern, graph);
        assertEquals(false, esm.isGraphIsomorphism());
        assertEquals(true, esm.isSubgraphIsomorphism());

        assertEquals(1, esm.getSubgraphMatchingMatches().size());
    }

    /**
     * Checking subgraph isomorphism capabilities with one simple pattern
     * 'conj_and' has been replaced by 'conj_or'
     */
    @Test
    public void isNotSubgraphIsmorphismTest1(){
        String g1 = "conj_and(PROTEIN0-39/NN, PROTEIN1-41/NN); nsubj(responsible-43/JJ, PROTEIN1-41/NN); cop(responsible-43/JJ, were-42/VBD); ccomp(revealed-37/VBD, responsible-43/JJ); nn(production-46/NN, PROTEIN2-45/NN); prep_for(responsible-43/JJ, production-46/NN); det(phase-50/NN, the-48/DT); amod(phase-50/NN, exponential-49/JJ); prep_during(responsible-43/JJ, phase-50/NN); prep_of(phase-50/NN, growth-52/NN); det(absence-55/NN, the-54/DT); prep_in(growth-52/NN, absence-55/NN); prep_of(absence-55/NN, KinA-57/NN); conj_and(KinA-57/NN, KinB-59/NN); prep_of(absence-55/NN, KinB-59/NN)";
        String p1 = "conj_or(PROTEIN123-1/NNS, PROTEIN234-2/NNS)";

        DirectedGraph<Vertex,Edge> graph = createGraph(g1);
        DirectedGraph<Vertex,Edge> pattern = createGraph(p1);

        ESM esm  = new ESM(pattern, graph);
        assertEquals(false, esm.isSubgraphIsomorphism());
    }


    /**
     * Checking subgraph isomorphism capabilities with a more complex pattern
     * 'depends' has been replaced with 'depend' in pattern and token Id's have been changed
     */
    @Test
    public void isSubgraphIsmorphismTest2(){
        String g1 = "det(mutant-3/NN, this-2/DT); prep_in(reduced-40/VBN, mutant-3/NN); conj_and(mutant-3/NN, expression-5/NN); prep_in(reduced-40/VBN, expression-5/NN); det(gene-9/NN, the-7/DT); nn(gene-9/NN, PROTEIN2-8/NN); prep_of(expression-5/NN, gene-9/NN); poss(transcription-12/NN, whose-11/WP$); nsubj(depends-13/VBZ, transcription-12/NN); rcmod(gene-9/NN, depends-13/VBZ); det(PROTEIN0-16/NN, both-15/DT); prep_on(depends-13/VBZ, PROTEIN0-16/NN); dep(PROTEIN0-16/NN, PROTEIN0-18/DT); det(protein-24/NN, the-21/DT); amod(protein-24/NN, phosphorylated-22/JJ); nn(protein-24/NN, PROTEIN1-23/NN); conj_and(mutant-3/NN, protein-24/NN); prep_in(reduced-40/VBN, protein-24/NN); nsubjpass(reduced-40/VBN, Spo0A~P-26/NN); det(factor-31/NN, a-28/DT); amod(factor-31/NN, major-29/JJ); nn(factor-31/NN, transcription-30/NN); appos(Spo0A~P-26/NN, factor-31/NN); amod(stages-34/NNS, early-33/JJ); prep_during(factor-31/NN, stages-34/NNS); prep_of(stages-34/NNS, sporulation-36/NN); auxpass(reduced-40/VBN, was-38/VBD); advmod(reduced-40/VBN, greatly-39/RB); number(degrees-43/NNS, 43-42/CD); num(C-44/NN, degrees-43/NNS); prep_at(reduced-40/VBN, C-44/NN)";
        String p1 = "prep_on(depend-1/VBZ, PROTEIN0-2/NN); rcmod(gene-3/NN, depend-1/VBZ); prep_of(expression-4/NN, gene-3/NN); nn(protein-7/NN, PROTEIN1-8/NN);  conj_and(mutant-3/NN, expression-4/NN); conj_and(mutant-3/NN, protein-7/NN)";

        DirectedGraph<Vertex,Edge> graph = createGraph(g1);
        DirectedGraph<Vertex,Edge> pattern = createGraph(p1);

        ESM esm  = new ESM(pattern, graph);
        assertEquals(false, esm.isGraphIsomorphism());
        assertEquals(true, esm.isSubgraphIsomorphism());

        assertEquals(1, esm.getSubgraphMatchingMatches().size());
    }

    /**
     * Checking subgraph isomorphism capabilities with a more complex pattern
     * Replaced gene with gnome
     */
    @Test
    public void isNotSubgraphIsmorphismTest2(){
        String g1 = "det(mutant-3/NN, this-2/DT); prep_in(reduced-40/VBN, mutant-3/NN); conj_and(mutant-3/NN, expression-5/NN); prep_in(reduced-40/VBN, expression-5/NN); det(gene-9/NN, the-7/DT); nn(gene-9/NN, PROTEIN2-8/NN); prep_of(expression-5/NN, gene-9/NN); poss(transcription-12/NN, whose-11/WP$); nsubj(depends-13/VBZ, transcription-12/NN); rcmod(gene-9/NN, depends-13/VBZ); det(PROTEIN0-16/NN, both-15/DT); prep_on(depends-13/VBZ, PROTEIN0-16/NN); dep(PROTEIN0-16/NN, PROTEIN0-18/DT); det(protein-24/NN, the-21/DT); amod(protein-24/NN, phosphorylated-22/JJ); nn(protein-24/NN, PROTEIN1-23/NN); conj_and(mutant-3/NN, protein-24/NN); prep_in(reduced-40/VBN, protein-24/NN); nsubjpass(reduced-40/VBN, Spo0A~P-26/NN); det(factor-31/NN, a-28/DT); amod(factor-31/NN, major-29/JJ); nn(factor-31/NN, transcription-30/NN); appos(Spo0A~P-26/NN, factor-31/NN); amod(stages-34/NNS, early-33/JJ); prep_during(factor-31/NN, stages-34/NNS); prep_of(stages-34/NNS, sporulation-36/NN); auxpass(reduced-40/VBN, was-38/VBD); advmod(reduced-40/VBN, greatly-39/RB); number(degrees-43/NNS, 43-42/CD); num(C-44/NN, degrees-43/NNS); prep_at(reduced-40/VBN, C-44/NN)";
        String p1 = "prep_on(depend-1/VBZ, PROTEIN0-2/NN); rcmod(gnome-3/NN, depend-1/VBZ); prep_of(expression-4/NN, gnome-3/NN); nn(protein-7/NN, PROTEIN1-8/NN);  conj_and(mutant-3/NN, expression-4/NN); conj_and(mutant-3/NN, protein-7/NN)";

        DirectedGraph<Vertex,Edge> graph = createGraph(g1);
        DirectedGraph<Vertex,Edge> pattern = createGraph(p1);

        ESM esm  = new ESM(pattern, graph);
        assertEquals(false, esm.isSubgraphIsomorphism());
    }

    /**
     * Test subgraph isomorphism
     */
    @Test
    public void isSubgraphIsomorphism() {
        assertEquals(true, test.isSubgraphIsomorphism());
    }

    /**
     * Test graph isomorphism
     */
    @Test
    public void isGraphIsomorphism() {
        assertEquals(false, test.isGraphIsomorphism());
    }

    /**
     * Test the matching results of subgraph isomorphism
     */
    @Test
    public void getSubgraphMatchingMatches() {
        List<Map<Vertex, Vertex>> matches = test.getSubgraphMatchingMatches();
        if(matches != null) {
            int count = 0;
            for(Map<Vertex, Vertex> m : matches) {
                count++;
                System.out.println("The No." + count + " matches are:");
                for(Entry<Vertex, Vertex> entry : m.entrySet()) {
                    System.out.println(entry.getKey().getToken() + " -> " + entry.getValue().getToken());
                }
            }
        }
    }

}
