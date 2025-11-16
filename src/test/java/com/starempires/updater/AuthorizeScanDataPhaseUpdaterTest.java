package com.starempires.updater;

import com.starempires.objects.Ship;
import com.starempires.objects.ShipCondition;
import com.starempires.orders.AuthorizeOrder;
import com.starempires.orders.OrderType;
import com.starempires.util.BaseTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuthorizeScanDataPhaseUpdaterTest extends BaseTest {

    private AuthorizeScanDataPhaseUpdater updater;

    @BeforeEach
    void setUp() {
        updater = new AuthorizeScanDataPhaseUpdater(turnData);
        empire1.addKnownEmpire(empire2);
    }

    @Test
    void updateAuthorizeAllSectors() {
        final AuthorizeOrder order = AuthorizeOrder.builder()
                .empire(empire1)
                .orderType(OrderType.AUTHORIZE)
                .parameters("all to empire2")
                .recipients(List.of(empire2))
                .allSectors(true)
                .build();
        turnData.addOrder(order);
        updater.update();
        assertTrue(empire1.getShareEmpires().contains(empire2));
    }

    @Test
    void updateAuthorizeShips() {
        final Ship probe = createShip(probeClass, ZERO_COORDINATE, "probe", empire1);
        final AuthorizeOrder order = AuthorizeOrder.builder()
                .empire(empire1)
                .orderType(OrderType.AUTHORIZE)
                .parameters("probe to empire2")
                .recipients(List.of(empire2))
                .ships(List.of(probe))
                .build();
        turnData.addOrder(order);
        updater.update();
        assertTrue(empire1.getShareShips().get(empire2).contains(probe));
    }

    @Test
    void updateAuthorizeDestroyedShips() {
        final Ship probe = createShip(probeClass, ZERO_COORDINATE, "probe", empire1);
        probe.destroy(ShipCondition.DESTROYED_IN_COMBAT);
        final AuthorizeOrder order = AuthorizeOrder.builder()
                .empire(empire1)
                .orderType(OrderType.AUTHORIZE)
                .parameters("probe to empire2")
                .recipients(List.of(empire2))
                .ships(List.of(probe))
                .build();
        turnData.addOrder(order);
        updater.update();
        assertFalse(empire1.getShareShips().get(empire2).contains(probe));
    }

    @Test
    void updateAuthorizeCoordinate() {
        final AuthorizeOrder order = AuthorizeOrder.builder()
                .empire(empire1)
                .orderType(OrderType.AUTHORIZE)
                .parameters("0,0 1 to empire2")
                .recipients(List.of(empire2))
                .coordinates(List.of(ZERO_COORDINATE))
                .build();
        turnData.addOrder(order);
        updater.update();
        assertTrue(empire1.getShareCoordinates().get(empire2).contains(ZERO_COORDINATE));
    }

    @Test
    void updateAuthorizeLocation() {
        final AuthorizeOrder order = AuthorizeOrder.builder()
                .empire(empire1)
                .orderType(OrderType.AUTHORIZE)
                .parameters("@world to empire2")
                .recipients(List.of(empire2))
                .mapObjects(List.of(world))
                .build();
        turnData.addOrder(order);
        updater.update();
        assertTrue(empire1.getShareObjects().get(empire2).contains(world));
    }
}