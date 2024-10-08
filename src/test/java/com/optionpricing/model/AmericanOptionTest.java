package com.optionpricing.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class AmericanOptionTest {

    @Test
    public void testAmericanOptionCreation() {
        AmericanOption option = new AmericanOption(100.0, 1.0, OptionType.CALL);
        assertEquals(100.0, option.getStrikePrice());
        assertEquals(1.0, option.getMaturity());
        assertEquals(OptionType.CALL, option.getOptionType());
        assertEquals(ExerciseType.AMERICAN, option.getExerciseType());
    }

    @Test
    public void testAmericanOptionPut() {
        AmericanOption option = new AmericanOption(100.0, 1.0, OptionType.PUT);
        assertEquals(OptionType.PUT, option.getOptionType());
    }

    @Test
    public void testInvalidStrikePriceNegative() {
        assertThrows(IllegalArgumentException.class, () -> {
            new AmericanOption(-100.0, 1.0, OptionType.PUT);
        });
    }

    @Test
    public void testInvalidStrikePriceZero() {
        assertThrows(IllegalArgumentException.class, () -> {
            new AmericanOption(0.0, 1.0, OptionType.PUT);
        });
    }

    @Test
    public void testInvalidMaturityNegative() {
        assertThrows(IllegalArgumentException.class, () -> {
            new AmericanOption(100.0, -1.0, OptionType.PUT);
        });
    }

    @Test
    public void testInvalidMaturityZero() {
        assertThrows(IllegalArgumentException.class, () -> {
            new AmericanOption(100.0, 0.0, OptionType.PUT);
        });
    }

    @Test
    public void testNullOptionType() {
        assertThrows(NullPointerException.class, () -> {
            new AmericanOption(100.0, 1.0, null);
        });
    }
}
