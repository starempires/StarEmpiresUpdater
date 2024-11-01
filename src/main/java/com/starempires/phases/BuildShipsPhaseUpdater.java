package com.starempires.phases;

import com.starempires.TurnData;
import com.starempires.objects.Empire;
import com.starempires.objects.Order;
import com.starempires.objects.OrderType;
import com.starempires.objects.Ship;
import com.starempires.objects.ShipClass;
import com.starempires.objects.World;

import java.util.List;

public class BuildShipsPhaseUpdater extends PhaseUpdater {

    public BuildShipsPhaseUpdater(final TurnData turnData) {
        super(Phase.BUILD_SHIPS, turnData);
    }

    @Override
    public void update() {
        final List<Order> orders = turnData.getOrders(OrderType.BUILD);
        orders.forEach(order -> {
            final Empire empire = order.getEmpire();
            final List<String> parameters = order.getParameters();
            final String shipClassName = parameters.get(0);
            final String worldName = parameters.get(1);
            final String shipName = parameters.get(2);

            final ShipClass shipClass = turnData.getShipClass(shipClassName);
            if (shipClass == null && !empire.isKnownShipClass(shipClass)) {
                addNewsResult(order, empire,
                        "You have no design information for ship class " + shipClassName);
            }
            else {
                final World world = turnData.getWorld(worldName);
                if (world == null || !world.getOwner().equals(empire)) {
                    addNewsResult(order, empire, "You do not own world " + world);
                }
                else if (world.isInterdicted()) {
                    addNewsResult(order, empire, "World " + world + " is interdicted; no builds possible.");
                }
                else {
                    final int stockpile = world.getStockpile();
                    final int cost = shipClass.getCost();
                    if (cost <= stockpile) {
                        final int remaining = world.adjustStockpile(-cost);
                        final Ship ship = empire.buildShip(shipClass, world, shipName, turnData.getTurnNumber());
                        addNewsResult(order, empire, "You built " + shipClass + " ship " + ship + " at world "
                                + world + " (cost " + cost + "; " + remaining + " RU remaining)");
                    }
                    else {
                        addNewsResult(order, empire, "Insufficient stockpile (" + stockpile + ") on world "
                                + world + " to build ship class " + shipClass + " (cost " + cost + ")");
                    }
                }
            }
        });
    }
}