package model_learner;

import au.com.d2dcrc.GramiMatcher;
import dataStructures.*;

import java.io.File;
import java.io.FileWriter;
import java.util.*;

public class MCSMatch {
    public static void main(String[] args) throws Exception {
        Graph kg = new Graph(1,0);
        kg.loadFromFile("D:\\DataMatching\\graphs\\museum_kg0.lg");
//        kg.loadFromFile("D:\\DataMatching\\museum-crm\\museum20200906.lg");
        HPListGraph lg = kg.getListGraph();
        kg.printFreqNodes();

        for (int i = 0; i < kg.getNumberOfNodes(); i++) {
            for (int j = 0; j < kg.getNumberOfNodes(); j++) {
                myNode node1 = kg.getNode(i);
                myNode node2 = kg.getNode(j);
                if (lg.getEdge(i,j)>-1){
                    node1.addreachableNode(node2, (Double) lg.getEdgeLabel(i,j));
                }
            }
        }

        File mcsFiles = new File("D:\\mcs");
        File[] mcsGraphFiles = mcsFiles.listFiles();
        FileWriter fw = new FileWriter("C:\\Users\\lr slxdr\\Desktop\\result0.txt");
        int i = 1;
        for (File graphFile : mcsGraphFiles) {
            Graph mcsGraph = new Graph(i++,0);
            mcsGraph.loadFromFile(graphFile.getAbsolutePath());
            Query qry = new Query(mcsGraph);
            GramiMatcher gm = new GramiMatcher();
            gm.setGraph(kg);
            gm.setQry(qry);
            HashMap<Integer, HashSet<Integer>> nonCandidates = new HashMap<>();
            int frq = gm.getFrequency(nonCandidates,1);
            fw.append(graphFile.getName()+"\t"+frq+"\n");
        }
        fw.close();

    }

}
