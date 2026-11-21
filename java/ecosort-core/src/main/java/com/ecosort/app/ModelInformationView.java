package com.ecosort.app;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class ModelInformationView extends VBox {

    public ModelInformationView() {

        setSpacing(16);
        setPadding(new Insets(28));

        setStyle(
                "-fx-background-color: #F2F7F3;"
        );

        Label title =
                new Label("Model Information");

        title.setStyle(
                "-fx-font-size: 28px;"
                        + "-fx-font-weight: bold;"
                        + "-fx-text-fill: #0E4938;"
        );

        Label subtitle =
                new Label(
                        "EcoSort V2 • Deep-learning architecture, "
                                + "dataset and held-out evaluation"
                );

        subtitle.setStyle(
                "-fx-font-size: 12px;"
                        + "-fx-text-fill: #668078;"
        );

        VBox heading =
                new VBox(
                        4,
                        title,
                        subtitle
                );


        // =====================================================
        // SUMMARY CARDS
        // =====================================================

        HBox summary =
                new HBox(14);

        VBox detector =
                createMetricCard(
                        "DETECTOR",
                        "YOLO11s",
                        "960 px object detection"
                );

        VBox segmenter =
                createMetricCard(
                        "SEGMENTER",
                        "YOLO11s-seg",
                        "640 px instance segmentation"
                );

        VBox classes =
                createMetricCard(
                        "MATERIALS",
                        "4 Classes",
                        "Plastic • Metal • Glass • Paper/Cardboard"
                );

        VBox modelState =
                createMetricCard(
                        "MODEL",
                        "EcoSort V2",
                        "Expanded-data experiment"
                );

        for (
                VBox card :
                new VBox[]{
                        detector,
                        segmenter,
                        classes,
                        modelState
                }
        ) {

            HBox.setHgrow(
                    card,
                    Priority.ALWAYS
            );
        }

        summary.getChildren().addAll(
                detector,
                segmenter,
                classes,
                modelState
        );


        // =====================================================
        // DATASET
        // =====================================================

        VBox dataset =
                createPanel(
                        "Dataset & Preparation",

                        "Version 1 source: TACO "
                                + "(Trash Annotations in Context)\n"
                                + "Version 2 expansion: Segmented TrashNet\n\n"

                                + "V2 training images: 2,636\n"
                                + "V2 training annotations: 4,120\n"
                                + "Validation images: 675\n"
                                + "Validation annotations: 956\n"
                                + "Held-out test images: 219\n"
                                + "Held-out test annotations: 488\n\n"

                                + "Supported classes: Plastic, Metal, Glass "
                                + "and Paper/Cardboard.\n\n"

                                + "The original 219-image test set was kept "
                                + "unchanged between V1 and V2 so both "
                                + "versions could be compared on the same data."
                );


        // =====================================================
        // DETECTION RESULTS
        // =====================================================

        VBox detection =
                createPanel(
                        "V2 Detection — Held-Out Test Results",

                        "Model: YOLO11s @ 960 px\n\n"

                                + "Precision: 35.14%\n"
                                + "Recall: 36.39%\n"
                                + "mAP@50: 32.20%\n"
                                + "mAP@50–95: 25.38%\n\n"

                                + "V1 → V2 comparison\n"
                                + "mAP@50: 26.60% → 32.20%\n"
                                + "mAP@50–95: 20.20% → 25.38%\n"
                                + "Recall: 30.10% → 36.39%\n\n"

                                + "V2 improved recall and both mAP metrics, "
                                + "while precision decreased. This represents "
                                + "a measurable precision–recall trade-off."
                );


        // =====================================================
        // SEGMENTATION RESULTS
        // =====================================================

        VBox segmentation =
                createPanel(
                        "V2 Instance Segmentation — Held-Out Test Results",

                        "Model: YOLO11s-seg @ 640 px\n\n"

                                + "Mask Precision: 38.7%\n"
                                + "Mask Recall: 31.2%\n"
                                + "Mask mAP@50: 29.4%\n"
                                + "Mask mAP@50–95: 20.2%\n\n"

                                + "V1 → V2 comparison\n"
                                + "Mask mAP@50: 21.0% → 29.4%\n"
                                + "Mask mAP@50–95: 14.5% → 20.2%"
                );


        // =====================================================
        // IOU
        // =====================================================

        VBox iou =
                createPanel(
                        "Segmentation IoU Analysis",

                        "Plastic IoU: 53.74%\n"
                                + "Metal IoU: 34.71%\n"
                                + "Glass IoU: 1.80%\n"
                                + "Paper/Cardboard IoU: 21.48%\n\n"

                                + "Mean Class IoU: 27.93%\n"
                                + "Global IoU: 42.17%\n"
                                + "Mean Image IoU: 42.48%\n\n"

                                + "Average local segmentation inference: "
                                + "584.18 ms\n"
                                + "Measured throughput: 1.71 FPS"
                );


        // =====================================================
        // ERROR ANALYSIS
        // =====================================================

        VBox limitations =
                createPanel(
                        "Error Analysis & Limitations",

                        "Glass remains the weakest material class. "
                                + "Transparent surfaces, reflections, limited "
                                + "class diversity and domain differences "
                                + "make Glass difficult to recognize reliably.\n\n"

                                + "Dataset expansion improved overall detection "
                                + "and segmentation mAP, Global IoU and Mean "
                                + "Image IoU, but improvements were not uniform "
                                + "across every class.\n\n"

                                + "EcoSort is a closed-set four-class system. "
                                + "Unsupported or ambiguous waste may still be "
                                + "mapped to a known class. Cross-model "
                                + "verification therefore sends unreliable or "
                                + "disagreeing predictions to MANUAL REVIEW."
                );


        // =====================================================
        // ARCHITECTURE
        // =====================================================

        VBox architecture =
                createPanel(
                        "Application Architecture",

                        "Waste image\n"
                                + "↓\n"
                                + "YOLO11s object detection + "
                                + "YOLO11s-seg instance segmentation\n"
                                + "↓\n"
                                + "Cross-model decision fusion\n"
                                + "↓\n"
                                + "Material composition, purity and contamination\n"
                                + "↓\n"
                                + "Recycling recommendation / Manual Review\n\n"

                                + "Python + Ultralytics perform inference.\n"
                                + "Java services launch the Python pipelines "
                                + "and receive JSON results.\n"
                                + "JavaFX provides the desktop interface and "
                                + "analysis-history workflow."
                );


        // =====================================================
        // TECHNICAL COMPONENTS
        // =====================================================

        VBox technical =
                createPanel(
                        "Deep-Learning Components",

                        "Transfer learning • CNN-based detection • "
                                + "Instance segmentation • Semantic material maps\n\n"

                                + "MLP • Forward propagation • Backpropagation • "
                                + "Gradient checking • Tensor operations • SVD\n\n"

                                + "Optimization • Regularization • Dropout • "
                                + "Hebbian learning • Competitive learning • "
                                + "Error-correction learning\n\n"

                                + "2D and multi-channel convolution • "
                                + "LSTM sequence modelling • "
                                + "Variational Autoencoder and latent space"
                );


        getChildren().addAll(
                heading,
                summary,
                dataset,
                detection,
                segmentation,
                iou,
                limitations,
                architecture,
                technical
        );
    }


    private VBox createMetricCard(
            String heading,
            String value,
            String description) {

        Label h =
                new Label(heading);

        h.setStyle(
                "-fx-font-size: 10px;"
                        + "-fx-font-weight: bold;"
                        + "-fx-text-fill: #6D8179;"
        );

        Label v =
                new Label(value);

        v.setStyle(
                "-fx-font-size: 20px;"
                        + "-fx-font-weight: bold;"
                        + "-fx-text-fill: #0E4938;"
        );

        Label d =
                new Label(description);

        d.setWrapText(true);

        d.setStyle(
                "-fx-font-size: 10px;"
                        + "-fx-text-fill: #87978F;"
        );

        VBox card =
                new VBox(
                        7,
                        h,
                        v,
                        d
                );

        card.setPadding(
                new Insets(17)
        );

        card.setMinHeight(115);
        card.setMaxWidth(Double.MAX_VALUE);

        card.setStyle(
                "-fx-background-color: white;"
                        + "-fx-background-radius: 14;"
                        + "-fx-border-color: #D5E4DC;"
                        + "-fx-border-radius: 14;"
                        + "-fx-effect: dropshadow("
                        + "gaussian, rgba(20,70,50,0.08), "
                        + "10, 0, 0, 3);"
        );

        return card;
    }


    private VBox createPanel(
            String title,
            String text) {

        Label heading =
                new Label(title);

        heading.setStyle(
                "-fx-font-size: 16px;"
                        + "-fx-font-weight: bold;"
                        + "-fx-text-fill: #123F32;"
        );

        Label body =
                new Label(text);

        body.setWrapText(true);

        body.setStyle(
                "-fx-font-size: 12px;"
                        + "-fx-text-fill: #61766D;"
                        + "-fx-line-spacing: 3;"
        );

        VBox panel =
                new VBox(
                        9,
                        heading,
                        body
                );

        panel.setPadding(
                new Insets(20)
        );

        panel.setStyle(
                "-fx-background-color: white;"
                        + "-fx-background-radius: 14;"
                        + "-fx-border-color: #D7E5DE;"
                        + "-fx-border-radius: 14;"
                        + "-fx-effect: dropshadow("
                        + "gaussian, rgba(20,70,50,0.06), "
                        + "9, 0, 0, 2);"
        );

        return panel;
    }
}