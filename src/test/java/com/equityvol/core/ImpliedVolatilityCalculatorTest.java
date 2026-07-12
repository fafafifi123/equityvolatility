package com.equityvol.core;

import com.equityvol.model.OptionType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ImpliedVolatilityCalculatorTest {

    private static final double S = 100;
    private static final double T = 1.0;
    private static final double R = 0.05;
    private static final double Q = 0.0;

    @Test
    void recoversKnownVolatilityForAtTheMoneyCall() {
        double trueSigma = 0.25;
        double price = BlackScholes.price(S, S, T, R, Q, trueSigma, OptionType.CALL);

        double impliedVol = ImpliedVolatilityCalculator.solve(price, S, S, T, R, Q, OptionType.CALL);

        assertEquals(trueSigma, impliedVol, 1e-6);
    }

    @Test
    void recoversKnownVolatilityForAtTheMoneyPut() {
        double trueSigma = 0.35;
        double price = BlackScholes.price(S, S, T, R, Q, trueSigma, OptionType.PUT);

        double impliedVol = ImpliedVolatilityCalculator.solve(price, S, S, T, R, Q, OptionType.PUT);

        assertEquals(trueSigma, impliedVol, 1e-6);
    }

    @Test
    void recoversKnownVolatilityForDeepOutOfTheMoneyOptions() {
        double trueSigma = 0.6;
        double strike = 160;
        double price = BlackScholes.price(S, strike, T, R, Q, trueSigma, OptionType.CALL);

        double impliedVol = ImpliedVolatilityCalculator.solve(price, S, strike, T, R, Q, OptionType.CALL);

        assertEquals(trueSigma, impliedVol, 1e-5);
    }

    @Test
    void recoversKnownVolatilityForDeepInTheMoneyOptions() {
        double trueSigma = 0.15;
        double strike = 60;
        double price = BlackScholes.price(S, strike, T, R, Q, trueSigma, OptionType.CALL);

        double impliedVol = ImpliedVolatilityCalculator.solve(price, S, strike, T, R, Q, OptionType.CALL);

        assertEquals(trueSigma, impliedVol, 1e-5);
    }

    @Test
    void rejectsPriceBelowNoArbitrageLowerBound() {
        // A call priced below intrinsic value (S - K*e^-rT) is not arbitrage-free.
        double strike = 50;
        double tinyPrice = 1e-6;

        assertThrows(IllegalArgumentException.class,
                () -> ImpliedVolatilityCalculator.solve(tinyPrice, S, strike, T, R, Q, OptionType.CALL));
    }

    @Test
    void rejectsPriceAboveNoArbitrageUpperBound() {
        double tooHighPrice = S * Math.exp(-Q * T) + 1;

        assertThrows(IllegalArgumentException.class,
                () -> ImpliedVolatilityCalculator.solve(tooHighPrice, S, S, T, R, Q, OptionType.CALL));
    }
}
