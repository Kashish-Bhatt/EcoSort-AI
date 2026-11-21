package com.ecosort.learning;

public class ErrorCorrectionLearningDemo {

    public static void main(String[] args) {

        double[] inputs = {
                0.2,
                0.4,
                0.6,
                0.8
        };

        double[] targets = {
                0,
                0,
                1,
                1
        };

        double weight = 0.1;
        double bias = 0.0;

        double learningRate = 0.2;

        for (int epoch = 0;
             epoch < 100;
             epoch++) {

            for (int i = 0;
                 i < inputs.length;
                 i++) {

                double prediction =
                        weight * inputs[i]
                                + bias;

                double error =
                        targets[i]
                                - prediction;

                weight +=
                        learningRate
                                * error
                                * inputs[i];

                bias +=
                        learningRate
                                * error;
            }
        }

        System.out.println(
                "\n===== ERROR-CORRECTION LEARNING ====="
        );

        System.out.printf(
                "Final weight: %.6f%n",
                weight
        );

        System.out.printf(
                "Final bias: %.6f%n",
                bias
        );

        for (double input : inputs) {

            double output =
                    weight * input
                            + bias;

            System.out.printf(
                    "Input %.2f -> %.4f%n",
                    input,
                    output
            );
        }

        System.out.println(
                "\nWeights change according to "
                        + "target - prediction error."
        );

        System.out.println(
                "======================================="
        );
    }
}