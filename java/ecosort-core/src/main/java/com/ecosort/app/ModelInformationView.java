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
                "-fx-background-color: #F4F7F5;"
        );

        Label title =
                new Label(
                        "Model Information"
                );

        title.setStyle(
                "-fx-font-size: 26px;"
                        + "-fx-font-weight: bold;"
                        + "-fx-text-fill: #143B30;"
        );

        Label subtitle =
                new Label(
                        "Final EcoSort deep-learning architecture, "
                                + "dataset and evaluation performance"
                );

        subtitle.setStyle(
                "-fx-font-size: 12px;"
                        + "-fx-text-fill: #6A7D76;"
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
                        "DETECTION",
                        "YOLO11s",
                        "960 x 960 object detection"
                );

        VBox segmenter =
                createMetricCard(
                        "SEGMENTATION",
                        "YOLO11s-seg",
                        "640 x 640 instance segmentation"
                );

        VBox classes =
                createMetricCard(
                        "MATERIALS",
                        "4 Classes",
                        "Plastic • Metal • Glass • Paper/Cardboard"
                );

        VBox modelState =
                createMetricCard(
                        "STATUS",
                        "Trained",
                        "Final weights available locally"
                );

        for (VBox card : new VBox[]{
                detector,
                segmenter,
                classes,
                modelState
        }) {

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
                        "Dataset",
                        "EcoSort segmentation dataset: 1,407 images\n"
                                + "Annotated waste instances: 3,414\n"
                                + "Training images: 976\n"
                                + "Validation images: 212\n"
                                + "Test images: 219\n\n"
                                + "Supported material classes:\n"
                                + "Plastic, Metal, Glass and Paper/Cardboard.\n\n"
                                + "The held-out test split is used for "
                                + "final evaluation and is not used for training."
                );


        // =====================================================
        // DETECTOR PERFORMANCE
        // =====================================================

        VBox detectorPerformance =
                createPanel(
                        "Final Detection Performance — Held-Out Test Set",
                        "Model: YOLO11s\n"
                                + "Input resolution: 960 x 960\n"
                                + "Test images: 219\n"
                                + "Test instances: 488\n\n"
                                + "Precision: 47.8%\n"
                                + "Recall: 30.1%\n"
                                + "mAP@50: 26.6%\n"
                                + "mAP@50-95: 20.2%\n\n"
                                + "CPU inference: approximately 760.5 ms/image\n"
                                + "End-to-end throughput: approximately 1.29 FPS"
                );


        // =====================================================
        // DETECTOR CLASS PERFORMANCE
        // =====================================================

        VBox classPerformance =
                createPanel(
                        "Detection Performance by Material",
                        "Plastic\n"
                                + "mAP@50: 31.9%   |   mAP@50-95: 23.7%\n\n"
                                + "Metal\n"
                                + "mAP@50: 45.9%   |   mAP@50-95: 36.9%\n\n"
                                + "Glass\n"
                                + "mAP@50: 1.08%   |   mAP@50-95: 0.82%\n\n"
                                + "Paper/Cardboard\n"
                                + "mAP@50: 27.5%   |   mAP@50-95: 19.4%\n\n"
                                + "Glass is the weakest class in the "
                                + "held-out evaluation and is treated as "
                                + "an identified model limitation."
                );


        // =====================================================
        // SEGMENTATION PERFORMANCE
        // =====================================================

        VBox segmentation =
                createPanel(
                        "Final Instance Segmentation Performance",
                        "Model: YOLO11s-seg\n"
                                + "Input resolution: 640 x 640\n"
                                + "Test images: 219\n"
                                + "Test instances: 488\n\n"
                                + "Mask Precision: 32.4%\n"
                                + "Mask Recall: 32.4%\n"
                                + "Mask mAP@50: 21.0%\n"
                                + "Mask mAP@50-95: 14.5%\n\n"
                                + "The segmentation masks are used to "
                                + "calculate pixel-area material composition, "
                                + "purity and contamination."
                );


        // =====================================================
        // IoU
        // =====================================================

        VBox iou =
                createPanel(
                        "Segmentation IoU Evaluation",
                        "Plastic IoU: 46.98%\n"
                                + "Metal IoU: 45.53%\n"
                                + "Glass IoU: 3.37%\n"
                                + "Paper/Cardboard IoU: 19.82%\n\n"
                                + "Mean Class IoU: 28.92%\n"
                                + "Global IoU: 39.71%\n"
                                + "Mean Image IoU: 37.47%\n\n"
                                + "IoU evaluation throughput: approximately "
                                + "2.07 FPS on CPU."
                );


        // =====================================================
        // BASELINE COMPARISON
        // =====================================================

        VBox comparison =
                createPanel(
                        "Detection Baseline Comparison",
                        "YOLO11n @ 640\n"
                                + "Precision: 38.5%\n"
                                + "Recall: 34.2%\n"
                                + "mAP@50: 30.6%\n"
                                + "mAP@50-95: 19.4%\n\n"
                                + "YOLO11s @ 960 — Final Detector\n"
                                + "Precision: 47.8%\n"
                                + "Recall: 30.1%\n"
                                + "mAP@50: 26.6%\n"
                                + "mAP@50-95: 20.2%\n\n"
                                + "The larger YOLO11s configuration improved "
                                + "precision and mAP@50-95, while the smaller "
                                + "baseline retained higher recall and mAP@50."
                );


        // =====================================================
        // SAFETY / DECISION FUSION
        // =====================================================

        VBox safety =
                createPanel(
                        "Cross-Model Verification",
                        "EcoSort combines object detection and instance "
                                + "segmentation before publishing the final "
                                + "material decision.\n\n"
                                + "If both models agree with sufficient "
                                + "confidence, the supported material result "
                                + "is displayed.\n\n"
                                + "Low-confidence predictions, missing "
                                + "segmentation regions or disagreement between "
                                + "the detection and segmentation models are "
                                + "flagged for MANUAL REVIEW.\n\n"
                                + "This safety layer reduces unsupported "
                                + "automatic recycling decisions but is not "
                                + "a substitute for full open-set or "
                                + "out-of-distribution recognition."
                );


        // =====================================================
        // ARCHITECTURE
        // =====================================================

        VBox architecture =
                createPanel(
                        "Application Architecture",
                        "JavaFX desktop interface\n"
                                + "Java analysis, quality and history services\n"
                                + "Python / Ultralytics inference engine\n"
                                + "YOLO11s object detection\n"
                                + "YOLO11s-seg instance segmentation\n"
                                + "Semantic material maps derived from masks\n"
                                + "Pixel-area purity and contamination analysis\n"
                                + "Cross-model decision verification\n"
                                + "JSON-based Java ↔ Python communication\n"
                                + "Persistent analysis history"
                );


        // =====================================================
        // LIMITATIONS
        // =====================================================

        VBox limitations =
                createPanel(
                        "Current Model Limitations",
                        "EcoSort is trained for four recyclable material "
                                + "classes only: Plastic, Metal, Glass and "
                                + "Paper/Cardboard.\n\n"
                                + "Unsupported materials such as organic waste, "
                                + "textiles and electronic waste are outside the "
                                + "trained class set.\n\n"
                                + "Performance is weaker for Glass and difficult "
                                + "conditions such as clutter, small objects, "
                                + "occlusion and unusual lighting.\n\n"
                                + "Future work includes additional balanced "
                                + "training data, more Glass examples, difficult "
                                + "real-world scenes and explicit unknown-material "
                                + "or open-set recognition."
                );


        getChildren().addAll(
                heading,
                summary,
                dataset,
                detectorPerformance,
                classPerformance,
                segmentation,
                iou,
                comparison,
                safety,
                architecture,
                limitations
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
                        + "-fx-text-fill: #71847C;"
        );

        Label v =
                new Label(value);

        v.setWrapText(true);

        v.setStyle(
                "-fx-font-size: 20px;"
                        + "-fx-font-weight: bold;"
                        + "-fx-text-fill: #173D30;"
        );

        Label d =
                new Label(description);

        d.setWrapText(true);

        d.setStyle(
                "-fx-font-size: 10px;"
                        + "-fx-text-fill: #899890;"
        );

        VBox card =
                new VBox(
                        7,
                        h,
                        v,
                        d
                );

        card.setPadding(
                new Insets(16)
        );

        card.setMinHeight(115);
        card.setMaxWidth(Double.MAX_VALUE);

        card.setStyle(
                "-fx-background-color: white;"
                        + "-fx-background-radius: 12;"
                        + "-fx-border-color: #DCE5E1;"
                        + "-fx-border-radius: 12;"
        );

        return card;
    }


    private VBox createPanel(
            String title,
            String text) {

        Label heading =
                new Label(title);

        heading.setStyle(
                "-fx-font-size: 15px;"
                        + "-fx-font-weight: bold;"
                        + "-fx-text-fill: #233C33;"
        );

        Label body =
                new Label(text);

        body.setWrapText(true);

        body.setStyle(
                "-fx-font-size: 12px;"
                        + "-fx-text-fill: #687A73;"
                        + "-fx-line-spacing: 3;"
        );

        VBox panel =
                new VBox(
                        8,
                        heading,
                        body
                );

        panel.setPadding(
                new Insets(18)
        );

        panel.setStyle(
                "-fx-background-color: white;"
                        + "-fx-background-radius: 12;"
                        + "-fx-border-color: #DCE5E1;"
                        + "-fx-border-radius: 12;"
        );

        return panel;
    }
}