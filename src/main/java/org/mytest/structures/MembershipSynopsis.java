package org.mytest.structures;

public interface MembershipSynopsis extends Synopsis<Boolean> {

    // insert value into synopsis as byte array
    @Override
    public void insert(byte[] value);

    // query the membership of a possibly inserted value
    @Override
    public Boolean query(byte[] value);

}

