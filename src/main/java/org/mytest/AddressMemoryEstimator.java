package org.mytest;

/**
 * ChatGPT generated helper function to calculate estimated memory size of the byte[][] in java
 */
public class AddressMemoryEstimator {
    /**
     * Estimates the memory size of a byte[n][4] structure for IPv4 addresses.
     * Each entry is a byte[4], stored as a separate object in Java.
     *
     * @param n Number of IPv4 addresses
     * @return Estimated memory in bytes
     */
    private static long estimateArraySize(int n) {
        final int outerArrayOverhead = 8 * n; // n references (64-bit JVM)
        final int innerArraySize = 4 + 16;    // 4 bytes for IPv4, 16 bytes overhead per byte[4]
        final int outerObjectOverhead = 16;   // Outer byte[n][4] array object

        return outerObjectOverhead + outerArrayOverhead + ((long) n * innerArraySize);
    }

    private static String toMegabytes(long bytes) {
        return String.format("%.2f MB", bytes / (1024.0 * 1024.0));
    }

    public static void printMemoryUsage(int n) {
        long sizeBytes = estimateArraySize(n);
        System.out.println("Estimated memory usage: " + sizeBytes + " bytes" + " ≈ " + toMegabytes(sizeBytes));
    }
}
