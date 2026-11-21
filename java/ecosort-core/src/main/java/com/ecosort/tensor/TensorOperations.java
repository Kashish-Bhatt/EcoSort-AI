package com.ecosort.tensor;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

public class TensorOperations {

    public static void main(String[] args) {

        System.out.println(
                "===== EcoSort Tensor Mathematics ====="
        );

        // --------------------------------------------------
        // 1. VECTOR CREATION
        // --------------------------------------------------

        INDArray vector = Nd4j.create(
                new double[]{1, 2, 3}
        );

        System.out.println("\n1. Vector:");
        System.out.println(vector);


        // --------------------------------------------------
        // 2. MATRIX CREATION
        // --------------------------------------------------

        INDArray matrix = Nd4j.create(
                new double[][]{
                        {1, 2, 3},
                        {4, 5, 6}
                }
        );

        System.out.println("\n2. Matrix:");
        System.out.println(matrix);


        // --------------------------------------------------
        // 3. MATRIX SHAPE
        // --------------------------------------------------

        System.out.println("\n3. Matrix shape:");

        long[] shape = matrix.shape();

        System.out.println(
                shape[0] + " x " + shape[1]
        );


        // --------------------------------------------------
        // 4. RESHAPING
        // --------------------------------------------------

        INDArray reshaped =
                matrix.reshape(3, 2);

        System.out.println("\n4. Reshaped matrix (3 x 2):");
        System.out.println(reshaped);


        // --------------------------------------------------
        // 5. BROADCASTING
        // --------------------------------------------------

        INDArray bias = Nd4j.create(
                new double[]{10, 20, 30}
        );

        INDArray broadcastResult =
                matrix.addRowVector(bias);

        System.out.println("\n5. Broadcasting:");

        System.out.println("Original matrix:");
        System.out.println(matrix);

        System.out.println("Bias vector:");
        System.out.println(bias);

        System.out.println("Matrix + Bias:");
        System.out.println(broadcastResult);


        // --------------------------------------------------
        // 6. INNER PRODUCT
        // --------------------------------------------------

        INDArray vectorA = Nd4j.create(
                new double[]{1, 2, 3}
        );

        INDArray vectorB = Nd4j.create(
                new double[]{4, 5, 6}
        );

        double innerProduct =
                vectorA.mul(vectorB)
                        .sumNumber()
                        .doubleValue();

        System.out.println("\n6. Inner Product:");

        System.out.println(
                "[1,2,3] dot [4,5,6] = "
                        + innerProduct
        );


        // --------------------------------------------------
        // 7. MATRIX MULTIPLICATION
        // --------------------------------------------------

        INDArray matrixA = Nd4j.create(
                new double[][]{
                        {1, 2},
                        {3, 4}
                }
        );

        INDArray matrixB = Nd4j.create(
                new double[][]{
                        {5, 6},
                        {7, 8}
                }
        );

        INDArray multiplication =
                matrixA.mmul(matrixB);

        System.out.println(
                "\n7. Matrix Multiplication:"
        );

        System.out.println(multiplication);


        // --------------------------------------------------
        // 8. ELEMENT-WISE OPERATIONS
        // --------------------------------------------------

        INDArray elementWise =
                matrixA.mul(matrixB);

        System.out.println(
                "\n8. Element-wise Multiplication:"
        );

        System.out.println(elementWise);


        // --------------------------------------------------
        // COMPLETE
        // --------------------------------------------------

        System.out.println(
                "\n===== Tensor Operations Complete ====="
        );
    }
}