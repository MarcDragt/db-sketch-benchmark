package org.mytest;

import org.mytest.structures.*;
import org.mytest.synopses.*;

import java.net.UnknownHostException;

public class Main{
    public static void main(String[] args) {
        //general settings
        int datasizeCount = 10_000_000;
        int querySize = 1_00_000;
        String dataSet = "Uniform";
        Boolean synthetic = true;
        int numberOfDistincts = 10;

        Object[][] cmParams = {
                {5, 50},
                {5, 250},
                {5, 500},
                {5, 2500},
                {5, 5000},
                {5, 25_000},
                {5, 50_000},
                {5, 250_000},
                {5, 500_000},
                {5, 2_500_000},
                {5, 5_000_000},
                {5, 25_000_000},
                {5, 50_000_000},
                {5, 250_000_000},
        };

        SketchFactory<FrequencySynopsis> cmFactory = (arguments) -> new CM((int) arguments[0], (int) arguments[1]);
        Benchmark.runFrequencyBenchmark(datasizeCount, querySize, dataSet, synthetic, numberOfDistincts, cmParams, cmFactory);

        Object[][] reservoirFreqParameters = {
                {250},
                {1250},
                {2500},
                {12500},
                {25_000},
        };
        SketchFactory<FrequencySynopsis> reservoirFreqFactory = (arguments) -> new ReservoirFrequency((int) arguments[0]);
        //Benchmark.runFrequencyBenchmark(datasizeCount, querySize, dataSet, synthetic, numberOfDistincts, reservoirFreqParameters, reservoirFreqFactory);

        Object[][] bloomParameters = {
                {8000, 3},
                {40_000, 3},
                {80_000, 3},
                {400_000, 3},
                {800_000, 3},
                {4_000_000, 3},
                {8_000_000, 3},
                {40_000_000, 3},
                {80_000_000, 3},
                {400_000_000, 3},
                {800_000_000, 3},
                {2_147_483_647, 3},
        };
        SketchFactory<MembershipSynopsis> bloomFactory = (arguments) -> new Bloom((int) arguments[0], (int) arguments[1]);
        Benchmark.runMembershipBenchmark(datasizeCount, querySize, dataSet, synthetic, numberOfDistincts, bloomParameters, bloomFactory);

        Object[][] reservoirMemParameters = {
                {250},
                {1250},
                {2500},
                {12_500},
                {25_000}
        };
        SketchFactory<MembershipSynopsis> reservoirMemFactory = (arguments) -> new ReservoirMembership((int) arguments[0]);
        //Benchmark.runMembershipBenchmark(datasizeCount, dataSet, synthetic, numberOfDistincts, reservoirMemParameters, reservoirMemFactory);


    }
}
