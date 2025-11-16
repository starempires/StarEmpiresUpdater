package com.starempires.updater;

import com.google.common.collect.Lists;
import com.starempires.TurnData;
import com.starempires.constants.Constants;
import com.starempires.objects.Coordinate;
import com.starempires.objects.Empire;
import com.starempires.objects.MappableObject;
import com.starempires.objects.Ship;
import com.starempires.objects.ShipClass;
import com.starempires.orders.AuthorizeOrder;
import com.starempires.orders.Order;
import com.starempires.orders.OrderType;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;

public class AuthorizeScanDataPhaseUpdater extends PhaseUpdater {

    public AuthorizeScanDataPhaseUpdater(final TurnData turnData) {
        super(Phase.AUTHORIZE_SCAN_ACCESS, turnData);
    }

    private void authorizeCoordinateData(final Order order, final List<Empire> recipients, final List<Coordinate> coordinates) {
        if (CollectionUtils.isNotEmpty(coordinates)) {
            final Empire empire = order.getEmpire();
            recipients.forEach(recipient -> {
                empire.addCoordinateScanAccess(recipient, coordinates);
                addNews(order, "You authorized empire " + recipient + " access to " +
                        plural(coordinates.size(), "coordinate") +
                        " of scan data");
            });
        }
    }

    private void authorizeMapObjectData(final Order order, final List<Empire> recipients, final List<MappableObject> mapObjects) {
        if (CollectionUtils.isNotEmpty(mapObjects)) {
            final Empire empire = order.getEmpire();
            recipients.forEach(recipient -> {
                empire.addObjectScanAccess(recipient, mapObjects);
                addNews(order, "You authorized empire " + recipient + " access to " +
                        plural(mapObjects.size(), "locations") +
                        " of scan data");
            });
        }
    }

    private void authorizeShipData(final Order order, final List<Empire> recipients, final List<Ship> ships) {
        if (CollectionUtils.isNotEmpty(ships)) {
            final Empire empire = order.getEmpire();
            final List<Ship> validShips = Lists.newArrayList();
            for (Ship ship : ships) {
                if (!ship.isAlive()) {
                    addNews(empire, "Ship %s is destroyed".formatted(ship));
                } else {
                    validShips.add(ship);
                }
            }

            recipients.forEach(recipient -> {
                empire.addShipScanAccess(recipient, validShips);
                addNews(order, "You have authorized empire " + recipient
                        + " access to scan data from " + plural(validShips.size(), "ship"));
            });
        }
    }

    private void authorizeShipClassData(final Order order, final List<Empire> recipients, final List<ShipClass> shipClasses) {
        if (CollectionUtils.isNotEmpty(shipClasses)) {
            final Empire empire = order.getEmpire();
            recipients.forEach(recipient -> {
                empire.addShipClassScanAccess(recipient, shipClasses);
                addNews(order,
                        "You have authorized empire " + recipient + " access to scan data from "
                                + plural(shipClasses.size(), "ship class", Constants.SUFFIX_ES));
            });
        }
    }

    private void authorizeAllData(final Order order, final List<Empire> recipients) {
        final Empire empire = order.getEmpire();
        recipients.forEach(recipient -> {
            empire.addEmpireScanAccess(recipient);
            addNews(order, "You have authorized empire " + recipient + " access to all scan data");
        });
    }

    @Override
    public void update() {
        final List<Order> orders = turnData.getOrders(OrderType.AUTHORIZE);
        orders.forEach(o -> {
            final AuthorizeOrder order = (AuthorizeOrder)o;
            final List<Empire> recipients = order.getRecipients();
            if (order.isAllSectors()) {
                authorizeAllData(order, recipients);
            }
            else {
                authorizeCoordinateData(order, recipients, order.getCoordinates());
                authorizeMapObjectData(order, recipients, order.getMapObjects());
                authorizeShipData(order, recipients, order.getShips());
            }
        });
    }
}