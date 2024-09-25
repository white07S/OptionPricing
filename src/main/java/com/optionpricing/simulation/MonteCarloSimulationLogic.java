package com.optionpricing.simulation;

import com.optionpricing.model.*;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.math3.stat.regression.OLSMultipleLinearRegression;

/**
 * MonteCarloSimulationLogic performs the actual Monte Carlo simulations to estimate the price of a given option.
 * It supports European, American, and Bermudan options and utilizes multi-threading to parallelize simulations.
 * This class is independent of JavaFX and can be tested without initializing the JavaFX toolkit.
 */
public class MonteCarloSimulationLogic implements Callable<Double> {
    private static final Logger LOGGER = Logger.getLogger(MonteCarloSimulationLogic.class.getName());

    private final Option option;
    private final MarketData marketData;
    private final int numSimulations;
    private final int threadPoolSize;
    private final ExecutorService executorService;

    /**
     * Constructs a MonteCarloSimulationLogic with the specified parameters.
     *
     * @param option         the option to be priced
     * @param marketData     the market data required for simulation
     * @param numSimulations the total number of Monte Carlo simulations to run (must be positive)
     * @param threadPoolSize the number of threads to use for parallel simulations (must be positive)
     * @throws NullPointerException     if option or marketData is null
     * @throws IllegalArgumentException if numSimulations or threadPoolSize are not positive
     */
    public MonteCarloSimulationLogic(Option option, MarketData marketData, int numSimulations, int threadPoolSize) {
        this.option = Objects.requireNonNull(option, "Option cannot be null.");
        this.marketData = Objects.requireNonNull(marketData, "MarketData cannot be null.");
        if (numSimulations <= 0) {
            throw new IllegalArgumentException("Number of simulations must be positive.");
        }
        if (threadPoolSize <= 0) {
            throw new IllegalArgumentException("Thread pool size must be positive.");
        }
        this.numSimulations = numSimulations;
        this.threadPoolSize = threadPoolSize;
        // Initialize a fixed thread pool for parallel simulations
        this.executorService = Executors.newFixedThreadPool(threadPoolSize);
    }

    /**
     * The main computation method that performs the Monte Carlo simulation.
     *
     * @return the estimated option price
     * @throws Exception if the simulation fails
     */
    @Override
    public Double call() throws Exception {
        try {
            // Determine the type of option and perform the corresponding simulation
            switch (option.getExerciseType()) {
                case EUROPEAN:
                    return simulateEuropeanOption();
                case AMERICAN:
                    return simulateAmericanOption();
                case BERMUDAN:
                    return simulateBermudanOption();
                default:
                    throw new IllegalArgumentException("Unsupported exercise type: " + option.getExerciseType());
            }
        } catch (Exception e) {
            // Log any exceptions that occur during simulation
            LOGGER.log(Level.SEVERE, "Simulation failed", e);
            throw e;
        } finally {
            // Ensure that the executor service is properly shut down
            executorService.shutdownNow();
        }
    }

    /**
     * Simulates the price of a European option using Monte Carlo simulations.
     *
     * @return the estimated European option price
     * @throws InterruptedException if the simulation is interrupted
     * @throws ExecutionException   if any simulation task fails
     */
    private Double simulateEuropeanOption() throws InterruptedException, ExecutionException {
        List<Callable<Double>> tasks = new ArrayList<>();
        int simulationsPerThread = numSimulations / threadPoolSize;
        int remainingSimulations = numSimulations % threadPoolSize;

        // Create simulation tasks for each thread
        for (int i = 0; i < threadPoolSize; i++) {
            final int simulationsForThisThread = simulationsPerThread + (i < remainingSimulations ? 1 : 0);
            tasks.add(() -> {
                double payoffSum = 0.0;
                PathGenerator pathGenerator = new PathGenerator(marketData, option, 100);

                double dt = option.getMaturity() / 100.0;
                double[] path = new double[101]; // Reuse the same array for the path

                for (int j = 0; j < simulationsForThisThread; j++) {
                    // Generate a simulated price path
                    pathGenerator.generatePricePath(path, dt);
                    // Calculate the payoff for this path
                    double payoff = calculatePayoff(path);
                    payoffSum += payoff;
                }
                return payoffSum;
            });
        }

        // Execute all simulation tasks in parallel
        List<Future<Double>> futures = executorService.invokeAll(tasks);

        double totalPayoff = 0.0;
        // Aggregate the results from all threads
        for (Future<Double> future : futures) {
            totalPayoff += future.get();
        }

        // Calculate the average payoff and discount it to present value
        double discountedPayoff = marketData.getInterestRateCurve().getDiscountFactor(option.getMaturity()) * (totalPayoff / numSimulations);

        return discountedPayoff;
    }

    /**
     * Simulates the price of an American option using Monte Carlo simulations.
     *
     * @return the estimated American option price
     * @throws InterruptedException if the simulation is interrupted
     * @throws ExecutionException   if any simulation task fails
     */
    private Double simulateAmericanOption() throws InterruptedException, ExecutionException {
        return simulateAmericanOrBermudanOption(true);
    }

    /**
     * Simulates the price of a Bermudan option using Monte Carlo simulations.
     *
     * @return the estimated Bermudan option price
     * @throws InterruptedException if the simulation is interrupted
     * @throws ExecutionException   if any simulation task fails
     */
    private Double simulateBermudanOption() throws InterruptedException, ExecutionException {
        return simulateAmericanOrBermudanOption(false);
    }

    /**
     * Performs the simulation for American or Bermudan options.
     *
     * @param isAmerican true if simulating an American option, false for Bermudan
     * @return the estimated option price
     * @throws InterruptedException if the simulation is interrupted
     * @throws ExecutionException   if any simulation task fails
     */
    private Double simulateAmericanOrBermudanOption(boolean isAmerican) throws InterruptedException, ExecutionException {
        final int numTimeSteps = 50;
        final double dt = option.getMaturity() / numTimeSteps;

        // Determine the time steps at which the option can be exercised
        Set<Integer> exerciseSteps = new HashSet<>();
        if (isAmerican) {
            // For American options, add all time steps as possible exercise dates
            for (int t = 1; t <= numTimeSteps; t++) {
                exerciseSteps.add(t);
            }
        } else {
            // For Bermudan options, add specific exercise dates
            List<Double> exerciseDates = ((BermudanOption) option).getExerciseDates();
            for (double date : exerciseDates) {
                int step = (int) Math.round(date / dt);
                if (step >= 1 && step <= numTimeSteps) {
                    exerciseSteps.add(step);
                }
            }
        }

        int simulationsPerThread = numSimulations / threadPoolSize;
        int remainingSimulations = numSimulations % threadPoolSize;

        // Preallocate arrays for price paths and cash flows
        double[][] pricePaths = new double[numSimulations][numTimeSteps + 1];
        double[][] cashFlows = new double[numSimulations][numTimeSteps + 1];

        List<Callable<Void>> tasks = new ArrayList<>();
        int simulationIndex = 0;

        // Create simulation tasks for each thread
        for (int i = 0; i < threadPoolSize; i++) {
            final int startSimulation = simulationIndex;
            final int simulationsForThisThread = simulationsPerThread + (i < remainingSimulations ? 1 : 0);
            simulationIndex += simulationsForThisThread;

            tasks.add(() -> {
                PathGenerator pathGenerator = new PathGenerator(marketData, option, numTimeSteps);
                double[] path = new double[numTimeSteps + 1];

                for (int j = 0; j < simulationsForThisThread; j++) {
                    int simulationIdx = startSimulation + j;

                    // Generate a simulated price path
                    pathGenerator.generatePricePath(path, dt);
                    System.arraycopy(path, 0, pricePaths[simulationIdx], 0, numTimeSteps + 1);

                    // Initialize cash flows to zero
                    Arrays.fill(cashFlows[simulationIdx], 0.0);
                }
                return null;
            });
        }

        // Execute all simulation tasks in parallel
        List<Future<Void>> futures = executorService.invokeAll(tasks);

        // Ensure all simulations are completed
        for (Future<Void> future : futures) {
            future.get();
        }

        // Initialize cash flows at maturity based on payoff
        for (int i = 0; i < numSimulations; i++) {
            double S_T = pricePaths[i][numTimeSteps];
            cashFlows[i][numTimeSteps] = calculateImmediatePayoff(S_T);
        }

        // Perform backward induction to determine optimal exercise strategy
        for (int t = numTimeSteps - 1; t >= 1; t--) {
            final double rate = marketData.getInterestRateCurve().getRate(t * dt);
            final double discountFactor = Math.exp(-rate * dt);

            if (!exerciseSteps.contains(t)) {
                // Not an exercise date, discount future cash flows
                for (int i = 0; i < numSimulations; i++) {
                    cashFlows[i][t] = cashFlows[i][t + 1] * discountFactor;
                }
                continue;
            }

            // Collect in-the-money paths at the current time step
            List<Double> inMoneyPrices = new ArrayList<>();
            List<Double> inMoneyCashFlows = new ArrayList<>();
            List<Integer> inMoneyIndices = new ArrayList<>();

            for (int i = 0; i < numSimulations; i++) {
                double S_t = pricePaths[i][t];
                double immediatePayoff = calculateImmediatePayoff(S_t);

                if (immediatePayoff > 0) {
                    double discountedCashFlow = cashFlows[i][t + 1] * discountFactor;
                    inMoneyPrices.add(S_t);
                    inMoneyCashFlows.add(discountedCashFlow);
                    inMoneyIndices.add(i);
                }
            }

            if (!inMoneyPrices.isEmpty()) {
                // Perform regression to estimate continuation values
                double[] regressionCoefficients = regress(inMoneyPrices, inMoneyCashFlows);

                // Decide whether to exercise or continue holding the option
                for (int idx = 0; idx < inMoneyPrices.size(); idx++) {
                    int i = inMoneyIndices.get(idx);
                    double S_t = inMoneyPrices.get(idx);
                    double immediatePayoff = calculateImmediatePayoff(S_t);

                    // Estimate the continuation value using regression coefficients
                    double continuationValue = regressionCoefficients[0]
                            + regressionCoefficients[1] * S_t
                            + regressionCoefficients[2] * S_t * S_t;

                    if (immediatePayoff >= continuationValue) {
                        // Optimal to exercise the option at this time step
                        cashFlows[i][t] = immediatePayoff;
                        // Zero out future cash flows as the option has been exercised
                        Arrays.fill(cashFlows[i], t + 1, cashFlows[i].length, 0.0);
                    } else {
                        // Continue holding the option, discount future cash flows
                        cashFlows[i][t] = cashFlows[i][t + 1] * discountFactor;
                    }
                }

                // For paths that are out-of-the-money, continue holding the option
                Set<Integer> inMoneySet = new HashSet<>(inMoneyIndices);
                for (int i = 0; i < numSimulations; i++) {
                    if (!inMoneySet.contains(i)) {
                        cashFlows[i][t] = cashFlows[i][t + 1] * discountFactor;
                    }
                }
            } else {
                // No in-the-money paths, discount future cash flows for all simulations
                for (int i = 0; i < numSimulations; i++) {
                    cashFlows[i][t] = cashFlows[i][t + 1] * discountFactor;
                }
            }
        }

        // Calculate the average option price by discounting the cash flows at the initial rate
        double optionPrice = 0.0;
        double initialRate = marketData.getInterestRateCurve().getRate(0);
        double initialDiscountFactor = Math.exp(-initialRate * (option.getMaturity() / numTimeSteps));

        for (int i = 0; i < numSimulations; i++) {
            optionPrice += cashFlows[i][1] * initialDiscountFactor;
        }

        optionPrice /= numSimulations;

        return optionPrice;
    }

    /**
     * Calculates the payoff of the option based on the simulated price path at maturity.
     *
     * @param path the simulated price path of the underlying asset
     * @return the option payoff
     */
    private double calculatePayoff(double[] path) {
        double S_T = path[path.length - 1];
        return calculateImmediatePayoff(S_T);
    }

    /**
     * Calculates the immediate payoff of the option at a given asset price.
     *
     * @param S the asset price at the current time step
     * @return the immediate option payoff
     */
    private double calculateImmediatePayoff(double S) {
        if (option.getOptionType() == OptionType.CALL) {
            return Math.max(0.0, S - option.getStrikePrice());
        } else {
            return Math.max(0.0, option.getStrikePrice() - S);
        }
    }

    /**
     * Performs an Ordinary Least Squares (OLS) regression to estimate the continuation value of the option.
     *
     * @param S the list of asset prices at the current exercise step
     * @param Y the list of discounted future cash flows corresponding to each asset price
     * @return an array of regression coefficients [intercept, beta1, beta2]
     */
    private double[] regress(List<Double> S, List<Double> Y) {
        if (S.isEmpty() || Y.isEmpty() || S.size() != Y.size()) {
            throw new IllegalArgumentException("Invalid data for regression.");
        }

        int n = S.size();

        // Prepare the regression model with basis functions: S and S^2
        double[][] X = new double[n][2]; // Excluding the intercept as it is handled by the regression model
        double[] Y_array = new double[n];

        for (int i = 0; i < n; i++) {
            double s = S.get(i);
            X[i][0] = s;
            X[i][1] = s * s;
            Y_array[i] = Y.get(i);
        }

        // Initialize the regression model
        OLSMultipleLinearRegression regression = new OLSMultipleLinearRegression();
        regression.setNoIntercept(false); // Include intercept in the model
        regression.newSampleData(Y_array, X);

        double[] beta;
        try {
            // Estimate the regression parameters
            beta = regression.estimateRegressionParameters(); // [intercept, beta1, beta2]
        } catch (Exception e) {
            // Log the exception and default to zero continuation value if regression fails
            LOGGER.log(Level.WARNING, "Regression failed, defaulting to zero continuation value.", e);
            return new double[]{0.0, 0.0, 0.0};
        }

        return beta;
    }
}
