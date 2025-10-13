package com.starempires.updater;

import com.starempires.objects.Prohibition;
import com.starempires.objects.World;
import com.starempires.orders.OrderType;
import com.starempires.orders.TransferOrder;
import com.starempires.util.BaseTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TransferResourceUnitsPhaseUpdaterTest extends BaseTest {

    private World destination;
    private TransferResourceUnitsPhaseUpdater updater;

    @BeforeEach
    void setUp() {
        destination = createWorld("destination", ZERO_COORDINATE, 5);
        destination.setOwner(empire1);
        updater = new TransferResourceUnitsPhaseUpdater(turnData);
    }

    @Test
    void updateSuccessTransferRU() {
        final TransferOrder order = TransferOrder.builder()
                .empire(empire1)
                .orderType(OrderType.TRANSFER)
                .parameters("world 2 destination")
                .destination(destination)
                .world(world)
                .recipient(empire1)
                .amount(1)
                .build();
        turnData.addOrder(order);
        updater.update();
        assertEquals(11, world.getStockpile());
        assertEquals(1, destination.getStockpile());
    }

    @Test
    void updateSuccessTransferTooManyRU() {
        world.setStockpile(2);
        final TransferOrder order = TransferOrder.builder()
                .empire(empire1)
                .orderType(OrderType.TRANSFER)
                .parameters("world 4 destination")
                .world(world)
                .destination(destination)
                .recipient(empire1)
                .amount(4)
                .build();
        turnData.addOrder(order);
        updater.update();
        assertEquals(0, world.getStockpile());
        assertEquals(2, destination.getStockpile());
    }

    @Test
    void updateTransferAll() {
        final TransferOrder order = TransferOrder.builder()
                .empire(empire1)
                .orderType(OrderType.TRANSFER)
                .parameters("world max destination")
                .world(world)
                .destination(destination)
                .recipient(empire1)
                .transferAll(true)
                .build();
        turnData.addOrder(order);
        updater.update();
        assertEquals(0, world.getStockpile());
        assertEquals(12, destination.getStockpile());
    }

    @Test
    void updateTransferBlockaded() {
        world.setProhibition(Prohibition.BLOCKADED);
        final TransferOrder order = TransferOrder.builder()
                .empire(empire1)
                .orderType(OrderType.TRANSFER)
                .parameters("fromworld max toworld")
                .world(world)
                .destination(destination)
                .recipient(empire2)
                .transferAll(true)
                .build();
        turnData.addOrder(order);
        updater.update();
        assertEquals(12, world.getStockpile());
        assertEquals(0, destination.getStockpile());
    }

    @Test
    void updateTransferFromWorldNotOwned() {
        world.setOwner(null);
        final TransferOrder order = TransferOrder.builder()
                .empire(empire1)
                .orderType(OrderType.TRANSFER)
                .parameters("world max toworld")
                .world(world)
                .destination(destination)
                .recipient(empire2)
                .transferAll(true)
                .build();
        turnData.addOrder(order);
        updater.update();
        assertEquals(12, world.getStockpile());
        assertEquals(0, destination.getStockpile());
    }

    @Test
    void updateTransferToWorldUnowned() {
        world.setOwner(null);
        final TransferOrder order = TransferOrder.builder()
                .empire(empire1)
                .orderType(OrderType.TRANSFER)
                .parameters("world max destination")
                .world(world)
                .destination(destination)
                .recipient(empire2)
                .transferAll(true)
                .build();
        turnData.addOrder(order);
        updater.update();
        assertEquals(12, world.getStockpile());
        assertEquals(0, destination.getStockpile());
    }

    @Test
    void updateTransferToOtherEmpire() {
        destination.setOwner(empire2);
        final TransferOrder order = TransferOrder.builder()
                .empire(empire1)
                .orderType(OrderType.TRANSFER)
                .parameters("fromworld max toworld empire2")
                .world(world)
                .destination(destination)
                .recipient(empire2)
                .transferAll(true)
                .build();
        turnData.addOrder(order);
        updater.update();
        assertEquals(0, world.getStockpile());
        assertEquals(12, destination.getStockpile());
    }

    @Test
    void updateTransferWrongRecipient() {
        destination.setOwner(null);
        final TransferOrder order = TransferOrder.builder()
                .empire(empire1)
                .orderType(OrderType.TRANSFER)
                .parameters("fromworld max toworld recipient")
                .world(world)
                .destination(destination)
                .recipient(empire2)
                .transferAll(true)
                .build();
        turnData.addOrder(order);
        updater.update();
        assertEquals(12, world.getStockpile());
        assertEquals(0, destination.getStockpile());
    }

    @Test
    void updateTransferNoStockpile() {
        world.setStockpile(0);
        final TransferOrder order = TransferOrder.builder()
                .empire(empire1)
                .orderType(OrderType.TRANSFER)
                .parameters("fromworld max toworld recipient")
                .world(world)
                .destination(destination)
                .recipient(empire2)
                .transferAll(true)
                .build();
        turnData.addOrder(order);
        updater.update();
        assertEquals(0, world.getStockpile());
        assertEquals(0, destination.getStockpile());
    }
}