// File: ExerciseType.java
package com.optionpricing.model;

/**
 * Enum representing the type of exercise strategy for an option.
 */
public enum ExerciseType {
    /**
     * European option: can be exercised only at maturity.
     */
    EUROPEAN,

    /**
     * American option: can be exercised at any time up to maturity.
     */
    AMERICAN,

    /**
     * Bermudan option: can be exercised on specific dates up to maturity.
     */
    BERMUDAN
}
