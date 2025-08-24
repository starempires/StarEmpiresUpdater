package com.starempires.orders;

import com.google.common.collect.Sets;
import com.starempires.objects.Empire;
import com.starempires.objects.EmpireType;
import com.starempires.objects.Portal;
import com.starempires.util.BaseTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TransmitOrderTest extends BaseTest {

    private Portal portal;
    private Empire other;

    @BeforeEach
    public void before() {
        portal = createPortal("portal", ZERO_COORDINATE, false);
        other = createEmpire("other");
        empire.addKnownPortal(portal);
        empire.addNavData(portal);
        empire.addKnownEmpire(other);
    }

    @Test
    void testParseInvalidOrder() {
        final TransmitOrder order = TransmitOrder.parse(turnData, empire, "foo");
        assertFalse(order.isReady());
        assertTrue(order.getResults().stream().anyMatch(s -> s.contains("Invalid TRANSMIT order")));
    }

    @Test
    void testParseUnknownPortal() {
        empire.removeKnownPortal(portal);
        final TransmitOrder order = TransmitOrder.parse(turnData, empire, "portal to other");
        assertFalse(order.isReady());
        assertTrue(order.getResults().stream().anyMatch(s -> s.contains("Unknown portal")));
    }

    @Test
    void testParseNoNavData() {
        empire.removeNavData(portal);
        final TransmitOrder order = TransmitOrder.parse(turnData, empire, "portal to other");
        assertFalse(order.isReady());
        assertTrue(order.getResults().stream().anyMatch(s -> s.contains("navigation data")));
    }

    @Test
    void testParseUnknownRecipient() {
        empire.removeKnownEmpire(other);
        final TransmitOrder order = TransmitOrder.parse(turnData, empire, "portal to other");
        assertFalse(order.isReady());
        assertTrue(order.getResults().stream().anyMatch(s -> s.contains("message contact")));
    }

    @Test
    void testParseSelf() {
        final TransmitOrder order = TransmitOrder.parse(turnData, empire, "portal to test");
        assertFalse(order.isReady());
        assertTrue(order.getResults().stream().anyMatch(s -> s.contains("yourself")));
    }

    @Test
    void testParseGM() {
        final Empire gm = Empire.builder().name("GM").empireType(EmpireType.GM).build();
        turnData.addEmpires(Sets.newHashSet(gm));
        empire.addKnownEmpire(gm);
        final TransmitOrder order = TransmitOrder.parse(turnData, empire, "portal to GM");
        assertFalse(order.isReady());
        assertTrue(order.getResults().stream().anyMatch(s -> s.contains("declines")));
    }

    @Test
    void testParseSuccess() {
        final TransmitOrder order = TransmitOrder.parse(turnData, empire, "portal to other");
        assertTrue(order.isReady());
        assertTrue(other.isKnownPortal(portal));
        assertTrue(other.hasNavData(portal));
    }
}