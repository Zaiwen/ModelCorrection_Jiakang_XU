package VF2.runner;

import VF2.algorithm.VF2;
import VF2.graph.LGGraph;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;

public class MCSMapping0 {
    public static void main(String[] args) throws FileNotFoundException {
        File graphFile1 = new File("D:\\mcs.lg");
        File graphFile2 = new File("D:\\mcs_e0.lg");
        File graphFile3 = new File("D:\\mcs_e1.lg");
        File kgFile = new File("D:\\DataMatching\\museum-crm\\museum20200906.lg");
        LGGraph mcs = LGGraph.loadGraphSetFromFile(graphFile1);
        LGGraph mcs_e0 = LGGraph.loadGraphSetFromFile(graphFile2);
        LGGraph mcs_e1 = LGGraph.loadGraphSetFromFile(graphFile3);
        LGGraph knowledgeGraph = LGGraph.loadGraphSetFromFile(kgFile);

        mapping(knowledgeGraph,mcs);
        mapping(knowledgeGraph,mcs_e0);
        mapping(knowledgeGraph,mcs_e1);

    }

    public static void mapping(LGGraph target, LGGraph query){
        Long start = System.currentTimeMillis();
        VF2 vf2 = new VF2();
        ArrayList result = vf2.matchGraphSetWithQuery(target,query);
        System.out.println("map_frq: "+result.size());
        Long end = System.currentTimeMillis();
        System.out.println("runtime: "+(end - start)/1000.0);
    }

}
