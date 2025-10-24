package com.starempires.updater;

import com.starempires.objects.Ship;
import com.starempires.orders.OrderType;
import com.starempires.orders.RelocateShipOrder;
import com.starempires.util.BaseTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RelocateShipsPhaseUpdaterTest extends BaseTest {

    private RelocateShipsPhaseUpdater updater;

    @BeforeEach
    void setUp() {
        updater = new RelocateShipsPhaseUpdater(turnData);
    }

    @Test
    void update() {
        final Ship ship = createShip(fighterClass, ZERO_COORDINATE, "ship", empire1);
        final RelocateShipOrder order = RelocateShipOrder.builder()
                .empire(gm)
                .orderType(OrderType.RELOCATESHIP)
                .parameters("empire1 ship to " + ONE_COORDINATE)
                .owner(empire1)
                .ships(List.of(ship))
                .coordinate(ONE_COORDINATE)
                .build();
        turnData.addOrder(order);
        updater.update();
        assertEquals(ONE_COORDINATE, ship.getCoordinate());
    }
}