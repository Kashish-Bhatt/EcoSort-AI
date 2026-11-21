package com.ecosort.fundamentals;

import java.util.Random;

public class OptimizerComparison {

    enum Optimizer {
        BATCH_GD,
        SGD,
        MOMENTUM,
        NESTEROV
    }

    private static final double[][] INPUTS = {
            {0, 0},
            {0, 1},
            {1, 0},
            {1, 1}
    };

    private static final double[] TARGETS = {
            0, 0, 0, 1
    };

    private static final int INPUT_SIZE = 2;
    private static final int HIDDEN_SIZE = 2;

    private final double[][] hiddenWeights;
    private final double[] hiddenBiases;

    private final double[] outputWeights;
    private double outputBias;

    private final double[][] velocityHiddenWeights;
    private final double[] velocityHiddenBiases;

    private final double[] velocityOutputWeights;
    private double velocityOutputBias;

    private final double learningRate;
    private final double momentum;

    public OptimizerComparison(
            double learningRate,
            double momentum) {

        this.learningRate = learningRate;
        this.momentum = momentum;

        hiddenWeights =
                new double[HIDDEN_SIZE][INPUT_SIZE];

        hiddenBiases =
                new double[HIDDEN_SIZE];

        outputWeights =
                new double[HIDDEN_SIZE];

        velocityHiddenWeights =
                new double[HIDDEN_SIZE][INPUT_SIZE];

        velocityHiddenBiases =
                new double[HIDDEN_SIZE];

        velocityOutputWeights =
                new double[HIDDEN_SIZE];

        initializeWeights();
    }

    private void initializeWeights() {

        Random random = new Random(42);

        for (int h = 0; h < HIDDEN_SIZE; h++) {

            for (int i = 0; i < INPUT_SIZE; i++) {

                hiddenWeights[h][i] =
                        random.nextDouble() - 0.5;
            }

            outputWeights[h] =
                    random.nextDouble() - 0.5;
        }
    }

    private double sigmoid(double value) {

        return 1.0 /
                (1.0 + Math.exp(-value));
    }

    private double sigmoidDerivative(
            double output) {

        return output * (1.0 - output);
    }

    private double binaryCrossEntropy(
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

    private ForwardResult forward(
            double[] input) {

        double[] hidden =
                new double[HIDDEN_SIZE];

        for (int h = 0;
             h < HIDDEN_SIZE;
             h++) {

            double sum =
                    hiddenBiases[h];

            for (int i = 0;
                 i < INPUT_SIZE;
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
             h < HIDDEN_SIZE;
             h++) {

            outputSum +=
                    hidden[h]
                    * outputWeights[h];
        }

        double output =
                sigmoid(outputSum);

        return new ForwardResult(
                hidden,
                output
        );
    }

    private Gradients calculateGradients(
            double[] input,
            double target) {

        ForwardResult result =
                forward(input);

        double outputError =
                result.output - target;

        Gradients gradients =
                new Gradients();

        for (int h = 0;
             h < HIDDEN_SIZE;
             h++) {

            gradients.outputWeights[h] =
                    outputError
                    * result.hidden[h];
        }

        gradients.outputBias =
                outputError;

        for (int h = 0;
             h < HIDDEN_SIZE;
             h++) {

            double hiddenError =
                    outputError
                    * outputWeights[h]
                    * sigmoidDerivative(
                            result.hidden[h]
                    );

            for (int i = 0;
                 i < INPUT_SIZE;
                 i++) {

                gradients.hiddenWeights[h][i] =
                        hiddenError
                        * input[i];
            }

            gradients.hiddenBiases[h] =
                    hiddenError;
        }

        return gradients;
    }

    private void applySGD(
            Gradients gradients) {

        for (int h = 0;
             h < HIDDEN_SIZE;
             h++) {

            for (int i = 0;
                 i < INPUT_SIZE;
                 i++) {

                hiddenWeights[h][i] -=
                        learningRate
                        * gradients.hiddenWeights[h][i];
            }

            hiddenBiases[h] -=
                    learningRate
                    * gradients.hiddenBiases[h];

            outputWeights[h] -=
                    learningRate
                    * gradients.outputWeights[h];
        }

        outputBias -=
                learningRate
                * gradients.outputBias;
    }

    private void applyMomentum(
            Gradients gradients) {

        for (int h = 0;
             h < HIDDEN_SIZE;
             h++) {

            for (int i = 0;
                 i < INPUT_SIZE;
                 i++) {

                velocityHiddenWeights[h][i] =
                        momentum
                        * velocityHiddenWeights[h][i]
                        -
                        learningRate
                        * gradients.hiddenWeights[h][i];

                hiddenWeights[h][i] +=
                        velocityHiddenWeights[h][i];
            }

            velocityHiddenBiases[h] =
                    momentum
                    * velocityHiddenBiases[h]
                    -
                    learningRate
                    * gradients.hiddenBiases[h];

            hiddenBiases[h] +=
                    velocityHiddenBiases[h];

            velocityOutputWeights[h] =
                    momentum
                    * velocityOutputWeights[h]
                    -
                    learningRate
                    * gradients.outputWeights[h];

            outputWeights[h] +=
                    velocityOutputWeights[h];
        }

        velocityOutputBias =
                momentum
                * velocityOutputBias
                -
                learningRate
                * gradients.outputBias;

        outputBias +=
                velocityOutputBias;
    }

    private void applyNesterov(
            double[] input,
            double target) {

        // Save original parameters
        double[][] originalHidden =
                copyMatrix(hiddenWeights);

        double[] originalHiddenBias =
                hiddenBiases.clone();

        double[] originalOutput =
                outputWeights.clone();

        double originalOutputBias =
                outputBias;

        // Look ahead
        for (int h = 0;
             h < HIDDEN_SIZE;
             h++) {

            for (int i = 0;
                 i < INPUT_SIZE;
                 i++) {

                hiddenWeights[h][i] +=
                        momentum
                        * velocityHiddenWeights[h][i];
            }

            hiddenBiases[h] +=
                    momentum
                    * velocityHiddenBiases[h];

            outputWeights[h] +=
                    momentum
                    * velocityOutputWeights[h];
        }

        outputBias +=
                momentum
                * velocityOutputBias;

        // Gradient at look-ahead position
        Gradients gradients =
                calculateGradients(
                        input,
                        target
                );

        // Restore original parameters
        copyInto(
                originalHidden,
                hiddenWeights
        );

        System.arraycopy(
                originalHiddenBias,
                0,
                hiddenBiases,
                0,
                HIDDEN_SIZE
        );

        System.arraycopy(
                originalOutput,
                0,
                outputWeights,
                0,
                HIDDEN_SIZE
        );

        outputBias =
                originalOutputBias;

        // Apply Nesterov update
        applyMomentum(gradients);
    }

    private void trainBatchGD() {

        Gradients total =
                new Gradients();

        for (int sample = 0;
             sample < INPUTS.length;
             sample++) {

            Gradients gradients =
                    calculateGradients(
                            INPUTS[sample],
                            TARGETS[sample]
                    );

            total.add(gradients);
        }

        total.divide(
                INPUTS.length
        );

        applySGD(total);
    }

    public double train(
            Optimizer optimizer,
            int epochs) {

        for (int epoch = 0;
             epoch < epochs;
             epoch++) {

            if (optimizer
                    == Optimizer.BATCH_GD) {

                trainBatchGD();

            } else {

                for (int sample = 0;
                     sample < INPUTS.length;
                     sample++) {

                    if (optimizer
                            == Optimizer.NESTEROV) {

                        applyNesterov(
                                INPUTS[sample],
                                TARGETS[sample]
                        );

                    } else {

                        Gradients gradients =
                                calculateGradients(
                                        INPUTS[sample],
                                        TARGETS[sample]
                                );

                        if (optimizer
                                == Optimizer.MOMENTUM) {

                            applyMomentum(
                                    gradients
                            );

                        } else {

                            applySGD(
                                    gradients
                            );
                        }
                    }
                }
            }
        }

        return calculateAverageLoss();
    }

    private double calculateAverageLoss() {

        double totalLoss = 0.0;

        for (int i = 0;
             i < INPUTS.length;
             i++) {

            double prediction =
                    forward(
                            INPUTS[i]
                    ).output;

            totalLoss +=
                    binaryCrossEntropy(
                            TARGETS[i],
                            prediction
                    );
        }

        return totalLoss /
                INPUTS.length;
    }

    private double calculateAccuracy() {

        int correct = 0;

        for (int i = 0;
             i < INPUTS.length;
             i++) {

            double prediction =
                    forward(
                            INPUTS[i]
                    ).output;

            int predictedClass =
                    prediction >= 0.5
                            ? 1
                            : 0;

            if (predictedClass
                    == (int) TARGETS[i]) {

                correct++;
            }
        }

        return (
                correct * 100.0
        ) / INPUTS.length;
    }

    private static double[][] copyMatrix(
            double[][] matrix) {

        double[][] copy =
                new double[matrix.length][];

        for (int i = 0;
             i < matrix.length;
             i++) {

            copy[i] =
                    matrix[i].clone();
        }

        return copy;
    }

    private static void copyInto(
            double[][] source,
            double[][] destination) {

        for (int i = 0;
             i < source.length;
             i++) {

            System.arraycopy(
                    source[i],
                    0,
                    destination[i],
                    0,
                    source[i].length
            );
        }
    }

    private static class ForwardResult {

        double[] hidden;
        double output;

        ForwardResult(
                double[] hidden,
                double output) {

            this.hidden = hidden;
            this.output = output;
        }
    }

    private static class Gradients {

        double[][] hiddenWeights =
                new double[HIDDEN_SIZE][INPUT_SIZE];

        double[] hiddenBiases =
                new double[HIDDEN_SIZE];

        double[] outputWeights =
                new double[HIDDEN_SIZE];

        double outputBias;

        void add(
                Gradients other) {

            for (int h = 0;
                 h < HIDDEN_SIZE;
                 h++) {

                for (int i = 0;
                     i < INPUT_SIZE;
                     i++) {

                    hiddenWeights[h][i] +=
                            other.hiddenWeights[h][i];
                }

                hiddenBiases[h] +=
                        other.hiddenBiases[h];

                outputWeights[h] +=
                        other.outputWeights[h];
            }

            outputBias +=
                    other.outputBias;
        }

        void divide(
                double value) {

            for (int h = 0;
                 h < HIDDEN_SIZE;
                 h++) {

                for (int i = 0;
                     i < INPUT_SIZE;
                     i++) {

                    hiddenWeights[h][i] /=
                            value;
                }

                hiddenBiases[h] /=
                        value;

                outputWeights[h] /=
                        value;
            }

            outputBias /=
                    value;
        }
    }

    public static void main(
            String[] args) {

        System.out.println(
                "Optimizer Comparison"
        );

        System.out.println(
                "=============================================="
        );

        System.out.printf(
                "%-15s %-15s %-15s%n",
                "Optimizer",
                "Final Loss",
                "Accuracy"
        );

        System.out.println(
                "----------------------------------------------"
        );

        for (Optimizer optimizer
                : Optimizer.values()) {

            OptimizerComparison network =
                    new OptimizerComparison(
                            0.5,
                            0.9
                    );

            double finalLoss =
                    network.train(
                            optimizer,
                            5000
                    );

            double accuracy =
                    network.calculateAccuracy();

            System.out.printf(
                    "%-15s %-15.6f %-14.2f%%%n",
                    optimizer,
                    finalLoss,
                    accuracy
            );
        }
    }
}