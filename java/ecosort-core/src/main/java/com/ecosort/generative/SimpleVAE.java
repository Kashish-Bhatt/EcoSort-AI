package com.ecosort.generative;

import java.util.Arrays;
import java.util.Random;

public class SimpleVAE {

    private static final int INPUT_SIZE = 4;
    private static final int LATENT_SIZE = 2;

    private final double[][] meanWeights;
    private final double[][] logVarianceWeights;
    private final double[][] decoderWeights;

    private final Random random;


    public SimpleVAE() {

        random = new Random(42);

        meanWeights =
                createWeights(
                        LATENT_SIZE,
                        INPUT_SIZE,
                        0.08
                );

        logVarianceWeights =
                createWeights(
                        LATENT_SIZE,
                        INPUT_SIZE,
                        0.04
                );

        decoderWeights =
                createWeights(
                        INPUT_SIZE,
                        LATENT_SIZE,
                        0.10
                );
    }


    // =========================================================
    // ENCODER
    // =========================================================

    public LatentDistribution encode(
            double[] input) {

        validateInput(input);

        double[] normalized =
                normalize(input);

        double[] mean =
                multiply(
                        meanWeights,
                        normalized
                );

        double[] logVariance =
                multiply(
                        logVarianceWeights,
                        normalized
                );

        return new LatentDistribution(
                mean,
                logVariance
        );
    }


    // =========================================================
    // REPARAMETERIZATION
    // z = mean + std * epsilon
    // =========================================================

    public double[] sample(
            LatentDistribution distribution) {

        double[] latent =
                new double[LATENT_SIZE];

        for (
                int i = 0;
                i < LATENT_SIZE;
                i++
        ) {

            double epsilon =
                    random.nextGaussian();

            double standardDeviation =
                    Math.exp(
                            0.5
                                    * distribution
                                    .logVariance[i]
                    );

            latent[i] =
                    distribution.mean[i]
                            + standardDeviation
                            * epsilon;
        }

        return latent;
    }


    // =========================================================
    // DECODER
    // =========================================================

    public double[] decode(
            double[] latent) {

        if (
                latent == null
                        || latent.length
                        != LATENT_SIZE
        ) {

            throw new IllegalArgumentException(
                    "Latent vector must contain "
                            + LATENT_SIZE
                            + " values."
            );
        }

        double[] raw =
                multiply(
                        decoderWeights,
                        latent
                );

        /*
         * Softmax converts decoder output
         * into a material-composition distribution.
         */

        double max =
                Arrays.stream(raw)
                        .max()
                        .orElse(0.0);

        double[] positive =
                new double[raw.length];

        double total =
                0.0;

        for (
                int i = 0;
                i < raw.length;
                i++
        ) {

            positive[i] =
                    Math.exp(
                            raw[i] - max
                    );

            total +=
                    positive[i];
        }

        double[] percentages =
                new double[raw.length];

        for (
                int i = 0;
                i < percentages.length;
                i++
        ) {

            percentages[i] =
                    (
                            positive[i]
                                    / total
                    )
                            * 100.0;
        }

        return percentages;
    }


    // =========================================================
    // COMPLETE VAE FORWARD PASS
    // =========================================================

    public VaeResult forward(
            double[] input) {

        LatentDistribution distribution =
                encode(input);

        double[] latent =
                sample(distribution);

        double[] reconstruction =
                decode(latent);

        double reconstructionLoss =
                reconstructionLoss(
                        input,
                        reconstruction
                );

        double klLoss =
                klDivergence(
                        distribution
                );

        double totalLoss =
                reconstructionLoss
                        + klLoss;

        return new VaeResult(
                distribution,
                latent,
                reconstruction,
                reconstructionLoss,
                klLoss,
                totalLoss
        );
    }


    // =========================================================
    // LATENT-SPACE MAP
    // =========================================================

    /*
     * Generates 2-D coordinates for multiple
     * waste profiles.
     *
     * These coordinates can be plotted as a
     * latent-space visualization.
     */

    public double[][] generateLatentMap(
            double[][] profiles) {

        if (
                profiles == null
                        || profiles.length == 0
        ) {

            throw new IllegalArgumentException(
                    "Profiles cannot be empty."
            );
        }

        double[][] coordinates =
                new double[
                        profiles.length
                        ][
                        LATENT_SIZE
                        ];

        for (
                int i = 0;
                i < profiles.length;
                i++
        ) {

            LatentDistribution distribution =
                    encode(
                            profiles[i]
                    );

            double[] mean =
                    distribution.getMean();

            coordinates[i][0] =
                    mean[0];

            coordinates[i][1] =
                    mean[1];
        }

        return coordinates;
    }


    // =========================================================
    // RECONSTRUCTION LOSS
    // =========================================================

    private double reconstructionLoss(
            double[] input,
            double[] reconstruction) {

        double loss =
                0.0;

        for (
                int i = 0;
                i < input.length;
                i++
        ) {

            double difference =
                    input[i]
                            - reconstruction[i];

            loss +=
                    difference
                            * difference;
        }

        return loss
                / input.length;
    }


    // =========================================================
    // KL DIVERGENCE
    // =========================================================

    private double klDivergence(
            LatentDistribution distribution) {

        double kl =
                0.0;

        for (
                int i = 0;
                i < LATENT_SIZE;
                i++
        ) {

            double mean =
                    distribution.mean[i];

            double logVariance =
                    distribution.logVariance[i];

            kl +=
                    -0.5
                            * (
                            1.0
                                    + logVariance
                                    - mean * mean
                                    - Math.exp(
                                    logVariance
                            )
                    );
        }

        return kl;
    }


    // =========================================================
    // NORMALIZATION
    // =========================================================

    private double[] normalize(
            double[] input) {

        double total =
                0.0;

        for (
                double value
                : input
        ) {

            total += value;
        }

        if (
                total <= 0.0
        ) {

            throw new IllegalArgumentException(
                    "Material distribution "
                            + "must have positive total."
            );
        }

        double[] normalized =
                new double[input.length];

        for (
                int i = 0;
                i < input.length;
                i++
        ) {

            normalized[i] =
                    input[i]
                            / total;
        }

        return normalized;
    }


    // =========================================================
    // INPUT VALIDATION
    // =========================================================

    private void validateInput(
            double[] input) {

        if (
                input == null
                        || input.length
                        != INPUT_SIZE
        ) {

            throw new IllegalArgumentException(
                    "VAE input must contain "
                            + INPUT_SIZE
                            + " material values."
            );
        }

        for (
                double value
                : input
        ) {

            if (
                    value < 0.0
            ) {

                throw new IllegalArgumentException(
                        "Material values cannot be negative."
                );
            }
        }
    }


    // =========================================================
    // MATRIX MULTIPLICATION
    // =========================================================

    private double[] multiply(
            double[][] matrix,
            double[] vector) {

        double[] result =
                new double[matrix.length];

        for (
                int row = 0;
                row < matrix.length;
                row++
        ) {

            double sum =
                    0.0;

            for (
                    int column = 0;
                    column < vector.length;
                    column++
            ) {

                sum +=
                        matrix[row][column]
                                * vector[column];
            }

            result[row] =
                    sum;
        }

        return result;
    }


    // =========================================================
    // DETERMINISTIC WEIGHTS
    // =========================================================

    private double[][] createWeights(
            int rows,
            int columns,
            double scale) {

        double[][] weights =
                new double[
                        rows
                        ][
                        columns
                        ];

        for (
                int row = 0;
                row < rows;
                row++
        ) {

            for (
                    int column = 0;
                    column < columns;
                    column++
            ) {

                weights[row][column] =
                        Math.cos(
                                (
                                        row + 1
                                )
                                        * (
                                        column + 2
                                )
                        )
                                * scale;
            }
        }

        return weights;
    }


    // =========================================================
    // LATENT DISTRIBUTION
    // =========================================================

    public static class LatentDistribution {

        private final double[] mean;
        private final double[] logVariance;


        public LatentDistribution(
                double[] mean,
                double[] logVariance) {

            this.mean =
                    Arrays.copyOf(
                            mean,
                            mean.length
                    );

            this.logVariance =
                    Arrays.copyOf(
                            logVariance,
                            logVariance.length
                    );
        }


        public double[] getMean() {

            return Arrays.copyOf(
                    mean,
                    mean.length
            );
        }


        public double[] getLogVariance() {

            return Arrays.copyOf(
                    logVariance,
                    logVariance.length
            );
        }
    }


    // =========================================================
    // VAE RESULT
    // =========================================================

    public static class VaeResult {

        private final LatentDistribution distribution;

        private final double[] latent;
        private final double[] reconstruction;

        private final double reconstructionLoss;
        private final double klLoss;
        private final double totalLoss;


        public VaeResult(
                LatentDistribution distribution,
                double[] latent,
                double[] reconstruction,
                double reconstructionLoss,
                double klLoss,
                double totalLoss) {

            this.distribution =
                    distribution;

            this.latent =
                    Arrays.copyOf(
                            latent,
                            latent.length
                    );

            this.reconstruction =
                    Arrays.copyOf(
                            reconstruction,
                            reconstruction.length
                    );

            this.reconstructionLoss =
                    reconstructionLoss;

            this.klLoss =
                    klLoss;

            this.totalLoss =
                    totalLoss;
        }


        public LatentDistribution
        getDistribution() {

            return distribution;
        }


        public double[] getLatent() {

            return Arrays.copyOf(
                    latent,
                    latent.length
            );
        }


        public double[] getReconstruction() {

            return Arrays.copyOf(
                    reconstruction,
                    reconstruction.length
            );
        }


        public double getReconstructionLoss() {
            return reconstructionLoss;
        }


        public double getKlLoss() {
            return klLoss;
        }


        public double getTotalLoss() {
            return totalLoss;
        }
    }


    // =========================================================
    // DEMONSTRATION
    // =========================================================

    public static void main(
            String[] args) {

        SimpleVAE vae =
                new SimpleVAE();


        // =====================================================
        // SINGLE FORWARD PASS
        // =====================================================

        double[] materialProfile = {

                65.0,
                15.0,
                5.0,
                15.0
        };


        VaeResult result =
                vae.forward(
                        materialProfile
                );


        System.out.println(
                "\n======================================"
        );

        System.out.println(
                "ECOSORT VARIATIONAL AUTOENCODER"
        );

        System.out.println(
                "======================================"
        );


        System.out.println(
                "\nInput material profile:"
        );

        System.out.println(
                Arrays.toString(
                        materialProfile
                )
        );


        System.out.println(
                "\nLatent mean:"
        );

        System.out.println(
                Arrays.toString(
                        result
                                .getDistribution()
                                .getMean()
                )
        );


        System.out.println(
                "\nLatent log variance:"
        );

        System.out.println(
                Arrays.toString(
                        result
                                .getDistribution()
                                .getLogVariance()
                )
        );


        System.out.println(
                "\nSampled latent vector:"
        );

        System.out.println(
                Arrays.toString(
                        result.getLatent()
                )
        );


        System.out.println(
                "\nReconstructed material profile:"
        );

        System.out.println(
                Arrays.toString(
                        result.getReconstruction()
                )
        );


        System.out.printf(
                "%nReconstruction loss: %.6f%n",
                result.getReconstructionLoss()
        );


        System.out.printf(
                "KL divergence: %.6f%n",
                result.getKlLoss()
        );


        System.out.printf(
                "Total VAE loss: %.6f%n",
                result.getTotalLoss()
        );


        // =====================================================
        // LATENT-SPACE DEMONSTRATION
        // =====================================================

        double[][] profiles = {

                // Plastic-dominant
                {
                        90,
                        4,
                        2,
                        4
                },

                {
                        80,
                        8,
                        4,
                        8
                },

                // Metal-dominant
                {
                        5,
                        85,
                        5,
                        5
                },

                {
                        10,
                        75,
                        5,
                        10
                },

                // Glass-dominant
                {
                        5,
                        5,
                        85,
                        5
                },

                {
                        10,
                        5,
                        75,
                        10
                },

                // Paper-dominant
                {
                        5,
                        5,
                        5,
                        85
                },

                {
                        10,
                        5,
                        10,
                        75
                }
        };


        String[] labels = {

                "PLASTIC-1",
                "PLASTIC-2",

                "METAL-1",
                "METAL-2",

                "GLASS-1",
                "GLASS-2",

                "PAPER-1",
                "PAPER-2"
        };


        double[][] latentMap =
                vae.generateLatentMap(
                        profiles
                );


        System.out.println(
                "\n======================================"
        );

        System.out.println(
                "VAE LATENT-SPACE COORDINATES"
        );

        System.out.println(
                "======================================"
        );


        System.out.printf(
                "%-12s %-15s %-15s%n",
                "Profile",
                "Latent X",
                "Latent Y"
        );


        for (
                int i = 0;
                i < latentMap.length;
                i++
        ) {

            System.out.printf(
                    "%-12s %-15.8f %-15.8f%n",
                    labels[i],
                    latentMap[i][0],
                    latentMap[i][1]
            );
        }


        System.out.println(
                "\nThese two-dimensional latent coordinates "
                        + "can be plotted to visualize how "
                        + "different waste-composition profiles "
                        + "are organized in latent space."
        );


        System.out.println(
                "\n======================================"
        );
    }
}