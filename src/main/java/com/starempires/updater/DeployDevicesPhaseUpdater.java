package com.starempires.updater;

import com.starempires.TurnData;
import com.starempires.objects.Empire;
import com.starempires.objects.Ship;
import com.starempires.orders.DeployOrder;
import com.starempires.orders.Order;
import com.starempires.orders.OrderType;
import org.apache.commons.collections4.CollectionUtils;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * parameters:
 * DEPLOY device1 device2 ...
 */
public class DeployDevicesPhaseUpdater extends PhaseUpdater {

    public DeployDevicesPhaseUpdater(final TurnData turnData) {
        super(Phase.DEPLOY_DEVICES, turnData);
    }

    private void deployDevice(final Order order, final Ship device) {
        final Collection<Empire> newsEmpires = turnData.getEmpiresPresent(device);
        final Set<Ship> starbases = turnData.getStarbases(device);
        if (CollectionUtils.isEmpty(starbases)) {
            turnData.deploy(device);
            addNewsResult(order, newsEmpires, "Device %s deployed in sector %s".formatted(device, device.getCoordinate()));
        } else {
            addNewsResult(order, newsEmpires,
                    "Starbase %s prevents deployment of device %s in sector %s".formatted(starbases.stream().findFirst().orElseThrow(), device, device.getCoordinate()));
        }
        addNewsResult(order, newsEmpires, "Device %s destroyed during deployment".formatted(device));
    }

    @Override
    public void update() {
        final List<Order> orders = turnData.getOrders(OrderType.DEPLOY);
        orders.forEach(o -> {
            final DeployOrder order = (DeployOrder) o;
            for (final Ship ship : order.getShips()) {
                deployDevice(order, ship);
            }
        });
    }
}