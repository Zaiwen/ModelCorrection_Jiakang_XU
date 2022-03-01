package VF2.runner;

import VF2.algorithm.VF2;
import VF2.graph.VF2Graph;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;

public class MCSMapping0 {
    public static void main(String[] args) throws FileNotFoundException {
        File graphFile1 = new File("D:\\mcs.lg");
        File graphFile2 = new File("D:\\mcs_e0.lg");
        File graphFile3 = new File("D:\\mcs_e1.lg");
        File kgFile = new File("D:\\DataMatching\\museum-crm\\museum20200906.lg");
        VF2Graph mcs = VF2Graph.loadGraphSetFromFile(graphFile1);
        VF2Graph mcs_e0 = VF2Graph.loadGraphSetFromFile(graphFile2);
        VF2Graph mcs_e1 = VF2Graph.loadGraphSetFromFile(graphFile3);
        VF2Graph knowledgeGraph = VF2Graph.loadGraphSetFromFile(kgFile);

        mapping(knowledgeGraph,mcs);
        mapping(knowledgeGraph,mcs_e0);
        mapping(knowledgeGraph,mcs_e1);

    }

    public static void mapping(VF2Graph target,VF2Graph query){
        Long start = System.currentTimeMillis();
        VF2 vf2 = new VF2();
        ArrayList result = vf2.matchGraphSetWithQuery(target,query);
        System.out.println("map_frq: "+result.size());
        Long end = System.currentTimeMillis();
        System.out.println("runtime: "+(end - start)/1000.0);
    }

}
