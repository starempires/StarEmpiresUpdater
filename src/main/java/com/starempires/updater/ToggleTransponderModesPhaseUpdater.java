package com.starempires.updater;

import com.starempires.TurnData;
import com.starempires.objects.Empire;
import com.starempires.objects.Ship;
import com.starempires.objects.ShipClass;
import com.starempires.orders.Order;
import com.starempires.orders.OrderType;
import com.starempires.orders.ToggleOrder;

import java.util.List;

public class ToggleTransponderModesPhaseUpdater extends PhaseUpdater {

    public ToggleTransponderModesPhaseUpdater(final TurnData turnData) {
        super(Phase.TOGGLE_TRANSPONDER_MODES, turnData);
    }

    @Override
    public void update() {
        final List<Order> orders = turnData.getOrders(OrderType.TOGGLE);
        orders.forEach(o -> {
            final ToggleOrder order = (ToggleOrder)o;
            final boolean publicMode = order.isPublicMode();
            final String modeText = publicMode ? "public" : "private";
            final Empire empire = order.getEmpire();
            for (ShipClass shipClass: order.getShipClasses()) {
                final List<Ship> shipsOfClass = empire.getShips(shipClass).stream().filter(Ship::isAlive).toList();
                shipsOfClass.forEach(s -> s.toggleTransponder(publicMode));
                addNewsResult(order, "Transponder set to %s mode on %d %s-class %s".formatted(modeText, shipsOfClass.size(), shipClass, plural(shipsOfClass.size(), "ship")));
            }

            for (Ship ship: order.getShips()) {
                if (ship.isAlive()) {
                    ship.toggleTransponder(publicMode);
                    addNewsResult(order, "Transponder set to %s mode on %s".formatted(modeText, ship));
                }
                else {
                    addNewsResult(order, "Ship %s is destroyed".formatted(ship));
                }
            }
        });
    }
}