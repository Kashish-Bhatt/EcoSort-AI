package com.ecosort.app;

import com.ecosort.history.AnalysisHistoryService;

import com.ecosort.inference.InferenceService;
import com.ecosort.inference.InferenceService.Detection;
import com.ecosort.inference.InferenceService.InferenceResult;

import com.ecosort.inference.SegmentationService;
import com.ecosort.inference.SegmentationService.SegmentationResult;

import com.ecosort.quality.RecyclingQualityAnalyzer.Material;

import java.io.File;

import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;

import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.ScrollPane;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;

import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import javafx.stage.FileChooser;
import javafx.stage.Stage;


public class AnalyzeWasteView extends ScrollPane {

    // =========================================================
    // SERVICES
    // =========================================================

    private final Stage stage;

    private final InferenceService inferenceService;

    private final SegmentationService segmentationService =
            new SegmentationService();

    private final AnalysisHistoryService historyService;


    // =========================================================
    // CURRENT IMAGE
    // =========================================================

    private File selectedImage;


    // =========================================================
    // UPLOAD UI
    // =========================================================

    private VBox emptyUploadView;

    private HBox selectedImageView;

    private ImageView imagePreview;

    private Label selectedFileName;

    private Label selectedFileDetails;

    private ProgressIndicator progressIndicator;


    // =========================================================
    // BUTTONS
    // =========================================================

    private Button analyzeButton;

    private Button changeButton;

    private Button clearButton;

    private Button cancelButton;


    // =========================================================
    // STATUS
    // =========================================================

    private Label systemStatus;


    // =========================================================
    // RESULT CARDS
    // =========================================================

    private Label materialValue;

    private Label purityValue;

    private Label contaminationValue;

    private Label confidenceValue;


    // =========================================================
    // RESULT PANELS
    // =========================================================

    private Label detectionSummary;

    private Label compositionText;

    private Label recommendationText;

    private Label inferenceTimeLabel;

    private Label modelDecisionText;

    private InferenceResult latestDetectionResult;

    private Task<?> activeAnalysisTask;

    private boolean analysisCancelled;



    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    public AnalyzeWasteView(
            Stage stage,
            InferenceService inferenceService,
            AnalysisHistoryService historyService) {

        this.stage =
                stage;

        this.inferenceService =
                inferenceService;

        this.historyService =
                historyService;


        VBox content =
                createContent();


        setContent(
                content
        );


        setFitToWidth(
                true
        );


        setHbarPolicy(
                ScrollBarPolicy.NEVER
        );


        setVbarPolicy(
                ScrollBarPolicy.AS_NEEDED
        );


        setPannable(
                true
        );


        setStyle(
                "-fx-background: #F1F7F2;"
                        + "-fx-background-color: #F1F7F2;"
                        + "-fx-border-color: transparent;"
        );
    }


    // =========================================================
    // MAIN CONTENT
    // =========================================================

    private VBox createContent() {

        HBox header =
                createHeader();


        HBox ecoBanner =
                createEcoBanner();


        StackPane uploadCard =
                createUploadCard();


        HBox resultCards =
                createResultCards();


        VBox modelDecisionPanel =
                createModelDecisionPanel();


        VBox detectionPanel =
                createDetectionPanel();


        VBox compositionPanel =
                createCompositionPanel();


        VBox recommendationPanel =
                createRecommendationPanel();


        Region bottomSpace =
                new Region();


        bottomSpace.setPrefHeight(
                30
        );


        VBox content =
                new VBox(
                        18,
                        header,
                        ecoBanner,
                        uploadCard,
                        resultCards,
                        modelDecisionPanel,
                        detectionPanel,
                        compositionPanel,
                        recommendationPanel,
                        bottomSpace
                );


        content.setPadding(
                new Insets(
                        28,
                        34,
                        32,
                        34
                )
        );


        content.setStyle(
                "-fx-background-color: #F1F7F2;"
        );


        return content;
    }


    // =========================================================
    // HEADER
    // =========================================================

    private HBox createHeader() {

        Label title =
                new Label(
                        "EcoSort Intelligence Dashboard"
                );


        title.setStyle(
                "-fx-font-size: 30px;"
                        + "-fx-font-weight: bold;"
                        + "-fx-text-fill: #123D2D;"
        );


        Label subtitle =
                new Label(
                        "AI-powered recyclable material detection, segmentation and recycling quality assessment"
                );


        subtitle.setStyle(
                "-fx-font-size: 13px;"
                        + "-fx-text-fill: #5E756B;"
        );


        VBox heading =
                new VBox(
                        4,
                        title,
                        subtitle
                );


        systemStatus =
                new Label();


        setReadyStatus();


        Region spacer =
                new Region();


        HBox.setHgrow(
                spacer,
                Priority.ALWAYS
        );


        HBox header =
                new HBox(
                        heading,
                        spacer,
                        systemStatus
                );


        header.setAlignment(
                Pos.CENTER_LEFT
        );


        return header;
    }


    private HBox createEcoBanner() {

        Label leaf = new Label("❧");
        leaf.setStyle(
                "-fx-font-size: 34px;"
                        + "-fx-text-fill: #D7F3E1;"
        );

        Label heading = new Label(
                "Smarter sorting. Cleaner recycling."
        );
        heading.setStyle(
                "-fx-font-size: 18px;"
                        + "-fx-font-weight: bold;"
                        + "-fx-text-fill: white;"
        );

        Label text = new Label(
                "Detection + instance segmentation + cross-model verification"
        );
        text.setStyle(
                "-fx-font-size: 11px;"
                        + "-fx-text-fill: #D8EEE2;"
        );

        VBox copy = new VBox(3, heading, text);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label version = new Label("ECOSORT V2  •  4 MATERIAL CLASSES");
        version.setStyle(
                "-fx-background-color: rgba(255,255,255,0.14);"
                        + "-fx-text-fill: white;"
                        + "-fx-font-size: 10px;"
                        + "-fx-font-weight: bold;"
                        + "-fx-padding: 8 13;"
                        + "-fx-background-radius: 16;"
        );

        HBox banner = new HBox(14, leaf, copy, spacer, version);
        banner.setAlignment(Pos.CENTER_LEFT);
        banner.setPadding(new Insets(16, 20, 16, 20));
        banner.setStyle(
                "-fx-background-color: linear-gradient(to right, #123F2D, #1F7652);"
                        + "-fx-background-radius: 16;"
                        + "-fx-effect: dropshadow(gaussian, rgba(20,70,48,0.14), 16, 0.15, 0, 4);"
        );

        return banner;
    }


    // =========================================================
    // UPLOAD CARD
    // =========================================================

    private StackPane createUploadCard() {

        emptyUploadView =
                createEmptyUploadView();


        selectedImageView =
                createSelectedImageView();


        selectedImageView.setVisible(
                false
        );


        selectedImageView.setManaged(
                false
        );


        StackPane card =
                new StackPane(
                        emptyUploadView,
                        selectedImageView
                );


        card.setMinHeight(
                270
        );


        card.setPrefHeight(
                270
        );


        card.setPadding(
                new Insets(18)
        );


        card.setStyle(
                "-fx-background-color: linear-gradient(to bottom right, #FFFFFF, #F5FBF6);"
                        + "-fx-background-radius: 18;"
                        + "-fx-border-color: #CFE3D7;"
                        + "-fx-border-radius: 18;"
                        + "-fx-effect: dropshadow(gaussian, rgba(20,70,48,0.10), 18, 0.15, 0, 5);"
        );


        // =====================================================
        // DRAG OVER
        // =====================================================

        card.setOnDragOver(
                event -> {

                    Dragboard board =
                            event.getDragboard();


                    if (
                            board.hasFiles()
                                    && isValidImage(
                                    board
                                            .getFiles()
                                            .get(0)
                            )
                    ) {

                        event.acceptTransferModes(
                                TransferMode.COPY
                        );
                    }


                    event.consume();
                }
        );


        // =====================================================
        // DRAG DROP
        // =====================================================

        card.setOnDragDropped(
                event -> {

                    Dragboard board =
                            event.getDragboard();


                    boolean success =
                            false;


                    if (
                            board.hasFiles()
                                    && isValidImage(
                                    board
                                            .getFiles()
                                            .get(0)
                            )
                    ) {

                        loadImage(
                                board
                                        .getFiles()
                                        .get(0)
                        );


                        success =
                                true;
                    }


                    event.setDropCompleted(
                            success
                    );


                    event.consume();
                }
        );


        return card;
    }


    // =========================================================
    // EMPTY UPLOAD VIEW
    // =========================================================

    private VBox createEmptyUploadView() {

        Label icon =
                new Label(
                        "♻  ECO"
                );


        icon.setStyle(
                "-fx-font-size: 34px;"
                        + "-fx-text-fill: #1B7A50;"
        );


        Label title =
                new Label(
                        "Drop a waste image to begin"
                );


        title.setStyle(
                "-fx-font-size: 17px;"
                        + "-fx-font-weight: bold;"
                        + "-fx-text-fill: #1E3C32;"
        );


        Label text =
                new Label(
                        "Drag & drop a JPG, JPEG or PNG image "
                                + "here, or select one from your computer."
                );


        text.setStyle(
                "-fx-font-size: 12px;"
                        + "-fx-text-fill: #61766D;"
        );


        Button chooseButton =
                createPrimaryButton(
                        "Choose Image"
                );


        chooseButton.setOnAction(
                event ->
                        chooseImage()
        );


        VBox box =
                new VBox(
                        9,
                        icon,
                        title,
                        text,
                        chooseButton
                );


        box.setAlignment(
                Pos.CENTER
        );


        return box;
    }


    // =========================================================
    // SELECTED IMAGE VIEW
    // =========================================================

    private HBox createSelectedImageView() {

        imagePreview =
                new ImageView();


        imagePreview.setPreserveRatio(
                true
        );


        imagePreview.setFitWidth(
                470
        );


        imagePreview.setFitHeight(
                215
        );


        progressIndicator =
                new ProgressIndicator();


        progressIndicator.setMaxSize(
                42,
                42
        );


        progressIndicator.setVisible(
                false
        );


        StackPane imageContainer =
                new StackPane(
                        imagePreview,
                        progressIndicator
                );


        imageContainer.setPrefWidth(
                500
        );


        imageContainer.setMinWidth(
                420
        );


        imageContainer.setStyle(
                "-fx-background-color: #E7F2EA;"
                        + "-fx-background-radius: 14;"
                        + "-fx-border-color: #C8DDD0;"
                        + "-fx-border-radius: 14;"
        );


        Label readyLabel =
                new Label(
                        "●  IMAGE READY"
                );


        readyLabel.setStyle(
                "-fx-text-fill: #16794B;"
                        + "-fx-font-size: 10px;"
                        + "-fx-font-weight: bold;"
        );


        Label title =
                new Label(
                        "Ready for intelligent waste analysis"
                );


        title.setStyle(
                "-fx-font-size: 17px;"
                        + "-fx-font-weight: bold;"
                        + "-fx-text-fill: #1C3C31;"
        );


        selectedFileName =
                new Label();


        selectedFileName.setWrapText(
                true
        );


        selectedFileName.setStyle(
                "-fx-font-size: 12px;"
                        + "-fx-text-fill: #415B51;"
                        + "-fx-font-weight: bold;"
        );


        selectedFileDetails =
                new Label();


        selectedFileDetails.setStyle(
                "-fx-font-size: 10px;"
                        + "-fx-text-fill: #7A8C85;"
        );


        analyzeButton =
                createPrimaryButton(
                        "Analyze Waste"
                );


        changeButton =
                createSecondaryButton(
                        "Change Image"
                );


        clearButton =
                createSecondaryButton(
                        "Clear"
                );


        cancelButton =
                createSecondaryButton(
                        "Cancel Analysis"
                );


        cancelButton.setVisible(
                false
        );


        cancelButton.setManaged(
                false
        );


        analyzeButton.setOnAction(
                event ->
                        analyzeSelectedImage()
        );


        changeButton.setOnAction(
                event ->
                        chooseImage()
        );


        clearButton.setOnAction(
                event ->
                        clearImage()
        );


        cancelButton.setOnAction(
                event ->
                        cancelAnalysis()
        );


        HBox actions =
                new HBox(
                        9,
                        analyzeButton,
                        changeButton,
                        clearButton,
                        cancelButton
                );


        VBox details =
                new VBox(
                        7,
                        readyLabel,
                        title,
                        selectedFileName,
                        selectedFileDetails,
                        actions
                );


        details.setAlignment(
                Pos.CENTER_LEFT
        );


        HBox.setHgrow(
                details,
                Priority.ALWAYS
        );


        HBox selected =
                new HBox(
                        22,
                        imageContainer,
                        details
                );


        selected.setAlignment(
                Pos.CENTER_LEFT
        );


        return selected;
    }


    // =========================================================
    // RESULT CARDS
    // =========================================================

    private HBox createResultCards() {

        materialValue =
                createValueLabel(
                        "—"
                );


        purityValue =
                createValueLabel(
                        "—"
                );


        contaminationValue =
                createValueLabel(
                        "—"
                );


        confidenceValue =
                createValueLabel(
                        "—"
                );


        VBox materialCard =
                createResultCard(
                        "PRIMARY MATERIAL",
                        materialValue,
                        "Dominant detected material"
                );


        VBox purityCard =
                createResultCard(
                        "PURITY SCORE",
                        purityValue,
                        "Mask-area consistency"
                );


        VBox contaminationCard =
                createResultCard(
                        "CONTAMINATION",
                        contaminationValue,
                        "Secondary material area"
                );


        VBox confidenceCard =
                createResultCard(
                        "DETECTION CONFIDENCE",
                        confidenceValue,
                        "Prediction certainty"
                );


        HBox cards =
                new HBox(
                        14,
                        materialCard,
                        purityCard,
                        contaminationCard,
                        confidenceCard
                );


        for (
                VBox card
                : new VBox[]{
                materialCard,
                purityCard,
                contaminationCard,
                confidenceCard
        }
        ) {

            HBox.setHgrow(
                    card,
                    Priority.ALWAYS
            );
        }


        return cards;
    }


    // =========================================================
    // MODEL DECISION PANEL
    // =========================================================

    private VBox createModelDecisionPanel() {

        Label title =
                createPanelTitle(
                        "Model Decision"
                );


        modelDecisionText =
                createBodyLabel(
                        "Analyze an image to compare detection and segmentation decisions."
                );


        return createPanel(
                title,
                modelDecisionText
        );
    }


    // =========================================================
    // DETECTED OBJECTS PANEL
    // =========================================================

    private VBox createDetectionPanel() {

        Label title =
                createPanelTitle(
                        "Detected Objects"
                );


        detectionSummary =
                createBodyLabel(
                        "No image has been analyzed yet."
                );


        inferenceTimeLabel =
                new Label();


        inferenceTimeLabel.setStyle(
                "-fx-font-size: 10px;"
                        + "-fx-text-fill: #8A9892;"
        );


        return createPanel(
                title,
                detectionSummary,
                inferenceTimeLabel
        );
    }


    // =========================================================
    // COMPOSITION PANEL
    // =========================================================

    private VBox createCompositionPanel() {

        Label title =
                createPanelTitle(
                        "Material Composition"
                );


        compositionText =
                createBodyLabel(
                        "Analyze an image to view material composition."
                );


        return createPanel(
                title,
                compositionText
        );
    }


    // =========================================================
    // RECOMMENDATION PANEL
    // =========================================================

    private VBox createRecommendationPanel() {

        Label title =
                createPanelTitle(
                        "Recycling Recommendation"
                );


        recommendationText =
                createBodyLabel(
                        "Analyze an image to receive recycling guidance."
                );


        return createPanel(
                title,
                recommendationText
        );
    }


    // =========================================================
    // ANALYSIS PIPELINE
    // =========================================================

    private void analyzeSelectedImage() {

        if (selectedImage == null) {
            return;
        }


        File imageToAnalyze =
                selectedImage;


        analysisCancelled =
                false;


        setProcessingState(
                true
        );


        Task<InferenceResult> detectionTask =
                new Task<>() {

                    @Override
                    protected InferenceResult call()
                            throws Exception {

                        return inferenceService
                                .analyze(
                                        imageToAnalyze
                                );
                    }
                };


        activeAnalysisTask =
                detectionTask;


        detectionTask.setOnSucceeded(
                event -> {

                    if (analysisCancelled || detectionTask.isCancelled()) {
                        return;
                    }


                    InferenceResult detection =
                            detectionTask.getValue();


                    displayDetectionResult(
                            detection
                    );


                    if (
                            segmentationService
                                    .isModelAvailable()
                    ) {

                        runSegmentation(
                                imageToAnalyze,
                                detection
                        );

                    } else {

                        setProcessingState(
                                false
                        );


                        purityValue.setText(
                                "Awaiting model"
                        );


                        contaminationValue.setText(
                                "Awaiting model"
                        );


                        saveHistory(
                                imageToAnalyze,
                                detection,
                                null
                        );


                        activeAnalysisTask = null;
                        setCompleteStatus();
                    }
                }
        );


        detectionTask.setOnFailed(
                event -> {

                    if (analysisCancelled || detectionTask.isCancelled()) {
                        return;
                    }


                    setProcessingState(
                            false
                    );


                    setErrorStatus();


                    Throwable error =
                            detectionTask
                                    .getException();


                    showInformation(
                            "Analysis Failed",
                            error == null
                                    ? "Unknown inference error."
                                    : error.getMessage()
                    );
                }
        );


        Thread thread =
                new Thread(
                        detectionTask,
                        "ecosort-detection"
                );


        thread.setDaemon(
                true
        );


        thread.start();
    }


    // =========================================================
    // DISPLAY DETECTION RESULT
    // =========================================================

    private void displayDetectionResult(
            InferenceResult result) {
                latestDetectionResult = result;

        if (result.getDetectionCount() == 0) {
            modelDecisionText.setText(
                    "Detection Model: No supported material detected\n"
                            + "Segmentation Model: Running...\n"
                            + "Final Decision: Pending segmentation\n"
                            + "Reason: Waiting for cross-model verification"
            );
        } else {
            modelDecisionText.setText(
                    "Detection Model: "
                            + displayMaterial(result.getHighestMaterial())
                            + " ("
                            + String.format("%.1f%%", result.getHighestConfidence() * 100.0)
                            + ")\n"
                            + "Segmentation Model: Running...\n"
                            + "Final Decision: Pending segmentation\n"
                            + "Reason: Waiting for cross-model verification"
            );
        }

        // Detection is only an intermediate result.
        // Keep the final-decision cards pending until segmentation completes.
        materialValue.setText(
                "Analyzing..."
        );

        purityValue.setText(
                "Analyzing..."
        );

        contaminationValue.setText(
                "Analyzing..."
        );

        if (
                result.getDetectionCount()
                        == 0
        ) {

            confidenceValue.setText(
                    "0.0%"
            );

        } else {

            confidenceValue.setText(
                    String.format(
                            "%.1f%%",
                            result
                                    .getHighestConfidence()
                                    * 100.0
                    )
            );
        }


        // =====================================================
        // ANNOTATED DETECTION IMAGE
        // =====================================================

        File annotatedImage =
                new File(
                        result
                                .getAnnotatedImagePath()
                );


        if (
                annotatedImage.exists()
        ) {

            imagePreview.setImage(
                    new Image(
                            annotatedImage
                                    .toURI()
                                    .toString(),
                            false
                    )
            );
        }


        // =====================================================
        // DETECTIONS
        // =====================================================

        if (
                result.getDetectionCount()
                        == 0
        ) {

            detectionSummary.setText(
                    "No supported recyclable material "
                            + "was detected above the "
                            + "confidence threshold."
            );


            recommendationText.setText(
                    "Manual review recommended. "
                            + "EcoSort could not identify "
                            + "a supported material reliably."
            );

        } else {

            StringBuilder detections =
                    new StringBuilder();


            for (
                    Detection detection
                    : result.getDetections()
            ) {

                if (
                        detections.length()
                                > 0
                ) {

                    detections.append(
                            "\n"
                    );
                }


                detections
                        .append("• ")
                        .append(
                                displayMaterial(
                                        detection
                                                .getMaterial()
                                )
                        )
                        .append("  —  ")
                        .append(
                                String.format(
                                        "%.1f%%",
                                        detection
                                                .getConfidence()
                                                * 100.0
                                )
                        );
            }


            detectionSummary.setText(
                    detections.toString()
            );


            updateDetectionRecommendation(
                    result
            );
        }


        // =====================================================
        // TEMPORARY OBJECT COUNTS
        // =====================================================

        compositionText.setText(
                "Plastic: "
                        + result.getPlasticCount()
                        + " object(s)\n"
                        + "Metal: "
                        + result.getMetalCount()
                        + " object(s)\n"
                        + "Glass: "
                        + result.getGlassCount()
                        + " object(s)\n"
                        + "Paper/Cardboard: "
                        + result.getPaperCardboardCount()
                        + " object(s)\n\n"
                        + (
                        segmentationService
                                .isModelAvailable()
                                ? "Segmentation analysis is running..."
                                : "Pixel-area composition will activate "
                                + "when the segmentation model is installed."
                )
        );


        inferenceTimeLabel.setText(
                String.format(
                        "Detection pipeline time: %.2f s",
                        result
                                .getInferenceTimeMs()
                                / 1000.0
                )
        );
    }


    // =========================================================
    // RUN SEGMENTATION
    // =========================================================

    private void runSegmentation(
            File image,
            InferenceResult detectionResult) {

        systemStatus.setText(
                "●  SEGMENTING"
        );


        systemStatus.setStyle(
                statusStyle(
                        "#E6EDFF",
                        "#3656A5"
                )
        );


        Task<SegmentationResult> task =
                new Task<>() {

                    @Override
                    protected SegmentationResult call()
                            throws Exception {

                        return segmentationService
                                .analyze(
                                        image
                                );
                    }
                };


        activeAnalysisTask =
                task;


        task.setOnSucceeded(
                event -> {

                    if (analysisCancelled || task.isCancelled()) {
                        return;
                    }


                    SegmentationResult segmentation =
                            task.getValue();


                    displaySegmentationResult(
                            segmentation
                    );


                    saveHistory(
                            image,
                            detectionResult,
                            segmentation
                    );


                    setProcessingState(
                            false
                    );


                    activeAnalysisTask = null;
                    setCompleteStatus();
                }
        );


        task.setOnFailed(
                event -> {

                    if (analysisCancelled || task.isCancelled()) {
                        return;
                    }


                    /*
                     * Detection is still valid even if
                     * segmentation fails.
                     */

                    purityValue.setText(
                            "Unavailable"
                    );


                    contaminationValue.setText(
                            "Unavailable"
                    );


                    saveHistory(
                            image,
                            detectionResult,
                            null
                    );


                    setProcessingState(
                            false
                    );


                    activeAnalysisTask = null;
                    setCompleteStatus();


                    Throwable error =
                            task.getException();


                    System.err.println(
                            "Segmentation failed: "
                                    + (
                                    error == null
                                            ? "Unknown error"
                                            : error.getMessage()
                            )
                    );
                }
        );


        Thread thread =
                new Thread(
                        task,
                        "ecosort-segmentation"
                );


        thread.setDaemon(
                true
        );


        thread.start();
    }


    // =========================================================
    // DISPLAY SEGMENTATION RESULT
    // =========================================================

    private void displaySegmentationResult(
            SegmentationResult result) {

        purityValue.setText(
                String.format(
                        "%.1f%%",
                        result.getPurity()
                )
        );

        contaminationValue.setText(
                String.format(
                        "%.1f%%",
                        result.getContamination()
                )
        );

        materialValue.setText(
                displayMaterial(
                        result.getDominantMaterial()
                )
        );

        StringBuilder composition =
                new StringBuilder();

        for (Material material : Material.values()) {
            if (composition.length() > 0) {
                composition.append("\n");
            }

            composition
                    .append(displayMaterial(material.name()))
                    .append(": ")
                    .append(
                            String.format(
                                    "%.1f%%",
                                    result.getComposition()
                                            .getOrDefault(material, 0.0)
                            )
                    );
        }

        compositionText.setText(
                composition.toString()
        );

        File segmentedImage =
                new File(
                        result.getSegmentedImagePath()
                );

        if (segmentedImage.exists()) {
            imagePreview.setImage(
                    new Image(
                            segmentedImage.toURI().toString(),
                            false
                    )
            );
        }

        String dominant =
                displayMaterial(
                        result.getDominantMaterial()
                );

        double purity =
                result.getPurity();

        double contamination =
                result.getContamination();

        boolean noSegmentation =
                result.getInstanceCount() == 0;

        boolean hasDetection =
                latestDetectionResult != null
                        && latestDetectionResult.getDetectionCount() > 0;

        boolean noDetection =
                !hasDetection;

        boolean lowDetectionConfidence =
                hasDetection
                        && latestDetectionResult.getHighestConfidence() < 0.40;

        boolean modelsDisagree =
                hasDetection
                        && latestDetectionResult.getHighestConfidence() >= 0.40
                        && latestDetectionResult.getHighestMaterial() != null
                        && result.getDominantMaterial() != null
                        && !latestDetectionResult
                        .getHighestMaterial()
                        .equalsIgnoreCase(
                                result.getDominantMaterial()
                        );

        String detectionDecision =
                !hasDetection
                        ? "No supported material detected"
                        : displayMaterial(latestDetectionResult.getHighestMaterial())
                        + " ("
                        + String.format(
                                "%.1f%%",
                                latestDetectionResult.getHighestConfidence() * 100.0
                        )
                        + ")";

        String segmentationDecision =
                noSegmentation
                        ? "No supported material detected"
                        : dominant
                        + " ("
                        + String.format("%.1f%% purity", purity)
                        + ")";

        String finalDecision;
        String decisionReason;

        if (noSegmentation) {
            finalDecision = "MANUAL REVIEW";
            decisionReason = "No reliable supported region was found by segmentation";

            materialValue.setText("MANUAL REVIEW");
            purityValue.setText("Unavailable");
            contaminationValue.setText("Unavailable");

            recommendationText.setText(
                    "Manual review recommended. "
                            + "The segmentation model did not identify a supported waste region. "
                            + "The material may be unsupported or visually ambiguous."
            );

        } else if (noDetection) {
            finalDecision = "MANUAL REVIEW";
            decisionReason = "Detection found no reliable supported material";

            materialValue.setText("MANUAL REVIEW");

            recommendationText.setText(
                    "Manual review recommended. "
                            + "The detection model found no reliable supported material, "
                            + "while segmentation suggests "
                            + dominant
                            + ". Verify the material before recycling."
            );

        } else if (lowDetectionConfidence) {
            finalDecision = "MANUAL REVIEW";
            decisionReason = "Detection confidence is below the reliability threshold";

            materialValue.setText("MANUAL REVIEW");

            recommendationText.setText(
                    "Manual review recommended. "
                            + "The detector confidence is below the reliability threshold. "
                            + "Segmentation suggests "
                            + dominant
                            + ", but the result should be verified before recycling."
            );

        } else if (modelsDisagree) {
            finalDecision = "MANUAL REVIEW";
            decisionReason = "Detection and segmentation models disagree";

            materialValue.setText("MANUAL REVIEW");

            recommendationText.setText(
                    "Manual review recommended. "
                            + "The detection and segmentation models disagree on the dominant material. "
                            + "Detection suggests "
                            + displayMaterial(latestDetectionResult.getHighestMaterial())
                            + ", while segmentation suggests "
                            + dominant
                            + ". Verify the material before recycling."
            );

        } else {
            finalDecision = dominant;
            decisionReason = hasDetection
                    ? "Detection and segmentation are consistent"
                    : "Segmentation produced the supported material decision";

            if (purity >= 90.0) {
                recommendationText.setText(
                        dominant
                                + " stream has high purity ("
                                + String.format("%.1f%%", purity)
                                + ") and is suitable for direct recycling processing."
                );

            } else if (purity >= 75.0) {
                recommendationText.setText(
                        dominant
                                + " is the dominant material with "
                                + String.format("%.1f%%", purity)
                                + " purity and "
                                + String.format("%.1f%%", contamination)
                                + " contamination. Minor contaminants should be removed before processing."
                );

            } else if (purity >= 50.0) {
                recommendationText.setText(
                        "Mixed "
                                + dominant
                                + " waste stream detected. Purity is "
                                + String.format("%.1f%%", purity)
                                + " with "
                                + String.format("%.1f%%", contamination)
                                + " contamination. Sorting is recommended before recycling."
                );

            } else {
                recommendationText.setText(
                        "Highly mixed waste stream detected. "
                                + "Material separation is required before recycling."
                );
            }
        }

        modelDecisionText.setText(
                "Detection Model: " + detectionDecision + "\n"
                        + "Segmentation Model: " + segmentationDecision + "\n"
                        + "Final Decision: " + finalDecision + "\n"
                        + "Reason: " + decisionReason
        );

        inferenceTimeLabel.setText(
                inferenceTimeLabel.getText()
                        + String.format(
                                "   |   Segmentation: %.2f s",
                                result.getPipelineTimeMs() / 1000.0
                        )
        );
    }


    // =========================================================
    // DETECTION-ONLY RECOMMENDATION
    // =========================================================

    private void updateDetectionRecommendation(
            InferenceResult result) {

        double confidence =
                result.getHighestConfidence();


        String material =
                displayMaterial(
                        result.getHighestMaterial()
                );


        if (
                confidence < 0.40
        ) {

            recommendationText.setText(
                    "Manual review recommended. "
                            + "Detection confidence is too low "
                            + "for reliable recycling guidance."
            );

        } else if (
                confidence < 0.60
        ) {

            recommendationText.setText(
                    "Possible "
                            + material
                            + " detected, but confidence is low. "
                            + "Verify the material before processing."
            );

        } else {

            recommendationText.setText(
                    material
                            + " is the strongest detected material "
                            + "with "
                            + String.format(
                            "%.1f%%",
                            confidence * 100.0
                    )
                            + " confidence."
                            + (
                            segmentationService
                                    .isModelAvailable()
                                    ? " Segmentation analysis is running."
                                    : " Purity and contamination will "
                                    + "activate when the segmentation "
                                    + "model is installed."
                    )
            );
        }
    }


    // =========================================================
    // SAVE HISTORY
    // =========================================================

    private void saveHistory(
            File image,
            InferenceResult detection,
            SegmentationResult segmentation) {

        try {

            Double purity =
                    segmentation == null
                            ? null
                            : segmentation
                            .getPurity();


            Double contamination =
                    segmentation == null
                            ? null
                            : segmentation
                            .getContamination();


            String resultImage =
                    segmentation != null
                            ? segmentation
                            .getSegmentedImagePath()
                            : detection
                            .getAnnotatedImagePath();


            String primaryMaterial;


            if (
                    segmentation != null
                            && segmentation
                            .getInstanceCount()
                            > 0
            ) {

                primaryMaterial =
                        displayMaterial(
                                segmentation
                                        .getDominantMaterial()
                        );

            } else if (
                    detection
                            .getDetectionCount()
                            > 0
            ) {

                primaryMaterial =
                        displayMaterial(
                                detection
                                        .getHighestMaterial()
                        );

            } else {

                primaryMaterial =
                        "UNKNOWN";
            }


            historyService.saveAnalysis(

                    image,

                    resultImage,

                    primaryMaterial,

                    detection
                            .getHighestConfidence(),

                    detection
                            .getDetectionCount(),

                    purity,

                    contamination,

                    recommendationText
                            .getText()
            );


        } catch (Exception exception) {

            System.err.println(
                    "History save failed: "
                            + exception.getMessage()
            );
        }
    }


    // =========================================================
    // DISPLAY MATERIAL
    // =========================================================

    private String displayMaterial(
            String material) {

        if (
                material == null
        ) {

            return "Unknown";
        }


        return switch (material) {

            case "PAPER_CARDBOARD" ->
                    "Paper/Cardboard";

            case "PLASTIC" ->
                    "Plastic";

            case "METAL" ->
                    "Metal";

            case "GLASS" ->
                    "Glass";

            case "UNKNOWN" ->
                    "Unknown";

            default ->
                    material;
        };
    }


    // =========================================================
    // IMAGE CHOOSER
    // =========================================================

    private void chooseImage() {

        FileChooser chooser =
                new FileChooser();


        chooser.setTitle(
                "Select Waste Image"
        );


        chooser
                .getExtensionFilters()
                .add(
                        new FileChooser.ExtensionFilter(
                                "Image Files",
                                "*.jpg",
                                "*.jpeg",
                                "*.png"
                        )
                );


        File file =
                chooser.showOpenDialog(
                        stage
                );


        if (
                file != null
        ) {

            loadImage(
                    file
            );
        }
    }


    // =========================================================
    // LOAD IMAGE
    // =========================================================

    private void loadImage(
            File file) {

        if (
                !isValidImage(
                        file
                )
        ) {

            showInformation(
                    "Unsupported File",
                    "Please choose a JPG, JPEG or PNG image."
            );

            return;
        }


        Image image =
                new Image(
                        file
                                .toURI()
                                .toString(),
                        false
                );


        if (
                image.isError()
        ) {

            showInformation(
                    "Image Error",
                    "EcoSort could not open this image."
            );

            return;
        }


        selectedImage =
                file;


        imagePreview.setImage(
                image
        );


        selectedFileName.setText(
                file.getName()
        );


        selectedFileDetails.setText(
                String.format(
                        "%.0f × %.0f px   •   %.1f KB",
                        image.getWidth(),
                        image.getHeight(),
                        file.length()
                                / 1024.0
                )
        );


        emptyUploadView.setVisible(
                false
        );


        emptyUploadView.setManaged(
                false
        );


        selectedImageView.setVisible(
                true
        );


        selectedImageView.setManaged(
                true
        );


        resetResults();


        setReadyStatus();
    }


    // =========================================================
    // CLEAR IMAGE
    // =========================================================

    private void clearImage() {

        selectedImage =
                null;


        imagePreview.setImage(
                null
        );


        selectedImageView.setVisible(
                false
        );


        selectedImageView.setManaged(
                false
        );


        emptyUploadView.setVisible(
                true
        );


        emptyUploadView.setManaged(
                true
        );


        resetResults();


        setReadyStatus();
    }


    // =========================================================
    // IMAGE VALIDATION
    // =========================================================

    private boolean isValidImage(
            File file) {

        if (
                file == null
                        || !file.isFile()
        ) {

            return false;
        }


        String name =
                file
                        .getName()
                        .toLowerCase();


        return name.endsWith(
                ".jpg"
        )
                || name.endsWith(
                ".jpeg"
        )
                || name.endsWith(
                ".png"
        );
    }


    // =========================================================
    // CANCEL ANALYSIS
    // =========================================================

    private void cancelAnalysis() {

        analysisCancelled =
                true;

        if (activeAnalysisTask != null) {
            activeAnalysisTask.cancel(true);
        }

        activeAnalysisTask =
                null;

        setProcessingState(
                false
        );

        systemStatus.setText(
                "●  ANALYSIS CANCELLED"
        );

        systemStatus.setStyle(
                statusStyle(
                        "#FFF1D6",
                        "#9A6518"
                )
        );

        recommendationText.setText(
                "Analysis cancelled. You can clear the current image or choose another image."
        );

        modelDecisionText.setText(
                "Analysis cancelled before a final cross-model decision was completed."
        );
    }


    // =========================================================
    // PROCESSING STATE
    // =========================================================

    private void setProcessingState(
            boolean processing) {

        analyzeButton.setDisable(
                processing
        );


        changeButton.setDisable(
                processing
        );


        clearButton.setDisable(
                processing
        );


        cancelButton.setVisible(
                processing
        );


        cancelButton.setManaged(
                processing
        );


        progressIndicator.setVisible(
                processing
        );


        if (
                processing
        ) {

            analyzeButton.setText(
                    "Analyzing..."
            );

        } else {

            analyzeButton.setText(
                    "Analyze Waste"
            );
        }
    }


    // =========================================================
    // STATUS HELPERS
    // =========================================================

    private void setReadyStatus() {

        systemStatus.setText(
                "●  SYSTEM READY"
        );


        systemStatus.setStyle(
                statusStyle(
                        "#DDF4E7",
                        "#16794B"
                )
        );
    }


    private void setCompleteStatus() {

        systemStatus.setText(
                "●  ANALYSIS COMPLETE"
        );


        systemStatus.setStyle(
                statusStyle(
                        "#DDF4E7",
                        "#16794B"
                )
        );
    }


    private void setErrorStatus() {

        systemStatus.setText(
                "●  ANALYSIS ERROR"
        );


        systemStatus.setStyle(
                statusStyle(
                        "#FDE2E2",
                        "#A12626"
                )
        );
    }


    private String statusStyle(
            String background,
            String text) {

        return "-fx-background-color: "
                + background
                + ";"
                + "-fx-text-fill: "
                + text
                + ";"
                + "-fx-padding: 7 14;"
                + "-fx-background-radius: 18;"
                + "-fx-font-size: 10px;"
                + "-fx-font-weight: bold;";
    }


    // =========================================================
    // RESET
    // =========================================================

    private void resetResults() {

        latestDetectionResult = null;
        activeAnalysisTask = null;
        analysisCancelled = false;

        materialValue.setText(
                "—"
        );


        purityValue.setText(
                "—"
        );


        contaminationValue.setText(
                "—"
        );


        confidenceValue.setText(
                "—"
        );


        detectionSummary.setText(
                "No image has been analyzed yet."
        );


        compositionText.setText(
                "Analyze an image to view material composition."
        );


        recommendationText.setText(
                "Analyze an image to receive recycling guidance."
        );


        modelDecisionText.setText(
                "Analyze an image to compare detection and segmentation decisions."
        );


        inferenceTimeLabel.setText(
                ""
        );
    }


    // =========================================================
    // PRIMARY BUTTON
    // =========================================================

    private Button createPrimaryButton(
            String text) {

        Button button =
                new Button(
                        text
                );


        button.setStyle(
                "-fx-background-color: linear-gradient(to right, #176B46, #23945F);"
                        + "-fx-text-fill: white;"
                        + "-fx-font-weight: bold;"
                        + "-fx-padding: 11 22;"
                        + "-fx-background-radius: 10;"
                        + "-fx-cursor: hand;"
        );


        return button;
    }


    // =========================================================
    // SECONDARY BUTTON
    // =========================================================

    private Button createSecondaryButton(
            String text) {

        Button button =
                new Button(
                        text
                );


        button.setStyle(
                "-fx-background-color: #E5F0E9;"
                        + "-fx-text-fill: #24563F;"
                        + "-fx-font-weight: bold;"
                        + "-fx-padding: 11 18;"
                        + "-fx-background-radius: 10;"
                        + "-fx-cursor: hand;"
        );


        return button;
    }


    // =========================================================
    // VALUE LABEL
    // =========================================================

    private Label createValueLabel(
            String text) {

        Label label =
                new Label(
                        text
                );


        label.setWrapText(
                true
        );


        label.setStyle(
                "-fx-font-size: 22px;"
                        + "-fx-font-weight: bold;"
                        + "-fx-text-fill: #123D2D;"
        );


        return label;
    }


    // =========================================================
    // RESULT CARD
    // =========================================================

    private VBox createResultCard(
            String heading,
            Label value,
            String description) {

        Label headingLabel =
                new Label(
                        heading
                );


        headingLabel.setStyle(
                "-fx-font-size: 10px;"
                        + "-fx-font-weight: bold;"
                        + "-fx-text-fill: #71847C;"
        );


        Label descriptionLabel =
                new Label(
                        description
                );


        descriptionLabel.setWrapText(
                true
        );


        descriptionLabel.setStyle(
                "-fx-font-size: 10px;"
                        + "-fx-text-fill: #899890;"
        );


        VBox card =
                new VBox(
                        7,
                        headingLabel,
                        value,
                        descriptionLabel
                );


        card.setPadding(
                new Insets(16)
        );


        card.setMinWidth(
                150
        );


        card.setMaxWidth(
                Double.MAX_VALUE
        );


        card.setMinHeight(
                110
        );


        card.setStyle(
                "-fx-background-color: #FFFFFF;"
                        + "-fx-background-radius: 15;"
                        + "-fx-border-color: #D4E5DA;"
                        + "-fx-border-radius: 15;"
                        + "-fx-effect: dropshadow(gaussian, rgba(20,70,48,0.07), 12, 0.12, 0, 3);"
        );


        return card;
    }


    // =========================================================
    // PANEL
    // =========================================================

    private VBox createPanel(
            javafx.scene.Node... nodes) {

        VBox panel =
                new VBox(7);


        panel
                .getChildren()
                .addAll(
                        nodes
                );


        panel.setPadding(
                new Insets(18)
        );


        panel.setStyle(
                "-fx-background-color: #FFFFFF;"
                        + "-fx-background-radius: 15;"
                        + "-fx-border-color: #D4E5DA;"
                        + "-fx-border-radius: 15;"
                        + "-fx-effect: dropshadow(gaussian, rgba(20,70,48,0.07), 12, 0.12, 0, 3);"
        );


        return panel;
    }


    // =========================================================
    // PANEL TITLE
    // =========================================================

    private Label createPanelTitle(
            String text) {

        Label label =
                new Label(
                        text
                );


        label.setStyle(
                "-fx-font-size: 16px;"
                        + "-fx-font-weight: bold;"
                        + "-fx-text-fill: #173F30;"
        );


        return label;
    }


    // =========================================================
    // BODY LABEL
    // =========================================================

    private Label createBodyLabel(
            String text) {

        Label label =
                new Label(
                        text
                );


        label.setWrapText(
                true
        );


        label.setStyle(
                "-fx-font-size: 12px;"
                        + "-fx-text-fill: #61766D;"
        );


        return label;
    }


    // =========================================================
    // INFORMATION DIALOG
    // =========================================================

    private void showInformation(
            String title,
            String message) {

        Alert alert =
                new Alert(
                        Alert.AlertType.INFORMATION
                );


        alert.setTitle(
                "EcoSort AI"
        );


        alert.setHeaderText(
                title
        );


        alert.setContentText(
                message
        );


        alert.showAndWait();
    }
}