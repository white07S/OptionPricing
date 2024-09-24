// File: PathGenerator.java
package com.optionpricing.simulation;

import com.optionpricing.model.MarketData;
import com.optionpricing.model.Option;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Generates simulated price paths for the underlying asset based on the specified market data and option.
 */
public class PathGenerator {
    /**
     * The market data required for generating the price paths.
     */
    private final MarketData marketData;

    /**
     * Constructs a PathGenerator with the specified market data and option.
     *
     * @param marketData the market data used for simulation
     * @param option     the option being priced
     * @param numTimeSteps the number of time steps in the simulation (currently unused but may be useful for extensions)
     * @throws NullPointerException if marketData or option is null
     */
    public PathGenerator(MarketData marketData, Option option, int numTimeSteps) {
        this.marketData = marketData;
    }

    /**
     * Generates a simulated price path for the underlying asset.
     *
     * @param numTimeSteps the number of discrete time steps in the simulation
     * @param dt           the size of each time step (in years)
     * @return an array of asset prices at each time step, including the initial price
     */
    public double[] generatePricePath(int numTimeSteps, double dt) {
        double[] prices = new double[numTimeSteps + 1];
        double S = marketData.getInitialPrice();
        prices[0] = S;

        double volatility = marketData.getVolatility();
        double lambda = marketData.getLambda();
        double gamma = marketData.getGamma();

        // ThreadLocalRandom is used for efficient and thread-safe random number generation
        ThreadLocalRandom random = ThreadLocalRandom.current();

        for (int i = 1; i <= numTimeSteps; i++) {
            double t = i * dt;
            // Retrieve the interest rate for the current time
            double r = marketData.getInterestRateCurve().getRate(t);

            // Calculate the drift component adjusted for jump intensity and size
            double drift = r - lambda * Math.log(1 + gamma) - 0.5 * Math.pow(volatility, 2);

            // Generate a Wiener process increment
            double dW = random.nextGaussian() * Math.sqrt(dt);
            // Determine the number of jumps using a Poisson distribution
            int jumps = randomPoisson(lambda * dt, random);
            // Calculate the jump factor based on the number of jumps
            double jumpFactor = Math.pow(1.0 + gamma, jumps);

            // Calculate the change in the logarithm of the asset price
            double dS = drift * dt + volatility * dW + Math.log(jumpFactor);
            // Update the asset price
            S *= Math.exp(dS);
            prices[i] = S;
        }
        return prices;
    }

    /**
     * Generates a random number of jumps based on the Poisson distribution with parameter lambdaDt.
     *
     * @param lambdaDt the expected number of jumps in the time step (λΔt)
     * @param random   the random number generator
     * @return the number of jumps (non-negative integer)
     */
    private int randomPoisson(double lambdaDt, ThreadLocalRandom random) {
        if (lambdaDt <= 0.0) {
            return 0;
        }
        double L = Math.exp(-lambdaDt);
        int k = 0;
        double p = 1.0;
        do {
            k++;
            p *= random.nextDouble();
        } while (p > L);
        return k - 1;
    }
}
