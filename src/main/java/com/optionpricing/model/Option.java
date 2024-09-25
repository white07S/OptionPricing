package com.optionpricing.model;

import java.util.Objects;

/**
 * Abstract base class representing a financial option.
 * Encapsulates common properties such as strike price, maturity, option type, and exercise type.
 */
public abstract class Option {
    /**
     * The strike price of the option.
     */
    private final double strikePrice;

    /**
     * The maturity of the option in years.
     */
    private final double maturity;

    /**
     * The type of the option (CALL or PUT).
     */
    private final OptionType optionType;

    /**
     * The exercise strategy of the option (EUROPEAN, AMERICAN, or BERMUDAN).
     */
    private final ExerciseType exerciseType;

    /**
     * Constructs an Option with the specified parameters.
     *
     * @param strikePrice  the strike price of the option (must be positive)
     * @param maturity     the maturity of the option in years (must be positive)
     * @param optionType   the type of the option (CALL or PUT)
     * @param exerciseType the exercise strategy of the option (EUROPEAN, AMERICAN, BERMUDAN)
     * @throws IllegalArgumentException if strikePrice or maturity are not positive
     * @throws NullPointerException     if optionType or exerciseType are null
     */
    public Option(double strikePrice, double maturity, OptionType optionType, ExerciseType exerciseType) {
        if (strikePrice <= 0) {
            throw new IllegalArgumentException("Strike price must be positive.");
        }
        if (maturity <= 0) {
            throw new IllegalArgumentException("Maturity must be positive.");
        }
        this.strikePrice = strikePrice;
        this.maturity = maturity;
        this.optionType = Objects.requireNonNull(optionType, "OptionType cannot be null.");
        this.exerciseType = Objects.requireNonNull(exerciseType, "ExerciseType cannot be null.");
    }

    /**
     * Returns the strike price of the option.
     *
     * @return the strike price
     */
    public double getStrikePrice() {
        return strikePrice;
    }

    /**
     * Returns the maturity of the option in years.
     *
     * @return the maturity
     */
    public double getMaturity() {
        return maturity;
    }

    /**
     * Returns the type of the option (CALL or PUT).
     *
     * @return the option type
     */
    public OptionType getOptionType() {
        return optionType;
    }

    /**
     * Returns the exercise strategy of the option.
     *
     * @return the exercise type
     */
    public ExerciseType getExerciseType() {
        return exerciseType;
    }
}
