package com.ecosort.app;

import com.ecosort.history.AnalysisHistoryService;
import com.ecosort.inference.InferenceService;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class EcoSortApplication extends Application {

    private BorderPane root;

    private AnalyzeWasteView dashboardView;
    private HistoryView historyView;
    private ModelInformationView modelInformationView;

    private final InferenceService inferenceService =
            new InferenceService();

    private final AnalysisHistoryService historyService =
            new AnalysisHistoryService();


    @Override
    public void start(Stage stage) {

        root = new BorderPane();

        root.setStyle(
                "-fx-background-color: #F4F7F5;"
        );

        dashboardView =
                new AnalyzeWasteView(
                        stage,
                        inferenceService,
                        historyService
                );

        historyView =
                new HistoryView(
                        historyService
                );

        modelInformationView =
                new ModelInformationView();

        root.setLeft(
                createSidebar()
        );

        showDashboard();

        Scene scene =
                new Scene(
                        root,
                        1200,
                        720
                );

        stage.setTitle(
                "EcoSort AI - Recycling Quality Controller"
        );

        stage.setMinWidth(1000);
        stage.setMinHeight(620);

        stage.setScene(scene);
        stage.centerOnScreen();
        stage.show();
    }


    private VBox createSidebar() {

        VBox sidebar =
                new VBox(16);

        sidebar.setPrefWidth(215);
        sidebar.setMinWidth(215);

        sidebar.setPadding(
                new Insets(
                        28,
                        18,
                        22,
                        18
                )
        );

        sidebar.setStyle(
                "-fx-background-color: #103F30;"
        );

        Label logo =
                new Label(
                        "♻  EcoSort AI"
                );

        logo.setStyle(
                "-fx-text-fill: white;"
                        + "-fx-font-size: 21px;"
                        + "-fx-font-weight: bold;"
        );

        Label subtitle =
                new Label(
                        "Recycling Intelligence"
                );

        subtitle.setStyle(
                "-fx-text-fill: #A9CBBE;"
                        + "-fx-font-size: 11px;"
        );

        VBox brand =
                new VBox(
                        4,
                        logo,
                        subtitle
                );

        brand.setPadding(
                new Insets(
                        0,
                        0,
                        20,
                        0
                )
        );

        Button dashboardButton =
                createSidebarButton(
                        "Dashboard"
                );

        Button historyButton =
                createSidebarButton(
                        "Analysis History"
                );

        Button modelButton =
                createSidebarButton(
                        "Model Information"
                );

        dashboardButton.setOnAction(
                event -> showDashboard()
        );

        historyButton.setOnAction(
                event -> showHistory()
        );

        modelButton.setOnAction(
                event -> showModelInformation()
        );

        Region spacer =
                new Region();

        VBox.setVgrow(
                spacer,
                Priority.ALWAYS
        );

        Label version =
                new Label(
                        "EcoSort AI • Model V2"
                );

        version.setStyle(
                "-fx-text-fill: #87AA9D;"
                        + "-fx-font-size: 10px;"
        );

        sidebar.getChildren().addAll(
                brand,
                dashboardButton,
                historyButton,
                modelButton,
                spacer,
                version
        );

        return sidebar;
    }


    private void showDashboard() {

        root.setCenter(
                dashboardView
        );

        dashboardView.setVvalue(0);
    }


    private void showHistory() {

        historyView.refresh();

        root.setCenter(
                historyView
        );
    }


    private void showModelInformation() {

        ScrollPane scroll =
                new ScrollPane(
                        modelInformationView
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

        root.setCenter(scroll);
    }


    private Button createSidebarButton(
            String text) {

        Button button =
                new Button(text);

        button.setMaxWidth(
                Double.MAX_VALUE
        );

        button.setAlignment(
                Pos.CENTER_LEFT
        );

        button.setStyle(
                "-fx-background-color: transparent;"
                        + "-fx-text-fill: #DDEAE4;"
                        + "-fx-font-size: 13px;"
                        + "-fx-padding: 11 13;"
                        + "-fx-cursor: hand;"
        );

        return button;
    }


    public static void main(
            String[] args) {

        launch(args);
    }
}