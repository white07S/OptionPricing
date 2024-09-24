package com.optionpricing.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class OptionTest {

    @Test
    public void testZeroMaturity() {
        assertThrows(IllegalArgumentException.class, () -> {
            new EuropeanOption(100.0, 0.0, OptionType.CALL);
        });
    }

    @Test
    public void testZeroStrikePrice() {
        assertThrows(IllegalArgumentException.class, () -> {
            new EuropeanOption(0.0, 1.0, OptionType.PUT);
        });
    }
}
