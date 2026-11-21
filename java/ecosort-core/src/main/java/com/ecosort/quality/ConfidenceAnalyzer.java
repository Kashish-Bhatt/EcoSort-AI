package com.ecosort.quality;


import java.util.List;

public class ConfidenceAnalyzer {

    // Detection confidence thresholds
    private static final double HIGH_CONFIDENCE = 0.75;
    private static final double MINIMUM_CONFIDENCE = 0.40;

    public enum ConfidenceStatus {
        HIGH_CONFIDENCE,
        ACCEPTABLE,
        LOW_CONFIDENCE,
        UNKNOWN
    }

    public static class Detection {

        private final String material;
        private final double confidence;

        public Detection(
                String material,
                double confidence) {

            this.material = material;
            this.confidence = confidence;
        }

        public String getMaterial() {
            return material;
        }

        public double getConfidence() {
            return confidence;
        }
    }

    public static class ConfidenceResult {

        private final ConfidenceStatus status;
        private final double highestConfidence;
        private final String predictedMaterial;
        private final boolean manualReviewRequired;
        private final String message;

        public ConfidenceResult(
                ConfidenceStatus status,
                double highestConfidence,
                String predictedMaterial,
                boolean manualReviewRequired,
                String message) {

            this.status = status;
            this.highestConfidence = highestConfidence;
            this.predictedMaterial = predictedMaterial;
            this.manualReviewRequired = manualReviewRequired;
            this.message = message;
        }

        public ConfidenceStatus getStatus() {
            return status;
        }

        public double getHighestConfidence() {
            return highestConfidence;
        }

        public String getPredictedMaterial() {
            return predictedMaterial;
        }

        public boolean isManualReviewRequired() {
            return manualReviewRequired;
        }

        public String getMessage() {
            return message;
        }
    }

    public ConfidenceResult analyze(
            List<Detection> detections) {

        if (detections == null ||
                detections.isEmpty()) {

            return new ConfidenceResult(
                    ConfidenceStatus.UNKNOWN,
                    0.0,
                    "UNKNOWN",
                    true,
                    "No recognized recyclable material was detected."
            );
        }

        Detection bestDetection = null;

        for (Detection detection : detections) {

            if (detection.getConfidence() < 0.0 ||
                    detection.getConfidence() > 1.0) {

                throw new IllegalArgumentException(
                        "Confidence must be between 0 and 1."
                );
            }

            if (bestDetection == null ||
                    detection.getConfidence()
                            > bestDetection.getConfidence()) {

                bestDetection = detection;
            }
        }

        double confidence =
                bestDetection.getConfidence();

        String material =
                bestDetection.getMaterial();

        if (confidence < MINIMUM_CONFIDENCE) {

            return new ConfidenceResult(
                    ConfidenceStatus.UNKNOWN,
                    confidence,
                    "UNKNOWN",
                    true,
                    "Prediction confidence is too low. "
                            + "Manual inspection is recommended."
            );
        }

        if (confidence < 0.60) {

            return new ConfidenceResult(
                    ConfidenceStatus.LOW_CONFIDENCE,
                    confidence,
                    material,
                    true,
                    "Possible "
                            + material
                            + " detected, but confidence is low. "
                            + "Verify before recycling."
            );
        }

        if (confidence < HIGH_CONFIDENCE) {

            return new ConfidenceResult(
                    ConfidenceStatus.ACCEPTABLE,
                    confidence,
                    material,
                    false,
                    material
                            + " detected with acceptable confidence."
            );
        }

        return new ConfidenceResult(
                ConfidenceStatus.HIGH_CONFIDENCE,
                confidence,
                material,
                false,
                material
                        + " detected with high confidence."
        );
    }

    public static void main(String[] args) {

        ConfidenceAnalyzer analyzer =
                new ConfidenceAnalyzer();

        // Example 1 — strong detection
        List<Detection> strongDetection =
                List.of(
                        new Detection(
                                "PLASTIC",
                                0.93
                        ),
                        new Detection(
                                "METAL",
                                0.21
                        )
                );

        // Example 2 — uncertain detection
        List<Detection> uncertainDetection =
                List.of(
                        new Detection(
                                "GLASS",
                                0.54
                        ),
                        new Detection(
                                "PLASTIC",
                                0.31
                        )
                );

        // Example 3 — unknown object
        List<Detection> unknownDetection =
                List.of(
                        new Detection(
                                "PAPER_CARDBOARD",
                                0.22
                        ),
                        new Detection(
                                "PLASTIC",
                                0.18
                        )
                );

        System.out.println(
                "\n===== ECOSORT CONFIDENCE ANALYSIS ====="
        );

        System.out.println(
                "\nCASE 1 — HIGH CONFIDENCE"
        );

        printResult(
                analyzer.analyze(
                        strongDetection
                )
        );

        System.out.println(
                "\nCASE 2 — LOW CONFIDENCE"
        );

        printResult(
                analyzer.analyze(
                        uncertainDetection
                )
        );

        System.out.println(
                "\nCASE 3 — UNKNOWN"
        );

        printResult(
                analyzer.analyze(
                        unknownDetection
                )
        );

        System.out.println(
                "\n======================================="
        );
    }

    private static void printResult(
            ConfidenceResult result) {

        System.out.println(
                "Material: "
                        + result.getPredictedMaterial()
        );

        System.out.printf(
                "Confidence: %.2f%%%n",
                result.getHighestConfidence()
                        * 100.0
        );

        System.out.println(
                "Status: "
                        + result.getStatus()
        );

        System.out.println(
                "Manual review: "
                        + (
                        result.isManualReviewRequired()
                                ? "YES"
                                : "NO"
                )
        );

        System.out.println(
                "Message: "
                        + result.getMessage()
        );
    }
}