package model_correction;

import dataStructures.*;


public class ModelExtension {
    public static void main(String[] args) throws Exception {
        Graph kg = new Graph(0,1000);
        kg.loadFromFile_Ehab("D:\\DataMatching\\museum_kg0.lg");
        HPListGraph<Integer, Double> lg = kg.getListGraph();
        kg.printFreqNodes();
//        for(int i = 0; i<kg.getNumberOfNodes(); i++){
//            for(int j = 0; j<kg.getNumberOfNodes(); j++){
//                myNode n1 = kg.getNode(i);
//                myNode n2 = kg.getNode(j);
//                if (lg.getEdge(i,j) > -1){
//                    n1.addreachableNode(n2, lg.getEdgeLabel(i,j));
//                }
//            }
//        }
//        System.out.println(kg.getSortedFreqLabels());


    }
}
