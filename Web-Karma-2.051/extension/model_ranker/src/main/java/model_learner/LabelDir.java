package model_learner;

import org.apache.avro.data.Json;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Scanner;

import static edu.isi.karma.modeling.research.Params.ROOT_DIR;

public class LabelDir {
    public static HashMap<String,Integer> NodeLabel = new HashMap<>();
    public static HashMap<String,Integer> EdgeLabel = new HashMap<>();

    public static void getLabel() throws FileNotFoundException {
//        String path = ROOT_DIR+"Museum_label.txt";
//        File f = new File(path);
//        Scanner scanner = new Scanner(f);
//        while(scanner.hasNextLine()){
//            String line = scanner.nextLine().trim();
//            if (line.startsWith("E")){
//                String[] lineSpilt = line.split("\t");
//                NodeLabel.put(lineSpilt[0],Integer.parseInt(lineSpilt[1]));
//            }else if (line.startsWith("P")){
//                String[] lineSpilt = line.split("\t");
//                EdgeLabel.put(lineSpilt[0],Integer.parseInt(lineSpilt[1]));
//            }else if (line.equals("")){
//                continue;
//            }
//
//        }
        NodeLabel.put("Offer", 0);
        NodeLabel.put("CreativeWork", 1);
        NodeLabel.put("PersonOrOrganization", 2);
        NodeLabel.put("Place", 3);
        NodeLabel.put("ContactPoint", 4);
        NodeLabel.put("PostalAddress", 5);
        NodeLabel.put("Firearm", 6);

        EdgeLabel.put("seller", 0);
        EdgeLabel.put("availableAtOrFrom", 1);
        EdgeLabel.put("address", 2);
        EdgeLabel.put("itemOffered", 3);
        EdgeLabel.put("contactPoint", 4);
        EdgeLabel.put("mainEntityOfPage", 5);
        EdgeLabel.put("manufacturer", 6);
        EdgeLabel.put("relatedTo", 7);



//        scanner.close();
    }

    public static void main(String[] args) throws FileNotFoundException {
        System.out.println("LabelDir.main");
    }
}
