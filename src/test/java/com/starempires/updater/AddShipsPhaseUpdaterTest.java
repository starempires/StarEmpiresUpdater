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
        final Ship ship = Ship
                .builder()
                .shipClass(fighterClass)
                .dpRemaining(fighterClass.getDp())
                .coordinate(ONE_COORDINATE)
                .name(name)
                .serialNumber("FX12345")
                .owner(empire1)
                .build();
        final AddShipOrder order = AddShipOrder.builder()
                .empire(gm)
                .orderType(OrderType.ADDSHIP)
                .parameters(ONE_COORDINATE + " empire1 1 fighter f1")
                .ships(List.of(ship))
                .gmOnly(true)
                .build();
        turnData.addOrder(order);
        updater.update();
        assertEquals(ship, empire1.getShip(name));
    }
}