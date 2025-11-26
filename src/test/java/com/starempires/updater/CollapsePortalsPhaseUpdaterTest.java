package com.starempires.updater;

import com.starempires.objects.Portal;
import com.starempires.objects.Ship;
import com.starempires.util.BaseTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class CollapsePortalsPhaseUpdaterTest extends BaseTest {

    private CollapsePortalsPhaseUpdater updater;

    @BeforeEach
    void setUp() {
        updater = new CollapsePortalsPhaseUpdater(turnData);
    }

    @Test
    void update() {
        final Portal testPortal = createPortal("testportal", ZERO_COORDINATE, false);
        turnData.addPortal(testPortal);
        final Ship hammer = createShip(hammerClass, ZERO_COORDINATE, "hammer", empire1);
        turnData.deploy(hammer);
        updater.update();
        assertTrue(testPortal.isCollapsed());
    }
}