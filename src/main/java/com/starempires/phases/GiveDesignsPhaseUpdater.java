package com.starempires.phases;

import com.google.common.collect.Lists;
import com.starempires.TurnData;
import com.starempires.objects.Empire;
import com.starempires.objects.Order;
import com.starempires.objects.OrderType;
import com.starempires.objects.ShipClass;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
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
        orders.forEach(order -> {
            final Matcher giveMatcher = GIVE_DESIGNS_PATTERN.matcher(order.getParametersAsString());
            if (giveMatcher.matches()) {
                final Empire empire = order.getEmpire();
                final List<String> shipClassNames = Arrays.asList(giveMatcher.group(DESIGNS_GROUP).split(" "));
                final List<String> recipientNames = Arrays.asList(giveMatcher.group(RECIPIENTS_GROUP).split(" "));

                final Collection<ShipClass> shipClassesToGive = Lists.newArrayList();
                shipClassNames.forEach(shipClassName -> {
                    final ShipClass shipClass = turnData.getShipClass(shipClassName);
                    if (shipClass == null || !empire.isKnownShipClass(shipClass)) {
                        addNewsResult(order, "You have no information about ship class " + shipClassName);
                    } else {
                        shipClassesToGive.add(shipClass);
                    }
                });

                if (shipClassesToGive.isEmpty()) {
                    return;
                }

                recipientNames.forEach(recipientName -> {
                    final Empire recipient = turnData.getEmpire(recipientName);
                    if (recipient == null || !empire.isKnownEmpire(recipient)) {
                        addNewsResult(order, "You have no information about empire " + recipient);
                    } else {
                        shipClassesToGive.forEach(shipClass -> {
                            recipient.addKnownShipClass(shipClass);
                            addNews(recipient, empire + " gave the design for the " + shipClass + " ship class to you.");
                            addNewsResult(order, "You gave the design for the " + shipClass + " ship class to " + recipient + ".");
                        });
                    }
                });
            }
        });
    }
}