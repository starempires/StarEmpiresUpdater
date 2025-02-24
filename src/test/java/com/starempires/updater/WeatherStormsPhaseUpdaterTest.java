package com.starempires.updater;

import com.starempires.objects.Ship;
import com.starempires.objects.ShipCondition;
import com.starempires.objects.Storm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WeatherStormsPhaseUpdaterTest extends BasePhaseUpdaterTest {

    private WeatherStormsPhaseUpdater updater;

    @BeforeEach
    void setUp() {
        updater = new WeatherStormsPhaseUpdater(turnData);
    }

    @Test
    void update() {
        final Ship ship = createShip(fighterClass, ZERO_COORDINATE, "ship", empire);
        final Storm storm = createStorm("storm", ZERO_COORDINATE, 1);
        updater.update();;
        assertEquals(1, ship.getStormDamageAccrued());
        assertTrue(ship.hasCondition(ShipCondition.DAMAGED_BY_STORM));
    }

    @Test
    void updateZeroRating() {
        final Ship ship = createShip(fighterClass, ZERO_COORDINATE, "ship", empire);
        final Storm storm = createStorm("storm", ZERO_COORDINATE, 0);
        updater.update();;
        assertEquals(0, ship.getStormDamageAccrued());
        assertFalse(ship.hasCondition(ShipCondition.DAMAGED_BY_STORM));
    }

    @Test
    void updateStarbaseInRange() {
        final Ship ship = createShip(fighterClass, ZERO_COORDINATE, "ship", empire);
        final Ship starbase = createShip(starbaseClass, ZERO_COORDINATE, "starbase", empire);
        final Storm storm = createStorm("storm", ZERO_COORDINATE, 5);
        updater.update();;
        assertEquals(0, ship.getStormDamageAccrued());
        assertFalse(ship.hasCondition(ShipCondition.DAMAGED_BY_STORM));
        assertFalse(starbase.hasCondition(ShipCondition.DAMAGED_BY_STORM));
    }
}