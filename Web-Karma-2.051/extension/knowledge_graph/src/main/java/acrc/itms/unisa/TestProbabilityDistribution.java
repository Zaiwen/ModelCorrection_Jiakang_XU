package acrc.itms.unisa;

import org.apache.commons.math3.distribution.BinomialDistribution;
import org.apache.commons.math3.distribution.NormalDistribution;

import java.util.Random;

/**for this distribution, x, this method returns x such that P(X=x)=p.*
 *
 *
 * */
public class TestProbabilityDistribution {

    public void testRandomDataNormalDistribution() {

        for(int run=0; run<1; run++){

            //Random r = new Random(System.currentTimeMillis());
            //System.out.println("the random number is " +  2);
            NormalDistribution dist = new NormalDistribution(0.0, 5);

            //matrix size
            int size = 5;
            System.out.println("the size is " + size);

            double[][] data = new double[size][size];
            for(int i=0; i<size; i++){
                for(int j=0; j<size; j++){
                    data[i][j]=dist.sample();
                    System.out.print(data[i][j]+" ");
                }
                System.out.println("");
            }

            double lowerTail = dist.cumulativeProbability(-5);
            double upperTail = dist.cumulativeProbability(5);
            System.out.println("lowTail is: " + lowerTail);
            System.out.println("upperTail is: " + upperTail);

            /**generate a random number between 0 and 1**/
            Random random= new Random();
            float randomFloat = random.nextFloat();
            System.out.println("the random float number is " + randomFloat);

            /**sampling**/
            double randomVariable = dist.inverseCumulativeProbability(randomFloat);
            System.out.println("the random variable is: " + randomVariable);
        }
    }

    public static void main(String args[]){

//        acrc.itms.unisa.TestProbabilityDistribution testProbabilityDistribution = new acrc.itms.unisa.TestProbabilityDistribution();
//        testProbabilityDistribution.testRandomDataNormalDistribution();
        BinomialDistribution binomialDistribution = new BinomialDistribution(7,0.5);
        int result = binomialDistribution.sample();
        System.out.println(result);
    }
}
