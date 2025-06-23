package org.mytest;

import java.nio.ByteBuffer;

/** helper class to store the insert and query arrays for generated data.
 *
 */
public class DataSynth {
    private final int[] inserts;
    private final int[] queries;
    private final int[] insertQueries;

    public DataSynth(int[] inserts, int[] queries, int[] insertQueries) {
        this.inserts = inserts;
        this.queries = queries;
        this.insertQueries = insertQueries;
    }


    public int[] getInserts() { return inserts; }

    public int[] getQueries() { return queries; }

    public int[] getInsertQueries() { return insertQueries; }

    public static byte[] toBytes(int value) {
        return ByteBuffer.allocate(4).putInt(value).array();
    }
}
