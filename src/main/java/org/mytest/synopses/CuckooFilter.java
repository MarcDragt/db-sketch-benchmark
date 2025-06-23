package org.mytest.synopses;

import com.google.common.hash.Hashing;
import com.google.common.hash.HashFunction;
import org.mytest.structures.MembershipSynopsis;

import java.util.Random;
import java.util.Arrays;
public class CuckooFilter implements MembershipSynopsis {
    public int b=4; // Variable b represents the bucket size

    public int d; // Variable d represents the nr of buckets

    private int m; // Varaible m represents the maximum number of allowed kicks (from hash table, during insertion)

    public int f;

    private final long[] table; // Declares the hash table

    private final HashFunction hashFunc1;
    private final HashFunction hashFunc2;

    private static int upperPowerOfTwo(int n) {
        /*
        This method is necessary for the calculation of the maximum number of buckets.
        It rounds the integer n up to the nearest upper power of 2.
         */
        if (n <= 1) return 1;
        n--;
        n |= n >>> 1;
        n |= n >>> 2;
        n |= n >>> 4;
        n |= n >>> 8;
        n |= n >>> 16;
        n |= n >>> 32;
        return n + 1;
    }
    public static int lowerPowerOfTwo(int x) {
        if (x < 1) return 1;
        return Integer.highestOneBit(x);
    }
    public CuckooFilter(int expectedNumKeys, int maxKicks, int fingerprintSize) {
        /*
        This method constructs the filter, taking the expected number of input items (from the stream),
        the bucket size (b=4 recommended for nr of hash functions k=2,
        the maximum number of kicks allowed (during insertion) before throwing an exception, or rehashing all entries.

        small note: the ideal number of buckets is calculated and rounded to the closest upper power of two,
        this makes it easier to deal with modulus operations later on.
         */
        this.f = fingerprintSize;
        this.d = lowerPowerOfTwo((int)(Math.max(1, (expectedNumKeys/b*0.95))));
        this.m = maxKicks;

        long totalBits = (long) d * b * f;
        int numLongs = (int) ((totalBits + 63) / 64);
        this.table = new long[numLongs + 1];

        this.hashFunc1 = Hashing.murmur3_128(80805);
        this.hashFunc2 = Hashing.murmur3_128(20235);
    }

//    private int getFingerprint(byte[] key){
//        // This method uses murmur3 hash and takes the first n bits
//        int hashValue = Hashing.murmur3_128(80805).hashBytes(key).asInt();
//        // By manipulating the Ox3F we can select more or less bits, OxFFFF gives us a fingerprint of 16 bits ideal for bitpacking into long
//        int fingerprint = (hashValue & 0xFFFF);
//        return fingerprint;
//    }

    private int getFingerprint(byte[] key) {
        int hashValue = hashFunc1.hashBytes(key).asInt();
        int mask = (1 << f) - 1;
        return hashValue & mask;
    }

    private int hashH1(byte[] key) {
        // hash function h1 is straightforward murmur3, modulus d (nr of buckets)
        // It is the first part of the partial-key cuckoo hashing implementation
        int hashCo = (hashFunc2.hashBytes(key).asInt() % d + d) % d;
        return hashCo;
    }

    private int hashH2(int bucket, int f) {
        /*
        This second hash fucntion takes the result of the first hash function (called before this one) as input.
        It then hashes the keys corresponding fingerprint, and uses a bitwise xOR to compute the second hash value.
        Finaly applying modulus d, ensures hashing to an existing bucket.
        */
        int hashF = (hashFunc2.hashInt(f).asInt() % d + d) % d;
        int hashFinal = ((bucket ^ hashF)% d +d)% d;
        return hashFinal;
    }

    @Override
    public void insert(byte[] key) {
        /*
        This is the insertion algorithm as suggested in the cuckoo filter paper
        First the initial buckets are checked, if neither are available, at random one bucket is chosen,
        form which a random fingerprint is kicked.
        Both the fingerprint and the "index" are updated after which the insertion cycle starts anew.
        The insertion cycle is called at most m times. Where m is the previously defined max number of kicks.
         */
        Random random = new Random();
        byte[] query_key = new byte[] {102, -13, -11, -125, 37, 53, -44, 83};
        int fingerprint = getFingerprint(key);
        int i = hashH1(key);
        int j = hashH2(i, fingerprint);


        for (int x = 0; x < b; x++) {
            if (getFingerprintFromIndex(i, x) == 0){
                setFingerprint(i, x, fingerprint);
                return;
            } else if (getFingerprintFromIndex(j, x) == 0){
                setFingerprint(j, x, fingerprint);
                return;
            }
        }


        int t = (random.nextInt(2) == 0) ? i : j;
        int count = 0;

        for (int y=0; y<m; y++) {
            int e = random.nextInt(b);
            int fingerprintE = getFingerprintFromIndex(t, e);

            setFingerprint(t, e, fingerprint);

            fingerprint = fingerprintE;


            t = hashH2(t, fingerprint);

            for (int x = 0; x < b; x++) {
                if (getFingerprintFromIndex(t, x) == 0) {
                    setFingerprint(t, x, fingerprint);
                    return;
                }
            }
        }
    }

    @Override
    public Boolean query(byte[] key){
        // The query step is straighforward, and simply checks if the fingerprint of the key is found,
        // in either of the buckets hashed to by h1 or h2.
        int fingerprint = getFingerprint(key);
        int i = hashH1(key);
        int j = hashH2(i, fingerprint);



        for (int x = 0; x < b; x++) {
            if (getFingerprintFromIndex(i, x) == fingerprint || getFingerprintFromIndex(j, x) == fingerprint) {
                return true;
            }
        }

        return false;

    }
    
    private int getFingerprintFromIndex(int bucketIndex, int slotIndex) {
        int bitIndex = (bucketIndex * b + slotIndex) * f;
        int longIndex = bitIndex / 64;
        int bitOffset = bitIndex % 64;

        if (bitOffset + f <= 64) {
            long val = table[longIndex] >>> bitOffset;
            return (int) (val & ((1L << f) - 1));
        } else {
            int firstPart = 64 - bitOffset;
            int secondPart = f - firstPart;

            long low = table[longIndex] >>> bitOffset;
            long high = table[longIndex + 1] & ((1L << secondPart) - 1);
            return (int) ((high << firstPart) | low);
        }
    }

    private void setFingerprint(int bucketIndex, int slotIndex, int fingerprint) {
        int bitIndex = (bucketIndex * b + slotIndex) * f;
        int longIndex = bitIndex / 64;
        int bitOffset = bitIndex % 64;
        long mask = (1L << f) - 1;

        if (bitOffset + f <= 64) {
            table[longIndex] = (table[longIndex] & ~(mask << bitOffset)) |
                    (((long) fingerprint & mask) << bitOffset);
        } else {
            int firstPart = 64 - bitOffset;
            int secondPart = f - firstPart;

            long mask1 = (1L << firstPart) - 1;
            long mask2 = (1L << secondPart) - 1;

            long part1 = fingerprint & mask1;
            long part2 = (fingerprint >>> firstPart) & mask2;

            table[longIndex] = (table[longIndex] & ~(mask1 << bitOffset)) |
                    (part1 << bitOffset);

            table[longIndex + 1] = (table[longIndex + 1] & ~mask2) | part2;
        }
    }

    public long calculateRAMBytes() {
        long totalBits = (long) d * b * f;
        long numLongs = (totalBits + 63) / 64;
        return numLongs * 8;
    }

}