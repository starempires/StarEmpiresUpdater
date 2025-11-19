package com.starempires.updater;

import com.starempires.objects.Ship;
import com.starempires.objects.ShipCondition;
import com.starempires.util.BaseTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ApplyStormDamagePhaseUpdaterTest extends BaseTest {

    private ApplyStormDamagePhaseUpdater updater;

    @BeforeEach
    void setUp() {
        updater = new ApplyStormDamagePhaseUpdater(turnData);
    }

    @Test
    void update() {
        final int damage = 2;
        final Ship ship = createShip(frigateClass, ZERO_COORDINATE, "frigate", empire1);
        ship.inflictStormDamage(damage);
        updater.update();
        assertEquals(frigateClass.getDp() - damage, ship.getDpRemaining());
        assertTrue(ship.hasCondition(ShipCondition.DAMAGED_BY_STORM));
    }
}