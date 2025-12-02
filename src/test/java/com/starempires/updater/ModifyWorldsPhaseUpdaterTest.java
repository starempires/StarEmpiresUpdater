package com.starempires.updater;

import com.starempires.objects.World;
import com.starempires.orders.ModifyWorldOrder;
import com.starempires.orders.OrderType;
import com.starempires.util.BaseTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class ModifyWorldsPhaseUpdaterTest extends BaseTest {

    private ModifyWorldsPhaseUpdater updater;
    private World testWorld;

    @BeforeEach
    public void setUp() {
        updater = new ModifyWorldsPhaseUpdater(turnData);
        testWorld = createWorld("testworld", ZERO_COORDINATE, 10);
    }

    @Test
    public void updateProduction() {
        final ModifyWorldOrder order = ModifyWorldOrder.builder()
                .orderType(OrderType.MODIFYWORLD)
                .gmOnly(true)
                .world(testWorld)
                .parameters("world 20 0")
                .production(20)
                .empire(gm)
                .build();
        turnData.addOrder(order);
        updater.update();
        assertEquals(20, testWorld.getProduction());
        assertEquals(0, testWorld.getStockpile());
        assertFalse(testWorld.isOwned());
    }

    @Test
    public void updateStockpile() {
        final ModifyWorldOrder order = ModifyWorldOrder.builder()
                .orderType(OrderType.MODIFYWORLD)
                .gmOnly(true)
                .world(testWorld)
                .parameters("world 10 20")
                .production(10)
                .stockpile(20)
                .empire(gm)
                .build();
        turnData.addOrder(order);
        updater.update();
        assertEquals(10, testWorld.getProduction());
        assertEquals(20, testWorld.getStockpile());
        assertFalse(testWorld.isOwned());
    }

    @Test
    public void updateOwner() {
        final ModifyWorldOrder order = ModifyWorldOrder.builder()
                .orderType(OrderType.MODIFYWORLD)
                .gmOnly(true)
                .world(testWorld)
                .parameters("world 10 0 empire1")
                .production(10)
                .owner(empire1)
                .empire(gm)
                .build();
        turnData.addOrder(order);
        updater.update();
        assertEquals(10, testWorld.getProduction());
        assertEquals(0, testWorld.getStockpile());
        assertEquals(empire1, testWorld.getOwner());
    }

    @Test
    public void updateRemoveOwner() {
        testWorld.setOwner(empire1);
        final ModifyWorldOrder order = ModifyWorldOrder.builder()
                .orderType(OrderType.MODIFYWORLD)
                .gmOnly(true)
                .world(testWorld)
                .parameters("world 10 0")
                .production(10)
                .empire(gm)
                .build();
        turnData.addOrder(order);
        updater.update();
        assertEquals(10, testWorld.getProduction());
        assertEquals(0, testWorld.getStockpile());
        assertFalse(testWorld.isOwned());
    }

}