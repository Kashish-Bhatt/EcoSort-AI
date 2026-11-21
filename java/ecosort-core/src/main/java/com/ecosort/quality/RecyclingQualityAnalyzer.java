package com.ecosort.quality;



import java.util.EnumMap;
import java.util.Map;

public class RecyclingQualityAnalyzer {

    public enum Material {
        PLASTIC,
        METAL,
        GLASS,
        PAPER_CARDBOARD
    }

    public enum QualityGrade {
        HIGH_PURITY,
        ACCEPTABLE,
        MODERATE_CONTAMINATION,
        HIGH_CONTAMINATION
    }

    public static class AnalysisResult {

        private final Material dominantMaterial;
        private final double purity;
        private final double contamination;
        private final QualityGrade qualityGrade;
        private final Map<Material, Double> composition;
        private final String recommendation;

        public AnalysisResult(
                Material dominantMaterial,
                double purity,
                double contamination,
                QualityGrade qualityGrade,
                Map<Material, Double> composition,
                String recommendation) {

            this.dominantMaterial = dominantMaterial;
            this.purity = purity;
            this.contamination = contamination;
            this.qualityGrade = qualityGrade;
            this.composition = composition;
            this.recommendation = recommendation;
        }

        public Material getDominantMaterial() {
            return dominantMaterial;
        }

        public double getPurity() {
            return purity;
        }

        public double getContamination() {
            return contamination;
        }

        public QualityGrade getQualityGrade() {
            return qualityGrade;
        }

        public Map<Material, Double> getComposition() {
            return composition;
        }

        public String getRecommendation() {
            return recommendation;
        }
    }

    public AnalysisResult analyze(
            Map<Material, Double> materialAreas) {

        if (materialAreas == null ||
                materialAreas.isEmpty()) {

            throw new IllegalArgumentException(
                    "Material area data cannot be empty."
            );
        }

        double totalArea = 0.0;

        for (Material material : Material.values()) {

            double area =
                    materialAreas.getOrDefault(
                            material,
                            0.0
                    );

            if (area < 0) {
                throw new IllegalArgumentException(
                        "Material area cannot be negative."
                );
            }

            totalArea += area;
        }

        if (totalArea <= 0.0) {

            throw new IllegalArgumentException(
                    "Total segmented waste area must be greater than zero."
            );
        }

        Map<Material, Double> composition =
                new EnumMap<>(Material.class);

        Material dominantMaterial = null;
        double dominantArea = -1.0;

        for (Material material : Material.values()) {

            double area =
                    materialAreas.getOrDefault(
                            material,
                            0.0
                    );

            double percentage =
                    (area / totalArea) * 100.0;

            composition.put(
                    material,
                    percentage
            );

            if (area > dominantArea) {

                dominantArea = area;
                dominantMaterial = material;
            }
        }

        double purity =
                (dominantArea / totalArea)
                        * 100.0;

        double contamination =
                100.0 - purity;

        QualityGrade qualityGrade =
                determineQualityGrade(
                        purity
                );

        String recommendation =
                createRecommendation(
                        dominantMaterial,
                        purity,
                        contamination,
                        composition
                );

        return new AnalysisResult(
                dominantMaterial,
                purity,
                contamination,
                qualityGrade,
                composition,
                recommendation
        );
    }

    private QualityGrade determineQualityGrade(
            double purity) {

        if (purity >= 90.0) {
            return QualityGrade.HIGH_PURITY;
        }

        if (purity >= 75.0) {
            return QualityGrade.ACCEPTABLE;
        }

        if (purity >= 50.0) {
            return QualityGrade.MODERATE_CONTAMINATION;
        }

        return QualityGrade.HIGH_CONTAMINATION;
    }

    private String createRecommendation(
            Material dominantMaterial,
            double purity,
            double contamination,
            Map<Material, Double> composition) {

        if (purity >= 90.0) {

            return dominantMaterial
                    + " stream has high purity and "
                    + "is suitable for direct recycling processing.";
        }

        StringBuilder contaminants =
                new StringBuilder();

        for (Material material : Material.values()) {

            if (material == dominantMaterial) {
                continue;
            }

            double percentage =
                    composition.getOrDefault(
                            material,
                            0.0
                    );

            if (percentage >= 5.0) {

                if (contaminants.length() > 0) {
                    contaminants.append(", ");
                }

                contaminants.append(
                        material
                );
            }
        }

        if (purity >= 75.0) {

            return "Primary stream is "
                    + dominantMaterial
                    + ". Minor contamination detected ("
                    + String.format("%.1f", contamination)
                    + "%). Separate "
                    + contaminantText(contaminants)
                    + " before recycling where practical.";
        }

        if (purity >= 50.0) {

            return "Mixed "
                    + dominantMaterial
                    + " stream with "
                    + String.format("%.1f", contamination)
                    + "% contamination. Sorting is recommended before "
                    + "recycling. Remove "
                    + contaminantText(contaminants)
                    + ".";
        }

        return "Highly mixed waste stream. "
                + "Material separation is required before recycling. "
                + "Do not process as a clean "
                + dominantMaterial
                + " stream.";
    }

    private String contaminantText(
            StringBuilder contaminants) {

        if (contaminants.length() == 0) {
            return "detected secondary materials";
        }

        return contaminants.toString();
    }

    public static void main(String[] args) {

        RecyclingQualityAnalyzer analyzer =
                new RecyclingQualityAnalyzer();

        /*
         * Example segmentation-mask areas.
         *
         * These represent pixel areas produced
         * by instance segmentation.
         */

        Map<Material, Double> areas =
                new EnumMap<>(Material.class);

        areas.put(
                Material.PLASTIC,
                7800.0
        );

        areas.put(
                Material.METAL,
                1400.0
        );

        areas.put(
                Material.GLASS,
                0.0
        );

        areas.put(
                Material.PAPER_CARDBOARD,
                800.0
        );

        AnalysisResult result =
                analyzer.analyze(areas);

        System.out.println(
                "\n===== ECOSORT RECYCLING QUALITY ANALYSIS ====="
        );

        System.out.println(
                "\nMaterial composition:"
        );

        for (Material material : Material.values()) {

            System.out.printf(
                    "%-20s %.2f%%%n",
                    material,
                    result.getComposition()
                            .get(material)
            );
        }

        System.out.println(
                "\nDominant material: "
                        + result.getDominantMaterial()
        );

        System.out.printf(
                "Purity: %.2f%%%n",
                result.getPurity()
        );

        System.out.printf(
                "Contamination: %.2f%%%n",
                result.getContamination()
        );

        System.out.println(
                "Quality grade: "
                        + result.getQualityGrade()
        );

        System.out.println(
                "\nRecycling recommendation:"
        );

        System.out.println(
                result.getRecommendation()
        );

        System.out.println(
                "\n=============================================="
        );
    }
}