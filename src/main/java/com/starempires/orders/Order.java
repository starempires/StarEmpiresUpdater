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
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

@Data
@SuperBuilder
@NoArgsConstructor
@Log4j2
public abstract class Order {

    /** empire who gave this order */
    @JsonSerialize(using = IdentifiableObject.IdentifiableObjectSerializer.class)
    @JsonDeserialize(using = IdentifiableObject.DeferredIdentifiableObjectDeserializer.class)
    protected Empire empire;
    protected String parameters;
    /** text results from processing this order */
    @JsonIgnore
    protected final List<String> results = Lists.newArrayList();
    /** was this order generated "synthetically", i.e., by the updater */
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    protected boolean synthetic;
    protected OrderType orderType;
    @Builder.Default
    protected boolean ready = true;

    protected static final String SPACE_REGEX = "\\s+";
    protected static final String ID_REGEX = "\\w+";
    protected static final String ID_LIST_REGEX = ID_REGEX + "(?:" + SPACE_REGEX + ID_REGEX + ")*";
    protected static final String INT_REGEX = "\\d+";
    protected static final String INT_OR_MAX_REGEX = INT_REGEX + "|max";
    protected static final String AMOUNT_GROUP = "dp";
    protected static final String AMOUNT_CAPTURE_REGEX = "(?<" + AMOUNT_GROUP + ">" + INT_OR_MAX_REGEX + ")";
    protected static final String RECIPIENT_LIST_GROUP = "recipientlist";
    protected static final String RECIPIENT_LIST_CAPTURE_REGEX = "(?<" + RECIPIENT_LIST_GROUP + ">" + ID_LIST_REGEX + ")";
    protected static final String WORLD_LIST_GROUP = "worldlist";
    protected static final String WORLD_LIST_CAPTURE_REGEX = "(?<" + WORLD_LIST_GROUP + ">" + ID_LIST_REGEX + ")";

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

    public static Order parse(final TurnData turnData, final Empire empire, final String parameters) {
        throw new UnsupportedOperationException("Subclasses must implement this method");
    }

    protected static String getString(final JsonNode node, final String fieldName) {
        final JsonNode valueNode = node.get(fieldName);
        return valueNode == null ? null : valueNode.asText();
    }

    protected static int getInt(JsonNode node, String fieldName) {
        JsonNode valueNode = node.get(fieldName);
        return valueNode == null ? 0 : valueNode.asInt();
    }

    protected static boolean getBoolean(JsonNode node, String fieldName) {
        JsonNode valueNode = node.get(fieldName);
        return valueNode != null && valueNode.asBoolean();
    }

    private static List<String> getStringListFromValueNode(JsonNode node) {
        final List<String> values = new ArrayList<>();
        if (node != null && node.isArray()) {
            for (JsonNode value : node) {
                values.add(value.asText());
            }
        }
        return values;
    }

    protected static List<String> getStringList(JsonNode node, String fieldName) {
        final JsonNode valueNode = node.get(fieldName);
        return getStringListFromValueNode(valueNode);
    }

    protected static <T extends IdentifiableObject> List<T> getTurnDataListFromJsonNode(final JsonNode node, final Function<String, T> lookupMethod) {
        final List<String> names = getStringListFromValueNode(node);
        final List<T> objects = new ArrayList<>();
        names.forEach(name -> {
            T obj = lookupMethod.apply(name);
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

    protected static void parseReady(final JsonNode node, final TurnData turnData, final OrderType orderType, final Order.OrderBuilder<?, ?> builder) {
        builder
           .empire(turnData.getEmpire(getString(node, "empire")))
           .parameters(getString(node, "parameters"))
           .synthetic(getBoolean(node, "synthetic"))
           .orderType(orderType);
    }
}