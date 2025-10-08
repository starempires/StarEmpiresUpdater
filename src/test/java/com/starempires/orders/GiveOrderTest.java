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

    @BeforeEach
    public void before() {
        shipClass = ShipClass.builder().name("shipclass").build();
        turnData.addShipClass(shipClass);
        empire1.addKnownShipClass(shipClass);;
        empire1.addKnownEmpire(empire2);
    }

    @Test
    void testParseInvalidOrder() {
        final GiveOrder order = GiveOrder.parse(turnData, empire1, "foo");
        assertFalse(order.isReady());
        assertTrue(order.getResults().stream().anyMatch(s -> s.contains("Invalid GIVE order")));
    }

    @Test
    void testParseUnknownShipClass() {
        empire1.removeKnownShipClass(shipClass);
        final GiveOrder order = GiveOrder.parse(turnData, empire1, "shipclass to empire2");
        assertFalse(order.isReady());
        assertTrue(order.getResults().stream().anyMatch(s -> s.contains("Unknown ")));
    }

    @Test
    void testParseUnknownRecipient() {
        empire1.removeKnownEmpire(empire2);
        final GiveOrder order = GiveOrder.parse(turnData, empire1, "shipclass to empire2");
        assertFalse(order.isReady());
        assertTrue(order.getResults().stream().anyMatch(s -> s.contains("message contact")));
    }

    @Test
    void testParseSelf() {
        final GiveOrder order = GiveOrder.parse(turnData, empire1, "shipclass to empire1");
        assertFalse(order.isReady());
        assertTrue(order.getResults().stream().anyMatch(s -> s.contains("yourself")));
    }

    @Test
    void testParseGM() {
        final Empire gm = Empire.builder().name("GM").empireType(EmpireType.GM).build();
        turnData.addEmpires(Sets.newHashSet(gm));
        empire1.addKnownEmpire(gm);
        final GiveOrder order = GiveOrder.parse(turnData, empire1, "shipclass to GM");
        assertFalse(order.isReady());
        assertTrue(order.getResults().stream().anyMatch(s -> s.contains("declines")));
    }

    @Test
    void testParseSuccess() {
        final GiveOrder order = GiveOrder.parse(turnData, empire1, "shipclass to empire2");
        assertTrue(order.isReady());
        assertTrue(empire2.isKnownShipClass(shipClass));
    }
}