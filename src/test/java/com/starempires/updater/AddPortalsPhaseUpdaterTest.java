package com.starempires.updater;

import com.starempires.orders.AddPortalOrder;
import com.starempires.orders.OrderType;
import com.starempires.util.BaseTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

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
                .parameters(ONE_COORDINATE + " " + name + " collapsed")
                .name(name)
                .coordinate(ONE_COORDINATE)
                .collapsed(true)
                .build();
        turnData.addOrder(order);
        updater.update();
        assertTrue(turnData.getPortal(name).isCollapsed());
    }
}