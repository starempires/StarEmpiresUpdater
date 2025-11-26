package com.starempires.objects;

import com.starempires.util.BaseTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WorldTest extends BaseTest {

    private World testworld;

    @BeforeEach
    void setUp() {
        testworld = createWorld("testworld", ZERO_COORDINATE, 10);
    }

    @Test
    void isBlockaded() {
        testworld.setProhibition(Prohibition.BLOCKADED);
        assertTrue(testworld.isBlockaded());
    }

    @Test
    void isInterdicted() {
        testworld.setProhibition(Prohibition.INTERDICTED);
        assertTrue(testworld.isBlockaded());
        assertTrue(testworld.isInterdicted());
    }

    @Test
    void adjustStockpile() {
        testworld.setStockpile(10);
        testworld.adjustStockpile(-1);
        assertEquals(9, testworld.getStockpile());
    }

    @Test
    void isOwned() {
        assertFalse(testworld.isOwned());
        testworld.setOwner(empire1);
        assertTrue(testworld.isOwned());
    }
}