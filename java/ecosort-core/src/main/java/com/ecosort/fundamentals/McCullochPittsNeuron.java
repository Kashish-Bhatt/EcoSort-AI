package com.ecosort.fundamentals;

public class McCullochPittsNeuron {

    private final double[] weights;
    private final double threshold;

    public McCullochPittsNeuron(
            double[] weights,
            double threshold) {

        this.weights = weights;
        this.threshold = threshold;
    }

    public int predict(double[] inputs) {

        if (inputs.length != weights.length) {
            throw new IllegalArgumentException(
                    "Number of inputs must match number of weights."
            );
        }

        double weightedSum = 0.0;

        for (int i = 0; i < inputs.length; i++) {
            weightedSum += inputs[i] * weights[i];
        }

        return weightedSum >= threshold ? 1 : 0;
    }

    public static void main(String[] args) {

        // AND gate
        double[] weights = {1.0, 1.0};

        McCullochPittsNeuron neuron =
                new McCullochPittsNeuron(
                        weights,
                        2.0
                );

        double[][] inputs = {
                {0, 0},
                {0, 1},
                {1, 0},
                {1, 1}
        };

        System.out.println(
                "McCulloch-Pitts Neuron - AND Gate"
        );

        System.out.println("-----------------------------");

        for (double[] input : inputs) {

            int output = neuron.predict(input);

            System.out.println(
                    (int) input[0]
                            + " AND "
                            + (int) input[1]
                            + " -> "
                            + output
            );
        }
    }
}