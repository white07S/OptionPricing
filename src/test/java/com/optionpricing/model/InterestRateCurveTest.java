package com.optionpricing.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.NavigableMap;
import java.util.TreeMap;

public class InterestRateCurveTest {

    @Test
    public void testInterestRateCurveCreation() {
        NavigableMap<Double, Double> rates = new TreeMap<>();
        rates.put(0.5, 0.02);
        rates.put(1.0, 0.025);
        rates.put(2.0, 0.03);

        InterestRateCurve curve = new InterestRateCurve(rates);
        assertEquals(0.02, curve.getRate(0.5));
        assertEquals(0.025, curve.getRate(1.0));
        assertEquals(0.03, curve.getRate(2.0));
    }

    @Test
    public void testInterestRateInterpolation() {
        NavigableMap<Double, Double> rates = new TreeMap<>();
        rates.put(1.0, 0.02);
        rates.put(2.0, 0.03);

        InterestRateCurve curve = new InterestRateCurve(rates);
        double interpolatedRate = curve.getRate(1.5);
        assertEquals(0.025, interpolatedRate, 1e-6);
    }

    @Test
    public void testInvalidMaturities() {
        NavigableMap<Double, Double> rates = new TreeMap<>();
        rates.put(-1.0, 0.02);

        assertThrows(IllegalArgumentException.class, () -> {
            new InterestRateCurve(rates);
        });
    }
}
