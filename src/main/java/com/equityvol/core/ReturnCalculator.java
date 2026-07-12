package com.equityvol.core;

/**
 * Converts a series of prices into log returns, the standard input for
 * realized-volatility estimation because they are additive across time
 * and symmetric for gains/losses.
 */
public final class ReturnCalculator {

    private ReturnCalculator() {
    }

    public static double[] logReturns(double[] prices) {
        if (prices == null || prices.length < 2) {
            throw new IllegalArgumentException("At least two prices are required to compute a return");
        }
        double[] returns = new double[prices.length - 1];
        for (int i = 1; i < prices.length; i++) {
            if (prices[i - 1] <= 0 || prices[i] <= 0) {
                throw new IllegalArgumentException("Prices must be positive");
            }
            returns[i - 1] = Math.log(prices[i] / prices[i - 1]);
        }
        return returns;
    }
}
