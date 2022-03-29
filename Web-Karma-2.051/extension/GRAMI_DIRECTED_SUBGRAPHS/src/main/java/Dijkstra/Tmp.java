package Dijkstra;

import au.com.d2dcrc.GramiMatcher;
import dataStructures.Graph;
import dataStructures.Query;

import java.io.File;
import java.io.FileWriter;
import java.util.HashMap;

public class Tmp {

    public static void main(String[] args) throws Exception {


        Graph kg = new Graph(1, 1);
        kg.loadFromFile_Ehab("D:\\ASM\\DataSets\\museum-crm\\museum_kg_20210604.lg");
//        kg.loadFromFile_Ehab("D:\\DataMatching\\s10.lg");


        kg.setShortestPaths_1hop();

//        FileWriter fw = new FileWriter("D:\\ASM\\experiment\\correct_model_freq1.txt");
//        fw.write("model\tfreq\n");


        Graph qryGraph = new Graph(2,1);
        qryGraph.loadFromFile("D:\\ASM\\experiment\\s10_model.lg");
//        File graphsPath = new File("D:\\ASM\\experiment\\correct_models");
//        File[] graphs = graphsPath.listFiles();
//        for (File graph : graphs) {
//            Graph qryGraph = new Graph(2,0);
//            qryGraph.loadFromFile_Ehab(graph.getAbsolutePath());
//            GramiMatcher gm = new GramiMatcher();
//            gm.setGraph(kg);
//            gm.setQry(new Query(qryGraph.getListGraph()));
//            fw.write(graph.getName()+'\t'+gm.getFrequency(new HashMap<>(), 1)+'\n');
//        }
//        fw.close();
        GramiMatcher gm = new GramiMatcher();
        gm.setGraph(kg);
        gm.setQry(new Query(qryGraph.getListGraph()));
        System.out.println(gm.getFrequency(new HashMap<>(), 1));

    }

}
