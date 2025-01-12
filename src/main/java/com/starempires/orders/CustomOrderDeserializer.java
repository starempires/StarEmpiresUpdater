package com.starempires.orders;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.starempires.TurnData;
import org.apache.commons.lang3.EnumUtils;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class CustomOrderDeserializer extends JsonDeserializer<Order> {
    private final TurnData turnData;

    // Constructor with a parameter
    public CustomOrderDeserializer(final TurnData turnData) {
        this.turnData = turnData;
    }

    @Override
    public Order deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
        final JsonNode node = jp.getCodec().readTree(jp);
        final String orderTypeText = node.get("orderType").asText();
        final OrderType orderType = EnumUtils.getEnumIgnoreCase(OrderType.class, orderTypeText);
        if (orderType == null) {
            throw new RuntimeException("Unknown order type: " + orderType);
        }
        final Class<? extends Order> orderClass = orderType.getOrderClass();
        try {
            final Method method = orderClass.getMethod("parseReady", JsonNode.class, TurnData.class);
            return (Order) method.invoke(null, node, turnData);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}