package org.mytest;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class DataBytes {
    private final byte[][] inserts;
    private final byte[][] queries;
    private final byte[][] insertQueries;

    public DataBytes(byte[][] inserts, byte[][] queries, byte[][] insertQueries) {
        this.inserts = inserts;
        this.queries = queries;
        this.insertQueries = insertQueries;
    }


    public byte[][] getInserts() { return inserts; }

    public byte[][] getQueries() { return queries; }

    public byte[][] getInsertQueries() { return insertQueries; }

    public static byte[][] getBytes(int[] value) {
        byte[][] result = new byte[value.length][];
        for (int i = 0; i < value.length; i++) {
            result[i] = toBytes(value[i]);
        }
        return result;
    }
    public static byte[][] getBytes(String[] value) {
        byte[][] result = new byte[value.length][];
        for (int i = 0; i < value.length; i++) {
            result[i] = toBytes(value[i]);
        }
        return result;
    }
    public static byte[] toBytes(int value) {
        return ByteBuffer.allocate(4).putInt(value).array();
    }
    public static byte[] toBytes(String value){
        return value.getBytes(StandardCharsets.UTF_8);
    }
    public static String toString(byte[] value) {
        return  new String(value, StandardCharsets.ISO_8859_1);
    }
}
