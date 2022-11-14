package Dijkstra;

import au.com.d2dcrc.GramiMatcher;
import dataStructures.Graph;
import dataStructures.Query;

import java.io.FileWriter;
import java.util.HashMap;

public class Tmp {

    public static void main(String[] args) throws Exception {


        Graph kg = new Graph(1, 1);

//        kg.loadFromFile_Ehab("C:\\D_Drive\\ASM\\DataSets\\museum-crm\\museum_kg_20220513.lg");
        kg.loadFromFile_Ehab("C:\\D_Drive\\ASM\\DataSets\\weapon-lod\\kg_20220720\\lg_20220919\\weapon_kg_elpasoguntrader.lg");
//        kg.loadFromFile_Ehab("C:\\D_Drive\\ASM\\experiment\\exp_20220530\\s15_result.lg");

        kg.setShortestPaths_1hop();

        Graph qryGraph = new Graph(2,1);
        qryGraph.loadFromFile("C:\\D_Drive\\ASM\\experiment\\exp_20220920\\train_2_6___1\\newSource_4\\s4_result.lg");
        GramiMatcher gm = new GramiMatcher();
        gm.setGraph(kg);
        gm.setQry(new Query(qryGraph.getListGraph()));
        System.out.println(gm.getFrequency(new HashMap<>(), 1));
        System.exit(1);

        FileWriter fw = new FileWriter("C:\\D_Drive\\ASM\\experiment\\correct_model_freq0.txt");
        fw.write("model\tfreq\n");
//
//

//        File graphsPath = new File("C:\\D_Drive\\ASM\\experiment\\correct_models\\lg");
//        File[] graphs = graphsPath.listFiles();
//        for (File graph : graphs) {
//            Graph qryGraph = new Graph(2,0);
//            qryGraph.loadFromFile_Ehab(graph.getAbsolutePath());
//            GramiMatcher gm = new GramiMatcher();
//            gm.setGraph(kg);
//            gm.setQry(new Query(qryGraph.getListGraph()));
//            fw.write(graph.getName()+'\t'+gm.getFrequency(new HashMap<>(), 1)+'\n');
//            fw.flush();
//        }
//        fw.close();


    }

}
