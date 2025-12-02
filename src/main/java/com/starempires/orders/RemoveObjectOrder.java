package com.starempires.orders;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.collect.Lists;
import com.starempires.TurnData;
import com.starempires.objects.Empire;
import com.starempires.objects.IdentifiableObject;
import com.starempires.objects.Portal;
import com.starempires.objects.Storm;
import com.starempires.objects.World;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuperBuilder
@Getter
public class RemoveObjectOrder extends Order {
    // order: REMOVEOBJECT name object-type object-name

    final static private String REGEX = OBJECT_TYPE_CAPTURE_REGEX + SPACE_REGEX + OBJECT_LIST_CAPTURE_REGEX;
    final static private Pattern PATTERN = Pattern.compile(REGEX, Pattern.CASE_INSENSITIVE);

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @JsonSerialize(using = IdentifiableObject.IdentifiableObjectSerializer.class)
    @JsonDeserialize(using = IdentifiableObject.DeferredIdentifiableObjectDeserializer.class)
    private List<World> worlds;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @JsonSerialize(using = IdentifiableObject.IdentifiableObjectSerializer.class)
    @JsonDeserialize(using = IdentifiableObject.DeferredIdentifiableObjectDeserializer.class)
    private List<Portal> portals;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @JsonSerialize(using = IdentifiableObject.IdentifiableObjectSerializer.class)
    @JsonDeserialize(using = IdentifiableObject.DeferredIdentifiableObjectDeserializer.class)
    private List<Storm> storms;

    public static RemoveObjectOrder parse(final TurnData turnData, final Empire empire, final String parameters) {
        final RemoveObjectOrder order = RemoveObjectOrder.builder()
                .empire(empire)
                .orderType(OrderType.REMOVEOBJECT)
                .parameters(parameters)
                .gmOnly(OrderType.REMOVEOBJECT.isGmOnly())
                .worlds(Lists.newArrayList())
                .portals(Lists.newArrayList())
                .storms(Lists.newArrayList())
                .build();
        if (!empire.isGM()) {
            order.addError("Command available only to GM");
            return order;
        }
        final Matcher matcher = PATTERN.matcher(parameters);
        if (matcher.matches()) {
            final String typeText = matcher.group(OBJECT_TYPE_GROUP);
            final String[] objectNames = matcher.group(OBJECT_LIST_GROUP).split(SPACE_REGEX);

            Arrays.stream(objectNames).forEach(objectName -> {
                switch (typeText.toLowerCase()) {
                    case "world" -> {
                        final World world = turnData.getWorld(objectName);
                        if (world == null) {
                            order.addError("Unknown world: " + objectName);
                        } else {
                            order.worlds.add(world);
                        }
                    }
                    case "portal" -> {
                        final Portal portal = turnData.getPortal(objectName);
                        if (portal == null) {
                            order.addError("Unknown portal: " + objectName);
                        } else {
                            order.portals.add(portal);
                        }
                    }
                    case "storm" -> {
                        final Storm storm = turnData.getStorm(objectName);
                        if (storm == null) {
                            order.addError("Unknown storm: " + objectName);
                        } else {
                            order.storms.add(storm);
                        }
                    }
                }
            });
            if (order.worlds.size() + order.portals.size() + order.storms.size() == 0) {
                order.addError("No valid objects found");
            }
            else {
                order.setReady(true);
            }
        } else {
            order.addError("Invalid REMOVEOBJECT order: " + parameters);
        }

        return order;
    }

    public static RemoveObjectOrder parseReady(final JsonNode node, final TurnData turnData) {
        final var builder = RemoveObjectOrder.builder();
        Order.parseReady(node, turnData, OrderType.REMOVEOBJECT, builder);
        return builder
                .worlds(getTurnDataListFromJsonNode(node.get("worlds"), turnData::getWorld))
                .portals(getTurnDataListFromJsonNode(node.get("portals"), turnData::getPortal))
                .storms(getTurnDataListFromJsonNode(node.get("storms"), turnData::getStorm))
                .build();
    }
}