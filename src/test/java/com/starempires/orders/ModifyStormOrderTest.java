package com.starempires.orders;

import com.starempires.objects.Storm;
import com.starempires.util.BaseTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ModifyStormOrderTest extends BaseTest {

    @Test
    void parseInvalidParameters() {
        final ModifyStormOrder order = ModifyStormOrder.parse(turnData, gm, "foo");
        assertFalse(order.isReady());
        assertTrue(order.getResults().stream().anyMatch(s -> s.contains("Invalid MODIFYSTORM order")));
    }

    @Test
    void parseNotGM() {
        final ModifyStormOrder order = ModifyStormOrder.parse(turnData, empire1, "foo");
        assertFalse(order.isReady());
        assertTrue(order.getResults().stream().anyMatch(s -> s.contains("available only to GM")));
    }

    @Test
    void parseUnknownStorm() {
        final ModifyStormOrder order = ModifyStormOrder.parse(turnData, gm, "foo 3");
        assertFalse(order.isReady());
        assertTrue(order.getResults().stream().anyMatch(s -> s.contains("Unknown storm")));
    }

    @Test
    void parseSuccess() {
        final Storm teststorm = createStorm("teststorm", ZERO_COORDINATE, 1);
        final ModifyStormOrder order = ModifyStormOrder.parse(turnData, gm, "teststorm 3");
        assertTrue(order.isReady());
        assertEquals(teststorm, order.getStorm());
        assertEquals(3, order.getIntensity());
        assertTrue(order.isGmOnly());
    }
}