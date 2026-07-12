package com.equityvol.core;

/**
 * Estimates annualized realized volatility from a price series using the
 * sample standard deviation of log returns, scaled by the square-root-of-time rule.
 */
public final class HistoricalVolatility {

    private HistoricalVolatility() {
    }

    public static double annualizedVolatility(double[] prices, int periodsPerYear) {
        double[] returns = ReturnCalculator.logReturns(prices);
        return annualizedVolatilityFromReturns(returns, periodsPerYear);
    }

    public static double annualizedVolatilityFromReturns(double[] returns, int periodsPerYear) {
        if (returns == null || returns.length < 2) {
            throw new IllegalArgumentException("At least two returns are required to compute a sample standard deviation");
        }
        if (periodsPerYear <= 0) {
            throw new IllegalArgumentException("periodsPerYear must be positive");
        }
        double mean = mean(returns);
        double sumSquaredDeviations = 0.0;
        for (double r : returns) {
            double deviation = r - mean;
            sumSquaredDeviations += deviation * deviation;
        }
        double sampleVariance = sumSquaredDeviations / (returns.length - 1);
        return Math.sqrt(sampleVariance) * Math.sqrt(periodsPerYear);
    }

    private static double mean(double[] values) {
        double sum = 0.0;
        for (double v : values) {
            sum += v;
        }
        return sum / values.length;
    }
}
