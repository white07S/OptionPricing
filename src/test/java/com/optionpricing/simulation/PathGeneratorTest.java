package com.optionpricing.simulation;

import com.optionpricing.model.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.NavigableMap;
import java.util.TreeMap;

public class PathGeneratorTest {

    @Test
    public void testGeneratePricePath() {
        // Setup market data
        NavigableMap<Double, Double> rates = new TreeMap<>();
        rates.put(1.0, 0.05); // Corrected maturity

        InterestRateCurve curve = new InterestRateCurve(rates);

        MarketData marketData = new MarketData(curve, 0.2, 0.05, 0.1, 0.02, 0.1, 100.0, true);
        Option option = new EuropeanOption(100.0, 1.0, OptionType.CALL);

        PathGenerator generator = new PathGenerator(marketData, option, 100);

        // Prepare the prices array
        int numTimeSteps = 100;
        double dt = option.getMaturity() / numTimeSteps;
        double[] prices = new double[numTimeSteps + 1];

        // Call the generatePricePath method
        generator.generatePricePath(prices, dt);

        assertEquals(numTimeSteps + 1, prices.length);
        assertEquals(100.0, prices[0], 1e-6);

        // Check that prices are positive
        for (double price : prices) {
            assertTrue(price > 0, "Price should be positive");
        }
    }

    @Test
    public void testGeneratePricePathRiskNeutral() {
        // Setup market data
        NavigableMap<Double, Double> rates = new TreeMap<>();
        rates.put(1.0, 0.05);

        InterestRateCurve curve = new InterestRateCurve(rates);

        MarketData marketData = new MarketData(curve, 0.2, 0.05, 0.0, 0.0, 0.0, 100.0, true);
        Option option = new EuropeanOption(100.0, 1.0, OptionType.CALL);

        PathGenerator generator = new PathGenerator(marketData, option, 100);

        int numTimeSteps = 100;
        double dt = option.getMaturity() / numTimeSteps;
        double[] prices = new double[numTimeSteps + 1];

        generator.generatePricePath(prices, dt);

        assertEquals(numTimeSteps + 1, prices.length);
        assertEquals(100.0, prices[0], 1e-6);
    }

    @Test
    public void testGeneratePricePathRealWorldMeasure() {
        // Setup market data
        NavigableMap<Double, Double> rates = new TreeMap<>();
        rates.put(1.0, 0.05);

        InterestRateCurve curve = new InterestRateCurve(rates);

        MarketData marketData = new MarketData(curve, 0.2, 0.08, 0.0, 0.0, 0.0, 100.0, false);
        Option option = new EuropeanOption(100.0, 1.0, OptionType.CALL);

        PathGenerator generator = new PathGenerator(marketData, option, 100);

        int numTimeSteps = 100;
        double dt = option.getMaturity() / numTimeSteps;
        double[] prices = new double[numTimeSteps + 1];

        generator.generatePricePath(prices, dt);

        assertEquals(numTimeSteps + 1, prices.length);
        assertEquals(100.0, prices[0], 1e-6);
    }

    @Test
    public void testNullMarketData() {
        Option option = new EuropeanOption(100.0, 1.0, OptionType.CALL);

        assertThrows(NullPointerException.class, () -> {
            new PathGenerator(null, option, 100);
        });
    }
}
