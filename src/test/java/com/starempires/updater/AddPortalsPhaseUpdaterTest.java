package com.starempires.updater;

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
        final String name = "p1";
        final AddPortalOrder order = AddPortalOrder.builder()
                .empire(gm)
                .orderType(OrderType.ADDPORTAL)
                .parameters(ONE_COORDINATE + " " + name)
                .name(name)
                .coordinate(ONE_COORDINATE)
                .build();
        turnData.addOrder(order);
        updater.update();
        assertEquals(portal, turnData.getPortal(portal.getName()));
    }
}