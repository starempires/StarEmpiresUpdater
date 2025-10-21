package com.starempires.orders;

import com.starempires.util.BaseTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MoveMapObjectOrderTest extends BaseTest {

    @Test
    void parseInvalidFormat() {
        final MoveMapObjectOrder order = MoveMapObjectOrder.parse(turnData, gm, "foo");
        assertFalse(order.isReady());
        assertTrue(order.getResults().stream().anyMatch(s -> s.contains("Invalid")));
    }

    @Test
    void parseNotGM() {
        final MoveMapObjectOrder order = MoveMapObjectOrder.parse(turnData, empire1, "foo");
        assertFalse(order.isReady());
        assertTrue(order.getResults().stream().anyMatch(s -> s.contains("available only to GM")));
    }

    @Test
    void parseMoveWorld() {
        final String params = "world world " + ONE_COORDINATE;
        final MoveMapObjectOrder order = MoveMapObjectOrder.parse(turnData, gm, params);
        assertTrue(order.isReady());
        assertEquals(world, order.getWorld());
        assertEquals(ONE_COORDINATE, order.getCoordinate());
    }

    @Test
    void parseMovePortal() {
        final String params = "portal portal " + ONE_COORDINATE;
        final MoveMapObjectOrder order = MoveMapObjectOrder.parse(turnData, gm, params);
        assertTrue(order.isReady());
        assertEquals(portal, order.getPortal());
        assertEquals(ONE_COORDINATE, order.getCoordinate());
    }

    @Test
    void parseMoveStorm() {
        final String params = "storm storm " + ONE_COORDINATE;
        final MoveMapObjectOrder order = MoveMapObjectOrder.parse(turnData, gm, params);
        assertTrue(order.isReady());
        assertEquals(storm, order.getStorm());
        assertEquals(ONE_COORDINATE, order.getCoordinate());
    }

    @Test
    void parseUnknownWorld() {
        final String params = "world unknown " + ONE_COORDINATE;
        final MoveMapObjectOrder order = MoveMapObjectOrder.parse(turnData, gm, params);
        assertFalse(order.isReady());
        assertTrue(order.getResults().stream().anyMatch(s -> s.contains("Unknown world")));
    }
}