// File: BermudanOption.java
package com.optionpricing.model;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Represents a Bermudan-style option, which can be exercised on specific dates up to maturity.
 * Extends the abstract {@link Option} class.
 */
public final class BermudanOption extends Option {
    /**
     * List of exercise dates in years.
     */
    private final List<Double> exerciseDates;

    /**
     * Constructs a BermudanOption with the specified parameters.
     *
     * @param strikePrice   the strike price of the option
     * @param maturity      the maturity of the option in years
     * @param optionType    the type of the option (CALL or PUT)
     * @param exerciseDates the list of specific dates (in years) when the option can be exercised
     * @throws NullPointerException     if exerciseDates is null
     * @throws IllegalArgumentException if exerciseDates is empty or contains invalid dates
     */
    public BermudanOption(double strikePrice, double maturity, OptionType optionType, List<Double> exerciseDates) {
        super(strikePrice, maturity, optionType, ExerciseType.BERMUDAN);
        Objects.requireNonNull(exerciseDates, "Exercise dates cannot be null.");
        if (exerciseDates.isEmpty()) {
            throw new IllegalArgumentException("Exercise dates cannot be empty.");
        }
        for (Double date : exerciseDates) {
            if (date <= 0 || date >= maturity) {
                throw new IllegalArgumentException("Exercise dates must be positive and less than maturity.");
            }
        }
        // Create an unmodifiable copy of the exercise dates list to ensure immutability
        this.exerciseDates = Collections.unmodifiableList(exerciseDates);
    }

    /**
     * Returns the list of exercise dates.
     *
     * @return an unmodifiable list of exercise dates in years
     */
    public List<Double> getExerciseDates() {
        return exerciseDates;
    }
}
