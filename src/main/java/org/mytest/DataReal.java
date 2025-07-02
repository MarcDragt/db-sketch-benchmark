package org.mytest;

import java.nio.charset.StandardCharsets;
import java.util.Set;

public class DataReal {
    private final String[] inserts;
    private final String[] queries;
    private final String[] insertQueries;
    private final int dataSizeCounter;
    private final int querySizeCounter;
    private final Set<String> tracker;


    public DataReal(String[] inserts, String[] queries, String[] insertQueries, int dataSizeCounter, int querySizeCounter, Set<String> tracker) {
        this.inserts = inserts;
        this.queries = queries;
        this.insertQueries = insertQueries;
        this.dataSizeCounter = dataSizeCounter;
        this.querySizeCounter = querySizeCounter;
        this.tracker = tracker;
    }


    public String[] getInserts() {
        return inserts;
    }

    public String[] getQueries() {
        return queries;
    }

    public String[] getInsertQueries() {
        return insertQueries;
    }

    public int getInsertSize() {
        return dataSizeCounter;
    }

    public int getQuerySize() {
        return querySizeCounter;
    }

    public Set<String> getTracker() {
        return tracker;
    }

    public static byte[] toBytes(String value){
        return value.getBytes(StandardCharsets.UTF_8);
    }
}
