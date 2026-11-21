package com.ecosort.optimization;

import java.util.Arrays;
import java.util.Random;

public class RegularizationExperiment {

    private static final Random RANDOM =
            new Random(42);


    // =========================================================
    // WEIGHT INITIALIZATION
    // =========================================================

    public static double[] zeroInitialization(
            int size) {

        return new double[size];
    }


    public static double[] randomInitialization(
            int size) {

        double[] weights =
                new double[size];

        for (int i = 0; i < size; i++) {

            weights[i] =
                    RANDOM.nextGaussian()
                            * 0.1;
        }

        return weights;
    }


    public static double[] xavierInitialization(
            int inputSize,
            int outputSize) {

        double limit =
                Math.sqrt(
                        6.0
                                / (
                                inputSize
                                        + outputSize
                        )
                );

        double[] weights =
                new double[inputSize];

        for (
                int i = 0;
                i < inputSize;
                i++
        ) {

            weights[i] =
                    (
                            RANDOM.nextDouble()
                                    * 2.0
                                    - 1.0
                    )
                            * limit;
        }

        return weights;
    }


    public static double[] heInitialization(
            int inputSize) {

        double standardDeviation =
                Math.sqrt(
                        2.0
                                / inputSize
                );

        double[] weights =
                new double[inputSize];

        for (
                int i = 0;
                i < inputSize;
                i++
        ) {

            weights[i] =
                    RANDOM.nextGaussian()
                            * standardDeviation;
        }

        return weights;
    }


    // =========================================================
    // STANDARDIZATION
    // =========================================================

    public static double[] standardize(
            double[] values) {

        if (
                values == null
                        || values.length == 0
        ) {

            throw new IllegalArgumentException(
                    "Values cannot be empty."
            );
        }


        double mean =
                mean(values);


        double variance =
                0.0;


        for (
                double value
                : values
        ) {

            variance +=
                    Math.pow(
                            value - mean,
                            2
                    );
        }


        variance /=
                values.length;


        double standardDeviation =
                Math.sqrt(
                        variance
                                + 1e-8
                );


        double[] normalized =
                new double[
                        values.length
                        ];


        for (
                int i = 0;
                i < values.length;
                i++
        ) {

            normalized[i] =
                    (
                            values[i]
                                    - mean
                    )
                            / standardDeviation;
        }


        return normalized;
    }


    // =========================================================
    // MIN-MAX NORMALIZATION
    // =========================================================

    public static double[] minMaxNormalize(
            double[] values) {

        if (
                values == null
                        || values.length == 0
        ) {

            throw new IllegalArgumentException(
                    "Values cannot be empty."
            );
        }


        double minimum =
                Arrays.stream(values)
                        .min()
                        .orElse(0.0);


        double maximum =
                Arrays.stream(values)
                        .max()
                        .orElse(1.0);


        double range =
                maximum
                        - minimum;


        double[] normalized =
                new double[
                        values.length
                        ];


        if (
                range == 0.0
        ) {

            return normalized;
        }


        for (
                int i = 0;
                i < values.length;
                i++
        ) {

            normalized[i] =
                    (
                            values[i]
                                    - minimum
                    )
                            / range;
        }


        return normalized;
    }


    // =========================================================
    // L1 REGULARIZATION
    // =========================================================

    public static double l1Penalty(
            double[] weights,
            double lambda) {

        validateWeights(
                weights
        );


        double penalty =
                0.0;


        for (
                double weight
                : weights
        ) {

            penalty +=
                    Math.abs(
                            weight
                    );
        }


        return lambda
                * penalty;
    }


    // =========================================================
    // L2 REGULARIZATION
    // =========================================================

    public static double l2Penalty(
            double[] weights,
            double lambda) {

        validateWeights(
                weights
        );


        double penalty =
                0.0;


        for (
                double weight
                : weights
        ) {

            penalty +=
                    weight
                            * weight;
        }


        return lambda
                * penalty;
    }


    // =========================================================
    // DROPOUT
    // =========================================================

    public static double[] applyDropout(
            double[] activations,
            double dropoutRate) {

        if (
                activations == null
                        || activations.length == 0
        ) {

            throw new IllegalArgumentException(
                    "Activations cannot be empty."
            );
        }


        if (
                dropoutRate < 0.0
                        || dropoutRate >= 1.0
        ) {

            throw new IllegalArgumentException(
                    "Dropout rate must be "
                            + "between 0 and 1."
            );
        }


        double[] result =
                new double[
                        activations.length
                        ];


        for (
                int i = 0;
                i < activations.length;
                i++
        ) {

            if (
                    RANDOM.nextDouble()
                            < dropoutRate
            ) {

                result[i] =
                        0.0;

            } else {

                /*
                 * Inverted dropout scaling.
                 */

                result[i] =
                        activations[i]
                                / (
                                1.0
                                        - dropoutRate
                        );
            }
        }


        return result;
    }


    // =========================================================
    // GENERALIZATION / OVERFITTING ANALYSIS
    // =========================================================

    public static String assessGeneralization(
            double trainingLoss,
            double validationLoss) {

        if (
                trainingLoss < 0.0
                        || validationLoss < 0.0
        ) {

            throw new IllegalArgumentException(
                    "Loss values cannot be negative."
            );
        }


        double gap =
                validationLoss
                        - trainingLoss;


        if (
                gap > 0.20
        ) {

            return "LIKELY_OVERFITTING";
        }


        if (
                gap > 0.08
        ) {

            return "GENERALIZATION_GAP";
        }


        return "GOOD_GENERALIZATION";
    }


    // =========================================================
    // MEAN
    // =========================================================

    private static double mean(
            double[] values) {

        double total =
                0.0;


        for (
                double value
                : values
        ) {

            total +=
                    value;
        }


        return total
                / values.length;
    }


    // =========================================================
    // VALIDATION
    // =========================================================

    private static void validateWeights(
            double[] weights) {

        if (
                weights == null
                        || weights.length == 0
        ) {

            throw new IllegalArgumentException(
                    "Weights cannot be empty."
            );
        }
    }


    // =========================================================
    // DEMONSTRATION
    // =========================================================

    public static void main(
            String[] args) {

        System.out.println(
                "\n============================================"
        );

        System.out.println(
                "ECOSORT TRAINING TECHNIQUES EXPERIMENT"
        );

        System.out.println(
                "============================================"
        );


        // =====================================================
        // 1. INITIALIZATION
        // =====================================================

        System.out.println(
                "\n1. WEIGHT INITIALIZATION"
        );


        System.out.println(
                "\nZero initialization:"
        );

        System.out.println(
                Arrays.toString(
                        zeroInitialization(
                                5
                        )
                )
        );


        System.out.println(
                "\nRandom initialization:"
        );

        System.out.println(
                Arrays.toString(
                        randomInitialization(
                                5
                        )
                )
        );


        System.out.println(
                "\nXavier initialization:"
        );

        System.out.println(
                Arrays.toString(
                        xavierInitialization(
                                5,
                                4
                        )
                )
        );


        System.out.println(
                "\nHe initialization:"
        );

        System.out.println(
                Arrays.toString(
                        heInitialization(
                                5
                        )
                )
        );


        // =====================================================
        // 2. NORMALIZATION
        // =====================================================

        double[] wasteFeatures = {

                10,
                20,
                40,
                70,
                100
        };


        System.out.println(
                "\n--------------------------------------------"
        );

        System.out.println(
                "2. NORMALIZATION"
        );


        System.out.println(
                "\nOriginal values:"
        );

        System.out.println(
                Arrays.toString(
                        wasteFeatures
                )
        );


        System.out.println(
                "\nStandardization:"
        );

        System.out.println(
                Arrays.toString(
                        standardize(
                                wasteFeatures
                        )
                )
        );


        System.out.println(
                "\nMin-Max normalization:"
        );

        System.out.println(
                Arrays.toString(
                        minMaxNormalize(
                                wasteFeatures
                        )
                )
        );


        // =====================================================
        // 3. REGULARIZATION
        // =====================================================

        double[] weights = {

                0.8,
                -0.5,
                0.3,
                1.1
        };


        System.out.println(
                "\n--------------------------------------------"
        );

        System.out.println(
                "3. REGULARIZATION"
        );


        System.out.printf(
                "%nL1 penalty: %.6f%n",
                l1Penalty(
                        weights,
                        0.01
                )
        );


        System.out.printf(
                "L2 penalty: %.6f%n",
                l2Penalty(
                        weights,
                        0.01
                )
        );


        // =====================================================
        // 4. DROPOUT
        // =====================================================

        double[] activations = {

                0.9,
                0.8,
                0.7,
                0.6,
                0.5,
                0.4
        };


        System.out.println(
                "\n--------------------------------------------"
        );

        System.out.println(
                "4. DROPOUT"
        );


        System.out.println(
                "\nBefore dropout:"
        );

        System.out.println(
                Arrays.toString(
                        activations
                )
        );


        System.out.println(
                "\nAfter 30% dropout:"
        );

        System.out.println(
                Arrays.toString(
                        applyDropout(
                                activations,
                                0.30
                        )
                )
        );


        // =====================================================
        // 5. GENERALIZATION
        // =====================================================

        System.out.println(
                "\n--------------------------------------------"
        );

        System.out.println(
                "5. OVERFITTING AND GENERALIZATION"
        );


        double trainingLossA =
                0.18;

        double validationLossA =
                0.20;


        System.out.println(
                "\nExperiment A"
        );

        System.out.printf(
                "Training loss   : %.2f%n",
                trainingLossA
        );

        System.out.printf(
                "Validation loss : %.2f%n",
                validationLossA
        );

        System.out.println(
                "Assessment      : "
                        + assessGeneralization(
                        trainingLossA,
                        validationLossA
                )
        );


        double trainingLossB =
                0.08;

        double validationLossB =
                0.35;


        System.out.println(
                "\nExperiment B"
        );

        System.out.printf(
                "Training loss   : %.2f%n",
                trainingLossB
        );

        System.out.printf(
                "Validation loss : %.2f%n",
                validationLossB
        );

        System.out.println(
                "Assessment      : "
                        + assessGeneralization(
                        trainingLossB,
                        validationLossB
                )
        );


        // =====================================================
        // 6. AUGMENTATION
        // =====================================================

        System.out.println(
                "\n--------------------------------------------"
        );

        System.out.println(
                "6. DATA AUGMENTATION"
        );


        System.out.println(
                "EcoSort Experiment 3 evaluates "
                        + "image augmentation including:"
        );


        System.out.println(
                "- HSV colour augmentation"
        );

        System.out.println(
                "- Rotation"
        );

        System.out.println(
                "- Translation"
        );

        System.out.println(
                "- Scaling"
        );

        System.out.println(
                "- Shearing"
        );

        System.out.println(
                "- Horizontal flipping"
        );

        System.out.println(
                "- Mosaic augmentation"
        );

        System.out.println(
                "- MixUp augmentation"
        );


        System.out.println(
                "\nAugmentation strategies were evaluated as part of the "
        + "controlled training and generalization workflow."
                        + "Experiment 2 using validation "
                        + "and test metrics."
        );


        System.out.println(
                "\n============================================"
        );

        System.out.println(
                "TRAINING TECHNIQUES DEMONSTRATION COMPLETE"
        );

        System.out.println(
                "============================================"
        );
    }
}