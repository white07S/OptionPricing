package com.optionpricing.simulation;

import com.optionpricing.model.*;
import javafx.concurrent.Task;

import java.util.Objects;

/**
 * MonteCarloSimulator extends JavaFX Task to perform Monte Carlo simulations asynchronously.
 * It uses MonteCarloSimulationLogic to execute the simulation logic.
 */
public class MonteCarloSimulator extends Task<Double> {
    private final MonteCarloSimulationLogic simulationLogic;

    /**
     * Constructs a MonteCarloSimulator with the specified parameters.
     *
     * @param option         the option to be priced
     * @param marketData     the market data required for simulation
     * @param numSimulations the total number of Monte Carlo simulations to run (must be positive)
     * @param threadPoolSize the number of threads to use for parallel simulations (must be positive)
     * @throws NullPointerException     if option or marketData is null
     * @throws IllegalArgumentException if numSimulations or threadPoolSize are not positive
     */
    public MonteCarloSimulator(Option option, MarketData marketData, int numSimulations, int threadPoolSize) {
        Objects.requireNonNull(option, "Option cannot be null.");
        Objects.requireNonNull(marketData, "MarketData cannot be null.");
        if (numSimulations <= 0) {
            throw new IllegalArgumentException("Number of simulations must be positive.");
        }
        if (threadPoolSize <= 0) {
            throw new IllegalArgumentException("Thread pool size must be positive.");
        }
        this.simulationLogic = new MonteCarloSimulationLogic(option, marketData, numSimulations, threadPoolSize);
    }

    /**
     * The main computation method that performs the Monte Carlo simulation.
     *
     * @return the estimated option price
     * @throws Exception if the simulation fails
     */
    @Override
    protected Double call() throws Exception {
        return simulationLogic.call();
    }
}
