package com.starempires.updater;

import com.starempires.objects.Ship;
import com.starempires.objects.ShipCondition;
import com.starempires.orders.DeployOrder;
import com.starempires.orders.OrderType;
import com.starempires.util.BaseTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DeployDevicesPhaseUpdaterTest extends BaseTest {

    private DeployDevicesPhaseUpdater updater;

    @BeforeEach
    public void setUp() {
        updater = new DeployDevicesPhaseUpdater(turnData);
    }

    @Test
    public void update() {
        final Ship device = createShip(hammerClass, ZERO_COORDINATE, "hammer", empire1);
        final DeployOrder order = DeployOrder.builder()
                .empire(empire1)
                .ships(List.of(device))
                .orderType(OrderType.DEPLOY)
                .parameters("device")
                .build();
        turnData.addOrder(order);
        updater.update();
        assertTrue(device.hasCondition(ShipCondition.DEPLOYED));
        assertEquals(device.getDpRemaining(), device.getDeploymentDamageAccrued());
    }
}