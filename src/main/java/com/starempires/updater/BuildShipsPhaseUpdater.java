package com.starempires.updater;

import com.starempires.TurnData;
import com.starempires.objects.Empire;
import com.starempires.objects.Ship;
import com.starempires.objects.ShipClass;
import com.starempires.objects.World;
import com.starempires.orders.BuildOrder;
import com.starempires.orders.Order;
import com.starempires.orders.OrderType;

import java.util.List;

public class BuildShipsPhaseUpdater extends PhaseUpdater {

    public BuildShipsPhaseUpdater(final TurnData turnData) {
        super(Phase.BUILD_SHIPS, turnData);
    }

    private void buildShips(final BuildOrder order) {
        final Empire empire = order.getEmpire();
        final ShipClass shipClass = order.getShipClass();
        final World world = order.getWorld();
        final int startingNumber = empire.getLargestBasenameNumber(order.getBasename());
        final int count = order.getCount();
        final int cost = shipClass.getCost();
        final String basename = order.getBasename();
        final List<String> names = order.getNames();
        for (int i = 0; i < count; ++i) {
            final int stockpile = world.getStockpile();
            if (cost <= stockpile) {
                final int remaining = world.adjustStockpile(-cost);
                String name = null;
                if (basename != null) {
                    name = basename + (startingNumber + i);
                }
                else if (count < names.size()) {
                    name = names.get(i);
                }
                final Ship ship = empire.buildShip(shipClass, world, name, turnData.getTurnNumber());
                addNewsResult(order, "You built " + shipClass + " ship " + ship + " at world "
                        + world + " (cost " + cost + "; " + remaining + " RU remaining)");
            } else {
                addNewsResult(order, "Insufficient stockpile (" + stockpile + ") on world "
                        + world + " to build ship class " + shipClass + " (cost " + cost + ")");
                break;
            }
        }
    }

    @Override
    public void update() {
        final List<Order> orders = turnData.getOrders(OrderType.BUILD);
        orders.forEach(o -> {
            final BuildOrder order = (BuildOrder) o;
            final Empire empire = order.getEmpire();
            final ShipClass shipClass = order.getShipClass();
            final World world = order.getWorld();

            if (!empire.isKnownShipClass(shipClass)) {
                addNewsResult(order, "You have no design information for ship class " + shipClass);
            } else {
                if (!world.isOwnedBy(empire)) {
                    addNewsResult(order, "You do not own world " + world);
                } else if (world.isInterdicted()) {
                    addNewsResult(order, "World " + world + " is interdicted; no builds possible.");
                } else {
                    buildShips(order);
                }
            }
        });
    }
}