package com.optionpricing.simulation;

import com.optionpricing.model.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.NavigableMap;
import java.util.TreeMap;

public class MonteCarloSimulatorTest {

    @Test
    public void testSimulateEuropeanOption() throws Exception {
        // Setup market data
        NavigableMap<Double, Double> rates = new TreeMap<>();
        rates.put(1.0, 0.05);
        InterestRateCurve curve = new InterestRateCurve(rates);

        MarketData marketData = new MarketData(curve, 0.2, 0.1, 0.05, 0.02, 100.0);
        Option option = new EuropeanOption(100.0, 1.0, OptionType.CALL);

        MonteCarloSimulator simulator = new MonteCarloSimulator(option, marketData, 10000, 4);
        simulator.setOnSucceeded(event -> {
            double price = simulator.getValue();
            assertTrue(price > 0);
        });

        Thread simulationThread = new Thread(simulator);
        simulationThread.start();
        simulationThread.join();
    }

    @Test
    public void testSimulateAmericanOption() throws Exception {
        // Similar setup for American option
    }

    @Test
    public void testInvalidNumberOfSimulations() {
        // Setup market data
        NavigableMap<Double, Double> rates = new TreeMap<>();
        rates.put(1.0, 0.05);
        InterestRateCurve curve = new InterestRateCurve(rates);

        MarketData marketData = new MarketData(curve, 0.2, 0.1, 0.05, 0.02, 100.0);
        Option option = new EuropeanOption(100.0, 1.0, OptionType.CALL);

        assertThrows(IllegalArgumentException.class, () -> {
            new MonteCarloSimulator(option, marketData, -10000, 4);
        });
    }
}
