package com.optionpricing.simulation;

import com.optionpricing.model.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.List;
import java.util.NavigableMap;
import java.util.TreeMap;

public class MonteCarloSimulationLogicTest {

    @Test
    public void testSimulateEuropeanOption() throws Exception {
        // Setup market data
        NavigableMap<Double, Double> rates = new TreeMap<>();
        rates.put(1.0, 0.05);
        InterestRateCurve curve = new InterestRateCurve(rates);

        MarketData marketData = new MarketData(curve, 0.2, 0.0, 0.0, 0.0, 0.0, 100.0, true);
        Option option = new EuropeanOption(100.0, 1.0, OptionType.CALL);

        MonteCarloSimulationLogic simulationLogic = new MonteCarloSimulationLogic(option, marketData, 100000, 4);

        // Run the simulation directly
        double price = simulationLogic.call();

        // Assert that the price is positive
        assertTrue(price > 0, "Option price should be positive");
    }

    @Test
    public void testSimulateAmericanOption() throws Exception {
        // Setup market data
        NavigableMap<Double, Double> rates = new TreeMap<>();
        rates.put(1.0, 0.05);
        InterestRateCurve curve = new InterestRateCurve(rates);

        MarketData marketData = new MarketData(curve, 0.2, 0.0, 0.0, 0.0, 0.0, 100.0, true);
        Option option = new AmericanOption(100.0, 1.0, OptionType.CALL);

        MonteCarloSimulationLogic simulationLogic = new MonteCarloSimulationLogic(option, marketData, 100000, 4);

        // Run the simulation directly
        double price = simulationLogic.call();

        // Assert that the price is positive
        assertTrue(price > 0, "Option price should be positive");
    }

    @Test
    public void testSimulateBermudanOption() throws Exception {
        // Setup market data
        NavigableMap<Double, Double> rates = new TreeMap<>();
        rates.put(1.0, 0.05);
        InterestRateCurve curve = new InterestRateCurve(rates);

        List<Double> exerciseDates = Arrays.asList(0.5, 0.75);

        MarketData marketData = new MarketData(curve, 0.2, 0.0, 0.0, 0.0, 0.0, 100.0, true);
        Option option = new BermudanOption(100.0, 1.0, OptionType.CALL, exerciseDates);

        MonteCarloSimulationLogic simulationLogic = new MonteCarloSimulationLogic(option, marketData, 100000, 4);

        // Run the simulation directly
        double price = simulationLogic.call();

        // Assert that the price is positive
        assertTrue(price > 0, "Option price should be positive");
    }

    @Test
    public void testInvalidNumberOfSimulations() {
        // Setup market data
        NavigableMap<Double, Double> rates = new TreeMap<>();
        rates.put(1.0, 0.05);
        InterestRateCurve curve = new InterestRateCurve(rates);

        MarketData marketData = new MarketData(curve, 0.2, 0.0, 0.0, 0.0, 0.0, 100.0, true);
        Option option = new EuropeanOption(100.0, 1.0, OptionType.CALL);

        assertThrows(IllegalArgumentException.class, () -> {
            new MonteCarloSimulationLogic(option, marketData, -10000, 4);
        });
    }

    @Test
    public void testInvalidThreadPoolSize() {
        // Setup market data
        NavigableMap<Double, Double> rates = new TreeMap<>();
        rates.put(1.0, 0.05);
        InterestRateCurve curve = new InterestRateCurve(rates);

        MarketData marketData = new MarketData(curve, 0.2, 0.0, 0.0, 0.0, 0.0, 100.0, true);
        Option option = new EuropeanOption(100.0, 1.0, OptionType.CALL);

        assertThrows(IllegalArgumentException.class, () -> {
            new MonteCarloSimulationLogic(option, marketData, 10000, 0);
        });
    }

    @Test
    public void testNullOption() {
        // Setup market data
        NavigableMap<Double, Double> rates = new TreeMap<>();
        rates.put(1.0, 0.05);
        InterestRateCurve curve = new InterestRateCurve(rates);

        MarketData marketData = new MarketData(curve, 0.2, 0.0, 0.0, 0.0, 0.0, 100.0, true);

        assertThrows(NullPointerException.class, () -> {
            new MonteCarloSimulationLogic(null, marketData, 10000, 4);
        });
    }

    @Test
    public void testNullMarketData() {
        Option option = new EuropeanOption(100.0, 1.0, OptionType.CALL);

        assertThrows(NullPointerException.class, () -> {
            new MonteCarloSimulationLogic(option, null, 10000, 4);
        });
    }
}
