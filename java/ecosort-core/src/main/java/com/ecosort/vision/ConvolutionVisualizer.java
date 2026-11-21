package com.ecosort.vision;

import java.util.Arrays;

public class ConvolutionVisualizer {

    public static double[][]
    convolution2D(
            double[][] input,
            double[][] kernel) {

        int outputRows =
                input.length
                        - kernel.length
                        + 1;

        int outputColumns =
                input[0].length
                        - kernel[0].length
                        + 1;

        double[][] output =
                new double[
                        outputRows
                        ][
                        outputColumns
                        ];

        for (int row = 0;
             row < outputRows;
             row++) {

            for (int column = 0;
                 column < outputColumns;
                 column++) {

                double sum = 0.0;

                for (int kr = 0;
                     kr < kernel.length;
                     kr++) {

                    for (int kc = 0;
                         kc < kernel[0].length;
                         kc++) {

                        sum +=
                                input[row + kr][column + kc]
                                        * kernel[kr][kc];
                    }
                }

                output[row][column] =
                        sum;
            }
        }

        return output;
    }

    public static double[][]
    convolution3D(
            double[][][] input,
            double[][][] kernel) {

        int channels =
                input.length;

        int outputRows =
                input[0].length
                        - kernel[0].length
                        + 1;

        int outputColumns =
                input[0][0].length
                        - kernel[0][0].length
                        + 1;

        double[][] output =
                new double[
                        outputRows
                        ][
                        outputColumns
                        ];

        for (int channel = 0;
             channel < channels;
             channel++) {

            double[][] channelOutput =
                    convolution2D(
                            input[channel],
                            kernel[channel]
                    );

            for (int row = 0;
                 row < outputRows;
                 row++) {

                for (int column = 0;
                     column < outputColumns;
                     column++) {

                    output[row][column] +=
                            channelOutput[row][column];
                }
            }
        }

        return output;
    }

    private static void printMatrix(
            double[][] matrix) {

        for (double[] row : matrix) {
            System.out.println(
                    Arrays.toString(row)
            );
        }
    }

    public static void main(String[] args) {

        double[][] grayscaleImage = {
                {1, 1, 0, 0},
                {1, 1, 0, 0},
                {0, 0, 1, 1},
                {0, 0, 1, 1}
        };

        double[][] edgeKernel = {
                {1, 0},
                {0, -1}
        };

        double[][] output2D =
                convolution2D(
                        grayscaleImage,
                        edgeKernel
                );

        System.out.println(
                "\n===== 2D CONVOLUTION ====="
        );

        printMatrix(output2D);

        double[][][] rgbInput = {
                grayscaleImage,
                grayscaleImage,
                grayscaleImage
        };

        double[][][] rgbKernel = {
                edgeKernel,
                edgeKernel,
                edgeKernel
        };

        double[][] output3D =
                convolution3D(
                        rgbInput,
                        rgbKernel
                );

        System.out.println(
                "\n===== 3D / MULTI-CHANNEL CONVOLUTION ====="
        );

        printMatrix(output3D);

        System.out.println(
                "\n2D convolution processes one spatial map."
        );

        System.out.println(
                "Multi-channel convolution combines information "
                        + "across image channels."
        );
    }
}