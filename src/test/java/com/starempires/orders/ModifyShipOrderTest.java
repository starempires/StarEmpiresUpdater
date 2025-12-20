package com.starempires.orders;

import com.starempires.objects.Ship;
import com.starempires.util.BaseTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ModifyShipOrderTest extends BaseTest {

    @Test
    void parseInvalidParameters() {
        final ModifyShipOrder order = ModifyShipOrder.parse(turnData, gm, "foo");
        assertFalse(order.isReady());
        assertTrue(order.getResults().stream().anyMatch(s -> s.contains("Invalid MODIFYSHIP order")));
    }

    @Test
    void parseNotGM() {
        final ModifyShipOrder order = ModifyShipOrder.parse(turnData, empire1, "foo");
        assertFalse(order.isReady());
        assertTrue(order.getResults().stream().anyMatch(s -> s.contains("available only to GM")));
    }

    @Test
    void parseUnknownOwner() {
        final ModifyShipOrder order = ModifyShipOrder.parse(turnData, gm, "foo ship 3");
        assertFalse(order.isReady());
        assertTrue(order.getResults().stream().anyMatch(s -> s.contains("Unknown owner")));
    }

    @Test
    void parseUnknownShip() {
        final ModifyShipOrder order = ModifyShipOrder.parse(turnData, gm, "empire1 foo 3");
        assertFalse(order.isReady());
        assertTrue(order.getResults().stream().anyMatch(s -> s.contains("Unknown ship")));
    }

    @Test
    void parseSuccess() {
        final Ship frigate = createShip(frigateClass, ZERO_COORDINATE, "frigate", empire1);
        final ModifyShipOrder order = ModifyShipOrder.parse(turnData, gm, "empire1 frigate 3");
        assertTrue(order.isReady());
        assertEquals(empire1, order.getOwner());
        assertEquals(frigate, order.getShip());
        assertEquals(3, order.getDp());
        assertTrue(order.isGmOnly());
    }

    @Test
    void parseDPExceedsMax() {
        final Ship frigate = createShip(frigateClass, ZERO_COORDINATE, "frigate", empire1);
        final ModifyShipOrder order = ModifyShipOrder.parse(turnData, gm, "empire1 frigate 100");
        assertFalse(order.isReady());
        assertTrue(order.getResults().stream().anyMatch(s -> s.contains("exceeds max")));
    }
}