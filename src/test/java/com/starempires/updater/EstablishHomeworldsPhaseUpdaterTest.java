package com.starempires.updater;

import com.starempires.objects.World;
import com.starempires.util.BaseTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EstablishHomeworldsPhaseUpdaterTest extends BaseTest {

    private EstablishHomeworldsPhaseUpdater updater;

    private World originalHomeworld;
    private World newHomeworld;

    @BeforeEach
    void setUp() {
        world.setOwner(null);
        updater = new EstablishHomeworldsPhaseUpdater(turnData);
        originalHomeworld = createWorld("original", ZERO_COORDINATE, 12);
        turnData.setHomeworld(empire1, originalHomeworld);
        newHomeworld = createWorld("new", ONE_COORDINATE, 12);
    }

    @Test
    void updateEstablishNewHomeworld() {
        originalHomeworld.setOwner(empire2);
        newHomeworld.setOwner(empire1);
        updater.update();
        assertTrue(newHomeworld.isHomeworld());
        assertEquals(2.0, newHomeworld.getProductionMultiplier());
        assertFalse(originalHomeworld.isHomeworld());
    }

    @Test
    void updateEstablishNewHomeworldNoWorlds() {
        originalHomeworld.setOwner(empire2);
        updater.update();
        assertFalse(originalHomeworld.isHomeworld());
        assertFalse(newHomeworld.isHomeworld());
        assertNull(empire1.getHomeworld());
    }
}