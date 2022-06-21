package VF2.runner;

import VF2.algorithm.VF2;
import VF2.graph.LGGraph;

import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Scanner;

public class Show {
    public static void main(String[] args) throws FileNotFoundException {

        Path graphPath = Paths.get("C:\\Users\\lr slxdr\\Desktop\\Graph", "gmark_kg20200605_N4000.lg");
        Path queryPath = Paths.get("C:\\Users\\lr slxdr\\Desktop\\Graph", "pattern_C4.lg");

        LGGraph targetGraph = loadGraphSetFromFile(graphPath, "targetGraph ");
        LGGraph queryGraph = loadGraphSetFromFile(queryPath, "queryGraph ");
        VF2 vf2 = new VF2();
        long start = System.currentTimeMillis();
        ArrayList<int[]> stateSet = vf2.matchGraphSetWithQuery(targetGraph, queryGraph);
        if (stateSet.isEmpty()) {
            System.out.println("Cannot find a map for: " + queryGraph.name);
        } else {
            for(int[] arr2 : stateSet){
                for (int q = 0 ; q < arr2.length ; q++) {
                    System.out.print("(" + arr2[q] + "-" + q + ") ");
                }
                System.out.println();
            }
            System.out.println("Found " + stateSet.size() + " maps for: " + queryGraph.name); //输出匹配到的子图个数
        }
        long end = System.currentTimeMillis();
        System.out.println("time: "+(end-start)/1000.0);
    }

    //加载文件
    private static LGGraph loadGraphSetFromFile(Path inpath, String namePrefix) throws FileNotFoundException{
        Scanner scanner = new Scanner(inpath.toFile());
        LGGraph graph = null;
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine().trim();
            if (line.startsWith("t")) {
                String graphId = line.split(" ")[2];
                graph = new LGGraph(namePrefix + graphId);
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
}

