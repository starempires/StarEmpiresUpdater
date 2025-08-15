package com.starempires.updater;

import com.google.common.collect.Lists;
import com.starempires.objects.Portal;
import com.starempires.objects.Ship;
import com.starempires.objects.ShipCondition;
import com.starempires.orders.OrderType;
import com.starempires.orders.TraverseOrder;
import com.starempires.util.BaseTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TraversePortalsPhaseUpdaterTest extends BaseTest {

    private TraversePortalsPhaseUpdater updater;

    @BeforeEach
    void setUp() {
        updater = new TraversePortalsPhaseUpdater(turnData);
    }

    @Test
    void updateSpecifiedExit() {
        final Ship ship = createShip(probeClass, ZERO_COORDINATE, "ship", empire);
        final Portal entry = createPortal("entry", ZERO_COORDINATE, false);
        final Portal exit = createPortal("exit", ONE_COORDINATE, false);
        turnData.addPortalConnection("entry", "exit");
        final TraverseOrder order = TraverseOrder.builder()
                .empire(empire)
                .orderType(OrderType.TRAVERSE)
                .ships(Lists.newArrayList(ship))
                .parameters("ship portal entry exit")
                .entry(entry)
                .exit(exit)
                .build();
        turnData.addOrder(order);
        updater.update();
        assertEquals(ONE_COORDINATE, ship.getCoordinate());
        assertTrue(ship.hasCondition(ShipCondition.MOVED));
        assertTrue(ship.hasCondition(ShipCondition.TRAVERSED_WORMNET));
    }

    @Test
    void updateUnspecifiedExit() {
        final Ship ship = createShip(probeClass, ZERO_COORDINATE, "ship", empire);
        final Portal entry = createPortal("entry", ZERO_COORDINATE, false);
        final Portal exit = createPortal("exit", ONE_COORDINATE, false);
        turnData.addPortalConnection("entry", "exit");
        final TraverseOrder order = TraverseOrder.builder()
                .empire(empire)
                .orderType(OrderType.TRAVERSE)
                .ships(Lists.newArrayList(ship))
                .parameters("ship portal entry")
                .entry(entry)
                .build();
        turnData.addOrder(order);
        updater.update();
        assertEquals(ONE_COORDINATE, ship.getCoordinate());
        assertTrue(ship.hasCondition(ShipCondition.MOVED));
        assertTrue(ship.hasCondition(ShipCondition.TRAVERSED_WORMNET));
    }

    @Test
    void updateDestroyedShip() {
        final Ship ship = createShip(probeClass, ZERO_COORDINATE, "ship", empire);
        ship.destroy(ShipCondition.DESTROYED_IN_COMBAT);
        final Portal entry = createPortal("entry", ZERO_COORDINATE, false);
        final Portal exit = createPortal("exit", ONE_COORDINATE, false);
        turnData.addPortalConnection("entry", "exit");
        final TraverseOrder order = TraverseOrder.builder()
                .empire(empire)
                .orderType(OrderType.TRAVERSE)
                .ships(Lists.newArrayList(ship))
                .parameters("ship portal entry")
                .entry(entry)
                .exit(exit)
                .build();
        turnData.addOrder(order);
        updater.update();
        assertEquals(ZERO_COORDINATE, ship.getCoordinate());
        assertFalse(ship.hasCondition(ShipCondition.MOVED));
        assertFalse(ship.hasCondition(ShipCondition.TRAVERSED_WORMNET));
    }

    @Test
    void updateNoEngines() {
        final Ship ship = createShip(starbaseClass, ZERO_COORDINATE, "ship", empire);
        final Portal entry = createPortal("entry", ZERO_COORDINATE, false);
        final Portal exit = createPortal("exit", ONE_COORDINATE, false);
        turnData.addPortalConnection("entry", "exit");
        final TraverseOrder order = TraverseOrder.builder()
                .empire(empire)
                .orderType(OrderType.TRAVERSE)
                .ships(Lists.newArrayList(ship))
                .parameters("ship portal entry")
                .entry(entry)
                .exit(exit)
                .build();
        turnData.addOrder(order);
        updater.update();
        assertEquals(ZERO_COORDINATE, ship.getCoordinate());
        assertFalse(ship.hasCondition(ShipCondition.MOVED));
        assertFalse(ship.hasCondition(ShipCondition.TRAVERSED_WORMNET));
    }

    @Test
    void updateCargoOnly() {
        final Ship carrier = createShip(carrierClass, ZERO_COORDINATE, "carrier", empire);
        final Ship cargo = createShip(probeClass, ZERO_COORDINATE, "cargo", empire);
        turnData.load(cargo, carrier);
        final Portal entry = createPortal("entry", ZERO_COORDINATE, false);
        final Portal exit = createPortal("exit", ONE_COORDINATE, false);
        turnData.addPortalConnection("entry", "exit");
        final TraverseOrder order = TraverseOrder.builder()
                .empire(empire)
                .orderType(OrderType.TRAVERSE)
                .ships(Lists.newArrayList(cargo))
                .parameters("ship portal entry")
                .entry(entry)
                .exit(exit)
                .build();
        turnData.addOrder(order);
        updater.update();
        assertEquals(ZERO_COORDINATE, cargo.getCoordinate());
        assertFalse(cargo.hasCondition(ShipCondition.MOVED));
        assertFalse(cargo.hasCondition(ShipCondition.TRAVERSED_WORMNET));
    }

    @Test
    void updateCarrierAndCargo() {
        final Ship carrier = createShip(carrierClass, ZERO_COORDINATE, "carrier", empire);
        final Ship cargo = createShip(probeClass, ZERO_COORDINATE, "cargo", empire);
        turnData.load(cargo, carrier);
        final Portal entry = createPortal("entry", ZERO_COORDINATE, false);
        final Portal exit = createPortal("exit", ONE_COORDINATE, false);
        turnData.addPortalConnection("entry", "exit");
        final TraverseOrder order = TraverseOrder.builder()
                .empire(empire)
                .orderType(OrderType.TRAVERSE)
                .ships(Lists.newArrayList(carrier))
                .parameters("ship portal entry")
                .entry(entry)
                .exit(exit)
                .build();
        turnData.addOrder(order);
        updater.update();
        assertEquals(ONE_COORDINATE, cargo.getCoordinate());
        assertEquals(ONE_COORDINATE, carrier.getCoordinate());
        assertTrue(cargo.hasCondition(ShipCondition.MOVED));
        assertTrue(cargo.hasCondition(ShipCondition.TRAVERSED_WORMNET));
        assertTrue(carrier.hasCondition(ShipCondition.MOVED));
        assertTrue(carrier.hasCondition(ShipCondition.TRAVERSED_WORMNET));
    }

    @Test
    void updateAttackingShip() {
        final Ship ship = createShip(carrierClass, ZERO_COORDINATE, "ship", empire);
        ship.fireGuns(1);
        final Portal entry = createPortal("entry", ZERO_COORDINATE, true);
        final Portal exit = createPortal("exit", ONE_COORDINATE, false);
        turnData.addPortalConnection("entry", "exit");
        final TraverseOrder order = TraverseOrder.builder()
                .empire(empire)
                .orderType(OrderType.TRAVERSE)
                .ships(Lists.newArrayList(ship))
                .parameters("ship portal entry")
                .entry(entry)
                .exit(exit)
                .build();
        turnData.addOrder(order);
        updater.update();
        assertEquals(ZERO_COORDINATE, ship.getCoordinate());
        assertFalse(ship.hasCondition(ShipCondition.MOVED));
        assertFalse(ship.hasCondition(ShipCondition.TRAVERSED_WORMNET));
    }

    @Test
    void updateEntryCollapsed() {
        final Ship ship = createShip(probeClass, ZERO_COORDINATE, "ship", empire);
        final Portal entry = createPortal("entry", ZERO_COORDINATE, true);
        final Portal exit = createPortal("exit", ONE_COORDINATE, false);
        turnData.addPortalConnection("entry", "exit");
        final TraverseOrder order = TraverseOrder.builder()
                .empire(empire)
                .orderType(OrderType.TRAVERSE)
                .ships(Lists.newArrayList(ship))
                .parameters("ship portal entry")
                .entry(entry)
                .exit(exit)
                .build();
        turnData.addOrder(order);
        updater.update();
        assertEquals(ZERO_COORDINATE, ship.getCoordinate());
        assertFalse(ship.hasCondition(ShipCondition.MOVED));
        assertFalse(ship.hasCondition(ShipCondition.TRAVERSED_WORMNET));
    }

    @Test
    void updateExitCollapsed() {
        final Ship ship = createShip(probeClass, ZERO_COORDINATE, "ship", empire);
        final Portal entry = createPortal("entry", ZERO_COORDINATE, false);
        final Portal exit = createPortal("exit", ONE_COORDINATE, true);
        turnData.addPortalConnection("entry", "exit");
        final TraverseOrder order = TraverseOrder.builder()
                .empire(empire)
                .orderType(OrderType.TRAVERSE)
                .ships(Lists.newArrayList(ship))
                .parameters("ship portal entry")
                .entry(entry)
                .exit(exit)
                .build();
        turnData.addOrder(order);
        updater.update();
        assertEquals(ZERO_COORDINATE, ship.getCoordinate());
        assertFalse(ship.hasCondition(ShipCondition.MOVED));
        assertFalse(ship.hasCondition(ShipCondition.TRAVERSED_WORMNET));
    }

    @Test
    void updateDisjointPortals() {
        final Ship ship = createShip(probeClass, ZERO_COORDINATE, "ship", empire);
        final Portal entry = createPortal("entry", ZERO_COORDINATE, false);
        final Portal exit = createPortal("exit", ONE_COORDINATE, false);
        final TraverseOrder order = TraverseOrder.builder()
                .empire(empire)
                .orderType(OrderType.TRAVERSE)
                .ships(Lists.newArrayList(ship))
                .parameters("ship portal entry")
                .entry(entry)
                .exit(exit)
                .build();
        turnData.addOrder(order);
        updater.update();
        assertEquals(ZERO_COORDINATE, ship.getCoordinate());
        assertFalse(ship.hasCondition(ShipCondition.MOVED));
        assertFalse(ship.hasCondition(ShipCondition.TRAVERSED_WORMNET));
    }

}