package com.starempires.orders;

import com.starempires.TurnData;
import com.starempires.dao.JsonStarEmpiresDAO;
import com.starempires.objects.Empire;
import com.starempires.objects.HullType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class DesignOrderTest {

    private static TurnData turnData;
    private static final String TEST_EMPIRE = "KRATOS";
    private static Empire empire;

    @BeforeAll
    static void beforeAll() throws Exception {
        JsonStarEmpiresDAO dao = new JsonStarEmpiresDAO("src/test/resources");
        turnData = dao.loadTurnData("test", 0);
        empire = turnData.getEmpire(TEST_EMPIRE);
    }

    @Test
    void parseDesignMissile() {
        final DesignOrder order = DesignOrder.parse(turnData, empire, "KRATOS torpedo missile 1 10");
        assertEquals(HullType.MISSILE, order.getHullType());
        assertEquals(1, order.getDp());
        assertEquals(1, order.getGuns());
        assertEquals(0, order.getEngines());
        assertEquals(0, order.getScan());
        assertEquals(0, order.getRacks());
        assertEquals(10, order.getTonnage());
    }

    @Test
    void parseDesignShip() {
        final DesignOrder order = DesignOrder.parse(turnData, empire, "KRATOS attackcube gunship 12 12 4 1 0");
        assertEquals(HullType.GUNSHIP, order.getHullType());
        assertEquals(12, order.getGuns());
        assertEquals(12, order.getDp());
        assertEquals(4, order.getEngines());
        assertEquals(1, order.getScan());
        assertEquals(0, order.getRacks());
    }

    @Test
    void parseDesignShipUnknownWorld() {
        final DesignOrder order = DesignOrder.parse(turnData, empire, "unknown attackcube gunship 12 12 4 1 0");
        assertFalse(order.isReady());
    }
}