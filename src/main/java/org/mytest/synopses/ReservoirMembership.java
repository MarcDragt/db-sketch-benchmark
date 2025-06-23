package org.mytest.synopses;

import org.mytest.structures.MembershipSynopsis;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Objects;

/**
 * Reservoir is the class to run reservoir sampling based synopses
 * with reservoir size n and set size N.
 */
public class ReservoirMembership implements MembershipSynopsis {
    private final String[] R;
    private final int n; // resevoir size
    private int t; // record counter

    public ReservoirMembership(int size) {
        this.n = size;
        this.R = new String[this.n];
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
    @Override
    public void insert(byte[] value){
        if (this.t < this.n){
            this.R[t] = Arrays.toString(value);
        }
        else {
            double chance = ((double)this.n/(double)(this.t)); // chance of item t being in a sample of size n of the first t items.
            int position = (int) (Math.random()*(this.n)); // randomly choose reservoir position to replace.
            if (Math.random() < chance){ // randomly decide with probability 'chance' to include item t in the sample.
                this.R[position] = Arrays.toString(value);
            }
        }
        this.t += 1;
    }

    // basically getter for R
    @Override
    public Boolean query(byte[] value){ // let the exact query be defined in the main/at call and let query access the data in R?
        //for testing return entire reservoir
        String target = Arrays.toString(value);
        for (int i = 0; i < this.n; i++){
            if (Objects.equals(this.R[i], target)){
                return true;
            }
        }
        return false;
    }
}


