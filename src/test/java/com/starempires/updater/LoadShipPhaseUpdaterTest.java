package com.starempires.updater;

import com.google.common.collect.Lists;
import com.starempires.objects.Ship;
import com.starempires.objects.ShipCondition;
import com.starempires.orders.LoadOrder;
import com.starempires.orders.OrderType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LoadShipPhaseUpdaterTest extends BasePhaseUpdaterTest {

    private Ship cargo;
    private Ship carrier;
    private LoadOrder order;
    private LoadShipPhaseUpdater updater;

    @BeforeEach
    void setUp() {
        cargo = createShip(probeClass, ZERO_COORDINATE, "cargo", empire);
        updater = new LoadShipPhaseUpdater(turnData);
    }

    @Test
    void updateSuccessfulLoad() {
        carrier = createShip(carrierClass, ZERO_COORDINATE, "carrier", empire);
        order = LoadOrder.builder()
                .empire(empire)
                .orderType(OrderType.LOAD)
                .parameters("cargo onto carrier")
                .carrier(carrier)
                .ships(Lists.newArrayList(cargo))
                .build();
        turnData.addOrder(order);

        updater.update();
        assertTrue(cargo.isLoaded());
        assertTrue(carrier.hasLoadedCargo());
        assertEquals(carrier.getEmptyRacks(), carrier.getRacks() - cargo.getTonnage());
        assertTrue(cargo.hasCondition(ShipCondition.LOADED_ONTO_CARRIER));
        assertTrue(carrier.hasCondition(ShipCondition.LOADED_CARGO));
    }

    @Test
    void updateNotLoadedInsufficientRacks() {
        carrier = createShip(probeClass, ZERO_COORDINATE, "carrier", empire);
        order = LoadOrder.builder()
                .empire(empire)
                .orderType(OrderType.LOAD)
                .parameters("cargo onto carrier")
                .carrier(carrier)
                .ships(Lists.newArrayList(cargo))
                .build();
        turnData.addOrder(order);

        updater.update();
        assertFalse(cargo.isLoaded());
        assertFalse(carrier.hasLoadedCargo());
        assertEquals(carrier.getRacks(), carrier.getEmptyRacks());
        assertFalse(cargo.hasCondition(ShipCondition.LOADED_ONTO_CARRIER));
        assertFalse(carrier.hasCondition(ShipCondition.LOADED_CARGO));
    }

    @Test
    void updateNotLoadedDifferentCoords() {
        carrier = createShip(carrierClass, ONE_COORDINATE, "carrier", empire);
        order = LoadOrder.builder()
                .empire(empire)
                .orderType(OrderType.LOAD)
                .parameters("cargo onto carrier")
                .carrier(carrier)
                .ships(Lists.newArrayList(cargo))
                .build();
        turnData.addOrder(order);

        updater.update();
        assertFalse(cargo.isLoaded());
        assertFalse(carrier.hasLoadedCargo());
        assertEquals(carrier.getRacks(), carrier.getEmptyRacks());
        assertFalse(cargo.hasCondition(ShipCondition.LOADED_ONTO_CARRIER));
        assertFalse(carrier.hasCondition(ShipCondition.LOADED_CARGO));
    }
}