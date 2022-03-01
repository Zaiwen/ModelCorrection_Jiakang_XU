package au.com.d2dcrc.yago2es;

/**This class supports to generate a CSV files, which is the input of RapidMiner**/

import java.io.IOException;
import java.io.Writer;
import java.util.List;


/**
 * Created by Zaiwen Feng on 1/06/2017.
 */
public class CSVUtils {

    private static final char DEFAULT_SEPARATOR = ',';

    /**
     * @param w Writer
     * @param values  a list of string
     * @throws IOException If the io exception happens
     *
     * **/
    public static void writeLine(Writer w, List<String> values) throws IOException {

        writeLine(w, values, DEFAULT_SEPARATOR, ' ');
    }


    /**
     * @param w writer
     * @param values values
     * @param separators separators
     * @throws IOException If writing lines fails
     * **/
    public static void writeLine(Writer w, List<String> values, char separators) throws IOException {

        writeLine(w, values, separators, ' ');
    }


    /**
     * @param value
     * **/
    //https://tools.ietf.org/html/rfc4180
    private static String followCSVformat(String value) {

        String result = value;
        if (result.contains("\"")) {

            result = result.replace("\"", "\"\"");
        }
        return result;
    }


    /**
     * @param w writer
     * @param values values
     * @param separators separators
     * @param customQuote custom quotes
     * @throws IOException If writing lines fails
     * **/
    public static void writeLine(Writer w, List<String> values, char separators, char customQuote) throws IOException{

        boolean first = true;

        if(separators == ' '){
            separators = DEFAULT_SEPARATOR;
        }

        StringBuilder sb = new StringBuilder();
        for(String value : values){

            if (!first) {
                sb.append(separators);
            }
            if (customQuote == ' ') {
                sb.append(followCSVformat(value));

            } else {
                sb.append(customQuote).append(followCSVformat(value)).append(customQuote);
            }

            first = false;
        }
        sb.append("\n");
        w.append(sb.toString());

    }



}
