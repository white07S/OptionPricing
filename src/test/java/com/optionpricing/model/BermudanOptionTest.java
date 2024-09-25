package com.optionpricing.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class BermudanOptionTest {

    @Test
    public void testBermudanOptionCreation() {
        List<Double> exerciseDates = Arrays.asList(0.5, 0.75);
        BermudanOption option = new BermudanOption(100.0, 1.0, OptionType.CALL, exerciseDates);
        assertEquals(100.0, option.getStrikePrice());
        assertEquals(1.0, option.getMaturity());
        assertEquals(OptionType.CALL, option.getOptionType());
        assertEquals(ExerciseType.BERMUDAN, option.getExerciseType());
        assertEquals(exerciseDates, option.getExerciseDates());
    }

    @Test
    public void testNullExerciseDates() {
        assertThrows(NullPointerException.class, () -> {
            new BermudanOption(100.0, 1.0, OptionType.CALL, null);
        });
    }

    @Test
    public void testEmptyExerciseDates() {
        assertThrows(IllegalArgumentException.class, () -> {
            new BermudanOption(100.0, 1.0, OptionType.CALL, Collections.emptyList());
        });
    }

    @Test
    public void testInvalidExerciseDateNegative() {
        List<Double> exerciseDates = Arrays.asList(-0.5);
        assertThrows(IllegalArgumentException.class, () -> {
            new BermudanOption(100.0, 1.0, OptionType.CALL, exerciseDates);
        });
    }

    @Test
    public void testInvalidExerciseDateZero() {
        List<Double> exerciseDates = Arrays.asList(0.0);
        assertThrows(IllegalArgumentException.class, () -> {
            new BermudanOption(100.0, 1.0, OptionType.CALL, exerciseDates);
        });
    }

    @Test
    public void testInvalidExerciseDateEqualMaturity() {
        List<Double> exerciseDates = Arrays.asList(1.0);
        assertThrows(IllegalArgumentException.class, () -> {
            new BermudanOption(100.0, 1.0, OptionType.CALL, exerciseDates);
        });
    }

    @Test
    public void testInvalidExerciseDateGreaterThanMaturity() {
        List<Double> exerciseDates = Arrays.asList(1.5);
        assertThrows(IllegalArgumentException.class, () -> {
            new BermudanOption(100.0, 1.0, OptionType.CALL, exerciseDates);
        });
    }

    @Test
    public void testUnmodifiableExerciseDates() {
        List<Double> exerciseDates = Arrays.asList(0.5, 0.75);
        BermudanOption option = new BermudanOption(100.0, 1.0, OptionType.CALL, exerciseDates);
        List<Double> returnedDates = option.getExerciseDates();
        assertThrows(UnsupportedOperationException.class, () -> {
            returnedDates.add(0.9);
        });
    }

    @Test
    public void testNullOptionType() {
        List<Double> exerciseDates = Arrays.asList(0.5, 0.75);
        assertThrows(NullPointerException.class, () -> {
            new BermudanOption(100.0, 1.0, null, exerciseDates);
        });
    }

    @Test
    public void testInvalidStrikePriceZero() {
        List<Double> exerciseDates = Arrays.asList(0.5);
        assertThrows(IllegalArgumentException.class, () -> {
            new BermudanOption(0.0, 1.0, OptionType.CALL, exerciseDates);
        });
    }

    @Test
    public void testInvalidMaturityZero() {
        List<Double> exerciseDates = Arrays.asList(0.5);
        assertThrows(IllegalArgumentException.class, () -> {
            new BermudanOption(100.0, 0.0, OptionType.CALL, exerciseDates);
        });
    }
}
