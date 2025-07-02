package org.mytest;
import org.mytest.structures.FrequencySynopsis;
import org.mytest.structures.MembershipSynopsis;
import org.mytest.structures.SketchFactory;
import org.mytest.structures.Synopsis;
import org.mytest.synopses.*;
import java.text.NumberFormat;
import java.util.*;
import org.openjdk.jol.info.GraphLayout;
import java.util.Map;

public class Benchmark {

    /**
     * Function to fetch the required synthetic dataset. If more datasets are added, they should be included here also.
     * @param dataset The name of the dataset.
     * @param dataSize The size of the dataset that will be inserted into a sketch.
     * @param querySize The number of queries that will be asked to the sketch.
     * @param seed The seed that will determine the data.
     * @param numberOfDistincts Depending on the type, the number of distinct items in the dataset.
     * @return The desired dataset.
     */
    private static DataBytes getData(String dataset, int dataSize, int querySize, int seed, int numberOfDistincts){
        DataBytes data = null;
        switch (dataset) {
            case "Uniform" -> data = DataGenerator.generateDisjointUniUniIPList(dataSize, querySize, 1);
            case "Uniform capped" -> data = DataGenerator.generateDisjointUniUniCapped(dataSize, querySize, 1, numberOfDistincts);
            case "Uniform hot" -> data = DataGenerator.generateDisjointUniZipfIPList(dataSize, querySize, 1);
            case "Zipf" -> data = DataGenerator.generateDisjointZipfUniIPList(dataSize, querySize, 1);
            case "Zipf hot" -> data = DataGenerator.generateDisjointZipfZipfIPList(dataSize, querySize, 1);
            case "CAIDA" -> data = DataLoader.parseCAIDA("src/main/resources/CAIDA/", dataSize, querySize);
        }
        try{
            assert data != null;
        } catch (Exception e){
            System.out.println("Not a valid dataset. Check name of the dataset.");
        }
        return data;
    }

    /**
     * Given an IP list and synopsis, measure the time taken to insert every item into the synopsis.
     * @param s
     * @param data
     * @param <T>
     */
    private static <T> String initialization(Synopsis<T> s, byte[][] data) {
        // start the timer and insert the data into the synopsis
        long startTime = System.nanoTime();
        for (byte[] entry : data) s.insert(entry);
        long endTime = System.nanoTime();

        // calculate and return the time taken
        long timeTaken = endTime - startTime;
        timeTaken = timeTaken / data.length; // convert to milliseconds
        return Long.toString(timeTaken);
    }

    /**
     * Given a list of IPs and a synopsis, measure the time to query every item in the list
     * @param s The synopsis to be tested
     * @param ipList Byte array of IPs to test membership or count
     * @param <T> Type of the synopsis
     */
    private static <T> String queryTime(Synopsis<T> s, byte[][] ipList) {
        // start the timer and query the data from the synopsis
        long startTime = System.nanoTime();
        for (byte[] entry : ipList) s.query(entry);
        long endTime = System.nanoTime();

        // calculate and return the time taken
        long timeTaken = endTime - startTime;
        timeTaken = timeTaken / ipList.length; // convert to milliseconds
        return Long.toString(timeTaken);
    }

    /**
     * Gives the truth values of a membership synopsis.
     * @param sketch
     * @param data
     * @param queries
     */
    private static String[] queryErrorMembership(MembershipSynopsis sketch, byte[][] data, byte[][] queries) {
        Set<String> reference = new HashSet<>();
        //store what queries are truly in data
        for (byte[] entry : data) reference.add(DataBytes.toString(entry));

        int FP = 0;
        int FN = 0;
        int TP = 0;
        int TN = 0;

        for (byte[] entry : queries) {
            boolean truth = reference.contains(DataBytes.toString(entry));
            boolean approx = sketch.query(entry);

            if (approx & !truth) FP++;
            if (!approx & truth) FN++;
            if (approx & truth) TP++;
            if (!approx & !truth) TN++;
        }

        return new String[] {String.valueOf(TP), String.valueOf(FP), String.valueOf(FN), String.valueOf(TN)};
    }

    /**
     * Gives the query error for a count synopsis
     * @param sketch
     * @param data
     * @param queries
     */
    private static String[] queryErrorCount(FrequencySynopsis sketch, byte[][] data, byte[][] queries) {
        Map<String, Integer> reference = new HashMap<>();

        //count the true values of the queries
        for (byte[] entry : data) {
            String bufferEntry = DataBytes.toString(entry);
            reference.put(bufferEntry, reference.getOrDefault(bufferEntry, 0) + 1);
        }
        // long[] errors = new long[queries.length]; // uncomment if interested in getting complete error list
        // can be used to plot a histogram for example
        double sumRelativeError = 0;
        long sumError = 0;
        // compare the CM sketch approximation with true count of reference
        //int i = 0; // uncomment if interested in getting complete error list
        for (byte[] entry : queries) {
            String bufferEntry = DataBytes.toString(entry);
            int truth = reference.getOrDefault(bufferEntry, 0);
            int approx = sketch.query(entry);

            // get the errors
            long error = Math.abs(approx - truth);
            double relativeError = (double)error/(double)truth;
            //errors[i] = error; // uncomment if interested in getting complete error list
            sumRelativeError += relativeError;
            sumError += error;
            //i++; // uncomment if interested in getting complete error list
        }
        double are = sumRelativeError/queries.length;
        if (Double.isNaN(are)||Double.isInfinite(are)) are = -1;
        return new String[] {String.valueOf(are), String.valueOf(sumError/queries.length)};
    }

    /**
     * Runs Bloom filter benchmark on a given filled percentage
     * @param sketch
     * @param filledPercentage
     * @param data
     */
    private static int[] initializationBloom(Bloom sketch, int filledPercentage, int[] data, int querySize) {
        // start the timer and insert the data into the synopsis
        long startTime = System.nanoTime();
        long filled = 0;
        long fillLimit = ((long)filledPercentage*(long)sketch.getLength())/100;
        System.out.println("Fill limit: " + fillLimit);

        // Due to nature of Bloom filter, if we are at fillLimit-1, 1 new entry may flip at most 'k' bits in B to 1,
        // and at least 0 new bits to 1.
        // this will result in [fillLimit, fillLimit + hashes] bound to number of bits set to 1.

        // keep track of filled% by counting upperbound with k bit flips and
        // resetting count to true filled% by calling cardinality of B when limit is reached

        // if filled% is set close to 100%, the filling process will take long,
        // the last bit(s) has(/have) a very low chance of being flipped

        int filledCount = 0;
        for (int entry : data) {
            if (filled < fillLimit) {
                sketch.insert(DataSynth.toBytes(entry));
                filledCount++;
                filled = filled + sketch.getHashes();
            } else {
                long cardinality = sketch.getB().cardinality();
                if (cardinality < fillLimit) {
                    sketch.insert(DataSynth.toBytes(entry));
                    filledCount++;
                    filled = cardinality;
                } else {
                    break;
                }
            }
        }
        long endTime = System.nanoTime();
        long timeTaken = endTime - startTime;
        timeTaken = timeTaken / 1_000_000; // convert to milliseconds
        System.out.println("Time taken for inserts: " + timeTaken + " ms");

        int length = Math.min(filledCount, querySize);
        int[] dataInserted = new int[length];
        System.arraycopy(data, 0, dataInserted, 0, length); //copy and let return only data inserted
        filled = sketch.getB().cardinality();
        System.out.print("Bloom filter filled by: " + filled + "/" + sketch.getLength() + " with " + (((float)filled/(float)sketch.getLength())*100) + "% of " + filledPercentage + "%");
        System.out.println(" and " + NumberFormat.getInstance().format(filledCount) + " objects inserted");
        return dataInserted;
    }

    private static String[] frequencyBenchmarkFramework(DataBytes data, FrequencySynopsis sketch) {

        String[] outputLine = new String[8];
        outputLine[0] = Long.toString(GraphLayout.parseInstance(sketch).totalSize());

        //perform tests
        outputLine[1] = initialization(sketch, data.getInserts());

        outputLine[2] = queryTime(sketch, data.getInsertQueries());
        outputLine[3] = queryTime(sketch, data.getQueries());

        //System.out.println("\nanalyzing errors");
        String[] value = queryErrorCount(sketch, data.getInserts(), data.getInsertQueries()); // 100% in the data
        outputLine[4] = value[0];
        outputLine[5] = value[1];

        value = queryErrorCount(sketch, data.getInserts(), data.getQueries()); // 0% in the data
        outputLine[6] = value[0];
        outputLine[7] = value[1];

        return outputLine;
    }

    private static String[] membershipBenchmarkFramework(DataBytes data, MembershipSynopsis sketch) {

        String[] outputLine = new String[12];
        outputLine[0] = Long.toString(GraphLayout.parseInstance(sketch).totalSize());
        outputLine[1] = initialization(sketch, data.getInserts());

        outputLine[2] = queryTime(sketch, data.getInsertQueries());
        outputLine[3] = queryTime(sketch, data.getQueries());

        String[] value = queryErrorMembership(sketch, data.getInserts(), data.getInsertQueries()); // 100% in the data

        outputLine[4] = value[0];
        outputLine[5] = value[1];
        outputLine[6] = value[2];
        outputLine[7] = value[3];

        value = queryErrorMembership(sketch, data.getInserts(), data.getQueries()); // 0% in the data

        outputLine[8] = value[0];
        outputLine[9] = value[1];
        outputLine[10] = value[2];
        outputLine[11] = value[3];

        return outputLine;
    }

    public static void runMembershipBenchmark(
            int dataSize,
            int querySize,
            String dataset,
            int numberOfDistincts,
            Object[][] parameters,
            SketchFactory<MembershipSynopsis> sketchFactory) {
        System.out.println("Starting Benchmark...");

        List<String[]> dataLines = new ArrayList<>();

        int seed = 0;

        long startTime = System.nanoTime();
        DataBytes data = getData(dataset, dataSize, querySize, seed, numberOfDistincts);
        long endTime = System.nanoTime();

        System.out.println("Done creating / loading " + dataset + " dataset of length " + NumberFormat.getInstance().format(dataSize));
        System.out.println("Time: " + (endTime - startTime) / 1_000_000 + " ms");
        System.out.println("MB, InsertTime, QueryTimeIn, QueryTimeOut, TPIn, FPIn, FNIn, TNIn, TPOut, FPOut, FNOut, TNOut");

        try {
            for(Object[] parameter: parameters) {
                    assert data != null;
                    MembershipSynopsis sketch = sketchFactory.create(parameter);
                    String[] outputLine = membershipBenchmarkFramework(data, sketch);
                    System.out.println(Arrays.toString(outputLine));
                    dataLines.add(outputLine);
            }
        }
        catch (Exception e){
            System.out.println("Dataset name incorrect");
            e.printStackTrace();
        }
    }

    public static void runFrequencyBenchmark(
            int dataSize,
            int querySize,
            String dataset,
            int numberOfDistincts,
            Object[][] parameters,
            SketchFactory<FrequencySynopsis> sketchFactory) {
        System.out.println("Starting Benchmark...");

        int seed = 0;

        long startTime = System.nanoTime();
        DataBytes data = getData(dataset, dataSize, querySize, seed, numberOfDistincts);
        long endTime = System.nanoTime();

        System.out.println("Done creating / loading " + dataset + " dataset of length " + NumberFormat.getInstance().format(dataSize));
        System.out.println("Time: " + (endTime - startTime) / 1_000_000 + " ms");
        System.out.println("MB, InsertTime, QueryTimeIn, QueryTimeOut, MREIn, MaeIn, MREOut, MaeOut");

        try {
            for(Object[] parameter: parameters) {
                assert data != null;
                FrequencySynopsis sketch = sketchFactory.create(parameter);
                String[] outputLine = frequencyBenchmarkFramework(data, sketch);
                System.out.println(Arrays.toString(outputLine));
            }
        }
        catch (Exception e){
            System.out.println("Dataset name incorrect");
            e.printStackTrace();
        }
    }
}

