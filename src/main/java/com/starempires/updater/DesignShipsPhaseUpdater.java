package com.starempires.updater;

import com.starempires.TurnData;
import com.starempires.constants.Constants;
import com.starempires.objects.Empire;
import com.starempires.objects.HullParameters;
import com.starempires.objects.HullType;
import com.starempires.objects.ShipClass;
import com.starempires.objects.World;
import com.starempires.orders.DesignOrder;
import com.starempires.orders.Order;
import com.starempires.orders.OrderType;

import java.util.List;

public class DesignShipsPhaseUpdater extends PhaseUpdater {

    public DesignShipsPhaseUpdater(final TurnData turnData) {
        super(Phase.CREATE_DESIGNS, turnData);
    }

    private void designMissile(final DesignOrder order) {
        final HullParameters hullParameters = turnData.getHullParameters(HullType.MISSILE);
        final int cost = hullParameters.getCost(order.getGuns(), order.getTonnage());
        final Empire empire = order.getEmpire();

        final ShipClass shipClass = ShipClass.builder()
                .name(order.getName())
                .guns(order.getGuns())
                .dp(1)
                .engines(0)
                .scan(0)
                .racks(0)
                .tonnage(order.getTonnage())
                .cost(cost)
                .ar(0)
                .hullType(HullType.MISSILE)
                .build();
        turnData.addShipClass(shipClass);
        empire.addKnownShipClass(shipClass);
        addNewsResult(order, "You have designed new missile class " + order.getName());
    }

    private void designShip(final DesignOrder order) {
        final HullType hullType = order.getHullType();
        final HullParameters hullParameters = turnData.getHullParameters(hullType);
        final int cost = hullParameters.getCost(order.getGuns(), order.getDp(), order.getEngines(), order.getScan(), order.getRacks());
        final int tonnage = hullParameters.getTonnage(order.getGuns(), order.getDp(), order.getEngines(), order.getScan(), order.getRacks());
        final int ar = Math.max(1, Math.round(order.getDp() * Constants.DEFAULT_AUTO_REPAIR_MULTIPLIER));
        final int designCost = Math.min(1, Math.round(cost * Constants.DEFAULT_DESIGN_MULTIPLIER));

        final World world = order.getWorld();
        final Empire empire = order.getEmpire();
        if (!world.isOwnedBy(empire)) {
            addNewsResult(order, empire, "You do not own world " + world);
            return;
        }
        if (world.isInterdicted()) {
            addNewsResult(order, empire, "World " + world + " is interdicted; no designs possible.");
            return;
        }

        int stockpile = world.getStockpile();
        if (designCost > stockpile) {
            addNewsResult(order, "World " + world + " has insufficient stockpile to pay design fee " + designCost + " RU.");
            return;
        }

        final ShipClass shipClass = ShipClass.builder()
                            .name(order.getName())
                            .guns(order.getGuns())
                            .dp(order.getDp())
                            .engines(order.getEngines())
                            .scan(order.getScan())
                            .racks(order.getRacks())
                            .tonnage(tonnage)
                            .cost(cost)
                            .ar(ar)
                            .hullType(hullType)
                            .build();

        turnData.addShipClass(shipClass);
        empire.addKnownShipClass(shipClass);
        stockpile = world.adjustStockpile(-designCost);
        addNewsResult(order, "You have designed new " + hullType + " class " + order.getName()
                            + " (" + designCost + " fee; " + stockpile + " RU remaining).");
    }

    @Override
    public void update() {
        final List<Order> orders = turnData.getOrders(OrderType.DESIGN);
        orders.forEach(o -> {
            final DesignOrder order = (DesignOrder) o;
            if (order.getHullType() == HullType.MISSILE) {
                designMissile(order);
            } else {
                designShip(order);
            }
        });
    }
}