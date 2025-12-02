package com.starempires.orders;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.starempires.TurnData;
import com.starempires.objects.Empire;
import com.starempires.objects.IdentifiableObject;
import com.starempires.objects.Storm;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuperBuilder
@Getter
public class ModifyStormOrder extends Order {

    // order: MODIFYSTORM storm rating
    final static private String REGEX = ID_CAPTURE_REGEX + SPACE_REGEX + INTENSITY_CAPTURE_REGEX;
    final static private Pattern PATTERN = Pattern.compile(REGEX, Pattern.CASE_INSENSITIVE);

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @JsonSerialize(using = IdentifiableObject.IdentifiableObjectSerializer.class)
    @JsonDeserialize(using = IdentifiableObject.DeferredIdentifiableObjectDeserializer.class)
    private Storm storm;
    @JsonInclude(JsonInclude.Include.ALWAYS)
    private int intensity;

    public static ModifyStormOrder parse(final TurnData turnData, final Empire empire, final String parameters) {
        final ModifyStormOrder order = ModifyStormOrder.builder()
                .empire(empire)
                .orderType(OrderType.MODIFYSTORM)
                .parameters(parameters)
                .gmOnly(OrderType.MODIFYSTORM.isGmOnly())
                .build();
        if (!empire.isGM()) {
            order.addError("Command available only to GM");
            return order;
        }

        final Matcher matcher = PATTERN.matcher(parameters);
        if (matcher.matches()) {
            final String stormName = matcher.group(ID_GROUP);
            final String intensityText = matcher.group(INTENSITY_GROUP);

            final Storm storm = turnData.getStorm(stormName);
            if (storm == null) {
                order.addError("Unknown storm: " + stormName);
                return order;
            }
            order.storm = storm;
            order.intensity = Integer.parseInt(intensityText);
            order.setReady(true);
        } else {
            order.addError("Invalid MODIFYSTORM order: " + parameters);
        }

        return order;
    }

    public static ModifyStormOrder parseReady(final JsonNode node, final TurnData turnData) {
        final var builder = ModifyStormOrder.builder();
        Order.parseReady(node, turnData, OrderType.MODIFYSTORM, builder);
        return builder
                .storm(getTurnDataItemFromJsonNode(node.get("storm"), turnData::getStorm))
                .intensity(getInt(node, "intensity"))
                .build();
    }
}