package com.starempires.updater;

import com.starempires.objects.World;
import com.starempires.orders.OrderType;
import com.starempires.orders.PoolOrder;
import com.starempires.util.BaseTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PoolResourceUnitsPhaseUpdaterTest extends BaseTest {

    private World world;
    private PoolResourceUnitsPhaseUpdater updater;

    @BeforeEach
    void setUp() {
        world = createWorld("world", ZERO_COORDINATE, 5);
        world.setOwner(empire);
        updater = new PoolResourceUnitsPhaseUpdater(turnData);
    }

    @Test
    void update() {
        final World other1 = createWorld("other1", ZERO_COORDINATE, 5);
        final World other2 = createWorld("other2", ZERO_COORDINATE, 5);
        other1.setOwner(empire);
        other1.setStockpile(2);
        other2.setOwner(empire);
        other2.setStockpile(4);
        final PoolOrder order = PoolOrder.builder()
                .empire(empire)
                .orderType(OrderType.POOL)
                .parameters("world")
                .world(world)
                .build();
        turnData.addOrder(order);
        updater.update();
        assertEquals(6,  world.getStockpile());
        assertEquals(0,  other1.getStockpile());
        assertEquals(0,  other2.getStockpile());
    }
}