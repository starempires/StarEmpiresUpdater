package com.starempires.objects;

import com.starempires.util.BaseTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
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

    private Ship fighter;
    private Ship carrier;
    private Ship missile;

    @BeforeEach
    void setUp() {
        // Common ships
        fighter = createShip(fighterClass, ZERO_COORDINATE, "fighter", empire1);
        carrier = createShip(carrierClass, ZERO_COORDINATE, "carrier", empire1);
        missile = createShip(missileClass, ZERO_COORDINATE, "missile", empire1);
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

        final Ship device = createShip(DEVICE_CLASS, ZERO_COORDINATE, "device", empire1);
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
        final Ship device = createShip(DEVICE_CLASS, ZERO_COORDINATE, "device", empire1);
        assertTrue(device.isDevice());
    }

    @Test
    void isMissile() {
        assertFalse(carrier.isMissile());
        assertTrue(missile.isMissile());
    }

    @Test
    void isOrbital() {
        assertFalse(carrier.isOrbital());
        final Ship starbase = createShip(starbaseClass, ZERO_COORDINATE, "device", empire1);
        assertTrue(starbase.isOrbital());
    }

    @Test
    void isConqueringShipOneShot() {
        assertFalse(missile.isConqueringShip());
    }

    @Test
    void isConqueringShipDestroyed() {
        assertTrue(carrier.isConqueringShip());
        carrier.setDpRemaining(0);
        assertFalse(carrier.isConqueringShip());
    }

    @Test
    void isConqueringShipNoGuns() {
        final ShipClass noGunsClass = ShipClass.builder().name("noguns").hullType(HullType.SCOUT).dp(1).guns(0).build();
        final Ship noGuns = createShip(noGunsClass, ZERO_COORDINATE, "noguns", empire1);
        assertFalse(noGuns.isConqueringShip());
    }

    @Test
    void isConqueringShipLoaded() {
        turnData.load(fighter, carrier);
        assertFalse(fighter.isConqueringShip());
    }

    @Test
    void isConqueringShipSuuccess() {
        assertTrue(fighter.isConqueringShip());
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
        carrier.repair(3, ShipCondition.REPAIRED);
        assertEquals(4, carrier.getDpRemaining());
        assertTrue(carrier.hasCondition(ShipCondition.REPAIRED));

        carrier.repair(100, ShipCondition.REPAIRED);
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
        assertTrue(missile.isOneShot());

        final Ship device = createShip(DEVICE_CLASS, ZERO_COORDINATE, "device", empire1);
        assertTrue(device.isOneShot());
    }

    @Test
    void isTransponderSetPublic() {
        carrier.setPublicTransponder(true);
        assertTrue(carrier.isTransponderSet(empire2));
    }

    @Test
    void isTransponderNotPublic() {
        assertFalse(carrier.isTransponderSet(empire2));
        carrier.addTransponder(empire2);
        assertTrue(carrier.isTransponderSet(empire2));
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
        carrier.isVisibleToEmpire(empire1);
    }

    @Test
    void isVisibleToEmpireVisibleStatus() {
        empire1.addScan(ZERO_COORDINATE, ScanStatus.VISIBLE);
        final Ship otherCarrier = createShip(carrierClass, ZERO_COORDINATE, "othercarrier", empire2);
        final Ship otherCargo = createShip(fighterClass, ZERO_COORDINATE, "otherfighter", empire2);
        assertTrue(otherCargo.isVisibleToEmpire(empire1));
        assertTrue(otherCarrier.isVisibleToEmpire(empire1));
        // loaded cargo should not be visible
        turnData.load(otherCargo, otherCarrier);
        assertFalse(otherCargo.isVisibleToEmpire(empire1));
        assertTrue(otherCarrier.isVisibleToEmpire(empire1));
    }

    @Test
    void isVisibleToEmpireScannedStatus() {
        empire1.addScan(ZERO_COORDINATE, ScanStatus.SCANNED);
        final Ship otherCarrier = createShip(carrierClass, ZERO_COORDINATE, "othercarrier", empire2);
        final Ship otherCargo = createShip(fighterClass, ZERO_COORDINATE, "otherfighter", empire2);
        otherCargo.addTransponder(empire1);
        assertTrue(otherCargo.isVisibleToEmpire(empire1));
        assertFalse(otherCarrier.isVisibleToEmpire(empire1));
    }

    @Test
    void testIsTargetableDestroyed() {
        fighter.setDpRemaining(0);
        assertFalse(fighter.isTargetable());
        carrier.destruct();
        assertFalse(carrier.isTargetable());
    }

    @Test
    void testIsTargetableLoaded() {
        fighter.setCarrier(carrier);
        assertFalse(fighter.isTargetable());
    }

    @Test
    void testMissile() {
        assertTrue(missile.isTargetable());
        missile.addCondition(ShipCondition.UNLOADED_FROM_CARRIER);
        assertFalse(missile.isTargetable());
    }

    @Test
    void testTargetable() {
        assertTrue(carrier.isTargetable());
    }

    @Test
    void getAbbreviatedConditions() {
        carrier.addCondition(ShipCondition.HIT_IN_COMBAT);
        carrier.addCondition(ShipCondition.MOVED);
        carrier.addCondition(ShipCondition.TRAVERSED_WORMNET);
        final List<String> expected = List.of(ShipCondition.HIT_IN_COMBAT.getAbbreviation(),
                ShipCondition.MOVED.getAbbreviation(),
                ShipCondition.TRAVERSED_WORMNET.getAbbreviation());
        assertEquals(expected, carrier.getAbbreviatedConditions());
    }
}