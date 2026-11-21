package com.ecosort.fundamentals;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

public class GradientCheckerTest {

    @Test
    void analyticAndNumericalGradientsShouldMatch() {

        assertTrue(
                GradientChecker.checkGradient(
                        0.7,
                        -0.2,
                        0.8,
                        1.0
                )
        );
    }
}