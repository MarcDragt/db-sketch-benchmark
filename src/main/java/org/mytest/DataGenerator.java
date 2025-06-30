package org.mytest;

import java.util.*;

import org.apache.commons.math3.distribution.ZipfDistribution;
import org.apache.commons.math3.random.Well19937c;

import static org.mytest.DataBytes.toBytes;

public class DataGenerator {


    /**
     * Generates a random string representing an IP adress using the java.util.Random class.
     * The random IP adress is then converted into byte array using the InetAdress.getAddress function.
     * @return Returns a byte array that represents a random IP adress between 0.0.0.0 and 255.255.255.255 (inclusive).
     */
    private static int generateRandomIP(Random rand) {
        return rand.nextInt();
    }

//    private static byte[][] generateUniformIPList (int length) {
//        byte[][] IpList = new byte[length][];
//        for (int i = 0; i < length; i++) {
//            Random rand = new Random();
//            IpList[i] = generateRandomIP(rand);
//        }
//        return IpList;
//    }

//    public static byte[][] generateUniformIPList (int length, long seed) {
//        byte[][] IpList = new byte[length][];
//        Random rand = new Random(seed);
//        for (int i = 0; i < length; i++) IpList[i] = generateRandomIP(rand);
//        return IpList;
//    }

    private static DataSynth DisjointUniUniIPList(int dataLength, int queryLength, long seed) {
        Random rand = new Random(seed);
        Set<Integer> tracker = new HashSet<>();
        int[] inserts = new int[dataLength];
        int[] insertQuery = new int[queryLength];
        int[] queries = new int[queryLength];

        for (int i = 0; i < queryLength; i++) {
            int entry = generateRandomIP(rand);
            tracker.add(entry);
            queries[i] = entry;
        }
        for (int i = 0; i < dataLength; i++) {
            int entry = generateRandomIP(rand);
            while (tracker.contains(entry)) {
                entry = generateRandomIP(rand);
            }
            inserts[i] = entry;
        }
        for (int i = 0; i < queryLength; i++) {
            insertQuery[i] = inserts[rand.nextInt(inserts.length)];
        }
        return new DataSynth(inserts, queries, insertQuery);
    }

    /**
     * Generate a disjoint uniform list of dataLength, and querylenght.
     * Advised to let querylength not be larger than 1.000.000 as this will be too intensive on heapsize,
     * and this will likely not make estimates more accurate (1M should be more than enough for error estimate)
     * @param dataLength
     * @param queryLength
     * @param seed
     * @return byteArrays object that stores two byte[][]s
     */
    public static DataBytes generateDisjointUniUniIPList(int dataLength, int queryLength, long seed) {
        Random rand = new Random(seed);
        Set<Integer> tracker = new HashSet<>();
        int[] inserts = new int[dataLength];
        int[] insertQuery = new int[queryLength];
        int[] queries = new int[queryLength];

        for (int i = 0; i < queryLength; i++) {
            int entry = generateRandomIP(rand);
            tracker.add(entry);
            queries[i] = entry;
        }
        for (int i = 0; i < dataLength; i++) {
            int entry = generateRandomIP(rand);
            while (tracker.contains(entry)) {
                entry = generateRandomIP(rand);
            }
            inserts[i] = entry;
        }
        for (int i = 0; i < queryLength; i++) {
            insertQuery[i] = inserts[rand.nextInt(inserts.length)];
        }
        return new DataBytes(DataBytes.getBytes(inserts), DataBytes.getBytes(queries), DataBytes.getBytes(insertQuery));
    }

    public static DataBytes generateDisjointUniUniCapped(int dataLength, int queryLength, long seed, int numberOfDistincts) {
        int[] pool = new int[numberOfDistincts];
        int[] inserts = new int[dataLength];
        int[] insertQuery = new int[queryLength];
        int[] queries = new int[queryLength];
        Set<Integer> tracker = new HashSet<>();
        Random rand = new Random(seed);

        for (int i = 0; i < queryLength; i++) {
            int entry = generateRandomIP(rand);
            tracker.add(entry);
            queries[i] = entry;
        }

        // forget about mixed for now, probably not gonna be used
        for (int i = 0; i < numberOfDistincts; i++) {
            int entry = generateRandomIP(rand);
            while (tracker.contains(entry)) {
                entry = generateRandomIP(rand);
            }
            pool[i] = entry;
        }

        for (int i = 0; i < queryLength; i++) {
            inserts[i] = pool[rand.nextInt(numberOfDistincts)];
        }
        for (int i = 0; i < queryLength; i++) {
            insertQuery[i] = inserts[rand.nextInt(inserts.length)];
        }
        for (int i = queryLength; i < dataLength; i++) inserts[i] = pool[rand.nextInt(numberOfDistincts)];

        return new DataBytes(DataBytes.getBytes(inserts), DataBytes.getBytes(queries), DataBytes.getBytes(insertQuery));
    }
    /**
     * Generate uniformly distributed data and Zipf distributed queries
     * @param dataLength
     * @param queryLength
     * @param seed
     * @return
     */
    public static DataBytes generateDisjointUniZipfIPList (int dataLength, int queryLength, long seed) {
        DataSynth data = DisjointUniUniIPList(dataLength, queryLength,  seed);
        int[] queries = new int[queryLength];
        int[] insertQuery = new int[queryLength];
        int[] insertsData = data.getInserts();
        int[] queriesData = data.getQueries();

        Well19937c randI = new Well19937c(seed+1); //standard implementation, but with seed set
        ZipfDistribution zipfInsertQ = new ZipfDistribution(randI, queryLength-1, 1);
        for (int i = 0; i < queryLength; i++) {
            int num = zipfInsertQ.sample();
            insertQuery[i] = queriesData[num];
        }

        Well19937c randQ = new Well19937c(seed+2); //standard implementation, but with seed set
        ZipfDistribution zipfQuery = new ZipfDistribution(randQ, queryLength-1, 1);
        for (int i = 0; i < queryLength; i++) {
            int num = zipfQuery.sample();
            queries[i] = queriesData[num];
        }

        return new DataBytes(DataBytes.getBytes(insertsData), DataBytes.getBytes(queries), DataBytes.getBytes(insertQuery));
    }

    /**
     * Generate a disjoint zipfian list of dataLength, and querylenght
     * Advised to let querylength not be larger than 1.000.000 as this will be too intensive on heapsize,
     * and this will likely not make estimates more accurate (1M should be more than enough for error estimate)
     * @param dataLength
     * @param queryLength
     * @param seed
     * @return
     */
    public static DataBytes generateDisjointZipfZipfIPList(int dataLength, int queryLength, long seed) {
        DataSynth data = DisjointUniUniIPList(dataLength, queryLength,  seed);
        int[] inserts = new int[dataLength];
        int[] queries = new int[queryLength];
        int[] insertsData = data.getInserts();
        int[] queriesData = data.getQueries();
        int[] insertQuery = new int[queryLength];
        Set<Integer> seen = new HashSet<>();
        List<Integer> uniqueInsertSamples = new ArrayList<>();

        Well19937c rand = new Well19937c(seed); //standard implementation, but with seed set
        ZipfDistribution zipfInsert = new ZipfDistribution(rand, dataLength-1, 1);

        for (int i = 0; i < dataLength; i++){
            int num = zipfInsert.sample();
            inserts[i] = insertsData[num];
            int key = inserts[i];
            if (seen.add(key)) {
                uniqueInsertSamples.add(inserts[i]);
            }
        }
        Well19937c randI = new Well19937c(seed+1); //standard implementation, but with seed set
        ZipfDistribution zipfInsertQ = new ZipfDistribution(randI, uniqueInsertSamples.size()-1, 1);
        for (int i = 0; i < queryLength; i++) {
            int num = zipfInsertQ.sample();
            insertQuery[i] = uniqueInsertSamples.get(num);
        }

        Well19937c randQ = new Well19937c(seed+2); //standard implementation, but with seed set
        ZipfDistribution zipfQuery = new ZipfDistribution(randQ, queryLength-1, 1);
        for (int i = 0; i < queryLength; i++) {
            int num = zipfQuery.sample();
            queries[i] = queriesData[num];
        }

        return new DataBytes(DataBytes.getBytes(insertsData), DataBytes.getBytes(queries), DataBytes.getBytes(insertQuery));
    }

    /**
     * Generate zipf distributed data and uniformly distributed queries
     * @param dataLength
     * @param queryLength
     * @param seed
     * @return
     */
    public static DataBytes generateDisjointZipfUniIPList (int dataLength, int queryLength, long seed) {
        DataSynth data = DisjointUniUniIPList(dataLength, queryLength,  seed);
        int[] inserts = new int[dataLength];
        int[] insertsData = data.getInserts();
        int[] queriesData = data.getQueries();
        Set<Integer> seen = new HashSet<>();
        List<Integer> uniqueInsertSamples = new ArrayList<>();


        //sample from the list and add all unique instances to list
        int queryLengthCounter = 0;
        ZipfDistribution zipfInsert = new ZipfDistribution(dataLength-1, 1);
        for (int i = 0; i < dataLength; i++) {
            int num = zipfInsert.sample();
            inserts[i] = insertsData[num];
            int key = inserts[i];
            if (seen.add(key)) {
                uniqueInsertSamples.add(inserts[i]);
            }
        }

        // If the distinct samples are enough to reach the queryLength size, use those
        // randomly sample from the distinct inserts to reach the required size
        int[] insertQuery = new int[queryLength];
        Random rand = new Random();
        if (uniqueInsertSamples.size() >= queryLength) {
            Collections.shuffle(uniqueInsertSamples, rand);
            for (int i = 0; i < queryLength; i++) {
                insertQuery[i] = uniqueInsertSamples.get(i);
            }
        }
        else {
            // part for mixed
            for (int i = 0; i < queryLength; i++) {
                int randIdx = rand.nextInt(uniqueInsertSamples.size());
                insertQuery[i] = uniqueInsertSamples.get(randIdx);
            }
        }
        return new DataBytes(DataBytes.getBytes(inserts), DataBytes.getBytes(queriesData), DataBytes.getBytes(insertQuery));
    }

    // think about how to sample from 4 billion items, 4 times independendant samples kinda ass
    // only the first length of 4 billion items seems kinda wierd also...

    // private until actual use can be found or deleted
//    private static byte[][] generateZipfianIPList (int length) {
//        byte[][] ipList = new byte[length][];
//        byte[][] sampleList = generateUniformIPList(length);
//        // zipfian distribution will be dependant on the length of the array
//        ZipfDistribution zipf = new ZipfDistribution(length, 1.5);
//        for (int i = 0; i < length; i++) {
//            int num = zipf.sample();
//            ipList[i] = sampleList[num];
//        }
//        return ipList;
//    }

//     public static byte[][] generateZipfianIPList (int length, long seed) {
//         byte[][] ipList = new byte[length][];
//         byte[][] sampleList = generateUniformIPList(length, seed);
//         // zipfian distribution will be dependant on the length of the array
//         ZipfDistribution zipf = new ZipfDistribution(length-1, 1.5);
//         for (int i = 0; i < length; i++) {
//             int num = zipf.sample();
//             ipList[i] = sampleList[num];
//         }
//         return ipList;
//     }
}
