package com.starempires.orders;

import com.starempires.objects.Coordinate;
import com.starempires.objects.Empire;
import com.starempires.objects.Ship;
import com.starempires.objects.ShipClass;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
public class LoadOrderTest {

    private static final Coordinate COORDINATE_ZERO = new Coordinate(0, 0);
    private static final Coordinate COORDINATE_ONE = new Coordinate(1, 1);
    private static final ShipClass ONE_TONNER = ShipClass.builder().tonnage(1).build();
    @Mock
    private Empire empire;

    private Ship carrier;
    private Ship cargo;

    private static final String PARAMS = "cargo onto carrier";

    @BeforeEach
    public void before() {
        carrier = Ship.builder()
                .name("carrier")
                .coordinate(COORDINATE_ZERO)
                .owner(empire)
                .dpRemaining(1)
                .shipClass(ShipClass.builder().racks(1).build())
                .build();
        cargo = Ship.builder()
                .name("cargo")
                .coordinate(COORDINATE_ZERO)
                .owner(empire)
                .dpRemaining(1)
                .shipClass(ONE_TONNER)
                .build();
        lenient().when(empire.getShip("cargo")).thenReturn(cargo);
        lenient().when(empire.getShip("carrier")).thenReturn(carrier);
        lenient().when(empire.getName()).thenReturn("MCRN");
    }

    @Test
    void parse_invalidFormat_addsErrorAndNotReady() {
        // Given: a malformed LOAD command that does not match the regex
        String params = "cargo to carrier"; // uses "to" instead of "onto" and invalid ship list format

        // When
        LoadOrder order = LoadOrder.parse(null, empire, params);

        // Then
        assertNotNull(order);
        assertFalse(order.isReady());
        assertTrue(order.getResults().stream().anyMatch(s -> s.contains("Invalid LOAD order")));
    }

    @Test
    void parseUnknownCarrier() {
        // When
        lenient().when(empire.getShip("carrier")).thenReturn(null);
        LoadOrder order = LoadOrder.parse(null, empire, PARAMS);

        // Then
        assertNotNull(order);
        assertFalse(order.isReady());
        assertTrue(order.getResults().stream().anyMatch(s -> s.contains("You do not own carrier")));
    }

    @Test
    void parseCargoIsCarrier() {
        final String params = "carrier onto carrier";
        // When
        LoadOrder order = LoadOrder.parse(null, empire, params);

        // Then
        assertNotNull(order);
        assertFalse(order.isReady());
        assertTrue(order.getResults().stream().anyMatch(s -> s.contains("Cannot load ship onto itself")));
    }

    @Test
    void parseCargoLoaded() {
        // When
        cargo.setCarrier(carrier);
        LoadOrder order = LoadOrder.parse(null, empire, PARAMS);

        // Then
        assertNotNull(order);
        assertFalse(order.isReady());
        assertTrue(order.getResults().stream().anyMatch(s -> s.contains("Already loaded onto carrier")));
    }

    @Test
    void parseCargoCarrierDifferentSectors() {
        // When
        cargo.setCoordinate(COORDINATE_ONE);
        LoadOrder order = LoadOrder.parse(null, empire, PARAMS);

        // Then
        assertNotNull(order);
        assertFalse(order.isReady());
        assertTrue(order.getResults().stream().anyMatch(s -> s.contains("Not in same sector as carrier")));
    }

    @Test
    void parseCarrierNoFreeRacks() {
        carrier.loadCargo(Ship.builder().dpRemaining(1).shipClass(ONE_TONNER).build());
        LoadOrder order = LoadOrder.parse(null, empire, PARAMS);

        // Then
        assertNotNull(order);
        assertFalse(order.isReady());
        assertTrue(order.getResults().stream().anyMatch(s -> s.contains("insufficient free racks")));
    }
}