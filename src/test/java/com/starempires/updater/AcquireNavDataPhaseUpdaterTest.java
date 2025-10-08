package com.starempires.updater;

import com.starempires.objects.Portal;
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
        final Portal portal = createPortal("portal", ZERO_COORDINATE, false);
        empire1.addPortalTraversed(portal);
        updater.update();
        assertTrue(empire1.hasNavData(portal));
    }

    @Test
    void updateTraversedKnownPortal() {
        final Portal portal = createPortal("portal", ZERO_COORDINATE, false);
        empire1.addNavData(portal);
        empire1.addPortalTraversed(portal);
        updater.update();
        assertTrue(empire1.hasNavData(portal));
    }

    @Test
    void updatePortalNotTraversed() {
        final Portal portal = createPortal("portal", ZERO_COORDINATE, false);
        updater.update();
        assertFalse(empire1.hasNavData(portal));
    }
}