package com.starempires.updater;

import com.starempires.objects.Portal;
import com.starempires.objects.Storm;
import com.starempires.objects.World;
import com.starempires.orders.OrderType;
import com.starempires.orders.RemoveObjectOrder;
import com.starempires.util.BaseTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNull;

class RemoveObjectsPhaseUpdaterTest extends BaseTest {

    private RemoveObjectsPhaseUpdater updater;

    @BeforeEach
    void setUp() {
        updater = new RemoveObjectsPhaseUpdater(turnData);
    }

    @Test
    void update() {
        final World testWorld = createWorld("testworld", ZERO_COORDINATE, 10);
        final Portal testPortal = createPortal("testportal", ZERO_COORDINATE, false);
        final Storm testStorm = createStorm("teststorm", ZERO_COORDINATE, 3);
        final RemoveObjectOrder order = RemoveObjectOrder.builder()
                .empire(gm)
                .orderType(OrderType.REMOVEOBJECT)
                .parameters("world world")
                .worlds(List.of(testWorld))
                .portals(List.of(testPortal))
                .storms(List.of(testStorm))
                .build();
        turnData.addOrder(order);
        updater.update();
        assertNull(turnData.getWorld("testworld"));
        assertNull(turnData.getPortal("testportal"));
        assertNull(turnData.getStorm("teststorm"));
    }
}