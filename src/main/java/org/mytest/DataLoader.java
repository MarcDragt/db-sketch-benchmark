package org.mytest;

import java.io.File;
import java.util.HashSet;
import java.util.Objects;
import java.util.Scanner;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This class is supposed to take in some external files, parse them and return them in the ByteArrays format so they can
 * be run in the Analyze class
 */

public class DataLoader {
    private static Set<String> listFilesUsingJavaIO(String dir) {
        return Stream.of(Objects.requireNonNull(new File(dir).listFiles()))
                .filter(file -> !file.isDirectory())
                .map(File::getName)
                .collect(Collectors.toSet());
    }

    public static DataBytes parseCAIDA(String dir, int dataSize, int querySize) {
        System.out.println("Loading data...");
        Set<String> tracker = new HashSet<>();
        DataReal data = new DataReal(new String[dataSize], new String[querySize], new String[querySize], 0, 0, tracker);

        Set<String> files = listFilesUsingJavaIO(dir);
        for (String file : files) {
            String pathname = dir + file;
            if (new File(pathname).exists() && (data.getInsertSize() < dataSize || data.getQuerySize() < querySize)) {
                data = loaderCAIDA(pathname, data, dataSize, querySize);
            }
        }
        if (data.getInsertSize() < dataSize || data.getQuerySize() < querySize){
            throw new IllegalArgumentException("Insufficient data: expected " + (dataSize + querySize) + " elements, but got " + (data.getInsertSize()+data.getQuerySize()));
        }
        DataBytes returnData = new DataBytes(DataBytes.getBytes(data.getInserts()), DataBytes.getBytes(data.getQueries()), DataBytes.getBytes(data.getInsertQueries()));
        return returnData;
    }

    private static DataReal loaderCAIDA(String pathname, DataReal data,  int dataSize, int querySize) {
        int dataSizeCounter = data.getInsertSize();
        int querySizeCounter = data.getQuerySize();
        String[] inserts = data.getInserts();
        String[] queries = data.getQueries();
        String[] insertQueries = data.getInsertQueries();
        Set<String> tracker = data.getTracker();

        try {
            File file = new File(pathname);
            Scanner sc = new Scanner(file);
            sc.nextLine();

            while (sc.hasNextLine()) {
                String line = sc.nextLine();
                line = line.replace("\"", "");
                String[] segments = line.split(",");
                try {
                    String variableOfInterest = segments[5].replace(".", "") + segments[6].replace(".", ""); //
                    if (querySizeCounter < querySize ) {
                        queries[querySizeCounter] = variableOfInterest;
                        tracker.add(variableOfInterest);
                        querySizeCounter++;
                    }
                    else if (dataSizeCounter < querySize && !tracker.contains(variableOfInterest)) {
                        inserts[dataSizeCounter] = variableOfInterest;
                        insertQueries[dataSizeCounter] = variableOfInterest;
                        dataSizeCounter++;
                    }
                    else if (dataSizeCounter < dataSize && !tracker.contains(variableOfInterest)) {
                        inserts[dataSizeCounter] = variableOfInterest;
                        dataSizeCounter++;
                    }
                    else if ((querySizeCounter < querySize || dataSizeCounter < dataSize) && tracker.contains(variableOfInterest)) {
                        continue;
                    }
                    else {
                        break;
                    }
                }
                catch (Exception e) {
                    continue;
                }
            }
        }
        catch (Exception e) {
            System.out.println("File not found!");
            e.printStackTrace();
        } return new DataReal(inserts, queries, insertQueries, dataSizeCounter, querySizeCounter, tracker);
    }
}
