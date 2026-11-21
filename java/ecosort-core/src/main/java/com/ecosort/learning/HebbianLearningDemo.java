package com.ecosort.learning;

import java.util.Arrays;

public class HebbianLearningDemo {

    public static double[] hebbian(
            double[][] inputs,
            double[] outputs,
            double learningRate) {

        double[] weights =
                new double[inputs[0].length];

        for (int sample = 0;
             sample < inputs.length;
             sample++) {

            for (int feature = 0;
                 feature < weights.length;
                 feature++) {

                weights[feature] +=
                        learningRate
                                * inputs[sample][feature]
                                * outputs[sample];
            }
        }

        return weights;
    }

    public static double[] modifiedHebbian(
            double[][] inputs,
            double[] outputs,
            double learningRate) {

        double[] weights =
                new double[inputs[0].length];

        for (int sample = 0;
             sample < inputs.length;
             sample++) {

            for (int feature = 0;
                 feature < weights.length;
                 feature++) {

                double correlation =
                        inputs[sample][feature]
                                * outputs[sample];

                // Weight-decay term stabilizes growth.
                weights[feature] +=
                        learningRate
                                * (
                                correlation
                                        - outputs[sample]
                                        * outputs[sample]
                                        * weights[feature]
                        );
            }
        }

        return weights;
    }

    public static void main(String[] args) {

        double[][] wasteFeatures = {
                {1, 0, 0, 0},
                {1, 0.2, 0, 0},
                {0, 1, 0, 0},
                {0, 0, 1, 0},
                {0, 0, 0, 1}
        };

        double[] plasticAssociation = {
                1,
                1,
                -1,
                -1,
                -1
        };

        double[] hebbian =
                hebbian(
                        wasteFeatures,
                        plasticAssociation,
                        0.1
                );

        double[] modified =
                modifiedHebbian(
                        wasteFeatures,
                        plasticAssociation,
                        0.1
                );

        System.out.println(
                "\n===== ECOSORT HEBBIAN LEARNING ====="
        );

        System.out.println(
                "Hebbian weights:"
        );

        System.out.println(
                Arrays.toString(hebbian)
        );

        System.out.println(
                "\nModified Hebbian weights:"
        );

        System.out.println(
                Arrays.toString(modified)
        );

        System.out.println(
                "\nHebbian learning strengthens "
                        + "correlated input-output activity."
        );

        System.out.println(
                "Modified Hebbian learning adds "
                        + "a stabilizing weight term."
        );

        System.out.println(
                "======================================"
        );
    }
}
