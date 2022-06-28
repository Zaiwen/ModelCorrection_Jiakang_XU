package Dijkstra;

import dataStructures.DFScodeSerializer;
import dataStructures.Graph;
import search.Searcher;

import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.*;

public class ModelExtension {

    public static int[] TRAIN = {};
    public static int TEST = 0;
    public static String GRAPH_PATH = "";
    public static String KGPATH = "";
    public static String OUT_PATH = "";
    public static int[] newNodeArr = {};
    public static int[] constraintMapKeys = {};
    public static int[] constraintMapValues = {};
    public static int FREQ = 1;


    public static void main(String[] args) throws Exception {

        Scanner scanner = new Scanner(new File(args[0]));
        while (scanner.hasNextLine()){
            String line = scanner.nextLine();
            if (line.startsWith("graph_path;")){
                GRAPH_PATH = line.split(";")[1];
            }else if (line.startsWith("train_set:")){
                String str = line.split(":")[1];
                String[] strArr = str.split(",");
                TRAIN = new int[strArr.length];
                for (int i = 0; i < strArr.length; i++) {
                    TRAIN[i] = Integer.parseInt(strArr[i]);
                }
            }else if(line.startsWith("new_source:")){
                TEST = Integer.parseInt(line.split(":")[1]);
            }else if (line.startsWith("kg_path;")){
                KGPATH = line.split(";")[1];
            }else if (line.startsWith("out_path;")){
                OUT_PATH = line.split(";")[1];
            }else if (line.startsWith("freq:")){
                FREQ = Integer.parseInt(line.split(":")[1]);
            }
            else if (line.startsWith("newNodes:")){
                String str = line.split(":")[1];
                String[] strArr = str.split(",");
                newNodeArr = new int[strArr.length];
                for (int i = 0; i < strArr.length; i++) {
                    newNodeArr[i] = Integer.parseInt(strArr[i]);
                }
            }
            else if (line.startsWith("constraintMapKeys:")){
                String str = line.split(":")[1];
                String[] strArr = str.split(",");
                constraintMapKeys = new int[strArr.length];
                for (int i = 0; i < strArr.length; i++) {
                    constraintMapKeys[i] = Integer.parseInt(strArr[i]);
                }
            }else if (line.startsWith("constraintMapValues:")){
                String str = line.split(":")[1];
                String[] strArr = str.split(",");
                constraintMapValues = new int[strArr.length];
                for (int i = 0; i < strArr.length; i++) {
                    constraintMapValues[i] = Integer.parseInt(strArr[i]);
                }
            }
        }

        test();
        System.exit(1);



        Graph graph = new Graph(1,0);
//        graph.loadFromFile_Ehab(GRAPH_PATH+"model_0.lg");
        graph.loadFromFile_Ehab(GRAPH_PATH);

        Scanner scanner1 = new Scanner(new File("newNodes.txt"));

        ArrayList<ArrayList<Integer>> newNodesList = new ArrayList<>();
        while (scanner1.hasNextLine()){
            String[] split = scanner1.nextLine().split(",");
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


        HashMap<Integer, Integer> constraintMap = new HashMap<>();
        assert constraintMapKeys.length == constraintMapValues.length;
        for (int i = 0; i < constraintMapKeys.length; i++) {
            constraintMap.put(constraintMapKeys[i], constraintMapValues[i]);
        }


        long start = System.currentTimeMillis();

        String fName = OUT_PATH;
        fName += "result.txt";

        if (!new File(OUT_PATH).exists()) {
            new File(OUT_PATH).mkdir();
        }

        FileWriter fw = new FileWriter(fName);
        fw.write("// train_set: s"+TRAIN[0]+" s"+TRAIN[1]+" s"+TRAIN[2]+"\n");
        fw.write("// new source: s"+TEST+"\n");
        fw.write("// constraintMap:"+constraintMap.toString()+"\n");
        fw.write("// run time: "+(System.currentTimeMillis() - start)/1000.0+"s\n");
        fw.write("\n");
        FileWriter fw1 = new FileWriter(OUT_PATH+"result.lg");


        for (ArrayList<Integer> newNodes : newNodesList) {
            Searcher<String, String> searcher = new Searcher<>(KGPATH, FREQ, 1, graph);
            searcher.initialize();
            searcher.extend(newNodes, constraintMap);
            if (searcher.result.size() > 0){
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
        graph.loadFromFile_Ehab(GRAPH_PATH);

        ArrayList<Integer> newNodes =new ArrayList<>();
        for (int i : newNodeArr) {
            newNodes.add(i);
        }

        HashMap<Integer, Integer> constraintMap = new HashMap<>();
        assert constraintMapKeys.length == constraintMapValues.length;
        for (int i = 0; i < constraintMapKeys.length; i++) {
            constraintMap.put(constraintMapKeys[i], constraintMapValues[i]);
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
        fName += "result"+sdf.format(date);
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
