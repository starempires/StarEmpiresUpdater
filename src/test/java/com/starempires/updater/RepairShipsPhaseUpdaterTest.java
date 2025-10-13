package com.starempires.updater;

import com.google.common.collect.Lists;
import com.starempires.objects.Prohibition;
import com.starempires.objects.Ship;
import com.starempires.objects.ShipCondition;
import com.starempires.orders.OrderType;
import com.starempires.orders.RepairOrder;
import com.starempires.util.BaseTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RepairShipsPhaseUpdaterTest extends BaseTest {

    private RepairShipsPhaseUpdater updater;
    private Ship ship;
    private RepairOrder order;

    @BeforeEach
    void setUp() {
        updater = new RepairShipsPhaseUpdater(turnData);
        ship = createShip(fighterClass, ZERO_COORDINATE, "ship", empire1);
        order = RepairOrder.builder()
                .empire(empire1)
                .orderType(OrderType.REPAIR)
                .parameters("ship 2 world")
                .ships(Lists.newArrayList(ship))
                .dpToRepair(2)
                .worlds(Lists.newArrayList(world))
                .build();
    }

    @Test
    void updateRepairSuccess() {
        ship.inflictCombatDamage(3);
        ship.applyCombatDamageAccrued();
        turnData.addOrder(order);
        updater.update();
        assertEquals(4, ship.getDpRemaining());
        assertTrue(ship.hasCondition(ShipCondition.REPAIRED));
        assertEquals(11, world.getStockpile());
    }

    @Test
    void updateRepairDestroyedShip() {
        ship.destroy(ShipCondition.DESTROYED_IN_COMBAT);
        turnData.addOrder(order);
        updater.update();
        assertEquals(0, ship.getDpRemaining());
        assertFalse(ship.isAlive());
        assertFalse(ship.hasCondition(ShipCondition.REPAIRED));
        assertEquals(12, world.getStockpile());
    }

    @Test
    void updateRepairNoRepairsNeeded() {
        turnData.addOrder(order);
        updater.update();
        assertEquals(ship.getDp(), ship.getDpRemaining());
        assertFalse(ship.hasCondition(ShipCondition.REPAIRED));
        assertEquals(12, world.getStockpile());
    }

    @Test
    void updateRepairWorldInterdicted() {
        ship.inflictCombatDamage(3);
        ship.applyCombatDamageAccrued();
        world.setProhibition(Prohibition.INTERDICTED);
        turnData.addOrder(order);
        updater.update();
        assertEquals(2, ship.getDpRemaining());
        assertFalse(ship.hasCondition(ShipCondition.REPAIRED));
        assertEquals(12, world.getStockpile());
    }

    @Test
    void updateRepairWorldBlockadedDifferentSector() {
        ship.inflictCombatDamage(3);
        ship.applyCombatDamageAccrued();
        ship.moveTo(ONE_COORDINATE);
        world.setProhibition(Prohibition.BLOCKADED);
        turnData.addOrder(order);
        updater.update();
        assertEquals(2, ship.getDpRemaining());
        assertFalse(ship.hasCondition(ShipCondition.REPAIRED));
        assertEquals(12, world.getStockpile());
    }

    @Test
    void updateRepairWorldBlockadedSameSector() {
        ship.inflictCombatDamage(3);
        ship.applyCombatDamageAccrued();
        world.setProhibition(Prohibition.BLOCKADED);

        turnData.addOrder(order);
        updater.update();
        assertEquals(4, ship.getDpRemaining());
        assertTrue(ship.hasCondition(ShipCondition.REPAIRED));
        assertEquals(11, world.getStockpile());
    }

    @Test
    void updateRepairWorldNoStockpile() {
        ship.inflictCombatDamage(3);
        ship.applyCombatDamageAccrued();
        world.setStockpile(0);
        turnData.addOrder(order);
        updater.update();
        assertEquals(2, ship.getDpRemaining());
        assertFalse(ship.hasCondition(ShipCondition.REPAIRED));
        assertEquals(0, world.getStockpile());
    }

    @Test
    void updateRepairDifferentEmpire() {
        ship.inflictCombatDamage(3);
        ship.applyCombatDamageAccrued();
        world.setOwner(null);
        turnData.addOrder(order);
        updater.update();
        assertEquals(2, ship.getDpRemaining());
        assertFalse(ship.hasCondition(ShipCondition.REPAIRED));
        assertEquals(12, world.getStockpile());
    }

    @Test
    void updateRepairFewerRepairNeeded() {
        ship.inflictCombatDamage(1);
        ship.applyCombatDamageAccrued();
        turnData.addOrder(order);
        updater.update();
        assertEquals(ship.getDp(), ship.getDpRemaining());
        assertTrue(ship.hasCondition(ShipCondition.REPAIRED));
        assertEquals(11, world.getStockpile());
    }

    @Test
    void updateRepairLimitedStockpile() {
        ship.inflictCombatDamage(4);
        ship.applyCombatDamageAccrued();
        world.setStockpile(1);
        order = RepairOrder.builder()
                .empire(empire1)
                .orderType(OrderType.REPAIR)
                .parameters("ship 4 world")
                .ships(Lists.newArrayList(ship))
                .dpToRepair(4)
                .worlds(Lists.newArrayList(world))
                .build();
        turnData.addOrder(order);
        updater.update();
        assertEquals(3, ship.getDpRemaining());
        assertTrue(ship.hasCondition(ShipCondition.REPAIRED));
        assertEquals(0, world.getStockpile());
    }

    @Test
    void updateRepairOrbital() {
        ship = createShip(starbaseClass, ZERO_COORDINATE, "ship", empire1);
        ship.inflictCombatDamage(3);
        ship.applyCombatDamageAccrued();
        order = RepairOrder.builder()
                .empire(empire1)
                .orderType(OrderType.REPAIR)
                .parameters("ship 3 world")
                .ships(Lists.newArrayList(ship))
                .dpToRepair(3)
                .worlds(Lists.newArrayList(world))
                .build();
        turnData.addOrder(order);
        updater.update();
        assertEquals(ship.getDp(), ship.getDpRemaining());
        assertTrue(ship.hasCondition(ShipCondition.REPAIRED));
        assertEquals(11, world.getStockpile());
    }

    @Test
    void updateRepairOrbitalFreeRepair() {
        final Ship starbase = createShip(starbaseClass, ZERO_COORDINATE, "starbase", empire1);
        turnData.load(ship, starbase);
        ship.inflictCombatDamage(3);
        ship.applyCombatDamageAccrued();
        updater.update();
        assertEquals(ship.getDp(), ship.getDpRemaining());
        assertTrue(ship.hasCondition(ShipCondition.REPAIRED));
        assertEquals(12, world.getStockpile());
    }
}