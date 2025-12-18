package com.starempires.updater;

import com.starempires.orders.OrderType;
import com.starempires.orders.RemoveKnownOrder;
import com.starempires.util.BaseTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;

class RemoveKnownItemsPhaseUpdaterTest extends BaseTest {
    private RemoveKnownItemsPhaseUpdater updater;

    @BeforeEach
    void setUp() {
        updater = new RemoveKnownItemsPhaseUpdater(turnData);
    }

    @Test
    void update() {
        empire2.addKnownEmpire(empire1);
        empire2.addKnownShipClass(frigateClass);
        empire2.addKnownWorld(world);
        empire2.addKnownPortal(portal);
        empire2.addNavData(portal);
        empire2.addKnownStorm(storm);
        final RemoveKnownOrder order = RemoveKnownOrder.builder()
                .empire(gm)
                .orderType(OrderType.REMOVEKNOWN)
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
        assertFalse(empire2.isKnownWorld(world));
        assertFalse(empire2.isKnownPortal(portal));
        assertFalse(empire2.hasNavData(portal));
        assertFalse(empire2.isKnownStorm(storm));
        assertFalse(empire2.isKnownShipClass(frigateClass));
        assertFalse(empire2.isKnownEmpire(empire1));
    }
}