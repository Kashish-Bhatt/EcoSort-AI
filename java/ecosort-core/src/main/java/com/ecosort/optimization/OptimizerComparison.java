package com.ecosort.optimization;

public class OptimizerComparison {

    private static final double LEARNING_RATE = 0.1;
    private static final double MOMENTUM = 0.9;
    private static final int EPOCHS = 40;

    // Simple objective: f(w) = (w - 3)^2
    private static double loss(double w) {
        return Math.pow(w - 3.0, 2);
    }

    private static double gradient(double w) {
        return 2.0 * (w - 3.0);
    }

    public static Result batchGD() {

        double w = -5.0;

        for (int epoch = 0; epoch < EPOCHS; epoch++) {
            w -= LEARNING_RATE * gradient(w);
        }

        return new Result("Batch GD", w, loss(w));
    }

    public static Result sgd() {

        double w = -5.0;

        for (int epoch = 0; epoch < EPOCHS; epoch++) {

            // Simulated individual observations around optimum.
            double[] targets = {2.8, 3.2, 3.0, 2.9, 3.1};

            for (double target : targets) {

                double grad =
                        2.0 * (w - target);

                w -= 0.02 * grad;
            }
        }

        return new Result("SGD", w, loss(w));
    }

    public static Result momentum() {

        double w = -5.0;
        double velocity = 0.0;

        for (int epoch = 0; epoch < EPOCHS; epoch++) {

            velocity =
                    MOMENTUM * velocity
                            - LEARNING_RATE * gradient(w);

            w += velocity;
        }

        return new Result(
                "Momentum",
                w,
                loss(w)
        );
    }

    public static Result nesterov() {

        double w = -5.0;
        double velocity = 0.0;

        for (int epoch = 0; epoch < EPOCHS; epoch++) {

            double lookAhead =
                    w + MOMENTUM * velocity;

            double grad =
                    gradient(lookAhead);

            velocity =
                    MOMENTUM * velocity
                            - LEARNING_RATE * grad;

            w += velocity;
        }

        return new Result(
                "Nesterov",
                w,
                loss(w)
        );
    }

    public record Result(
            String optimizer,
            double finalWeight,
            double finalLoss) {
    }

    public static void main(String[] args) {

        Result[] results = {
                batchGD(),
                sgd(),
                momentum(),
                nesterov()
        };

        System.out.println(
                "\n===== ECOSORT OPTIMIZER COMPARISON ====="
        );

        System.out.printf(
                "%-15s %-15s %-15s%n",
                "Optimizer",
                "Final Weight",
                "Final Loss"
        );

        for (Result result : results) {

            System.out.printf(
                    "%-15s %-15.6f %-15.8f%n",
                    result.optimizer(),
                    result.finalWeight(),
                    result.finalLoss()
            );
        }

        System.out.println(
                "\nTarget optimum: w = 3.0"
        );

        System.out.println(
                "========================================"
        );
    }
}