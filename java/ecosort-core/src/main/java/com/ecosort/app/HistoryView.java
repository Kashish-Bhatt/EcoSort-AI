package com.ecosort.app;

import com.ecosort.history.AnalysisHistoryService;
import com.ecosort.history.AnalysisHistoryService.HistoryRecord;

import java.io.File;
import java.util.List;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class HistoryView extends VBox {

    private final AnalysisHistoryService historyService;

    private final VBox recordsContainer;


    public HistoryView(
            AnalysisHistoryService historyService) {

        this.historyService =
                historyService;

        setSpacing(16);

        setPadding(
                new Insets(28)
        );

        setStyle(
                "-fx-background-color: #F4F7F5;"
        );


        // =====================================================
        // HEADER
        // =====================================================

        Label title =
                new Label(
                        "Analysis History"
                );

        title.setStyle(
                "-fx-font-size: 26px;"
                        + "-fx-font-weight: bold;"
                        + "-fx-text-fill: #143B30;"
        );

        Label subtitle =
                new Label(
                        "Persistent results from previously "
                                + "completed EcoSort analyses"
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


        Button refreshButton =
                createSecondaryButton(
                        "Refresh"
                );

        Button clearButton =
                createSecondaryButton(
                        "Clear History"
                );


        HBox actions =
                new HBox(
                        10,
                        refreshButton,
                        clearButton
                );


        HBox header =
                new HBox(
                        20,
                        heading,
                        actions
                );

        header.setAlignment(
                Pos.CENTER_LEFT
        );

        HBox.setHgrow(
                heading,
                Priority.ALWAYS
        );


        // =====================================================
        // RECORDS
        // =====================================================

        recordsContainer =
                new VBox(12);


        ScrollPane scroll =
                new ScrollPane(
                        recordsContainer
                );

        scroll.setFitToWidth(true);

        scroll.setHbarPolicy(
                ScrollPane.ScrollBarPolicy.NEVER
        );

        scroll.setVbarPolicy(
                ScrollPane.ScrollBarPolicy.AS_NEEDED
        );

        scroll.setStyle(
                "-fx-background: #F4F7F5;"
                        + "-fx-background-color: #F4F7F5;"
                        + "-fx-border-color: transparent;"
        );

        VBox.setVgrow(
                scroll,
                Priority.ALWAYS
        );


        refreshButton.setOnAction(
                event -> refresh()
        );


        clearButton.setOnAction(
                event -> {

                    historyService.clearHistory();

                    refresh();
                }
        );


        getChildren().addAll(
                header,
                scroll
        );


        refresh();
    }


    // =========================================================
    // REFRESH
    // =========================================================

    public void refresh() {

        recordsContainer
                .getChildren()
                .clear();


        List<HistoryRecord> records =
                historyService.loadHistory();


        if (records.isEmpty()) {

            VBox empty =
                    new VBox(8);

            empty.setAlignment(
                    Pos.CENTER
            );

            empty.setPadding(
                    new Insets(70)
            );


            Label title =
                    new Label(
                            "No analyses yet"
                    );

            title.setStyle(
                    "-fx-font-size: 18px;"
                            + "-fx-font-weight: bold;"
                            + "-fx-text-fill: #27483C;"
            );


            Label text =
                    new Label(
                            "Run an analysis and EcoSort "
                                    + "will automatically save it here."
                    );

            text.setStyle(
                    "-fx-text-fill: #75867F;"
            );


            empty.getChildren()
                    .addAll(
                            title,
                            text
                    );


            recordsContainer
                    .getChildren()
                    .add(empty);

            return;
        }


        for (HistoryRecord record
                : records) {

            recordsContainer
                    .getChildren()
                    .add(
                            createHistoryCard(
                                    record
                            )
                    );
        }
    }


    // =========================================================
    // HISTORY CARD
    // =========================================================

    private HBox createHistoryCard(
            HistoryRecord record) {

        ImageView thumbnail =
                new ImageView();

        thumbnail.setFitWidth(150);
        thumbnail.setFitHeight(105);
        thumbnail.setPreserveRatio(true);


        File previewFile =
                new File(
                        record.getAnnotatedImagePath()
                );


        if (!previewFile.exists()) {

            previewFile =
                    new File(
                            record.getOriginalImagePath()
                    );
        }


        if (previewFile.exists()) {

            thumbnail.setImage(
                    new Image(
                            previewFile.toURI()
                                    .toString(),
                            false
                    )
            );
        }


        StackPane imageBox =
                new StackPane(
                        thumbnail
                );

        imageBox.setPrefWidth(170);

        imageBox.setStyle(
                "-fx-background-color: #EDF2EF;"
                        + "-fx-background-radius: 9;"
        );


        Label imageName =
                new Label(
                        record.getImageName()
                );

        imageName.setStyle(
                "-fx-font-size: 14px;"
                        + "-fx-font-weight: bold;"
                        + "-fx-text-fill: #203F34;"
        );


        Label time =
                new Label(
                        record.getDisplayTime()
                );

        time.setStyle(
                "-fx-font-size: 10px;"
                        + "-fx-text-fill: #809089;"
        );


        Label material =
                new Label(
                        "Primary material: "
                                + record.getPrimaryMaterial()
                );


        Label confidence =
                new Label(
                        String.format(
                                "AI confidence: %.1f%%",
                                record.getConfidence()
                                        * 100.0
                        )
                );


        Label detections =
                new Label(
                        "Detected objects: "
                                + record.getDetectionCount()
                );


        String purityText =
                record.getPurity() == null
                        ? "Purity: Not available until segmentation"
                        : String.format(
                        "Purity: %.1f%%",
                        record.getPurity()
                );


        String contaminationText =
                record.getContamination() == null
                        ? "Contamination: Not available until segmentation"
                        : String.format(
                        "Contamination: %.1f%%",
                        record.getContamination()
                );


        Label purity =
                new Label(
                        purityText
                );


        Label contamination =
                new Label(
                        contaminationText
                );


        VBox details =
                new VBox(
                        5,
                        imageName,
                        time,
                        material,
                        confidence,
                        detections,
                        purity,
                        contamination
                );


        HBox.setHgrow(
                details,
                Priority.ALWAYS
        );


        Button viewButton =
                createSecondaryButton(
                        "View Result"
                );


        File finalPreview =
                previewFile;


        viewButton.setOnAction(
                event -> {

                    if (finalPreview.exists()) {

                        showImageWindow(
                                finalPreview,
                                record.getImageName()
                        );
                    }
                }
        );


        HBox card =
                new HBox(
                        18,
                        imageBox,
                        details,
                        viewButton
                );


        card.setAlignment(
                Pos.CENTER_LEFT
        );


        card.setPadding(
                new Insets(16)
        );


        card.setStyle(
                "-fx-background-color: white;"
                        + "-fx-background-radius: 12;"
                        + "-fx-border-color: #DCE5E1;"
                        + "-fx-border-radius: 12;"
        );


        return card;
    }


    // =========================================================
    // IMAGE PREVIEW WINDOW
    // =========================================================

    private void showImageWindow(
            File imageFile,
            String title) {

        Image image =
                new Image(
                        imageFile.toURI()
                                .toString(),
                        false
                );


        ImageView view =
                new ImageView(
                        image
                );

        view.setPreserveRatio(true);
        view.setFitWidth(800);
        view.setFitHeight(600);


        StackPane root =
                new StackPane(
                        view
                );

        root.setPadding(
                new Insets(20)
        );

        root.setStyle(
                "-fx-background-color: #18221E;"
        );


        Stage preview =
                new Stage();

        preview.setTitle(
                "EcoSort Result - "
                        + title
        );

        preview.setScene(
                new Scene(
                        root,
                        850,
                        650
                )
        );

        preview.show();
    }


    private Button createSecondaryButton(
            String text) {

        Button button =
                new Button(text);

        button.setStyle(
                "-fx-background-color: #E8EFEC;"
                        + "-fx-text-fill: #315A4A;"
                        + "-fx-font-weight: bold;"
                        + "-fx-padding: 10 17;"
                        + "-fx-background-radius: 8;"
                        + "-fx-cursor: hand;"
        );

        return button;
    }
}