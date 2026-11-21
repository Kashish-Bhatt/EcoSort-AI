package com.ecosort.learning;

import java.util.Arrays;

public class LearningRules {

    // =====================================================
    // 1. HEBBIAN LEARNING
    // Δw = learningRate * x * y
    // =====================================================

    public static double[] hebbianLearning(
            double[][] inputs,
            double[] outputs,
            double learningRate) {

        double[] weights =
                new double[inputs[0].length];

        for (int sample = 0;
             sample < inputs.length;
             sample++) {

            for (int i = 0;
                 i < weights.length;
                 i++) {

                weights[i] +=
                        learningRate
                        * inputs[sample][i]
                        * outputs[sample];
            }
        }

        return weights;
    }


    // =====================================================
    // 2. MODIFIED HEBBIAN / OJA'S RULE
    //
    // Δw = η * y * (x - y*w)
    //
    // Prevents uncontrolled weight growth.
    // =====================================================

    public static double[] ojaLearning(
            double[][] inputs,
            double learningRate,
            int epochs) {

        double[] weights = {
                0.2,
                0.1
        };

        for (int epoch = 0;
             epoch < epochs;
             epoch++) {

            for (double[] input : inputs) {

                double output =
                        dot(input, weights);

                for (int i = 0;
                     i < weights.length;
                     i++) {

                    weights[i] +=
                            learningRate
                            * output
                            * (
                                input[i]
                                - output * weights[i]
                            );
                }
            }
        }

        return weights;
    }


    // =====================================================
    // 3. COMPETITIVE LEARNING
    //
    // Winner neuron moves toward input.
    // =====================================================

    public static double[][] competitiveLearning(
            double[][] inputs,
            double learningRate,
            int epochs) {

        double[][] prototypes = {

                {0.1, 0.1},

                {0.9, 0.9}
        };


        for (int epoch = 0;
             epoch < epochs;
             epoch++) {

            for (double[] input : inputs) {

                int winner =
                        findWinner(
                                input,
                                prototypes
                        );


                for (int i = 0;
                     i < input.length;
                     i++) {

                    prototypes[winner][i] +=
                            learningRate
                            * (
                                input[i]
                                - prototypes[winner][i]
                            );
                }
            }
        }

        return prototypes;
    }


    // =====================================================
    // 4. ERROR-CORRECTION LEARNING
    //
    // w = w + η(target - prediction)x
    // =====================================================

    public static double[] errorCorrection(
            double[][] inputs,
            double[] targets,
            double learningRate,
            int epochs) {

        double[] weights =
                new double[inputs[0].length];

        for (int epoch = 0;
             epoch < epochs;
             epoch++) {

            for (int sample = 0;
                 sample < inputs.length;
                 sample++) {

                double activation =
                        dot(
                                inputs[sample],
                                weights
                        );

                double prediction =
                        activation >= 0.5
                                ? 1.0
                                : 0.0;

                double error =
                        targets[sample]
                        - prediction;


                for (int i = 0;
                     i < weights.length;
                     i++) {

                    weights[i] +=
                            learningRate
                            * error
                            * inputs[sample][i];
                }
            }
        }

        return weights;
    }


    // =====================================================
    // HELPERS
    // =====================================================

    private static double dot(
            double[] a,
            double[] b) {

        double result = 0.0;

        for (int i = 0;
             i < a.length;
             i++) {

            result +=
                    a[i] * b[i];
        }

        return result;
    }


    private static int findWinner(
            double[] input,
            double[][] prototypes) {

        int winner = 0;

        double bestDistance =
                Double.MAX_VALUE;


        for (int neuron = 0;
             neuron < prototypes.length;
             neuron++) {

            double distance = 0.0;


            for (int i = 0;
                 i < input.length;
                 i++) {

                double difference =
                        input[i]
                        - prototypes[neuron][i];

                distance +=
                        difference * difference;
            }


            if (distance < bestDistance) {

                bestDistance = distance;

                winner = neuron;
            }
        }

        return winner;
    }


    // =====================================================
    // DEMONSTRATION
    // =====================================================

    public static void main(String[] args) {

        double[][] inputs = {

                {0, 0},
                {0, 1},
                {1, 0},
                {1, 1}
        };


        double[] targets = {

                0,
                0,
                0,
                1
        };


        System.out.println(
                "\n===== ECOSORT LEARNING RULES ====="
        );


        // -------------------------------------------------
        // Hebbian
        // -------------------------------------------------

        double[] hebbian =
                hebbianLearning(
                        inputs,
                        targets,
                        0.1
                );


        System.out.println(
                "\n1. Hebbian Learning"
        );

        System.out.println(
                "Weights: "
                + Arrays.toString(
                        hebbian
                )
        );


        // -------------------------------------------------
        // Modified Hebbian / Oja
        // -------------------------------------------------

        double[] oja =
                ojaLearning(
                        inputs,
                        0.01,
                        100
                );


        System.out.println(
                "\n2. Modified Hebbian (Oja)"
        );

        System.out.println(
                "Weights: "
                + Arrays.toString(
                        oja
                )
        );


        // -------------------------------------------------
        // Competitive
        // -------------------------------------------------

        double[][] prototypes =
                competitiveLearning(
                        inputs,
                        0.1,
                        100
                );


        System.out.println(
                "\n3. Competitive Learning"
        );


        for (int i = 0;
             i < prototypes.length;
             i++) {

            System.out.println(
                    "Prototype "
                    + i
                    + ": "
                    + Arrays.toString(
                            prototypes[i]
                    )
            );
        }


        // -------------------------------------------------
        // Error Correction
        // -------------------------------------------------

        double[] errorWeights =
                errorCorrection(
                        inputs,
                        targets,
                        0.1,
                        100
                );


        System.out.println(
                "\n4. Error-Correction Learning"
        );

        System.out.println(
                "Weights: "
                + Arrays.toString(
                        errorWeights
                )
        );


        System.out.println(
                "\n===== LEARNING RULE DEMO COMPLETE ====="
        );
    }
}