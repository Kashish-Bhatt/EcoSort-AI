package com.ecosort.quality;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import com.ecosort.quality.ConfidenceAnalyzer.ConfidenceResult;
import com.ecosort.quality.ConfidenceAnalyzer.Detection;
import com.ecosort.quality.RecyclingQualityAnalyzer.AnalysisResult;
import com.ecosort.quality.RecyclingQualityAnalyzer.Material;

public class EcoSortAnalysisService {

    private final RecyclingQualityAnalyzer qualityAnalyzer;
    private final ConfidenceAnalyzer confidenceAnalyzer;

    public EcoSortAnalysisService() {

        qualityAnalyzer =
                new RecyclingQualityAnalyzer();

        confidenceAnalyzer =
                new ConfidenceAnalyzer();
    }

    public static class EcoSortAnalysis {

        private final String primaryMaterial;

        private final double confidence;

        private final ConfidenceAnalyzer.ConfidenceStatus
                confidenceStatus;

        private final Map<Material, Double>
                materialComposition;

        private final double purity;

        private final double contamination;

        private final RecyclingQualityAnalyzer.QualityGrade
                qualityGrade;

        private final boolean manualReviewRequired;

        private final String recommendation;

        public EcoSortAnalysis(
                String primaryMaterial,
                double confidence,
                ConfidenceAnalyzer.ConfidenceStatus
                        confidenceStatus,
                Map<Material, Double>
                        materialComposition,
                double purity,
                double contamination,
                RecyclingQualityAnalyzer.QualityGrade
                        qualityGrade,
                boolean manualReviewRequired,
                String recommendation) {

            this.primaryMaterial =
                    primaryMaterial;

            this.confidence =
                    confidence;

            this.confidenceStatus =
                    confidenceStatus;

            this.materialComposition =
                    materialComposition;

            this.purity =
                    purity;

            this.contamination =
                    contamination;

            this.qualityGrade =
                    qualityGrade;

            this.manualReviewRequired =
                    manualReviewRequired;

            this.recommendation =
                    recommendation;
        }

        public String getPrimaryMaterial() {
            return primaryMaterial;
        }

        public double getConfidence() {
            return confidence;
        }

        public ConfidenceAnalyzer.ConfidenceStatus
        getConfidenceStatus() {

            return confidenceStatus;
        }

        public Map<Material, Double>
        getMaterialComposition() {

            return materialComposition;
        }

        public double getPurity() {
            return purity;
        }

        public double getContamination() {
            return contamination;
        }

        public RecyclingQualityAnalyzer.QualityGrade
        getQualityGrade() {

            return qualityGrade;
        }

        public boolean isManualReviewRequired() {
            return manualReviewRequired;
        }

        public String getRecommendation() {
            return recommendation;
        }
    }


    public EcoSortAnalysis analyze(
            List<Detection> detections,
            Map<Material, Double> maskAreas) {

        ConfidenceResult confidenceResult =
                confidenceAnalyzer.analyze(
                        detections
                );

        /*
         * If the detector cannot confidently identify
         * recyclable material, we should not generate
         * misleading purity/recycling advice.
         */

        if (confidenceResult.getStatus()
                == ConfidenceAnalyzer
                .ConfidenceStatus.UNKNOWN) {

            Map<Material, Double> emptyComposition =
                    new EnumMap<>(
                            Material.class
                    );

            for (Material material
                    : Material.values()) {

                emptyComposition.put(
                        material,
                        0.0
                );
            }

            return new EcoSortAnalysis(
                    "UNKNOWN",
                    confidenceResult
                            .getHighestConfidence(),
                    confidenceResult
                            .getStatus(),
                    emptyComposition,
                    0.0,
                    0.0,
                    null,
                    true,
                    "Material could not be identified "
                            + "reliably. Manual inspection "
                            + "is required before recycling."
            );
        }

        AnalysisResult qualityResult =
                qualityAnalyzer.analyze(
                        maskAreas
                );

        boolean manualReview =
                confidenceResult
                        .isManualReviewRequired();

        String recommendation =
                qualityResult
                        .getRecommendation();

        if (manualReview) {

            recommendation =
                    "LOW-CONFIDENCE RESULT: "
                            + recommendation
                            + " Verify the detected "
                            + "material before processing.";
        }

        return new EcoSortAnalysis(
                qualityResult
                        .getDominantMaterial()
                        .name(),

                confidenceResult
                        .getHighestConfidence(),

                confidenceResult
                        .getStatus(),

                qualityResult
                        .getComposition(),

                qualityResult
                        .getPurity(),

                qualityResult
                        .getContamination(),

                qualityResult
                        .getQualityGrade(),

                manualReview,

                recommendation
        );
    }


    public static void main(String[] args) {

        EcoSortAnalysisService service =
                new EcoSortAnalysisService();


        // ==========================================
        // SIMULATED MODEL DETECTIONS
        // ==========================================

        List<Detection> detections =
                List.of(

                        new Detection(
                                "PLASTIC",
                                0.91
                        ),

                        new Detection(
                                "METAL",
                                0.82
                        ),

                        new Detection(
                                "PAPER_CARDBOARD",
                                0.76
                        )
                );


        // ==========================================
        // SIMULATED SEGMENTATION MASK AREAS
        // ==========================================

        Map<Material, Double> maskAreas =
                new EnumMap<>(
                        Material.class
                );

        maskAreas.put(
                Material.PLASTIC,
                7200.0
        );

        maskAreas.put(
                Material.METAL,
                1800.0
        );

        maskAreas.put(
                Material.GLASS,
                0.0
        );

        maskAreas.put(
                Material.PAPER_CARDBOARD,
                1000.0
        );


        EcoSortAnalysis result =
                service.analyze(
                        detections,
                        maskAreas
                );


        System.out.println(
                "\n======================================"
        );

        System.out.println(
                "       ECOSORT FINAL ANALYSIS"
        );

        System.out.println(
                "======================================"
        );


        System.out.println(
                "\nPrimary material: "
                        + result
                        .getPrimaryMaterial()
        );


        System.out.printf(
                "Model confidence: %.2f%%%n",
                result.getConfidence()
                        * 100.0
        );


        System.out.println(
                "Confidence status: "
                        + result
                        .getConfidenceStatus()
        );


        System.out.println(
                "\nMaterial composition:"
        );


        for (Material material
                : Material.values()) {

            System.out.printf(
                    "%-20s %.2f%%%n",
                    material,
                    result
                            .getMaterialComposition()
                            .get(material)
            );
        }


        System.out.printf(
                "\nPurity: %.2f%%%n",
                result.getPurity()
        );


        System.out.printf(
                "Contamination: %.2f%%%n",
                result.getContamination()
        );


        System.out.println(
                "Quality grade: "
                        + result
                        .getQualityGrade()
        );


        System.out.println(
                "Manual review required: "
                        + (
                        result
                                .isManualReviewRequired()
                                ? "YES"
                                : "NO"
                )
        );


        System.out.println(
                "\nRecommendation:"
        );


        System.out.println(
                result.getRecommendation()
        );


        System.out.println(
                "\n======================================"
        );
    }
}