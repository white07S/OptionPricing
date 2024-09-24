// File: MainView.java
package com.optionpricing.view;

import com.optionpricing.model.*;
import com.optionpricing.simulation.MonteCarloSimulator;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.control.Alert.AlertType;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents the main user interface for the Option Pricing Application.
 * Provides input fields for option parameters, market data, and simulation settings,
 * and displays the resulting option price.
 */
public class MainView {
    private static final Logger LOGGER = Logger.getLogger(MainView.class.getName());

    /**
     * The main container pane for the user interface.
     */
    private final VBox mainPane;

    // Input fields
    private ComboBox<String> exerciseTypeCombo;
    private ComboBox<String> optionTypeCombo;
    private TextField exerciseDatesField;
    private Label exerciseDatesLabel;
    private TextField strikePriceField;
    private TextField maturityField;
    private TextField interestRateCurveField;
    private TextField volatilityField;
    private TextField driftField;
    private TextField lambdaField;
    private TextField gammaField;
    private TextField initialPriceField;
    private TextField simulationsField;
    private TextField threadsField;

    /**
     * Constructs the MainView and initializes all UI components.
     */
    public MainView() {
        mainPane = new VBox();
        mainPane.setPadding(new Insets(15));
        mainPane.setSpacing(10);

        // Title label
        Label titleLabel = new Label("Option Pricing Application");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        // Create the grid of input fields
        GridPane inputGrid = createInputGrid();

        // Calculate button and result label
        Button calculateButton = new Button("Calculate Option Price");
        Label resultLabel = new Label("Option Price: ");

        // Progress bar to indicate simulation progress
        ProgressBar progressBar = new ProgressBar(0);
        progressBar.setPrefWidth(300);
        progressBar.setVisible(false);

        // Event handler for the calculate button
        calculateButton.setOnAction(event -> {
            try {
                // Read and validate inputs
                OptionInput optionInput = parseInputs();
                if (optionInput == null) {
                    return; // Parsing failed, error already shown
                }

                // Create MarketData instance based on user inputs
                MarketData marketData = new MarketData(optionInput.interestRateCurve,
                        optionInput.volatility, optionInput.drift, optionInput.lambda,
                        optionInput.gamma, optionInput.initialPrice);

                // Create Option instance based on user inputs
                Option option = createOption(optionInput);

                // Create MonteCarloSimulator task
                MonteCarloSimulator simulationTask = new MonteCarloSimulator(option,
                        marketData, optionInput.numSimulations, optionInput.threadPoolSize);

                // Bind the progress bar to the simulation task's progress
                progressBar.progressProperty().bind(simulationTask.progressProperty());
                progressBar.setVisible(true);

                // Define behavior upon successful simulation
                simulationTask.setOnSucceeded(e -> {
                    double optionPrice = simulationTask.getValue();
                    resultLabel.setText(String.format("Option Price: %.4f", optionPrice));
                    progressBar.setVisible(false);
                });

                // Define behavior if the simulation fails
                simulationTask.setOnFailed(e -> {
                    Throwable exception = simulationTask.getException();
                    showAlert("Simulation Error", "An error occurred during simulation: " + exception.getMessage(), AlertType.ERROR);
                    resultLabel.setText("Option Price: ");
                    progressBar.setVisible(false);
                    LOGGER.log(Level.SEVERE, "Simulation failed", exception);
                });

                // Run the simulation task in a new daemon thread
                Thread simulationThread = new Thread(simulationTask);
                simulationThread.setDaemon(true);
                simulationThread.start();

            } catch (Exception e) {
                // Handle any unexpected exceptions
                showAlert("Error", "An unexpected error occurred: " + e.getMessage(), AlertType.ERROR);
                LOGGER.log(Level.SEVERE, "Unexpected error", e);
            }
        });

        // Add all components to the main pane
        mainPane.getChildren().addAll(titleLabel, inputGrid, calculateButton, progressBar, resultLabel);
    }

    /**
     * Creates and configures the grid layout containing all input fields.
     *
     * @return the configured GridPane with input fields
     */
    private GridPane createInputGrid() {
        GridPane grid = new GridPane();
        grid.setVgap(10);
        grid.setHgap(10);

        // Initialize input fields
        exerciseTypeCombo = new ComboBox<>();
        exerciseTypeCombo.getItems().addAll("European", "American", "Bermudan");
        exerciseTypeCombo.setValue("European");

        optionTypeCombo = new ComboBox<>();
        optionTypeCombo.getItems().addAll("Call", "Put");
        optionTypeCombo.setValue("Call");

        exerciseDatesLabel = new Label("Exercise Dates (years, comma-separated):");
        exerciseDatesField = new TextField("0.5,1");
        exerciseDatesLabel.setVisible(false);
        exerciseDatesField.setVisible(false);

        // Show or hide exercise dates fields based on the selected exercise type
        exerciseTypeCombo.setOnAction(e -> {
            String selected = exerciseTypeCombo.getValue();
            boolean isBermudan = "Bermudan".equalsIgnoreCase(selected);
            exerciseDatesLabel.setVisible(isBermudan);
            exerciseDatesField.setVisible(isBermudan);
        });

        strikePriceField = new TextField("100");
        maturityField = new TextField("1");
        interestRateCurveField = new TextField("0.5:0.02,1:0.025,2:0.03,5:0.035,10:0.04");
        volatilityField = new TextField("0.2");
        driftField = new TextField("0.05");
        lambdaField = new TextField("0.1");
        gammaField = new TextField("0.02");
        initialPriceField = new TextField("100");
        simulationsField = new TextField("1000000");
        threadsField = new TextField("10");

        // Add components to grid
        int row = 0;
        grid.add(new Label("Exercise Type:"), 0, row);
        grid.add(exerciseTypeCombo, 1, row++);

        grid.add(new Label("Option Type:"), 0, row);
        grid.add(optionTypeCombo, 1, row++);

        grid.add(exerciseDatesLabel, 0, row);
        grid.add(exerciseDatesField, 1, row++);

        grid.add(new Label("Strike Price:"), 0, row);
        grid.add(strikePriceField, 1, row++);

        grid.add(new Label("Maturity (years):"), 0, row);
        grid.add(maturityField, 1, row++);

        grid.add(new Label("Interest Rate Curve (maturity:rate, ...):"), 0, row);
        grid.add(interestRateCurveField, 1, row++);

        grid.add(new Label("Volatility:"), 0, row);
        grid.add(volatilityField, 1, row++);

        grid.add(new Label("Drift:"), 0, row);
        grid.add(driftField, 1, row++);

        grid.add(new Label("Lambda (λ):"), 0, row);
        grid.add(lambdaField, 1, row++);

        grid.add(new Label("Gamma (γ):"), 0, row);
        grid.add(gammaField, 1, row++);

        grid.add(new Label("Initial Price (S₀):"), 0, row);
        grid.add(initialPriceField, 1, row++);

        grid.add(new Label("Number of Simulations:"), 0, row);
        grid.add(simulationsField, 1, row++);

        grid.add(new Label("Thread Pool Size:"), 0, row);
        grid.add(threadsField, 1, row++);

        return grid;
    }

    /**
     * Parses and validates all user inputs from the UI fields.
     *
     * @return an {@link OptionInput} object containing all parsed inputs, or null if parsing fails
     */
    private OptionInput parseInputs() {
        try {
            // Read and validate inputs
            String exerciseTypeStr = exerciseTypeCombo.getValue();
            String optionTypeStr = optionTypeCombo.getValue();
            String strikePriceStr = strikePriceField.getText();
            String maturityStr = maturityField.getText();
            String interestRateCurveStr = interestRateCurveField.getText();
            String volatilityStr = volatilityField.getText();
            String driftStr = driftField.getText();
            String lambdaStr = lambdaField.getText();
            String gammaStr = gammaField.getText();
            String initialPriceStr = initialPriceField.getText();
            String simulationsStr = simulationsField.getText();
            String threadsStr = threadsField.getText();

            // Parse enums
            ExerciseType exerciseType = ExerciseType.valueOf(exerciseTypeStr.toUpperCase());
            OptionType optionType = OptionType.valueOf(optionTypeStr.toUpperCase());

            // Parse numerical inputs
            double strikePrice = parsePositiveDouble(strikePriceStr, "Strike Price");
            double maturity = parsePositiveDouble(maturityStr, "Maturity");
            double volatility = parseNonNegativeDouble(volatilityStr, "Volatility");
            double drift = parseDouble(driftStr, "Drift");
            double lambda = parseNonNegativeDouble(lambdaStr, "Lambda");
            double gamma = parseNonNegativeDouble(gammaStr, "Gamma");
            double initialPrice = parsePositiveDouble(initialPriceStr, "Initial Price");
            int numSimulations = parsePositiveInt(simulationsStr, "Number of Simulations");
            int threadPoolSize = parsePositiveInt(threadsStr, "Thread Pool Size");

            // Parse interest rate curve
            NavigableMap<Double, Double> ratesMap = parseInterestRateCurve(interestRateCurveStr);

            InterestRateCurve interestRateCurve = new InterestRateCurve(ratesMap);

            // Parse exercise dates for Bermudan options
            List<Double> exerciseDates = new ArrayList<>();
            if (exerciseType == ExerciseType.BERMUDAN) {
                String[] dateTokens = exerciseDatesField.getText().split(",");
                for (String dateStr : dateTokens) {
                    double date = parsePositiveDouble(dateStr.trim(), "Exercise Date");
                    if (date >= maturity) {
                        throw new IllegalArgumentException("Exercise dates must be less than maturity.");
                    }
                    exerciseDates.add(date);
                }
                if (exerciseDates.isEmpty()) {
                    throw new IllegalArgumentException("At least one exercise date must be provided for Bermudan options.");
                }
            }

            // Return a new OptionInput object containing all parsed inputs
            return new OptionInput(exerciseType, optionType, exerciseDates,
                    strikePrice, maturity, interestRateCurve,
                    volatility, drift, lambda, gamma,
                    initialPrice, numSimulations, threadPoolSize);

        } catch (Exception e) {
            // Show an alert to the user if input parsing fails
            showAlert("Input Error", "Invalid input: " + e.getMessage(), AlertType.ERROR);
            LOGGER.log(Level.WARNING, "Invalid input", e);
            return null;
        }
    }

    /**
     * Creates an Option object based on the provided OptionInput.
     *
     * @param input the parsed option inputs
     * @return an instance of a subclass of Option (EuropeanOption, AmericanOption, BermudanOption)
     * @throws IllegalArgumentException if the exercise type is unsupported
     */
    private Option createOption(OptionInput input) {
        switch (input.exerciseType) {
            case EUROPEAN:
                return new EuropeanOption(input.strikePrice, input.maturity, input.optionType);
            case AMERICAN:
                return new AmericanOption(input.strikePrice, input.maturity, input.optionType);
            case BERMUDAN:
                return new BermudanOption(input.strikePrice, input.maturity, input.optionType, input.exerciseDates);
            default:
                throw new IllegalArgumentException("Unsupported exercise type: " + input.exerciseType);
        }
    }

    /**
     * Parses the interest rate curve input string into a NavigableMap.
     *
     * @param input the interest rate curve input string in the format "maturity:rate, ..."
     * @return a NavigableMap with maturities as keys and rates as values
     * @throws IllegalArgumentException if the input format is invalid
     */
    private NavigableMap<Double, Double> parseInterestRateCurve(String input) {
        NavigableMap<Double, Double> ratesMap = new TreeMap<>();
        String[] pairs = input.split(",");
        for (String pair : pairs) {
            String[] tokens = pair.split(":");
            if (tokens.length != 2) {
                throw new IllegalArgumentException("Invalid interest rate curve format.");
            }
            double maturity = parsePositiveDouble(tokens[0].trim(), "Interest Rate Curve Maturity");
            double rate = parseNonNegativeDouble(tokens[1].trim(), "Interest Rate Curve Rate");
            ratesMap.put(maturity, rate);
        }
        return ratesMap;
    }

    /**
     * Parses a string to a positive double.
     *
     * @param value     the string to parse
     * @param fieldName the name of the field being parsed (for error messages)
     * @return the parsed positive double
     * @throws IllegalArgumentException if the string cannot be parsed or is not positive
     */
    private double parsePositiveDouble(String value, String fieldName) {
        try {
            double d = Double.parseDouble(value);
            if (d <= 0) {
                throw new IllegalArgumentException(fieldName + " must be positive.");
            }
            return d;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(fieldName + " must be a valid number.");
        }
    }

    /**
     * Parses a string to a non-negative double.
     *
     * @param value     the string to parse
     * @param fieldName the name of the field being parsed (for error messages)
     * @return the parsed non-negative double
     * @throws IllegalArgumentException if the string cannot be parsed or is negative
     */
    private double parseNonNegativeDouble(String value, String fieldName) {
        try {
            double d = Double.parseDouble(value);
            if (d < 0) {
                throw new IllegalArgumentException(fieldName + " cannot be negative.");
            }
            return d;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(fieldName + " must be a valid number.");
        }
    }

    /**
     * Parses a string to a double.
     *
     * @param value     the string to parse
     * @param fieldName the name of the field being parsed (for error messages)
     * @return the parsed double
     * @throws IllegalArgumentException if the string cannot be parsed
     */
    private double parseDouble(String value, String fieldName) {
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(fieldName + " must be a valid number.");
        }
    }

    /**
     * Parses a string to a positive integer.
     *
     * @param value     the string to parse
     * @param fieldName the name of the field being parsed (for error messages)
     * @return the parsed positive integer
     * @throws IllegalArgumentException if the string cannot be parsed or is not positive
     */
    private int parsePositiveInt(String value, String fieldName) {
        try {
            int i = Integer.parseInt(value);
            if (i <= 0) {
                throw new IllegalArgumentException(fieldName + " must be positive.");
            }
            return i;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(fieldName + " must be a valid integer.");
        }
    }

    /**
     * Displays an alert dialog to the user.
     *
     * @param title   the title of the alert dialog
     * @param message the content message of the alert
     * @param type    the type of alert (e.g., ERROR, INFORMATION)
     */
    private void showAlert(String title, String message, AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Returns the main pane of the view, which contains all UI components.
     *
     * @return the main VBox pane
     */
    public VBox getMainPane() {
        return mainPane;
    }

    /**
     * Inner class to encapsulate all parsed option inputs.
     */
    private static class OptionInput {
        ExerciseType exerciseType;
        OptionType optionType;
        List<Double> exerciseDates;
        double strikePrice;
        double maturity;
        InterestRateCurve interestRateCurve;
        double volatility;
        double drift;
        double lambda;
        double gamma;
        double initialPrice;
        int numSimulations;
        int threadPoolSize;

        /**
         * Constructs an OptionInput with all required parameters.
         *
         * @param exerciseType     the type of exercise strategy
         * @param optionType       the type of the option (CALL or PUT)
         * @param exerciseDates    the list of exercise dates (only for Bermudan options)
         * @param strikePrice      the strike price of the option
         * @param maturity         the maturity of the option in years
         * @param interestRateCurve the interest rate curve for discounting
         * @param volatility       the volatility of the underlying asset
         * @param drift            the drift rate of the underlying asset
         * @param lambda           the intensity of the Poisson process (for jump processes)
         * @param gamma            the jump size factor
         * @param initialPrice     the initial price of the underlying asset
         * @param numSimulations   the number of Monte Carlo simulations to run
         * @param threadPoolSize   the number of threads to use for simulations
         */
        public OptionInput(ExerciseType exerciseType, OptionType optionType, List<Double> exerciseDates,
                          double strikePrice, double maturity, InterestRateCurve interestRateCurve,
                          double volatility, double drift, double lambda, double gamma,
                          double initialPrice, int numSimulations, int threadPoolSize) {
            this.exerciseType = exerciseType;
            this.optionType = optionType;
            this.exerciseDates = exerciseDates;
            this.strikePrice = strikePrice;
            this.maturity = maturity;
            this.interestRateCurve = interestRateCurve;
            this.volatility = volatility;
            this.drift = drift;
            this.lambda = lambda;
            this.gamma = gamma;
            this.initialPrice = initialPrice;
            this.numSimulations = numSimulations;
            this.threadPoolSize = threadPoolSize;
        }
    }
}
