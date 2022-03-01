package au.edu.unisa;

import java.util.HashSet;
import java.util.Set;

/**
 * @Anchor Zaiwen Feng
 * @create 8 Mar 2019
 * The instance of this class represents a functional dependency in relational data
 *
 * e.g. A,B --> C
 * We say C depends on A & B. Or, A & B determine C
 * Here, A & B are determiners
 * C is dependent
 * **/
public class FD {

    private String determinant = null;
    private String dependent = null;

    //Constructor
    public FD (String determinant, String dependent) {
        this.determinant = determinant;
        this.dependent = dependent;
    }

    public void setDeterminant (String determinant) {
        this.determinant = determinant;
    }

    public String getDeterminant () {
        return this.determinant;
    }


    public void setDependent (String dependent) {
        this.dependent = dependent;
    }

    public String getDependent () {
        return this.dependent;
    }
}
