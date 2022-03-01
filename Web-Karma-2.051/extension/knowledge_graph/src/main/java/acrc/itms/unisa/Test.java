package acrc.itms.unisa;
import com.google.common.collect.Iterables;

import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;

public class Test {

    public static void main (String args[]) {

        Set<String> colors = new HashSet<>();
        colors.add("yellow");
        colors.add("blue");
        colors.add("black");
        colors.add("purple");
        colors.add("red");



        Iterable<String> strings = Iterables.limit(colors,3);
        Iterator<String> it = strings.iterator();
        while (it.hasNext()) {

            String str = it.next();
            System.out.println(str);

        }

    }


}
