package com.ecosort.fundamentals;

public class SigmoidNeuron {

    private final double[] weights;
    private final double bias;

    public SigmoidNeuron(
            double[] weights,
            double bias) {

        this.weights = weights;
        this.bias = bias;
    }

    // Weighted sum:
    // z = w1*x1 + w2*x2 + ... + b
    public double weightedSum(double[] inputs) {

        if (inputs.length != weights.length) {
            throw new IllegalArgumentException(
                    "Number of inputs must match number of weights."
            );
        }

        double sum = bias;

        for (int i = 0; i < inputs.length; i++) {
            sum += inputs[i] * weights[i];
        }

        return sum;
    }

    // Sigmoid activation:
    // sigmoid(z) = 1 / (1 + e^(-z))
    public double sigmoid(double z) {

        return 1.0 / (1.0 + Math.exp(-z));
    }

    public double predictProbability(double[] inputs) {

        double z = weightedSum(inputs);

        return sigmoid(z);
    }

    public int predict(double[] inputs) {

        double probability =
                predictProbability(inputs);

        return probability >= 0.5 ? 1 : 0;
    }

    public static void main(String[] args) {

        // Example sigmoid neuron behaving like AND

        double[] weights = {
                5.0,
                5.0
        };

        double bias = -7.5;

        SigmoidNeuron neuron =
                new SigmoidNeuron(
                        weights,
                        bias
                );

        double[][] inputs = {
                {0, 0},
                {0, 1},
                {1, 0},
                {1, 1}
        };

        System.out.println(
                "Sigmoid Neuron - AND Gate"
        );

        System.out.println(
                "---------------------------------------"
        );

        for (double[] input : inputs) {

            double probability =
                    neuron.predictProbability(input);

            int prediction =
                    neuron.predict(input);

            System.out.printf(
                    "%d AND %d -> probability = %.4f, output = %d%n",
                    (int) input[0],
                    (int) input[1],
                    probability,
                    prediction
            );
        }
    }
}