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
    public void testInvalidStrikePrice() {
        assertThrows(IllegalArgumentException.class, () -> {
            new AmericanOption(-100.0, 1.0, OptionType.PUT);
        });
    }

    @Test
    public void testInvalidMaturity() {
        assertThrows(IllegalArgumentException.class, () -> {
            new AmericanOption(100.0, -1.0, OptionType.PUT);
        });
    }
}
