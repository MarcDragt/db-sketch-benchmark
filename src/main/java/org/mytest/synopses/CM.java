package org.mytest.synopses;
import org.mytest.structures.FrequencySynopsis;
import org.apache.commons.codec.digest.MurmurHash3;

import java.util.Random;

/**
 * CM instance, represents a Count-Min sketch
 */
public class CM implements FrequencySynopsis {

    private final int depth;
    private final int width;
    private final int[][] C;
    private final int hashCodeSeed1;
    private final int hashCodeSeed2;

    public int getDepth() {
        return depth;
    }
    public int getWidth() {
        return width;
    }
    public int[][] getC() {
        return C;
    }

    /**
     * Creates a Count-Min sketch
     * @param d - depth of Count-Min: number of hash functions to be used
     * @param w - width of Count-Min: the range onto which can be hashed {1, ..., w}
     */
    public CM(int d, int w){
        this.C = new int[d][w];
        this.depth = d;
        this.width = w;
        Random rand = new Random(0); // the hash is not random but different :/
        this.hashCodeSeed1 = rand.nextInt();
        this.hashCodeSeed2 = rand.nextInt();
    }

    private int hash(byte[] value, int j){
        int hashCode1 = MurmurHash3.hash32x86(value, 0, value.length, hashCodeSeed1);
        int hashCode2 = MurmurHash3.hash32x86(value, 0 , value.length, hashCodeSeed2);
        int combinedHash = hashCode1 + (j * hashCode2);
        if (combinedHash < 0) {
            combinedHash = ~combinedHash;
        }
        return combinedHash%this.width;
    }

    /**
     * Insert function to insert a value into the Count-Min sketch
     * @param value - The value to be inserted
     */
    public void insert(byte[] value){
        for (int j = 0; j < this.depth; j++){
            int position = hash(value, j);
            C[j][position] += 1;
        }
    }

    /**
     * Query function to look up the estimated count of the variable value
     * @param value - The value for which you want the estimated count
     * @return - The integer value of estimated count
     */
    public Integer query(byte[] value){
        int minValue = Integer.MAX_VALUE;
        for (int j = 0; j < this.depth; j++){
            int position = hash(value, j);
            if (C[j][position] < minValue){
                minValue = C[j][position];
            }
        }
        return minValue;
    }
}
