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
    private final double initialPrice;
    private final double volatility;
    private final double lambda;
    private final double gamma;

    /**
     * Constructs a PathGenerator with the specified market data and option.
     *
     * @param marketData   the market data used for simulation
     * @param option       the option being priced
     * @param numTimeSteps the number of time steps in the simulation (currently unused but may be useful for extensions)
     * @throws NullPointerException if marketData or option is null
     */
    public PathGenerator(MarketData marketData, Option option, int numTimeSteps) {
        this.marketData = marketData;
        this.initialPrice = marketData.getInitialPrice();
        this.volatility = marketData.getVolatility();
        this.lambda = marketData.getLambda();
        this.gamma = marketData.getGamma();
    }

    /**
     * Generates a simulated price path for the underlying asset.
     *
     * @param prices an array to store the asset prices at each time step, including the initial price
     * @param dt     the size of each time step (in years)
     */
    public void generatePricePath(double[] prices, double dt) {
        int numTimeSteps = prices.length - 1;
        double S = initialPrice;
        prices[0] = S;

        // ThreadLocalRandom is used for efficient and thread-safe random number generation
        ThreadLocalRandom random = ThreadLocalRandom.current();

        double sqrtDt = Math.sqrt(dt);
        double volSquared = volatility * volatility;
        double logOnePlusGamma = Math.log(1 + gamma);

        for (int i = 1; i <= numTimeSteps; i++) {
            double t = i * dt;
            // Retrieve the interest rate for the current time
            double r = marketData.getInterestRateCurve().getRate(t);

            // Calculate the drift component adjusted for jump intensity and size
            double drift = r - lambda * logOnePlusGamma - 0.5 * volSquared;

            // Generate a Wiener process increment
            double dW = random.nextGaussian() * sqrtDt;

            // Determine the number of jumps using a Poisson distribution
            int jumps = randomPoisson(lambda * dt, random);

            // Calculate the jump factor based on the number of jumps
            double jumpFactor = Math.exp(jumps * logOnePlusGamma);

            // Calculate the change in the logarithm of the asset price
            double dS = drift * dt + volatility * dW + Math.log(jumpFactor);

            // Update the asset price
            S *= Math.exp(dS);
            prices[i] = S;
        }
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
        while (p > L) {
            p *= random.nextDouble();
            k++;
        }
        return k - 1;
    }
}
