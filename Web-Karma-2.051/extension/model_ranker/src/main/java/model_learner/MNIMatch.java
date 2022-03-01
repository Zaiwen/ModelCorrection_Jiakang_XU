package model_learner;

import au.com.d2dcrc.GramiMatcher;
import dataStructures.Graph;
import dataStructures.HPListGraph;
import dataStructures.Query;
import dataStructures.myNode;

import java.io.File;
import java.io.FileWriter;
import java.util.HashMap;


public class MNIMatch {
    public static void main(String[] args) throws Exception {
        Graph kg = new Graph(1, 0);
//        kg.loadFromFile("D:\\DataMatching\\museum-crm\\museum20200906.lg");
        kg.loadFromFile("D:\\DataMatching\\museum_kg_20210604.lg");
//        System.out.println(kg.getFreqNodesByLabel());
        HPListGraph<Integer, Double> lg = kg.getListGraph();
        for(int i = 0; i<kg.getNumberOfNodes(); i++){
            for(int j = 0; j<kg.getNumberOfNodes(); j++){
                myNode n1 = kg.getNode(i);
                myNode n2 = kg.getNode(j);
                if (lg.getEdge(i,j) > -1){
                    n1.addreachableNode(n2, lg.getEdgeLabel(i,j));
                }
            }
        }


//        Graph qryGraph = new Graph(2,0);
//        qryGraph.loadFromFile_Ehab("E:\\tmp.lg");
//        GramiMatcher gm = new GramiMatcher();
//        gm.setGraph(kg);
//        gm.setQry(new Query(qryGraph.getListGraph()));
//        System.out.println(gm.getFrequency(new HashMap<>(), 1));


//        FileWriter fw = new FileWriter("C:\\Users\\Dell\\Desktop\\GramiMatch_result_test.txt");
//        fw.write("qry\tfrq\truntime\n");
//
//        String path = "E:\\model_correction\\mcs";
//        File graphsPath = new File(path);
//        File[] graphs = graphsPath.listFiles();
//
//        GramiMatcher gm = new GramiMatcher();
//        int i = 2;
//        for (File graph : graphs) {
//            Graph qryGraph = new Graph(i++, 0);
//            qryGraph.loadFromFile(graph.getAbsolutePath());
//            Query qry = new Query(qryGraph.getListGraph());
//            fw.write(graph.getName().substring(0,graph.getName().lastIndexOf('.'))+"\t");
//            gm.setQry(qry);
//            gm.setGraph(kg);
//            long start = System.currentTimeMillis();
//            int frq = gm.getFrequency(new HashMap<>(),1);
//            long end = System.currentTimeMillis();
//            fw.write(frq+"\t");
//            fw.write((end-start)/1000.0+"\n");
//        }
//
//        fw.close();


//        int[][] train = {
//                {0,5,11},{1,4,5},{3,8,23}
//        };

        int[][] train = {
                {1, 6, 11}, {0, 21, 23}, {17, 18, 23}
        };

        for (int[] trainDataIndex : train) {
            for (int i = 0; i < 29; i++) {
                if(i != trainDataIndex[0] && i!= trainDataIndex[1] && i != trainDataIndex[2] && i != 26){
                    String mcsPath = String.format("E:\\model_correction_20210705\\train_%d_%d_%d\\newSource_",
                            trainDataIndex[0]+1,trainDataIndex[1]+1,trainDataIndex[2]+1);
                    mcsPath += (i+1);
                    mcsPath += "\\mcs";
                    if(!new File(mcsPath).exists()){
                        continue;
                    }
                    System.out.println(mcsPath);
                    mniMatch(kg,mcsPath);
                }
            }
        }


    }


    public static void mniMatch(Graph kg,String mcsPath) throws Exception {
        FileWriter fw = new FileWriter(new File(mcsPath).getParentFile().getAbsolutePath()+"\\result.txt");
        fw.write("qry\tfrq\truntime\n");


        File graphsPath = new File(mcsPath);
        File[] graphs = graphsPath.listFiles();

        GramiMatcher gm = new GramiMatcher();
        int i = 2;
        for (File graph : graphs) {
            Graph qryGraph = new Graph(i++, 0);
            qryGraph.loadFromFile(graph.getAbsolutePath());
            Query qry = new Query(qryGraph.getListGraph());
            fw.write(graph.getName().substring(0,graph.getName().lastIndexOf('.'))+"\t");
            gm.setQry(qry);
            gm.setGraph(kg);
            long start = System.currentTimeMillis();
            int frq = gm.getFrequency(new HashMap<>(),1);
            long end = System.currentTimeMillis();
            fw.write(frq+"\t");
            fw.write((end-start)/1000.0+"\n");
        }
        fw.close();
    }

}
