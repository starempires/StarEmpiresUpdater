package com.starempires.orders;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.collect.Lists;
import com.starempires.TurnData;
import com.starempires.objects.Coordinate;
import com.starempires.objects.Empire;
import com.starempires.objects.IdentifiableObject;
import com.starempires.objects.MappableObject;
import com.starempires.objects.Ship;
import lombok.Data;
import lombok.experimental.SuperBuilder;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Data
@SuperBuilder
public abstract class Order {

    final static private String COORDINATE_GROUP = "coordinate";
    final static private String LOCATION_GROUP = "location";
    final static private String COORDINATE_EXCEPT_LIST_GROUP = "coordexcept";
    final static private String LOCATION_EXCEPT_LIST_GROUP = "locationexcept";
    final static private String SHIP_LIST_GROUP = "shiplist";

    final static private String COORDINATE_REGEX = "(?<" + COORDINATE_GROUP + ">\\(?\\s*-?\\d+\\s*,\\s*-?\\d+\\s*\\)?)(?:\\s+except\\s+(?<" + COORDINATE_EXCEPT_LIST_GROUP + ">\\w+(?:\\s+\\w+)*))?";
    final static private String LOCATION_REGEX = "(?<" + LOCATION_GROUP + ">@\\w+)(?:\\\\s+except\\\\s+(?<" + LOCATION_EXCEPT_LIST_GROUP + ">\\\\w+(?:\\\\s+\\\\w+)*))?";
    final static private String SHIP_LIST_REGEX = "(?<" + SHIP_LIST_GROUP + ">\\w+(?:\\s+\\w+)*)";

    final static private String SHIP_GROUP_REGEX = COORDINATE_REGEX + "|" + LOCATION_REGEX + "|" + SHIP_LIST_REGEX;
    final static private Pattern SHIP_GROUP_PATTERN = Pattern.compile(SHIP_GROUP_REGEX, Pattern.CASE_INSENSITIVE);

    /** empire who gave this order */
    @JsonSerialize(using = IdentifiableObject.IdentifiableObjectSerializer.class)
    @JsonDeserialize(using = IdentifiableObject.DeferredIdentifiableObjectDeserializer.class)
    protected final Empire empire;
    protected final String parameters;
    /** text results from processing this order */
    @JsonIgnore
    protected final List<String> results = Lists.newArrayList();
    /** was this order generated "synthetically", i.e., by the updater */
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    protected final boolean synthetic;
    protected final OrderType orderType;
    protected boolean ready = true;

    public void addResult(final String text) {
        results.add(text);
    }

    public void addWarning(final IdentifiableObject object, final String text) {
        results.add("%s: Warning: %s".formatted(object, text));
    }

    public void addError(final Object object, final String text) {
        results.add("%s: Error: %s".formatted(object, text));
    }

    public void addError(final String text) {
        results.add("Error: %s".formatted(text));
    }

    public void addOKResult(final Object object) {
        results.add("%s: OK".formatted(object));
    }

    @JsonIgnore
    public String getResultText() {
        return StringUtils.join("\\n", results);
    }

    //TODO remove
    @JsonIgnore
    public String getParametersAsString() {
        return parameters;
    }

    public String toString() {
        return orderType.name() + " " + parameters;
    }

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
            final String locationText = matcher.group(LOCATION_GROUP);
            final String shipListText = matcher.group(SHIP_LIST_GROUP);
            if (coordText != null) {
                final String exceptListText = matcher.group(COORDINATE_EXCEPT_LIST_GROUP);
                final Coordinate coordinate = Coordinate.parse(coordText);
                locationShips.addAll(empire.getShips(coordinate));
                final Collection<Ship> exceptShips = getShipsFromNames(empire, exceptListText, order);
                exceptShips.forEach(locationShips::remove);
            } else if (locationText != null) {
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

    public static Order parse(final TurnData turnData, final Empire empire, final String parameters) {
        throw new UnsupportedOperationException("Subclasses must implement this method");
    }
}