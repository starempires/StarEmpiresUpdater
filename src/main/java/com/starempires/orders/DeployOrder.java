package com.starempires.orders;

import com.starempires.TurnData;
import com.starempires.objects.Empire;
import com.starempires.objects.Ship;
import lombok.experimental.SuperBuilder;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;
import java.util.Set;

@SuperBuilder
public class DeployOrder extends ShipBasedOrder {

    public static DeployOrder parse(final TurnData turnData, final Empire empire, final String parameters) {
        final DeployOrder order = DeployOrder.builder().orderType(OrderType.DEPLOY).empire(empire).parameters(parameters).build();
        final List<Ship> devices = getShipsFromNames(empire, parameters, order);
        for (final Ship device : devices) {
            if (!device.isDevice()) {
                order.addError(device, "Ship class %s is not a deployable device".formatted(device.getShipClass()));
            }
            else {
                final Set<Ship> starbases = turnData.getStarbases(device);
                if (CollectionUtils.isEmpty(starbases)) {
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
        return order;
    }
}