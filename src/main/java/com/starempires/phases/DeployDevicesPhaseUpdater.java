package com.starempires.phases;

import com.starempires.TurnData;
import com.starempires.objects.Empire;
import com.starempires.objects.Order;
import com.starempires.objects.OrderType;
import com.starempires.objects.Ship;
import org.apache.commons.collections4.CollectionUtils;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
  * parameters:
  *  DEPLOY device1 device2 ...
  */
public class DeployDevicesPhaseUpdater extends PhaseUpdater {

    public DeployDevicesPhaseUpdater(final TurnData turnData) {
        super(Phase.DEPLOY_DEVICES, turnData);
    }

    @Override
    public void update() {
        final List<Order> orders = turnData.getOrders(OrderType.DEPLOY);
        for (final Order order: orders) {
            final List<String> deviceHandles = order.getParameters();
            for (final String deviceHandle: deviceHandles) {
                final Ship device = order.getEmpire().getShip(deviceHandle);
                if (device == null) {
                    addNewsResult(order, order.getEmpire(), "Unknown device %s".formatted(device));
                }
                else {
                    final Collection<Empire> newsEmpires = turnData.getEmpiresPresent(device);
                    final Set<Ship> starbases = turnData.getStarbases(device);
                    if (CollectionUtils.isEmpty(starbases)) {
                        turnData.deploy(device);
                        addNewsResult(order, newsEmpires,
                                "Device " + device + " deployed in sector " + device.getCoordinate());
                    } else {
                        addNewsResult(order, newsEmpires,
                                "Starbase " + starbases.stream().findFirst().orElseThrow()
                                        + " prevents activation of deployed device " + device + " in sector "
                                        + device.getCoordinate());
                    }
                    addNewsResult(order, newsEmpires, "Device " + device + " destroyed.");
                };
            }
        };
    }
}