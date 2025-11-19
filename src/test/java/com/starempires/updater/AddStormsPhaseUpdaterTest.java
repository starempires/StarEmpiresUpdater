package com.starempires.updater;

import com.starempires.orders.AddStormOrder;
import com.starempires.orders.OrderType;
import com.starempires.util.BaseTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AddStormsPhaseUpdaterTest extends BaseTest {

    private AddStormsPhaseUpdater updater;

    @BeforeEach
    void setUp() {
        updater = new AddStormsPhaseUpdater(turnData);
    }

    @Test
    void testUpdate() {
        final String name = "s1";
        final int intensity = 1;
        final AddStormOrder order = AddStormOrder.builder()
                .empire(gm)
                .orderType(OrderType.ADDSTORM)
                .parameters(ONE_COORDINATE + " " + name + " " + intensity)
                .name(name)
                .intensity(intensity)
                .coordinate(ONE_COORDINATE)
                .build();
        turnData.addOrder(order);
        updater.update();
        assertEquals(ONE_COORDINATE, turnData.getStorm(name).getCoordinate());
    }
}