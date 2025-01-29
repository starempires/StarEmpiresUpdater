package com.starempires.orders;

import com.starempires.TurnData;
import com.starempires.dao.JsonStarEmpiresDAO;
import com.starempires.objects.Empire;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BuildOrderTest {

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
    void parse() {
        final BuildOrder order = BuildOrder.parse(turnData, empire, "KRATOS 2 probe cube*");
        assertEquals("cube", order.getBasename());
    }
}