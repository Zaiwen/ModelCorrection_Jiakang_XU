package Dijkstra;

import dataStructures.DFScodeSerializer;
import dataStructures.Graph;
import search.Searcher;

import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class Extension {

    public static final int[] TRAIN = {2, 5, 6};
    public static final int TEST = 8;
//    public static final String GRAPH_PATH =
//            String.format("D:\\ASM\\experiment\\model_correction_20220217\\train_%d_%d_%d\\newSource_%d\\modelgraphs\\",
//                    TRAIN[0], TRAIN[1], TRAIN[2] , TEST);
    public static final String GRAPH_PATH = "D:\\ASM\\experiment\\exp_20220329\\s08_seed.lg";
    public static final String KGPATH = "D:\\ASM\\DataSets\\museum-crm\\museum_kg_20210906.lg";
//    public static final String OUT_PATH =
//            String.format("D:\\ASM\\experiment\\exp_20220218\\(%d, %d, %d)-%d\\", TRAIN[0], TRAIN[1], TRAIN[2], TEST);
    public static final String OUT_PATH = "D:\\ASM\\experiment\\exp_20220329\\";
    public static final int[] newNodeArr = {23};
    public static final int FREQ = 10;


    public static void main(String[] args) throws Exception {


        Graph graph = new Graph(1,0);
//        graph.loadFromFile_Ehab(GRAPH_PATH+"model_0.lg");
        graph.loadFromFile_Ehab(GRAPH_PATH);


        ArrayList<Integer> newNodes =new ArrayList<>();

        for (int i : newNodeArr) {
            newNodes.add(i);
        }

        HashMap<Integer, Integer> constraintMap = new HashMap<>();
        constraintMap.put(0,1);
        constraintMap.put(1,1);
        constraintMap.put(2,1);
//        constraintMap.put(3,1);
//        constraintMap.put(4,3);
//        constraintMap.put(5,1);
//        constraintMap.put(7,1);
//        constraintMap.put(8,1);
//        constraintMap.put(17,1);


        long start = System.currentTimeMillis();

        Searcher<String, String> searcher = new Searcher<>(KGPATH, FREQ, 1, graph);
//        Searcher<String, String> searcher = new Searcher<>("E:\\extension_test\\museum_kg_20210604.lg",90, 1, graph);
        searcher.initialize();
        searcher.extend(newNodes, constraintMap);
//        searcher.search();

        String fName = OUT_PATH;

        if (!new File(OUT_PATH).exists()) {
            new File(OUT_PATH).mkdir();
        }


        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("_MM_dd_H_m_");
        fName += "result"+TEST;
//        fName += sdf.format(date);
        fName += ".txt";

        FileWriter fw = new FileWriter(fName);
        fw.write("// train: s"+TRAIN[0]+" s"+TRAIN[1]+" s"+TRAIN[2]+"\n");
        fw.write("// new source: s"+TEST+"\n");
        fw.write("// constraintMap:"+constraintMap.toString()+"\n");
        fw.write("// run time: "+(System.currentTimeMillis() - start)/1000.0+"s\n");
        fw.write("\n");
        FileWriter fw1 = new FileWriter(OUT_PATH+"result.lg");
        fw1.write("t # 1\n");

        for (int i = 0; i < searcher.result.size(); i++) {
            String out = DFScodeSerializer.serialize(searcher.result.get(i));
            fw.write(i + ":\n");
            fw.write(out);
            fw1.write(out);
        }
        fw1.close();
        fw.close();


    }

}
