package com.starempires.updater;

import com.starempires.objects.Ship;
import com.starempires.objects.ShipCondition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AutoRepairShipsPhaseUpdaterTest extends BasePhaseUpdaterTest {

    private AutoRepairShipsPhaseUpdater updater;

    @BeforeEach
    void setUp() {
        updater = new AutoRepairShipsPhaseUpdater(turnData);
    }

    @Test
    void update() {
        final int damage = 3;
        final Ship ship = createShip(fighterClass, ZERO_COORDINATE, "ship", empire);
        ship.inflictCombatDamage(damage);
        ship.applyCombatDamageAccrued();
        updater.update();
        assertEquals(ship.getDp() - damage + ship.getAr(), ship.getDpRemaining());
        assertTrue(ship.hasCondition(ShipCondition.AUTO_REPAIRED));
    }

    @Test
    void updateNothingToRepair() {
        final Ship ship = createShip(fighterClass, ZERO_COORDINATE, "ship", empire);
        updater.update();
        assertEquals(ship.getDp(), ship.getDpRemaining());
        assertFalse(ship.hasCondition(ShipCondition.AUTO_REPAIRED));
    }
}