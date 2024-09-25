package com.optionpricing.simulation;

import com.optionpricing.model.*;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.List;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

import javafx.application.Platform;

public class MonteCarloSimulatorTest {

    private static final AtomicBoolean isJavaFxInitialized = new AtomicBoolean(false);

    @BeforeAll
    public static void initJavaFx() throws InterruptedException {
        if (isJavaFxInitialized.get()) {
            return;
        }
        final CountDownLatch latch = new CountDownLatch(1);
        Platform.startup(() -> {
            // No need to do anything here
            isJavaFxInitialized.set(true);
            latch.countDown();
        });
        latch.await();
    }

    @Test
    public void testSimulateEuropeanOption() throws Exception {
        // Setup market data
        NavigableMap<Double, Double> rates = new TreeMap<>();
        rates.put(1.0, 0.05);
        InterestRateCurve curve = new InterestRateCurve(rates);

        MarketData marketData = new MarketData(curve, 0.2, 0.0, 0.0, 0.0, 0.0, 100.0, true);
        Option option = new EuropeanOption(100.0, 1.0, OptionType.CALL);

        MonteCarloSimulator simulator = new MonteCarloSimulator(option, marketData, 100000, 4);

        // Run the simulation
        Platform.runLater(simulator);

        // Wait for the simulation to complete and get the result
        double price = simulator.get(); // This blocks until the computation is complete

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

        MonteCarloSimulator simulator = new MonteCarloSimulator(option, marketData, 100000, 4);

        // Run the simulation
        Platform.runLater(simulator);

        // Wait for the simulation to complete and get the result
        double price = simulator.get(); // This blocks until the computation is complete

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

        MonteCarloSimulator simulator = new MonteCarloSimulator(option, marketData, 100000, 4);

        // Run the simulation
        Platform.runLater(simulator);

        // Wait for the simulation to complete and get the result
        double price = simulator.get(); // This blocks until the computation is complete

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
            new MonteCarloSimulator(option, marketData, -10000, 4);
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
            new MonteCarloSimulator(option, marketData, 10000, 0);
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
            new MonteCarloSimulator(null, marketData, 10000, 4);
        });
    }

    @Test
    public void testNullMarketData() {
        Option option = new EuropeanOption(100.0, 1.0, OptionType.CALL);

        assertThrows(NullPointerException.class, () -> {
            new MonteCarloSimulator(option, null, 10000, 4);
        });
    }
}
