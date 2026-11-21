package com.ecosort.fundamentals;

public class ActivationFunctionsDemo {

    public static double step(double x) {
        return x >= 0.0 ? 1.0 : 0.0;
    }

    public static double sigmoid(double x) {
        return 1.0 / (1.0 + Math.exp(-x));
    }

    public static double tanh(double x) {
        return Math.tanh(x);
    }

    public static double sigmoidDerivative(double x) {

        double s = sigmoid(x);

        return s * (1.0 - s);
    }

    public static double tanhDerivative(double x) {

        double t = tanh(x);

        return 1.0 - (t * t);
    }

    public static void main(String[] args) {

        double[] inputs = {
                -3.0,
                -2.0,
                -1.0,
                -0.5,
                0.0,
                0.5,
                1.0,
                2.0,
                3.0
        };

        System.out.println(
                "\n=============================================="
        );

        System.out.println(
                "ECOSORT ACTIVATION FUNCTION COMPARISON"
        );

        System.out.println(
                "=============================================="
        );

        System.out.printf(
                "%-8s %-10s %-12s %-12s%n",
                "Input",
                "Step",
                "Sigmoid",
                "Tanh"
        );

        System.out.println(
                "----------------------------------------------"
        );

        for (double x : inputs) {

            System.out.printf(
                    "%-8.2f %-10.2f %-12.6f %-12.6f%n",
                    x,
                    step(x),
                    sigmoid(x),
                    tanh(x)
            );
        }

        System.out.println(
                "\nKey behaviour:"
        );

        System.out.println(
                "Step    : binary threshold; non-differentiable at 0."
        );

        System.out.println(
                "Sigmoid : smooth output between 0 and 1."
        );

        System.out.println(
                "Tanh    : smooth zero-centred output between -1 and 1."
        );

        System.out.println(
                "\nSigmoid derivative at 0 = "
                        + sigmoidDerivative(0.0)
        );

        System.out.println(
                "Tanh derivative at 0    = "
                        + tanhDerivative(0.0)
        );

        System.out.println(
                "=============================================="
        );
    }
}