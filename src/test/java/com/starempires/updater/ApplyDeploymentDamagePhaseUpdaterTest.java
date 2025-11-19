package com.starempires.updater;

import com.starempires.objects.Ship;
import com.starempires.objects.ShipCondition;
import com.starempires.util.BaseTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ApplyDeploymentDamagePhaseUpdaterTest extends BaseTest {

    private ApplyDeploymentDamagePhaseUpdater updater;

    @BeforeEach
    void setUp() {
        updater = new ApplyDeploymentDamagePhaseUpdater(turnData);
    }

    @Test
    void update() {
        final Ship device = createShip(shieldClass, ZERO_COORDINATE, "shield", empire1);
        device.deploy();
        updater.update();
        assertEquals(0, device.getDpRemaining());
        assertTrue(device.hasCondition(ShipCondition.DEPLOYED));
    }
}