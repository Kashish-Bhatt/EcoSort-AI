package com.ecosort.learning;

public class EnergyBasedLearning {

    /*
     * Simple two-neuron energy model.
     *
     * Energy:
     *
     * E = -(w * s1 * s2)
     *     -(b1 * s1)
     *     -(b2 * s2)
     *
     * Lower energy represents a more preferred/stable state.
     */

    public static double calculateEnergy(
            int s1,
            int s2,
            double weight,
            double bias1,
            double bias2) {

        return -(
                weight * s1 * s2
        )
                - (bias1 * s1)
                - (bias2 * s2);
    }


    /*
     * Boltzmann probability:
     *
     * P(state) proportional to exp(-E / T)
     */

    public static double boltzmannScore(
            double energy,
            double temperature) {

        return Math.exp(
                -energy / temperature
        );
    }


    public static void main(String[] args) {

        double weight = 1.0;

        double bias1 = 0.2;
        double bias2 = 0.2;

        double temperature = 1.0;


        int[][] states = {

                {-1, -1},
                {-1, 1},
                {1, -1},
                {1, 1}
        };


        double[] energies =
                new double[states.length];

        double[] scores =
                new double[states.length];


        double totalScore = 0.0;


        System.out.println(
                "\n===== ENERGY-BASED / BOLTZMANN DEMO =====\n"
        );


        // Calculate energies and unnormalized probabilities
        for (int i = 0;
             i < states.length;
             i++) {

            int s1 = states[i][0];
            int s2 = states[i][1];


            energies[i] =
                    calculateEnergy(
                            s1,
                            s2,
                            weight,
                            bias1,
                            bias2
                    );


            scores[i] =
                    boltzmannScore(
                            energies[i],
                            temperature
                    );


            totalScore +=
                    scores[i];
        }


        // Normalize probabilities
        for (int i = 0;
             i < states.length;
             i++) {

            double probability =
                    scores[i] / totalScore;


            System.out.printf(
                    "State [%2d, %2d] "
                    + "Energy = %7.4f "
                    + "Probability = %.4f%n",

                    states[i][0],
                    states[i][1],
                    energies[i],
                    probability
            );
        }


        // Find minimum-energy state
        int minimumIndex = 0;


        for (int i = 1;
             i < energies.length;
             i++) {

            if (energies[i]
                    < energies[minimumIndex]) {

                minimumIndex = i;
            }
        }


        System.out.println(
                "\nLowest-energy state:"
        );


        System.out.printf(
                "[%d, %d] with energy %.4f%n",
                states[minimumIndex][0],
                states[minimumIndex][1],
                energies[minimumIndex]
        );


        System.out.println(
                "\nInterpretation:"
        );


        System.out.println(
                "Lower-energy states receive higher "
                + "Boltzmann probability."
        );


        System.out.println(
                "Unlike error-correction learning, "
                + "energy-based models represent preferred "
                + "configurations through an energy function."
        );


        System.out.println(
                "\n===== ENERGY DEMO COMPLETE ====="
        );
    }
}