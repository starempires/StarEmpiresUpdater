package com.starempires.updater;

import com.starempires.orders.RelocateObjectOrder;
import com.starempires.orders.OrderType;
import com.starempires.util.BaseTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RelocateObjectsPhaseUpdaterTest extends BaseTest {

    private RelocateObjectsPhaseUpdater updater;

    @BeforeEach
    void setUp() {
        updater = new RelocateObjectsPhaseUpdater(turnData);
    }

    @Test
    void update() {
        final RelocateObjectOrder order = RelocateObjectOrder.builder()
                .empire(gm)
                .orderType(OrderType.RELOCATEOBJECT)
                .parameters("world world " + ONE_COORDINATE)
                .world(world)
                .coordinate(ONE_COORDINATE)
                .build();
        turnData.addOrder(order);
        updater.update();
        assertEquals(ONE_COORDINATE, world.getCoordinate());
    }
}