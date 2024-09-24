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
        rates.put(1.0, 0.05);
        InterestRateCurve curve = new InterestRateCurve(rates);

        MarketData marketData = new MarketData(curve, 0.2, 0.1, 0.05, 0.02, 100.0);
        Option option = new EuropeanOption(100.0, 1.0, OptionType.CALL);

        PathGenerator generator = new PathGenerator(marketData, option, 100);
        double[] prices = generator.generatePricePath(100, 0.01);

        assertEquals(101, prices.length);
        assertEquals(100.0, prices[0]);

        // Check that prices are positive
        for (double price : prices) {
            assertTrue(price > 0);
        }
    }
}
