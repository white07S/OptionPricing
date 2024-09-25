package com.optionpricing.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class EuropeanOptionTest {

    @Test
    public void testEuropeanOptionCreation() {
        EuropeanOption option = new EuropeanOption(100.0, 1.0, OptionType.CALL);
        assertEquals(100.0, option.getStrikePrice());
        assertEquals(1.0, option.getMaturity());
        assertEquals(OptionType.CALL, option.getOptionType());
        assertEquals(ExerciseType.EUROPEAN, option.getExerciseType());
    }

    @Test
    public void testEuropeanOptionPut() {
        EuropeanOption option = new EuropeanOption(100.0, 1.0, OptionType.PUT);
        assertEquals(OptionType.PUT, option.getOptionType());
    }

    @Test
    public void testInvalidStrikePriceNegative() {
        assertThrows(IllegalArgumentException.class, () -> {
            new EuropeanOption(-100.0, 1.0, OptionType.PUT);
        });
    }

    @Test
    public void testInvalidStrikePriceZero() {
        assertThrows(IllegalArgumentException.class, () -> {
            new EuropeanOption(0.0, 1.0, OptionType.PUT);
        });
    }

    @Test
    public void testInvalidMaturityNegative() {
        assertThrows(IllegalArgumentException.class, () -> {
            new EuropeanOption(100.0, -1.0, OptionType.PUT);
        });
    }

    @Test
    public void testInvalidMaturityZero() {
        assertThrows(IllegalArgumentException.class, () -> {
            new EuropeanOption(100.0, 0.0, OptionType.PUT);
        });
    }

    @Test
    public void testNullOptionType() {
        assertThrows(NullPointerException.class, () -> {
            new EuropeanOption(100.0, 1.0, null);
        });
    }
}
