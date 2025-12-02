package com.starempires.updater;

import com.starempires.objects.Portal;
import com.starempires.orders.OrderType;
import com.starempires.orders.RemoveConnectionOrder;
import com.starempires.util.BaseTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;

class RemoveConnectionsPhaseUpdaterTest extends BaseTest {

    private RemoveConnectionsPhaseUpdater updater;

    @BeforeEach
    void setUp() {
        updater = new RemoveConnectionsPhaseUpdater(turnData);
    }

    @Test
    void testUpdate() {
        final Portal entry = createPortal("entry", ZERO_COORDINATE, false);
        final Portal exit = createPortal("exit", ONE_COORDINATE, false);
        entry.addConnection(exit);
        final RemoveConnectionOrder order = RemoveConnectionOrder.builder()
                .empire(gm)
                .orderType(OrderType.REMOVECONNECTION)
                .parameters("entry exit")
                .entry(entry)
                .exit(exit)
                .build();
        turnData.addOrder(order);
        updater.update();
        assertFalse(entry.getConnections().contains(exit));
    }
}