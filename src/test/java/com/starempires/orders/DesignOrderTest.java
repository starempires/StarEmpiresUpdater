package com.starempires.orders;

import com.starempires.objects.HullType;
import com.starempires.objects.Prohibition;
import com.starempires.util.BaseTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DesignOrderTest extends BaseTest {

    @BeforeEach
    void before() throws Exception {
        empire1.addKnownShipClass(probeClass);
    }

    @Test
    void parseDesignMissile() {
        final DesignOrder order = DesignOrder.parse(turnData, empire1, "world torpedo missile 1 10");
        assertEquals(HullType.MISSILE, order.getHullType());
        assertEquals(1, order.getDp());
        assertEquals(1, order.getGuns());
        assertEquals(0, order.getEngines());
        assertEquals(0, order.getScan());
        assertEquals(0, order.getRacks());
        assertEquals(10, order.getTonnage());
        assertEquals(1, order.getCost());
        assertEquals(0, order.getDesignFee());
        assertEquals(0, order.getAr());
    }

    @Test
    void parseDesignShip() {
        final DesignOrder order = DesignOrder.parse(turnData, empire1, "world lander scout 1 1 3 3 1");
        assertEquals(HullType.SCOUT, order.getHullType());
        assertEquals(1, order.getGuns());
        assertEquals(3, order.getEngines());
        assertEquals(3, order.getScan());
        assertEquals(1, order.getDp());
        assertEquals(1, order.getRacks());
        assertEquals(5, order.getCost());
        assertEquals(3, order.getDesignFee());
        assertEquals(1, order.getAr());
    }

    @Test
    void parseDesignShipTooFewAttribute() {
        final DesignOrder order = DesignOrder.parse(turnData, empire1, "world lander scout 1 0 3 3 1");
        assertFalse(order.isReady());
    }

    @Test
    void parseDesignShipTooManyAttribute() {
        final DesignOrder order = DesignOrder.parse(turnData, empire1, "world lander scout 1 1 3 3 10");
        assertFalse(order.isReady());
    }

    @Test
    void parseDesignShipNonExistentWorld() {
        final DesignOrder order = DesignOrder.parse(turnData, empire1, "unknown attackcube gunship 12 12 4 1 0");
        assertFalse(order.isReady());
    }

    @Test
    void testUnknownBuildWorld() {
        empire1.removeKnownWorld(world);
        final DesignOrder order = DesignOrder.parse(turnData, empire1, "world lander scout 1 1 3 3 1");
        assertFalse(order.isReady());
    }

    @Test
    void testUnownedBuildWorld() {
        world.setOwner(null);
        final DesignOrder order = DesignOrder.parse(turnData, empire1, "world lander scout 1 1 3 3 1");
        assertTrue(order.getResults().stream().anyMatch(s -> s.contains("do not currently own")));
        assertTrue(order.isReady());
    }

    @Test
    void testInterdictedDesignWorld() {
        world.setProhibition(Prohibition.INTERDICTED);
        final DesignOrder order = DesignOrder.parse(turnData, empire1, "world lander scout 1 1 3 3 1");
        assertTrue(order.getResults().stream().anyMatch(s -> s.contains("interdicted")));
        assertTrue(order.isReady());
    }

    @Test
    void testDuplicateDesignWorld() {
        final DesignOrder order = DesignOrder.parse(turnData, empire1, "world probe scout 1 1 3 3 1");
        assertTrue(order.getResults().stream().anyMatch(s -> s.contains("Duplicate")));
        assertFalse(order.isReady());
    }
}