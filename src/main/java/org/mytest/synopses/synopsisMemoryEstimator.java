package org.mytest.synopses;

/**
 * ChatGPT inspired helper function to calculate estimated CM sketch memory size in java
 */
public class synopsisMemoryEstimator {

    /**
     * Estimates the memory usage (in bytes) of a CM (Count-Min Sketch) object.
     *
     * @param depth Number of hash functions (rows)
     * @param width Number of counters per hash (columns)
     * @return Estimated memory usage in bytes
     */
    private static long estimateCMSize(int depth, int width) {
        final int objectOverhead = 16;      // Base object overhead
        final int primitiveFields = 4 * 4;  // 4 ints: depth, width, seed1, seed2
        final int outerArrayOverhead = 8 * depth; // References to inner arrays

        // Each inner array has 4 * width bytes (for int[]), plus ~16 bytes array overhead
        long innerArraysTotal = depth * (4L * width + 16);

        // Total estimated size
        return objectOverhead + primitiveFields + outerArrayOverhead + innerArraysTotal;
    }

    // Optional: Convert bytes to human-readable MB
    private static String toMegabytes(long bytes) {
        return String.format("%.2f MB", bytes / (1024.0 * 1024.0));
    }

    // function that prints the result cause gpt didnt implement that

    public static void printMemoryUsageCM(int depth, int width) {
        long sizeBytes = estimateCMSize(depth, width);
        System.out.println("Estimated memory usage: " + sizeBytes + " bytes" + " ≈ " + toMegabytes(sizeBytes));
    }

    public static String returnMemoryUsageCM(int depth, int width) {
        long sizeBytes = estimateCMSize(depth, width);
        return Double.toString(sizeBytes / (1024.0 * 1024.0));
    }

    public static void printMemoryUsageBloom(int length) {
        int sizeBytes = (length/64)*8;
        System.out.println("Estimated memory usage: " + sizeBytes + " bytes" + " ≈ " + toMegabytes(sizeBytes));
    }

    public static String returnMemoryUsageBloom(int length) {
        int sizeBytes = (length/64)*8;
        return Double.toString(sizeBytes / (1024.0 * 1024.0));
    }

    /**
     * Insert an MB amount and depth for CM sketch to get maximum width for size.
     * @param MB The desired sketch size expressed in Megabytes
     * @param depth The depth of the sketch
     * @return sketch depth
     */
    public static int bindSizeCM(double MB, int depth) {
        long maxBytes = (long) (MB * 1024 * 1024);
        return (int) (maxBytes / (depth * 4)); // 4 bytes per counter
    }

    public static int[] bindSizeBatchCM(double[] listMB, int depth) {
        int[] lengthList = new int[listMB.length];
        int i = 0;
        for (double MB : listMB) {
            lengthList[i] = bindSizeCM(MB, depth);
            i++;
        }
        return lengthList;
    }

    /**
     * Insert an MB amount for Bloom filter to get maximum length of bloom filter
     * @param MB The desired sketch size expressed in Megabytes
     * @return sketch length
     */
    public static int bindSizeBloom(double MB){
        return (int) (MB * 1024 * 1024 * 8);
    }

    public static int[] bindSizeBatchBloom(double[] listMB) {
        int[] lengthList = new int[listMB.length];
        int i = 0;
        for (double MB : listMB) {
            lengthList[i] = bindSizeBloom(MB);
            i++;
        }
        return lengthList;
    }
}
