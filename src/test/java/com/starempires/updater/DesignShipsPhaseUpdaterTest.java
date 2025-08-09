package com.starempires.updater;

import com.google.common.collect.Lists;
import com.starempires.objects.HullType;
import com.starempires.objects.Prohibition;
import com.starempires.objects.ShipClass;
import com.starempires.objects.World;
import com.starempires.orders.DesignOrder;
import com.starempires.orders.OrderType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DesignShipsPhaseUpdaterTest extends BasePhaseUpdaterTest {

    private static final String NAME = "cruiser";
    private static final int GUNS = 20;
    private static final int DP = 10;
    private static final int ENGINES = 2;
    private static final int SCAN = 2;
    private static final int RACKS = 2;
    private World world;
    private DesignShipsPhaseUpdater updater;
    private DesignOrder order;

    @BeforeEach
    void setUp() {
        updater = new DesignShipsPhaseUpdater(turnData);
        world = createWorld("world", ZERO_COORDINATE, 10);
        world.setOwner(empire);
        world.setStockpile(20);

        order = DesignOrder.builder()
                .empire(empire)
                .orderType(OrderType.DESIGN)
                .parameters("world cruiser gunship 20 10 2 2 2")
                .hullType(HullType.GUNSHIP)
                .name(NAME)
                .guns(GUNS)
                .dp(DP)
                .engines(ENGINES)
                .scan(SCAN)
                .racks(RACKS)
                .world(world)
                .build();
        turnData.addOrders(Lists.newArrayList(order));
    }

    @Test
    void updateDesignShip() {
        updater.update();
        final ShipClass cruiserClass = turnData.getShipClass(NAME);
        assertNotNull(cruiserClass);
        assertEquals(GUNS, cruiserClass.getGuns());
        assertEquals(DP, cruiserClass.getDp());
        assertEquals(ENGINES, cruiserClass.getEngines());
        assertEquals(SCAN, cruiserClass.getScan());
        assertEquals(RACKS, cruiserClass.getRacks());
        assertEquals(1, cruiserClass.getAr());
        assertEquals(HullType.GUNSHIP, cruiserClass.getHullType());
        assertEquals(NAME, cruiserClass.getName());
        assertEquals(29, cruiserClass.getCost());
        assertEquals(13, cruiserClass.getTonnage());
        assertTrue(empire.getKnownShipClasses().contains(cruiserClass));
        assertEquals(5, world.getStockpile());
    }

    @Test
    void updateDesignInterdicted() {
        world.setProhibition(Prohibition.INTERDICTED);
        updater.update();
        final ShipClass cruiserClass = turnData.getShipClass(NAME);
        assertNull(cruiserClass);
        assertEquals(20, world.getStockpile());
    }

    @Test
    void updateDesignWorldUnowned() {
        world.setOwner(null);
        updater.update();
        final ShipClass cruiserClass = turnData.getShipClass(NAME);
        assertNull(cruiserClass);
        assertEquals(20, world.getStockpile());
    }

    @Test
    void updateDesignInsufficientStockpile() {
        world.setStockpile(0);
        updater.update();
        final ShipClass cruiserClass = turnData.getShipClass(NAME);
        assertNull(cruiserClass);
        assertEquals(0, world.getStockpile());
    }

    @Test
    void updateDesignMissile() {
        final int missileGuns = 15;
        final int missileTonnage = 2;
        final String name = "supernuke";
        final World world2 = createWorld("world2", ZERO_COORDINATE, 10);
        world2.setOwner(empire);
        world2.setStockpile(20);
        order = DesignOrder.builder()
                .empire(empire)
                .orderType(OrderType.DESIGN)
                .parameters("world2 supernuke missile 15 2")
                .hullType(HullType.MISSILE)
                .name(name)
                .guns(missileGuns)
                .tonnage(missileTonnage)
                .world(world2)
                .build();
        turnData.addOrders(Lists.newArrayList(order));
        updater.update();
        final ShipClass missileClass = turnData.getShipClass(name);
        assertNotNull(missileClass);
        assertEquals(missileGuns, missileClass.getGuns());
        assertEquals(1, missileClass.getDp());
        assertEquals(0, missileClass.getEngines());
        assertEquals(0, missileClass.getScan());
        assertEquals(0, missileClass.getRacks());
        assertEquals(0, missileClass.getAr());
        assertEquals(HullType.MISSILE, missileClass.getHullType());
        assertEquals(name, missileClass.getName());
        assertEquals(4, missileClass.getCost());
        assertEquals(missileTonnage, missileClass.getTonnage());
        assertTrue(empire.getKnownShipClasses().contains(missileClass));
        assertEquals(20, world2.getStockpile());
    }
}