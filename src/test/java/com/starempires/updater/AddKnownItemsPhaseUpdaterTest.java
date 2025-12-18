package com.starempires.updater;

import com.starempires.orders.AddKnownOrder;
import com.starempires.orders.OrderType;
import com.starempires.util.BaseTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class AddKnownItemsPhaseUpdaterTest extends BaseTest {

    private AddKnownItemsPhaseUpdater updater;

    @BeforeEach
    void setUp() {
        updater = new AddKnownItemsPhaseUpdater(turnData);
    }

    @Test
    void update() {
        final AddKnownOrder order = AddKnownOrder.builder()
                .empire(gm)
                .orderType(OrderType.ADDKNOWN)
                .parameters("world world to empire2")
                .worlds(List.of(world))
                .portals(List.of(portal))
                .navData(List.of(portal))
                .storms(List.of(storm))
                .shipClasses(List.of(frigateClass))
                .contacts(List.of(empire1))
                .recipients(List.of(empire2))
                .build();
        turnData.addOrder(order);
        updater.update();
        assertTrue(empire2.isKnownWorld(world));
        assertTrue(empire2.isKnownPortal(portal));
        assertTrue(empire2.hasNavData(portal));
        assertTrue(empire2.isKnownStorm(storm));
        assertTrue(empire2.isKnownShipClass(frigateClass));
        assertTrue(empire2.isKnownEmpire(empire1));
    }
}