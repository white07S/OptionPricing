package com.optionpricing.model;

/**
 * Enum representing the type of a financial option.
 */
public enum OptionType {
    /**
     * Call option: gives the holder the right to buy the underlying asset at the strike price.
     */
    CALL,

    /**
     * Put option: gives the holder the right to sell the underlying asset at the strike price.
     */
    PUT
}
