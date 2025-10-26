package com.starempires.updater;

import com.starempires.objects.World;
import com.starempires.orders.AddWorldOrder;
import com.starempires.orders.OrderType;
import com.starempires.util.BaseTest;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AddWorldsPhaseUpdaterTest extends BaseTest {

    private AddWorldsPhaseUpdater updater;

    @BeforeEach
    void setUp() {
        updater = new AddWorldsPhaseUpdater(turnData);
    }

    @Test
    void testUpdate() {
        final String name = "w1";
        final int production = 10;
        final int stockpile = 5;
        final String params = StringUtils.join(ONE_COORDINATE, name, production, stockpile, empire1);
        final AddWorldOrder order = AddWorldOrder.builder()
                .empire(gm)
                .orderType(OrderType.ADDWORLD)
                .parameters(params)
                .name(name)
                .production(production)
                .stockpile(stockpile)
                .coordinate(ONE_COORDINATE)
                .owner(empire1)
                .build();
        turnData.addOrder(order);
        updater.update();
        final World world = turnData.getWorld(name);
        assertEquals(ONE_COORDINATE, world.getCoordinate());
        assertEquals(empire1, world.getOwner());
    }
}