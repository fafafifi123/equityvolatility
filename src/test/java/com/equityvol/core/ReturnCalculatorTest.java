package com.equityvol.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ReturnCalculatorTest {

    private static final double TOLERANCE = 1e-6;

    @Test
    void computesLogReturnsForEachConsecutivePair() {
        double[] prices = {100, 102, 101, 105};

        double[] returns = ReturnCalculator.logReturns(prices);

        assertEquals(3, returns.length);
        assertEquals(Math.log(102.0 / 100.0), returns[0], TOLERANCE);
        assertEquals(Math.log(101.0 / 102.0), returns[1], TOLERANCE);
        assertEquals(Math.log(105.0 / 101.0), returns[2], TOLERANCE);
    }

    @Test
    void flatPriceSeriesProducesZeroReturns() {
        double[] prices = {50, 50, 50};

        double[] returns = ReturnCalculator.logReturns(prices);

        for (double r : returns) {
            assertEquals(0.0, r, TOLERANCE);
        }
    }

    @Test
    void rejectsSeriesShorterThanTwoPrices() {
        assertThrows(IllegalArgumentException.class, () -> ReturnCalculator.logReturns(new double[]{100}));
    }

    @Test
    void rejectsNonPositivePrices() {
        assertThrows(IllegalArgumentException.class, () -> ReturnCalculator.logReturns(new double[]{100, -5}));
        assertThrows(IllegalArgumentException.class, () -> ReturnCalculator.logReturns(new double[]{0, 100}));
    }
}
