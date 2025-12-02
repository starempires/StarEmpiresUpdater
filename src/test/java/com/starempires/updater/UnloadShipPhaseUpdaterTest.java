package com.starempires.updater;

import com.google.common.collect.Lists;
import com.starempires.objects.Ship;
import com.starempires.objects.ShipCondition;
import com.starempires.orders.DeployOrder;
import com.starempires.orders.FireOrder;
import com.starempires.orders.OrderType;
import com.starempires.orders.UnloadOrder;
import com.starempires.util.BaseTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UnloadShipPhaseUpdaterTest extends BaseTest {

    private Ship cargo;
    private Ship carrier;
    private UnloadShipPhaseUpdater updater;

    @BeforeEach
    void setUp() {
        carrier = createShip(carrierClass, ZERO_COORDINATE, "carrier", empire1);
        cargo = createShip(probeClass, ZERO_COORDINATE, "cargo", empire1);
        updater = new UnloadShipPhaseUpdater(turnData);
    }

    @Test
    void updateSuccessfulUnload() {
        final UnloadOrder order = UnloadOrder.builder()
                .empire(empire1)
                .orderType(OrderType.UNLOAD)
                .parameters("cargo")
                .ships(Lists.newArrayList(cargo))
                .build();
        turnData.addOrder(order);
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
        final UnloadOrder order = UnloadOrder.builder()
                .empire(empire1)
                .orderType(OrderType.UNLOAD)
                .parameters("cargo")
                .ships(Lists.newArrayList(cargo))
                .build();
        turnData.addOrder(order);
        updater.update();
        assertFalse(cargo.isLoaded());
        assertFalse(carrier.hasLoadedCargo());
        assertEquals(carrier.getRacks(), carrier.getEmptyRacks());
        assertFalse(cargo.hasCondition(ShipCondition.UNLOADED_FROM_CARRIER));
        assertFalse(carrier.hasCondition(ShipCondition.UNLOADED_CARGO));
    }

    @Test
    void updateCreateUnloadForDeployOrder() {
        final Ship probe = createShip(probeClass, ZERO_COORDINATE, "probe", empire1);
        turnData.load(probe, carrier);
        final DeployOrder order = DeployOrder.builder()
                .empire(empire1)
                .orderType(OrderType.DEPLOY)
                .parameters("probe")
                .ships(Lists.newArrayList(probe))
                .build();
        turnData.addOrder(order);
        updater.update();
        assertFalse(probe.isLoaded());
        assertTrue(turnData.getOrders(OrderType.UNLOAD).getFirst().isSynthetic());
    }

    @Test
    void updateCreateUnloadForFireOrder() {
        final Ship missile = createShip(missileClass, ZERO_COORDINATE, "missile", empire1);
        turnData.load(missile, carrier);
        final FireOrder order = FireOrder.builder()
                .empire(empire1)
                .orderType(OrderType.FIRE)
                .parameters("missile")
                .ships(List.of(missile))
                .targets(List.of(empire2))
                .build();
        turnData.addOrder(order);
        updater.update();
        assertFalse(missile.isLoaded());
        assertTrue(turnData.getOrders(OrderType.UNLOAD).getFirst().isSynthetic());
    }
}