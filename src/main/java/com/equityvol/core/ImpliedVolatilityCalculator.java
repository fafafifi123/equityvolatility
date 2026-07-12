package com.equityvol.core;

import com.equityvol.model.OptionType;

/**
 * Solves for the volatility that reprices a European option to its observed
 * market price ("implied volatility").
 *
 * <p>Strategy: Newton-Raphson using Black-Scholes vega as the derivative,
 * since it converges in a handful of iterations near the money. If a step
 * would leave the search bracket (vega near zero, or the guess drifts
 * non-positive — common deep in/out-of-the-money), it falls back to
 * bisection on that iteration, which is slower but always converges given
 * a valid bracket. This mirrors how production vol solvers are built:
 * fast method first, robust method as a safety net.
 */
public final class ImpliedVolatilityCalculator {

    private static final int MAX_ITERATIONS = 100;
    private static final double PRICE_TOLERANCE = 1e-8;
    private static final double MIN_VOLATILITY = 1e-6;
    private static final double MAX_VOLATILITY = 5.0;

    private ImpliedVolatilityCalculator() {
    }

    public static double solve(double marketPrice, double s, double k, double t, double r, double q, OptionType type) {
        validateArbitrageBounds(marketPrice, s, k, t, r, q, type);

        double lowVol = MIN_VOLATILITY;
        double highVol = MAX_VOLATILITY;
        double sigma = initialGuess(s, k, t, r, q);

        for (int i = 0; i < MAX_ITERATIONS; i++) {
            double price = BlackScholes.price(s, k, t, r, q, sigma, type);
            double diff = price - marketPrice;

            if (Math.abs(diff) < PRICE_TOLERANCE) {
                return sigma;
            }

            if (diff > 0) {
                highVol = sigma;
            } else {
                lowVol = sigma;
            }

            double vega = BlackScholes.vega(s, k, t, r, q, sigma);
            double newtonStep = vega > 1e-10 ? sigma - diff / vega : Double.NaN;

            if (Double.isNaN(newtonStep) || newtonStep <= lowVol || newtonStep >= highVol) {
                sigma = 0.5 * (lowVol + highVol);
            } else {
                sigma = newtonStep;
            }
        }

        throw new ArithmeticException("Implied volatility did not converge within " + MAX_ITERATIONS + " iterations");
    }

    private static double initialGuess(double s, double k, double t, double r, double q) {
        double forward = s * Math.exp((r - q) * t);
        double moneyness = Math.abs(Math.log(forward / k));
        return 0.2 + moneyness;
    }

    private static void validateArbitrageBounds(double marketPrice, double s, double k, double t, double r, double q, OptionType type) {
        if (marketPrice <= 0) {
            throw new IllegalArgumentException("Market price must be positive");
        }
        double discountedSpot = s * Math.exp(-q * t);
        double discountedStrike = k * Math.exp(-r * t);
        double intrinsicLowerBound = type == OptionType.CALL
                ? Math.max(0, discountedSpot - discountedStrike)
                : Math.max(0, discountedStrike - discountedSpot);
        double upperBound = type == OptionType.CALL ? discountedSpot : discountedStrike;

        if (marketPrice < intrinsicLowerBound || marketPrice > upperBound) {
            throw new IllegalArgumentException(
                    "Market price " + marketPrice + " violates no-arbitrage bounds [" + intrinsicLowerBound + ", " + upperBound + "]");
        }
    }
}
