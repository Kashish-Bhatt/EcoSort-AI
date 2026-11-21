package com.ecosort.fundamentals;

import java.util.Random;

public class TrainableMLP {

    private final double[][] hiddenWeights;
    private final double[] hiddenBiases;

    private final double[] outputWeights;
    private double outputBias;

    private final double learningRate;

    public TrainableMLP(double learningRate) {

        this.learningRate = learningRate;

        hiddenWeights = new double[2][2];
        hiddenBiases = new double[2];

        outputWeights = new double[2];

        Random random = new Random(42);

        // Small reproducible random initialization
        for (int h = 0; h < 2; h++) {

            for (int i = 0; i < 2; i++) {

                hiddenWeights[h][i] =
                        random.nextDouble() - 0.5;
            }

            hiddenBiases[h] = 0.0;

            outputWeights[h] =
                    random.nextDouble() - 0.5;
        }

        outputBias = 0.0;
    }


    private double sigmoid(double x) {

        return 1.0 /
                (1.0 + Math.exp(-x));
    }


    private double sigmoidDerivative(
            double sigmoidOutput) {

        return sigmoidOutput
                * (1.0 - sigmoidOutput);
    }


    public double predict(double[] input) {

        double[] hidden =
                new double[2];


        // Hidden layer
        for (int h = 0; h < 2; h++) {

            double sum =
                    hiddenBiases[h];

            for (int i = 0; i < 2; i++) {

                sum +=
                        input[i]
                        * hiddenWeights[h][i];
            }

            hidden[h] =
                    sigmoid(sum);
        }


        // Output layer
        double outputSum =
                outputBias;


        for (int h = 0; h < 2; h++) {

            outputSum +=
                    hidden[h]
                    * outputWeights[h];
        }


        return sigmoid(outputSum);
    }


    public double binaryCrossEntropy(
            double target,
            double prediction) {

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


    public void train(
            double[][] inputs,
            double[] targets,
            int epochs) {


        for (int epoch = 1;
             epoch <= epochs;
             epoch++) {


            double totalLoss = 0.0;


            for (int sample = 0;
                 sample < inputs.length;
                 sample++) {


                double[] input =
                        inputs[sample];

                double target =
                        targets[sample];


                // ==========================================
                // FORWARD PROPAGATION
                // ==========================================

                double[] hidden =
                        new double[2];


                for (int h = 0;
                     h < 2;
                     h++) {

                    double sum =
                            hiddenBiases[h];


                    for (int i = 0;
                         i < 2;
                         i++) {

                        sum +=
                                input[i]
                                * hiddenWeights[h][i];
                    }


                    hidden[h] =
                            sigmoid(sum);
                }


                double outputSum =
                        outputBias;


                for (int h = 0;
                     h < 2;
                     h++) {

                    outputSum +=
                            hidden[h]
                            * outputWeights[h];
                }


                double prediction =
                        sigmoid(outputSum);


                totalLoss +=
                        binaryCrossEntropy(
                                target,
                                prediction
                        );


                // ==========================================
                // BACKPROPAGATION
                // ==========================================

                /*
                 * For sigmoid output + binary
                 * cross-entropy:
                 *
                 * dL/dz = prediction - target
                 */

                double outputError =
                        prediction - target;


                // Save output weights BEFORE updating them.
                // They are needed to calculate hidden errors.

                double[] oldOutputWeights =
                        outputWeights.clone();


                // ==========================================
                // OUTPUT LAYER UPDATE
                // ==========================================

                for (int h = 0;
                     h < 2;
                     h++) {

                    double gradient =
                            outputError
                            * hidden[h];


                    outputWeights[h] -=
                            learningRate
                            * gradient;
                }


                outputBias -=
                        learningRate
                        * outputError;


                // ==========================================
                // HIDDEN LAYER UPDATE
                // ==========================================

                for (int h = 0;
                     h < 2;
                     h++) {


                    double hiddenError =
                            outputError
                            * oldOutputWeights[h]
                            * sigmoidDerivative(
                                    hidden[h]
                            );


                    for (int i = 0;
                         i < 2;
                         i++) {


                        double gradient =
                                hiddenError
                                * input[i];


                        hiddenWeights[h][i] -=
                                learningRate
                                * gradient;
                    }


                    hiddenBiases[h] -=
                            learningRate
                            * hiddenError;
                }
            }


            // Show learning progress
            if (epoch == 1
                    || epoch % 1000 == 0
                    || epoch == epochs) {


                double averageLoss =
                        totalLoss
                        / inputs.length;


                System.out.printf(
                        "Epoch %5d | Average Loss = %.6f%n",
                        epoch,
                        averageLoss
                );
            }
        }
    }


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


        TrainableMLP network =
                new TrainableMLP(
                        0.5
                );


        System.out.println(
                "Before Training"
        );

        System.out.println(
                "----------------------------"
        );


        for (double[] input : inputs) {

            System.out.printf(
                    "[%.0f, %.0f] -> %.4f%n",
                    input[0],
                    input[1],
                    network.predict(input)
            );
        }


        System.out.println(
                "\nTraining...\n"
        );


        network.train(
                inputs,
                targets,
                10000
        );


        System.out.println(
                "\nAfter Training"
        );

        System.out.println(
                "----------------------------"
        );


        for (int i = 0;
             i < inputs.length;
             i++) {


            double prediction =
                    network.predict(
                            inputs[i]
                    );


            int predictedClass =
                    prediction >= 0.5
                            ? 1
                            : 0;


            System.out.printf(
                    "[%.0f, %.0f] "
                    + "Target = %.0f "
                    + "Prediction = %.4f "
                    + "Class = %d%n",

                    inputs[i][0],
                    inputs[i][1],

                    targets[i],

                    prediction,

                    predictedClass
            );
        }
    }
}