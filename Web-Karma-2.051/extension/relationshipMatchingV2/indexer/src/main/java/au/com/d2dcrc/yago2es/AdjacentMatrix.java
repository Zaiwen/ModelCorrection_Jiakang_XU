package au.com.d2dcrc.yago2es;

import java.util.List;
import java.util.Collections;
import java.util.HashMap;
import java.util.ArrayList;

/**
 * Created by Zaiwen FENG on 7/05/2017.
 */

public class AdjacentMatrix {

    private List<Vertex> vertexes;
    private List<Edge> edges;

    private int[][] matrix;
    private HashMap<Integer, String> indexLabel;

    /**
     * convert Graph into Matrix directly int the Constructor
     *
     * @param graph the graph to be converted
     */
    public AdjacentMatrix(Graph graph) {
        vertexes = graph.getAllVertices();
        edges = graph.getAllEdges();

        matrix = new int[vertexes.size()][vertexes.size()];
        indexLabel = new HashMap<Integer, String>();

        // initialize the matrix
        // since 0 is not any weight, it's fine to set 0 as default value
        for (int i = 0; i < vertexes.size(); i++) {
            for (int j = 0; j < vertexes.size(); j++) {
                matrix[i][j] = 0;
            }
        }

        // collect all the labels in the vertexes
        for (int i = 0; i < vertexes.size(); i++) {
            indexLabel.put(i, "0" + vertexes.get(i).getLabel());
//            indexLabel.put(i, vertexes.get(i).getLabel());
//            System.out.print(vertexes.get(i) + ":" + vertexes.get(i).getLabel() + "\t");
        }

        int rowIndex;
        int columnIndex;

        for (int i = 0; i < edges.size(); i++) {
            rowIndex = Integer.parseInt(edges.get(i).getStartPoint().getId());
            columnIndex = Integer.parseInt(edges.get(i).getEndPoint().getId());

            // since it's symmetric, we need to get weight twice
            matrix[rowIndex][columnIndex] = edges.get(i).getWeight();
            matrix[columnIndex][rowIndex] = edges.get(i).getWeight();
        }

//-----------------------------printing begin---------------------------------------------------------------
//        System.out.println("Index - Label:");
//        Iterator it = indexLabel.entrySet().iterator();
//        while(it.hasNext()){
//            Map.Entry pair = (Map.Entry) it.next();
//            System.out.print("v" + pair.getKey() + ": " + pair.getValue() + "  ");
//        }
//        System.out.println("\nMatrix:");
//        for(int i = 0; i < vertexes.size(); i++){
//            for(int j = 0; j < vertexes.size(); j++){
//                System.out.print(matrix[i][j] + "\t");
//            }
//            System.out.println();
//        }
//        System.out.println();
//-----------------------------printing end-----------------------------------------------------------------
    }

    /**
     * matrix Getter
     *
     * @return matrix
     */
    public int[][] getMatrix() {
        return matrix;
    }


    /**
     * indexLabel Getter
     *
     * @return indexlabel
     */
    public HashMap<Integer, String> getIndexLabel() {
        return indexLabel;
    }


    /**
     * Acquire edge-weight---vertexes tuple for each row,
     * then put them in alphabetic order
     * e.g     0|0:1    0|0:2   -----   v0,v1   v0,v6
     *
     * @return the ArrayList for each row edge weight pair
     */
    public ArrayList<String> acquireEdgeWeightTuple() {
        ArrayList<String> edgeWeight = new ArrayList<String>();
        String tempString;
        String tempPair;

        // though it's symmetric, it may be helpful to put every grid into consideration
        for (int i = 0; i < vertexes.size(); i++) {
            tempString = "";
            tempPair = "";
            for (int j = 0; j < vertexes.size(); j++) {
                if (matrix[i][j] != 0) {
                    tempString = tempString + vertexes.get(i).getLabel() + "|" +
                            vertexes.get(j).getLabel() + ":" + matrix[i][j] + "\t";
                    tempPair = tempPair + vertexes.get(i) + "," + vertexes.get(j) + "\t";
                }
            }

            if (!tempString.equals("")) {
                edgeWeight.add(tempString + "-----\t" + tempPair);
            }
        }

//--------------------------------------symmetry way--------------------------------------
        // collect edge-weight from each row, skip half due to the symmetry
//        for(int i = 0; i < vertexes.size(); i++){
//            tempString = "";
//            tempPair = "";
//            for(int j = i + 1; j < vertexes.size(); j++){
//                if(matrix[i][j] != 0){
//                    tempString = tempString + vertexes.get(i).getLabel() + "|" +
//                            vertexes.get(j).getLabel() + ":" + matrix[i][j] + "\t";
//                    tempPair = tempPair + vertexes.get(i) + "," + vertexes.get(j) + "\t";
//                }
//            }
//
//            if(!tempString.equals("")) {
//                edgeWeight.add(tempString + "-----\t" + tempPair);
//            }
//        }
//-------------------------------------------------------------------------------------------

        // sort edgeWeight
        Collections.sort(edgeWeight);

//        for(int i = 0; i < edgeWeight.size(); i++){
//            System.out.println(edgeWeight.get(i));
//        }

        return edgeWeight;
    }


    /**
     * acquire metadata for the matrix, which is an ArrayList
     * for each item, it consists of label(row), non-zero value counter and the sum of corresponding row
     *
     * @return sorted metadata
     */
    public ArrayList<String> acquireMetadata() {
        ArrayList<String> metadata = new ArrayList<String>();
        int counter;
        int sum;
        String tempString;

        for (int i = 0; i < indexLabel.size(); i++) {
            counter = 0;
            sum = 0;
            for (int j = 0; j < indexLabel.size(); j++) {
                if (matrix[i][j] != 0) {
                    counter++;
                    sum = sum + matrix[i][j];
                }
            }
            tempString = indexLabel.get(i).toString();
            metadata.add("lable:" + tempString + ",count:" + counter + ",sum:" + sum);
        }

        // sort the metadata in alphabetical order, which means "12" would be smaller than 2
        Collections.sort(metadata);

//        for(int i = 0; i < metadata.size(); i++) {
//            System.out.println(metadata.get(i));
//        }

        return metadata;
    }

}

