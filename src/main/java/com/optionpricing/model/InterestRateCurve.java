// File: InterestRateCurve.java
package com.optionpricing.model;

import java.util.Collections;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.TreeMap;

/**
 * Represents an interest rate curve, mapping maturities to corresponding interest rates.
 * Provides methods to retrieve rates and discount factors through interpolation.
 */
public final class InterestRateCurve {
    /**
     * Navigable map where the key is maturity in years and the value is the interest rate.
     */
    private final NavigableMap<Double, Double> rates;

    /**
     * Constructs an InterestRateCurve with the specified rates.
     *
     * @param rates a NavigableMap where keys are maturities in years and values are interest rates
     * @throws NullPointerException     if rates is null
     * @throws IllegalArgumentException if rates map is empty, contains non-positive maturities, or negative rates
     */
    public InterestRateCurve(NavigableMap<Double, Double> rates) {
        Objects.requireNonNull(rates, "Rates map cannot be null.");
        if (rates.isEmpty()) {
            throw new IllegalArgumentException("Rates map cannot be empty.");
        }
        // Validate that maturities are positive and rates are non-negative
        for (Double maturity : rates.keySet()) {
            if (maturity <= 0) {
                throw new IllegalArgumentException("Maturity must be positive.");
            }
            if (rates.get(maturity) < 0) {
                throw new IllegalArgumentException("Rate cannot be negative.");
            }
        }
        // Create an unmodifiable navigable map to ensure immutability
        this.rates = Collections.unmodifiableNavigableMap(new TreeMap<>(rates));
    }

    /**
     * Retrieves the interest rate for a given maturity.
     * If the exact maturity is not present, performs linear interpolation between the closest maturities.
     * If the maturity is outside the provided curve, extrapolates using the closest rate.
     *
     * @param maturity the maturity in years for which to retrieve the rate
     * @return the interpolated or extrapolated interest rate
     */
    public double getRate(double maturity) {
        if (rates.containsKey(maturity)) {
            return rates.get(maturity);
        }

        if (maturity <= rates.firstKey()) {
            // Extrapolate below the curve using the first rate
            return rates.firstEntry().getValue();
        }
        if (maturity >= rates.lastKey()) {
            // Extrapolate above the curve using the last rate
            return rates.lastEntry().getValue();
        }

        // Find the closest lower and upper keys for interpolation
        double lowerKey = rates.floorKey(maturity);
        double upperKey = rates.ceilingKey(maturity);
        double lowerRate = rates.get(lowerKey);
        double upperRate = rates.get(upperKey);

        // Perform linear interpolation between the two rates
        return lowerRate + (upperRate - lowerRate) * (maturity - lowerKey) / (upperKey - lowerKey);
    }

    /**
     * Calculates the discount factor for a given maturity using the continuous compounding formula.
     *
     * @param maturity the maturity in years for which to calculate the discount factor
     * @return the discount factor
     */
    public double getDiscountFactor(double maturity) {
        double rate = getRate(maturity);
        return Math.exp(-rate * maturity);
    }

    /**
     * Returns an unmodifiable view of the rates map.
     *
     * @return the rates map
     */
    public NavigableMap<Double, Double> getRates() {
        return rates;
    }
}
