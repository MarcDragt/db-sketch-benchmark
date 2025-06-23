package org.mytest;
import org.mytest.synopses.CM;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DataTest {

    public static CM sketchInit(){
        float delta = 0.01f;
        float epsilon = 0.0001f; // practically accepted %N error

        // following p.212 of "Synopses for Massive Data: Samples, Histograms, Wavelets, Sketches"
        // to get a maximum epsN error with probability 1-delta
        int depth = (int) Math.ceil(Math.log(1/delta));
        int width = (int) Math.ceil(2/epsilon);

        System.out.println("Depth: " + depth + " Width: " + width);

        return new CM(depth, width);
    }

    public static CM trueCountInit(int width){
        int depth = 1;
        System.out.println("Depth: " + depth + " Width: " + width);
        return new CM(depth, width);
    }

    public static Set<String> listFilesUsingJavaIO(String dir) {
        return Stream.of(new File(dir).listFiles())
                .filter(file -> !file.isDirectory())
                .map(File::getName)
                .collect(Collectors.toSet());
    }

    public static void knmiCMInsert(String pathname, CM sketch, Map<String, Integer> reference, Set<String> setKeys){
        try {
            File file = new File(pathname);
            Scanner sc = new Scanner(file);

            // get rid of the header
            for (int i=0; i<15; i++) {
                sc.nextLine();
            }

            // as a test run through all lines and retrieve data of interest.
            int numberOfIncompleteSections = 0;
            while (sc.hasNextLine()) {
                String line = sc.nextLine();
                if (line.isEmpty()) break;
                String[] info = line.split("  +"); //the data seems to be split by at least 2 white spaces
                try {
//                    String datetime = info[0];
                    String place = info[2];
                    String radAvg = info[6];
//                    String radMin = info[7];
//                    String radMax = info[8];
//                    String date = datetime.substring(0, datetime.indexOf(" "));
//                    String time = datetime.substring(datetime.indexOf(" ") + 1);

                    // convert place and radAvg to byte array, and add to sketches and keylist
                    String dataString = place + radAvg;
                    byte[] dataBytes = dataString.getBytes();
                    sketch.insert(dataBytes);
                    reference.put(dataString, reference.getOrDefault(dataString, 0) + 1);
                    setKeys.add(dataString);
                }
                catch (ArrayIndexOutOfBoundsException e) {
                    numberOfIncompleteSections += 1;
                }
            }
            System.out.println("Successfully run with: " + numberOfIncompleteSections +  " skipped lines");
        }
        catch (FileNotFoundException e) {
            System.out.println("File not found");
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        Set<String> setKeys = new HashSet<>();
        Map<String, Integer> reference = new HashMap<>();
        CM sketch = sketchInit();

        Set<String> files = listFilesUsingJavaIO("src/main/java/org/mytest/data/knmiRad/");
        for (String file : files) {
            String pathname = "src/main/java/org/mytest/data/knmiRad/" + file;
            knmiCMInsert(pathname, sketch, reference, setKeys);
        }

        int i = 0;
        for (String keyString : setKeys) {
            byte[] key = keyString.getBytes();
            System.out.println("True amount: " + reference.get(keyString));
            System.out.println("sketch amount: " + sketch.query(key));
            System.out.println(new String(key) + "\n");
            i++;
        }
        System.out.println(i);

    }
}
