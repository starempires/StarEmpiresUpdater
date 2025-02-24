package com.starempires.updater;

import com.google.common.collect.Lists;
import com.starempires.objects.Ship;
import com.starempires.objects.ShipCondition;
import com.starempires.orders.DestructOrder;
import com.starempires.orders.OrderType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DestructShipsPhaseUpdaterTest extends BasePhaseUpdaterTest {

    private DestructShipsPhaseUpdater updater;

    @BeforeEach
    void setUp() {
        updater = new DestructShipsPhaseUpdater(turnData);
    }

    @Test
    void update() {
        final Ship ship = createShip(probeClass, ZERO_COORDINATE, "ship", empire);
        final Ship collateral = createShip(probeClass, ZERO_COORDINATE, "collateral", empire);

        final DestructOrder order = DestructOrder.builder()
                .empire(empire)
                .orderType(OrderType.DESTRUCT)
                .parameters("ship")
                .ships(Lists.newArrayList(ship))
                .build();
        turnData.addOrder(order);
        updater.update();
        assertFalse(ship.isAlive());
        assertTrue(ship.hasCondition(ShipCondition.SELF_DESTRUCTED));
        assertEquals(1, collateral.getCombatDamageAccrued());
        assertTrue(collateral.hasCondition(ShipCondition.HIT_IN_COMBAT));
    }

    @Test
    void updateDestructLoadedCargo() {
        final Ship ship = createShip(carrierClass, ZERO_COORDINATE, "ship", empire);
        final Ship cargo = createShip(probeClass, ZERO_COORDINATE, "cargo", empire);
        turnData.load(cargo, ship);

        final DestructOrder order = DestructOrder.builder()
                .empire(empire)
                .orderType(OrderType.DESTRUCT)
                .parameters("ship")
                .ships(Lists.newArrayList(ship))
                .build();
        turnData.addOrder(order);
        updater.update();
        assertFalse(ship.isAlive());
        assertFalse(cargo.isAlive());
        assertTrue(ship.hasCondition(ShipCondition.SELF_DESTRUCTED));
        assertTrue(cargo.hasCondition(ShipCondition.SELF_DESTRUCTED));
    }

    @Test
    void updateDestructFailedLoadedShip() {
        final Ship ship = createShip(probeClass, ZERO_COORDINATE, "ship", empire);
        final Ship carrier = createShip(carrierClass, ZERO_COORDINATE, "carrier", empire);
        turnData.load(ship, carrier);

        final DestructOrder order = DestructOrder.builder()
                .empire(empire)
                .orderType(OrderType.DESTRUCT)
                .parameters("ship")
                .ships(Lists.newArrayList(ship))
                .build();
        turnData.addOrder(order);
        updater.update();
        assertTrue(ship.isAlive());
        assertTrue(ship.isLoaded());
        assertFalse(ship.hasCondition(ShipCondition.SELF_DESTRUCTED));
        assertEquals(0, carrier.getCombatDamageAccrued());
    }

    @Test
    void updateDestructFailedStarbase() {
        final Ship ship = createShip(starbaseClass, ZERO_COORDINATE, "ship", empire);

        final DestructOrder order = DestructOrder.builder()
                .empire(empire)
                .orderType(OrderType.DESTRUCT)
                .parameters("ship")
                .ships(Lists.newArrayList(ship))
                .build();
        turnData.addOrder(order);
        updater.update();
        assertTrue(ship.isAlive());
        assertFalse(ship.hasCondition(ShipCondition.SELF_DESTRUCTED));
        assertEquals(0, ship.getCombatDamageAccrued());
    }
}