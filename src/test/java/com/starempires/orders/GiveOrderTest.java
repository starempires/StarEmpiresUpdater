package com.starempires.orders;

import com.google.common.collect.Sets;
import com.starempires.objects.Empire;
import com.starempires.objects.EmpireType;
import com.starempires.objects.ShipClass;
import com.starempires.util.BaseTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GiveOrderTest extends BaseTest {

    private ShipClass shipClass;
    private Empire other;

    @BeforeEach
    public void before() {
        shipClass = ShipClass.builder().name("shipclass").build();
        turnData.addShipClass(shipClass);
        other = createEmpire("other");
        empire.addKnownShipClass(shipClass);;
        empire.addKnownEmpire(other);
    }

    @Test
    void testParseInvalidOrder() {
        final GiveOrder order = GiveOrder.parse(turnData, empire, "foo");
        assertFalse(order.isReady());
        assertTrue(order.getResults().stream().anyMatch(s -> s.contains("Invalid GIVE order")));
    }

    @Test
    void testParseUnknownShipClass() {
        empire.removeKnownShipClass(shipClass);
        final GiveOrder order = GiveOrder.parse(turnData, empire, "shipclass to other");
        assertFalse(order.isReady());
        assertTrue(order.getResults().stream().anyMatch(s -> s.contains("Unknown ")));
    }

    @Test
    void testParseUnknownRecipient() {
        empire.removeKnownEmpire(other);
        final GiveOrder order = GiveOrder.parse(turnData, empire, "shipclass to other");
        assertFalse(order.isReady());
        assertTrue(order.getResults().stream().anyMatch(s -> s.contains("message contact")));
    }

    @Test
    void testParseSelf() {
        final GiveOrder order = GiveOrder.parse(turnData, empire, "shipclass to test");
        assertFalse(order.isReady());
        assertTrue(order.getResults().stream().anyMatch(s -> s.contains("yourself")));
    }

    @Test
    void testParseGM() {
        final Empire gm = Empire.builder().name("GM").empireType(EmpireType.GM).build();
        turnData.addEmpires(Sets.newHashSet(gm));
        empire.addKnownEmpire(gm);
        final GiveOrder order = GiveOrder.parse(turnData, empire, "shipclass to GM");
        assertFalse(order.isReady());
        assertTrue(order.getResults().stream().anyMatch(s -> s.contains("declines")));
    }

    @Test
    void testParseSuccess() {
        final GiveOrder order = GiveOrder.parse(turnData, empire, "shipclass to other");
        assertTrue(order.isReady());
        assertTrue(other.isKnownShipClass(shipClass));
    }
}