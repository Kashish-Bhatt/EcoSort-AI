package com.ecosort;

import com.ecosort.fundamentals.ActivationFunctionsDemo;
import com.ecosort.fundamentals.GradientChecker;
import com.ecosort.fundamentals.SVDAnalyzer;
import com.ecosort.fundamentals.TensorOperationsDemo;

import com.ecosort.optimization.OptimizerComparison;
import com.ecosort.optimization.RegularizationExperiment;

import com.ecosort.learning.HebbianLearningDemo;
import com.ecosort.learning.CompetitiveLearningDemo;
import com.ecosort.learning.ErrorCorrectionLearningDemo;
import com.ecosort.learning.BoltzmannLearningDemo;

import com.ecosort.vision.ConvolutionVisualizer;

import com.ecosort.sequence.WasteStreamLSTM;

import com.ecosort.generative.SimpleVAE;

public class SyllabusDemoRunner {

    public static void main(
            String[] args) {

        run(
                "ACTIVATION FUNCTIONS",
                () ->
                        ActivationFunctionsDemo.main(
                                new String[0]
                        )
        );

        run(
                "GRADIENT CHECKING",
                () ->
                        GradientChecker.main(
                                new String[0]
                        )
        );

        run(
                "TENSOR OPERATIONS",
                () ->
                        TensorOperationsDemo.main(
                                new String[0]
                        )
        );

        run(
                "SVD",
                () ->
                        SVDAnalyzer.main(
                                new String[0]
                        )
        );

        run(
                "OPTIMIZERS",
                () ->
                        OptimizerComparison.main(
                                new String[0]
                        )
        );

        run(
                "INITIALIZATION / NORMALIZATION / REGULARIZATION",
                () ->
                        RegularizationExperiment.main(
                                new String[0]
                        )
        );

        run(
                "HEBBIAN LEARNING",
                () ->
                        HebbianLearningDemo.main(
                                new String[0]
                        )
        );

        run(
                "COMPETITIVE LEARNING",
                () ->
                        CompetitiveLearningDemo.main(
                                new String[0]
                        )
        );

        run(
                "ERROR-CORRECTION LEARNING",
                () ->
                        ErrorCorrectionLearningDemo.main(
                                new String[0]
                        )
        );

        run(
                "BOLTZMANN / ENERGY LEARNING",
                () ->
                        BoltzmannLearningDemo.main(
                                new String[0]
                        )
        );

        run(
                "2D / MULTI-CHANNEL CONVOLUTION",
                () ->
                        ConvolutionVisualizer.main(
                                new String[0]
                        )
        );

        run(
                "LSTM STREAM MONITOR",
                () ->
                        WasteStreamLSTM.main(
                                new String[0]
                        )
        );

        run(
                "VARIATIONAL AUTOENCODER",
                () ->
                        SimpleVAE.main(
                                new String[0]
                        )
        );

        System.out.println(
                "\n========================================"
        );

        System.out.println(
                "ECOSORT SYLLABUS DEMONSTRATION COMPLETE"
        );

        System.out.println(
                "========================================"
        );
    }


    private static void run(
            String name,
            Runnable runnable) {

        System.out.println(
                "\n\n########################################"
        );

        System.out.println(
                name
        );

        System.out.println(
                "########################################"
        );

        try {

            runnable.run();

        } catch (Exception exception) {

            System.err.println(
                    "Demo failed: "
                            + exception.getMessage()
            );
        }
    }
}