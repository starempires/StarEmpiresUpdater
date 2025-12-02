package com.starempires.updater;

import com.starempires.objects.Prohibition;
import com.starempires.objects.World;
import com.starempires.orders.OrderType;
import com.starempires.orders.PoolOrder;
import com.starempires.util.BaseTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PoolResourceUnitsPhaseUpdaterTest extends BaseTest {

    private PoolResourceUnitsPhaseUpdater updater;
    private World poolWorld;
    private World other1;
    private World other2;

    @BeforeEach
    void setUp() {
        world.setOwner(null);
        updater = new PoolResourceUnitsPhaseUpdater(turnData);
        poolWorld = createWorld("poolworld", ZERO_COORDINATE, 5);
        poolWorld.setOwner(empire1);
        poolWorld.setStockpile(0);
        other1 = createWorld("other1", ZERO_COORDINATE, 5);
        other2 = createWorld("other2", ZERO_COORDINATE, 5);
        other1.setOwner(empire1);
        other1.setStockpile(2);
        other2.setOwner(empire1);
        other2.setStockpile(4);
    }

    @Test
    void testPool() {
        final PoolOrder order = PoolOrder.builder()
                .empire(empire1)
                .orderType(OrderType.POOL)
                .parameters("poolworld")
                .world(poolWorld)
                .build();
        turnData.addOrder(order);
        updater.update();
        assertEquals(6, poolWorld.getStockpile());
        assertEquals(0, other1.getStockpile());
        assertEquals(0, other2.getStockpile());
    }

    @Test
    void testPoolToUnownedWorld() {
        poolWorld.setOwner(empire2);
        final PoolOrder order = PoolOrder.builder()
                .empire(empire1)
                .orderType(OrderType.POOL)
                .parameters("poolworld")
                .world(poolWorld)
                .build();
        turnData.addOrder(order);
        updater.update();
        assertEquals(0, poolWorld.getStockpile());
        assertEquals(2, other1.getStockpile());
        assertEquals(4, other2.getStockpile());
    }

    @Test
    void testPoolExceptWorld() {
        final PoolOrder order = PoolOrder.builder()
                .empire(empire1)
                .orderType(OrderType.POOL)
                .parameters("poolworld except other2")
                .world(poolWorld)
                .exceptedWorlds(List.of(other2))
                .build();
        turnData.addOrder(order);
        updater.update();
        assertEquals(2, poolWorld.getStockpile());
        assertEquals(0, other1.getStockpile());
        assertEquals(4, other2.getStockpile());
    }

    @Test
    void testPoolUnownedExceptWorld() {
        final PoolOrder order = PoolOrder.builder()
                .empire(empire1)
                .orderType(OrderType.POOL)
                .parameters("poolworld except other2")
                .world(poolWorld)
                .exceptedWorlds(List.of(other2))
                .build();
        other2.setOwner(empire2);
        turnData.addOrder(order);
        updater.update();
        assertEquals(2, poolWorld.getStockpile());
        assertEquals(0, other1.getStockpile());
        assertEquals(4, other2.getStockpile());
    }

    @Test
    void testPoolBlockadedWorld() {
        final PoolOrder order = PoolOrder.builder()
                .empire(empire1)
                .orderType(OrderType.POOL)
                .parameters("poolworld")
                .world(poolWorld)
                .build();
        other2.setProhibition(Prohibition.BLOCKADED);
        turnData.addOrder(order);
        updater.update();
        assertEquals(2, poolWorld.getStockpile());
        assertEquals(0, other1.getStockpile());
        assertEquals(4, other2.getStockpile());
    }

    @Test
    void testPoolNoStockpiles() {
        final PoolOrder order = PoolOrder.builder()
                .empire(empire1)
                .orderType(OrderType.POOL)
                .parameters("poolworld")
                .world(poolWorld)
                .build();
        other1.setStockpile(0);
        other2.setStockpile(0);
        turnData.addOrder(order);
        updater.update();
        assertEquals(0, poolWorld.getStockpile());
        assertEquals(0, other1.getStockpile());
        assertEquals(0, other2.getStockpile());
    }
}