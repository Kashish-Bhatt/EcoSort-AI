package com.ecosort.sequence;

import java.util.Arrays;

public class WasteStreamLSTM {

    private static final int INPUT_SIZE = 5;
    private static final int HIDDEN_SIZE = 4;

    private final double[][] forgetWeights;
    private final double[][] inputWeights;
    private final double[][] candidateWeights;
    private final double[][] outputWeights;

    private final double[] forgetBias;
    private final double[] inputBias;
    private final double[] candidateBias;
    private final double[] outputBias;

    private double[] hiddenState;
    private double[] cellState;


    public WasteStreamLSTM() {

        /*
         * Input features:
         *
         * 0 = Plastic %
         * 1 = Metal %
         * 2 = Glass %
         * 3 = Paper/Cardboard %
         * 4 = Contamination %
         *
         * Each gate receives:
         *
         * input features + previous hidden state
         */

        int combinedSize =
                INPUT_SIZE + HIDDEN_SIZE;


        forgetWeights =
                createWeights(
                        HIDDEN_SIZE,
                        combinedSize,
                        0.08
                );

        inputWeights =
                createWeights(
                        HIDDEN_SIZE,
                        combinedSize,
                        0.06
                );

        candidateWeights =
                createWeights(
                        HIDDEN_SIZE,
                        combinedSize,
                        0.10
                );

        outputWeights =
                createWeights(
                        HIDDEN_SIZE,
                        combinedSize,
                        0.07
                );


        forgetBias =
                new double[]{
                        0.5,
                        0.5,
                        0.5,
                        0.5
                };

        inputBias =
                new double[]{
                        0.1,
                        0.1,
                        0.1,
                        0.1
                };

        candidateBias =
                new double[]{
                        0.0,
                        0.0,
                        0.0,
                        0.0
                };

        outputBias =
                new double[]{
                        0.1,
                        0.1,
                        0.1,
                        0.1
                };


        resetState();
    }


    // =========================================================
    // PROCESS ONE TIME STEP
    // =========================================================

    public double[] step(
            double[] input) {

        if (
                input == null
                        || input.length
                        != INPUT_SIZE
        ) {

            throw new IllegalArgumentException(
                    "LSTM input must contain "
                            + INPUT_SIZE
                            + " features."
            );
        }


        double[] normalizedInput =
                normalizeInput(
                        input
                );


        double[] combined =
                concatenate(
                        normalizedInput,
                        hiddenState
                );


        // Forget gate:
        // f_t = sigmoid(W_f [x_t, h_t-1] + b_f)

        double[] forgetGate =
                sigmoidVector(
                        affine(
                                forgetWeights,
                                combined,
                                forgetBias
                        )
                );


        // Input gate:
        // i_t = sigmoid(W_i [x_t, h_t-1] + b_i)

        double[] inputGate =
                sigmoidVector(
                        affine(
                                inputWeights,
                                combined,
                                inputBias
                        )
                );


        // Candidate state:
        // C~_t = tanh(W_c [x_t, h_t-1] + b_c)

        double[] candidate =
                tanhVector(
                        affine(
                                candidateWeights,
                                combined,
                                candidateBias
                        )
                );


        // Cell state:
        // C_t =
        // f_t * C_t-1
        // +
        // i_t * C~_t

        double[] newCellState =
                new double[
                        HIDDEN_SIZE
                        ];


        for (
                int i = 0;
                i < HIDDEN_SIZE;
                i++
        ) {

            newCellState[i] =
                    forgetGate[i]
                            * cellState[i]
                            + inputGate[i]
                            * candidate[i];
        }


        // Output gate:
        // o_t = sigmoid(W_o [x_t, h_t-1] + b_o)

        double[] outputGate =
                sigmoidVector(
                        affine(
                                outputWeights,
                                combined,
                                outputBias
                        )
                );


        // Hidden state:
        // h_t = o_t * tanh(C_t)

        double[] newHiddenState =
                new double[
                        HIDDEN_SIZE
                        ];


        for (
                int i = 0;
                i < HIDDEN_SIZE;
                i++
        ) {

            newHiddenState[i] =
                    outputGate[i]
                            * Math.tanh(
                            newCellState[i]
                    );
        }


        cellState =
                newCellState;

        hiddenState =
                newHiddenState;


        return Arrays.copyOf(
                hiddenState,
                hiddenState.length
        );
    }


    // =========================================================
    // PROCESS SEQUENCE
    // =========================================================

    public StreamAssessment analyzeSequence(
            double[][] sequence) {

        if (
                sequence == null
                        || sequence.length == 0
        ) {

            throw new IllegalArgumentException(
                    "Waste stream sequence "
                            + "cannot be empty."
            );
        }


        resetState();


        double contaminationTotal =
                0.0;


        for (
                double[] observation
                : sequence
        ) {

            step(
                    observation
            );


            contaminationTotal +=
                    observation[4];
        }


        double averageContamination =
                contaminationTotal
                        / sequence.length;


        /*
         * The explicit LSTM hidden state represents
         * temporal information.
         *
         * The contamination average provides a
         * human-interpretable stream-quality signal.
         */

        double hiddenSignal =
                0.0;


        for (
                double value
                : hiddenState
        ) {

            hiddenSignal +=
                    Math.abs(
                            value
                    );
        }


        hiddenSignal /=
                HIDDEN_SIZE;


        StreamRisk risk;


        if (
                averageContamination
                        >= 35.0
        ) {

            risk =
                    StreamRisk.HIGH;

        } else if (
                averageContamination
                        >= 20.0
        ) {

            risk =
                    StreamRisk.MODERATE;

        } else {

            risk =
                    StreamRisk.LOW;
        }


        return new StreamAssessment(

                risk,

                averageContamination,

                hiddenSignal,

                Arrays.copyOf(
                        hiddenState,
                        hiddenState.length
                ),

                Arrays.copyOf(
                        cellState,
                        cellState.length
                )
        );
    }


    // =========================================================
    // RESET
    // =========================================================

    public void resetState() {

        hiddenState =
                new double[
                        HIDDEN_SIZE
                        ];

        cellState =
                new double[
                        HIDDEN_SIZE
                        ];
    }


    // =========================================================
    // NORMALIZATION
    // =========================================================

    private double[] normalizeInput(
            double[] input) {

        double[] normalized =
                new double[
                        input.length
                        ];


        for (
                int i = 0;
                i < input.length;
                i++
        ) {

            if (
                    input[i] < 0.0
                            || input[i] > 100.0
            ) {

                throw new IllegalArgumentException(
                        "Waste stream percentages "
                                + "must be between 0 and 100."
                );
            }


            normalized[i] =
                    input[i] / 100.0;
        }


        return normalized;
    }


    // =========================================================
    // AFFINE TRANSFORMATION
    // =========================================================

    private double[] affine(
            double[][] weights,
            double[] input,
            double[] bias) {

        double[] output =
                new double[
                        weights.length
                        ];


        for (
                int row = 0;
                row < weights.length;
                row++
        ) {

            double value =
                    bias[row];


            for (
                    int column = 0;
                    column < input.length;
                    column++
            ) {

                value +=
                        weights[row][column]
                                * input[column];
            }


            output[row] =
                    value;
        }


        return output;
    }


    // =========================================================
    // ACTIVATIONS
    // =========================================================

    private double[] sigmoidVector(
            double[] values) {

        double[] output =
                new double[
                        values.length
                        ];


        for (
                int i = 0;
                i < values.length;
                i++
        ) {

            output[i] =
                    1.0
                            / (
                            1.0
                                    + Math.exp(
                                    -values[i]
                            )
                    );
        }


        return output;
    }


    private double[] tanhVector(
            double[] values) {

        double[] output =
                new double[
                        values.length
                        ];


        for (
                int i = 0;
                i < values.length;
                i++
        ) {

            output[i] =
                    Math.tanh(
                            values[i]
                    );
        }


        return output;
    }


    // =========================================================
    // CONCATENATION
    // =========================================================

    private double[] concatenate(
            double[] input,
            double[] hidden) {

        double[] combined =
                new double[
                        input.length
                                + hidden.length
                        ];


        System.arraycopy(
                input,
                0,
                combined,
                0,
                input.length
        );


        System.arraycopy(
                hidden,
                0,
                combined,
                input.length,
                hidden.length
        );


        return combined;
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

                /*
                 * Deterministic initialization keeps
                 * this educational implementation
                 * reproducible.
                 */

                weights[row][column] =
                        Math.sin(
                                (
                                        row + 1
                                )
                                        * (
                                        column + 1
                                )
                        )
                                * scale;
            }
        }


        return weights;
    }


    // =========================================================
    // RESULT TYPES
    // =========================================================

    public enum StreamRisk {

        LOW,
        MODERATE,
        HIGH
    }


    public static class StreamAssessment {

        private final StreamRisk risk;

        private final double
                averageContamination;

        private final double
                hiddenSignal;

        private final double[]
                hiddenState;

        private final double[]
                cellState;


        public StreamAssessment(
                StreamRisk risk,
                double averageContamination,
                double hiddenSignal,
                double[] hiddenState,
                double[] cellState) {

            this.risk =
                    risk;

            this.averageContamination =
                    averageContamination;

            this.hiddenSignal =
                    hiddenSignal;

            this.hiddenState =
                    hiddenState;

            this.cellState =
                    cellState;
        }


        public StreamRisk getRisk() {
            return risk;
        }


        public double getAverageContamination() {
            return averageContamination;
        }


        public double getHiddenSignal() {
            return hiddenSignal;
        }


        public double[] getHiddenState() {
            return Arrays.copyOf(
                    hiddenState,
                    hiddenState.length
            );
        }


        public double[] getCellState() {
            return Arrays.copyOf(
                    cellState,
                    cellState.length
            );
        }
    }


    // =========================================================
    // DEMONSTRATION
    // =========================================================

    public static void main(
            String[] args) {

        WasteStreamLSTM lstm =
                new WasteStreamLSTM();


        /*
         * Five consecutive waste-stream observations.
         *
         * Columns:
         *
         * Plastic
         * Metal
         * Glass
         * Paper/Cardboard
         * Contamination
         */

        double[][] sequence = {

                {
                        70,
                        10,
                        5,
                        15,
                        12
                },

                {
                        65,
                        12,
                        8,
                        15,
                        16
                },

                {
                        60,
                        15,
                        10,
                        15,
                        22
                },

                {
                        55,
                        18,
                        12,
                        15,
                        28
                },

                {
                        48,
                        20,
                        15,
                        17,
                        38
                }
        };


        StreamAssessment result =
                lstm.analyzeSequence(
                        sequence
                );


        System.out.println(
                "\n======================================"
        );

        System.out.println(
                "ECOSORT LSTM STREAM MONITOR"
        );

        System.out.println(
                "======================================"
        );


        System.out.printf(
                "%nAverage contamination: %.2f%%%n",
                result
                        .getAverageContamination()
        );


        System.out.println(
                "Stream risk: "
                        + result.getRisk()
        );


        System.out.printf(
                "Temporal hidden signal: %.6f%n",
                result.getHiddenSignal()
        );


        System.out.println(
                "\nFinal hidden state:"
        );


        System.out.println(
                Arrays.toString(
                        result.getHiddenState()
                )
        );


        System.out.println(
                "\nFinal cell state:"
        );


        System.out.println(
                Arrays.toString(
                        result.getCellState()
                )
        );


        System.out.println(
                "\n======================================"
        );
    }
}