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
        final AddStormOrder order = AddStormOrder.builder()
                .empire(gm)
                .orderType(OrderType.ADDSTORM)
                .parameters(ONE_COORDINATE + " " + storm.getName() + " " + storm.getRating())
                .storm(storm)
                .build();
        turnData.addOrder(order);
        updater.update();
        assertEquals(storm, turnData.getStorm(storm.getName()));
    }
}