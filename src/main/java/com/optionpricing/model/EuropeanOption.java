// File: EuropeanOption.java
package com.optionpricing.model;

/**
 * Represents a European-style option, which can only be exercised at maturity.
 * Extends the abstract {@link Option} class.
 */
public final class EuropeanOption extends Option {

    /**
     * Constructs a EuropeanOption with the specified parameters.
     *
     * @param strikePrice the strike price of the option
     * @param maturity    the maturity of the option in years
     * @param optionType  the type of the option (CALL or PUT)
     * @throws IllegalArgumentException if any of the parameters are invalid
     */
    public EuropeanOption(double strikePrice, double maturity, OptionType optionType) {
        super(strikePrice, maturity, optionType, ExerciseType.EUROPEAN);
    }
}
