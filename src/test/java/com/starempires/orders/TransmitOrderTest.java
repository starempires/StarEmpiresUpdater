package com.starempires.orders;

import com.google.common.collect.Sets;
import com.starempires.objects.Empire;
import com.starempires.objects.EmpireType;
import com.starempires.util.BaseTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TransmitOrderTest extends BaseTest {

    @BeforeEach
    public void before() {
        empire1.addKnownPortal(portal);
        empire1.addNavData(portal);
        empire1.addKnownEmpire(empire2);
    }

    @Test
    void testParseInvalidOrder() {
        final TransmitOrder order = TransmitOrder.parse(turnData, empire1, "foo");
        assertFalse(order.isReady());
        assertTrue(order.getResults().stream().anyMatch(s -> s.contains("Invalid TRANSMIT order")));
    }

    @Test
    void testParseUnknownPortal() {
        empire1.removeKnownPortal(portal);
        final TransmitOrder order = TransmitOrder.parse(turnData, empire1, "portal to empire2");
        assertFalse(order.isReady());
        assertTrue(order.getResults().stream().anyMatch(s -> s.contains("Unknown portal")));
    }

    @Test
    void testParseNoNavData() {
        empire1.removeNavData(portal);
        final TransmitOrder order = TransmitOrder.parse(turnData, empire1, "portal to empire2");
        assertFalse(order.isReady());
        assertTrue(order.getResults().stream().anyMatch(s -> s.contains("navigation data")));
    }

    @Test
    void testParseUnknownRecipient() {
        empire1.removeKnownEmpire(empire2);
        final TransmitOrder order = TransmitOrder.parse(turnData, empire1, "portal to empire2");
        assertFalse(order.isReady());
        assertTrue(order.getResults().stream().anyMatch(s -> s.contains("message contact")));
    }

    @Test
    void testParseSelf() {
        final TransmitOrder order = TransmitOrder.parse(turnData, empire1, "portal to empire1");
        assertFalse(order.isReady());
        assertTrue(order.getResults().stream().anyMatch(s -> s.contains("yourself")));
    }

    @Test
    void testParseGM() {
        final Empire gm = Empire.builder().name("GM").empireType(EmpireType.GM).build();
        turnData.addEmpires(Sets.newHashSet(gm));
        empire1.addKnownEmpire(gm);
        final TransmitOrder order = TransmitOrder.parse(turnData, empire1, "portal to GM");
        assertFalse(order.isReady());
        assertTrue(order.getResults().stream().anyMatch(s -> s.contains("declines")));
    }

    @Test
    void testParseSuccess() {
        final TransmitOrder order = TransmitOrder.parse(turnData, empire1, "portal to empire2");
        assertTrue(order.isReady());
        assertTrue(empire2.isKnownPortal(portal));
        assertTrue(empire2.hasNavData(portal));
    }
}