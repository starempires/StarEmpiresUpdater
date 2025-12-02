package com.starempires.orders;

import com.starempires.util.BaseTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RemoveObjectOrderTest extends BaseTest {

    @Test
    void parseInvalidFormat() {
        final RemoveObjectOrder order = RemoveObjectOrder.parse(turnData, gm, "foo");
        assertFalse(order.isReady());
        assertTrue(order.getResults().stream().anyMatch(s -> s.contains("Invalid")));
    }

    @Test
    void parseNotGM() {
        final RemoveObjectOrder order = RemoveObjectOrder.parse(turnData, empire1, "foo");
        assertFalse(order.isReady());
        assertTrue(order.getResults().stream().anyMatch(s -> s.contains("available only to GM")));
    }

    @Test
    void parseRemoveWorld() {
        final RemoveObjectOrder order = RemoveObjectOrder.parse(turnData, gm, "world world");
        assertTrue(order.isReady());
        assertEquals(List.of(world), order.getWorlds());
    }

    @Test
    void parseRemovePortal() {
        final RemoveObjectOrder order = RemoveObjectOrder.parse(turnData, gm, "portal portal");
        assertTrue(order.isReady());
        assertEquals(List.of(portal), order.getPortals());
    }

    @Test
    void parseRemoveStorm() {
        final RemoveObjectOrder order = RemoveObjectOrder.parse(turnData, gm, "storm storm");
        assertTrue(order.isReady());
        assertEquals(List.of(storm), order.getStorms());
    }

    @Test
    void parseRemoveUnknownObject() {
        final RemoveObjectOrder order = RemoveObjectOrder.parse(turnData, gm, "world foo");
        assertFalse(order.isReady());
        assertTrue(order.getResults().stream().anyMatch(s -> s.contains("Unknown")));
    }
}