package com.starempires.updater;

import com.starempires.TurnData;
import com.starempires.objects.Empire;
import com.starempires.objects.ShipClass;
import com.starempires.orders.GiveOrder;
import com.starempires.orders.Order;
import com.starempires.orders.OrderType;

import java.util.List;
import java.util.regex.Pattern;

public class GiveDesignsPhaseUpdater extends PhaseUpdater {


    // give design1 [design2 ...] to empire1 [empire2 ...]
    final static private String DESIGNS_GROUP = "designs";
    final static private String RECIPIENTS_GROUP = "recipients";
    final static private String GIVE_DESIGNS_REGEX = "^move\\s+@(?<" + DESIGNS_GROUP + ">\\w+(?:\\s+[\\w]+)*)\\s+to\\s+(?<" + RECIPIENTS_GROUP + ">[\\w]+(?:\\s+[\\w]+)*)\\s*$";
    final static private Pattern GIVE_DESIGNS_PATTERN = Pattern.compile(GIVE_DESIGNS_REGEX, Pattern.CASE_INSENSITIVE);

    public GiveDesignsPhaseUpdater(final TurnData turnData) {
        super(Phase.GIVE_DESIGNS, turnData);
    }

    @Override
    public void update() {
        final List<Order> orders = turnData.getOrders(OrderType.GIVE);
        orders.forEach(o -> {
            final GiveOrder order = (GiveOrder) o;
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