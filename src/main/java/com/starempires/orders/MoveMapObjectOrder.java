package com.starempires.orders;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.starempires.TurnData;
import com.starempires.objects.Coordinate;
import com.starempires.objects.Empire;
import com.starempires.objects.IdentifiableObject;
import com.starempires.objects.Portal;
import com.starempires.objects.Storm;
import com.starempires.objects.World;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.apache.commons.lang3.ObjectUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuperBuilder
@Getter
public class MoveMapObjectOrder extends GMOrder {

    // MOVEMAPOBJECT TYPE NAME coord
    final static protected String ID_GROUP = "id";
    final static protected String TYPE_GROUP = "type";
    final static protected String TYPE_REGEX = "world|portal|storm";
    final static protected String ID_CAPTURE_REGEX = "(?<" + ID_GROUP + ">" + ID_REGEX + ")";
    final static protected String TYPE_CAPTURE_REGEX = "(?<" + TYPE_GROUP + ">" + TYPE_REGEX + ")";

    final static private String REGEX = TYPE_CAPTURE_REGEX + SPACE_REGEX + ID_CAPTURE_REGEX + SPACE_REGEX + COORDINATE_CAPTURE_REGEX;
    final static private Pattern PATTERN = Pattern.compile(REGEX, Pattern.CASE_INSENSITIVE);

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @JsonSerialize(using = IdentifiableObject.IdentifiableObjectSerializer.class)
    @JsonDeserialize(using = IdentifiableObject.DeferredIdentifiableObjectDeserializer.class)
    private World world;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @JsonSerialize(using = IdentifiableObject.IdentifiableObjectSerializer.class)
    @JsonDeserialize(using = IdentifiableObject.DeferredIdentifiableObjectDeserializer.class)
    private Portal portal;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @JsonSerialize(using = IdentifiableObject.IdentifiableObjectSerializer.class)
    @JsonDeserialize(using = IdentifiableObject.DeferredIdentifiableObjectDeserializer.class)
    private Storm storm;
    private Coordinate coordinate;

    public static MoveMapObjectOrder parse(final TurnData turnData, final Empire empire, final String parameters) {
        final MoveMapObjectOrder order = MoveMapObjectOrder.builder()
                .empire(empire)
                .orderType(OrderType.MOVEMAPOBJECT)
                .parameters(parameters)
                .gmOnly(true)
                .build();
        if (!empire.isGM()) {
            order.addError("Command available only to GM");
            return order;
        }
        final Matcher matcher = PATTERN.matcher(parameters);
        if (matcher.matches()) {
            final String typeText = matcher.group(TYPE_GROUP);
            final String idText = matcher.group(ID_GROUP);
            final String coordText = matcher.group(COORDINATE_GROUP);

            order.coordinate = Coordinate.parse(coordText);

            switch (typeText.toLowerCase()) {
                case "world" -> order.world = turnData.getWorld(idText);
                case "portal" -> order.portal = turnData.getPortal(idText);
                case "storm" -> order.storm = turnData.getStorm(idText);
            }

            if (ObjectUtils.anyNotNull(order.world, order.portal, order.storm)) {
                order.setReady(true);
            }
            else {
                order.addError("Unknown %s: %s".formatted(typeText.toLowerCase(), idText));
            }
        } else {
            order.addError("Invalid MOVEMAPOBJECT order: " + parameters);
        }

        return order;
    }

    public static MoveMapObjectOrder parseReady(final JsonNode node, final TurnData turnData) {
        final var builder = MoveMapObjectOrder.builder();
        Order.parseReady(node, turnData, OrderType.MOVEMAPOBJECT, builder);
        return builder
                .world(getTurnDataItemFromJsonNode(node.get("world"), turnData::getWorld))
                .portal(getTurnDataItemFromJsonNode(node.get("portal"), turnData::getPortal))
                .storm(getTurnDataItemFromJsonNode(node.get("storm"), turnData::getStorm))
                .coordinate(getCoordinateFromJsonNode(node.get("coordinate")))
                .build();
    }
}