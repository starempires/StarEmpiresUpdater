package com.starempires.updater;

import com.starempires.orders.MoveMapObjectOrder;
import com.starempires.orders.OrderType;
import com.starempires.util.BaseTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MoveMapObjectsPhaseUpdaterTest extends BaseTest {

    private MoveMapObjectsPhaseUpdater updater;

    @BeforeEach
    void setUp() {
        updater = new MoveMapObjectsPhaseUpdater(turnData);
    }

    @Test
    void update() {
        final MoveMapObjectOrder order = MoveMapObjectOrder.builder()
                .empire(gm)
                .orderType(OrderType.MOVEMAPOBJECT)
                .parameters("world world " + ONE_COORDINATE)
                .world(world)
                .coordinate(ONE_COORDINATE)
                .build();
        turnData.addOrder(order);
        updater.update();
        assertEquals(ONE_COORDINATE, world.getCoordinate());
    }
}