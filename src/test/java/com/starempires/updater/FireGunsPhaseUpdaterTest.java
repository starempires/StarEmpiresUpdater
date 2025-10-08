package com.starempires.updater;

import com.google.common.collect.Lists;
import com.starempires.objects.Ship;
import com.starempires.objects.ShipCondition;
import com.starempires.orders.FireOrder;
import com.starempires.orders.OrderType;
import com.starempires.util.BaseTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FireGunsPhaseUpdaterTest extends BaseTest {

    private Ship ship;
    private FireOrder order;
    private FireGunsPhaseUpdater updater;

    @BeforeEach
    void setUp() {
        updater = new FireGunsPhaseUpdater(turnData);
    }

    @Test
    void update() {
        ship = createShip(probeClass, ZERO_COORDINATE, "ship", empire1);
        final Ship target = createShip(probeClass, ZERO_COORDINATE, "target", empire2);
        order = FireOrder.builder()
                .empire(empire1)
                .orderType(OrderType.FIRE)
                .parameters("ship at empire2")
                .ships(Lists.newArrayList(ship))
                .targets(Lists.newArrayList(empire2))
                .coordinate(ZERO_COORDINATE)
                .build();
        turnData.addOrders(Lists.newArrayList(order));
        updater.update();
        assertEquals(ship.getAvailableGuns(), target.getCombatDamageAccrued());
        assertTrue(ship.getConditions().contains(ShipCondition.FIRED_GUNS));
        assertTrue(target.getConditions().contains(ShipCondition.HIT_IN_COMBAT));
    }

    @Test
    void updateMultipleTargets() {
        ship = createShip(starbaseClass, ZERO_COORDINATE, "ship", empire1);
        final Ship target1 = createShip(probeClass, ZERO_COORDINATE, "target1", empire2);
        final Ship target2 = createShip(probeClass, ZERO_COORDINATE, "target2", empire2);
        order = FireOrder.builder()
                .empire(empire1)
                .orderType(OrderType.FIRE)
                .parameters("ship at empire2")
                .ships(Lists.newArrayList(ship))
                .targets(Lists.newArrayList(empire2))
                .coordinate(ZERO_COORDINATE)
                .build();
        turnData.addOrders(Lists.newArrayList(order));
        updater.update();
        assertEquals(target1.getDpRemaining(), target1.getCombatDamageAccrued());
        assertEquals(target2.getDpRemaining(), target2.getCombatDamageAccrued());
        assertTrue(ship.getConditions().contains(ShipCondition.FIRED_GUNS));
        assertTrue(target1.getConditions().contains(ShipCondition.HIT_IN_COMBAT));
        assertTrue(target2.getConditions().contains(ShipCondition.HIT_IN_COMBAT));
    }

    @Test
    void updateMultipleAttackers() {
        final Ship attacker1 = createShip(probeClass, ZERO_COORDINATE, "attacker2", empire1);
        final Ship attacker2 = createShip(probeClass, ZERO_COORDINATE, "attacker2", empire1);
        final Ship target = createShip(starbaseClass, ZERO_COORDINATE, "target", empire2);
        order = FireOrder.builder()
                .empire(empire1)
                .orderType(OrderType.FIRE)
                .parameters("ship at empire2")
                .ships(Lists.newArrayList(attacker1, attacker2))
                .targets(Lists.newArrayList(empire2))
                .coordinate(ZERO_COORDINATE)
                .build();
        turnData.addOrders(Lists.newArrayList(order));
        updater.update();
        assertEquals(attacker1.getAvailableGuns() + attacker2.getAvailableGuns(), target.getCombatDamageAccrued());
        assertTrue(attacker1.getConditions().contains(ShipCondition.FIRED_GUNS));
        assertTrue(attacker2.getConditions().contains(ShipCondition.FIRED_GUNS));
        assertTrue(target.getConditions().contains(ShipCondition.HIT_IN_COMBAT));
    }

    @Test
    void updateMoreAttackersThanNeeded() {
        final Ship attacker1 = createShip(probeClass, ZERO_COORDINATE, "attacker2", empire1);
        final Ship attacker2 = createShip(probeClass, ZERO_COORDINATE, "attacker2", empire1);
        final Ship target = createShip(probeClass, ZERO_COORDINATE, "target", empire2);
        order = FireOrder.builder()
                .empire(empire1)
                .orderType(OrderType.FIRE)
                .parameters("ship at empire2")
                .ships(Lists.newArrayList(attacker1, attacker2))
                .targets(Lists.newArrayList(empire2))
                .coordinate(ZERO_COORDINATE)
                .build();
        turnData.addOrders(Lists.newArrayList(order));
        updater.update();
        assertEquals(target.getDpRemaining(), target.getCombatDamageAccrued());
        assertTrue(attacker1.getConditions().contains(ShipCondition.FIRED_GUNS));
        assertFalse(attacker2.getConditions().contains(ShipCondition.FIRED_GUNS));
        assertTrue(target.getConditions().contains(ShipCondition.HIT_IN_COMBAT));
    }

    @Test
    void updateMissilesAttackFirst() {
        final Ship missile = createShip(missileClass, ZERO_COORDINATE, "missile", empire1);
        final Ship attacker = createShip(probeClass, ZERO_COORDINATE, "attacker", empire1);
        final Ship target = createShip(fighterClass, ZERO_COORDINATE, "target", empire2);
        order = FireOrder.builder()
                .empire(empire1)
                .orderType(OrderType.FIRE)
                .parameters("ship at empire2")
                .ships(Lists.newArrayList(missile, attacker))
                .targets(Lists.newArrayList(empire2))
                .coordinate(ZERO_COORDINATE)
                .build();
        turnData.addOrders(Lists.newArrayList(order));
        updater.update();
        assertEquals(missile.getGuns(), target.getCombatDamageAccrued());
        assertTrue(missile.getConditions().contains(ShipCondition.FIRED_GUNS));
        assertFalse(attacker.getConditions().contains(ShipCondition.FIRED_GUNS));
        assertTrue(target.getConditions().contains(ShipCondition.HIT_IN_COMBAT));
        assertTrue(missile.getConditions().contains(ShipCondition.DESTROYED_IN_COMBAT));
        assertFalse(missile.isAlive());
    }

    @Test
    void updateMultipleAttackersWithMissileAndMultipleTargets() {
        final Ship missile = createShip(missileClass, ZERO_COORDINATE, "missile", empire1);
        final Ship attacker = createShip(probeClass, ZERO_COORDINATE, "attacker", empire1);
        final Ship target1 = createShip(fighterClass, ZERO_COORDINATE, "target1", empire2);
        final Ship target2 = createShip(fighterClass, ZERO_COORDINATE, "target2", empire2);
        order = FireOrder.builder()
                .empire(empire1)
                .orderType(OrderType.FIRE)
                .parameters("ship at empire2")
                .ships(Lists.newArrayList(missile, attacker))
                .targets(Lists.newArrayList(empire2))
                .coordinate(ZERO_COORDINATE)
                .build();
        turnData.addOrders(Lists.newArrayList(order));
        updater.update();
        assertEquals(missile.getGuns(), target1.getCombatDamageAccrued());
        assertEquals(attacker.getGuns(), target2.getCombatDamageAccrued());
        assertTrue(missile.getConditions().contains(ShipCondition.FIRED_GUNS));
        assertTrue(attacker.getConditions().contains(ShipCondition.FIRED_GUNS));
        assertTrue(target1.getConditions().contains(ShipCondition.HIT_IN_COMBAT));
        assertTrue(target2.getConditions().contains(ShipCondition.HIT_IN_COMBAT));
        assertTrue(missile.getConditions().contains(ShipCondition.DESTROYED_IN_COMBAT));
        assertFalse(missile.isAlive());
    }

}