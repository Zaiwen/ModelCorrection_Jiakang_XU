package acrc.itms.unisa;

import ESM.ESMMatcher;
import edu.isi.karma.rep.alignment.LabeledLink;
import edu.isi.karma.rep.alignment.Node;
import org.gephi.graph.api.DirectedGraph;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.io.exporter.api.ExportController;
import org.gephi.io.exporter.spi.GraphExporter;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.jgrapht.graph.DirectedWeightedMultigraph;
import org.openide.util.Lookup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**acrc.itms.unisa.Utilities for gephi 0.9.2**/
public class GephiUtilities {

    private static Logger logger = LoggerFactory.getLogger(GephiUtilities.class);

    private GephiUtilities() {
    }

    /**set up a gephi adaptor from Directed Weighed Multiple Graph to gephi graph*
     * @from 13 Sep 2018
     * @param knowledgeGraph to be visualized
     * @param vizFileUrl the directory to the vizFile
     * @param vizFileName viz file name read by gephi, e.g. .gexf, .gml. .dot...
     * @param colorMap color for each entity
     * @return a map saving correspondence between relationship type and kind number. Key: relationship type. Value: kind (a number) in gephi
     * */
    public static void convertToGephi(DirectedWeightedMultigraph<Node, LabeledLink> knowledgeGraph, String vizFileUrl, String vizFileName, Map<String,Color> colorMap){
        //init a project = and therefore workspace
        ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
        pc.newProject();
        Workspace workspace = pc.getCurrentWorkspace();

        //Get a graph model - it exists because we have a workspace
        GraphModel graphModel = Lookup.getDefault().lookup(GraphController.class).getGraphModel();

        Set<Node> nodeSet = knowledgeGraph.vertexSet();//get all of the nodes
        Set<LabeledLink> linkSet = knowledgeGraph.edgeSet();//get all of the links

        Map<String, org.gephi.graph.api.Node> gephiNodeMap = new HashMap<>();//create a map for gephi nodes. Key: id, Value: gephi node
        //Map<String,Integer> relationshipMap = new HashMap<>();//create a map for the correspondence between karma graph and  gephi links


        DirectedGraph directedGraph = graphModel.getDirectedGraph();

        /**convert to gephi nodes**/
        for(edu.isi.karma.rep.alignment.Node node : nodeSet){
            String id = node.getId();
            String nodeLabel = node.getUri();
            org.gephi.graph.api.Node gephiNode = graphModel.factory().newNode(id);//set id to gephi node
            gephiNode.setLabel(nodeLabel);//set label to gephi node
            Iterator it = colorMap.entrySet().iterator();
            while (it.hasNext()){
                Map.Entry pair = (Map.Entry)it.next();
                String label = (String)pair.getKey();//get key
                Color color = (Color) pair.getValue();//get value
                if (nodeLabel.equals(label)) {
                    gephiNode.setColor(color);
                }
            }
            gephiNodeMap.put(id,gephiNode);
            directedGraph.addNode(gephiNode);//add node to the gephi graph
        }

        /**convert to gephi edges**/
        //int kind = 1;//the first kind of edge is numbered as 1...
        for(LabeledLink labeledlink : linkSet){
            //int type;
            edu.isi.karma.rep.alignment.Node source = labeledlink.getSource();//get source
            edu.isi.karma.rep.alignment.Node target = labeledlink.getTarget();//get target
            String relationshipType = labeledlink.getUri();//get relationship type
            //Integer value = relationshipMap.get(relationshipType);
//            if(value != null){
//                type = value;
//            }else {
//                relationshipMap.put(relationshipType,kind);//put this relationship type and its number into the hash map
//                type = kind;
//                kind++;
//            }
            String sourceId = source.getId();//get source id
            String targetId = target.getId();//get target id
            org.gephi.graph.api.Node gephiSource = gephiNodeMap.get(sourceId);//get gephi source
            org.gephi.graph.api.Node gephiTarget = gephiNodeMap.get(targetId);//get gephi target
            Edge edge = graphModel.factory().newEdge(gephiSource,gephiTarget,true);//create a new gephi edge
            edge.setLabel(relationshipType);//set label on an edge
            directedGraph.addEdge(edge);//add edge to the gephi graph
        }

        System.out.println("gephi graph has been built....");

        ExportController ec = Lookup.getDefault().lookup(ExportController.class);
        GraphExporter exporter = (GraphExporter) ec.getExporter("gexf");
        exporter.setExportVisible(true);
        exporter.setWorkspace(workspace);

        try{
            ec.exportFile(new File(vizFileUrl.concat(vizFileName)),exporter);
        }catch (IOException ex){
            ex.printStackTrace();
        }
    }
    public static void visualizeMuseumKG (String graphDataSourceUrl, String graphDataSourceName,  String vizFileURL, String vizFileName) {

        /**read the graph data and convert the data into java object**/
        DirectedWeightedMultigraph<Node, LabeledLink> graph = ESMMatcher.loadDirectedWeightedMultipGraph(graphDataSourceUrl,graphDataSourceName);
        Map<String, Color> colorMap = new HashMap<>();/**Begin to define the color of the entities**/
        colorMap.put("0",Color.black);//0-E12_Production
        colorMap.put("1",Color.blue);//1-E21_Person
        colorMap.put("2",Color.green);//2-E22_Man_Made_Object
        colorMap.put("3",Color.yellow);//3-E35_Title
        colorMap.put("4",Color.red);//4-Time-Span
        colorMap.put("5",Color.orange);//5-E54_Dimension
        colorMap.put("6",Color.CYAN);//6-E55_Type
        colorMap.put("7",Color.MAGENTA);//7-E67_Birth
        colorMap.put("8",Color.pink);//8-E69_Death
        colorMap.put("9",Color.gray);//9-E82_Actor_Appellation
        colorMap.put("10",Color.BLACK);//10-E30_Right
        colorMap.put("11",Color.pink);//11-E40_Legal_Body
        colorMap.put("12",Color.gray);//12-E74_Group
        colorMap.put("13",Color.CYAN);//13-E33_Linguistic_Object
        colorMap.put("14",Color.MAGENTA);//14-E53_Place
        colorMap.put("15",Color.black);//15-E8_Acquisition
        colorMap.put("16",Color.GREEN);//16-E48_Place_Name
        colorMap.put("17",Color.cyan);
        colorMap.put("18",Color.pink);
        colorMap.put("19",Color.red);
        colorMap.put("20",Color.blue);
        colorMap.put("21",Color.yellow);
        colorMap.put("22",Color.green);
        colorMap.put("23",Color.magenta);

        /**convert java object into gephi viz file**/
        convertToGephi(graph,vizFileURL,vizFileName,colorMap);
    }


    public static void main(String args[]){
        visualizeMuseumKG("D:\\DataMatching\\","museum_kg0.lg","D:\\DataMatching\\","museum_kg.gexf");
    }


}
