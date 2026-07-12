package com.equityvol.core;

import com.equityvol.model.OptionType;

/**
 * Black-Scholes-Merton pricing and Greeks for European options on a
 * dividend/carry-adjusted underlying.
 *
 * <p>Inputs: spot {@code S}, strike {@code K}, time to expiry in years {@code T},
 * continuously-compounded risk-free rate {@code r}, continuous dividend yield {@code q},
 * and annualized volatility {@code sigma}.
 */
public final class BlackScholes {

    private BlackScholes() {
    }

    public static double price(double s, double k, double t, double r, double q, double sigma, OptionType type) {
        validateInputs(s, k, t, sigma);
        double d1 = d1(s, k, t, r, q, sigma);
        double d2 = d2(d1, t, sigma);
        double discountedSpot = s * Math.exp(-q * t);
        double discountedStrike = k * Math.exp(-r * t);

        if (type == OptionType.CALL) {
            return discountedSpot * NormalDistribution.cdf(d1) - discountedStrike * NormalDistribution.cdf(d2);
        }
        return discountedStrike * NormalDistribution.cdf(-d2) - discountedSpot * NormalDistribution.cdf(-d1);
    }

    public static double delta(double s, double k, double t, double r, double q, double sigma, OptionType type) {
        validateInputs(s, k, t, sigma);
        double d1 = d1(s, k, t, r, q, sigma);
        double discountFactor = Math.exp(-q * t);
        if (type == OptionType.CALL) {
            return discountFactor * NormalDistribution.cdf(d1);
        }
        return discountFactor * (NormalDistribution.cdf(d1) - 1.0);
    }

    /** Same value for calls and puts. */
    public static double gamma(double s, double k, double t, double r, double q, double sigma) {
        validateInputs(s, k, t, sigma);
        double d1 = d1(s, k, t, r, q, sigma);
        return Math.exp(-q * t) * NormalDistribution.pdf(d1) / (s * sigma * Math.sqrt(t));
    }

    /** Same value for calls and puts. Sensitivity to a 1.0 (100%) change in volatility. */
    public static double vega(double s, double k, double t, double r, double q, double sigma) {
        validateInputs(s, k, t, sigma);
        double d1 = d1(s, k, t, r, q, sigma);
        return s * Math.exp(-q * t) * NormalDistribution.pdf(d1) * Math.sqrt(t);
    }

    /** Annualized theta (time decay per year). */
    public static double theta(double s, double k, double t, double r, double q, double sigma, OptionType type) {
        validateInputs(s, k, t, sigma);
        double d1 = d1(s, k, t, r, q, sigma);
        double d2 = d2(d1, t, sigma);
        double discountedSpot = s * Math.exp(-q * t);
        double discountedStrike = k * Math.exp(-r * t);
        double decayTerm = -discountedSpot * NormalDistribution.pdf(d1) * sigma / (2 * Math.sqrt(t));

        if (type == OptionType.CALL) {
            return decayTerm - r * discountedStrike * NormalDistribution.cdf(d2)
                    + q * discountedSpot * NormalDistribution.cdf(d1);
        }
        return decayTerm + r * discountedStrike * NormalDistribution.cdf(-d2)
                - q * discountedSpot * NormalDistribution.cdf(-d1);
    }

    public static double rho(double s, double k, double t, double r, double q, double sigma, OptionType type) {
        validateInputs(s, k, t, sigma);
        double d1 = d1(s, k, t, r, q, sigma);
        double d2 = d2(d1, t, sigma);
        double discountedStrike = k * Math.exp(-r * t);

        if (type == OptionType.CALL) {
            return t * discountedStrike * NormalDistribution.cdf(d2);
        }
        return -t * discountedStrike * NormalDistribution.cdf(-d2);
    }

    private static double d1(double s, double k, double t, double r, double q, double sigma) {
        return (Math.log(s / k) + (r - q + 0.5 * sigma * sigma) * t) / (sigma * Math.sqrt(t));
    }

    private static double d2(double d1, double t, double sigma) {
        return d1 - sigma * Math.sqrt(t);
    }

    private static void validateInputs(double s, double k, double t, double sigma) {
        if (s <= 0 || k <= 0) {
            throw new IllegalArgumentException("Spot and strike must be positive");
        }
        if (t <= 0) {
            throw new IllegalArgumentException("Time to expiry must be positive");
        }
        if (sigma <= 0) {
            throw new IllegalArgumentException("Volatility must be positive");
        }
    }
}
