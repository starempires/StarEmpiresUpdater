package com.starempires.updater;

import com.google.common.collect.Lists;
import com.starempires.objects.Ship;
import com.starempires.objects.ShipCondition;
import com.starempires.orders.OrderType;
import com.starempires.orders.ToggleOrder;
import com.starempires.util.BaseTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ToggleTransponderModesPhaseUpdaterTest extends BaseTest {

    private Ship ship;
    private ToggleTransponderModesPhaseUpdater updater;

    @BeforeEach
    void setUp() {
        ship = createShip(fighterClass, ZERO_COORDINATE, "ship", empire);
        updater = new ToggleTransponderModesPhaseUpdater(turnData);
    }

    @Test
    void updatePublic() {
        ship.setPublicTransponder(false);
        final ToggleOrder order = ToggleOrder.builder()
                .empire(empire)
                .orderType(OrderType.TOGGLE)
                .parameters("public ship")
                .ships(Lists.newArrayList(ship))
                .publicMode(true)
                .build();
        turnData.addOrder(order);
        updater.update();
        assertTrue(ship.isPublicTransponder());
    }

    @Test
    void updatePrivate() {
        ship.setPublicTransponder(true);
        final ToggleOrder order = ToggleOrder.builder()
                .empire(empire)
                .orderType(OrderType.TOGGLE)
                .parameters("private ship")
                .ships(Lists.newArrayList(ship))
                .publicMode(false)
                .build();
        turnData.addOrder(order);
        updater.update();
        assertFalse(ship.isPublicTransponder());
    }

    @Test
    void updateDestroyedShip() {
        ship.setPublicTransponder(false);
        ship.destroy(ShipCondition.DESTROYED_IN_COMBAT);
        final ToggleOrder order = ToggleOrder.builder()
                .empire(empire)
                .orderType(OrderType.TOGGLE)
                .parameters("public ship")
                .ships(Lists.newArrayList(ship))
                .publicMode(true)
                .build();
        turnData.addOrder(order);
        updater.update();
        assertFalse(ship.isPublicTransponder());
        assertFalse(ship.isAlive());
    }

    @Test
    void updateShipClass() {
        ship.setPublicTransponder(false);
        final ToggleOrder order = ToggleOrder.builder()
                .empire(empire)
                .orderType(OrderType.TOGGLE)
                .parameters("public fighter")
                .ships(Lists.newArrayList())
                .shipClasses(List.of(fighterClass))
                .publicMode(true)
                .build();
        turnData.addOrder(order);
        updater.update();
        assertTrue(ship.isPublicTransponder());
    }
}