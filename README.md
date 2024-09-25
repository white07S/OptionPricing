# Option Pricing Application

[![Java](https://img.shields.io/badge/java-17%2B-blue)](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html)
![CI](https://github.com/white07S/OptionPricing/actions/workflows/ci.yml/badge.svg)
![CD](https://github.com/white07S/OptionPricing/actions/workflows/release.yml/badge.svg)

## Overview

This JavaFX application is designed to price **American**, **Bermudan**, and **European** options using Monte Carlo simulations. It allows users to define option characteristics and market parameters, including:

- **Option Features**: Payoff type (Call/Put), Maturity, Strike Price, Exercise Type (European, American, Bermudan), and Exercise Dates for Bermudan options.
- **Market Data**: Interest rate curve, Volatility, Drift, Jump Intensity ($\lambda$), Mean Jump Size Factor ($\gamma$), Jump Volatility ($\sigma_J$), and Initial Asset Price ($S_0$). Users can choose to simulate under the **Risk-Neutral Measure** or the **Real-World Measure**.
- **Simulation Settings**: Number of simulations (default is 1 million) and thread pool size for multi-threading (default is 10).

The application efficiently computes the option price by leveraging multi-threading and provides a user-friendly interface to input parameters and display results.

## Mathematical Derivation

### Underlying Asset Model

The underlying asset price $S_t$ is modeled using a stochastic differential equation (SDE) that incorporates drift, diffusion, and jump components.

#### Under the Risk-Neutral Measure

When simulating under the **Risk-Neutral Measure**, the SDE for the asset price is:

$$
\frac{dS_t}{S_{t-}} = \left( r - \lambda \kappa \right) dt + \sigma dW_t + dJ_t
$$

#### Under the Real-World Measure

When simulating under the **Real-World Measure**, the SDE becomes:

$$
\frac{dS_t}{S_{t-}} = \left( \mu - \lambda \kappa \right) dt + \sigma dW_t + dJ_t
$$

Where:

- $S_{t-}$ is the asset price just before time $t$.
- $r$ is the instantaneous risk-free interest rate, obtained from the interest rate curve.
- $\mu$ is the real-world drift rate of the underlying asset.
- $\sigma$ is the volatility of the underlying asset.
- $dW_t$ is a Wiener process (standard Brownian motion).
- $\lambda$ is the intensity (frequency) of the Poisson process representing jump events.
- $\kappa = E[e^{Y} - 1]$ is the expected percentage jump size.
- $dJ_t$ represents the jump component, accounting for sudden jumps in the asset price.

#### Jump Component

The jump component $dJ_t$ is defined as:

$$
dJ_t = \left( e^{Y} - 1 \right) dN_t
$$

- $N_t$ is a Poisson process with intensity $\lambda$, representing the cumulative number of jumps up to time $t$.
- $Y$ is the random logarithmic jump size, typically modeled as a normally distributed variable:

$$
Y \sim N\left( \mu_J, \sigma_J^2 \right)
$$

- $\mu_J$ is the mean of the logarithmic jump size.
- $\sigma_J$ is the standard deviation (volatility) of the logarithmic jump size.

#### Expected Jump Size Adjustment ($\kappa$)

The term $\kappa$ adjusts the drift to account for the expected loss or gain from jumps:

$$
\kappa = E\left[ e^{Y} - 1 \right] = e^{\mu_J + \frac{1}{2} \sigma_J^2} - 1
$$

This ensures that the asset price model correctly reflects the expected change due to jumps.

### Solving the SDE Using the Euler-Maruyama Method

To simulate the asset price paths, we discretize the SDE using the Euler-Maruyama method over small time increments $\Delta t$.

#### Asset Price Evolution

For each time step, the asset price evolves as:

$$
S_{t+\Delta t} = S_t \cdot \exp\left( \left( \theta - \frac{\sigma^2}{2} \right) \Delta t + \sigma \Delta W_t + Y \cdot N_{\Delta t} \right)
$$

Where:

- $\theta$ is the adjusted drift term:
  - Under Risk-Neutral Measure: $\theta = r - \lambda \kappa$
  - Under Real-World Measure: $\theta = \mu - \lambda \kappa$
- $\Delta W_t$ is a Wiener increment, normally distributed: $\Delta W_t \sim N(0, \Delta t)$
- $N_{\Delta t}$ is the number of jumps in the time interval $\Delta t$, sampled from a Poisson distribution with parameter $\lambda \Delta t$.
- $Y$ is the logarithmic jump size, sampled from $N(\mu_J, \sigma_J^2)$.

### Option Pricing via Monte Carlo Simulation

The option price is calculated using the discounted expected payoff under the chosen measure.

#### For European Options

- **Call Option**:

$$
C = e^{-rT} \mathbb{E}\left[ \max(0, S_T - K) \right]
$$

- **Put Option**:

$$
P = e^{-rT} \mathbb{E}\left[ \max(0, K - S_T) \right]
$$

#### For American and Bermudan Options

American and Bermudan options allow early exercise before maturity. Pricing these options requires determining the optimal exercise strategy at each possible exercise date. The **Least Squares Monte Carlo (LSM)** method is used:

1. **Simulate Asset Price Paths**: Generate multiple asset price paths using the underlying asset model with jumps and the selected measure (risk-neutral or real-world).

2. **Backward Induction**:
   - At each possible exercise date, calculate the immediate exercise payoff.
   - Estimate the continuation value using regression techniques (e.g., polynomial basis functions).
   - Decide whether to exercise the option or continue holding it based on the comparison of immediate payoff and estimated continuation value.

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
  - **Interest Rate Curve** (`InterestRateCurve`): For discounting future cash flows.
  - **Volatility** ($\sigma$): The standard deviation of asset returns.
  - **Drift** ($\mu$): The expected return rate of the asset (used in real-world measure).
  - **Lambda** ($\lambda$): Jump intensity (frequency of jumps).
  - **Gamma** ($\gamma$): Mean jump size factor.
  - **Jump Volatility** ($\sigma_J$): Volatility of the logarithmic jump sizes.
  - **Initial Price** ($S_0$): The starting price of the underlying asset.
  - **Risk-Neutral Indicator** (`riskNeutral`): Determines whether to simulate under the risk-neutral or real-world measure.

#### 2. `Option` Classes

- **`Option`**: Abstract base class with common attributes (strike price, maturity, option type, exercise type).
- **`EuropeanOption`**, **`AmericanOption`**, **`BermudanOption`**: Subclasses representing specific option exercise styles.

#### 3. `InterestRateCurve` Class

- Represents the term structure of interest rates.
- Allows for interpolation of rates and calculation of discount factors for any maturity.

#### 4. `PathGenerator` Class

- Generates simulated price paths for the underlying asset.
- Implements the asset model, including drift, diffusion, and jump components.
- **Adjusts the drift term based on the selected measure**:
  - **Risk-Neutral Measure**: Drift is set to the risk-free rate minus the expected jump adjustment: $\theta = r - \lambda \kappa$.
  - **Real-World Measure**: Drift is the user-provided drift minus the expected jump adjustment: $\theta = \mu - \lambda \kappa$.
- Uses the Euler-Maruyama method for discretization:

  $$
    S_{t+\Delta t} = S_t \cdot \exp\left( \left( \theta - \frac{\sigma^2}{2} \right) \Delta t + \sigma \Delta W_t + \sum_{i=1}^{N_{\Delta t}} Y_i \right)
  $$

  - $\Delta W_t$ is the Wiener increment.
  - $N_{\Delta t}$ is the number of jumps in the interval $\Delta t$, sampled from a Poisson distribution with parameter $\lambda \Delta t$.
  - $Y_i$ are the individual jump sizes, sampled from a normal distribution.

#### 5. `MonteCarloSimulator` Class

- Extends `javafx.concurrent.Task<Double>` to allow progress updates and cancellation.
- Performs Monte Carlo simulations in parallel using a fixed thread pool.
- Supports pricing of European, American, and Bermudan options.
- **European Option Pricing**:
  - Simulate paths and calculate the discounted payoff at maturity.
- **American/Bermudan Option Pricing**:
  - Uses the Least Squares Monte Carlo (LSM) method for optimal exercise strategy.
  - Performs regression to estimate continuation values at each exercise date.
- **Adjusts calculations based on the selected measure** (risk-neutral or real-world).

#### 6. `MainView` Class

- Builds the JavaFX user interface.
- Provides input fields for all option and market parameters.
- **Risk-Neutral Checkbox**:
  - Allows the user to choose whether to simulate under the risk-neutral measure.
  - When selected, the drift input field is disabled, as the drift is set to the risk-free rate.
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
  - **Strike Price** ($K$).
  - **Maturity** ($T$ in years).
  - **Exercise Dates**: For Bermudan options (comma-separated list).
- **Market Data**:
  - **Interest Rate Curve**: Maturity:Rate pairs (e.g., `0.5:0.02,1:0.025`).
  - **Volatility** ($\sigma$).
  - **Drift** ($\mu$): Enabled only when not simulating under the risk-neutral measure.
  - **Lambda** ($\lambda$): Jump intensity.
  - **Gamma** ($\gamma$): Mean jump size factor.
  - **Jump Volatility** ($\sigma_J$): Volatility of the logarithmic jump sizes.
  - **Initial Price** ($S_0$).
  - **Risk-Neutral Measure Checkbox**: Select to simulate under the risk-neutral measure.
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
   - Enter volatility, lambda, gamma, jump volatility, and the initial asset price.
   - If simulating under the real-world measure, input the drift.
   - Select or deselect the "Simulate under Risk-Neutral Measure" checkbox as desired.

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

- **Using Maven**:

  ```bash
  mvn clean javafx:run
  ```

  Ensure that the `OptionPricing-0.0.1-SNAPSHOT.jar` file is in your current directory.

## Conclusion

The Option Pricing Application provides a robust solution for pricing different types of options by:

- **Implementing a comprehensive asset model** that includes drift, diffusion, and jump components, with the flexibility to simulate under the risk-neutral or real-world measure.
- **Adjusting the drift term** based on the selected measure to accurately model the expected asset price dynamics.
- **Applying the Least Squares Monte Carlo (LSM) method** for American and Bermudan options to handle early exercise features.
- **Leveraging multi-threading** to efficiently perform computationally intensive simulations.
- **Providing a user-friendly interface** that allows for flexible input of option characteristics and market data.

This application demonstrates the practical application of advanced mathematical concepts and numerical methods in financial engineering, offering a valuable tool for option pricing analysis.
