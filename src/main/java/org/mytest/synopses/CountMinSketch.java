package org.mytest.synopses;

import com.google.common.hash.Hashing;
import org.mytest.structures.FrequencySynopsis;

public class CountMinSketch implements FrequencySynopsis {
    private int w;
    private int d;
    private final int[][] table;
    public CountMinSketch(int maxRAM, double delta) {
//        this.w = width;
//        this.d = depth;
        this.d = (int) Math.ceil(Math.log(1.0 / delta));
        this.w = Math.max(1, (int) (maxRAM / (4L * d)));
        this.table = new int[d][w];

    }

    private int hash(byte[] key, int i) {
        int hash = (Hashing.murmur3_128(i).hashBytes(key).asInt() % w + w)% w;
        return hash;
    }

    @Override
    public void insert(byte[] key){
        for (int i=0; i<d; i++){
            int coord = hash(key, i);

            table[i][coord] += 1;
        }
    }

    @Override
    public Integer query(byte[] key){
        int min = Integer.MAX_VALUE;
        for (int i = 0; i < d; i++) {
            int coord = hash(key, i);
            min = Math.min(min, table[i][coord]);
        }

        return min;
    }

    public int getWidth() {
        return w;
    }

    public int getDepth() {
        return d;
    }
}
