package org.mytest.synopses;
import org.mytest.structures.MembershipSynopsis;
import org.apache.commons.codec.digest.MurmurHash3;
import java.util.BitSet;
import java.util.Random;

/**
 * Bloom instance, represents a Bloom filter
 */
public class Bloom implements MembershipSynopsis {
    private final BitSet B;
    private final int length;
    private final int hashes;
    private final int hashCodeSeed1;
    private final int hashCodeSeed2;


    /**
     * Creates a Bloom filter instance
     * @param m - length m of bloom filter
     * @param k - number k of hash functions
     */
    public Bloom(int m, int k) {
        // create Bitset B of length m filled with 0's
        this.B = new BitSet(m);
        this.length = m;
        this.hashes = k;
        Random rand = new Random(0); // the hash is not random but different :/
        this.hashCodeSeed1 = rand.nextInt();
        this.hashCodeSeed2 = rand.nextInt();
    }

    public BitSet getB() {
        return B;
    }

    public int getLength() {
        return length;
    }

    public int getHashes() {
        return hashes;
    }

    private int hash(byte[] value, int j){
        int hashCode1 = MurmurHash3.hash32x86(value, 0, value.length, hashCodeSeed1);
        int hashCode2 = MurmurHash3.hash32x86(value, 0 , value.length, hashCodeSeed2);
        int combinedHash = hashCode1 + (j * hashCode2);
        if (combinedHash < 0) {
            combinedHash = ~combinedHash;
        }
        return combinedHash%this.length;
    }

    /**
     * Insert a byte array value into the Bloom filter
     * @param value - byte array to insert
     */
    @Override
    public void insert(byte[] value) {
        for (int j = 0; j < this.hashes; j++) {
            int position = hash(value, j);
            this.B.set(position);
        }
    }

    /**
     * Query the existence of the value in the Bloom filter
     * @param value - byte array to query
     * @return true if all hash functions return true, false otherwise
     */
    @Override
    public Boolean query(byte[] value) {
        for (int j = 0; j < this.hashes; j++) {
            int position = hash(value, j);
            if (!this.B.get(position)) {
                return false;
            }
        }
        return true;
    }
}
