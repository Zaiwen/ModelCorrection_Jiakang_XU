/*******************************************************************************
 * Copyright 2018 University of South Australia
 *
 *@Author Zaiwen FENG
 ******************************************************************************/
package edu.isi.karma.rep.alignment;


import org.apache.commons.math3.distribution.AbstractIntegerDistribution;
import org.apache.commons.math3.distribution.BinomialDistribution;
import org.apache.commons.math3.distribution.UniformIntegerDistribution;

/**The enumeration class is used to define all the enumeration types about cardinality. 18 Aug 2018**/
public class   CardinalityInfo {
    private int min;
    private int max;
    private AbstractIntegerDistribution distribution;//added on 3 Sep 2018。 Modified on 9 Nov 2018.

    public CardinalityInfo(int min, int max){
        this.min=min;
        this.max=max;
    }

    /**
     * @param min minimal cardinality degree
     * @param max maximum cardinality degree
     * @param distribution a certain discrete distribution
     * @From 7 Nov 2018
     * **/
    public CardinalityInfo(int min, int max, AbstractIntegerDistribution distribution){
        this.min=min;
        this.max=max;
        this.distribution=distribution;
    }

    public void setMin(int min){
        this.min=min;
    }

    public int getMin(){
        return this.min;
    }

    public void setMax(int max){
        this.max=max;
    }

    public int getMax(){
        return this.max;
    }

    public AbstractIntegerDistribution getDistribution(){//added on 3 Sep 2018
        return distribution;
    }

    public void setDistribution(AbstractIntegerDistribution distribution){//added on 3 Sep 2018
        this.distribution=distribution;
    }

    /**execute inverse transform sampling according to distribution type, min and max of cardinality*
     * //currently, only consider uniform distribution and binomial distribution
     * @From 12 Nov 2018
     *@return random degree
     * */
    public int sample() throws Exception{
        int randomDegree;
        if (min == max) {
            randomDegree = max;
        } else {//follow a certain distribution
            if(distribution instanceof BinomialDistribution){
                randomDegree=distribution.sample()+min;//execute inverse transform sampling
            }else if(distribution instanceof UniformIntegerDistribution){
                randomDegree=distribution.sample();
            }else {
                throw new Exception("The distribution has not been supported yet!");
            }
        }

        return randomDegree;
    }
}
