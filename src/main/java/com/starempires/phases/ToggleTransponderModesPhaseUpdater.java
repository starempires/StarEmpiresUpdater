package com.starempires.phases;

import com.starempires.TurnData;
import com.starempires.constants.Constants;
import com.starempires.objects.Empire;
import com.starempires.objects.Order;
import com.starempires.objects.OrderType;
import com.starempires.objects.Ship;

import java.util.Collection;
import java.util.List;

public class ToggleTransponderModesPhaseUpdater extends PhaseUpdater {

    public ToggleTransponderModesPhaseUpdater(final TurnData turnData) {
        super(Phase.TOGGLE_TRANSPONDER_MODES, turnData);
    }

    @Override
    public void update() {
        final List<Order> orders = turnData.getOrders(OrderType.TOGGLE);
        orders.forEach(order -> {
            final Empire empire = order.getEmpire();
            final String mode = order.getStringParameter(0);
            final List<String> shipHandles = order.getParameterSubList(1);
            final Collection<Ship> ships = empire.getShips(shipHandles);
            final boolean isPublic = mode.equalsIgnoreCase(Constants.TOKEN_PUBLIC);
            ships.forEach(ship -> ship.toggleTransponder(isPublic));
            addNewsResult(order, empire,
                    "You have set " + mode.toLowerCase() + " transponder mode on " + plural(ships.size(), "ship"));
        });
    }
}