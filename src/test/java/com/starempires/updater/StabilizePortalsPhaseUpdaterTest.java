package com.starempires.updater;

import com.starempires.objects.Portal;
import com.starempires.util.BaseTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;

class StabilizePortalsPhaseUpdaterTest extends BaseTest {

    private StabilizePortalsPhaseUpdater updater;

    @BeforeEach
    void setUp() {
        updater = new StabilizePortalsPhaseUpdater(turnData);
    }

    @Test
    void update() {
        final Portal testPortal = createPortal("testportal", ZERO_COORDINATE, true);
        turnData.addPortal(testPortal);
        updater.update();
        assertFalse(testPortal.isCollapsed());
    }
}