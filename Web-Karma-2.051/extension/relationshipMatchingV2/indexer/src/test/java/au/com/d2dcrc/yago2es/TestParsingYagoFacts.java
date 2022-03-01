package au.com.d2dcrc.yago2es;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Zaiwen FENG on 19/07/2017.
 */
public class TestParsingYagoFacts {

    @Test
    public void testCreateYagoBoundaryGraph() throws  Exception{

//        final YagoTtlParser parser = new YagoTtlParser();
//
        //YagoFact yagoFact = new YagoFact("Ernest_Hemingway", "influences", "Paolo_Bacigalupi");
        YagoFact yagoFact = new YagoFact("Ernest_Hemingway", "influences", "Paolo_Bacigalupi");

        Graph boundaryGraph = LinkedEntityGraph.createYagoBoundaryGraph(yagoFact,"/Users/fengz/Documents/Data_Modeling/YAGO/RDF-Graph@27June17/yagoFacts_all.ttl",2, 2500);

        List<Vertex> allVertex = boundaryGraph.getAllVertices();

        List<Vertex> allAdjacentVertex = new ArrayList<>();
        List<Vertex> allPreAdjacentVertex = new ArrayList<>();

        for (Vertex vertex : allVertex) {

            if (vertex.getInstanceName().equals("Pulitzer_Prize")) {

                System.out.println("the record has been found! ");

                allAdjacentVertex = vertex.getAdjacentVertexes();

                allPreAdjacentVertex = vertex.getPreAdjacentVertex();

                for (Vertex adjacentVertex : allAdjacentVertex) {

                    System.out.println(adjacentVertex.getInstanceName());
                }

                for (Vertex preAdjacentVertex : allPreAdjacentVertex) {

                    System.out.println(preAdjacentVertex.getInstanceName());
                }
            }
        }

        System.out.println("done!");

    }

}
