package com.starempires.updater;

import com.google.common.collect.Lists;
import com.starempires.objects.Ship;
import com.starempires.objects.ShipCondition;
import com.starempires.orders.OrderType;
import com.starempires.orders.UnloadOrder;
import com.starempires.util.BaseTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UnloadShipPhaseUpdaterTest extends BaseTest {

    private Ship cargo;
    private Ship carrier;
    private UnloadShipPhaseUpdater updater;

    @BeforeEach
    void setUp() {
        carrier = createShip(carrierClass, ZERO_COORDINATE, "carrier", empire);
        cargo = createShip(probeClass, ZERO_COORDINATE, "cargo", empire);
        final UnloadOrder order = UnloadOrder.builder()
                .empire(empire)
                .orderType(OrderType.UNLOAD)
                .parameters("cargo")
                .ships(Lists.newArrayList(cargo))
                .build();
        turnData.addOrder(order);
        updater = new UnloadShipPhaseUpdater(turnData);
    }

    @Test
    void updateSuccessfulUnload() {
        turnData.load(cargo, carrier);
        updater.update();
        assertFalse(cargo.isLoaded());
        assertFalse(carrier.hasLoadedCargo());
        assertEquals(carrier.getEmptyRacks(), carrier.getRacks());
        assertTrue(cargo.hasCondition(ShipCondition.UNLOADED_FROM_CARRIER));
        assertTrue(carrier.hasCondition(ShipCondition.UNLOADED_CARGO));
    }

    @Test
    void updateNotLoaded() {
        updater.update();
        assertFalse(cargo.isLoaded());
        assertFalse(carrier.hasLoadedCargo());
        assertEquals(carrier.getRacks(), carrier.getEmptyRacks());
        assertFalse(cargo.hasCondition(ShipCondition.UNLOADED_FROM_CARRIER));
        assertFalse(carrier.hasCondition(ShipCondition.UNLOADED_CARGO));
    }
}