package com.starempires.phases;

import com.google.common.collect.Lists;
import com.starempires.TurnData;
import com.starempires.constants.Constants;
import com.starempires.objects.Empire;
import com.starempires.objects.Order;
import com.starempires.objects.OrderType;
import com.starempires.objects.ShipClass;

import java.util.Collection;
import java.util.List;

public class GiveDesignsPhaseUpdater extends PhaseUpdater {

    public GiveDesignsPhaseUpdater(final TurnData turnData) {
        super(Phase.GIVE_DESIGNS, turnData);
    }

    @Override
    public void update() {
        final List<Order> orders = turnData.getOrders(OrderType.GIVE);
        orders.forEach(order -> {
            final int index = order.indexOfIgnoreCase(Constants.TOKEN_TO);

            final Empire empire = order.getEmpire();
            final List<String> shipClassNames = order.getParameterSubList(0, index);
            final List<String> recipientHandles = order.getParameterSubList(index + 1);

            final Collection<ShipClass> shipClasses = Lists.newArrayList();
            shipClassNames.forEach(shipClassName -> {
                final ShipClass shipClass = turnData.getShipClass(shipClassName);
                if (shipClass == null || !empire.isKnownShipClass(shipClass)) {
                    addNewsResult(order, empire, "You have no information about ship class " + shipClassName);
                }
                else {
                    shipClasses.add(shipClass);
                }
            });

            if (shipClasses.isEmpty()) {
                return;
            }

            recipientHandles.forEach(recipientHandle -> {
                final Empire recipient = turnData.getEmpire(recipientHandle);
                if (recipient == null || !empire.isKnownEmpire(recipient)) {
                    addNewsResult(order, empire, "You have no information about empire " + recipient);
                }
                else {
                    shipClasses.forEach(shipClass -> {
                        if (!recipient.isKnownShipClass(shipClass)) {
                            recipient.addKnownShipClass(shipClass);
                            addNews(recipient,
                                    empire + " gave the design for the " + shipClass + " ship class to you.");
                        }
                        addNewsResult(order, empire,
                                "You gave the design for the " + shipClass + " ship class to " + recipient + ".");
                    });
                }
            });
        });
    }
}