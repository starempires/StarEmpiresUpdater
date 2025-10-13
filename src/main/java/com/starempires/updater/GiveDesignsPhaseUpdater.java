package com.starempires.updater;

import com.starempires.TurnData;
import com.starempires.objects.Empire;
import com.starempires.objects.ShipClass;
import com.starempires.orders.GiveOrder;
import com.starempires.orders.Order;
import com.starempires.orders.OrderType;

import java.util.List;

public class GiveDesignsPhaseUpdater extends PhaseUpdater {

    public GiveDesignsPhaseUpdater(final TurnData turnData) {
        super(Phase.GIVE_DESIGNS, turnData);
    }

    @Override
    public void update() {
        final List<Order> orders = turnData.getOrders(OrderType.GIVE);
        orders.forEach(o -> {
            final GiveOrder order = (GiveOrder) o;
            addOrderText(order);
            final Empire empire = order.getEmpire();
            for (ShipClass shipClass : order.getShipClasses()) {
                for (Empire recipient : order.getRecipients()) {
                    recipient.addKnownShipClass(shipClass);
                    addNews(recipient, empire + " gave the design for the " + shipClass + " ship class to you.");
                    addNewsResult(order, "You gave the design for the " + shipClass + " ship class to " + recipient + ".");
                }
            }
        });
    }
}