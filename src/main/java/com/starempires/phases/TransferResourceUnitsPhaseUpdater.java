package com.starempires.phases;

import com.starempires.TurnData;
import com.starempires.objects.Empire;
import com.starempires.objects.Order;
import com.starempires.objects.OrderType;
import com.starempires.objects.World;

import java.util.List;

public class TransferResourceUnitsPhaseUpdater extends PhaseUpdater {

    public TransferResourceUnitsPhaseUpdater(final TurnData turnData) {
        super(Phase.TRANSFER_RESOURCE_UNITS, turnData);
    }

    @Override
    public void update() {
        final List<Order> orders = turnData.getOrders(OrderType.TRANSFER);
        orders.forEach(order -> {
            final Empire empire = order.getEmpire();

            final String fromWorldName = order.getStringParameter(0);
            int amount = order.getIntParameter(1);
            final String toWorldName = order.getStringParameter(2);
            final Empire toEmpire;
            if (order.getParameters().size() == 4) {
                final String toEmpireName = order.getStringParameter(3);
                toEmpire = turnData.getEmpire(toEmpireName);
            }
            else {
                toEmpire = empire;
            }

            if (empire == null || (!empire.equals(toEmpire) && !empire.isKnownEmpire(toEmpire))) {
                addNewsResult(order, empire, "You have no information about empire " + toEmpire);
            }
            else {
                final World fromWorld = turnData.getWorld(fromWorldName);
                if (fromWorld == null || !fromWorld.getOwner().equals(empire)) {
                    addNewsResult(order, empire, "You do not own world " + fromWorld);
                }
                else if (fromWorld.isBlockaded()) {
                    addNewsResult(order, empire, "World " + fromWorld + " is blockaded -- no RU transfers possible");
                }
                else {
                    final int stockpile = fromWorld.getStockpile();
                    if (stockpile <= 0) {
                        addNewsResult(order, empire, "No RUs remaining at world " + fromWorld);
                    }
                    else {
                        final World toWorld = turnData.getWorld(toWorldName);
                        if (toWorld == null || !empire.isKnownWorld(toWorld)) {
                            addNewsResult(order, empire,
                                    "You have no information about destination world " + toWorldName);
                        }
                        else if (!toWorld.getOwner().equals(toEmpire)) {
                            if (toEmpire.equals(empire)) {
                                addNewsResult(order, empire, "You do not own world " + toWorld);
                            }
                            else {
                                addNewsResult(order, empire,
                                        "World " + toWorld + " is not owned by intended recipient " + toEmpire);
                            }
                        }
                        else {
                            if (stockpile < amount) {
                                amount = stockpile;
                                addNewsResult(order, empire,
                                        "Only " + stockpile + " RU available at world " + fromWorld);
                            }
                            addNews(empire, "World " + fromWorld + " transferred " + amount
                                    + " RU to destination world " + toWorld);
                            if (!toEmpire.equals(empire)) {
                                addNews(toEmpire, "World " + toWorld + " has received a shipment of " + amount
                                        + "RU from " + empire);
                            }
                            fromWorld.adjustStockpile(-amount);
                            toWorld.adjustStockpile(amount);
                        }
                    }
                }
            }
        });
    }
}