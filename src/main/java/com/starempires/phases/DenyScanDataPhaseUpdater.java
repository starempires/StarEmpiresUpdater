package com.starempires.phases;

import com.starempires.TurnData;
import com.starempires.constants.Constants;
import com.starempires.objects.Empire;
import com.starempires.objects.Order;
import com.starempires.objects.OrderType;
import com.starempires.objects.RadialCoordinate;
import com.starempires.objects.Ship;
import com.starempires.objects.ShipClass;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class DenyScanDataPhaseUpdater extends PhaseUpdater {

    public DenyScanDataPhaseUpdater(final TurnData turnData) {
        super(Phase.DENY_SCAN_ACCESS, turnData);
    }

    void denySectorData(final Order order, final List<Empire> recipients, final List<String> sectors) {
        final Empire empire = order.getEmpire();
        final List<RadialCoordinate> coordinates = sectors.stream().map(sector -> {
            final RadialCoordinate coordinate = RadialCoordinate.parseRadial(sector);
            if (coordinate == null) {
                addNewsResult(order, empire, "Unknown coordinate " + sector);
                return null;
            }
            return coordinate;
        }).filter(Objects::nonNull).collect(Collectors.toList());
        recipients.forEach(recipient -> {
            empire.removeCoordinateScanAccess(recipient, coordinates);
            addNewsResult(order, empire, "You have denied empire " + recipient + " access to "
                    + plural(coordinates.size(), "sector") + " of scan data");
        });
    }

    void denyShipData(final Order order, final List<Empire> recipients, final List<String> shipHandles) {
        final Empire empire = order.getEmpire();
        final List<Ship> ships = empire.getShips(shipHandles);

        recipients.forEach(recipient -> {
            empire.removeShipScanAccess(recipient, ships);
            addNewsResult(order, empire, "You have denied empire " + recipient
                    + " access to scan data from " + plural(ships.size(), "ship"));
        });
    }

    void denyShipClassData(final Order order, final List<Empire> recipients, final List<String> shipClassNames) {
        final Empire empire = order.getEmpire();
        final List<ShipClass> shipClasses = turnData.getShipClasses(empire, shipClassNames);

        recipients.forEach(recipient -> {
            empire.removeShipClassScanAccess(recipient, shipClasses);
            addNewsResult(order, empire,
                    "You have denied empire " + recipient + " access to scan data from "
                            + plural(shipClasses.size(), "ship class", Constants.SUFFIX_ES));
        });
    }

    void denyAllData(final Order order, final List<Empire> recipients) {
        final Empire empire = order.getEmpire();
        recipients.forEach(recipient -> {
            empire.removeEmpireScanAccess(recipient);
            addNewsResult(order, empire,
                    "You have denied empire " + recipient + " access to all scan data");
        });
    }

    @Override
    public void update() {
        final List<Order> orders = turnData.getOrders(OrderType.DENY);
        orders.forEach(order -> {
            final Empire empire = order.getEmpire();
            final int index = order.indexOfIgnoreCase(Constants.TOKEN_FROM);
            final String denyType = order.getStringParameter(0);
            final List<String> recipientNames = order.getParameterSubList(index + 1);
            final List<Empire> recipients = recipientNames.stream()
                    .map(recipientName -> {
                        Empire recipient = turnData.getEmpire(recipientName);
                        if (recipient == null || !empire.isKnownEmpire(recipient)) {
                            addNewsResult(order, empire, "Unknown empire " + recipientName);
                            return null;
                        }
                        return recipient;
                    }).filter(Objects::nonNull).collect(Collectors.toList());

            if (denyType.equalsIgnoreCase(Constants.TOKEN_SECTOR)) {
                final List<String> sectors = order.getParameterSubList(1, index);
                denySectorData(order, recipients, sectors);
            }
            else if (denyType.equalsIgnoreCase(Constants.TOKEN_SHIP)) {
                final List<String> shipHandles = order.getParameterSubList(1, index);
                denyShipData(order, recipients, shipHandles);
            }
            else if (denyType.equalsIgnoreCase(Constants.TOKEN_CLASS)) {
                final List<String> shipClassNames = order.getParameterSubList(1, index);
                denyShipClassData(order, recipients, shipClassNames);
            }
            else if (denyType.equalsIgnoreCase(Constants.TOKEN_ALLDATA)) {
                denyAllData(order, recipients);
            }
        });
    }
}