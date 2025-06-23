package org.mytest.synopses;

import com.google.common.hash.Hashing;
import com.google.common.hash.HashFunction;
import org.mytest.structures.MembershipSynopsis;

import java.util.Random;
import java.util.BitSet;

public class BloomFilter implements MembershipSynopsis {

    private int m;
    private BitSet bm;
    private final int k;

    private final HashFunction[] hashFunctions;

    public BloomFilter(double epsilon, int exp_nr) {
//        this.w = (int) Math.ceil(Math.E / epsilon);
//        this.d = (int) Math.ceil(Math.log(1 / delta))

        double ln2 = Math.log(2);
        this.m = (int) Math.ceil(-(exp_nr * Math.log(epsilon)) / (ln2 * ln2));
        this.k = (int) Math.ceil((m / (double) exp_nr) * ln2);
        this.bm = new BitSet(m);

        hashFunctions = new HashFunction[k];
        for (int i = 0; i < k; i++) {
            hashFunctions[i] = Hashing.murmur3_128(i + 1); // seeds from 1 to k
        }

    }

    private int Hash(byte[] key, int i) {
        int hash = (hashFunctions[i].hashBytes(key).asInt() % m + m)% m;
        return hash;
    }

    public void insert(byte[] key){
        for (int i=0; i<k; i++){
            int coord = Hash(key, i);

            bm.set(coord);
        }
    }

    public Boolean query(byte[] key){
        boolean exists = true;
        for (int i = 0; i < k; i++) {
            int coord = Hash(key, i);
            if (!bm.get(coord)){
                exists = false;
                return exists;
            }
        }
        return exists;
    }


    public long calculateRAMBytes() {
        return ((long) Math.ceil(m / 64.0)) * 8;
    }

    public double filledPercentage() {return ((double)bm.cardinality())/((double) this.m);}

}
