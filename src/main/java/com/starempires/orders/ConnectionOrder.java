package com.starempires.orders;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.starempires.TurnData;
import com.starempires.objects.Empire;
import com.starempires.objects.IdentifiableObject;
import com.starempires.objects.Portal;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuperBuilder
@Getter
public abstract class ConnectionOrder extends Order {
    final static private String REGEX = ENTRY_CAPTURE_REGEX + SPACE_REGEX + EXIT_CAPTURE_REGEX;
    final static private Pattern PATTERN = Pattern.compile(REGEX, Pattern.CASE_INSENSITIVE);

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonSerialize(using = IdentifiableObject.IdentifiableObjectSerializer.class)
    @JsonDeserialize(using = IdentifiableObject.DeferredIdentifiableObjectDeserializer.class)
    Portal entry;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonSerialize(using = IdentifiableObject.IdentifiableObjectSerializer.class)
    @JsonDeserialize(using = IdentifiableObject.DeferredIdentifiableObjectDeserializer.class)
    Portal exit;

    public static <T extends ConnectionOrder> T parse(final TurnData turnData, final Empire empire, final String parameters, final OrderType orderType, final ConnectionOrder.ConnectionOrderBuilder<T, ?> builder) {
        final T order = builder.empire(empire)
               .orderType(orderType)
               .parameters(parameters)
               .gmOnly(orderType.isGmOnly())
               .build();
        if (!empire.isGM()) {
            order.addError("Command available only to GM");
            return order;
        }
        final Matcher matcher = PATTERN.matcher(parameters);
        if (matcher.matches()) {
            final String entryText = matcher.group(ENTRY_GROUP);
            final String exitText = matcher.group(EXIT_GROUP);
            final Portal entry = turnData.getPortal(entryText);
            if (entry == null) {
                order.addError("Unknown entry portal: " + entryText);
                return order;
            }
            final Portal exit = turnData.getPortal(exitText);
            if (exit == null) {
                order.addError("Unknown exit portal: " + exitText);
                return order;
            }
            order.entry = entry;
            order.exit = exit;
            order.setReady(true);
        } else {
            order.addError("Invalid %s order: %s".formatted(orderType, parameters));
        }

        return order;
    }

    public static void parseReady(final JsonNode node, final TurnData turnData, final OrderType orderType, final ConnectionOrder.ConnectionOrderBuilder<? extends ConnectionOrder, ?> builder) {
        Order.parseReady(node, turnData, orderType, builder);
        builder.entry(getTurnDataItemFromJsonNode(node.get("entry"), turnData::getPortal))
               .exit(getTurnDataItemFromJsonNode(node.get("exit"), turnData::getPortal));
    }
}