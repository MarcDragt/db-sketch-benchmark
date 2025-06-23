package org.mytest.synopses;

import com.google.common.hash.Hashing;
import org.mytest.structures.FrequencySynopsis;

import java.util.Arrays;
public class Count implements FrequencySynopsis {
    private int w; // Variable w represents the hash table width
    private int d; // Variable d represents the hash table depth
    private final int[][] table;
    public Count(int maxRAM, double delta) {
        // The constructor simply initiates the hash table
        this.d = (int) Math.ceil(Math.log(1.0 / delta) / Math.log(2));
        this.w = Math.max(1, (int) (maxRAM / (4L * d)));
        this.table = new int[d][w];
    }

    private int hashH(byte[] key, int i) {
        // murmur3 hash, modulus w to map to the right domain
        int hashCo = (Hashing.murmur3_128(i).hashBytes(key).asInt() % w + w)% w;
        return hashCo;
    }

    private int hashS(byte[] key, int i) {
        // murmur3 hash mapping to an int seeded by i ensures si(x) itself is pairwise independent.
        // a small ofset in the seed of +100 to ensure independence from corresponding hi(x)
        int hashV = Hashing.murmur3_128(i+100).hashBytes(key).asInt();
        // is hash is even mapped to 1, else to -1
        hashV = (hashV & 1) == 0 ? 1 : -1;
        return hashV;
    }

    @Override
    public void insert(byte[] key){
        for (int i=0; i<d; i++){
            int coord = hashH(key, i);
            table[i][coord] += hashS(key, i);
        }
    }

    @Override
    public Integer query(byte[] key){
        // This method can either implement QuickSelect to attempt finding the median in O(n),
        // or find the median of the array with sorting and selecting, dominated by O(n logn)
        int [] arr = new int[d];
        int res;

        for (int i = 0; i < d; i++) {
            int coord = hashH(key,i);
            int sign = hashS(key, i);
            int cij = sign*(table[i][coord]);
            arr[i]=cij;
        }
//        res = (int)Math.round(getMedianQuick(arr));
        res = (int)Math.round(getMedianSort(arr));
        return res;
    }

    public int getWidth() {
        return w;
    }

    public int getDepth() {
        return d;
    }

    public static double getMedianSort(int[] arr){
        int n = arr.length;
        double median;
        double leftMid;
        double rightMid;
        Arrays.sort(arr);

        if ((n & 1)==0){
            median = arr[n/2];
        }
        else{
            leftMid = arr[(n/2)-1];
            rightMid = arr[(n/2)+1];
            median = (leftMid+rightMid)/2;
        }

        return median;
    }

//    public static double getMedianQuick(int[] arr){
//        // This method obtains the median of an array through quickselect
//        int n = arr.length;
//        double median;
//        double leftMid;
//        double rightMid;
//        QuickSelect quickSelect = new QuickSelect();
//
//        if ((n & 1)==0){
//            median = (double)quickSelect.quickSelect(arr, (n/2));
//        }
//        else{
//            leftMid = (double)quickSelect.quickSelect(arr, (n/2)-1);
//            rightMid = (double)quickSelect.quickSelect(arr, (n/2)+1);
//            median = (leftMid+rightMid)/2;
//        }
//
//        return median;
//    }
}