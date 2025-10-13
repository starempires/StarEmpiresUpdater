package com.starempires.updater;

import com.starempires.TurnData;
import com.starempires.objects.Empire;
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
        final Empire empire = order.getEmpire();

        final ShipClass shipClass = ShipClass.builder()
                .name(order.getName())
                .guns(order.getGuns())
                .dp(1)
                .engines(0)
                .scan(0)
                .racks(0)
                .tonnage(order.getTonnage())
                .cost(order.getCost())
                .ar(0)
                .hullType(HullType.MISSILE)
                .buildable(true)
                .build();
        turnData.addShipClass(shipClass);
        empire.addKnownShipClass(shipClass);
        addNewsResult(order, "You have designed new missile class " + order.getName());
    }

    private void designShip(final DesignOrder order) {
        final HullType hullType = order.getHullType();
        final int cost = order.getCost();
        final int tonnage = order.getTonnage();
        final int ar = order.getAr();
        final int designFee = order.getDesignFee();

        final World world = order.getWorld();
        final Empire empire = order.getEmpire();

        int stockpile = world.getStockpile();
        if (designFee > stockpile) {
            addNewsResult(order, "World %s has insufficient stockpile (%d RU) to pay %d RU design fee".formatted(world, stockpile, designFee));
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
                            .buildable(true)
                            .build();

        turnData.addShipClass(shipClass);
        empire.addKnownShipClass(shipClass);
        stockpile = world.adjustStockpile(-designFee);
        addNewsResult(order, "You designed new " + hullType + " class " + order.getName()
                            + " (" + designFee + " RU design fee; " + stockpile + " RU remaining).");
    }

    @Override
    public void update() {
        final List<Order> orders = turnData.getOrders(OrderType.DESIGN);
        orders.forEach(o -> {
            final DesignOrder order = (DesignOrder) o;
            addOrderText(order);
            final World world = order.getWorld();
            final Empire empire = order.getEmpire();
            if (!world.isOwnedBy(empire)) {
                addNewsResult(order, empire, "You do not own world %s".formatted(world));
                return;
            }
            if (world.isInterdicted()) {
                addNewsResult(order, empire, "World %s is interdicted; no designs possible".formatted(world));
                return;
            }

            if (order.getHullType() == HullType.MISSILE) {
                designMissile(order);
            } else {
                designShip(order);
            }
        });
    }
}