package com.starempires.orders;

import com.starempires.objects.Ship;
import com.starempires.objects.ShipCondition;
import com.starempires.util.BaseTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DeployOrderTest extends BaseTest {

    private Ship device;
    private Ship carrier;

    @BeforeEach
    public void before() {
        carrier = createShip(carrierClass, ZERO_COORDINATE, "carrier", empire1);
        device = createShip(hammerClass, ZERO_COORDINATE, "hammer", empire1);
    }

    @Test
    void parse_invalidFormat_addsErrorAndNotReady() {
        // When
        final DeployOrder order = DeployOrder.parse(turnData, empire1, "");

        // Then
        assertNotNull(order);
        assertFalse(order.isReady());
        assertTrue(order.getResults().stream().anyMatch(s -> s.contains("Invalid DEPLOY order")));
    }

    @Test
    void parseDeployLoadedSuccess() {
        turnData.load(device, carrier);
        final DeployOrder order = DeployOrder.parse(turnData, empire1, "hammer");

        // Then
        assertTrue(order.isReady());
        assertTrue(device.getConditions().contains(ShipCondition.DEPLOYED));
        assertTrue(order.ships.contains(device));
    }

    @Test
    void parseDeploySuccess() {
        final DeployOrder order = DeployOrder.parse(turnData, empire1, "hammer");

        // Then
        assertTrue(order.isReady());
        assertTrue(device.getConditions().contains(ShipCondition.DEPLOYED));
        assertTrue(order.ships.contains(device));
    }

    @Test
    void parseDeployUnknownShip() {
        final DeployOrder order = DeployOrder.parse(turnData, empire1, "unknown");

        // Then
        assertFalse(order.isReady());
        assertTrue(order.ships.isEmpty());
    }

    @Test
    void parseDeployNonDevice() {
        final DeployOrder order = DeployOrder.parse(turnData, empire1, "carrier");

        // Then
        assertFalse(order.isReady());
        assertTrue(order.ships.isEmpty());
    }
}