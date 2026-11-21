package com.ecosort.history;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AnalysisHistoryService {

    private final Path projectRoot;
    private final Path historyFile;

    private final ObjectMapper objectMapper;

    private static final DateTimeFormatter DISPLAY_FORMAT =
            DateTimeFormatter.ofPattern(
                    "dd MMM yyyy, hh:mm a"
            );


    // =========================================================
    // HISTORY RECORD
    // =========================================================

    public static class HistoryRecord {

        private String timestamp;
        private String displayTime;

        private String imageName;
        private String originalImagePath;
        private String annotatedImagePath;

        private String primaryMaterial;

        private double confidence;

        private int detectionCount;

        private Double purity;
        private Double contamination;

        private String recommendation;


        // Required by Jackson
        public HistoryRecord() {
        }


        public HistoryRecord(
                String timestamp,
                String displayTime,
                String imageName,
                String originalImagePath,
                String annotatedImagePath,
                String primaryMaterial,
                double confidence,
                int detectionCount,
                Double purity,
                Double contamination,
                String recommendation) {

            this.timestamp = timestamp;
            this.displayTime = displayTime;

            this.imageName = imageName;

            this.originalImagePath =
                    originalImagePath;

            this.annotatedImagePath =
                    annotatedImagePath;

            this.primaryMaterial =
                    primaryMaterial;

            this.confidence =
                    confidence;

            this.detectionCount =
                    detectionCount;

            this.purity =
                    purity;

            this.contamination =
                    contamination;

            this.recommendation =
                    recommendation;
        }


        public String getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(
                String timestamp) {
            this.timestamp = timestamp;
        }


        public String getDisplayTime() {
            return displayTime;
        }

        public void setDisplayTime(
                String displayTime) {
            this.displayTime = displayTime;
        }


        public String getImageName() {
            return imageName;
        }

        public void setImageName(
                String imageName) {
            this.imageName = imageName;
        }


        public String getOriginalImagePath() {
            return originalImagePath;
        }

        public void setOriginalImagePath(
                String originalImagePath) {
            this.originalImagePath =
                    originalImagePath;
        }


        public String getAnnotatedImagePath() {
            return annotatedImagePath;
        }

        public void setAnnotatedImagePath(
                String annotatedImagePath) {
            this.annotatedImagePath =
                    annotatedImagePath;
        }


        public String getPrimaryMaterial() {
            return primaryMaterial;
        }

        public void setPrimaryMaterial(
                String primaryMaterial) {
            this.primaryMaterial =
                    primaryMaterial;
        }


        public double getConfidence() {
            return confidence;
        }

        public void setConfidence(
                double confidence) {
            this.confidence = confidence;
        }


        public int getDetectionCount() {
            return detectionCount;
        }

        public void setDetectionCount(
                int detectionCount) {
            this.detectionCount =
                    detectionCount;
        }


        public Double getPurity() {
            return purity;
        }

        public void setPurity(
                Double purity) {
            this.purity = purity;
        }


        public Double getContamination() {
            return contamination;
        }

        public void setContamination(
                Double contamination) {
            this.contamination =
                    contamination;
        }


        public String getRecommendation() {
            return recommendation;
        }

        public void setRecommendation(
                String recommendation) {
            this.recommendation =
                    recommendation;
        }
    }


    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    public AnalysisHistoryService() {

        Path currentDirectory =
                Paths.get("")
                        .toAbsolutePath()
                        .normalize();

        /*
         * Maven/JavaFX runs from:
         *
         * EcoSort-AI/java/ecosort-core
         *
         * Project root is therefore ../..
         */

        projectRoot =
                currentDirectory
                        .resolve("../..")
                        .normalize();


        historyFile =
                projectRoot.resolve(
                        "results/history/"
                                + "analysis_history.json"
                );


        objectMapper =
                new ObjectMapper();

        objectMapper.enable(
                SerializationFeature.INDENT_OUTPUT
        );


        createHistoryDirectory();
    }


    // =========================================================
    // SAVE NEW ANALYSIS
    // =========================================================

    public synchronized HistoryRecord saveAnalysis(
            File originalImage,
            String annotatedImagePath,
            String primaryMaterial,
            double confidence,
            int detectionCount,
            Double purity,
            Double contamination,
            String recommendation) {

        if (originalImage == null) {

            throw new IllegalArgumentException(
                    "Original image cannot be null."
            );
        }


        LocalDateTime now =
                LocalDateTime.now();


        HistoryRecord record =
                new HistoryRecord(

                        now.toString(),

                        now.format(
                                DISPLAY_FORMAT
                        ),

                        originalImage.getName(),

                        originalImage
                                .getAbsolutePath(),

                        annotatedImagePath,

                        primaryMaterial,

                        confidence,

                        detectionCount,

                        purity,

                        contamination,

                        recommendation
                );


        List<HistoryRecord> records =
                loadHistoryInternal();


        /*
         * Newest analysis goes first.
         */

        records.add(
                0,
                record
        );


        writeHistory(
                records
        );


        return record;
    }


    // =========================================================
    // LOAD HISTORY
    // =========================================================

    public synchronized List<HistoryRecord>
    loadHistory() {

        List<HistoryRecord> records =
                loadHistoryInternal();

        return Collections.unmodifiableList(
                records
        );
    }


    // =========================================================
    // CLEAR HISTORY
    // =========================================================

    public synchronized void clearHistory() {

        writeHistory(
                new ArrayList<>()
        );
    }


    // =========================================================
    // NUMBER OF RECORDS
    // =========================================================

    public synchronized int getHistoryCount() {

        return loadHistoryInternal()
                .size();
    }


    // =========================================================
    // INTERNAL LOAD
    // =========================================================

    private List<HistoryRecord>
    loadHistoryInternal() {

        try {

            if (
                    !Files.exists(
                            historyFile
                    )
            ) {

                return new ArrayList<>();
            }


            if (
                    Files.size(
                            historyFile
                    ) == 0
            ) {

                return new ArrayList<>();
            }


            return objectMapper.readValue(

                    historyFile.toFile(),

                    new TypeReference<
                            List<HistoryRecord>>() {
                    }
            );


        } catch (Exception exception) {

            throw new IllegalStateException(
                    "Could not load EcoSort "
                            + "analysis history.",
                    exception
            );
        }
    }


    // =========================================================
    // WRITE HISTORY
    // =========================================================

    private void writeHistory(
            List<HistoryRecord> records) {

        try {

            objectMapper.writeValue(
                    historyFile.toFile(),
                    records
            );


        } catch (Exception exception) {

            throw new IllegalStateException(
                    "Could not save EcoSort "
                            + "analysis history.",
                    exception
            );
        }
    }


    // =========================================================
    // CREATE DIRECTORY
    // =========================================================

    private void createHistoryDirectory() {

        try {

            Files.createDirectories(
                    historyFile.getParent()
            );


        } catch (Exception exception) {

            throw new IllegalStateException(
                    "Could not create EcoSort "
                            + "history directory.",
                    exception
            );
        }
    }


    // =========================================================
    // TEST
    // =========================================================

    public static void main(
            String[] args) {

        AnalysisHistoryService service =
                new AnalysisHistoryService();


        Path testImage =
                service.projectRoot.resolve(
                        "data/ecosort_yolo/"
                                + "images/test/"
                                + "ecosort_00016.jpg"
                );


        Path annotated =
                service.projectRoot.resolve(
                        "results/inference/"
                                + "ecosort_00016_annotated.jpg"
                );


        HistoryRecord record =
                service.saveAnalysis(

                        testImage.toFile(),

                        annotated.toString(),

                        "PLASTIC",

                        0.856508,

                        3,

                        null,

                        null,

                        "Plastic is the strongest "
                                + "detected material. "
                                + "Segmentation analysis "
                                + "is pending."
                );


        System.out.println(
                "\n===== ECOSORT HISTORY TEST ====="
        );


        System.out.println(
                "Saved image: "
                        + record.getImageName()
        );


        System.out.println(
                "Time: "
                        + record.getDisplayTime()
        );


        System.out.println(
                "Material: "
                        + record.getPrimaryMaterial()
        );


        System.out.printf(
                "Confidence: %.2f%%%n",
                record.getConfidence()
                        * 100.0
        );


        System.out.println(
                "Detections: "
                        + record.getDetectionCount()
        );


        System.out.println(
                "Total history records: "
                        + service.getHistoryCount()
        );


        System.out.println(
                "\nHistory file:"
        );


        System.out.println(
                service.historyFile
        );


        System.out.println(
                "\n===== HISTORY TEST COMPLETE ====="
        );
    }
}