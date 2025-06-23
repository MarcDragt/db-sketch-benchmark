package org.mytest.structures;

public interface FrequencySynopsis extends Synopsis<Integer>{
    @Override
    public void insert(byte[] value);

    @Override
    public Integer query(byte[] value);

}
