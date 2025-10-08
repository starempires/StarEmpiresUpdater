package com.starempires.orders;

import com.starempires.objects.Ship;
import com.starempires.util.BaseTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UnloadOrderTest extends BaseTest {
    private Ship carrier;
    private Ship probe;

    @BeforeEach
    public void before() {
        carrier = createShip(carrierClass, ZERO_COORDINATE, "carrier", empire1);
        probe = createShip(probeClass, ZERO_COORDINATE, "probe", empire1);
    }

    @Test
    void parse_invalidFormat_addsErrorAndNotReady() {
        // When
        final UnloadOrder order = UnloadOrder.parse(turnData, empire1, "");

        // Then
        assertNotNull(order);
        assertFalse(order.isReady());
        assertTrue(order.getResults().stream().anyMatch(s -> s.contains("Invalid UNLOAD order")));
    }

    @Test
    void parseUnloadSuccess() {
        carrier.loadCargo(probe);
        probe.loadOntoCarrier(carrier);

        final UnloadOrder order = UnloadOrder.parse(turnData, empire1, "probe");

        // Then
        assertTrue(order.isReady());
        assertFalse(probe.isLoaded());
        assertTrue(order.ships.contains(probe));
    }

    @Test
    void parseUnloadNotLoaded() {
        final UnloadOrder order = UnloadOrder.parse(turnData, empire1, "probe");

        // Then
        assertFalse(order.isReady());
        assertTrue(order.ships.isEmpty());
    }

    @Test
    void parseUnloadUnknownShip() {
        final UnloadOrder order = UnloadOrder.parse(turnData, empire1, "unknown");

        // Then
        assertFalse(order.isReady());
        assertTrue(order.ships.isEmpty());
    }
}