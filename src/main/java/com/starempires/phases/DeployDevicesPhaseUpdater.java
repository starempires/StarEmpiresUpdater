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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * parameters:
 * DEPLOY device1 device2 ...
 */
public class DeployDevicesPhaseUpdater extends PhaseUpdater {

    final private String DEVICES_GROUP = "devices";
    final private String PARAMETERS_REGEX = "^(<" + DEVICES_GROUP + ">)$";
    final private Pattern PATTERN = Pattern.compile(PARAMETERS_REGEX, Pattern.CASE_INSENSITIVE);

    public DeployDevicesPhaseUpdater(final TurnData turnData) {
        super(Phase.DEPLOY_DEVICES, turnData);
    }

    private void deployDevice(final Order order, final String deviceHandle) {
        final Ship device = order.getEmpire().getShip(deviceHandle);
        if (device == null) {
            addNewsResult(order, "Unknown device %s".formatted(device));
        } else {
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
        }
        ;
    }

    @Override
    public void update() {
        final List<Order> orders = turnData.getOrders(OrderType.DEPLOY);
        for (final Order order : orders) {
            final Matcher matcher = PATTERN.matcher(order.getParametersAsString());
            if (matcher.matches()) {
                final String deviceHandles = matcher.group(DEVICES_GROUP);
                for (String deviceHandle : deviceHandles.split(" ")) {
                    deployDevice(order, deviceHandle);
                }
            }
        }
        ;
    }
}