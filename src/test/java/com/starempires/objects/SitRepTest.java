package com.starempires.objects;

import com.starempires.util.BaseTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SitRepTest extends BaseTest {

    SitRep sitRep;

    @BeforeEach
    public void before() {
        sitRep = new SitRep(empire1, ZERO_COORDINATE);
    }

    @Test
    public void testAddNonConqueringShip() {
        final Ship missile = createShip(missileClass, ZERO_COORDINATE, "missile", empire1);
        sitRep.add(missile);
        assertEquals(0, sitRep.getFriendlyGuns());
        assertEquals(0, sitRep.getFriendlyDp());
        assertEquals(0, sitRep.getEnemyGuns());
        assertEquals(0, sitRep.getEnemyDp());
    }

    @Test
    public void testAddFriendlyShip() {
        final Ship fighter = createShip(fighterClass, ZERO_COORDINATE, "fighter", empire1);
        sitRep.add(fighter);
        assertEquals(fighter.getGuns(), sitRep.getFriendlyGuns());
        assertEquals(fighter.getDp(), sitRep.getFriendlyDp());
        assertEquals(0, sitRep.getEnemyGuns());
        assertEquals(0, sitRep.getEnemyDp());
    }

    @Test
    public void testAddEnemyShip() {
        final Ship fighter = createShip(fighterClass, ZERO_COORDINATE, "fighter", empire2);
        sitRep.add(fighter);
        assertEquals(fighter.getGuns(), sitRep.getEnemyGuns());
        assertEquals(fighter.getDp(), sitRep.getEnemyDp());
        assertEquals(0, sitRep.getFriendlyGuns());
        assertEquals(0, sitRep.getFriendlyDp());
    }

    @Test
    public void getDefensiveRatio() {
        final Ship fighter = createShip(fighterClass, ZERO_COORDINATE, "fighter", empire1);
        final Ship frigate = createShip(frigateClass, ZERO_COORDINATE, "frigate", empire2);
        sitRep.add(fighter);
        sitRep.add(frigate);
        assertEquals(0.25, sitRep.getDefensiveRatio());
    }

    @Test
    void isEnemyToFriendlyRatioExceeded() {
        final Ship carrier = createShip(carrierClass, ZERO_COORDINATE, "carrier", empire1);
        final Ship frigate = createShip(frigateClass, ZERO_COORDINATE, "frigate", empire2);
        final Ship fighter = createShip(fighterClass, ZERO_COORDINATE, "fighter", empire2);
        sitRep.add(carrier);
        sitRep.add(frigate);
        assertFalse(sitRep.isEnemyToFriendlyRatioExceeded(2.0));
        sitRep.add(fighter);
        assertTrue(sitRep.isEnemyToFriendlyRatioExceeded(2.0));
    }
}