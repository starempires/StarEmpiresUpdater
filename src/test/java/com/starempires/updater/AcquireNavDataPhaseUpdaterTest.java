package com.starempires.updater;

import com.starempires.objects.Portal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AcquireNavDataPhaseUpdaterTest extends BasePhaseUpdaterTest {

    private AcquireNavDataPhaseUpdater updater;

    @BeforeEach
    void setUp() {
        updater = new AcquireNavDataPhaseUpdater(turnData);
    }

    @Test
    void updateTraversedNewPortal() {
        final Portal portal = createPortal("portal", ZERO_COORDINATE, false);
        empire.addPortalTraversed(portal);
        updater.update();
        assertTrue(empire.hasNavData(portal));
    }

    @Test
    void updateTraversedKnownPortal() {
        final Portal portal = createPortal("portal", ZERO_COORDINATE, false);
        empire.addNavData(portal);
        empire.addPortalTraversed(portal);
        updater.update();
        assertTrue(empire.hasNavData(portal));
    }

    @Test
    void updatePortalNotTraversed() {
        final Portal portal = createPortal("portal", ZERO_COORDINATE, false);
        updater.update();
        assertFalse(empire.hasNavData(portal));
    }
}