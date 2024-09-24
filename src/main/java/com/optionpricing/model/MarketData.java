// File: MarketData.java
package com.optionpricing.model;

import java.util.Objects;

/**
 * Encapsulates the market data required for option pricing simulations.
 * Includes interest rate curves, volatility, drift, jump intensity, jump size, and initial asset price.
 */
public final class MarketData {
    /**
     * The interest rate curve used for discounting cash flows.
     */
    private final InterestRateCurve interestRateCurve;

    /**
     * The volatility of the underlying asset (σ).
     */
    private final double volatility;

    /**
     * The drift rate of the underlying asset (μ).
     */
    private final double drift;

    /**
     * The intensity of the Poisson process representing jump events (λ).
     */
    private final double lambda;

    /**
     * The jump size factor (γ).
     */
    private final double gamma;

    /**
     * The initial price of the underlying asset (S₀).
     */
    private final double initialPrice;

    /**
     * Constructs a MarketData instance with the specified parameters.
     *
     * @param interestRateCurve the interest rate curve for discounting
     * @param volatility        the volatility of the underlying asset (must be non-negative)
     * @param drift             the drift rate of the underlying asset
     * @param lambda            the intensity of the Poisson process (must be non-negative)
     * @param gamma             the jump size factor (must be non-negative)
     * @param initialPrice      the initial price of the underlying asset (must be positive)
     * @throws NullPointerException     if interestRateCurve is null
     * @throws IllegalArgumentException if volatility is negative or initialPrice is not positive
     */
    public MarketData(InterestRateCurve interestRateCurve, double volatility, double drift, double lambda, double gamma, double initialPrice) {
        this.interestRateCurve = Objects.requireNonNull(interestRateCurve, "InterestRateCurve cannot be null.");
        if (volatility < 0) {
            throw new IllegalArgumentException("Volatility cannot be negative.");
        }
        if (initialPrice <= 0) {
            throw new IllegalArgumentException("Initial price must be positive.");
        }
        if (lambda < 0) {
            throw new IllegalArgumentException("Lambda (intensity) cannot be negative.");
        }
        if (gamma < 0) {
            throw new IllegalArgumentException("Gamma (jump size) cannot be negative.");
        }
        this.volatility = volatility;
        this.drift = drift;
        this.lambda = lambda;
        this.gamma = gamma;
        this.initialPrice = initialPrice;
    }

    /**
     * Returns the interest rate curve.
     *
     * @return the interest rate curve
     */
    public InterestRateCurve getInterestRateCurve() {
        return interestRateCurve;
    }

    /**
     * Returns the volatility of the underlying asset.
     *
     * @return the volatility (σ)
     */
    public double getVolatility() {
        return volatility;
    }

    /**
     * Returns the drift rate of the underlying asset.
     *
     * @return the drift rate (μ)
     */
    public double getDrift() {
        return drift;
    }

    /**
     * Returns the intensity of the Poisson process representing jump events.
     *
     * @return the intensity (λ)
     */
    public double getLambda() {
        return lambda;
    }

    /**
     * Returns the jump size factor.
     *
     * @return the jump size (γ)
     */
    public double getGamma() {
        return gamma;
    }

    /**
     * Returns the initial price of the underlying asset.
     *
     * @return the initial price (S₀)
     */
    public double getInitialPrice() {
        return initialPrice;
    }
}
