package Dijkstra;

import dataStructures.DFScodeSerializer;
import dataStructures.Graph;
import search.Searcher;

import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.*;

public class Extension {

    public static final int[] TRAIN = {1, 6, 12};
    public static final int TEST = 17;
//    public static final String GRAPH_PATH =
//            String.format("D:\\ASM\\experiment\\model_correction_20220217\\train_%d_%d_%d\\newSource_%d\\modelgraphs\\",
//                    TRAIN[0], TRAIN[1], TRAIN[2] , TEST);
//    public static final String GRAPH_PATH =
//        String.format("C:\\D_Drive\\ASM\\experiment\\exp_20220522\\extension\\(%d,%d,%d)\\seed_models\\s%d_seed.lg",
//                TRAIN[0], TRAIN[1], TRAIN[2], TEST);

    public static final String GRAPH_PATH = "C:\\D_Drive\\ASM\\experiment\\exp_20220530\\s17_seed.lg";
    public static final String KGPATH = "C:\\D_Drive\\ASM\\DataSets\\museum-crm\\museum_kg_20220513.lg";
//    public static final String OUT_PATH =
//            String.format("D:\\ASM\\experiment\\exp_20220218\\(%d, %d, %d)-%d\\", TRAIN[0], TRAIN[1], TRAIN[2], TEST);
//    public static final String OUT_PATH =
//        String.format("C:\\D_Drive\\ASM\\experiment\\exp_20220530\\extension\\(%d,%d,%d)\\result_models\\",
//                TRAIN[0], TRAIN[1], TRAIN[2]);
    public static final String OUT_PATH ="C:\\D_Drive\\ASM\\experiment\\exp_20220530\\";
    public static final int[] newNodeArr = {17};
    public static final int FREQ = 1;


    public static void main(String[] args) throws Exception {

        test();
        System.exit(2);

        Scanner scanner = new Scanner(new File("C:\\D_Drive\\ASM\\experiment\\exp_20220410\\newNodes.txt"));
        ArrayList<ArrayList<Integer>> newNodesList = new ArrayList<>();
        while (scanner.hasNextLine()){
            String[] split = scanner.nextLine().split(",");
            ArrayList<Integer> tmp = new ArrayList<>();
            for (String s : split) {
                tmp.add(Integer.parseInt(s));
            }
            tmp.sort((i1, i2) -> {
                if (Objects.equals(i1, i2)){
                    return 0;
                }
                return i1 > i2 ? 1:-1;
            });
            newNodesList.add(tmp);
        }

        System.out.println(newNodesList);
        System.out.println(newNodesList.size());


        Graph graph = new Graph(1,0);
//        graph.loadFromFile_Ehab(GRAPH_PATH+"model_0.lg");
        graph.loadFromFile_Ehab(GRAPH_PATH);


//        ArrayList<Integer> newNodes =new ArrayList<>();
//


        HashMap<Integer, Integer> constraintMap = new HashMap<>();
        constraintMap.put(0,1);
        constraintMap.put(1,1);
        constraintMap.put(2,1);
        constraintMap.put(3,1);
        constraintMap.put(4,3);
        constraintMap.put(5,1);
        constraintMap.put(6,1);
        constraintMap.put(7,1);
        constraintMap.put(8,1);
        constraintMap.put(9,1);
        constraintMap.put(12,1);
        constraintMap.put(15,1);
        constraintMap.put(13,1);
        constraintMap.put(17,1);

        long start = System.currentTimeMillis();

        String fName = OUT_PATH;

        if (!new File(OUT_PATH).exists()) {
            new File(OUT_PATH).mkdir();
        }

        for (ArrayList<Integer> newNodes : newNodesList) {

            Searcher<String, String> searcher = new Searcher<>(KGPATH, FREQ, 1, graph);
            searcher.initialize();
            searcher.extend(newNodes, constraintMap);

            if (searcher.result.size() > 0){
                Date date = new Date();
                SimpleDateFormat sdf = new SimpleDateFormat("_MM_dd_H_m_");
                fName += "result"+TEST;
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

                break;
            }

        }
    }


    public static void test() throws Exception {
        Graph graph = new Graph(1,0);
//        graph.loadFromFile_Ehab(GRAPH_PATH+"model_0.lg");
        graph.loadFromFile_Ehab(GRAPH_PATH);


        ArrayList<Integer> newNodes =new ArrayList<>();
        for (int i : newNodeArr) {
            newNodes.add(i);
        }

        HashMap<Integer, Integer> constraintMap = new HashMap<>();

        for (Integer nodeLabel : graph.getListGraph().getNodeLabels()) {
            if (!constraintMap.containsKey(new Integer(nodeLabel))){
                constraintMap.put(nodeLabel, 1);
            }else {
                constraintMap.put(nodeLabel, constraintMap.get(nodeLabel)+1);
            }
        }

        for (Integer newNode : newNodes) {
            if (!constraintMap.containsKey(new Integer(newNode))){
                constraintMap.put(newNode, 1);
            }else {
                constraintMap.put(newNode, constraintMap.get(newNode)+1);
            }
        }

//        System.out.println(constraintMap);
//        System.exit(1);

        if (!constraintMap.containsKey(new Integer(8))){
            constraintMap.put(8, 0);
        }

        if (!constraintMap.containsKey(new Integer(7))){
            constraintMap.put(7, 0);
        }


        long start = System.currentTimeMillis();

        String fName = OUT_PATH;

        if (!new File(OUT_PATH).exists()) {
            new File(OUT_PATH).mkdir();
        }


        Searcher<String, String> searcher = new Searcher<>(KGPATH, FREQ, 1, graph);
        searcher.initialize();
        searcher.extend(newNodes, constraintMap);

        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("_MM_dd_H_m_");
//        +sdf.format(date)
        fName += "s" + TEST + "_result";
        fName += ".txt";
        FileWriter fw = new FileWriter(fName);
        fw.write("// train: s"+TRAIN[0]+" s"+TRAIN[1]+" s"+TRAIN[2]+"\n");
        fw.write("// new source: s"+TEST+"\n");
        fw.write("// constraintMap:"+constraintMap.toString()+"\n");
        fw.write("// run time: "+(System.currentTimeMillis() - start)/1000.0+"s\n");
        fw.write("\n");
        FileWriter fw1 = new FileWriter(OUT_PATH+ "s" + TEST + "_result.lg");
        fw1.write("t # 1\n");

        for (int i = 0; i < searcher.result.size(); i++) {
            String out = DFScodeSerializer.serialize(searcher.result.get(i));
            fw.write(i + ":\n");
            fw.write(out);
            fw1.write(out);
        }
        fw1.close();
        fw.close();
        System.out.println((System.currentTimeMillis() - start) / 1000.0);
    }

}
