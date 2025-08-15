package com.starempires.updater;

import com.starempires.objects.Prohibition;
import com.starempires.objects.World;
import com.starempires.orders.BuildOrder;
import com.starempires.orders.OrderType;
import com.starempires.util.BaseTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class BuildShipsPhaseUpdaterTest extends BaseTest {

    private BuildShipsPhaseUpdater updater;
    private World world;

    @BeforeEach
    void setUp() {
        updater = new BuildShipsPhaseUpdater(turnData);
        world = createWorld("world", ZERO_COORDINATE, 10);
    }

    @Test
    void updateSuccessNames() {
        final int stockpile = 10;
        world.setOwner(empire);
        world.setStockpile(stockpile);
        empire.addKnownShipClass(probeClass);
        final BuildOrder order = BuildOrder.builder()
                .empire(empire)
                .orderType(OrderType.BUILD)
                .parameters("NewClass to recipient")
                .shipClassName(probeClass.getName())
                .count(2)
                .names(List.of("probe1", "probe2"))
                .world(world)
                .build();
        turnData.addOrder(order);
        updater.update();
        assertEquals(probeClass, empire.getShip("probe1").getShipClass());
        assertEquals(probeClass, empire.getShip("probe2").getShipClass());
        assertEquals(stockpile - 2 * probeClass.getCost(), world.getStockpile());
    }

    @Test
    void updateSuccessTooFewNames() {
        final int stockpile = 10;
        world.setOwner(empire);
        world.setStockpile(stockpile);
        empire.addKnownShipClass(probeClass);
        final BuildOrder order = BuildOrder.builder()
                .empire(empire)
                .orderType(OrderType.BUILD)
                .parameters("NewClass to recipient")
                .shipClassName(probeClass.getName())
                .count(2)
                .names(List.of("probe1"))
                .world(world)
                .build();
        turnData.addOrder(order);
        updater.update();
        assertEquals(probeClass, empire.getShip("probe1").getShipClass());
        assertNull(empire.getShip("probe2"));
        assertEquals(2, empire.getShips().size());
        assertEquals(stockpile - 2 * probeClass.getCost(), world.getStockpile());
    }

    @Test
    void updateSuccessBasename() {
        final int stockpile = 10;
        world.setOwner(empire);
        world.setStockpile(stockpile);
        empire.addKnownShipClass(probeClass);
        final BuildOrder order = BuildOrder.builder()
                .empire(empire)
                .orderType(OrderType.BUILD)
                .parameters("NewClass to recipient")
                .shipClassName(probeClass.getName())
                .count(2)
                .basename("probe")
                .world(world)
                .build();
        turnData.addOrder(order);
        updater.update();
        assertEquals(probeClass, empire.getShip("probe1").getShipClass());
        assertEquals(probeClass, empire.getShip("probe2").getShipClass());
        assertEquals(stockpile - 2 * probeClass.getCost(), world.getStockpile());
    }

    @Test
    void updateWorldNotOwned() {
        final int stockpile = 10;
        world.setStockpile(stockpile);
        empire.addKnownShipClass(probeClass);
        final BuildOrder order = BuildOrder.builder()
                .empire(empire)
                .orderType(OrderType.BUILD)
                .parameters("NewClass to recipient")
                .shipClassName(probeClass.getName())
                .count(2)
                .names(List.of("probe1", "probe2"))
                .world(world)
                .build();
        turnData.addOrder(order);
        updater.update();
        assertNull(empire.getShip("probe1"));
        assertNull(empire.getShip("probe2"));
        assertEquals(stockpile, world.getStockpile());
    }

    @Test
    void updateUnknownShipClass() {
        final int stockpile = 10;
        world.setOwner(empire);
        world.setStockpile(stockpile);
        final BuildOrder order = BuildOrder.builder()
                .empire(empire)
                .orderType(OrderType.BUILD)
                .parameters("NewClass to recipient")
                .shipClassName(probeClass.getName())
                .count(2)
                .names(List.of("probe1", "probe2"))
                .world(world)
                .build();
        turnData.addOrder(order);
        updater.update();
        assertNull(empire.getShip("probe1"));
        assertNull(empire.getShip("probe2"));
        assertEquals(stockpile, world.getStockpile());
    }

    @Test
    void updateWorldInterdicted() {
        final int stockpile = 10;
        world.setOwner(empire);
        world.setProhibition(Prohibition.INTERDICTED);
        world.setStockpile(stockpile);
        empire.addKnownShipClass(probeClass);
        final BuildOrder order = BuildOrder.builder()
                .empire(empire)
                .orderType(OrderType.BUILD)
                .parameters("NewClass to recipient")
                .shipClassName(probeClass.getName())
                .count(2)
                .names(List.of("probe1", "probe2"))
                .world(world)
                .build();
        turnData.addOrder(order);
        updater.update();
        assertNull(empire.getShip("probe1"));
        assertNull(empire.getShip("probe2"));
        assertEquals(stockpile, world.getStockpile());
    }

    @Test
    void updateInsufficientStockpile() {
        world.setOwner(empire);
        world.setStockpile(probeClass.getCost()); // enough for one ship
        empire.addKnownShipClass(probeClass);
        final BuildOrder order = BuildOrder.builder()
                .empire(empire)
                .orderType(OrderType.BUILD)
                .parameters("NewClass to recipient")
                .shipClassName(probeClass.getName())
                .count(2)
                .names(List.of("probe1", "probe2"))
                .world(world)
                .build();
        turnData.addOrder(order);
        updater.update();
        assertEquals(probeClass, empire.getShip("probe1").getShipClass());
        assertNull(empire.getShip("probe2"));
        assertEquals(0, world.getStockpile());
    }
}