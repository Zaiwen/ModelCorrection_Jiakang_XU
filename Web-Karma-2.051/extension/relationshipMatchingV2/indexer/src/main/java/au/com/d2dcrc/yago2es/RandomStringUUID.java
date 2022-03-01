package au.com.d2dcrc.yago2es;

import java.util.UUID;

/**
 * Created by Zaiwen Feng on 16/04/2017.
 *
 * This class aims to create a random id for vertices and edges in the linked data graph*/


public class RandomStringUUID {

    /**
     * @param args input args
     * **/
    public static void main(String args[]){

        UUID uuid = UUID.randomUUID();

        String randomUUIDString = uuid.toString();

        System.out.println("Random UUID String = " + randomUUIDString);


    }

    /**
     * @return UUID
     * **/
    public static String createUUID(){

        UUID uuid = UUID.randomUUID();

        String randomUUIDString = uuid.toString();

        return randomUUIDString;

    }

}
