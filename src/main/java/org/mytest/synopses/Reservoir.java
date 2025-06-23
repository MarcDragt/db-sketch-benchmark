package org.mytest.synopses;
import org.mytest.structures.Synopsis;
import java.lang.Math;

/**
 * Reservoir is the class to run reservoir sampling based synopses
 * with reservoir size n and set size N.
 */
public class Reservoir {
    private final Object[] R;
    private final int n; // resevoir size
    private int t; // record counter

    public Reservoir(int size) {
        this.n = size;
        this.R = new Object[this.n];
        this.t = 0;
    }

    public int getN() {
        return n;
    }

    public int getT() {
        return t;
    }

    /**
     * Insert an Object into the reservoir. If the reservoir is not full, insert at the next available position.
     * If the reservoir is full, randomly decide whether to replace an existing item with the new item.
     * @param value - Object to insert into reservoir
     */
    public void insert(Object value){
        if (this.t < this.n){
            this.R[t] = value;
        }
        else {
            double chance = ((double)this.n/(double)(this.t)); // chance of item t being in a sample of size n of the first t items.
            int position = (int) (Math.random()*(this.n)); // randomly choose reservoir position to replace.
            if (Math.random() < chance){ // randomly decide with probability 'chance' to include item t in the sample.
                this.R[position] = value;
            }
        }
        this.t += 1;
    }

    // basically getter for R
    public Object[] query(byte[] value){ // let the exact query be defined in the main/at call and let query access the data in R?
        //for testing return entire reservoir
        return R;
    }
}


