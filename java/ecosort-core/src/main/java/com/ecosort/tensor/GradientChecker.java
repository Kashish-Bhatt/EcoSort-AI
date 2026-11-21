package com.ecosort.tensor;

public class GradientChecker {

    // Function: f(w) = w^2
    public static double function(double w) {
        return w * w;
    }

    // Analytical derivative: df/dw = 2w
    public static double analyticalGradient(double w) {
        return 2.0 * w;
    }

    // Numerical gradient using central difference
    public static double numericalGradient(
            double w,
            double epsilon) {

        double positive =
                function(w + epsilon);

        double negative =
                function(w - epsilon);

        return (positive - negative)
                / (2.0 * epsilon);
    }

    public static void main(String[] args) {

        double w = 3.0;
        double epsilon = 1e-5;

        double analytical =
                analyticalGradient(w);

        double numerical =
                numericalGradient(
                        w,
                        epsilon
                );

        double difference =
                Math.abs(
                        analytical - numerical
                );

        System.out.println(
                "\n===== NUMERICAL GRADIENT CHECK =====\n"
        );

        System.out.println(
                "Parameter w: "
                        + w
        );

        System.out.println(
                "Analytical gradient: "
                        + analytical
        );

        System.out.println(
                "Numerical gradient: "
                        + numerical
        );

        System.out.println(
                "Absolute difference: "
                        + difference
        );

        if (difference < 1e-4) {

            System.out.println(
                    "\nGRADIENT CHECK PASSED"
            );

        } else {

            System.out.println(
                    "\nGRADIENT CHECK FAILED"
            );
        }

        System.out.println(
                "\n===================================="
        );
    }
}