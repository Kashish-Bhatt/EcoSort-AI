package com.ecosort.tensor;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

public class SVDAnalysis {

    public static void main(String[] args) {

        System.out.println(
                "\n===== ECOSORT SVD ANALYSIS =====\n"
        );

        // --------------------------------------------------
        // ORIGINAL MATRIX
        // --------------------------------------------------

        double[][] data = {
                {3.0, 1.0, 1.0},
                {-1.0, 3.0, 1.0}
        };

        INDArray matrix = Nd4j.create(data);

        System.out.println("Original Matrix:");
        System.out.println(matrix);


        // --------------------------------------------------
        // COMPUTE A^T * A
        //
        // Singular values of A are the square roots
        // of the eigenvalues of A^T A.
        // --------------------------------------------------

        INDArray transpose =
                matrix.transpose();

        INDArray ata =
                transpose.mmul(matrix);

        System.out.println("\nA^T * A:");

        System.out.println(ata);


        // --------------------------------------------------
        // POWER ITERATION
        //
        // Estimate largest eigenvalue of A^T A.
        // --------------------------------------------------

        INDArray vector = Nd4j.create(
                new double[]{
                        1.0,
                        1.0,
                        1.0
                }
        ).reshape(3, 1);


        for (int iteration = 0;
             iteration < 100;
             iteration++) {

            vector = ata.mmul(vector);

            double norm =
                    vector.norm2Number()
                            .doubleValue();

            vector =
                    vector.div(norm);
        }


        INDArray numerator =
                vector.transpose()
                        .mmul(ata)
                        .mmul(vector);

        INDArray denominator =
                vector.transpose()
                        .mmul(vector);


        double largestEigenvalue =
                numerator.getDouble(0)
                        /
                denominator.getDouble(0);


        double largestSingularValue =
                Math.sqrt(
                        largestEigenvalue
                );


        // --------------------------------------------------
        // RESULT
        // --------------------------------------------------

        System.out.printf(
                "\nLargest Eigenvalue of A^T A: %.6f%n",
                largestEigenvalue
        );


        System.out.printf(
                "Largest Singular Value: %.6f%n",
                largestSingularValue
        );


        System.out.println(
                "\nInterpretation:"
        );


        System.out.println(
                "SVD decomposes a matrix into important "
                + "directions and their strengths."
        );


        System.out.println(
                "Large singular values represent dominant "
                + "features or information in the data."
        );


        System.out.println(
                "In EcoSort, SVD can be used for feature "
                + "analysis, dimensionality reduction, "
                + "compression and diagnostics."
        );


        System.out.println(
                "\n===== SVD ANALYSIS COMPLETE ====="
        );
    }
}