package com.equityvol.core;

import com.equityvol.model.OptionType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BlackScholesTest {

    // Classic textbook example (Hull, "Options, Futures, and Other Derivatives"):
    // S=42, K=40, r=10%, sigma=20%, T=0.5y, no dividend -> call ~= 4.76, put ~= 0.81.
    private static final double S = 42;
    private static final double K = 40;
    private static final double T = 0.5;
    private static final double R = 0.10;
    private static final double Q = 0.0;
    private static final double SIGMA = 0.20;

    @Test
    void callPriceMatchesKnownReferenceValue() {
        double price = BlackScholes.price(S, K, T, R, Q, SIGMA, OptionType.CALL);
        assertEquals(4.76, price, 0.01);
    }

    @Test
    void putPriceMatchesKnownReferenceValue() {
        double price = BlackScholes.price(S, K, T, R, Q, SIGMA, OptionType.PUT);
        assertEquals(0.81, price, 0.01);
    }

    @Test
    void satisfiesPutCallParity() {
        double call = BlackScholes.price(S, K, T, R, Q, SIGMA, OptionType.CALL);
        double put = BlackScholes.price(S, K, T, R, Q, SIGMA, OptionType.PUT);

        double lhs = call - put;
        double rhs = S * Math.exp(-Q * T) - K * Math.exp(-R * T);

        assertEquals(rhs, lhs, 1e-9);
    }

    @Test
    void deltaMatchesCentralFiniteDifferenceOfPrice() {
        double h = 1e-4;
        for (OptionType type : OptionType.values()) {
            double analytic = BlackScholes.delta(S, K, T, R, Q, SIGMA, type);
            double numeric = (BlackScholes.price(S + h, K, T, R, Q, SIGMA, type)
                    - BlackScholes.price(S - h, K, T, R, Q, SIGMA, type)) / (2 * h);
            assertEquals(numeric, analytic, 1e-4, "delta mismatch for " + type);
        }
    }

    @Test
    void gammaMatchesCentralFiniteDifferenceOfPrice() {
        double h = 1e-2;
        double analytic = BlackScholes.gamma(S, K, T, R, Q, SIGMA);
        double numeric = (BlackScholes.price(S + h, K, T, R, Q, SIGMA, OptionType.CALL)
                - 2 * BlackScholes.price(S, K, T, R, Q, SIGMA, OptionType.CALL)
                + BlackScholes.price(S - h, K, T, R, Q, SIGMA, OptionType.CALL)) / (h * h);
        assertEquals(numeric, analytic, 1e-3);
    }

    @Test
    void vegaMatchesCentralFiniteDifferenceOfPrice() {
        double h = 1e-4;
        for (OptionType type : OptionType.values()) {
            double analytic = BlackScholes.vega(S, K, T, R, Q, SIGMA);
            double numeric = (BlackScholes.price(S, K, T, R, Q, SIGMA + h, type)
                    - BlackScholes.price(S, K, T, R, Q, SIGMA - h, type)) / (2 * h);
            assertEquals(numeric, analytic, 1e-4, "vega mismatch for " + type);
        }
    }

    @Test
    void rhoMatchesCentralFiniteDifferenceOfPrice() {
        double h = 1e-4;
        for (OptionType type : OptionType.values()) {
            double analytic = BlackScholes.rho(S, K, T, R, Q, SIGMA, type);
            double numeric = (BlackScholes.price(S, K, T, R + h, Q, SIGMA, type)
                    - BlackScholes.price(S, K, T, R - h, Q, SIGMA, type)) / (2 * h);
            assertEquals(numeric, analytic, 1e-4, "rho mismatch for " + type);
        }
    }

    @Test
    void thetaMatchesCentralFiniteDifferenceOfPriceWithRespectToTime() {
        // Price is a function of time-to-expiry T; as T decreases towards expiry, theta = -dPrice/dT.
        double h = 1e-4;
        for (OptionType type : OptionType.values()) {
            double analytic = BlackScholes.theta(S, K, T, R, Q, SIGMA, type);
            double numeric = -(BlackScholes.price(S, K, T + h, R, Q, SIGMA, type)
                    - BlackScholes.price(S, K, T - h, R, Q, SIGMA, type)) / (2 * h);
            assertEquals(numeric, analytic, 1e-3, "theta mismatch for " + type);
        }
    }

    @Test
    void deepInTheMoneyCallDeltaApproachesOne() {
        double delta = BlackScholes.delta(200, K, T, R, Q, SIGMA, OptionType.CALL);
        assertTrue(delta > 0.999);
    }

    @Test
    void deepOutOfTheMoneyPutDeltaApproachesZero() {
        double delta = BlackScholes.delta(200, K, T, R, Q, SIGMA, OptionType.PUT);
        assertTrue(Math.abs(delta) < 0.001);
    }

    @Test
    void rejectsNonPositiveVolatility() {
        assertThrows(IllegalArgumentException.class, () -> BlackScholes.price(S, K, T, R, Q, 0, OptionType.CALL));
        assertThrows(IllegalArgumentException.class, () -> BlackScholes.price(S, K, T, R, Q, -0.1, OptionType.CALL));
    }

    @Test
    void rejectsNonPositiveTimeToExpiry() {
        assertThrows(IllegalArgumentException.class, () -> BlackScholes.price(S, K, 0, R, Q, SIGMA, OptionType.CALL));
    }
}
