package com.starempires.objects;

import com.starempires.util.BaseTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for Ship.java.
 * Each test covers one path of the method named by the test.
 * Additional tests are added where useful to exercise alternate paths.
 */
class ShipTest extends BaseTest {

    private static final ShipClass DEVICE_CLASS = ShipClass.builder().name("device").hullType(HullType.DEVICE).dp(1).build();
    private Empire otherEmpire;

    private Ship fighter;
    private Ship carrier;

    @BeforeEach
    void setUp() {
        // Owners
        otherEmpire = createEmpire("other");

        // Common ships
        fighter = createShip(fighterClass, ZERO_COORDINATE, "fighter", empire);
        carrier = createShip(carrierClass, ZERO_COORDINATE, "carrier", empire);
    }

    @Test
    void getCargoGroup() {
        turnData.load(fighter, carrier);
        final Set<Ship> group = carrier.getCargoGroup();
        assertTrue(group.contains(fighter));
        assertTrue(group.contains(carrier));
    }

    @Test
    void unloadCargo() {
        turnData.load(fighter, carrier);
        turnData.unload(fighter);
        assertNull(fighter.getCarrier());
        assertFalse(carrier.getCargo().contains(fighter));
        assertTrue(fighter.getConditions().contains(ShipCondition.UNLOADED_FROM_CARRIER));
        assertTrue(carrier.getConditions().contains(ShipCondition.UNLOADED_CARGO));
    }

    @Test
    void loadCargo() {
        turnData.load(fighter, carrier);
        assertEquals(carrier, fighter.getCarrier());
        assertTrue(carrier.getCargo().contains(fighter));
        assertTrue(carrier.hasCondition(ShipCondition.LOADED_CARGO));
    }

    @Test
    void isLoaded() {
        turnData.load(fighter, carrier);
        assertTrue(fighter.isLoaded());
        assertFalse(carrier.isLoaded());
    }

    @Test
    void hasLoadedCargo() {
        assertFalse(carrier.hasLoadedCargo());
        carrier.addCargo(fighter);
        assertTrue(carrier.hasLoadedCargo());
    }

    @Test
    void wasJustUnloaded() {
        turnData.load(fighter, carrier);
        assertFalse(fighter.wasJustUnloaded());
        fighter.unloadFromCarrier();
        assertTrue(fighter.wasJustUnloaded());
    }

    @Test
    void unloadFromCarrier() {
        turnData.load(fighter, carrier);
        assertFalse(fighter.hasCondition(ShipCondition.UNLOADED_FROM_CARRIER));
        fighter.unloadFromCarrier();
        assertTrue(fighter.hasCondition(ShipCondition.UNLOADED_FROM_CARRIER));
    }

    @Test
    void loadOntoCarrier() {
        fighter.loadOntoCarrier(carrier);
        assertEquals(carrier, fighter.getCarrier());
        assertTrue(fighter.hasCondition(ShipCondition.LOADED_ONTO_CARRIER));
    }

    @Test
    void deploy() {
        carrier.deploy();
        assertFalse(carrier.hasCondition(ShipCondition.DEPLOYED));

        final Ship device = createShip(DEVICE_CLASS, ZERO_COORDINATE, "device", empire);
        device.deploy();
        assertTrue(device.hasCondition(ShipCondition.DEPLOYED));
    }

    @Test
    void inflictCombatDamage() {
        carrier.inflictCombatDamage(1);
        assertTrue(carrier.hasReceivedCombatDamage());
        assertTrue(carrier.hasCondition(ShipCondition.HIT_IN_COMBAT));
    }

    @Test
    void inflictStormDamage() {
        carrier.inflictStormDamage(1);
        assertTrue(carrier.hasReceivedStormDamage());
        assertTrue(carrier.hasCondition(ShipCondition.DAMAGED_BY_STORM));
    }

    @Test
    void applyCombatDamageAccrued() {
        carrier.inflictCombatDamage(1);
        carrier.applyCombatDamageAccrued();
        assertEquals(carrierClass.getDp() - 1, carrier.getDpRemaining());

        carrier.inflictCombatDamage(carrierClass.getDp());
        carrier.applyCombatDamageAccrued();
        assertEquals(0, carrier.getDpRemaining());
        assertTrue(carrier.hasCondition(ShipCondition.DESTROYED_IN_COMBAT));
    }

    @Test
    void applyStormDamageAccrued() {
        carrier.inflictStormDamage(1);
        carrier.applyStormDamageAccrued();
        assertEquals(carrierClass.getDp() - 1, carrier.getDpRemaining());

        carrier.inflictStormDamage(carrierClass.getDp());
        carrier.applyStormDamageAccrued();
        assertEquals(0, carrier.getDpRemaining());
        assertTrue(carrier.hasCondition(ShipCondition.DESTROYED_BY_STORM));
    }

    @Test
    void destruct() {
        carrier.destruct();
        assertEquals(0, carrier.getDpRemaining());
        assertTrue(carrier.hasCondition(ShipCondition.SELF_DESTRUCTED));
    }

    @Test
    void getUnfiredGuns() {
        carrier.fireGuns(1);
        assertEquals(carrierClass.getGuns() - 1, carrier.getUnfiredGuns());
    }

    @Test
    void getOperationRating() {
        carrier.setDpRemaining(carrier.getDpRemaining()/2);
        assertEquals(0.707, carrier.getOperationRating(), 0.02);
    }

    @Test
    void getAvailableGuns() {
        carrier.setDpRemaining(carrier.getDpRemaining()/2);
        assertEquals(1, carrier.getAvailableGuns());
    }

    @Test
    void getAvailableEngines() {
        carrier.setDpRemaining(carrier.getDpRemaining()/2);
        assertEquals(1, carrier.getAvailableGuns());
    }

    @Test
    void getAvailableScan() {
        carrier.setDpRemaining(carrier.getDpRemaining()/2);
        assertEquals(1, carrier.getAvailableGuns());
    }

    @Test
    void fireGuns() {
        carrier.fireGuns(0);
        assertFalse(carrier.hasCondition(ShipCondition.FIRED_GUNS));
        assertEquals(2, carrier.getUnfiredGuns());

        carrier.fireGuns(1);
        assertTrue(carrier.hasCondition(ShipCondition.FIRED_GUNS));
        assertEquals(1, carrier.getUnfiredGuns());
    }

    @Test
    void isAlive() {
        assertTrue(carrier.isAlive());
        carrier.setDpRemaining(0);
        assertFalse(carrier.isAlive());
    }

    @Test
    void isDevice() {
        assertFalse(carrier.isDevice());
        final Ship device = createShip(DEVICE_CLASS, ZERO_COORDINATE, "device", empire);
        assertTrue(device.isDevice());
    }

    @Test
    void isMissile() {
        assertFalse(carrier.isMissile());
        final Ship missile = createShip(missileClass, ZERO_COORDINATE, "device", empire);
        assertTrue(missile.isMissile());
    }

    @Test
    void isOrbital() {
        assertFalse(carrier.isOrbital());
        final Ship starbase = createShip(starbaseClass, ZERO_COORDINATE, "device", empire);
        assertTrue(starbase.isOrbital());
    }

    @Test
    void isConqueringShip() {
        final Ship missile = createShip(missileClass, ZERO_COORDINATE, "missile", empire);
        assertFalse(missile.isConqueringShip());

        assertTrue(carrier.isConqueringShip());
        carrier.setDpRemaining(0);
        assertFalse(carrier.isConqueringShip());

        assertTrue(fighter.isConqueringShip());
        fighter.setDpRemaining(1);
        assertFalse(carrier.isConqueringShip());
    }

    @Test
    void isWing() {
        assertTrue(fighter.isWing());
        assertFalse(carrier.isWing());
    }

    @Test
    void getMaxRepairAmount() {
        assertEquals(0, carrier.getMaxRepairAmount());
        carrier.setDpRemaining(1);
        assertEquals(carrier.getDp() - 1, carrier.getMaxRepairAmount());
        carrier.setDpRemaining(-1);
        assertEquals(carrier.getDp(), carrier.getMaxRepairAmount());
    }

    @Test
    void repair() {
        carrier.setDpRemaining(1);
        carrier.repair(3);
        assertEquals(4, carrier.getDpRemaining());
        assertTrue(carrier.hasCondition(ShipCondition.REPAIRED));

        carrier.repair(100);
        assertEquals(carrier.getDp(), carrier.getDpRemaining());
    }

    @Test
    void autoRepairAliveShip() {
        carrier.setDpRemaining(1);
        int ar = carrier.autoRepair();
        assertEquals(carrier.getAr(), ar);
        assertTrue(carrier.hasCondition(ShipCondition.AUTO_REPAIRED));
    }

    @Test
    void autoRepairDeadShip() {
        carrier.setDpRemaining(0);
        int ar = carrier.autoRepair();
        assertEquals(0, ar);
        assertFalse(carrier.hasCondition(ShipCondition.AUTO_REPAIRED));
    }

    @Test
    void autoRepairUndamaged() {
        int ar = carrier.autoRepair();
        assertEquals(0, ar);
        assertFalse(carrier.hasCondition(ShipCondition.AUTO_REPAIRED));
    }

    @Test
    void moveTo() {
        carrier.moveTo(ONE_COORDINATE);
        assertEquals(ONE_COORDINATE, carrier.getCoordinate());
        assertTrue(carrier.hasCondition(ShipCondition.MOVED));
    }

    @Test
    void traverseTo() {
        carrier.traverseTo(ONE_COORDINATE);
        assertEquals(ONE_COORDINATE, carrier.getCoordinate());
        assertTrue(carrier.hasCondition(ShipCondition.TRAVERSED_WORMNET));
    }

    @Test
    void isSalvageable() {
        assertFalse(carrier.isSalvageable());
        // Destroyed in combat -> salvageable (not self-destructed and not a fired missile)
        carrier.destroy(ShipCondition.DESTROYED_IN_COMBAT);
        assertTrue(carrier.isSalvageable());
    }

    @Test
    void testIsSalvageableSelfDestructed() {
        carrier.destroy(ShipCondition.SELF_DESTRUCTED);
        assertFalse(carrier.isSalvageable());
    }

    @Test
    void testIsSalvageableFiredMissile() {
        final Ship missile = createShip(missileClass, ZERO_COORDINATE, "missile", empire);
        missile.destroy(ShipCondition.DESTROYED_IN_COMBAT);
        assertTrue(missile.isSalvageable());
        missile.fireGuns(missile.getGuns());
        assertFalse(missile.isSalvageable());
    }

    @Test
    void hasReceivedCombatDamage() {
        assertFalse(carrier.hasReceivedCombatDamage());
        carrier.inflictCombatDamage(1);
        assertTrue(carrier.hasReceivedCombatDamage());
    }

    @Test
    void hasReceivedStormDamage() {
        assertFalse(carrier.hasReceivedStormDamage());
        carrier.inflictStormDamage(1);
        assertTrue(carrier.hasReceivedStormDamage());
    }

    @Test
    void hasAccruedTotalDamageExceededRemainingDp() {
        assertFalse(carrier.hasAccruedTotalDamageExceededRemainingDp());
        carrier.inflictCombatDamage(20);
        assertTrue(carrier.hasAccruedTotalDamageExceededRemainingDp());
    }

    @Test
    void isRepairable() {
        assertFalse(carrier.isRepairable());
        carrier.setDpRemaining(1);
        assertTrue(carrier.isRepairable());
        carrier.setDpRemaining(0); // dead ships not repairable
        assertFalse(carrier.isRepairable());
    }

    @Test
    void isOneShot() {
        assertFalse(carrier.isOneShot());
        final Ship missile = createShip(missileClass, ZERO_COORDINATE, "missile", empire);
        assertTrue(missile.isOneShot());

        final Ship device = createShip(DEVICE_CLASS, ZERO_COORDINATE, "device", empire);
        assertTrue(device.isOneShot());
    }

    @Test
    void isTransponderSetPublic() {
        carrier.setPublicTransponder(true);
        assertTrue(carrier.isTransponderSet(otherEmpire));
    }

    @Test
    void isTransponderNotPublic() {
        assertFalse(carrier.isTransponderSet(otherEmpire));
        carrier.addTransponder(otherEmpire);
        assertTrue(carrier.isTransponderSet(otherEmpire));
    }

    @Test
    void getEmptyRacks() {
        assertEquals(carrier.getRacks(), carrier.getEmptyRacks());
        carrier.addCargo(fighter);
        assertEquals(carrier.getRacks() - fighter.getTonnage(), carrier.getEmptyRacks());
    }

    @Test
    void canLoadCargo() {
        assertTrue(carrier.canLoadCargo(fighter));
        assertFalse(carrier.canLoadCargo(carrier));
    }

    @Test
    void toggleTransponder() {
        carrier.toggleTransponder(false);
        assertFalse(carrier.isPublicTransponder());
        assertFalse(carrier.hasCondition(ShipCondition.TOGGLED_TRANSPONDER));

        carrier.toggleTransponder(true);
        assertTrue(carrier.isPublicTransponder());
        assertTrue(carrier.hasCondition(ShipCondition.TOGGLED_TRANSPONDER));
    }

    @Test
    void hasUnfiredGuns() {
        assertTrue(carrier.hasUnfiredGuns());
        carrier.fireGuns(carrier.getGuns());
        assertFalse(carrier.hasUnfiredGuns());
    }

    @Test
    void isVisibleToEmpireOwned() {
        carrier.isVisibleToEmpire(empire);
    }

    @Test
    void isVisibleToEmpireVisibleStatus() {
        empire.addScan(ZERO_COORDINATE, ScanStatus.VISIBLE);
        final Ship otherCarrier = createShip(carrierClass, ZERO_COORDINATE, "othercarrier", otherEmpire);
        final Ship otherCargo = createShip(fighterClass, ZERO_COORDINATE, "otherfighter", otherEmpire);
        assertTrue(otherCargo.isVisibleToEmpire(empire));
        assertTrue(otherCarrier.isVisibleToEmpire(empire));
        // loaded cargo should not be visible
        turnData.load(otherCargo, otherCarrier);
        assertFalse(otherCargo.isVisibleToEmpire(empire));
        assertTrue(otherCarrier.isVisibleToEmpire(empire));
    }

    @Test
    void isVisibleToEmpireScannedStatus() {
        empire.addScan(ZERO_COORDINATE, ScanStatus.SCANNED);
        final Ship otherCarrier = createShip(carrierClass, ZERO_COORDINATE, "othercarrier", otherEmpire);
        final Ship otherCargo = createShip(fighterClass, ZERO_COORDINATE, "otherfighter", otherEmpire);
        otherCargo.addTransponder(empire);
        assertTrue(otherCargo.isVisibleToEmpire(empire));
        assertFalse(otherCarrier.isVisibleToEmpire(empire));
    }
}