package com.ecosort.fundamentals;

public class SimpleMLP {

    private final double[][] hiddenWeights;
    private final double[] hiddenBiases;

    private final double[] outputWeights;
    private double outputBias;

    public SimpleMLP() {

        // 2 inputs -> 2 hidden neurons
        hiddenWeights = new double[][]{
                {0.5, -0.4},
                {0.3, 0.8}
        };

        hiddenBiases = new double[]{
                0.1,
                -0.2
        };

        // 2 hidden neurons -> 1 output
        outputWeights = new double[]{
                0.7,
                -0.5
        };

        outputBias = 0.2;
    }


    // Sigmoid activation
    private double sigmoid(double x) {

        return 1.0 / (
                1.0 + Math.exp(-x)
        );
    }


    // Forward propagation
    public double forward(double[] input) {

        if (input.length != 2) {

            throw new IllegalArgumentException(
                    "MLP expects exactly 2 inputs."
            );
        }


        // -------------------------
        // HIDDEN LAYER
        // -------------------------

        double[] hiddenOutput =
                new double[2];


        for (int neuron = 0;
             neuron < 2;
             neuron++) {

            double weightedSum =
                    hiddenBiases[neuron];

            for (int i = 0;
                 i < input.length;
                 i++) {

                weightedSum +=
                        input[i]
                        * hiddenWeights[neuron][i];
            }

            hiddenOutput[neuron] =
                    sigmoid(weightedSum);
        }


        // -------------------------
        // OUTPUT LAYER
        // -------------------------

        double outputSum =
                outputBias;


        for (int i = 0;
             i < hiddenOutput.length;
             i++) {

            outputSum +=
                    hiddenOutput[i]
                    * outputWeights[i];
        }


        return sigmoid(outputSum);
    }


    // Binary Cross-Entropy Loss
    public double binaryCrossEntropy(
            double target,
            double prediction) {

        // Prevent log(0)
        double epsilon = 1e-12;

        prediction = Math.max(
                epsilon,
                Math.min(
                        1.0 - epsilon,
                        prediction
                )
        );


        return -(
                target * Math.log(prediction)
                +
                (1.0 - target)
                * Math.log(1.0 - prediction)
        );
    }


    public static void main(String[] args) {

        SimpleMLP network =
                new SimpleMLP();


        // AND dataset
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
                "Simple MLP - Forward Propagation"
        );

        System.out.println(
                "------------------------------------------"
        );


        double totalLoss = 0.0;


        for (int i = 0;
             i < inputs.length;
             i++) {

            double prediction =
                    network.forward(
                            inputs[i]
                    );


            double loss =
                    network.binaryCrossEntropy(
                            targets[i],
                            prediction
                    );


            totalLoss += loss;


            System.out.printf(
                    "Input [%d, %d] "
                    + "Target = %.0f "
                    + "Prediction = %.4f "
                    + "Loss = %.4f%n",

                    (int) inputs[i][0],
                    (int) inputs[i][1],

                    targets[i],

                    prediction,

                    loss
            );
        }


        double averageLoss =
                totalLoss / inputs.length;


        System.out.println(
                "------------------------------------------"
        );

        System.out.printf(
                "Average Loss = %.4f%n",
                averageLoss
        );
    }
}