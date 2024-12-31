package com.starempires.updater;

import com.starempires.TurnData;
import com.starempires.objects.Empire;
import com.starempires.objects.World;
import com.starempires.orders.Order;
import com.starempires.orders.OrderType;
import com.starempires.orders.TransferOrder;

import java.util.List;

public class TransferResourceUnitsPhaseUpdater extends PhaseUpdater {

    public TransferResourceUnitsPhaseUpdater(final TurnData turnData) {
        super(Phase.TRANSFER_RESOURCE_UNITS, turnData);
    }

    @Override
    public void update() {
        final List<Order> orders = turnData.getOrders(OrderType.TRANSFER);
        orders.forEach(o -> {
            final TransferOrder order = (TransferOrder)o;
            final Empire empire = order.getEmpire();
            final World fromWorld = order.getFromWorld();
            final World toWorld = order.getToWorld();
            final Empire toEmpire = order.getToEmpire();
            int amount = order.getAmount();
            if (!fromWorld.isOwnedBy(empire)) {
                addNewsResult(order, "You do not own world " + fromWorld);
                return;
            }

            if (fromWorld.isBlockaded()) {
                addNewsResult(order, "World " + fromWorld + " is blockaded; no RU transfers possible");
                return;
            }

            if (!toWorld.isOwnedBy(toEmpire)) {
                if (toEmpire.equals(empire)) {
                    addNewsResult(order, "You do not own world " + toWorld);
                }
                else {
                    addNewsResult(order, "World " + toWorld + " is not owned by intended recipient " + toEmpire);
                }
            }

            final int stockpile = fromWorld.getStockpile();
            if (order.isTransferAll()) {
                if (stockpile <= 0) {
                    addNewsResult(order, "No RUs remaining at world " + fromWorld);
                    return;
                }
                amount = stockpile;
            }
            else {
                if (amount > stockpile) {
                    addNewsResult(order, "Transfer amount " + amount + " exceeds stockpile " + stockpile
                            + " at world " + fromWorld + "; sending available stockpile");
                    amount = stockpile;
                }
            }

            fromWorld.adjustStockpile(-amount);
            toWorld.adjustStockpile(amount);

            addNews(empire, "World " + fromWorld + " transferred " + amount + " RU to destination " + toWorld);
            if (!toEmpire.equals(empire)) {
                addNews(toEmpire, "World " + toWorld + " has received " + amount + "RU from " + empire);
            }
        });
    }
}