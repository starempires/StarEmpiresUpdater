package com.starempires.orders;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.collect.Lists;
import com.starempires.objects.Coordinate;
import com.starempires.objects.Empire;
import com.starempires.objects.IdentifiableObject;
import com.starempires.objects.MappableObject;
import com.starempires.objects.Ship;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.apache.commons.lang3.ObjectUtils;

import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Getter
@SuperBuilder
public abstract class ShipBasedOrder extends Order {


    final static protected String COORDINATE_GROUP = "coordinate";
    final static protected String LOCATION_GROUP = "location";
    final static private String COORDINATE_EXCEPT_LIST_GROUP = "coordexcept";
    final static private String LOCATION_EXCEPT_LIST_GROUP = "locationexcept";
    final static private String SHIP_LIST_GROUP = "shiplist";

    final static protected String COORDINATE_REGEX = "(?<" + COORDINATE_GROUP + ">\\(?\\s*-?\\d+\\s*,\\s*-?\\d+\\s*\\)?)";
    final static private String COORDINATE_EXCEPT_REGEX = COORDINATE_REGEX + "(?:\\s+except\\s+(?<" + COORDINATE_EXCEPT_LIST_GROUP + ">\\w+(?:\\s+\\w+)*))?";
    final static protected String LOCATION_REGEX = "(?<" + LOCATION_GROUP + ">@\\w+)";
    final static protected String LOCATION_EXCEPT_REGEX = LOCATION_REGEX + "(?:\\s+except\\s+(?<" + LOCATION_EXCEPT_LIST_GROUP + ">\\w+(?:\\s+\\w+)*))?";
    final static private String SHIP_LIST_REGEX = "(?<" + SHIP_LIST_GROUP + ">\\w+(?:\\s+\\w+)*)";

    final static private String SHIP_GROUP_REGEX = COORDINATE_EXCEPT_REGEX + "|" + LOCATION_EXCEPT_REGEX + "|" + SHIP_LIST_REGEX;
    final static private Pattern SHIP_GROUP_PATTERN = Pattern.compile(SHIP_GROUP_REGEX, Pattern.CASE_INSENSITIVE);

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @JsonSerialize(using = IdentifiableObject.IdentifiableObjectCollectionSerializer.class)
    @JsonDeserialize(using = IdentifiableObject.DeferredIdentifiableObjectCollectionDeserializer.class)
    protected final List<Ship> ships = Lists.newArrayList();

    /**
     * Given a list of ship names, return the matching list of Ships.
     * @param empire
     * @param text
     * @return
     */
    protected static List<Ship> getShipsFromNames(final Empire empire, final String text, final Order order) {
        final String[] shipNames = text.split(" ");
        final List<Ship> ships = Lists.newArrayList();
        for (String shipName : shipNames) {
            final Ship ship = empire.getShip(shipName);
            if (ship == null) {
                order.addError(shipName, "Unknown ship");
            } else {
                ships.add(ship);
            }
        }
        return ships;
    }

    protected static MappableObject getMappableObjectFromName(Collection<? extends MappableObject> mappableObjects, final String name) {
        for (final MappableObject mappableObject : mappableObjects) {
            if (mappableObject.getName().equalsIgnoreCase(name)) {
                return mappableObject;
            }
        }
        return null;
    }

    protected static Coordinate getCoordinateFromMapObject(final Empire empire, final String name) {
        final MappableObject mappableObject = ObjectUtils.firstNonNull(
                getMappableObjectFromName(empire.getKnownWorlds(), name),
                getMappableObjectFromName(empire.getKnownPortals(), name),
                getMappableObjectFromName(empire.getKnownStorms(), name));
        if (mappableObject == null) {
            return null;
        }
        return mappableObject.getCoordinate();
    }

    /**
     * Get a list of Ships from a coordinate, a location, or a list of ships. Coordinate
     * or location can be followed by a list of ships to exclude.
     *
     * @param empire
     * @param text
     * @return
     */
    protected static List<Ship> getLocationShips(final Empire empire, final String text, final Order order) {
        final Matcher matcher = SHIP_GROUP_PATTERN.matcher(text);
        final List<Ship> locationShips = Lists.newArrayList();
        if (matcher.matches()) {
            final String coordText = matcher.group(COORDINATE_GROUP);
            String locationText = matcher.group(LOCATION_GROUP);
            final String shipListText = matcher.group(SHIP_LIST_GROUP);
            if (coordText != null) {
                final String exceptListText = matcher.group(COORDINATE_EXCEPT_LIST_GROUP);
                final Coordinate coordinate = Coordinate.parse(coordText);
                locationShips.addAll(empire.getShips(coordinate));
                final Collection<Ship> exceptShips = getShipsFromNames(empire, exceptListText, order);
                exceptShips.forEach(locationShips::remove);
            } else if (locationText != null) {
                locationText = locationText.replace("@", "");
                final Coordinate coordinate = getCoordinateFromMapObject(empire, locationText);
                if (coordinate == null) {
                    order.addError(locationText, "Unknown location");
                } else {
                    final String exceptListText = matcher.group(LOCATION_EXCEPT_LIST_GROUP);
                    locationShips.addAll(empire.getShips(coordinate));
                    final Collection<Ship> exceptShips = getShipsFromNames(empire, exceptListText, order);
                    exceptShips.forEach(locationShips::remove);
                }
            } else if (shipListText != null) {
                locationShips.addAll(getShipsFromNames(empire, shipListText, order));
            }
        } else {
            order.addError("Invalid ship group: " + text);
        }
        return locationShips;
    }

}