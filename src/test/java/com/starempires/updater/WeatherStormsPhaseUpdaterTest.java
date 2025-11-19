package com.starempires.updater;

import com.starempires.objects.Ship;
import com.starempires.objects.ShipCondition;
import com.starempires.util.BaseTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WeatherStormsPhaseUpdaterTest extends BaseTest {

    private WeatherStormsPhaseUpdater updater;

    @BeforeEach
    void setUp() {
        updater = new WeatherStormsPhaseUpdater(turnData);
    }

    @Test
    void update() {
        final Ship ship = createShip(fighterClass, ZERO_COORDINATE, "ship", empire1);
        updater.update();;
        assertEquals(1, ship.getStormDamageAccrued());
        assertTrue(ship.hasCondition(ShipCondition.DAMAGED_BY_STORM));
    }

    @Test
    void updateZeroIntensity() {
        final Ship ship = createShip(fighterClass, ONE_COORDINATE, "ship", empire1);
        createStorm("storm", ONE_COORDINATE, 0);
        updater.update();;
        assertEquals(0, ship.getStormDamageAccrued());
        assertFalse(ship.hasCondition(ShipCondition.DAMAGED_BY_STORM));
    }

    @Test
    void updateStarbaseInRange() {
        final Ship ship = createShip(fighterClass, ZERO_COORDINATE, "ship", empire1);
        final Ship starbase = createShip(starbaseClass, ZERO_COORDINATE, "starbase", empire1);
        updater.update();
        assertEquals(0, ship.getStormDamageAccrued());
        assertFalse(ship.hasCondition(ShipCondition.DAMAGED_BY_STORM));
        assertFalse(starbase.hasCondition(ShipCondition.DAMAGED_BY_STORM));
    }

    @Test
    void updateIonShieldDeployed() {
        final Ship ship = createShip(fighterClass, ZERO_COORDINATE, "ship", empire1);
        final Ship shield = createShip(shieldClass, ZERO_COORDINATE, "shield", empire1);
        turnData.deploy(shield);
        updater.update();;
        assertEquals(0, ship.getStormDamageAccrued());
        assertFalse(ship.hasCondition(ShipCondition.DAMAGED_BY_STORM));
    }
}