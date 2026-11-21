package com.ecosort.fundamentals;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.eigen.Eigen;
import org.nd4j.linalg.factory.Nd4j;

import java.util.Arrays;

public class SVDAnalyzer {

    public static double[] calculateSingularValues(
            INDArray matrix) {

        if (matrix == null) {
            throw new IllegalArgumentException(
                    "Input matrix cannot be null."
            );
        }

        /*
         * For matrix A:
         *
         * singular values of A
         * =
         * square roots of eigenvalues of A^T A.
         */

        INDArray ata =
                matrix.transpose()
                        .mmul(matrix);

        INDArray eigenvalues =
                Eigen.symmetricGeneralizedEigenvalues(
                        ata
                );

        double[] singularValues =
                new double[
                        (int) eigenvalues.length()
                        ];

        for (
                int i = 0;
                i < singularValues.length;
                i++
        ) {

            double eigenvalue =
                    eigenvalues.getDouble(i);

            /*
             * Numerical floating-point calculations can
             * occasionally produce tiny negative values
             * such as -1e-12.
             */

            eigenvalue =
                    Math.max(
                            eigenvalue,
                            0.0
                    );

            singularValues[i] =
                    Math.sqrt(
                            eigenvalue
                    );
        }

        /*
         * Eigenvalues may be returned in ascending order.
         */

        Arrays.sort(
                singularValues
        );

        reverse(
                singularValues
        );

        return singularValues;
    }


    public static double[]
    calculateExplainedEnergy(
            double[] singularValues) {

        double totalEnergy =
                0.0;

        for (
                double singularValue
                : singularValues
        ) {

            totalEnergy +=
                    singularValue
                            * singularValue;
        }

        double[] energy =
                new double[
                        singularValues.length
                        ];

        if (totalEnergy == 0.0) {
            return energy;
        }

        for (
                int i = 0;
                i < singularValues.length;
                i++
        ) {

            energy[i] =
                    (
                            singularValues[i]
                                    * singularValues[i]
                                    / totalEnergy
                    )
                            * 100.0;
        }

        return energy;
    }


    private static void reverse(
            double[] array) {

        for (
                int left = 0,
                right = array.length - 1;
                left < right;
                left++, right--
        ) {

            double temp =
                    array[left];

            array[left] =
                    array[right];

            array[right] =
                    temp;
        }
    }


    public static void main(
            String[] args) {

        /*
         * Waste feature matrix.
         *
         * Columns:
         * Plastic
         * Metal
         * Glass
         * Paper/Cardboard
         */

        INDArray matrix =
                Nd4j.create(
                        new double[][]{

                                {
                                        70,
                                        10,
                                        5,
                                        15
                                },

                                {
                                        65,
                                        15,
                                        5,
                                        15
                                },

                                {
                                        20,
                                        60,
                                        10,
                                        10
                                },

                                {
                                        15,
                                        10,
                                        65,
                                        10
                                },

                                {
                                        10,
                                        10,
                                        10,
                                        70
                                }
                        }
                );


        System.out.println(
                "\n======================================"
        );

        System.out.println(
                "ECOSORT SVD FEATURE ANALYSIS"
        );

        System.out.println(
                "======================================"
        );


        System.out.println(
                "\nWaste feature matrix:"
        );

        System.out.println(
                matrix
        );


        INDArray ata =
                matrix.transpose()
                        .mmul(matrix);


        System.out.println(
                "\nA^T A matrix:"
        );

        System.out.println(
                ata
        );


        double[] singularValues =
                calculateSingularValues(
                        matrix
                );


        double[] explainedEnergy =
                calculateExplainedEnergy(
                        singularValues
                );


        System.out.println(
                "\nSingular values:"
        );

        System.out.println(
                Arrays.toString(
                        singularValues
                )
        );


        System.out.println(
                "\nExplained energy:"
        );


        for (
                int i = 0;
                i < explainedEnergy.length;
                i++
        ) {

            System.out.printf(
                    "Component %d: %.2f%%%n",
                    i + 1,
                    explainedEnergy[i]
            );
        }


        System.out.println(
                "\nInterpretation:"
        );

        System.out.println(
                "Large singular values represent "
                        + "dominant directions of variation "
                        + "in the waste feature data."
        );

        System.out.println(
                "SVD can be used for dimensionality "
                        + "reduction, compression and "
                        + "feature diagnostics."
        );


        System.out.println(
                "\n======================================"
        );
    }
}