package com.starempires.updater;

import com.starempires.objects.Storm;
import com.starempires.orders.ModifyStormOrder;
import com.starempires.orders.OrderType;
import com.starempires.util.BaseTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ModifyStormsPhaseUpdaterTest extends BaseTest {

    private ModifyStormsPhaseUpdater updater;

    @BeforeEach
    public void setUp() {
        updater = new ModifyStormsPhaseUpdater(turnData);
    }

    @Test
    void update() {
        final Storm teststorm = createStorm("TestStorm", ZERO_COORDINATE, 1);
        final ModifyStormOrder order = ModifyStormOrder.builder()
                .orderType(OrderType.MODIFYSTORM)
                .gmOnly(true)
                .storm(teststorm)
                .intensity(7)
                .empire(gm)
                .build();
        turnData.addOrder(order);
        updater.update();
        assertEquals(7, teststorm.getIntensity());
    }
}