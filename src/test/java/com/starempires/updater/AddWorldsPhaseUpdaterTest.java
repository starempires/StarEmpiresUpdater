package com.starempires.updater;

import com.starempires.orders.AddWorldOrder;
import com.starempires.orders.OrderType;
import com.starempires.util.BaseTest;
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
        final String params = ONE_COORDINATE + " " + world.getName() + " " + world.getProduction() + " " + 10 + " " + empire1;
        final AddWorldOrder order = AddWorldOrder.builder()
                .empire(gm)
                .orderType(OrderType.ADDWORLD)
                .parameters(params)
                .world(world)
                .build();
        turnData.addOrder(order);
        updater.update();
        assertEquals(world, turnData.getWorld(world.getName()));
    }
}