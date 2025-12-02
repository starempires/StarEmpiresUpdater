package com.starempires.orders;

import com.starempires.objects.Portal;
import com.starempires.util.BaseTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RemoveConnectionOrderTest extends BaseTest {

    @Test
    void parseInvalidFormat() {
        final RemoveConnectionOrder order = RemoveConnectionOrder.parse(turnData, gm, "foo");
        assertFalse(order.isReady());
        assertTrue(order.getResults().stream().anyMatch(s -> s.contains("Invalid")));
    }

    @Test
    void parseNotGM() {
        final RemoveConnectionOrder order = RemoveConnectionOrder.parse(turnData, empire1, "foo");
        assertFalse(order.isReady());
        assertTrue(order.getResults().stream().anyMatch(s -> s.contains("available only to GM")));
    }

    @Test
    void testParse() {
        final Portal portal1 = createPortal("portal1", ZERO_COORDINATE, false);
        final Portal portal2 = createPortal("portal2", ONE_COORDINATE, false);
        final RemoveConnectionOrder order = RemoveConnectionOrder.parse(turnData, gm, portal1 + " " + portal2);
        assertTrue(order.isReady());
        assertEquals(portal1, order.getEntry());
        assertEquals(portal2, order.getExit());
    }

    @Test
    void testParseUnknownEntry() {
        final RemoveConnectionOrder order = RemoveConnectionOrder.parse(turnData, gm, "unknown " + portal);
        assertFalse(order.isReady());
        assertTrue(order.getResults().stream().anyMatch(s -> s.contains("Unknown entry")));
    }

    @Test
    void testParseUnknownExit() {
        final RemoveConnectionOrder order = RemoveConnectionOrder.parse(turnData, gm, portal + " unknown");
        assertFalse(order.isReady());
        assertTrue(order.getResults().stream().anyMatch(s -> s.contains("Unknown exit")));
    }
}