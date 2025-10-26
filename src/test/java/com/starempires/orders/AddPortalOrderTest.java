package com.starempires.orders;

import com.starempires.util.BaseTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AddPortalOrderTest extends BaseTest {

    @Test
    void parseInvalidFormat() {
        final AddPortalOrder order = AddPortalOrder.parse(turnData, gm, "foo");
        assertFalse(order.isReady());
        assertTrue(order.getResults().stream().anyMatch(s -> s.contains("Invalid")));
    }

    @Test
    void parseNotGM() {
        final AddPortalOrder order = AddPortalOrder.parse(turnData, empire1, "foo");
        assertFalse(order.isReady());
        assertTrue(order.getResults().stream().anyMatch(s -> s.contains("available only to GM")));
    }

    @Test
    void testAddPortal() {
        final String name = "portal1";
        final AddPortalOrder order = AddPortalOrder.parse(turnData, gm, ONE_COORDINATE + " " + name);
        assertTrue(order.isReady());
        assertEquals(OrderType.ADDPORTAL, order.getOrderType());
        assertEquals(ONE_COORDINATE, order.getCoordinate());
        assertEquals(name, order.getName());
    }
}