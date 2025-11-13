package com.starempires.orders;

import com.fasterxml.jackson.databind.JsonNode;
import com.starempires.TurnData;
import com.starempires.objects.Empire;
import com.starempires.objects.Ship;
import lombok.experimental.SuperBuilder;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuperBuilder
public class DeployOrder extends ShipBasedOrder {

    // order: DEPLOY ship1 [ship2 ... ]
    private static final String REGEX = OBJECT_LIST_CAPTURE_REGEX;
    private static final Pattern PATTERN = Pattern.compile(REGEX, java.util.regex.Pattern.CASE_INSENSITIVE);

    public static DeployOrder parse(final TurnData turnData, final Empire empire, final String parameters) {
        final DeployOrder order = DeployOrder.builder().orderType(OrderType.DEPLOY).empire(empire).parameters(parameters).build();
        final Matcher matcher = PATTERN.matcher(parameters);
        if (matcher.matches()) {
            final String deviceNamesText = matcher.group(OBJECT_LIST_GROUP);
            final List<Ship> devices = getLiveShipsFromNames(empire, deviceNamesText, order);
            for (final Ship device : devices) {
                if (!device.isDevice()) {
                    order.addError(device, "Ship class %s is not a deployable device".formatted(device.getShipClass()));
                } else {
                    final Set<Ship> starbases = turnData.getStarbases(device);
                    if (CollectionUtils.isEmpty(starbases)) {
                        order.addOKResult(device);
                        if (device.isLoaded()) {
                            order.addWarning(device, "Device will be unloaded");
                        }
                        order.ships.add(device);
                        turnData.deploy(device);
                    } else {
                        order.addError(device, "Starbase %s prevents deployment of devices in sector %s".formatted(
                                starbases.stream().findFirst().orElseThrow(), device.getCoordinate()));
                    }
                }
            }
            order.setReady(!order.ships.isEmpty());
        }
        else {
            order.addError("Invalid DEPLOY order: " + parameters);
        }
        return order;
    }

    public static DeployOrder parseReady(final JsonNode node, final TurnData turnData) {
        final var builder = DeployOrder.builder();
        ShipBasedOrder.parseReady(node, turnData, OrderType.DEPLOY, builder);
        return builder.build();
    }
}