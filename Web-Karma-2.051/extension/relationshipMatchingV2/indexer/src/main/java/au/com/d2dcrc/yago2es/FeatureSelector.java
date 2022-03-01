package au.com.d2dcrc.yago2es;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Zaiwen Feng on 28/05/2017, Modified on 17/07/2017 and 4/8/2017
 */
public class FeatureSelector {

    /**Filter out subgraphs that don't contain edge with the common relationship 'X' *
     * @param initialFrequentSubGraphs the original frequent subgraphs before filter
     * @param edgeInfoList the label of start, end point and edge that must be contained
     * @return updated frequent sub-graphs
     * */
    public List<Graph> removeUselessFrequentGraph(List<Graph> initialFrequentSubGraphs, List<Integer> edgeInfoList){

        List<Graph> usefulFrequentGraphs = new ArrayList<Graph>();

        Integer labelOfStart = edgeInfoList.get(0);
        Integer labelOfEnd = edgeInfoList.get(1);
        Integer commonLabel = edgeInfoList.get(3);//added on 4/8/17

        /**Iterate each initial subgarph**/
        Iterator<Graph> it = initialFrequentSubGraphs.iterator();
        while (it.hasNext())
        {

            Graph initialFrequentSubgraph = it.next();

            /**Get all the edges of each initial frequent sub-graph**/
            List<Edge> allEdges = initialFrequentSubgraph.getAllEdges();

            for(Edge edge : allEdges){


                /**Get the label of start and end point as well as edge for each edge**/
                Vertex start = edge.getStartPoint();
                Vertex end = edge.getEndPoint();
                String labelOfStartPoint = start.getLabel();
                String labelOfEndPoint = end.getLabel();
                Integer labelOfEdge = edge.getWeight();

                if(labelOfStartPoint.equals(labelOfStart.toString()) && (labelOfEndPoint.equals(labelOfEnd.toString())) &&
                        (labelOfEdge.equals(commonLabel))){

                    usefulFrequentGraphs.add(initialFrequentSubgraph);
                    break;
                }else {
                    continue;
                }

            }
        }

        /**Remove the frequent sub-graph only containing the edge with the relationship type (e.g. 'isMarriedTo'), added on 17/07/2017**/
        Iterator<Graph> its = usefulFrequentGraphs.iterator();
        while (its.hasNext()) {

            Graph usefulFrequentGraph = its.next();
            List<Edge> allEdges = usefulFrequentGraph.getAllEdges();

            if (allEdges.size() == 1) {

                Edge uniEdge = allEdges.get(0);
                /**Get the start point**/
                Vertex start = uniEdge.getStartPoint();
                /**Get the end point**/
                Vertex end = uniEdge.getEndPoint();

                String startPointLabel = start.getLabel();
                String endPointLabel = end.getLabel();

                if ((startPointLabel.equals(labelOfStart.toString())) && (endPointLabel.equals(labelOfEnd.toString()))) {

                    /**Remove this graph**/
                    its.remove();
                }
            }

        }


        return usefulFrequentGraphs;
    }


    /**Filter out subgraphs that don't contain edge with the specified relationship , for example, livesIn, or isMarriedTo *
     * @param initialFrequentSubGraphs the original frequent subgraphs before filter
     * @param edgeInfoList the label of start, end point and edge that must be contained
     * @return updated frequent sub-graphs
     * */
    public List<Graph> removeUselessFrequentGraph2(List<Graph> initialFrequentSubGraphs, List<Integer> edgeInfoList){

        List<Graph> usefulFrequentGraphs = new ArrayList<Graph>();

        Integer labelOfStart = edgeInfoList.get(0);
        Integer labelOfEnd = edgeInfoList.get(1);
        Integer labelOfE = edgeInfoList.get(2);

        /**Iterate each initial subgarph**/
        Iterator<Graph> it = initialFrequentSubGraphs.iterator();
        while (it.hasNext())
        {

            Graph initialFrequentSubgraph = it.next();

            /**Get all the edges of each initial frequent sub-graph**/
            List<Edge> allEdges = initialFrequentSubgraph.getAllEdges();

            for(Edge edge : allEdges){


                /**Get the label of start and end point as well as edge for each edge**/
                Vertex start = edge.getStartPoint();
                Vertex end = edge.getEndPoint();
                String labelOfStartPoint = start.getLabel();
                String labelOfEndPoint = end.getLabel();
                Integer labelOfEdge = edge.getWeight();

                if(labelOfStartPoint.equals(labelOfStart.toString()) && (labelOfEndPoint.equals(labelOfEnd.toString())) &&
                        (labelOfEdge.equals(labelOfE))){

                    usefulFrequentGraphs.add(initialFrequentSubgraph);
                    break;
                }else {
                    continue;
                }

            }
        }

        /**Remove the frequent sub-graph only containing the edge with the relationship type (e.g. 'isMarriedTo'), added on 17/07/2017**/
        Iterator<Graph> its = usefulFrequentGraphs.iterator();
        while (its.hasNext()) {

            Graph usefulFrequentGraph = its.next();
            List<Edge> allEdges = usefulFrequentGraph.getAllEdges();

            if (allEdges.size() == 1) {

                Edge uniEdge = allEdges.get(0);
                /**Get the start point**/
                Vertex start = uniEdge.getStartPoint();
                /**Get the end point**/
                Vertex end = uniEdge.getEndPoint();

                String startPointLabel = start.getLabel();
                String endPointLabel = end.getLabel();

                if ((startPointLabel.equals(labelOfStart.toString())) && (endPointLabel.equals(labelOfEnd.toString()))) {

                    /**Remove this graph**/
                    its.remove();
                }
            }

        }


        return usefulFrequentGraphs;
    }

    /**Filter out subgraphs that don't contain the starting point or ending point of the edge with common label 'X' *
     * @param initialFrequentSubGraphs the original frequent subgraphs before filter
     * @param edgeInfoList the label of start, end point and edge that must be contained
     * @return updated frequent sub-graphs
     * */
    public List<Graph> removeUselessFrequentGraph3(List<Graph> initialFrequentSubGraphs, List<Integer> edgeInfoList){

        List<Graph> usefulFrequentGraphs = new ArrayList<Graph>();

        Integer labelOfStart = edgeInfoList.get(0);
        Integer labelOfEnd = edgeInfoList.get(1);
        Integer labelOfE = edgeInfoList.get(2);
        Integer commonLabel = edgeInfoList.get(3);

        /**Iterate each initial subgarph**/
        Iterator<Graph> it = initialFrequentSubGraphs.iterator();
        while (it.hasNext())
        {

            Graph initialFrequentSubgraph = it.next();

            /**Get all the edges of each initial frequent sub-graph**/
            List<Edge> allEdges = initialFrequentSubgraph.getAllEdges();

            for(Edge edge : allEdges){


                /**Get the label of start and end point as well as edge for each edge**/
                Vertex start = edge.getStartPoint();
                Vertex end = edge.getEndPoint();
                String labelOfStartPoint = start.getLabel();
                String labelOfEndPoint = end.getLabel();
                Integer labelOfEdge = edge.getWeight();

                if((labelOfStartPoint.equals(labelOfStart.toString())) || (labelOfEndPoint.equals(labelOfEnd.toString())) ||
                        (labelOfStartPoint.equals(labelOfEnd.toString())) || (labelOfEndPoint.equals(labelOfStart.toString()))){

                    usefulFrequentGraphs.add(initialFrequentSubgraph);
                    break;
                }else {
                    continue;
                }

            }
        }

        /**Remove the frequent sub-graph only containing the edge with the relationship type (e.g. 'isMarriedTo'), added on 17/07/2017**/
        Iterator<Graph> its = usefulFrequentGraphs.iterator();
        while (its.hasNext()) {

            Graph usefulFrequentGraph = its.next();
            List<Edge> allEdges = usefulFrequentGraph.getAllEdges();

            if (allEdges.size() == 1) {

                Edge uniEdge = allEdges.get(0);
                /**Get the start point**/
                Vertex start = uniEdge.getStartPoint();
                /**Get the end point**/
                Vertex end = uniEdge.getEndPoint();
                /**Get the weight of the edge**/
                int weight = uniEdge.getWeight();

                String startPointLabel = start.getLabel();
                String endPointLabel = end.getLabel();

                if ((startPointLabel.equals(labelOfStart.toString())) && (endPointLabel.equals(labelOfEnd.toString())) &&
                        (weight == commonLabel)) {

                    /**Remove this graph**/
                    its.remove();
                }
            }

        }


        return usefulFrequentGraphs;
    }
}
