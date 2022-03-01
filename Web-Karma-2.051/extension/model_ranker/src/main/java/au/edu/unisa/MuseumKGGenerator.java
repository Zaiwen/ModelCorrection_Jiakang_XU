package au.edu.unisa;

import org.jgrapht.graph.DirectedWeightedMultigraph;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.graph.AsUndirectedGraph;
import org.jgrapht.graph.DirectedWeightedMultigraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.modeling.ModelingParams;
import edu.isi.karma.rep.alignment.DefaultLink;
import edu.isi.karma.rep.alignment.LabeledLink;
import edu.isi.karma.rep.alignment.Node;

import java.util.List;

/**A knowledge graph generator based on Museum RDF triples*
 * @from 5 May 2020
 * */
public class MuseumKGGenerator {


    /**parse a RDF file and output a linked data graph*
     * @from 5 May 2020
     * @param url address of the RDF file
     * @param ds_name name of the RDF file
     * */
    public static DirectedWeightedMultigraph<Node, LabeledLink> generator (String url, String ds_name) {

        DirectedWeightedMultigraph<Node, LabeledLink> lod = new DirectedWeightedMultigraph<>(LabeledLink.class);
        List<Triple> tripleList = Triple.parseTriples(url.concat(ds_name));

        System.out.println("We have get " + tripleList.size() + " triples!");







        return lod;
    }


    public static void main (String args[]) {

        generator(Settings.MUSEUM_CRM_NORMALIZED_RDF_Address, Settings.DS_NORMALIZED_RDF_DS25);



    }


}
