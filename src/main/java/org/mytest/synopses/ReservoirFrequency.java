package org.mytest.synopses;

import org.mytest.structures.FrequencySynopsis;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Objects;

/**
 * Reservoir is the class to run reservoir sampling based synopses
 * with reservoir size n and set size N.
 */
public class ReservoirFrequency implements FrequencySynopsis {
    private final ByteBuffer[] R;
    private final int n; // resevoir size
    private int t; // record counter

    public ReservoirFrequency(int size) {
        this.n = size;
        this.R = new ByteBuffer[this.n];
        this.t = 0;
    }

    /**
     * Insert an Object into the reservoir. If the reservoir is not full, insert at the next available position.
     * If the reservoir is full, randomly decide whether to replace an existing item with the new item.
     * @param value - Object to insert into reservoir
     */
    @Override
    public void insert(byte[] value){
        if (this.t < this.n){
            this.R[t] = ByteBuffer.wrap(value.clone());
        }
        else {
            double chance = ((double)this.n/(double)(this.t)); // chance of item t being in a sample of size n of the first t items.
            int position = (int) (Math.random()*(this.n)); // randomly choose reservoir position to replace.
            if (Math.random() < chance){ // randomly decide with probability 'chance' to include item t in the sample.
                this.R[position] = ByteBuffer.wrap(value.clone());
            }
        }
        this.t += 1;
    }

    @Override
    public Integer query(byte[] value){ // let the exact query be defined in the main/at call and let query access the data in R?
        int count = 0;
        ByteBuffer target = ByteBuffer.wrap(value.clone());
        for (int i = 0; i < this.n; i++){
            if (target.equals(R[i])){
                count++;
            }
        }
        return count * (this.t/this.n);
    }
}


