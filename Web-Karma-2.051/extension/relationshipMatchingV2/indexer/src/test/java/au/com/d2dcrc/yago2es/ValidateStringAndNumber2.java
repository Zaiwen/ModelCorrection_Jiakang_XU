package au.com.d2dcrc.yago2es;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by fengz on 17/04/2017.
 */
public class ValidateStringAndNumber2 {

    public static void main(String args[]){

        String URL = "519908316d0a1e7d4a4c9c9f983c9e70c6c7481d";
        //String URL = "account1";
        Pattern pattern = Pattern.compile("^(?=.*[a-z])(?=.*[0-9])[a-z0-9]{40}$");
        Matcher matcher = pattern.matcher(URL);
        boolean b = matcher.find();
        System.out.println(b);

    }



}
