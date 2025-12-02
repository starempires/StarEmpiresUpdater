package com.starempires.orders;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.starempires.TurnData;
import com.starempires.objects.Empire;
import com.starempires.objects.IdentifiableObject;
import com.starempires.objects.World;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuperBuilder
@Getter
public class ModifyWorldOrder extends WorldBasedOrder {

    // order: MODIFYWORLD world production stockpile [owner]

    final static private String REGEX = ID_CAPTURE_REGEX + SPACE_REGEX + PRODUCTION_CAPTURE_REGEX +
            SPACE_REGEX + STOCKPILE_CAPTURE_REGEX + OPTIONAL_OWNER_CAPTURE_REGEX;
    final static private Pattern PATTERN = Pattern.compile(REGEX, Pattern.CASE_INSENSITIVE);

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @JsonSerialize(using = IdentifiableObject.IdentifiableObjectSerializer.class)
    @JsonDeserialize(using = IdentifiableObject.DeferredIdentifiableObjectDeserializer.class)
    private Empire owner;
    @JsonInclude(JsonInclude.Include.ALWAYS)
    private int production;
    @JsonInclude(JsonInclude.Include.ALWAYS)
    private int stockpile;

    public static ModifyWorldOrder parse(final TurnData turnData, final Empire empire, final String parameters) {
        final ModifyWorldOrder order = ModifyWorldOrder.builder()
                .empire(empire)
                .orderType(OrderType.MODIFYWORLD)
                .parameters(parameters)
                .gmOnly(OrderType.MODIFYWORLD.isGmOnly())
                .build();
        if (!empire.isGM()) {
            order.addError("Command available only to GM");
            return order;
        }

        final Matcher matcher = PATTERN.matcher(parameters);
        if (matcher.matches()) {
            final String worldName = matcher.group(ID_GROUP);
            final World world = turnData.getWorld(worldName);
            if (world == null) {
                order.addError("Unknown world: " + worldName);
                return order;
            }
            order.world = world;
            order.production = Integer.parseInt(matcher.group(PRODUCTION_GROUP));
            order.stockpile = Integer.parseInt(matcher.group(STOCKPILE_GROUP));
            final String ownerName = matcher.group(OWNER_GROUP);
            if (ownerName != null) {
                final Empire owner = turnData.getEmpire(ownerName);
                if (owner == null) {
                    order.addError("Unknown owner: " + ownerName);
                    return order;
                }
                order.owner = owner;
            }
            order.setReady(true);
        } else {
            order.addError("Invalid MODIFYWORLD order: " + parameters);
        }

        return order;
    }

    public static ModifyWorldOrder parseReady(final JsonNode node, final TurnData turnData) {
        final var builder = ModifyWorldOrder.builder();
        Order.parseReady(node, turnData, OrderType.MODIFYWORLD, builder);
        return builder
                .owner(getTurnDataItemFromJsonNode(node.get("owner"), turnData::getEmpire))
                .production(getInt(node, "production"))
                .stockpile(getInt(node, "stockpile"))
                .build();
    }

}