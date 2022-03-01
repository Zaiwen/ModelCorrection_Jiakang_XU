package au.com.d2dcrc.yago2es;

import org.junit.Test;

/**
 * Created by Zaiwen Feng on 3/07/2017.
 */
public class testUtil {


    public void testBubbleSort () {

        int intArray[] = new int[] {5,90,35,45,150,3};

        System.out.println("Array before bubble sort");

        for (int i = 0; i < intArray.length; i++) {

            System.out.print(intArray[i] + " ");

        }

        Util.bubbleSort(intArray);

        System.out.println("");

        System.out.println("Array after bubble sort: ");

        for (int j = 0; j < intArray.length; j++) {

            System.out.print(intArray[j] + " ");

        }

    }


    public void testSubString () {

        String str = "<wordnet_person_123456";
        String subStr = str.substring(0,8);
        System.out.println ("The substring is: " + subStr);

    }


}
