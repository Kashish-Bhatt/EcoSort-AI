package com.ecosort.fundamentals;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

public class TensorOperationsDemo {

    public static void main(String[] args) {

        System.out.println(
                "\n========================================"
        );

        System.out.println(
                "ECOSORT TENSOR OPERATIONS DEMONSTRATION"
        );

        System.out.println(
                "========================================"
        );


        // =====================================================
        // 1. TENSOR CREATION
        // =====================================================

        INDArray wasteMatrix =
                Nd4j.create(
                        new double[][]{
                                {70, 10, 5, 15},
                                {20, 60, 10, 10},
                                {10, 10, 10, 70}
                        }
                );

        System.out.println(
                "\n1. Tensor creation"
        );

        System.out.println(
                "Waste composition matrix:"
        );

        System.out.println(
                wasteMatrix
        );

        System.out.println(
                "Shape: "
                        + java.util.Arrays.toString(
                        wasteMatrix.shape()
                )
        );


        // =====================================================
        // 2. ELEMENT-WISE OPERATIONS
        // =====================================================

        INDArray normalized =
                wasteMatrix.div(100.0);

        System.out.println(
                "\n2. Element-wise division / normalization"
        );

        System.out.println(
                normalized
        );


        // =====================================================
        // 3. BROADCASTING
        // =====================================================

        INDArray adjustment =
                Nd4j.create(
                        new double[]{
                                1.0,
                                2.0,
                                3.0,
                                4.0
                        }
                );

        INDArray broadcastResult =
                wasteMatrix.addRowVector(
                        adjustment
                );

        System.out.println(
                "\n3. Broadcasting"
        );

        System.out.println(
                "Row vector:"
        );

        System.out.println(
                adjustment
        );

        System.out.println(
                "Matrix + broadcast row:"
        );

        System.out.println(
                broadcastResult
        );


        // =====================================================
        // 4. INNER PRODUCT
        // =====================================================

        INDArray observationA =
                Nd4j.create(
                        new double[]{
                                0.7,
                                0.1,
                                0.05,
                                0.15
                        }
                );

        INDArray observationB =
                Nd4j.create(
                        new double[]{
                                0.6,
                                0.15,
                                0.1,
                                0.15
                        }
                );

        double innerProduct =
                observationA
                        .mul(observationB)
                        .sumNumber()
                        .doubleValue();

        System.out.println(
                "\n4. Inner product"
        );

        System.out.printf(
                "A · B = %.6f%n",
                innerProduct
        );


        // =====================================================
        // 5. RESHAPING
        // =====================================================

        INDArray flat =
                Nd4j.create(
                        new double[]{
                                1,
                                2,
                                3,
                                4,
                                5,
                                6,
                                7,
                                8
                        }
                );

        INDArray reshaped =
                flat.reshape(
                        2,
                        4
                );

        System.out.println(
                "\n5. Tensor reshaping"
        );

        System.out.println(
                "Original:"
        );

        System.out.println(
                flat
        );

        System.out.println(
                "Reshaped 2 x 4:"
        );

        System.out.println(
                reshaped
        );


        // =====================================================
        // 6. MATRIX MULTIPLICATION
        // =====================================================

        INDArray matrixA =
                Nd4j.create(
                        new double[][]{
                                {1, 2},
                                {3, 4}
                        }
                );

        INDArray matrixB =
                Nd4j.create(
                        new double[][]{
                                {5, 6},
                                {7, 8}
                        }
                );

        INDArray multiplication =
                matrixA.mmul(
                        matrixB
                );

        System.out.println(
                "\n6. Matrix multiplication"
        );

        System.out.println(
                multiplication
        );


        // =====================================================
        // 7. TRANSPOSE
        // =====================================================

        INDArray transpose =
                wasteMatrix.transpose();

        System.out.println(
                "\n7. Transpose"
        );

        System.out.println(
                transpose
        );


        // =====================================================
        // 8. REDUCTION
        // =====================================================

        INDArray columnMeans =
                wasteMatrix.mean(0);

        System.out.println(
                "\n8. Column-wise mean"
        );

        System.out.println(
                columnMeans
        );


        // =====================================================
        // 9. SIMPLE 3-D TENSOR
        // =====================================================

        INDArray tensor3D =
                Nd4j.arange(
                        24
                ).reshape(
                        2,
                        3,
                        4
                );

        System.out.println(
                "\n9. 3-D tensor"
        );

        System.out.println(
                tensor3D
        );

        System.out.println(
                "3-D shape: "
                        + java.util.Arrays.toString(
                        tensor3D.shape()
                )
        );


        System.out.println(
                "\n========================================"
        );

        System.out.println(
                "TENSOR DEMONSTRATION COMPLETE"
        );

        System.out.println(
                "========================================"
        );
    }
}