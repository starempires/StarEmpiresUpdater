package com.starempires.updater;

import com.starempires.objects.Portal;
import com.starempires.orders.AddConnectionOrder;
import com.starempires.orders.OrderType;
import com.starempires.util.BaseTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class AddConnectionsPhaseUpdaterTest extends BaseTest {

    private AddConnectionsPhaseUpdater updater;

    @BeforeEach
    void setUp() {
        updater = new AddConnectionsPhaseUpdater(turnData);
    }

    @Test
    void testUpdate() {
        final Portal entry = createPortal("entry", ZERO_COORDINATE, false);
        final Portal exit = createPortal("exit", ONE_COORDINATE, false);
        final AddConnectionOrder order = AddConnectionOrder.builder()
                .empire(gm)
                .orderType(OrderType.ADDCONNECTION)
                .parameters("entry exit")
                .entry(entry)
                .exit(exit)
                .build();
        turnData.addOrder(order);
        updater.update();
        assertTrue(entry.getConnections().contains(exit));
    }
}