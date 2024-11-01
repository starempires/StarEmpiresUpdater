package com.starempires.phases;

import com.starempires.TurnData;
import com.starempires.constants.Constants;
import com.starempires.objects.Empire;
import com.starempires.objects.HullParameters;
import com.starempires.objects.HullType;
import com.starempires.objects.Order;
import com.starempires.objects.OrderType;
import com.starempires.objects.ShipClass;
import com.starempires.objects.World;
import org.apache.commons.lang3.EnumUtils;

import java.util.List;

public class CreateDesignsPhaseUpdater extends PhaseUpdater {

    public CreateDesignsPhaseUpdater(final TurnData turnData) {
        super(Phase.CREATE_DESIGNS, turnData);
    }

    private void designMissile(final Order order, final String className, final int guns, final int tonnage) {
        final double tonnageCost = turnData.getDoubleParameter(Constants.PARAMETER_MISSILE_TONNAGE_COST,
                Constants.DEFAULT_MISSILE_TONNAGE_COST);

        final HullParameters hdp = turnData.getHullParameters(HullType.MISSILE);
        final int cost = hdp.getCost(guns, tonnage, tonnageCost);
        final Empire empire = order.getEmpire();

        final ShipClass shipClass = ShipClass.builder()
                .name(className)
                .guns(guns)
                .dp(1)
                .engines(0)
                .scan(0)
                .racks(0)
                .tonnage(tonnage)
                .cost(cost)
                .ar(0)
                .hullType(HullType.MISSILE)
                .build();
        turnData.addShipClass(shipClass);

        empire.addKnownShipClass(shipClass);
        addNewsResult(order, empire, "You have designed new missile class " + className);
    }

    private void designShip(final Order order, final String className, final String worldName,
            final String hullTypeName, final int guns, final int dp,
            final int engines, final int scan, final int racks) {
        final HullType hullType = EnumUtils.getEnum(HullType.class, hullTypeName.toUpperCase());
        final HullParameters hdp = turnData.getHullParameters(hullType);
        final int cost = hdp.getCost(guns, dp, engines, scan, racks);
        final int tonnage = hdp.getTonnage(guns, dp, engines, scan, racks);
        final double arMultiplier = hdp.getArMultiplier();
        final int ar = (int) Math.ceil(dp * arMultiplier);

        final double designMultiplier = turnData.getDoubleParameter(Constants.PARAMETER_DESIGN_MULTIPLIER,
                Constants.DEFAULT_DESIGN_MULTIPLIER);
        final int fee = (int) Math.ceil(cost * designMultiplier);

        final World world = turnData.getWorld(worldName);
        final Empire empire = order.getEmpire();
        if (world.getOwner().equals(empire)) {
            if (!world.isInterdicted()) {
                int stockpile = world.getStockpile();
                if (fee <= stockpile) {

                    final ShipClass shipClass = ShipClass.builder()
                            .name(className)
                            .guns(guns)
                            .dp(dp)
                            .engines(engines)
                            .scan(scan)
                            .racks(racks)
                            .tonnage(tonnage)
                            .cost(cost)
                            .ar(ar)
                            .hullType(HullType.MISSILE)
                            .build();

                    empire.addKnownShipClass(shipClass);
                    stockpile = world.adjustStockpile(-fee);
                    addNewsResult(order, empire, "You have designed new " + hullTypeName + " class " + className
                            + " (" + fee + " fee; " + stockpile + " RU remaining).");
                }
                else {
                    addNewsResult(order, empire,
                            "World " + worldName + " has insufficient stockpile to pay design fee " + fee + " RU.");
                }
            }
            else {
                addNewsResult(order, empire, "World " + worldName + " is interdicted; no designs possible.");
            }
        }
        else {
            addNewsResult(order, empire, "You do not own world " + worldName);
        }
    }

    @Override
    public void update() {
        final List<Order> orders = turnData.getOrders(OrderType.DESIGN);
        orders.forEach(order -> {
            final String designType = order.getStringParameter(0);
            if (designType.equalsIgnoreCase(Constants.TOKEN_MISSILE)) {
                final String className = order.getStringParameter(1);
                final int guns = order.getIntParameter(2);
                final int tonnage = order.getIntParameter(3);
                designMissile(order, className, guns, tonnage);
            }
            else {
                final String className = order.getStringParameter(1);
                final String worldName = order.getStringParameter(2);
                final String hullTypeName = order.getStringParameter(3);
                final int guns = order.getIntParameter(4);
                final int dp = order.getIntParameter(5);
                final int engines = order.getIntParameter(6);
                final int scan = order.getIntParameter(7);
                final int racks = order.getIntParameter(8);
                designShip(order, className, worldName, hullTypeName, guns, dp, engines, scan, racks);
            }
        });
    }

}