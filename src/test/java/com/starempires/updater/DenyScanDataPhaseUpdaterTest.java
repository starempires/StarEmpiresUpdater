package com.starempires.updater;

import com.starempires.objects.Coordinate;
import com.starempires.objects.MappableObject;
import com.starempires.objects.Ship;
import com.starempires.orders.DenyOrder;
import com.starempires.orders.OrderType;
import com.starempires.util.BaseTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;

class DenyScanDataPhaseUpdaterTest extends BaseTest {

    private DenyScanDataPhaseUpdater updater;

    @BeforeEach
    void setUp() {
        updater = new DenyScanDataPhaseUpdater(turnData);
        empire1.addKnownEmpire(empire2);
    }

    @Test
    void updateDenyAllSectors() {
        empire1.addEmpireScanAccess(empire2);
        final DenyOrder order = DenyOrder.builder()
                .empire(empire1)
                .orderType(OrderType.DENY)
                .parameters("all to empire2")
                .recipients(List.of(empire2))
                .allSectors(true)
                .build();
        turnData.addOrder(order);
        updater.update();
        assertFalse(empire1.getShareEmpires().contains(empire2));
    }

    @Test
    void updateDenyShips() {
        final Ship probe = createShip(probeClass, ZERO_COORDINATE, "probe", empire1);
        empire1.addShipScanAccess(empire2, List.of(probe));
        final DenyOrder order = DenyOrder.builder()
                .empire(empire1)
                .orderType(OrderType.DENY)
                .parameters("probe to empire2")
                .recipients(List.of(empire2))
                .ships(List.of(probe))
                .build();
        turnData.addOrder(order);
        updater.update();
        assertFalse(empire1.getShareShips().get(empire2).contains(probe));
    }

    @Test
    void updateDenyCoordinate() {
        final List<Coordinate> coordinates = List.of(ZERO_COORDINATE);
        empire1.addCoordinateScanAccess(empire2, coordinates);
        final DenyOrder order = DenyOrder.builder()
                .empire(empire1)
                .orderType(OrderType.DENY)
                .parameters("0,0 1 to empire2")
                .recipients(List.of(empire2))
                .coordinates(coordinates)
                .build();
        turnData.addOrder(order);
        updater.update();
        assertFalse(empire1.getShareCoordinates().get(empire2).contains(ZERO_COORDINATE));
    }

    @Test
    void updateDenyLocation() {
        final List<MappableObject> mapObjects = List.of(world);
        empire1.addObjectScanAccess(empire2, mapObjects);
        final DenyOrder order = DenyOrder.builder()
                .empire(empire1)
                .orderType(OrderType.DENY)
                .parameters("@world to empire2")
                .recipients(List.of(empire2))
                .mapObjects(mapObjects)
                .build();
        turnData.addOrder(order);
        updater.update();
        assertFalse(empire1.getShareObjects().get(empire2).contains(world));
    }
}