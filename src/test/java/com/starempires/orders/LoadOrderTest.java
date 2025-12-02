package com.starempires.orders;

import com.starempires.objects.Ship;
import com.starempires.util.BaseTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
public class LoadOrderTest extends BaseTest {

    private Ship carrier;
    private Ship probe;

    @BeforeEach
    public void before() {
        carrier = createShip(carrierClass, ZERO_COORDINATE, "carrier", empire1);
        probe = createShip(probeClass, ZERO_COORDINATE, "probe", empire1);
    }

    @Test
    void parse_invalidFormat_addsErrorAndNotReady() {
        // Given: a malformed LOAD command that does not match the regex
        final String params = "cargo to carrier"; // uses "to" instead of "onto" and invalid ship list format

        // When
        final LoadOrder order = LoadOrder.parse(turnData, empire1, params);

        // Then
        assertNotNull(order);
        assertFalse(order.isReady());
        assertTrue(order.getResults().stream().anyMatch(s -> s.contains("Invalid LOAD order")));
    }

    @Test
    void parseUnknownCarrier() {
        // When
        final String params = "probe onto unknown"; // uses "to" instead of "onto" and invalid ship list format

        final LoadOrder order = LoadOrder.parse(turnData, empire1, params);

        // Then
        assertNotNull(order);
        assertFalse(order.isReady());
        assertTrue(order.getResults().stream().anyMatch(s -> s.contains("You do not own carrier")));
    }

    @Test
    void parseCargoIsCarrier() {
        final String params = "carrier onto carrier";
        // When
        final LoadOrder order = LoadOrder.parse(turnData, empire1, params);

        // Then
        assertNotNull(order);
        assertFalse(order.isReady());
        assertTrue(order.getResults().stream().anyMatch(s -> s.contains("Cannot load ship onto itself")));
    }

    @Test
    void parseCargoLoaded() {
        // When

        carrier.loadCargo(probe);
        probe.loadOntoCarrier(carrier);
        final String params = "probe onto carrier";
        final LoadOrder order = LoadOrder.parse(turnData, empire1, params);

        // Then
        assertNotNull(order);
        assertFalse(order.isReady());
        assertTrue(order.getResults().stream().anyMatch(s -> s.contains("Already loaded onto carrier")));
    }

    @Test
    void parseCargoCarrierDifferentSectors() {
        // When
        probe.setCoordinate(ONE_COORDINATE);
        final String params = "probe onto carrier";
        final LoadOrder order = LoadOrder.parse(turnData, empire1, params);

        // Then
        assertNotNull(order);
        assertFalse(order.isReady());
        assertTrue(order.getResults().stream().anyMatch(s -> s.contains("Not in same sector as carrier")));
    }

    @Test
    void parseCarrierNoFreeRacks() {
        final Ship missile1 = createShip(missileClass, ZERO_COORDINATE, "m1", empire1);
        final Ship missile2 = createShip(missileClass, ZERO_COORDINATE, "m2", empire1);
        probe.loadCargo(missile1);

        final String params = "m2 onto probe";
        final LoadOrder order = LoadOrder.parse(turnData, empire1, params);

        // Then
        assertNotNull(order);
        assertFalse(order.isReady());
        assertTrue(order.getResults().stream().anyMatch(s -> s.contains("insufficient free racks")));
    }
}