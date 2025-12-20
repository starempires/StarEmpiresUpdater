package com.starempires.updater;

import com.starempires.objects.Ship;
import com.starempires.orders.AddShipOrder;
import com.starempires.orders.OrderType;
import com.starempires.util.BaseTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AddShipsPhaseUpdaterTest extends BaseTest {

    private AddShipsPhaseUpdater updater;

    @BeforeEach
    void setUp() {
        updater = new AddShipsPhaseUpdater(turnData);
    }

    @Test
    void testUpdate() {
        final String name = "f1";
        final AddShipOrder order = AddShipOrder.builder()
                .empire(gm)
                .orderType(OrderType.ADDSHIP)
                .parameters(ONE_COORDINATE + " empire1 1 fighter " + name)
                .count(1)
                .coordinate(ONE_COORDINATE)
                .owner(empire1)
                .shipClass(fighterClass)
                .names(List.of(name))
                .dp(1)
                .gmOnly(true)
                .build();
        turnData.addOrder(order);
        updater.update();
        final Ship ship = empire1.getShip(name);
        assertEquals(name, ship.getName());
        assertEquals(fighterClass, ship.getShipClass());
        assertEquals(1, ship.getDpRemaining());
    }
}