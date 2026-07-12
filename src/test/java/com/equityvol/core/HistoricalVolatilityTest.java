package com.equityvol.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class HistoricalVolatilityTest {

    @Test
    void annualizesSampleStandardDeviationOfReturns() {
        // Hand-computed: mean = 0, sample variance = 0.0010 / 3, std = 0.0182574,
        // annualized by sqrt(252) => 0.289867
        double[] returns = {0.01, -0.01, 0.02, -0.02};

        double annualizedVol = HistoricalVolatility.annualizedVolatilityFromReturns(returns, 252);

        assertEquals(0.289867, annualizedVol, 1e-3);
    }

    @Test
    void higherPeriodsPerYearIncreasesAnnualizedVolatility() {
        double[] returns = {0.01, -0.01, 0.02, -0.02};

        double dailyAnnualized = HistoricalVolatility.annualizedVolatilityFromReturns(returns, 252);
        double weeklyAnnualized = HistoricalVolatility.annualizedVolatilityFromReturns(returns, 52);

        // Fewer, larger periods per year annualize to a smaller figure than more, smaller periods.
        assertEquals(true, weeklyAnnualized < dailyAnnualized);
    }

    @Test
    void computesVolatilityDirectlyFromPrices() {
        double[] prices = {100, 101, 100, 102, 100};

        double fromPrices = HistoricalVolatility.annualizedVolatility(prices, 252);
        double fromReturns = HistoricalVolatility.annualizedVolatilityFromReturns(ReturnCalculator.logReturns(prices), 252);

        assertEquals(fromReturns, fromPrices, 1e-9);
    }

    @Test
    void rejectsFewerThanTwoReturns() {
        assertThrows(IllegalArgumentException.class,
                () -> HistoricalVolatility.annualizedVolatilityFromReturns(new double[]{0.01}, 252));
    }

    @Test
    void rejectsNonPositivePeriodsPerYear() {
        assertThrows(IllegalArgumentException.class,
                () -> HistoricalVolatility.annualizedVolatilityFromReturns(new double[]{0.01, -0.01}, 0));
    }
}
