package com.starempires.updater;

import com.starempires.objects.Ship;
import com.starempires.objects.ShipCondition;
import com.starempires.util.BaseTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ApplyCombatDamagePhaseUpdaterTest extends BaseTest {

    private ApplyCombatDamagePhaseUpdater updater;

    @BeforeEach
    void setUp() {
        updater = new ApplyCombatDamagePhaseUpdater(turnData);
    }

    @Test
    void update() {
        final int damage = 1;
        final Ship ship = createShip(fighterClass, ZERO_COORDINATE, "ship", empire1);
        ship.inflictCombatDamage(damage);
        updater.update();
        assertEquals(damage, ship.getCombatDamageAccrued());
        assertEquals(ship.getDp() - damage, ship.getDpRemaining());
        assertTrue(ship.hasCondition(ShipCondition.HIT_IN_COMBAT));
    }
}