package org.mytest.structures;

public interface Synopsis<T> {

    // insert value into synopsis
    public void insert(byte[] value);

    // return query answer to args
    public T query(byte[] value); // return what? just generic

}
