package com.starempires.orders;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

@Data
@SuperBuilder
@NoArgsConstructor
@Log4j2
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public abstract class Order {

    // base tokens
    final static protected String MAX_TOKEN = "max";
    final static protected String TO_TOKEN = "to";
    final static protected String FROM_TOKEN = "from";
    final static protected String ONTO_TOKEN = "onto";
    final static protected String AT_TOKEN = "at";
    final static protected String THROUGH_TOKEN = "through";
    final static protected String EXCEPT_TOKEN = "except";
    final static protected String ALL_TOKEN = "all";

    // object regexes
    final static protected String SPACE_REGEX = "\\s+";
    final static protected String ID_REGEX = "\\w+";
    final static protected String INT_REGEX = "\\d+";
    final static protected String INT_OR_MAX_REGEX = INT_REGEX + "|" + MAX_TOKEN;
    final static protected String COORDINATE_REGEX = "\\(?\\s*-?\\d+\\s*,\\s*-?\\d+\\s*\\)?";
    final static protected String ID_LIST_REGEX = ID_REGEX + "(?:" + SPACE_REGEX + ID_REGEX + ")*";
    final static protected String OBJECT_TYPE_REGEX = "world|portal|storm";
    final static protected String TOGGLE_MODE_REGEX = "public|private";
    final static protected String DESIGN_PARAMETERS_REGEX = "[\\d\\s]+";
    final static protected String TARGET_ORDER_REGEX = "asc|desc";

    // capture groups
    final static protected String SHIP_GROUP = "ship";
    final static protected String TARGET_ORDER_GROUP = "targetorder";
    final static protected String SHIP_LOCATION_GROUP = "location";
    final static protected String COORDINATE_EXCEPT_LIST_GROUP = "coordexcept";
    final static protected String LOCATION_EXCEPT_LIST_GROUP = "locationexcept";
    final static protected String DESTINATION_COORDINATE_GROUP = "destcoordinate";
    final static protected String DESTINATION_LOCATION_GROUP = "destlocation";
    final static protected String COORDINATE_GROUP = "coordinate";
    final static protected String HULLTYPE_GROUP = "hulltype";
    final static protected String DESIGN_PARAMETERS_GROUP = "parameters";
    final static protected String TOGGLE_MODE_GROUP = "mode";
    final static protected String DESTINATION_GROUP = "destination";
    final static protected String COUNT_GROUP = "count";
    final static protected String RATING_GROUP = "rating";
    final static protected String PRODUCTION_GROUP = "production";
    final static protected String STOCKPILE_GROUP = "stockpile";
    final static protected String OBJECT_TYPE_GROUP = "type";
    final static protected String WORLD_GROUP = "world";
    final static protected String RADIUS_GROUP = "radius";
    final static protected String ID_GROUP = "id";
    final static protected String AMOUNT_GROUP = "amount";
    final static protected String RECIPIENT_LIST_GROUP = "recipientlist";
    final static protected String OBJECT_LIST_GROUP = "objectlist";
    final static protected String OWNER_GROUP = "owner";
    final static protected String SHIP_NAMES_GROUP = "shipnames";
    final static protected String ENTRY_GROUP = "entry";
    final static protected String EXIT_GROUP = "exit";
    final static protected String SHIP_CLASS_GROUP = "shipclass";

    // named ID_REGEX capture groups
    final static protected String SHIP_CAPTURE_REGEX = regexWithCaptureGroup(SHIP_GROUP, ID_REGEX);
    final static protected String SHIP_CLASS_CAPTURE_REGEX = regexWithCaptureGroup(SHIP_CLASS_GROUP, ID_REGEX);
    final static protected String ENTRY_CAPTURE_REGEX = regexWithCaptureGroup(ENTRY_GROUP, ID_REGEX);
    final static protected String EXIT_CAPTURE_REGEX = regexWithCaptureGroup(EXIT_GROUP, ID_REGEX);
    final static protected String HULLTYPE_CAPTURE_REGEX = regexWithCaptureGroup(HULLTYPE_GROUP, ID_REGEX);
    final static protected String DESTINATION_CAPTURE_REGEX = regexWithCaptureGroup(DESTINATION_GROUP, ID_REGEX);
    final static protected String ID_CAPTURE_REGEX = regexWithCaptureGroup(ID_GROUP, ID_REGEX);
    final static protected String OWNER_CAPTURE_REGEX = regexWithCaptureGroup(OWNER_GROUP, ID_REGEX);
    final static protected String DESTINATION_LOCATION_CAPTURE_REGEX = regexWithCaptureGroup(DESTINATION_LOCATION_GROUP, ID_REGEX);

    // named ID_LIST_REGEX capture groups
    final static protected String RECIPIENT_LIST_CAPTURE_REGEX = regexWithCaptureGroup(RECIPIENT_LIST_GROUP, ID_LIST_REGEX);
    final static protected String OBJECT_LIST_CAPTURE_REGEX = regexWithCaptureGroup(OBJECT_LIST_GROUP, ID_LIST_REGEX);

    // named INT_REGEX capture groups
    final static protected String COUNT_CAPTURE_REGEX = regexWithCaptureGroup(COUNT_GROUP, INT_REGEX);
    final static protected String RATING_CAPTURE_REGEX = regexWithCaptureGroup(RATING_GROUP, INT_REGEX);
    final static protected String PRODUCTION_CAPTURE_REGEX = regexWithCaptureGroup(PRODUCTION_GROUP, INT_REGEX);
    final static protected String STOCKPILE_CAPTURE_REGEX = regexWithCaptureGroup(STOCKPILE_GROUP, INT_REGEX);
    final static protected String RADIUS_CAPTURE_REGEX = regexWithCaptureGroup(RADIUS_GROUP, INT_REGEX);

    // optional capture regexes
    final static protected String OPTIONAL_TARGET_ORDER_CAPTURE_REGEX = "(?:" + regexWithCaptureGroup(TARGET_ORDER_GROUP, TARGET_ORDER_REGEX) + SPACE_REGEX + ")?";
    final static protected String OPTIONAL_OWNER_CAPTURE_REGEX = "(?:" + SPACE_REGEX + OWNER_CAPTURE_REGEX + ")?";
    final static protected String OPTIONAL_EXIT_CAPTURE_REGEX = "(?:" + SPACE_REGEX + EXIT_CAPTURE_REGEX + ")?";
    final static protected String OPTIONAL_COORDINATE_EXCEPT_CAPTURE_REGEX = "(?:" + SPACE_REGEX + EXCEPT_TOKEN + SPACE_REGEX +
            regexWithCaptureGroup(COORDINATE_EXCEPT_LIST_GROUP, ID_LIST_REGEX) + ")?";
    final static protected String OPTIONAL_LOCATION_EXCEPT_CAPTURE_REGEX = "(?:"+ SPACE_REGEX + EXCEPT_TOKEN + SPACE_REGEX +
            regexWithCaptureGroup(LOCATION_EXCEPT_LIST_GROUP, ID_LIST_REGEX) + ")?";

    // special capture regexes
    final static protected String COORDINATE_CAPTURE_REGEX = regexWithCaptureGroup(COORDINATE_GROUP, COORDINATE_REGEX);
    final static protected String DESTINATION_COORDINATE_CAPTURE_REGEX = regexWithCaptureGroup(DESTINATION_COORDINATE_GROUP, COORDINATE_REGEX);
    final static protected String AMOUNT_CAPTURE_REGEX = regexWithCaptureGroup(AMOUNT_GROUP, INT_OR_MAX_REGEX);
    final static protected String OBJECT_TYPE_CAPTURE_REGEX = regexWithCaptureGroup(OBJECT_TYPE_GROUP, OBJECT_TYPE_REGEX);
    final static protected String TOGGLE_MODE_CAPTURE_REGEX = regexWithCaptureGroup(TOGGLE_MODE_GROUP, TOGGLE_MODE_REGEX);
    final static protected String SHIP_PARAMS_CAPTURE_REGEX = regexWithCaptureGroup(DESIGN_PARAMETERS_GROUP, DESIGN_PARAMETERS_REGEX);

    final static protected String OBJECT_LIST_EXCEPT_CAPTURE_REGEX = "(?:" + SPACE_REGEX + EXCEPT_TOKEN + SPACE_REGEX + OBJECT_LIST_CAPTURE_REGEX + ")?";
    final static protected String SHIP_LOCATION_CAPTURE_REGEX = "(?<" + SHIP_LOCATION_GROUP + ">@" + ID_REGEX + ")";
    final static protected String COORDINATE_CAPTURE_EXCEPT_REGEX = COORDINATE_CAPTURE_REGEX + OPTIONAL_COORDINATE_EXCEPT_CAPTURE_REGEX;
    final static protected String SHIP_LOCATION_EXCEPT_CAPTURE_REGEX = SHIP_LOCATION_CAPTURE_REGEX + OPTIONAL_LOCATION_EXCEPT_CAPTURE_REGEX;

    final static protected String LOCATION_OR_SHIP_LIST_CAPTURE_REGEX = "(" + COORDINATE_CAPTURE_EXCEPT_REGEX + "|" +
                                                                              SHIP_LOCATION_EXCEPT_CAPTURE_REGEX + "|" +
                                                                              OBJECT_LIST_CAPTURE_REGEX + ")";

    final static protected String SHIP_NAMES_CAPTURE_REGEX = "(?<" + SHIP_NAMES_GROUP + ">(" + ID_LIST_REGEX + "|" + ID_REGEX + "\\*))";



    protected static String regexWithCaptureGroup(final String name, final String regex) {
        return "(?<" + name + ">" + regex + ")";
    }

    /** empire who gave this order */
    @JsonSerialize(using = IdentifiableObject.IdentifiableObjectSerializer.class)
    @JsonDeserialize(using = IdentifiableObject.DeferredIdentifiableObjectDeserializer.class)
    protected Empire empire;
    protected String parameters;
    /** text results from processing this order */
    @JsonIgnore
    protected final List<String> results = Lists.newArrayList();
    /** was this order generated "synthetically", i.e., by the updater */
    protected boolean synthetic;
    protected OrderType orderType;
    @JsonIgnore
    protected boolean ready;
    protected boolean gmOnly;

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

    //TODO remove
    @JsonIgnore
    public String getParametersAsString() {
        return parameters;
    }

    public String toString() {
        return orderType.name() + " " + parameters;
    }

    public static Order parse(final TurnData turnData, final Empire empire, final String parameters) {
        throw new UnsupportedOperationException("Subclasses must implement this method");
    }

    protected static String getString(final JsonNode node, final String fieldName) {
        final JsonNode valueNode = node.get(fieldName);
        return valueNode == null ? null : valueNode.asText();
    }

    protected static int getInt(final JsonNode node, final String fieldName) {
        final JsonNode valueNode = node.get(fieldName);
        return valueNode == null ? 0 : valueNode.asInt();
    }

    protected static boolean getBoolean(final JsonNode node, final String fieldName) {
        final JsonNode valueNode = node.get(fieldName);
        return valueNode != null && valueNode.asBoolean();
    }

    private static List<String> getStringListFromValueNode(final JsonNode node) {
        final List<String> values = new ArrayList<>();
        if (node != null && node.isArray()) {
            for (JsonNode value : node) {
                values.add(value.asText());
            }
        }
        return values;
    }

    protected static List<String> getStringList(final JsonNode node, final String fieldName) {
        final JsonNode valueNode = node.get(fieldName);
        return getStringListFromValueNode(valueNode);
    }

    protected static <T extends IdentifiableObject> List<T> getTurnDataListFromJsonNode(final JsonNode node, final Function<String, T> lookupMethod) {
        final List<String> names = getStringListFromValueNode(node);
        final List<T> objects = new ArrayList<>();
        names.forEach(name -> {
            final T obj = lookupMethod.apply(name);
            if (obj == null) {
                log.error("Unknown object {}", name);
            } else {
                objects.add(obj);
            }
        });
        return objects;
    }

    protected static <T extends IdentifiableObject> T getTurnDataItemFromJsonNode(final JsonNode jsonNode, final Function<String, T> lookupMethod) {
        if (jsonNode != null) {
            final String name = jsonNode.asText();
            return lookupMethod.apply(name);
        }
        return null;
    }

    protected static Coordinate getCoordinateFromJsonNode(final JsonNode node) {
        if (node == null) {
            return null;
        }
        return new Coordinate(getInt(node, "oblique"), getInt(node, "y"));
    }

    protected static MappableObject getMappableObjectFromName(Collection<? extends MappableObject> mappableObjects, final String name) {
        for (final MappableObject mappableObject : mappableObjects) {
            if (mappableObject.getName().equalsIgnoreCase(name)) {
                return mappableObject;
            }
        }
        return null;
    }

    protected static MappableObject getKnownMappableObjectFromName(final Empire empire, final String name) {
        return ObjectUtils.firstNonNull(
                getMappableObjectFromName(empire.getKnownWorlds(), name),
                getMappableObjectFromName(empire.getKnownPortals(), name),
                getMappableObjectFromName(empire.getKnownStorms(), name));
    }

    /**
     * Given a list of ship names, return the matching list of Ships.
     * @param empire
     * @param text
     * @return
     */
    protected static List<Ship> getLiveShipsFromNames(final Empire empire, final String text, final Order order) {
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

    protected static void parseReady(final JsonNode node, final TurnData turnData, final OrderType orderType, final Order.OrderBuilder<?, ?> builder) {
        builder
           .empire(turnData.getEmpire(getString(node, "empire")))
           .parameters(getString(node, "parameters"))
           .synthetic(getBoolean(node, "synthetic"))
           .gmOnly(orderType.isGmOnly())
           .orderType(orderType);
    }

    public String formatResults() {
        final String formatted;
        if (results.isEmpty()) {
            formatted = "%s # %s".formatted(this, isReady() ? "OK" : "Error");
        }
        else {
            formatted = "%s\n # %s".formatted(this, StringUtils.join(results, "\n # "));
        }
        return formatted;
    }
}