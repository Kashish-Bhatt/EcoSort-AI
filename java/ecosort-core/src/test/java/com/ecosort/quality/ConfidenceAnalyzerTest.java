package com.ecosort.quality;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.ecosort.quality.ConfidenceAnalyzer.ConfidenceResult;
import com.ecosort.quality.ConfidenceAnalyzer.ConfidenceStatus;
import com.ecosort.quality.ConfidenceAnalyzer.Detection;

public class ConfidenceAnalyzerTest {

    @Test
    void strongPredictionShouldBeHighConfidence() {

        ConfidenceAnalyzer analyzer =
                new ConfidenceAnalyzer();

        ConfidenceResult result =
                analyzer.analyze(
                        List.of(
                                new Detection(
                                        "PLASTIC",
                                        0.93
                                )
                        )
                );

        assertEquals(
                ConfidenceStatus.HIGH_CONFIDENCE,
                result.getStatus()
        );

        assertEquals(
                "PLASTIC",
                result.getPredictedMaterial()
        );

        assertFalse(
                result.isManualReviewRequired()
        );
    }


    @Test
    void lowConfidenceShouldRequireReview() {

        ConfidenceAnalyzer analyzer =
                new ConfidenceAnalyzer();

        ConfidenceResult result =
                analyzer.analyze(
                        List.of(
                                new Detection(
                                        "GLASS",
                                        0.54
                                )
                        )
                );

        assertEquals(
                ConfidenceStatus.LOW_CONFIDENCE,
                result.getStatus()
        );

        assertTrue(
                result.isManualReviewRequired()
        );
    }


    @Test
    void veryLowConfidenceShouldBecomeUnknown() {

        ConfidenceAnalyzer analyzer =
                new ConfidenceAnalyzer();

        ConfidenceResult result =
                analyzer.analyze(
                        List.of(
                                new Detection(
                                        "PLASTIC",
                                        0.22
                                )
                        )
                );

        assertEquals(
                ConfidenceStatus.UNKNOWN,
                result.getStatus()
        );

        assertEquals(
                "UNKNOWN",
                result.getPredictedMaterial()
        );

        assertTrue(
                result.isManualReviewRequired()
        );
    }


    @Test
    void emptyDetectionsShouldBecomeUnknown() {

        ConfidenceAnalyzer analyzer =
                new ConfidenceAnalyzer();

        ConfidenceResult result =
                analyzer.analyze(
                        List.of()
                );

        assertEquals(
                ConfidenceStatus.UNKNOWN,
                result.getStatus()
        );

        assertTrue(
                result.isManualReviewRequired()
        );
    }


    @Test
    void invalidConfidenceShouldBeRejected() {

        ConfidenceAnalyzer analyzer =
                new ConfidenceAnalyzer();

        assertThrows(
                IllegalArgumentException.class,
                () -> analyzer.analyze(
                        List.of(
                                new Detection(
                                        "METAL",
                                        1.25
                                )
                        )
                )
        );
    }
}