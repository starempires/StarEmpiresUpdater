package com.starempires.orders;

import com.starempires.TurnData;
import com.starempires.dao.JsonStarEmpiresDAO;
import com.starempires.objects.Empire;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BuildOrderTest {

    private static TurnData turnData;
    private static final String TEST_EMPIRE = "KRATOS";
    private static Empire empire;

    @BeforeAll
    static void beforeAll() throws Exception {
        JsonStarEmpiresDAO dao = new JsonStarEmpiresDAO("src/test/resources", null);
        turnData = dao.loadTurnData("test", 0);
        empire = turnData.getEmpire(TEST_EMPIRE);
    }

    @Test
    void testBadOrder() {
        final BuildOrder order = BuildOrder.parse(turnData, empire, "KRATOS");
        assertFalse(order.isReady());
    }

    @Test
    void testParsePrefix() {
        final BuildOrder order = BuildOrder.parse(turnData, empire, "KRATOS 2 probe cube*");
        assertEquals("cube", order.getBasename());
        assertEquals("probe", order.getShipClassName());
        assertTrue(order.isReady());
    }

    @Test
    void testParseNames() {
        final BuildOrder order = BuildOrder.parse(turnData, empire, "KRATOS 2 probe p1 p2");
        assertNull(order.getBasename());
        assertEquals(List.of("p1", "p2"), order.getNames());
        assertEquals("probe", order.getShipClassName());
        assertTrue(order.isReady());
    }

    @Test
    void testInvalidBuildCount() {
        final BuildOrder order = BuildOrder.parse(turnData, empire, "KRATOS 0 probe p1 p2");
        assertFalse(order.isReady());
    }

    @Test
    void testUnknownBuildWorld() {
        final BuildOrder order = BuildOrder.parse(turnData, empire, "Unknown 2 probe p1 p2");
        assertFalse(order.isReady());
    }

    @Test
    void testBuildMax() {
        final BuildOrder order = BuildOrder.parse(turnData, empire, "KRATOS max probe p*");
        assertEquals("p", order.getBasename());
        assertEquals("probe", order.getShipClassName());
        assertTrue(order.isBuildMax());
        assertTrue(order.isReady());
    }
}