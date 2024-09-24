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

        MarketData data = new MarketData(curve, 0.2, 0.1, 0.05, 0.02, 100.0);
        assertEquals(curve, data.getInterestRateCurve());
        assertEquals(0.2, data.getVolatility());
        assertEquals(0.1, data.getDrift());
        assertEquals(0.05, data.getLambda());
        assertEquals(0.02, data.getGamma());
        assertEquals(100.0, data.getInitialPrice());
    }

    @Test
    public void testInvalidVolatility() {
        NavigableMap<Double, Double> rates = new TreeMap<>();
        rates.put(1.0, 0.05);
        InterestRateCurve curve = new InterestRateCurve(rates);

        assertThrows(IllegalArgumentException.class, () -> {
            new MarketData(curve, -0.2, 0.1, 0.05, 0.02, 100.0);
        });
    }

    @Test
    public void testInvalidInitialPrice() {
        NavigableMap<Double, Double> rates = new TreeMap<>();
        rates.put(1.0, 0.05);
        InterestRateCurve curve = new InterestRateCurve(rates);

        assertThrows(IllegalArgumentException.class, () -> {
            new MarketData(curve, 0.2, 0.1, 0.05, 0.02, -100.0);
        });
    }
}
