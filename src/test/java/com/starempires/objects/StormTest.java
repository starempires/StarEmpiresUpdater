package com.starempires.objects;

import com.starempires.util.BaseTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StormTest extends BaseTest {

    private Storm teststorm;

    @BeforeEach
    void setUp() {
        teststorm = createStorm("teststorm", ZERO_COORDINATE, 3);
    }

    @Test
    void getFluctuation() {
        teststorm.addFluctuations(List.of(1, 2, 3));
        assertEquals(1, teststorm.getFluctuation(0));
        assertEquals(2, teststorm.getFluctuation(1));
        assertEquals(3, teststorm.getFluctuation(2));
        assertEquals(1, teststorm.getFluctuation(3));
    }

    @Test
    void getFluctuationNone() {
        assertEquals(0, teststorm.getFluctuation(0));
        assertEquals(0, teststorm.getFluctuation(1));
        assertEquals(0, teststorm.getFluctuation(2));
    }
}