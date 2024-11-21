package com.starempires.phases;

import com.google.common.collect.Lists;
import com.starempires.TurnData;
import com.starempires.objects.Coordinate;
import com.starempires.objects.Empire;
import com.starempires.orders.Order;
import com.starempires.orders.OrderType;
import com.starempires.objects.Ship;
import lombok.NonNull;

import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MoveShipsPhaseUpdater extends PhaseUpdater {

    final static private String DESTINATION_GROUP = "destination";
    final static private String SHIPS_GROUP = "ships";
    final static private String COORDINATE_GROUP = "coordinate";
    final static private String LOCATION_GROUP = "location";

    final static private String COORDINATE_PARAMETERS_REGEX = "^move\\s+(?<" + COORDINATE_GROUP + ">\\(?-?[0-9]+,\\s*-?[0-9]+\\)?)\\s+to";
    final static private String LOCATION_PARAMETERS_REGEX = "^move\\s+@(?<" + LOCATION_GROUP + ">\\w+)\\s+to";
    final static private String SHIPS_PARAMETERS_REGEX = "^move\\s+(?<" + SHIPS_GROUP + ">[\\w]+(?:\\s+[\\w]+)*)\\s+to";

    final static private String COORDINATE_DESTINATION_REGEX = "to\\s+(?<" + DESTINATION_GROUP + ">>\\(?-?[0-9]+,\\s*-?[0-9]+\\)?)\\s*$";
    final static private String LOCATION_DESTINATION_REGEX = "to\\s+(?<" + DESTINATION_GROUP + ">[\\w]+)$";

    final static private Pattern COORDINATE_PATTERN = Pattern.compile(COORDINATE_PARAMETERS_REGEX, Pattern.CASE_INSENSITIVE);
    final static private Pattern LOCATION_PATTERN = Pattern.compile(LOCATION_PARAMETERS_REGEX, Pattern.CASE_INSENSITIVE);
    final static private Pattern SHIPS_PATTERN = Pattern.compile(SHIPS_PARAMETERS_REGEX, Pattern.CASE_INSENSITIVE);

    final static private Pattern COORDINATE_DESTINATION_PATTERN = Pattern.compile(COORDINATE_DESTINATION_REGEX, Pattern.CASE_INSENSITIVE);
    final static private Pattern LOCATION_DESTINATION_PATTERN = Pattern.compile(LOCATION_DESTINATION_REGEX, Pattern.CASE_INSENSITIVE);

    public MoveShipsPhaseUpdater(TurnData turnData) {
        super(Phase.MOVE_SHIPS, turnData);
    }

    private List<Ship> getValidMovers(@NonNull final Order order) {
        // parse the order according to its format
        final Matcher coordinateMatcher = COORDINATE_PATTERN.matcher(order.getParametersAsString());
        final Matcher locationMatcher = LOCATION_PATTERN.matcher(order.getParametersAsString());
        final Matcher shipsMatcher = SHIPS_PATTERN.matcher(order.getParametersAsString());

        final List<Ship> validMovers = Lists.newArrayList();
        final Empire empire = order.getEmpire();
        if (coordinateMatcher.matches()) {
            String coordinateText = coordinateMatcher.group(COORDINATE_GROUP);
            Coordinate coordinate = Coordinate.parse(coordinateText);
            validMovers.addAll(empire.getLiveShips(coordinate));
        } else if (locationMatcher.matches()) {
            String location = locationMatcher.group(LOCATION_GROUP);
            Coordinate coordinate = getCoordinateFromLocation(location);
            if (location == null) {
                addNewsResult(order, "Unknown location %s".formatted(location));
            } else {
                validMovers.addAll(empire.getLiveShips(coordinate));
            }
        } else if (shipsMatcher.matches()) {
            final String moverNames = shipsMatcher.group(SHIPS_GROUP);
            for (String moverName : moverNames.split(" ")) {
                Ship ship = empire.getShip(moverName);
                if (ship == null) {
                    order.addResult("Omitting unknown ship %s".formatted(moverName));
                } else if (!ship.isAlive()) {
                    addNewsResult(order, "Ship " + ship + " is destroyed");
                } else {
                    validMovers.add(ship);
                }
            }
        }
        return validMovers;
    }

    private String getDestinationText(@NonNull final Order order) {
        final Matcher coordinateDestinationMatcher = COORDINATE_DESTINATION_PATTERN.matcher(order.getParametersAsString());
        final Matcher locationDestinationMatcher = LOCATION_DESTINATION_PATTERN.matcher(order.getParametersAsString());
        if (coordinateDestinationMatcher.matches()) {
            return coordinateDestinationMatcher.group(DESTINATION_GROUP);
        }
        else if (locationDestinationMatcher.matches()) {
            return locationDestinationMatcher.group(DESTINATION_GROUP);
        }
        else {
            return null;
        }
    }

    private List<Ship> gatherValidMovers(@NonNull Order order, List<Ship> possibleMovers) {
        final List<Ship> validMovers = Lists.newArrayList();
        for (final Ship mover: possibleMovers) {
            if (!mover.isAlive()) {
                order.addResult("Omitting destroyed ship %s".formatted(mover));
            }
            else if (mover.isLoaded()) {
                order.addResult("Omitting loaded ship %s".formatted(mover));
            }
            else if (mover.getGunsActuallyFired() > 0) {
                order.addResult("Omitting attacking ship %s".formatted(mover));
            }
            else {
                validMovers.add(mover);
            }
        }
        return validMovers;
    }

    @Override
    public void update() {
        final List<Order> orders = turnData.getOrders(OrderType.MOVE);
        orders.forEach(order -> {
            final Empire empire = order.getEmpire();
            final List<Ship> validMovers = getValidMovers(order);
            if (validMovers.isEmpty()) {
                order.addResult("No valid movers found");
                return;
            }
            final String destinationText = getDestinationText(order);
            if (destinationText == null) {
                order.addResult("No valid destination found");
                return;
            }

            final Coordinate destination = getCoordinateFromLocation(destinationText);
            validMovers.forEach(mover -> {
                 final int availableEngines = mover.getAvailableEngines();
                 final int distance = mover.distanceTo(destination);
                 if (distance <= availableEngines) {
                     final Collection<Empire> newsEmpires = turnData.getEmpiresPresent(mover);
                     newsEmpires.remove(empire);
                     addNewsResult(order, empire, "Ship " + mover + " moved to destination " + destination);
                        addNews(newsEmpires, "Ship " + mover + " moved out of sector " + mover.getCoordinate());
                        empire.moveShip(mover, destination);
                    }
                    else {
                        addNewsResult(order, empire, "Ship " + mover + " has insufficient operational engines (max move "
                                + availableEngines + ") to reach destination (distance " + distance + ")");
                    }
            });
        });
    }
}