package au.com.d2dcrc.yago2es;

/**
 * Created by fengz on 13/04/2017.
 */
public class ValidateNumber {

    public static void main(String args[]){


        String regex = "[0-9]+";
        String data = "1233899898";
        System.out.print(data.matches(regex));


    }


}
