package com.starempires.orders;

import com.google.common.collect.Lists;
import com.starempires.TurnData;
import com.starempires.objects.Empire;
import com.starempires.objects.Ship;
import com.starempires.objects.ShipClass;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Getter
@SuperBuilder
public class ToggleOrder extends ShipBasedOrder {
    // toggle {public|private} ship1 [ship2 ...]
    // toggle {public|private} ship-class1 [ship-class2 ...]

    final private static String MODE_GROUP = "mode";
    final private static String REGEX = "(<" + MODE_GROUP + ">public|private)\\s+" + SHIP_LIST_REGEX;
    final private static Pattern PATTERN = Pattern.compile(REGEX,  Pattern.CASE_INSENSITIVE);

    private boolean publicMode;
    private final List<ShipClass> shipClasses = Lists.newArrayList();

    public static ToggleOrder parse(final TurnData turnData, final Empire empire, final String parameters) {
        final ToggleOrder order = ToggleOrder.builder()
                .empire(empire)
                .orderType(OrderType.TOGGLE)
                .parameters(parameters)
                .build();
        final Matcher matcher = PATTERN.matcher(parameters);
        if (matcher.matches()) {
            order.publicMode = matcher.group(MODE_GROUP).equalsIgnoreCase("public");
            final String[] shipNames = matcher.group(SHIP_LIST_GROUP).split(" ");
            for (String shipName : shipNames) {
                final ShipClass shipClass = turnData.getShipClass(shipName);
                if (shipClass == null || !empire.isKnownShipClass(shipClass)) {
                    final Ship ship = turnData.getShip(shipName);
                    if (ship == null || !ship.isOwnedBy(empire)) {
                        order.addError("Unknown ship/class " + shipName);
                    } else {
                        order.ships.add(ship);
                        order.addOKResult(ship);
                    }
                } else {
                    order.shipClasses.add(shipClass);
                    order.addOKResult(shipClass);
                }
            }
            order.setReady(!order.ships.isEmpty() && !order.shipClasses.isEmpty());
        } else {
            order.addError("Invalid toggle order: " + parameters);
            order.setReady(false);
        }

        return order;
    }
}