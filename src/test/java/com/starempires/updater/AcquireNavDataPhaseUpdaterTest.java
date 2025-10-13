package com.starempires.updater;

import com.starempires.util.BaseTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AcquireNavDataPhaseUpdaterTest extends BaseTest {

    private AcquireNavDataPhaseUpdater updater;

    @BeforeEach
    void setUp() {
        updater = new AcquireNavDataPhaseUpdater(turnData);
    }

    @Test
    void updateTraversedNewPortal() {
        empire1.addPortalTraversed(portal);
        updater.update();
        assertTrue(empire1.hasNavData(portal));
    }

    @Test
    void updateTraversedKnownPortal() {
        empire1.addNavData(portal);
        empire1.addPortalTraversed(portal);
        updater.update();
        assertTrue(empire1.hasNavData(portal));
    }

    @Test
    void updatePortalNotTraversed() {
        updater.update();
        assertFalse(empire1.hasNavData(portal));
    }
}