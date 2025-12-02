package com.starempires.orders;

import com.starempires.util.BaseTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ModifyWorldOrderTest extends BaseTest {

    @Test
    void parseInvalidParameters() {
        final ModifyWorldOrder order = ModifyWorldOrder.parse(turnData, gm, "foo");
        assertFalse(order.isReady());
        assertTrue(order.getResults().stream().anyMatch(s -> s.contains("Invalid MODIFYWORLD order")));
    }
    @Test
    void parseNotGM() {
        final ModifyWorldOrder order = ModifyWorldOrder.parse(turnData, empire1, "foo");
        assertFalse(order.isReady());
        assertTrue(order.getResults().stream().anyMatch(s -> s.contains("available only to GM")));
    }

    @Test
    void parseUnknownOwner() {
        final ModifyWorldOrder order = ModifyWorldOrder.parse(turnData, gm, "world 3 3 foo");
        assertFalse(order.isReady());
        assertTrue(order.getResults().stream().anyMatch(s -> s.contains("Unknown owner")));
    }

    @Test
    void parseUnknownWorld() {
        final ModifyWorldOrder order = ModifyWorldOrder.parse(turnData, gm, "foo 3 3");
        assertFalse(order.isReady());
        assertTrue(order.getResults().stream().anyMatch(s -> s.contains("Unknown world")));
    }

    @Test
    void parseSuccessNoOwner() {
        final ModifyWorldOrder order = ModifyWorldOrder.parse(turnData, gm, "world 3 3");
        assertTrue(order.isReady());
        assertEquals(world, order.getWorld());
        assertEquals(3, order.getProduction());
        assertEquals(3, order.getStockpile());
        assertNull(order.getOwner());
        assertTrue(order.isGmOnly());
    }

    @Test
    void parseSuccessOwner() {
        final ModifyWorldOrder order = ModifyWorldOrder.parse(turnData, gm, "world 3 3 empire2");
        assertTrue(order.isReady());
        assertEquals(world, order.getWorld());
        assertEquals(3, order.getProduction());
        assertEquals(3, order.getStockpile());
        assertEquals(empire2, order.getOwner());
        assertTrue(order.isGmOnly());
    }
}