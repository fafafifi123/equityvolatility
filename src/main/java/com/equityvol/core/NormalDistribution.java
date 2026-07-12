package com.equityvol.core;

/**
 * Standard normal density and cumulative distribution, needed by Black-Scholes.
 * The CDF uses the Abramowitz &amp; Stegun 7.1.26 rational approximation
 * (max error ~1.5e-7), which is accurate enough for option pricing without
 * pulling in an external math library.
 */
final class NormalDistribution {

    private static final double A1 = 0.254829592;
    private static final double A2 = -0.284496736;
    private static final double A3 = 1.421413741;
    private static final double A4 = -1.453152027;
    private static final double A5 = 1.061405429;
    private static final double P = 0.3275911;

    private NormalDistribution() {
    }

    static double pdf(double x) {
        return Math.exp(-0.5 * x * x) / Math.sqrt(2 * Math.PI);
    }

    static double cdf(double x) {
        double sign = x < 0 ? -1.0 : 1.0;
        double z = Math.abs(x) / Math.sqrt(2.0);

        double t = 1.0 / (1.0 + P * z);
        double poly = ((((A5 * t + A4) * t + A3) * t + A2) * t + A1) * t;
        double erf = 1.0 - poly * Math.exp(-z * z);

        return 0.5 * (1.0 + sign * erf);
    }
}
