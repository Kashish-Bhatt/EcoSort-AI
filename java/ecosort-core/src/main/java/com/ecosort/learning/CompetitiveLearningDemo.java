package com.ecosort.learning;

import java.util.Arrays;

public class CompetitiveLearningDemo {

    private final double[][] prototypes = {
            {0.8, 0.1, 0.05, 0.05},
            {0.1, 0.75, 0.1, 0.05},
            {0.1, 0.1, 0.7, 0.1},
            {0.05, 0.05, 0.1, 0.8}
    };

    private final double learningRate = 0.2;

    public int winner(double[] input) {

        int winner = 0;
        double bestDistance =
                Double.MAX_VALUE;

        for (int i = 0;
             i < prototypes.length;
             i++) {

            double distance = 0.0;

            for (int j = 0;
                 j < input.length;
                 j++) {

                double difference =
                        input[j]
                                - prototypes[i][j];

                distance +=
                        difference * difference;
            }

            if (distance < bestDistance) {

                bestDistance = distance;
                winner = i;
            }
        }

        return winner;
    }

    public void train(double[] input) {

        int winner =
                winner(input);

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

    public static void main(String[] args) {

        CompetitiveLearningDemo model =
                new CompetitiveLearningDemo();

        double[] observation = {
                0.72,
                0.15,
                0.05,
                0.08
        };

        int before =
                model.winner(observation);

        model.train(observation);

        int after =
                model.winner(observation);

        System.out.println(
                "\n===== COMPETITIVE LEARNING ====="
        );

        System.out.println(
                "Waste observation: "
                        + Arrays.toString(observation)
        );

        System.out.println(
                "Winning prototype before training: "
                        + before
        );

        System.out.println(
                "Winning prototype after training: "
                        + after
        );

        System.out.println(
                "Updated prototype: "
                        + Arrays.toString(
                        model.prototypes[after]
                )
        );

        System.out.println(
                "================================"
        );
    }
}