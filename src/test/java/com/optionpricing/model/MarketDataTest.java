package com.optionpricing.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.NavigableMap;
import java.util.TreeMap;

public class MarketDataTest {

    @Test
    public void testMarketDataCreation() {
        NavigableMap<Double, Double> rates = new TreeMap<>();
        rates.put(1.0, 0.05);
        InterestRateCurve curve = new InterestRateCurve(rates);

        MarketData data = new MarketData(curve, 0.2, 0.1, 0.05, 0.02, 0.1, 100.0, true);
        assertEquals(curve, data.getInterestRateCurve());
        assertEquals(0.2, data.getVolatility());
        assertEquals(0.1, data.getDrift());
        assertEquals(0.05, data.getLambda());
        assertEquals(0.02, data.getGamma());
        assertEquals(0.1, data.getJumpVolatility());
        assertEquals(100.0, data.getInitialPrice());
        assertTrue(data.isRiskNeutral());
    }

    @Test
    public void testInvalidVolatilityNegative() {
        NavigableMap<Double, Double> rates = new TreeMap<>();
        rates.put(1.0, 0.05);
        InterestRateCurve curve = new InterestRateCurve(rates);

        assertThrows(IllegalArgumentException.class, () -> {
            new MarketData(curve, -0.2, 0.1, 0.05, 0.02, 0.1, 100.0, true);
        });
    }

    @Test
    public void testValidVolatilityZero() {
        NavigableMap<Double, Double> rates = new TreeMap<>();
        rates.put(1.0, 0.05);
        InterestRateCurve curve = new InterestRateCurve(rates);

        MarketData data = new MarketData(curve, 0.0, 0.1, 0.05, 0.02, 0.1, 100.0, true);
        assertEquals(0.0, data.getVolatility());
    }

    @Test
    public void testInvalidInitialPriceZero() {
        NavigableMap<Double, Double> rates = new TreeMap<>();
        rates.put(1.0, 0.05);
        InterestRateCurve curve = new InterestRateCurve(rates);

        assertThrows(IllegalArgumentException.class, () -> {
            new MarketData(curve, 0.2, 0.1, 0.05, 0.02, 0.1, 0.0, true);
        });
    }

    @Test
    public void testInvalidInitialPriceNegative() {
        NavigableMap<Double, Double> rates = new TreeMap<>();
        rates.put(1.0, 0.05);
        InterestRateCurve curve = new InterestRateCurve(rates);

        assertThrows(IllegalArgumentException.class, () -> {
            new MarketData(curve, 0.2, 0.1, 0.05, 0.02, 0.1, -100.0, true);
        });
    }

    @Test
    public void testNullInterestRateCurve() {
        assertThrows(NullPointerException.class, () -> {
            new MarketData(null, 0.2, 0.1, 0.05, 0.02, 0.1, 100.0, true);
        });
    }

    @Test
    public void testNegativeLambda() {
        NavigableMap<Double, Double> rates = new TreeMap<>();
        rates.put(1.0, 0.05);
        InterestRateCurve curve = new InterestRateCurve(rates);

        assertThrows(IllegalArgumentException.class, () -> {
            new MarketData(curve, 0.2, 0.1, -0.05, 0.02, 0.1, 100.0, true);
        });
    }

    @Test
    public void testNegativeGamma() {
        NavigableMap<Double, Double> rates = new TreeMap<>();
        rates.put(1.0, 0.05);
        InterestRateCurve curve = new InterestRateCurve(rates);

        assertThrows(IllegalArgumentException.class, () -> {
            new MarketData(curve, 0.2, 0.1, 0.05, -0.02, 0.1, 100.0, true);
        });
    }

    @Test
    public void testNegativeJumpVolatility() {
        NavigableMap<Double, Double> rates = new TreeMap<>();
        rates.put(1.0, 0.05);
        InterestRateCurve curve = new InterestRateCurve(rates);

        assertThrows(IllegalArgumentException.class, () -> {
            new MarketData(curve, 0.2, 0.1, 0.05, 0.02, -0.1, 100.0, true);
        });
    }

    @Test
    public void testZeroLambdaGammaJumpVolatility() {
        NavigableMap<Double, Double> rates = new TreeMap<>();
        rates.put(1.0, 0.05);
        InterestRateCurve curve = new InterestRateCurve(rates);

        MarketData data = new MarketData(curve, 0.2, 0.1, 0.0, 0.0, 0.0, 100.0, true);
        assertEquals(0.0, data.getLambda());
        assertEquals(0.0, data.getGamma());
        assertEquals(0.0, data.getJumpVolatility());
    }
}
