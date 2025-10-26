package com.starempires.orders;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.starempires.TurnData;
import com.starempires.objects.Coordinate;
import com.starempires.objects.Empire;
import com.starempires.objects.IdentifiableObject;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuperBuilder
@Getter
public class AddWorldOrder extends Order {

    // ADDWORLD coord name production stockpile owner
    final static protected String PRODUCTION_GROUP = "production";
    final static protected String PRODUCTION_CAPTURE_REGEX = "(?<" + PRODUCTION_GROUP + ">" + INT_REGEX + ")";
    final static protected String STOCKPILE_GROUP = "stockpile";
    final static protected String STOCKPILE_CAPTURE_REGEX = "(?<" + STOCKPILE_GROUP + ">" + INT_REGEX + ")";

    final static private String REGEX = COORDINATE_CAPTURE_REGEX + SPACE_REGEX + ID_CAPTURE_REGEX + SPACE_REGEX + PRODUCTION_CAPTURE_REGEX + SPACE_REGEX + STOCKPILE_CAPTURE_REGEX +
                                        OPTIONAL_OWNER_CAPTURE_REGEX;
    final static private Pattern PATTERN = Pattern.compile(REGEX, Pattern.CASE_INSENSITIVE);

    @JsonInclude
    private Coordinate coordinate;
    @JsonInclude
    private String name;
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private int production;
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private int stockpile;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonSerialize(using = IdentifiableObject.IdentifiableObjectSerializer.class)
    @JsonDeserialize(using = IdentifiableObject.DeferredIdentifiableObjectDeserializer.class)
    private Empire owner;

    public static AddWorldOrder parse(final TurnData turnData, final Empire empire, final String parameters) {
        final AddWorldOrder order = AddWorldOrder.builder()
                .empire(empire)
                .orderType(OrderType.ADDWORLD)
                .parameters(parameters)
                .gmOnly(OrderType.ADDWORLD.isGmOnly())
                .build();
        if (!empire.isGM()) {
            order.addError("Command available only to GM");
            return order;
        }
        final Matcher matcher = PATTERN.matcher(parameters);
        if (matcher.matches()) {
            order.name = matcher.group(ID_GROUP);
            order.production = Integer.parseInt(matcher.group(PRODUCTION_GROUP));
            order.stockpile = Integer.parseInt(matcher.group(STOCKPILE_GROUP));
            final String coordText = matcher.group(COORDINATE_GROUP);
            order.coordinate = Coordinate.parse(coordText);
            final String ownerName = matcher.group(OWNER_GROUP);
            Empire owner = null;
            if (ownerName != null) {
                owner = turnData.getEmpire(ownerName);
                if (owner == null) {
                    order.addError("Unknown owner: " + ownerName);
                    return order;
                }
            }
            order.owner = owner;
            order.setReady(true);
        } else {
            order.addError("Invalid ADDWORLD order: " + parameters);
        }

        return order;
    }

    public static AddWorldOrder parseReady(final JsonNode node, final TurnData turnData) {
        final var builder = AddWorldOrder.builder();
        Order.parseReady(node, turnData, OrderType.ADDWORLD, builder);
        return builder
                .coordinate(getCoordinateFromJsonNode(node.get("coordinate")))
                .name(getString(node, "name"))
                .production(getInt(node, "production"))
                .stockpile(getInt(node, "stockpile"))
                .owner(getTurnDataItemFromJsonNode(node.get("owner"), turnData::getEmpire))
                .gmOnly(true)
                .build();
    }
}