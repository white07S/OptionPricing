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
    public void testInterestRateExtrapolationBelow() {
        NavigableMap<Double, Double> rates = new TreeMap<>();
        rates.put(1.0, 0.02);
        rates.put(2.0, 0.03);

        InterestRateCurve curve = new InterestRateCurve(rates);
        double extrapolatedRate = curve.getRate(0.5);
        assertEquals(0.02, extrapolatedRate, 1e-6);
    }

    @Test
    public void testInterestRateExtrapolationAbove() {
        NavigableMap<Double, Double> rates = new TreeMap<>();
        rates.put(1.0, 0.02);
        rates.put(2.0, 0.03);

        InterestRateCurve curve = new InterestRateCurve(rates);
        double extrapolatedRate = curve.getRate(3.0);
        assertEquals(0.03, extrapolatedRate, 1e-6);
    }

    @Test
    public void testInvalidMaturitiesNegative() {
        NavigableMap<Double, Double> rates = new TreeMap<>();
        rates.put(-1.0, 0.02);

        assertThrows(IllegalArgumentException.class, () -> {
            new InterestRateCurve(rates);
        });
    }

    @Test
    public void testInvalidMaturitiesZero() {
        NavigableMap<Double, Double> rates = new TreeMap<>();
        rates.put(0.0, 0.02);

        assertThrows(IllegalArgumentException.class, () -> {
            new InterestRateCurve(rates);
        });
    }

    @Test
    public void testNegativeRate() {
        NavigableMap<Double, Double> rates = new TreeMap<>();
        rates.put(1.0, -0.02);

        assertThrows(IllegalArgumentException.class, () -> {
            new InterestRateCurve(rates);
        });
    }

    @Test
    public void testNullRatesMap() {
        assertThrows(NullPointerException.class, () -> {
            new InterestRateCurve(null);
        });
    }

    @Test
    public void testEmptyRatesMap() {
        NavigableMap<Double, Double> rates = new TreeMap<>();

        assertThrows(IllegalArgumentException.class, () -> {
            new InterestRateCurve(rates);
        });
    }

    @Test
    public void testGetDiscountFactor() {
        NavigableMap<Double, Double> rates = new TreeMap<>();
        rates.put(1.0, 0.05);

        InterestRateCurve curve = new InterestRateCurve(rates);
        double discountFactor = curve.getDiscountFactor(1.0);
        assertEquals(Math.exp(-0.05 * 1.0), discountFactor, 1e-6);
    }

    @Test
    public void testGetRatesUnmodifiable() {
        NavigableMap<Double, Double> rates = new TreeMap<>();
        rates.put(1.0, 0.05);

        InterestRateCurve curve = new InterestRateCurve(rates);
        NavigableMap<Double, Double> returnedRates = curve.getRates();
        assertThrows(UnsupportedOperationException.class, () -> {
            returnedRates.put(2.0, 0.06);
        });
    }
}
