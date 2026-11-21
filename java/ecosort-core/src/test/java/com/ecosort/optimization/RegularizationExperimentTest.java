package com.ecosort.optimization;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class RegularizationExperimentTest {

    @Test
    void minMaxNormalizationShouldStartAtZeroAndEndAtOne() {

        double[] result =
                RegularizationExperiment.minMaxNormalize(
                        new double[]{
                                10.0,
                                20.0,
                                30.0,
                                40.0
                        }
                );

        assertEquals(
                0.0,
                result[0],
                0.0001
        );

        assertEquals(
                1.0,
                result[3],
                0.0001
        );
    }


    @Test
    void standardizationShouldProduceApproximatelyZeroMean() {

        double[] result =
                RegularizationExperiment.standardize(
                        new double[]{
                                10.0,
                                20.0,
                                30.0,
                                40.0
                        }
                );

        double sum = 0.0;

        for (double value : result) {
            sum += value;
        }

        double mean =
                sum / result.length;

        assertEquals(
                0.0,
                mean,
                0.0001
        );
    }


    @Test
    void l1PenaltyShouldBePositive() {

        double penalty =
                RegularizationExperiment.l1Penalty(
                        new double[]{
                                1.0,
                                -2.0,
                                3.0
                        },
                        0.01
                );

        assertTrue(
                penalty > 0.0
        );

        assertEquals(
                0.06,
                penalty,
                0.0001
        );
    }


    @Test
    void l2PenaltyShouldBePositive() {

        double penalty =
                RegularizationExperiment.l2Penalty(
                        new double[]{
                                1.0,
                                -2.0,
                                3.0
                        },
                        0.01
                );

        assertTrue(
                penalty > 0.0
        );

        assertEquals(
                0.14,
                penalty,
                0.0001
        );
    }


    @Test
    void largeGeneralizationGapShouldFlagOverfitting() {

        String result =
                RegularizationExperiment.assessGeneralization(
                        0.05,
                        0.40
                );

        assertEquals(
                "LIKELY_OVERFITTING",
                result
        );
    }


    @Test
    void smallGeneralizationGapShouldBeGood() {

        String result =
                RegularizationExperiment.assessGeneralization(
                        0.18,
                        0.20
                );

        assertEquals(
                "GOOD_GENERALIZATION",
                result
        );
    }


    @Test
    void invalidDropoutRateShouldBeRejected() {

        assertThrows(
                IllegalArgumentException.class,
                () ->
                        RegularizationExperiment.applyDropout(
                                new double[]{
                                        0.9,
                                        0.8,
                                        0.7
                                },
                                1.0
                        )
        );
    }
}