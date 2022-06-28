package Dijkstra;

import com.google.gson.stream.JsonReader;
import dataStructures.DFScodeSerializer;
import dataStructures.Graph;
import search.Searcher;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

public class Extension {

    public static int[] TRAIN = {1,2,3};
    public static int TEST = 2;
    public static ArrayList<ArrayList<ArrayList<String>>> isoColsList = new ArrayList<>();

    //    public static final String GRAPH_PATH =
//            String.format("D:\\ASM\\experiment\\model_correction_20220217\\train_%d_%d_%d\\newSource_%d\\modelgraphs\\",
//                    TRAIN[0], TRAIN[1], TRAIN[2] , TEST);
    public static String GRAPH_PATH =
        String.format("C:\\D_Drive\\ASM\\experiment\\exp_20220613\\train_%d_%d_%d___1\\newSource_%d\\seed.lg",
                TRAIN[0], TRAIN[1], TRAIN[2], TEST);

//    public static final String GRAPH_PATH = "C:\\D_Drive\\ASM\\experiment\\correct_models\\lg\\s28.csv.lg";
    public static final String KGPATH = "C:\\D_Drive\\ASM\\DataSets\\museum-crm\\museum_kg_20220513.lg";
//    public static final String OUT_PATH =
//            String.format("D:\\ASM\\experiment\\exp_20220218\\(%d, %d, %d)-%d\\", TRAIN[0], TRAIN[1], TRAIN[2], TEST);
    public static String OUT_PATH =
        String.format("C:\\D_Drive\\ASM\\experiment\\exp_20220613\\train_%d_%d_%d___1\\newSource_%d\\",
                TRAIN[0], TRAIN[1], TRAIN[2], TEST);
//    public static final String OUT_PATH ="C:\\D_Drive\\ASM\\experiment\\exp_20220606\\";
    public static final int FREQ = 1;


    public static void main(String[] args) throws Exception {

        boolean flag = true;
        TRAIN = new int[]{7, 19, 23};


        if (!flag) {
            TEST = 9;
            GRAPH_PATH = String.format("C:\\D_Drive\\ASM\\experiment\\exp_20220613\\train_%d_%d_%d___1\\newSource_%d\\seed.lg",
                    TRAIN[0], TRAIN[1], TRAIN[2], TEST);
            OUT_PATH = String.format("C:\\D_Drive\\ASM\\experiment\\exp_20220613\\train_%d_%d_%d___1\\newSource_%d\\",
                    TRAIN[0], TRAIN[1], TRAIN[2], TEST);
            isoColsList = new ArrayList<>();
            try {
                loadIsoCols(String.format("C:\\D_Drive\\ASM\\experiment\\exp_20220613\\train_%d_%d_%d___1\\newSource_%d\\isoCols.json",
                        TRAIN[0], TRAIN[1], TRAIN[2], TEST));
            } catch (FileNotFoundException ignored) {
            }
            test();
        }else {
            for (int i = 1; i < 30; i++) {
                if (i != TRAIN[0] && i != TRAIN[1] && i != TRAIN[2] && i != 27) {
                    TEST = i;
                    GRAPH_PATH = String.format("C:\\D_Drive\\ASM\\experiment\\exp_20220627\\train_%d_%d_%d___1\\newSource_%d\\seed.lg",
                            TRAIN[0], TRAIN[1], TRAIN[2], TEST);
                    OUT_PATH = String.format("C:\\D_Drive\\ASM\\experiment\\exp_20220627\\train_%d_%d_%d___1\\newSource_%d\\",
                            TRAIN[0], TRAIN[1], TRAIN[2], TEST);
                    isoColsList = new ArrayList<>();
                    try {
                        loadIsoCols(String.format("C:\\D_Drive\\ASM\\experiment\\exp_20220627\\train_%d_%d_%d___1\\newSource_%d\\isoCols.json",
                                TRAIN[0], TRAIN[1], TRAIN[2], TEST));
//                        isoColsList = new ArrayList<>();
                    } catch (FileNotFoundException ignored) {
                    }
                    test();
                }
            }
        }


    }


    public static void test() throws Exception {

        Graph graph = new Graph(1,0);
        try {
            graph.loadFromFile_Ehab(GRAPH_PATH);
        }catch (FileNotFoundException e){
            return;
        }



        if (isoColsList.isEmpty()){
            return;
        }

        for (ArrayList<ArrayList<String>> isoCols : isoColsList) {

            ArrayList<Integer> newNodes =new ArrayList<>();
            ArrayList<Integer> graphNodes = graph.getListGraph().getNodeLabels();
            System.out.println(graphNodes);
            for (ArrayList<String> col : isoCols) {
                Integer newNode = Integer.parseInt(col.get(0).replace("\"", ""));
                if (newNode != 4 && newNode != 6){
                    if (!newNodes.contains(new Integer(newNode)) && !graphNodes.contains(new Integer(newNode))){
                        newNodes.add(newNode);
                    }
                }
                if (newNode == 4 && newNodes.stream().filter(integer -> integer == 4).count() < 3){
                    newNodes.add(newNode);
                }
                if (newNode == 6 && newNodes.stream().filter(integer -> integer == 6).count() < 2){
                    newNodes.add(newNode);
                }
            }

            if (newNodes.isEmpty()){
//                return;
                continue;
            }

            System.out.println(newNodes);


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
                    constraintMap.put(newNode, constraintMap.get(newNode) + 1);
                }
            }


            if (!constraintMap.containsKey(new Integer(8))){
                constraintMap.put(8, 0);
            }

            if (!constraintMap.containsKey(new Integer(7))){
                constraintMap.put(7, 0);
            }

//            System.exit(1);

            long start = System.currentTimeMillis();

            String fName = OUT_PATH;

            if (!new File(OUT_PATH).exists()) {
                new File(OUT_PATH).mkdir();
            }


            Searcher<String, String> searcher = new Searcher<>(KGPATH, FREQ, 1, graph);
            searcher.initialize();
            searcher.extend(newNodes, constraintMap);

            if (searcher.result.size() > 0){
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
                FileWriter fw2 = new FileWriter(OUT_PATH + "isoColsTypes.txt");
                fw2.write(isoCols.toString());
                fw2.close();
                break;
            }
        }


    }


    public static void loadIsoCols(String isoColsPath) throws IOException {
        JsonReader jr = new JsonReader(new FileReader(isoColsPath));

        while (jr.hasNext()){
            jr.beginArray();
            while (jr.hasNext()){
                jr.beginArray();
                ArrayList<ArrayList<String>> list1 = new ArrayList<>();
                while (jr.hasNext()){
                    ArrayList<String> list2 = new ArrayList<>();
                    jr.beginArray();
                    while (jr.hasNext()){
                        String e = jr.nextString();
                        list2.add("\""+e+"\"");
                    }
                    list1.add(list2);
                    jr.endArray();
                }
                isoColsList.add(list1);
                jr.endArray();
            }
        }
    }
}
