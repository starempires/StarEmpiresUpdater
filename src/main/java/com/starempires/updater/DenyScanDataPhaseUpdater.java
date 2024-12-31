package com.starempires.updater;

import com.starempires.TurnData;
import com.starempires.constants.Constants;
import com.starempires.objects.Empire;
import com.starempires.objects.RadialCoordinate;
import com.starempires.objects.Ship;
import com.starempires.objects.ShipClass;
import com.starempires.orders.Order;
import com.starempires.orders.OrderType;

import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class DenyScanDataPhaseUpdater extends PhaseUpdater {

    // deny (oblique, y, radius) from empire1 [empire2...]
    // deny {ship1|@ship-class1} [{ship2|@ship-class2}...] from empire1 [empire2...]
    // deny all from empire1 [empire2...]
    private static final String COORDINATE_GROUP = "coordinate";
    private static final String ITEMS_GROUP = "items";
    private static final String EMPIRES_GROUP = "empires";

    private static final String DENY_SECTOR_REGEX = "^deny\\s(?<" + COORDINATE_GROUP + ">\\(??-[0-9]+\\s*,\\s*-?[0-9]+\\s*,[0-9]+\\s*\\)?)\\s+from\\s+(?<" + EMPIRES_GROUP + ">[\\w]+(?:\\s+[\\w]+)*)\\s*$";
    private static final String DENY_ITEMS_REGEX = "^deny\\s(?<"+ ITEMS_GROUP + ">@?\\w+(?:\\s+@?\\w+)*)\\s+from\\s+(?<" + EMPIRES_GROUP + ">[\\w]+(?:\\s+[\\w]+)*)\\s*$";

    private static final Pattern DENY_SECTOR_PATTERN = Pattern.compile(DENY_SECTOR_REGEX, Pattern.CASE_INSENSITIVE);
    private static final Pattern DENY_ITEMS_PATTERN = Pattern.compile(DENY_ITEMS_REGEX, Pattern.CASE_INSENSITIVE);

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
            addNewsResult(order, "You have denied empire " + recipient + " access to "
                    + plural(coordinates.size(), "sector") + " of scan data");
        });
    }

    void denyShipData(final Order order, final List<Empire> recipients, final List<String> shipHandles) {
        final Empire empire = order.getEmpire();
        final List<Ship> ships = empire.getShips(shipHandles);

        recipients.forEach(recipient -> {
            empire.removeShipScanAccess(recipient, ships);
            addNewsResult(order, "You have denied empire " + recipient
                    + " access to scan data from " + plural(ships.size(), "ship"));
        });
    }

    void denyShipClassData(final Order order, final List<Empire> recipients, final List<String> shipClassNames) {
        final Empire empire = order.getEmpire();
        final List<ShipClass> shipClasses = turnData.getShipClasses(empire, shipClassNames);

        recipients.forEach(recipient -> {
            empire.removeShipClassScanAccess(recipient, shipClasses);
            addNewsResult(order, "You have denied empire " + recipient + " access to scan data from "
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
//            final Empire empire = order.getEmpire();
//            final Matcher coordinateMatcher = DENY_SECTOR_PATTERN.matcher(order.getParametersAsString());
//            final Matcher itemsMatcher = DENY_ITEMS_PATTERN.matcher(order.getParametersAsString());
//            final List<String> recipientNames = Lists.newArrayList();
//
//            if (coordinateMatcher.matches()) {
//                final String coordinateString = coordinateMatcher.group(COORDINATE_GROUP);
//                final RadialCoordinate coordinate = RadialCoordinate.parseRadial(coordinateString);
//                recipientNames.addAll(Arrays.asList(coordinateMatcher.group(EMPIRES_GROUP).split(" ")));
//                recipients.forEach(recipient -> {
//                    empire.denyCoordinateScanAccess(recipient, coordinate, radius);
//                    addNewsResult(order, "You have denied empire " + recipient
//                            + " access to scan data from sector " + coordinateString + " of radius " + radius);
//                });
//                return;
//            }
//            else if (itemsMatcher.matches()) {
//            }
//
//            final List<Empire> recipients = recipientNames.stream()
//                    .map(recipientName -> {
//                        final Empire recipient = turnData.getEmpire(recipientName);
//                        if (recipient == null || !empire.isKnownEmpire(recipient)) {
//                            addNewsResult(order, empire, "Unknown empire " + recipientName);
//                            return null;
//                        }
//                        return recipient;
//                    }).filter(Objects::nonNull).collect(Collectors.toList());
//            final String denyType = order.getStringParameter(0);
//            final List<String> recipientNames = order.getParameterSubList(index + 1);
//            final List<Empire> recipients = recipientNames.stream()
//                    .map(recipientName -> {
//                        Empire recipient = turnData.getEmpire(recipientName);
//                        if (recipient == null || !empire.isKnownEmpire(recipient)) {
//                            addNewsResult(order, empire, "Unknown empire " + recipientName);
//                            return null;
//                        }
//                        return recipient;
//                    }).filter(Objects::nonNull).collect(Collectors.toList());
//
//            if (denyType.equalsIgnoreCase(Constants.TOKEN_SECTOR)) {
//                final List<String> sectors = order.getParameterSubList(1, index);
//                denySectorData(order, recipients, sectors);
//            }
//            else if (denyType.equalsIgnoreCase(Constants.TOKEN_SHIP)) {
//                final List<String> shipHandles = order.getParameterSubList(1, index);
//                denyShipData(order, recipients, shipHandles);
//            }
//            else if (denyType.equalsIgnoreCase(Constants.TOKEN_CLASS)) {
//                final List<String> shipClassNames = order.getParameterSubList(1, index);
//                denyShipClassData(order, recipients, shipClassNames);
//            }
//            else if (denyType.equalsIgnoreCase(Constants.TOKEN_ALLDATA)) {
//                denyAllData(order, recipients);
//            }
        });
    }
}