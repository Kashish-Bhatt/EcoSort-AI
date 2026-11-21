package com.ecosort.quality;

import static org.junit.jupiter.api.Assertions.*;

import java.util.EnumMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.ecosort.quality.RecyclingQualityAnalyzer.AnalysisResult;
import com.ecosort.quality.RecyclingQualityAnalyzer.Material;
import com.ecosort.quality.RecyclingQualityAnalyzer.QualityGrade;

public class RecyclingQualityAnalyzerTest {

    @Test
    void purePlasticShouldGiveHundredPercentPurity() {

        RecyclingQualityAnalyzer analyzer =
                new RecyclingQualityAnalyzer();

        Map<Material, Double> areas =
                new EnumMap<>(Material.class);

        areas.put(Material.PLASTIC, 1000.0);
        areas.put(Material.METAL, 0.0);
        areas.put(Material.GLASS, 0.0);
        areas.put(Material.PAPER_CARDBOARD, 0.0);

        AnalysisResult result =
                analyzer.analyze(areas);

        assertEquals(
                Material.PLASTIC,
                result.getDominantMaterial()
        );

        assertEquals(
                100.0,
                result.getPurity(),
                0.001
        );

        assertEquals(
                0.0,
                result.getContamination(),
                0.001
        );

        assertEquals(
                QualityGrade.HIGH_PURITY,
                result.getQualityGrade()
        );
    }


    @Test
    void mixedStreamShouldCalculateCorrectPurity() {

        RecyclingQualityAnalyzer analyzer =
                new RecyclingQualityAnalyzer();

        Map<Material, Double> areas =
                new EnumMap<>(Material.class);

        areas.put(Material.PLASTIC, 7800.0);
        areas.put(Material.METAL, 1400.0);
        areas.put(Material.GLASS, 0.0);
        areas.put(Material.PAPER_CARDBOARD, 800.0);

        AnalysisResult result =
                analyzer.analyze(areas);

        assertEquals(
                78.0,
                result.getPurity(),
                0.001
        );

        assertEquals(
                22.0,
                result.getContamination(),
                0.001
        );

        assertEquals(
                QualityGrade.ACCEPTABLE,
                result.getQualityGrade()
        );
    }


    @Test
    void negativeAreaShouldBeRejected() {

        RecyclingQualityAnalyzer analyzer =
                new RecyclingQualityAnalyzer();

        Map<Material, Double> areas =
                new EnumMap<>(Material.class);

        areas.put(
                Material.PLASTIC,
                -100.0
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> analyzer.analyze(areas)
        );
    }


    @Test
    void zeroTotalAreaShouldBeRejected() {

        RecyclingQualityAnalyzer analyzer =
                new RecyclingQualityAnalyzer();

        Map<Material, Double> areas =
                new EnumMap<>(Material.class);

        for (Material material : Material.values()) {

            areas.put(
                    material,
                    0.0
            );
        }

        assertThrows(
                IllegalArgumentException.class,
                () -> analyzer.analyze(areas)
        );
    }
}