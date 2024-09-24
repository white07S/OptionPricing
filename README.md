# Option Pricing Application

[![Java](https://img.shields.io/badge/java-17%2B-blue)](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html)
![CI](https://github.com/white07S/OptionPricing/actions/workflows/ci.yml/badge.svg)
![CD](https://github.com/white07S/OptionPricing/actions/workflows/release.yml/badge.svg)

## Overview

This JavaFX application is designed to price **American**, **Bermudan**, and **European** options using Monte Carlo simulations. It allows users to define option characteristics and market parameters, including:

- **Option Features**: Payoff type (Call/Put), Maturity, Strike Price, Exercise Type (European, American, Bermudan), and Exercise Dates for Bermudan options.
- **Market Data**: Interest rate curve, Volatility, Drift, Jump Intensity (\( \lambda \)), and Jump Size (\( \gamma \)).
- **Simulation Settings**: Number of simulations (default is 1 million) and thread pool size for multi-threading (default is 10).

The application efficiently computes the option price by leveraging multi-threading and provides a user-friendly interface to input parameters and display results.

## Mathematical Derivation

### Underlying Asset Model

The underlying asset price \( S_t \) is modeled using a stochastic differential equation (SDE) that incorporates drift, diffusion, and jump components:

\[
\frac{dS_t}{S_t} = \mu dt + \sigma dW_t + \gamma dq_t
\]

Where:

- \( \mu \) is the constant drift term.
- \( \sigma \) is the constant volatility (diffusion term).
- \( dW_t \) is a Wiener process (standard Brownian motion).
- \( \gamma \) is the jump size (constant).
- \( dq_t \) is a Poisson process with intensity \( \lambda \) representing the number of jumps.

### Solving the SDE Using Ito's Lemma

To find the diffusion process, we apply Ito's Lemma to solve the SDE with jumps.

#### Without Jumps

First, consider the SDE without the jump component:

\[
\frac{dS_t}{S_t} = \mu dt + \sigma dW_t
\]

This is a standard geometric Brownian motion, and its solution is:

\[
S_t = S_0 \exp\left( \left( \mu - \frac{\sigma^2}{2} \right) t + \sigma W_t \right)
\]

#### Incorporating Jumps

Including the jump component, the solution becomes:

\[
S_t = S_0 \exp\left( \left( \mu - \frac{\sigma^2}{2} - \lambda \ln(1 + \gamma) \right) t + \sigma W_t + N_t \ln(1 + \gamma) \right)
\]

Where:

- \( N_t \) is a Poisson process counting the number of jumps up to time \( t \).
- \( \ln(1 + \gamma) \) is the logarithm of the relative jump size.

This equation accounts for the continuous diffusion and the discrete jumps in the asset price.

### Option Pricing via Monte Carlo Simulation

The option price is calculated using the discounted expected payoff under the risk-neutral measure:

#### For European Options

- **Call Option**:

  \[
  C = e^{-rT} \mathbb{E}\left[ \max(0, S_T - K) \right]
  \]

- **Put Option**:

  \[
  P = e^{-rT} \mathbb{E}\left[ \max(0, K - S_T) \right]
  \]

#### For American and Bermudan Options

American and Bermudan options allow early exercise before maturity. Pricing these options requires determining the optimal exercise strategy at each possible exercise date. The **Least Squares Monte Carlo (LSM)** method is used:

1. **Simulate Asset Price Paths**: Generate multiple asset price paths using the underlying asset model with jumps.
2. **Backward Induction**:
   - At each possible exercise date, calculate the immediate exercise payoff.
   - Estimate the continuation value using regression techniques.
   - Decide whether to exercise the option or continue holding it based on the comparison of immediate payoff and continuation value.
3. **Discount Cash Flows**: Calculate the present value of the payoffs to estimate the option price.

## Implementation Details

### Project Structure

- **`com.optionpricing.MainApp`**: Contains the main entry point (`MainApp.java`) that initializes the JavaFX application.
- **`com.optionpricing.model`**: Defines the data models, including options (`Option`, `EuropeanOption`, `AmericanOption`, `BermudanOption`), market data (`MarketData`), and the interest rate curve (`InterestRateCurve`).
- **`com.optionpricing.simulation`**: Contains the simulation logic, primarily the `MonteCarloSimulator` class for running simulations and the `PathGenerator` class for generating asset price paths.
- **`com.optionpricing.view`**: Handles the user interface components using JavaFX (`MainView.java`).

### Key Components

#### 1. `MarketData` Class

- Encapsulates market parameters:
  - Interest rate curve (`InterestRateCurve`).
  - Volatility (`\sigma`).
  - Drift (`\mu`).
  - Jump intensity (`\lambda`).
  - Jump size (`\gamma`).
  - Initial asset price (`S_0`).

#### 2. `Option` Classes

- **`Option`**: Abstract base class with common attributes (strike price, maturity, option type, exercise type).
- **`EuropeanOption`**, **`AmericanOption`**, **`BermudanOption`**: Subclasses representing specific option exercise styles.

#### 3. `InterestRateCurve` Class

- Represents the term structure of interest rates.
- Allows for interpolation of rates and calculation of discount factors for any maturity.

#### 4. `PathGenerator` Class

- Generates simulated price paths for the underlying asset.
- Implements the asset model, including drift, diffusion, and jump components.
- Uses the Euler-Maruyama method for discretization:
  \[
  S_{t+\Delta t} = S_t \exp\left( \left( \mu - \frac{\sigma^2}{2} - \lambda \ln(1+\gamma) \right) \Delta t + \sigma \Delta W_t + \ln(1+\gamma) N_{\Delta t} \right)
  \]
  - \( \Delta W_t \) is the Wiener increment.
  - \( N_{\Delta t} \) is the number of jumps in the interval \( \Delta t \), sampled from a Poisson distribution with parameter \( \lambda \Delta t \).

#### 5. `MonteCarloSimulator` Class

- Extends `javafx.concurrent.Task<Double>` to allow progress updates and cancellation.
- Performs Monte Carlo simulations in parallel using a fixed thread pool.
- Supports pricing of European, American, and Bermudan options.
- **European Option Pricing**:
  - Simulate paths and calculate the discounted payoff at maturity.
- **American/Bermudan Option Pricing**:
  - Uses the Least Squares Monte Carlo (LSM) method for optimal exercise strategy.
  - Performs regression to estimate continuation values at each exercise date.

#### 6. `MainView` Class

- Builds the JavaFX user interface.
- Provides input fields for all option and market parameters.
- Displays the calculated option price and simulation progress.
- Handles user interactions and input validation.

### Multi-threading and Performance

- Utilizes `ExecutorService` with a configurable thread pool size to parallelize simulations.
- Atomic counters and synchronized lists are used to manage shared data safely.
- Progress updates are provided to the UI via JavaFX properties.

## User Interface

### Input Parameters

- **Option Features**:
  - **Exercise Type**: European, American, Bermudan.
  - **Option Type**: Call or Put.
  - **Strike Price** (`K`).
  - **Maturity** (`T` in years).
  - **Exercise Dates**: For Bermudan options (comma-separated list).
- **Market Data**:
  - **Interest Rate Curve**: Maturity:Rate pairs (e.g., `0.5:0.02,1:0.025`).
  - **Volatility** (`\sigma`).
  - **Drift** (`\mu`).
  - **Lambda** (`\lambda`): Jump intensity.
  - **Gamma** (`\gamma`): Jump size.
  - **Initial Price** (`S_0`).
- **Simulation Settings**:
  - **Number of Simulations**: Default is 1,000,000.
  - **Thread Pool Size**: Default is 10.

### Usage Steps

1. **Launch the Application**:
   ```bash
   java -jar OptionPricing-0.0.1-SNAPSHOT.jar
   ```
2. **Enter Option Parameters**:
   - Select the exercise type and option type.
   - Input the strike price and maturity.
   - For Bermudan options, provide the exercise dates.
3. **Enter Market Data**:
   - Input the interest rate curve.
   - Enter volatility, drift, lambda, gamma, and the initial asset price.
4. **Configure Simulation Settings**:
   - Set the number of simulations and thread pool size as desired.
5. **Run the Simulation**:
   - Click on "Calculate Option Price."
   - The progress bar will indicate the simulation progress.
   - The calculated option price will be displayed upon completion.

## How to Run

### Prerequisites

- **Java 17 or higher**: [Download Java 17](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html)

### Running the Application

- **Using the JAR File**:

  ```bash
  mvn clean javafx:run
  ```

- Ensure that the `OptionPricing-0.0.1-SNAPSHOT.jar` file is in your current directory.

## Conclusion

The Option Pricing Application provides a robust solution for pricing different types of options by:

- **Implementing a comprehensive asset model** that includes drift, diffusion, and jump components.
- **Utilizing Ito's Lemma** to derive the diffusion process necessary for simulating asset paths.
- **Applying the Least Squares Monte Carlo (LSM) method** for American and Bermudan options to handle early exercise features.
- **Leveraging multi-threading** to efficiently perform computationally intensive simulations.
- **Providing a user-friendly interface** that allows for flexible input of option characteristics and market data.

This application demonstrates the practical application of advanced mathematical concepts and numerical methods in financial engineering, offering a valuable tool for option pricing analysis.

