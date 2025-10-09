package com.starempires.orders;

import com.starempires.objects.Prohibition;
import com.starempires.objects.World;
import com.starempires.util.BaseTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BuildOrderTest extends BaseTest {

    private World world;

    @BeforeEach
    void before() throws Exception {
        world = createWorld("KRATOS", ZERO_COORDINATE, 12);
        world.setOwner(empire1);
        world.setStockpile(12);
        empire1.addKnownWorld(world);
        empire1.addKnownShipClass(probeClass);
        empire1.addKnownShipClass(starbaseClass);
    }

    @Test
    void testBadOrder() {
        final BuildOrder order = BuildOrder.parse(turnData, empire1, "KRATOS");
        assertFalse(order.isReady());
    }

    @Test
    void testParsePrefix() {
        final BuildOrder order = BuildOrder.parse(turnData, empire1, "KRATOS 2 probe cube*");
        assertEquals("cube", order.getBasename());
        assertEquals("probe", order.getShipClassName());
        assertTrue(order.isReady());
    }

    @Test
    void testParseNames() {
        final BuildOrder order = BuildOrder.parse(turnData, empire1, "KRATOS 2 probe p1 p2");
        assertNull(order.getBasename());
        assertEquals(List.of("p1", "p2"), order.getNames());
        assertEquals("probe", order.getShipClassName());
        assertTrue(order.isReady());
    }

    @Test
    void testInvalidBuildCount() {
        final BuildOrder order = BuildOrder.parse(turnData, empire1, "KRATOS 0 probe p1 p2");
        assertFalse(order.isReady());
    }

    @Test
    void testNonExistentBuildWorld() {
        final BuildOrder order = BuildOrder.parse(turnData, empire1, "Unknown 2 probe p1 p2");
        assertFalse(order.isReady());
    }

    @Test
    void testUnknownBuildWorld() {
        empire1.removeKnownWorld(world);
        final BuildOrder order = BuildOrder.parse(turnData, empire1, "KRATOS 2 probe p1 p2");
        assertFalse(order.isReady());
    }

    @Test
    void testUnownedBuildWorld() {
        world.setOwner(null);
        final BuildOrder order = BuildOrder.parse(turnData, empire1, "KRATOS 2 probe p1 p2");
        assertTrue(order.getResults().stream().anyMatch(s -> s.contains("do not currently own")));
        assertTrue(order.isReady());
    }

    @Test
    void testInterdictedBuildWorld() {
        world.setProhibition(Prohibition.INTERDICTED);
        final BuildOrder order = BuildOrder.parse(turnData, empire1, "KRATOS 2 probe p1 p2");
        assertTrue(order.getResults().stream().anyMatch(s -> s.contains("interdicted")));
        assertTrue(order.isReady());
    }

    @Test
    void testBuildMax() {
        final BuildOrder order = BuildOrder.parse(turnData, empire1, "KRATOS max probe p*");
        assertEquals("p", order.getBasename());
        assertEquals("probe", order.getShipClassName());
        assertTrue(order.isBuildMax());
        assertTrue(order.isReady());
    }

    @Test
    void testBuildNonBuildable() {
        final BuildOrder order = BuildOrder.parse(turnData, empire1, "KRATOS 1 starbase s*");
        assertFalse(order.isReady());
    }

    @Test
    void testBuildNoShipClass() {
        final BuildOrder order = BuildOrder.parse(turnData, empire1, "KRATOS 1 unknown f*");
        assertTrue(order.isReady());
    }

    @Test
    void testBuildUnknownShipClass() {
        final BuildOrder order = BuildOrder.parse(turnData, empire1, "KRATOS 1 fighter f*");
        assertTrue(order.isReady());
    }
}