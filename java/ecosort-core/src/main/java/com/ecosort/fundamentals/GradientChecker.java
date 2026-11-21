package com.ecosort.fundamentals;

public class GradientChecker {

    private static final double EPSILON =
            1e-5;

    private static final double TOLERANCE =
            1e-4;


    /*
     * Simple loss:
     *
     * y_hat = sigmoid(wx + b)
     *
     * L = 1/2 (y_hat - y)^2
     */

    public static double loss(
            double weight,
            double bias,
            double input,
            double target) {

        double prediction =
                sigmoid(
                        weight * input
                                + bias
                );

        double error =
                prediction - target;

        return 0.5
                * error
                * error;
    }


    public static double analyticWeightGradient(
            double weight,
            double bias,
            double input,
            double target) {

        double z =
                weight * input
                        + bias;

        double prediction =
                sigmoid(z);

        double dLossDPrediction =
                prediction - target;

        double dPredictionDZ =
                prediction
                        * (1.0 - prediction);

        return dLossDPrediction
                * dPredictionDZ
                * input;
    }


    public static double numericalWeightGradient(
            double weight,
            double bias,
            double input,
            double target) {

        double positive =
                loss(
                        weight + EPSILON,
                        bias,
                        input,
                        target
                );

        double negative =
                loss(
                        weight - EPSILON,
                        bias,
                        input,
                        target
                );

        return (
                positive - negative
        )
                / (
                2.0 * EPSILON
        );
    }


    public static boolean checkGradient(
            double weight,
            double bias,
            double input,
            double target) {

        double analytic =
                analyticWeightGradient(
                        weight,
                        bias,
                        input,
                        target
                );

        double numerical =
                numericalWeightGradient(
                        weight,
                        bias,
                        input,
                        target
                );

        return Math.abs(
                analytic - numerical
        ) < TOLERANCE;
    }


    private static double sigmoid(
            double value) {

        return 1.0
                / (
                1.0
                        + Math.exp(
                        -value
                )
        );
    }


    public static void main(
            String[] args) {

        double weight =
                0.7;

        double bias =
                -0.2;

        double input =
                0.8;

        double target =
                1.0;


        double analytic =
                analyticWeightGradient(
                        weight,
                        bias,
                        input,
                        target
                );


        double numerical =
                numericalWeightGradient(
                        weight,
                        bias,
                        input,
                        target
                );


        double difference =
                Math.abs(
                        analytic - numerical
                );


        System.out.println(
                "\n======================================"
        );

        System.out.println(
                "ECOSORT NUMERICAL GRADIENT CHECK"
        );

        System.out.println(
                "======================================"
        );


        System.out.printf(
                "Analytic gradient : %.10f%n",
                analytic
        );


        System.out.printf(
                "Numerical gradient: %.10f%n",
                numerical
        );


        System.out.printf(
                "Absolute difference: %.12f%n",
                difference
        );


        System.out.println(
                "Gradient check: "
                        + (
                        checkGradient(
                                weight,
                                bias,
                                input,
                                target
                        )
                                ? "PASSED"
                                : "FAILED"
                )
        );


        System.out.println(
                "======================================"
        );
    }
}