package au.com.d2dcrc.yago2es;

import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

/**The calss is created on 17 Nov 2017. Author. Zaiwen FENG*
 * This class is used to extract boundary graph with a specific anchor label.
 * For an instance,
 * Sometimes, we might get a bundle of boundary graphs for a specific relationship 'wasBorn'.
 * However, the right anchor might be labeled as 'region', 'location' or 'owl:thing', or some other class.
 * We need to extract some boundary graphs with a specific anchor label.
 * */
public class SelectBGWithSpecificAnchorL {

    final static String ORIGINAL_BG1 = "/Users/fengz/Project/parsemis/Yago_RT_1.lg";
    final static String EXTRACT_BG1 = "/Users/fengz/Project/parsemis/extract/Yago_RT_1.lg";

    final static String ORIGINAL_BG2 = "/Users/fengz/Project/parsemis/Yago_RT_2.lg";
    final static String EXTRACT_BG2 = "/Users/fengz/Project/parsemis/extract/Yago_RT_2.lg";

//    final static String ORIGINAL_BG3 = "/Users/fengz/Project/parsemis/Yago_RT_3.lg";
//    final static String EXTRACT_BG3 = "/Users/fengz/Project/parsemis/extract/Yago_RT_3.lg";
//
//    final static String ORIGINAL_BG4 = "/Users/fengz/Project/parsemis/Yago_RT_4.lg";
//    final static String EXTRACT_BG4 = "/Users/fengz/Project/parsemis/extract/Yago_RT_4.lg";

    /**
     * @param bgList an original file containing a list of boundary graphs with a specific relationship. e.g. 'wasBornIn'.
     * @param extractedBGList an extracted file containging a list of boundary graphs with a specific relationship (e.g. 'wasBornIn') and expected anchor points (e.g. 'Person' and 'Region').
     * @param expectedLeftAnchorL label of expected left anchor
     * @param expectedRightAnchorL label of expected right anchor
     * */
    private static void extract (String bgList, String extractedBGList, Integer expectedLeftAnchorL, Integer expectedRightAnchorL) {

        /**Initialize a new array to store extracted boundary graph**/
        List<Graph> extracted = new ArrayList<Graph>();

        /**Load boundary graphs from file**/
        List<Graph> boundaryGraphs_RT1 = GSpanWrapper.loadGraph(bgList);

        Iterator<Graph> iterator = boundaryGraphs_RT1.iterator();
        while (iterator.hasNext()) {

            Graph boundary = iterator.next();

            /**Get the left anchor of this boundary graph**/
            Vertex leftAnchorPoint = boundary.getLeftAnchorPoint();
            String leftAnchorPointLabel = leftAnchorPoint.getLabel();

            /**Get the right anchor of this boundary graph**/
            Vertex rightAnchorPoint = boundary.getRightAnchorPoint();
            String rightAnchorPointLabel = rightAnchorPoint.getLabel();

            if (leftAnchorPointLabel.equals(expectedLeftAnchorL.toString()) && rightAnchorPointLabel.equals(expectedRightAnchorL.toString())) {

                extracted.add(boundary);
            }
        }

        /**Write these boundary graphs to files **/
        GSpanWrapper.writeIndexedGraphToInputFile(extracted, extractedBGList);

        System.out.println("boundary graphs extraction done...");

    }

    public static void main (String args[]) {

        extract(ORIGINAL_BG1, EXTRACT_BG1, 202, 209);
        extract(ORIGINAL_BG2, EXTRACT_BG2, 202, 209);
//        extract(ORIGINAL_BG3, EXTRACT_BG3, 582, 631);
//        extract(ORIGINAL_BG4, EXTRACT_BG4, 582, 631);

    }


}
