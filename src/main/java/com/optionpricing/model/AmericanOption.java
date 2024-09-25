package com.optionpricing.model;

/**
 * Represents an American-style option, which can be exercised at any time up to maturity.
 * Extends the abstract {@link Option} class.
 */
public final class AmericanOption extends Option {

    /**
     * Constructs an AmericanOption with the specified parameters.
     *
     * @param strikePrice the strike price of the option
     * @param maturity    the maturity of the option in years
     * @param optionType  the type of the option (CALL or PUT)
     * @throws IllegalArgumentException if any of the parameters are invalid
     */
    public AmericanOption(double strikePrice, double maturity, OptionType optionType) {
        super(strikePrice, maturity, optionType, ExerciseType.AMERICAN);
    }
}
