package com.starempires.updater;

import com.starempires.objects.Ship;
import com.starempires.orders.AddPortalOrder;
import com.starempires.orders.OrderType;
import com.starempires.util.BaseTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AddPortalsPhaseUpdaterTest extends BaseTest {

    private AddPortalsPhaseUpdater updater;

    @BeforeEach
    void setUp() {
        updater = new AddPortalsPhaseUpdater(turnData);
    }

    @Test
    void testUpdate() {
        final Ship ship = createShip(fighterClass, ONE_COORDINATE, "f1", empire1);
        final AddPortalOrder order = AddPortalOrder.builder()
                .empire(gm)
                .orderType(OrderType.ADDSHIP)
                .parameters(ONE_COORDINATE + " " + portal.getName())
                .portal(portal)
                .build();
        turnData.addOrder(order);
        updater.update();
        assertEquals(portal, turnData.getPortal(portal.getName()));
    }
}