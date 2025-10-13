package com.starempires.updater;

import com.starempires.objects.Empire;
import com.starempires.objects.Ship;
import com.starempires.util.BaseTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DetermineOwnershipPhaseUpdaterTest extends BaseTest {

    private DetermineOwnershipIPhaseUpdater updater;
    private Empire empire1;
    private Empire empire2;

    @BeforeEach
    void setUp() {
        updater = new DetermineOwnershipIPhaseUpdater(turnData);
        empire1 = createEmpire("empire1");
        empire2 = createEmpire("empire2");
    }

    @Test
    void updateNoOwnerNoEmpires() {
        world.setOwner(null);
        final Ship ship1 = createShip(fighterClass, ZERO_COORDINATE, "ship1", empire1);
        final Ship ship2 = createShip(fighterClass, ZERO_COORDINATE, "ship2", empire2);
        updater.update();
        assertFalse(world.isOwned());
    }

    @Test
    void updateNoOwnerOneEmpire() {
        world.setOwner(null);
        final Ship ship = createShip(fighterClass, ZERO_COORDINATE, "name", empire1);
        updater.update();
        assertTrue(world.isOwnedBy(empire1));
    }

    @Test
    void updateNoOwnerTwoEmpires() {
        world.setOwner(null);
        updater.update();
        assertFalse(world.isOwned());
    }

    @Test
    void updateOwnerNoEmpires() {
        updater.update();
        assertTrue(world.isOwnedBy(empire1));
    }

    @Test
    void updateOwnerSameEmpire() {
        final Ship ship = createShip(fighterClass, ZERO_COORDINATE, "name", empire1);
        updater.update();
        assertTrue(world.isOwnedBy(empire1));
    }

    @Test
    void updateOwnerDifferentEmpire() {
        final Ship ship = createShip(fighterClass, ZERO_COORDINATE, "name", empire2);
        updater.update();
        assertTrue(world.isOwnedBy(empire2));
    }

    @Test
    void updateOwnerTwoEmpiresOneSame() {
        final Ship ship1 = createShip(fighterClass, ZERO_COORDINATE, "ship1", empire1);
        final Ship ship2 = createShip(fighterClass, ZERO_COORDINATE, "ship2", empire2);
        updater.update();
        assertTrue(world.isOwnedBy(empire1));
    }

    @Test
    void updateOwnerTwoEmpiresDifferent() {
        final Empire empire3 = createEmpire("empire3");
        world.setOwner(empire3);

        final Ship ship1 = createShip(fighterClass, ZERO_COORDINATE, "ship1", empire1);
        final Ship ship2 = createShip(fighterClass, ZERO_COORDINATE, "ship2", empire2);
        updater.update();
        assertFalse(world.isOwned());
    }
}