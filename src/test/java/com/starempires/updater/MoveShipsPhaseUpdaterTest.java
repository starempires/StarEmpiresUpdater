package com.starempires.updater;

import com.google.common.collect.Lists;
import com.starempires.objects.Ship;
import com.starempires.objects.ShipCondition;
import com.starempires.orders.MoveOrder;
import com.starempires.orders.OrderType;
import com.starempires.util.BaseTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MoveShipsPhaseUpdaterTest extends BaseTest {

    private MoveShipsPhaseUpdater updater;

    @BeforeEach
    void setUp() {
        updater = new MoveShipsPhaseUpdater(turnData);
    }

    @Test
    void update() {
        final Ship ship = createShip(probeClass, ZERO_COORDINATE, "ship", empire);
        final MoveOrder order = MoveOrder.builder()
                .empire(empire)
                .orderType(OrderType.MOVE)
                .ships(Lists.newArrayList(ship))
                .parameters("TO (1,1)")
                .destination(ONE_COORDINATE)
                .build();
        turnData.addOrder(order);
        updater.update();
        assertEquals(ONE_COORDINATE, ship.getCoordinate());
        assertTrue(ship.hasCondition(ShipCondition.MOVED));
    }

    @Test
    void updateDestroyedMover() {
        final Ship ship = createShip(probeClass, ZERO_COORDINATE, "ship", empire);
        ship.destroy(ShipCondition.DESTROYED_IN_COMBAT);
        final MoveOrder order = MoveOrder.builder()
                .empire(empire)
                .orderType(OrderType.MOVE)
                .ships(Lists.newArrayList(ship))
                .parameters("TO (1,1)")
                .destination(ONE_COORDINATE)
                .build();
        turnData.addOrder(order);
        updater.update();
        assertEquals(ZERO_COORDINATE, ship.getCoordinate());
        assertFalse(ship.hasCondition(ShipCondition.MOVED));
    }

    @Test
    void updateMoveCargo() {
        final Ship cargo = createShip(probeClass, ZERO_COORDINATE, "cargo", empire);
        final Ship carrier = createShip(carrierClass, ZERO_COORDINATE, "carrier", empire);
        turnData.load(cargo, carrier);
        final MoveOrder order = MoveOrder.builder()
                .empire(empire)
                .orderType(OrderType.MOVE)
                .ships(Lists.newArrayList(cargo))
                .parameters("TO (1,1)")
                .destination(ONE_COORDINATE)
                .build();
        turnData.addOrder(order);
        updater.update();
        assertEquals(ZERO_COORDINATE, cargo.getCoordinate());
        assertFalse(cargo.hasCondition(ShipCondition.MOVED));
        assertEquals(ZERO_COORDINATE, carrier.getCoordinate());
        assertFalse(carrier.hasCondition(ShipCondition.MOVED));
    }

    @Test
    void updateMoveCarrier() {
        final Ship cargo = createShip(probeClass, ZERO_COORDINATE, "cargo", empire);
        final Ship carrier = createShip(carrierClass, ZERO_COORDINATE, "carrier", empire);
        turnData.load(cargo, carrier);
        final MoveOrder order = MoveOrder.builder()
                .empire(empire)
                .orderType(OrderType.MOVE)
                .ships(Lists.newArrayList(carrier))
                .parameters("TO (1,1)")
                .destination(ONE_COORDINATE)
                .build();
        turnData.addOrder(order);
        updater.update();
        assertEquals(ONE_COORDINATE, cargo.getCoordinate());
        assertTrue(cargo.hasCondition(ShipCondition.MOVED));
        assertEquals(ONE_COORDINATE, carrier.getCoordinate());
        assertTrue(carrier.hasCondition(ShipCondition.MOVED));
    }

    @Test
    void updateMoveAttacker() {
        final Ship attacker = createShip(probeClass, ZERO_COORDINATE, "attacker", empire);
        attacker.fireGuns(1);
        final MoveOrder order = MoveOrder.builder()
                .empire(empire)
                .orderType(OrderType.MOVE)
                .ships(Lists.newArrayList(attacker))
                .parameters("TO (1,1)")
                .destination(ONE_COORDINATE)
                .build();
        turnData.addOrder(order);
        updater.update();
        assertEquals(ZERO_COORDINATE, attacker.getCoordinate());
        assertFalse(attacker.hasCondition(ShipCondition.MOVED));
    }

    @Test
    void updateMoveTooFar() {
        final Ship ship = createShip(fighterClass, ZERO_COORDINATE, "attacker", empire);
        final MoveOrder order = MoveOrder.builder()
                .empire(empire)
                .orderType(OrderType.MOVE)
                .ships(Lists.newArrayList(ship))
                .parameters("TO (1,1)")
                .destination(ONE_COORDINATE)
                .build();
        turnData.addOrder(order);
        updater.update();
        assertEquals(ZERO_COORDINATE, ship.getCoordinate());
        assertFalse(ship.hasCondition(ShipCondition.MOVED));
    }
}