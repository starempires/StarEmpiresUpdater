package com.starempires.orders;

import com.starempires.util.BaseTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AddStormOrderTest extends BaseTest {

    @Test
    void parseInvalidFormat() {
        final AddStormOrder order = AddStormOrder.parse(turnData, gm, "foo");
        assertFalse(order.isReady());
        assertTrue(order.getResults().stream().anyMatch(s -> s.contains("Invalid")));
    }

    @Test
    void parseNotGM() {
        final AddStormOrder order = AddStormOrder.parse(turnData, empire1, "foo");
        assertFalse(order.isReady());
        assertTrue(order.getResults().stream().anyMatch(s -> s.contains("available only to GM")));
    }

    @Test
    void testAddStorm() {
        final String name = "storm1";
        final int rating = 1;
        final AddStormOrder order = AddStormOrder.parse(turnData, gm, ONE_COORDINATE + " " + name + " " + rating);
        assertTrue(order.isReady());
        assertEquals(OrderType.ADDSTORM, order.getOrderType());
        assertEquals(ONE_COORDINATE, order.getCoordinate());
        assertEquals(name, order.getName());
        assertEquals(rating, order.getRating());
    }
}