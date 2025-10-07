package com.starempires.objects;

import org.junit.jupiter.api.Test;

import java.util.EnumSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ShipConditionTest {

    @Test
    void fromAbbreviation() {
        EnumSet.allOf(ShipCondition.class).forEach(sc -> assertEquals(sc, ShipCondition.fromAbbreviation(sc.getAbbreviation())));
    }

    @Test
    void fromAbbreviationException() {
        assertThrows(IllegalArgumentException.class, () -> ShipCondition.fromAbbreviation("Z"));
    }
}