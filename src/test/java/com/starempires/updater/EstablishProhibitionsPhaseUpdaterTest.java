package com.starempires.updater;

import com.starempires.objects.Empire;
import com.starempires.objects.Ship;
import com.starempires.objects.World;
import com.starempires.util.BaseTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EstablishProhibitionsPhaseUpdaterTest extends BaseTest {

    private EstablishProhibitionsPhaseUpdater updater;

    @BeforeEach
    void setUp() {
        updater = new EstablishProhibitionsPhaseUpdater(turnData);
    }

    @Test
    void updateNoProhibition() {
        final Empire empire1 = createEmpire("empire1");
        final Empire empire2 = createEmpire("empire2");
        final World world = createWorld("world", ZERO_COORDINATE, 10);
        world.setOwner(empire1);
        final Ship ship1 =  createShip(fighterClass, ZERO_COORDINATE, "ship1", empire1);
        final Ship ship2 =  createShip(fighterClass, ZERO_COORDINATE, "ship2", empire2);
        updater.update();
        assertFalse(world.isInterdicted());
        assertFalse(world.isBlockaded());
    }

    @Test
    void updateBlockaded() {
        final Empire empire1 = createEmpire("empire1");
        final Empire empire2 = createEmpire("empire2");
        final World world = createWorld("world", ZERO_COORDINATE, 10);
        world.setOwner(empire1);
        final Ship ship1a =  createShip(probeClass, ZERO_COORDINATE, "ship1a", empire1);
        final Ship ship1b =  createShip(probeClass, ZERO_COORDINATE, "ship1b", empire1);
        final Ship ship2 =  createShip(fighterClass, ZERO_COORDINATE, "ship2", empire2);
        updater.update();
        assertTrue(world.isBlockaded());
        assertFalse(world.isInterdicted());
    }

    @Test
    void updateBlockadedWithDefendingMissiles() {
        final Empire empire1 = createEmpire("empire1");
        final Empire empire2 = createEmpire("empire2");
        final World world = createWorld("world", ZERO_COORDINATE, 10);
        world.setOwner(empire1);
        final Ship ship1a =  createShip(probeClass, ZERO_COORDINATE, "ship1a", empire1);
        final Ship ship1b =  createShip(probeClass, ZERO_COORDINATE, "ship1b", empire1);
        final Ship m1 =  createShip(missileClass, ZERO_COORDINATE, "m1", empire1);
        final Ship m2 =  createShip(missileClass, ZERO_COORDINATE, "m2", empire1);
        final Ship m3 =  createShip(missileClass, ZERO_COORDINATE, "m3", empire1);
        final Ship ship2 =  createShip(fighterClass, ZERO_COORDINATE, "ship2", empire2);
        updater.update();
        assertTrue(world.isBlockaded());
        assertFalse(world.isInterdicted());
    }

    @Test
    void updateInterdicted() {
        final Empire empire1 = createEmpire("empire1");
        final Empire empire2 = createEmpire("empire2");
        final World world = createWorld("world", ZERO_COORDINATE, 10);
        world.setOwner(empire1);
        final Ship ship1 =  createShip(probeClass, ZERO_COORDINATE, "ship1", empire1);
        final Ship ship2 =  createShip(fighterClass, ZERO_COORDINATE, "ship2", empire2);
        updater.update();
        assertTrue(world.isInterdicted());
        assertTrue(world.isBlockaded());
    }

    @Test
    void updateUnownedWorld() {
        final Empire empire1 = createEmpire("empire1");
        final Empire empire2 = createEmpire("empire2");
        final World world = createWorld("world", ZERO_COORDINATE, 10);
        final Ship ship1 =  createShip(probeClass, ZERO_COORDINATE, "ship1", empire1);
        final Ship ship2 =  createShip(fighterClass, ZERO_COORDINATE, "ship2", empire2);
        updater.update();
        assertFalse(world.isInterdicted());
        assertFalse(world.isBlockaded());
    }

    @Test
    void updateHomeworld() {
        final Empire empire1 = createEmpire("empire1");
        final Empire empire2 = createEmpire("empire2");
        final World world = createWorld("world", ZERO_COORDINATE, 10);
        world.setOwner(empire1);
        world.setHomeworld(true);
        final Ship ship1 =  createShip(probeClass, ZERO_COORDINATE, "ship1", empire1);
        final Ship ship2 =  createShip(fighterClass, ZERO_COORDINATE, "ship2", empire2);
        updater.update();
        assertTrue(world.isBlockaded());
        assertFalse(world.isInterdicted());
    }

    @Test
    void updateStarbase() {
        final Empire empire1 = createEmpire("empire1");
        final Empire empire2 = createEmpire("empire2");
        final World world = createWorld("world", ZERO_COORDINATE, 10);
        world.setOwner(empire1);
        final Ship ship1 =  createShip(starbaseClass, ZERO_COORDINATE, "ship1", empire1);
        ship1.inflictCombatDamage(starbaseClass.getDp() - 1);
        ship1.applyCombatDamageAccrued();
        final Ship ship2 =  createShip(fighterClass, ZERO_COORDINATE, "ship2", empire2);
        updater.update();
        assertFalse(world.isBlockaded());
        assertFalse(world.isInterdicted());
    }
}