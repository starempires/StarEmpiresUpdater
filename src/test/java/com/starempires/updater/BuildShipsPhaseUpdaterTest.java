package com.starempires.updater;

import com.starempires.objects.Prohibition;
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

    @BeforeEach
    void setUp() {
        updater = new BuildShipsPhaseUpdater(turnData);
    }

    @Test
    void updateSuccessNames() {
        final int startingStockpile = world.getStockpile();
        empire1.addKnownShipClass(probeClass);
        final BuildOrder order = BuildOrder.builder()
                .empire(empire1)
                .orderType(OrderType.BUILD)
                .parameters("NewClass to recipient")
                .shipClassName(probeClass.getName())
                .count(2)
                .names(List.of("probe1", "probe2"))
                .world(world)
                .build();
        turnData.addOrder(order);
        updater.update();
        assertEquals(probeClass, empire1.getShip("probe1").getShipClass());
        assertEquals(probeClass, empire1.getShip("probe2").getShipClass());
        assertEquals(startingStockpile - 2 * probeClass.getCost(), world.getStockpile());
    }

    @Test
    void updateSuccessTooFewNames() {
        final int startingStockpile = world.getStockpile();
        empire1.addKnownShipClass(probeClass);
        final BuildOrder order = BuildOrder.builder()
                .empire(empire1)
                .orderType(OrderType.BUILD)
                .parameters("NewClass to recipient")
                .shipClassName(probeClass.getName())
                .count(2)
                .names(List.of("probe1"))
                .world(world)
                .build();
        turnData.addOrder(order);
        updater.update();
        assertEquals(probeClass, empire1.getShip("probe1").getShipClass());
        assertNull(empire1.getShip("probe2"));
        assertEquals(2, empire1.getShips().size());
        assertEquals(startingStockpile - 2 * probeClass.getCost(), world.getStockpile());
    }

    @Test
    void updateSuccessBasename() {
        final int startingStockpile = world.getStockpile();
        empire1.addKnownShipClass(probeClass);
        final BuildOrder order = BuildOrder.builder()
                .empire(empire1)
                .orderType(OrderType.BUILD)
                .parameters("NewClass to recipient")
                .shipClassName(probeClass.getName())
                .count(2)
                .basename("probe")
                .world(world)
                .build();
        turnData.addOrder(order);
        updater.update();
        assertEquals(probeClass, empire1.getShip("probe1").getShipClass());
        assertEquals(probeClass, empire1.getShip("probe2").getShipClass());
        assertEquals(startingStockpile - 2 * probeClass.getCost(), world.getStockpile());
    }

    @Test
    void updateWorldNotOwned() {
        final int startingStockpile = world.getStockpile();
        world.setOwner(null);
        empire1.addKnownShipClass(probeClass);
        final BuildOrder order = BuildOrder.builder()
                .empire(empire1)
                .orderType(OrderType.BUILD)
                .parameters("NewClass to recipient")
                .shipClassName(probeClass.getName())
                .count(2)
                .names(List.of("probe1", "probe2"))
                .world(world)
                .build();
        turnData.addOrder(order);
        updater.update();
        assertNull(empire1.getShip("probe1"));
        assertNull(empire1.getShip("probe2"));
        assertEquals(startingStockpile, world.getStockpile());
    }

    @Test
    void updateUnknownShipClass() {
        final int startingStockpile = world.getStockpile();
        final BuildOrder order = BuildOrder.builder()
                .empire(empire1)
                .orderType(OrderType.BUILD)
                .parameters("NewClass to recipient")
                .shipClassName(probeClass.getName())
                .count(2)
                .names(List.of("probe1", "probe2"))
                .world(world)
                .build();
        turnData.addOrder(order);
        updater.update();
        assertNull(empire1.getShip("probe1"));
        assertNull(empire1.getShip("probe2"));
        assertEquals(startingStockpile, world.getStockpile());
    }

    @Test
    void updateWorldInterdicted() {
        final int startingStockpile = world.getStockpile();
        world.setProhibition(Prohibition.INTERDICTED);
        empire1.addKnownShipClass(probeClass);
        final BuildOrder order = BuildOrder.builder()
                .empire(empire1)
                .orderType(OrderType.BUILD)
                .parameters("NewClass to recipient")
                .shipClassName(probeClass.getName())
                .count(2)
                .names(List.of("probe1", "probe2"))
                .world(world)
                .build();
        turnData.addOrder(order);
        updater.update();
        assertNull(empire1.getShip("probe1"));
        assertNull(empire1.getShip("probe2"));
        assertEquals(startingStockpile, world.getStockpile());
    }

    @Test
    void updateInsufficientStockpile() {
        world.setStockpile(probeClass.getCost()); // enough for one ship
        empire1.addKnownShipClass(probeClass);
        final BuildOrder order = BuildOrder.builder()
                .empire(empire1)
                .orderType(OrderType.BUILD)
                .parameters("NewClass to recipient")
                .shipClassName(probeClass.getName())
                .count(2)
                .names(List.of("probe1", "probe2"))
                .world(world)
                .build();
        turnData.addOrder(order);
        updater.update();
        assertEquals(probeClass, empire1.getShip("probe1").getShipClass());
        assertNull(empire1.getShip("probe2"));
        assertEquals(0, world.getStockpile());
    }
}