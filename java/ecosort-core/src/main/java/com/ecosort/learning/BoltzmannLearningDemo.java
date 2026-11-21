package com.ecosort.learning;

import java.util.Random;

public class BoltzmannLearningDemo {

    private final double[][] weights = {
            {0.0, 0.7, -0.3},
            {0.7, 0.0, 0.5},
            {-0.3, 0.5, 0.0}
    };

    private final Random random =
            new Random(42);

    public double energy(int[] state) {

        double energy = 0.0;

        for (int i = 0;
             i < state.length;
             i++) {

            for (int j = i + 1;
                 j < state.length;
                 j++) {

                energy -=
                        weights[i][j]
                                * state[i]
                                * state[j];
            }
        }

        return energy;
    }

    public double activationProbability(
            double deltaEnergy,
            double temperature) {

        return 1.0
                / (
                1.0
                        + Math.exp(
                        deltaEnergy
                                / temperature
                )
        );
    }

    public boolean acceptTransition(
            double oldEnergy,
            double newEnergy,
            double temperature) {

        if (newEnergy <= oldEnergy) {
            return true;
        }

        double probability =
                Math.exp(
                        -(
                                newEnergy
                                        - oldEnergy
                        )
                                / temperature
                );

        return random.nextDouble()
                < probability;
    }

    public static void main(String[] args) {

        BoltzmannLearningDemo model =
                new BoltzmannLearningDemo();

        int[] stateA = {
                1,
                1,
                -1
        };

        int[] stateB = {
                1,
                -1,
                1
        };

        double energyA =
                model.energy(stateA);

        double energyB =
                model.energy(stateB);

        System.out.println(
                "\n===== ECOSORT ENERGY-BASED DEMO ====="
        );

        System.out.printf(
                "State A energy: %.4f%n",
                energyA
        );

        System.out.printf(
                "State B energy: %.4f%n",
                energyB
        );

        System.out.println(
                "Transition accepted: "
                        + model.acceptTransition(
                        energyA,
                        energyB,
                        1.0
                )
        );

        System.out.println(
                "\nLower-energy states are preferred, "
                        + "while temperature permits "
                        + "probabilistic exploration."
        );

        System.out.println(
                "======================================"
        );
    }
}