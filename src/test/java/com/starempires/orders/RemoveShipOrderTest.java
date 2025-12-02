package com.starempires.orders;

import com.starempires.objects.Ship;
import com.starempires.util.BaseTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RemoveShipOrderTest extends BaseTest {

    @Test
    void parseInvalidFormat() {
        final RemoveShipOrder order = RemoveShipOrder.parse(turnData, gm, "foo");
        assertFalse(order.isReady());
        assertTrue(order.getResults().stream().anyMatch(s -> s.contains("Invalid")));
    }

    @Test
    void parseNotGM() {
        final RemoveShipOrder order = RemoveShipOrder.parse(turnData, empire1, "foo");
        assertFalse(order.isReady());
        assertTrue(order.getResults().stream().anyMatch(s -> s.contains("available only to GM")));
    }

    @Test
    void parseRemoveUnknownOwner() {
        final RemoveShipOrder order = RemoveShipOrder.parse(turnData, gm, "foo ship");
        assertFalse(order.isReady());
        assertTrue(order.getResults().stream().anyMatch(s -> s.contains("Unknown owner")));
    }

    @Test
    void parseRemoveUnknownShip() {
        final RemoveShipOrder order = RemoveShipOrder.parse(turnData, gm, "empire1 foo");
        assertFalse(order.isReady());
        assertTrue(order.getResults().stream().anyMatch(s -> s.contains("Unknown ship")));
    }

    @Test
    void parseRemoveShip() {
        final Ship probe = createShip(probeClass, ZERO_COORDINATE, "probe", empire1);
        final RemoveShipOrder order = RemoveShipOrder.parse(turnData, gm, "empire1 probe");
        assertTrue(order.isReady());
        assertEquals(empire1, order.getOwner());
        assertEquals(List.of(probe), order.getShips());
        assertTrue(order.isGmOnly());
    }
}