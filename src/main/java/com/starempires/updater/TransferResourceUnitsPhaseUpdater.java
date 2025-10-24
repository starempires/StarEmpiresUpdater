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
            addOrderText(order);
            final Empire empire = order.getEmpire();
            final World world = order.getWorld();
            final World destination = order.getDestination();
            final Empire owner = order.getOwner();
            int amount = order.getAmount();
            if (world == null || !world.isOwnedBy(empire)) {
                addNews(order, "You do not own world " + world);
                return;
            }

            if (world.isBlockaded()) {
                addNews(order, "No RU transfers possible from blockaded world " + world);
                return;
            }

            if (destination == null || !destination.isOwnedBy(owner)) {
                if (owner.equals(empire)) {
                    addNews(order, "You do not own world " + destination);
                    return;
                }
                else {
                    addNews(order, "World " + destination + " is not owned by intended owner " + owner);
                    return;
                }
            }

            final int stockpile = world.getStockpile();
            if (order.isTransferAll()) {
                if (stockpile <= 0) {
                    addNews(order, "No RUs remaining at world " + world);
                    return;
                }
                amount = stockpile;
            }
            else {
                if (amount > stockpile) {
                    addNews(order, "Transfer amount " + amount + " exceeds stockpile " + stockpile
                            + " at world " + world + "; sending available stockpile");
                    amount = stockpile;
                }
            }

            world.adjustStockpile(-amount);
            destination.adjustStockpile(amount);

            addNews(empire, "World " + world + " transferred " + amount + " RU to destination " + destination);
            if (!owner.equals(empire)) {
                addNews(owner, "World " + destination + " has received " + amount + " RU from " + empire);
            }
        });
    }
}