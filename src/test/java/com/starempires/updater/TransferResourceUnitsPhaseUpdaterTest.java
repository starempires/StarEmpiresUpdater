package com.starempires.updater;

import com.starempires.objects.Empire;
import com.starempires.objects.Prohibition;
import com.starempires.objects.World;
import com.starempires.orders.OrderType;
import com.starempires.orders.TransferOrder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TransferResourceUnitsPhaseUpdaterTest extends BasePhaseUpdaterTest {

    private World fromWorld;
    private World toWorld;
    private TransferResourceUnitsPhaseUpdater updater;

    @BeforeEach
    void setUp() {
        fromWorld = createWorld("fromworld", ZERO_COORDINATE, 5);
        fromWorld.setOwner(empire);
        toWorld = createWorld("toworld", ZERO_COORDINATE, 5);
        toWorld.setOwner(empire);
        updater = new TransferResourceUnitsPhaseUpdater(turnData);
    }

    @Test
    void updateSuccessTransferRU() {
        fromWorld.setStockpile(2);
        final TransferOrder order = TransferOrder.builder()
                .empire(empire)
                .orderType(OrderType.TRANSFER)
                .parameters("fromworld 2 toworld")
                .fromWorld(fromWorld)
                .toWorld(toWorld)
                .toEmpire(empire)
                .amount(1)
                .build();
        turnData.addOrder(order);
        updater.update();
        assertEquals(1, fromWorld.getStockpile());
        assertEquals(1, toWorld.getStockpile());
    }

    @Test
    void updateSuccessTransferTooManyRU() {
        fromWorld.setStockpile(2);
        final TransferOrder order = TransferOrder.builder()
                .empire(empire)
                .orderType(OrderType.TRANSFER)
                .parameters("fromworld 4 toworld")
                .fromWorld(fromWorld)
                .toWorld(toWorld)
                .toEmpire(empire)
                .amount(4)
                .build();
        turnData.addOrder(order);
        updater.update();
        assertEquals(0, fromWorld.getStockpile());
        assertEquals(2, toWorld.getStockpile());
    }

    @Test
    void updateTransferAll() {
        fromWorld.setStockpile(2);
        final TransferOrder order = TransferOrder.builder()
                .empire(empire)
                .orderType(OrderType.TRANSFER)
                .parameters("fromworld all toworld")
                .fromWorld(fromWorld)
                .toWorld(toWorld)
                .toEmpire(empire)
                .transferAll(true)
                .build();
        turnData.addOrder(order);
        updater.update();
        assertEquals(0, fromWorld.getStockpile());
        assertEquals(2, toWorld.getStockpile());
    }

    @Test
    void updateTransferBlockaded() {
        fromWorld.setStockpile(2);
        fromWorld.setProhibition(Prohibition.BLOCKADED);
        final TransferOrder order = TransferOrder.builder()
                .empire(empire)
                .orderType(OrderType.TRANSFER)
                .parameters("fromworld all toworld")
                .fromWorld(fromWorld)
                .toWorld(toWorld)
                .toEmpire(empire)
                .transferAll(true)
                .build();
        turnData.addOrder(order);
        updater.update();
        assertEquals(2, fromWorld.getStockpile());
        assertEquals(0, toWorld.getStockpile());
    }

    @Test
    void updateTransferFromWorldNotOwned() {
        fromWorld.setStockpile(2);
        fromWorld.setOwner(null);
        final TransferOrder order = TransferOrder.builder()
                .empire(empire)
                .orderType(OrderType.TRANSFER)
                .parameters("fromworld all toworld")
                .fromWorld(fromWorld)
                .toWorld(toWorld)
                .toEmpire(empire)
                .transferAll(true)
                .build();
        turnData.addOrder(order);
        updater.update();
        assertEquals(2, fromWorld.getStockpile());
        assertEquals(0, toWorld.getStockpile());
    }

    @Test
    void updateTransferToWorldUnowned() {
        fromWorld.setStockpile(2);
        toWorld.setOwner(null);
        final TransferOrder order = TransferOrder.builder()
                .empire(empire)
                .orderType(OrderType.TRANSFER)
                .parameters("fromworld all toworld")
                .fromWorld(fromWorld)
                .toWorld(toWorld)
                .toEmpire(empire)
                .transferAll(true)
                .build();
        turnData.addOrder(order);
        updater.update();
        assertEquals(2, fromWorld.getStockpile());
        assertEquals(0, toWorld.getStockpile());
    }

    @Test
    void updateTransferToOtherEmpire() {
        final Empire recipient = createEmpire("recipient");
        toWorld.setOwner(recipient);
        fromWorld.setStockpile(2);
        final TransferOrder order = TransferOrder.builder()
                .empire(empire)
                .orderType(OrderType.TRANSFER)
                .parameters("fromworld all toworld recipient")
                .fromWorld(fromWorld)
                .toWorld(toWorld)
                .toEmpire(recipient)
                .transferAll(true)
                .build();
        turnData.addOrder(order);
        updater.update();
        assertEquals(0, fromWorld.getStockpile());
        assertEquals(2, toWorld.getStockpile());
    }

    @Test
    void updateTransferWrongRecipient() {
        final Empire recipient = createEmpire("recipient");
        toWorld.setOwner(null);
        fromWorld.setStockpile(2);
        final TransferOrder order = TransferOrder.builder()
                .empire(empire)
                .orderType(OrderType.TRANSFER)
                .parameters("fromworld all toworld recipient")
                .fromWorld(fromWorld)
                .toWorld(toWorld)
                .toEmpire(recipient)
                .transferAll(true)
                .build();
        turnData.addOrder(order);
        updater.update();
        assertEquals(2, fromWorld.getStockpile());
        assertEquals(0, toWorld.getStockpile());
    }

    @Test
    void updateTransferNoStockpile() {
        fromWorld.setStockpile(0);
        final TransferOrder order = TransferOrder.builder()
                .empire(empire)
                .orderType(OrderType.TRANSFER)
                .parameters("fromworld all toworld recipient")
                .fromWorld(fromWorld)
                .toWorld(toWorld)
                .toEmpire(empire)
                .transferAll(true)
                .build();
        turnData.addOrder(order);
        updater.update();
        assertEquals(0, fromWorld.getStockpile());
        assertEquals(0, toWorld.getStockpile());
    }
}