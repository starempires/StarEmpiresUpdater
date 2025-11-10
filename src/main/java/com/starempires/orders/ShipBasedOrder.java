package com.starempires.orders;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.collect.Lists;
import com.starempires.TurnData;
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

@Getter
@SuperBuilder
public abstract class ShipBasedOrder extends Order {

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @JsonSerialize(using = IdentifiableObject.IdentifiableObjectCollectionSerializer.class)
    @JsonDeserialize(using = IdentifiableObject.DeferredIdentifiableObjectCollectionDeserializer.class)
    protected final List<Ship> ships;

    /**
     * Given a list of ship names, return the matching list of Ships.
     * @param empire
     * @param text
     * @return
     */
    protected static List<Ship> getShipsFromNames(final Empire empire, final String text, final Order order) {
        final List<Ship> ships = Lists.newArrayList();
        if (text != null) {
            final String[] shipNames = text.split(SPACE_REGEX);
            for (String shipName : shipNames) {
                final Ship ship = empire.getShip(shipName);
                if (ship == null) {
                    order.addError(shipName, "Unknown ship");
                } else if (!ship.isAlive()) {
                    order.addError(ship, "Ship is destroyed");
                } else {
                    ships.add(ship);
                }
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
     * @param matcher
     * @param order
     * @return
     */
    protected static List<Ship> getLocationShips(final Empire empire, final Matcher matcher, final Order order) {
        final List<Ship> locationShips = Lists.newArrayList();
        if (matcher.matches()) {
            final String coordText = matcher.group(COORDINATE_GROUP);
            String locationText = matcher.group(SHIP_LOCATION_GROUP);
            final String shipListText = matcher.group(OBJECT_LIST_GROUP);
            if (coordText != null) {
                final String exceptListText = matcher.group(COORDINATE_EXCEPT_LIST_GROUP);
                final Coordinate localCoordinate = Coordinate.parse(coordText);
                final Coordinate galacticCoordinate = empire.toGalactic(localCoordinate);
                locationShips.addAll(empire.getShips(galacticCoordinate));
                locationShips.removeIf(Ship::isLoaded);
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
                    locationShips.removeIf(Ship::isLoaded);
                    final Collection<Ship> exceptShips = getShipsFromNames(empire, exceptListText, order);
                    exceptShips.forEach(locationShips::remove);
                }
            } else if (shipListText != null) {
                locationShips.addAll(getShipsFromNames(empire, shipListText, order));
            }
        }
        return locationShips;
    }

    protected static void parseReady(final JsonNode node, final TurnData turnData, final OrderType orderType, final ShipBasedOrder.ShipBasedOrderBuilder<?, ?> builder) {
        Order.parseReady(node, turnData, orderType, builder);
        final JsonNode shipsNode = node.get("ships");
        final Empire empire = turnData.getEmpire(node.get("empire").asText());
        builder.ships(getTurnDataListFromJsonNode(shipsNode, empire::getShip));
    }
}