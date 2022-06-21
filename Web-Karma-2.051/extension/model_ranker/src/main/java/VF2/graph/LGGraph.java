package VF2.graph;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class LGGraph {
    public String name;
    public ArrayList<Node> nodes = new ArrayList<Node>();
    public ArrayList<Edge> edges = new ArrayList<Edge>();


    public LGGraph() {
    }

    public LGGraph(String name) {
        this.name = name;
    }

    public void addNode(int id, int label) {
        nodes.add(new Node(id, label));
    }

    public void addEdge(Node source, Node target, int label) {
        edges.add(new Edge(source, target, label));
    }

    public void addEdge(int sourceId, int targetId, int label) {
        this.addEdge(this.nodes.get(sourceId), this.nodes.get(targetId), label);
    }

    public static void printVF2Graph(LGGraph LGGraph){
        for (Node node: LGGraph.nodes){
            System.out.print("v ");
            System.out.print(node.id+" ");
            System.out.println(node.label);
        }

        for(Edge edge: LGGraph.edges){
            System.out.print("e ");
            System.out.println(edge.source.id+" "+edge.target.id+" "+ edge.label);
        }
        System.out.println();
    }

    public static LGGraph loadGraphSetFromFile(File file) throws FileNotFoundException {
        Scanner scanner = new Scanner(file);
        LGGraph graph = null;
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine().trim();
            if (line.startsWith("t")) {
                String graphId = line.split(" ")[2];
                graph = new LGGraph();
            } else if (line.startsWith("v")) {
                String[] lineSplit = line.split(" ");
                int nodeId = Integer.parseInt(lineSplit[1]);
                int nodeLabel = Integer.parseInt(lineSplit[2]);
                graph.addNode(nodeId, nodeLabel);
            } else if (line.startsWith("e")) {
                String[] lineSplit = line.split(" ");
                int sourceId = Integer.parseInt(lineSplit[1]);
                int targetId = Integer.parseInt(lineSplit[2]);
                int edgeLabel = Integer.parseInt(lineSplit[3]);
                graph.addEdge(sourceId, targetId, edgeLabel);
            } else if (line.equals("")) {
                continue;
            }
        }
        scanner.close();
        return graph;
    }

    public static void writeIntoFile(LGGraph LGGraph, String path) throws IOException {
        FileWriter fw = new FileWriter(path);
        fw.write("t # 1\n");
        for (Node node: LGGraph.nodes){
//            System.out.print("v ");
//            System.out.print(node.id+" ");
//            System.out.println(node.label);
            fw.write("v ");
            fw.write(node.id+" ");
            fw.write(node.label+"\n");
        }

        for(Edge edge: LGGraph.edges){
//            System.out.print("e ");
//            System.out.println(edge.source.id+" "+edge.target.id+" "+ edge.label);
            fw.write("e ");
            fw.write(edge.source.id+" ");
            fw.write(edge.target.id+" ");
            fw.write(edge.label+"\n");
        }

        fw.close();
    }



}
