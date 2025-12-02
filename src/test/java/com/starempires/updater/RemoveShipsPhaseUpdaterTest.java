package com.starempires.updater;

import com.starempires.objects.Ship;
import com.starempires.orders.OrderType;
import com.starempires.orders.RemoveShipOrder;
import com.starempires.util.BaseTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNull;

class RemoveShipsPhaseUpdaterTest extends BaseTest {
    private RemoveShipsPhaseUpdater updater;

    @BeforeEach
    void setUp() {
        updater = new RemoveShipsPhaseUpdater(turnData);
    }

    @Test
    void update() {
        final Ship frigate = createShip(frigateClass, ZERO_COORDINATE, "frigate", empire1);
        final RemoveShipOrder order = RemoveShipOrder.builder()
                .empire(gm)
                .orderType(OrderType.REMOVESHIP)
                .parameters("empire1 ship")
                .ships(List.of(frigate))
                .owner(empire1)
                .build();
        turnData.addOrder(order);
        updater.update();
        assertNull(empire1.getShip("frigate"));
    }
}