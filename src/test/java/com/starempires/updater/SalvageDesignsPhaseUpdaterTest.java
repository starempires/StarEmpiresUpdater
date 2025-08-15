package com.starempires.updater;

import com.starempires.objects.Empire;
import com.starempires.objects.Ship;
import com.starempires.objects.ShipClass;
import com.starempires.objects.ShipCondition;
import com.starempires.util.BaseTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SalvageDesignsPhaseUpdaterTest extends BaseTest {

    private SalvageDesignsPhaseUpdater updater;
    private static final ShipClass NEW_CLASS = ShipClass.builder()
            .name("NewClass")
            .build();

    @BeforeEach
    void setUp() {
        updater = new SalvageDesignsPhaseUpdater(turnData);
    }

    @Test
    void updateSalvage() {
        final Empire empire1 = createEmpire("empire1");
        final Empire empire2 = createEmpire("empire2");
        final Empire empire3 = createEmpire("empire3");
        final Ship newShip = createShip(NEW_CLASS, ZERO_COORDINATE, "destroyed", empire1);
        newShip.destroy(ShipCondition.DESTROYED_IN_COMBAT);
        turnData.removeDestroyedShips(List.of(newShip));
        final Ship probe = createShip(probeClass, ZERO_COORDINATE, "probe", empire2);
        final Ship fighter = createShip(fighterClass, ZERO_COORDINATE, "fighter", empire3);
        updater.update();
        assertTrue(empire3.getKnownShipClasses().contains(NEW_CLASS));
        assertFalse(empire2.getKnownShipClasses().contains(NEW_CLASS));
    }

    @Test
    void updateSalvageKnownClass() {
        final Empire empire1 = createEmpire("empire1");
        final Empire empire2 = createEmpire("empire2");
        final Empire empire3 = createEmpire("empire3");
        empire3.addKnownShipClass(NEW_CLASS);
        final Ship newShip = createShip(NEW_CLASS, ZERO_COORDINATE, "destroyed", empire1);
        newShip.destroy(ShipCondition.DESTROYED_IN_COMBAT);
        turnData.removeDestroyedShips(List.of(newShip));
        final Ship probe = createShip(probeClass, ZERO_COORDINATE, "probe", empire2);
        final Ship fighter = createShip(fighterClass, ZERO_COORDINATE, "fighter", empire3);
        updater.update();
        assertTrue(empire3.getKnownShipClasses().contains(NEW_CLASS));
        assertFalse(empire2.getKnownShipClasses().contains(NEW_CLASS));
    }

    @Test
    void updateSalvageNoOneWinner() {
        final Empire empire1 = createEmpire("empire1");
        final Empire empire2 = createEmpire("empire2");
        final Empire empire3 = createEmpire("empire3");
        final Ship newShip = createShip(NEW_CLASS, ZERO_COORDINATE, "destroyed", empire1);
        newShip.destroy(ShipCondition.DESTROYED_IN_COMBAT);
        turnData.removeDestroyedShips(List.of(newShip));
        final Ship e2fighter = createShip(fighterClass, ZERO_COORDINATE, "e2fighter", empire2);
        final Ship e3fighter = createShip(fighterClass, ZERO_COORDINATE, "e3fighter", empire3);
        updater.update();
        assertFalse(empire3.getKnownShipClasses().contains(NEW_CLASS));
        assertFalse(empire2.getKnownShipClasses().contains(NEW_CLASS));
    }

    @Test
    void updateSalvageInsufficientGuns() {
        final Empire empire1 = createEmpire("empire1");
        final Empire empire2 = createEmpire("empire2");
        final Empire empire3 = createEmpire("empire3");
        final Ship newShip = createShip(NEW_CLASS, ZERO_COORDINATE, "destroyed", empire1);
        newShip.destroy(ShipCondition.DESTROYED_IN_COMBAT);
        turnData.removeDestroyedShips(List.of(newShip));
        final Ship e1fighter = createShip(fighterClass, ZERO_COORDINATE, "e1fighter", empire1);
        final Ship e2probe = createShip(probeClass, ZERO_COORDINATE, "e2probe", empire2);
        final Ship e3fighter = createShip(fighterClass, ZERO_COORDINATE, "e3fighter", empire3);
        updater.update();
        assertFalse(empire3.getKnownShipClasses().contains(NEW_CLASS));
        assertFalse(empire2.getKnownShipClasses().contains(NEW_CLASS));
    }
}