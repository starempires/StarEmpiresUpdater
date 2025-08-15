package com.starempires.updater;

import com.starempires.objects.Ship;
import com.starempires.util.BaseTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RemoveDestroyedShipsPhaseUpdaterTest extends BaseTest {

    private RemoveDestroyedShipsIPhaseUpdater updater;

    @BeforeEach
    void setUp() {
        updater = new RemoveDestroyedShipsIPhaseUpdater(turnData);
    }

    @Test
    void update() {
        final String handle = "ship";
        final Ship ship = createShip(probeClass, ZERO_COORDINATE, handle, empire);
        ship.inflictCombatDamage(ship.getDpRemaining());
        ship.applyCombatDamageAccrued();
        updater.update();
        assertFalse(ship.isAlive());
        assertNull(empire.getShip(handle));
        assertTrue(turnData.getPossibleSalvages().contains(ship));
    }
}