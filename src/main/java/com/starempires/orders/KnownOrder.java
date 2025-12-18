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
import com.starempires.objects.ShipClass;
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
public abstract class KnownOrder extends EmpireBasedOrder {

    final static private String REGEX = OBJECT_TYPE_CAPTURE_REGEX +
            SPACE_REGEX + OBJECT_LIST_CAPTURE_REGEX +
            SPACE_REGEX + "(" + FROM_TOKEN + "|" + TO_TOKEN + ")" +
            SPACE_REGEX + RECIPIENT_LIST_CAPTURE_REGEX;
    final static private Pattern PATTERN = Pattern.compile(REGEX, Pattern.CASE_INSENSITIVE);

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @JsonSerialize(using = IdentifiableObject.IdentifiableObjectCollectionSerializer.class)
    @JsonDeserialize(using = IdentifiableObject.DeferredIdentifiableObjectCollectionDeserializer.class)
    List<World> worlds;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @JsonSerialize(using = IdentifiableObject.IdentifiableObjectCollectionSerializer.class)
    @JsonDeserialize(using = IdentifiableObject.DeferredIdentifiableObjectCollectionDeserializer.class)
    List<Portal> portals;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @JsonSerialize(using = IdentifiableObject.IdentifiableObjectCollectionSerializer.class)
    @JsonDeserialize(using = IdentifiableObject.DeferredIdentifiableObjectCollectionDeserializer.class)
    List<Portal> navData;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @JsonSerialize(using = IdentifiableObject.IdentifiableObjectCollectionSerializer.class)
    @JsonDeserialize(using = IdentifiableObject.DeferredIdentifiableObjectCollectionDeserializer.class)
    List<Storm> storms;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @JsonSerialize(using = IdentifiableObject.IdentifiableObjectCollectionSerializer.class)
    @JsonDeserialize(using = IdentifiableObject.DeferredIdentifiableObjectCollectionDeserializer.class)
    List<ShipClass> shipClasses;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @JsonSerialize(using = IdentifiableObject.IdentifiableObjectCollectionSerializer.class)
    @JsonDeserialize(using = IdentifiableObject.DeferredIdentifiableObjectCollectionDeserializer.class)
    List<Empire> contacts;

    public static <T extends KnownOrder> T parse(final TurnData turnData, final Empire empire, final String parameters,
                                                 final OrderType orderType,
                                                 final KnownOrder.KnownOrderBuilder<T, ?> builder) {
        final T order = builder
                .empire(empire)
                .orderType(orderType)
                .parameters(parameters)
                .recipients(Lists.newArrayList())
                .gmOnly(orderType.isGmOnly())
                .worlds(Lists.newArrayList())
                .portals(Lists.newArrayList())
                .navData(Lists.newArrayList())
                .storms(Lists.newArrayList())
                .shipClasses(Lists.newArrayList())
                .contacts(Lists.newArrayList())
                .build();
        if (!empire.isGM()) {
            order.addError("Command available only to GM");
            return order;
        }
        final Matcher matcher = PATTERN.matcher(parameters);
        if (matcher.matches()) {
            final String typeText = matcher.group(OBJECT_TYPE_GROUP);
            final String[] objectNames = matcher.group(OBJECT_LIST_GROUP).split(SPACE_REGEX);
            final String[] recipientNames = matcher.group(RECIPIENT_LIST_GROUP).split(SPACE_REGEX);

            for (String recipientName: recipientNames) {
                final Empire recipient = turnData.getEmpire(recipientName);
                if (recipient == null) {
                    order.addError("Unknown recipient: " + recipientName);
                } else {
                    order.recipients.add(recipient);
                }
            }
            if (order.recipients.isEmpty()) {
                order.addError("No valid recipients");
                return order;
            }
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
                    case "navdata" -> {
                        final Portal portal = turnData.getPortal(objectName);
                        if (portal == null) {
                            order.addError("Unknown portal: " + objectName);
                        } else {
                            order.navData.add(portal);
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
                    case "shipclass" -> {
                        final ShipClass shipClass = turnData.getShipClass(objectName);
                        if (shipClass == null) {
                            order.addError("Unknown ship class: " + objectName);
                        } else {
                            order.shipClasses.add(shipClass);
                        }
                    }
                    case "contact" -> {
                        final Empire contact = turnData.getEmpire(objectName);
                        if (contact == null) {
                            order.addError("Unknown contacat: " + objectName);
                        } else {
                            order.contacts.add(contact);
                        }
                    }
                }
            });

            if (order.worlds.size() + order.portals.size() + order.navData.size() + order.storms.size()
                    + order.shipClasses.size() + order.contacts.size() == 0) {
                order.addError("No valid objects found");
            }
            else {
                order.setReady(true);
            }
        } else {
            order.addError("Invalid %s order: %s".formatted(orderType, parameters));
        }

        return order;
    }

    public static void parseReady(final JsonNode node, final TurnData turnData,
                                  final OrderType orderType,
                                  final KnownOrder.KnownOrderBuilder<? extends KnownOrder, ?> builder) {
        EmpireBasedOrder.parseReady(node, turnData, orderType, builder);
        builder.worlds(getTurnDataListFromJsonNode(node.get("worlds"), turnData::getWorld))
               .portals(getTurnDataListFromJsonNode(node.get("portals"), turnData::getPortal))
               .navData(getTurnDataListFromJsonNode(node.get("navData"), turnData::getPortal))
               .storms(getTurnDataListFromJsonNode(node.get("storms"), turnData::getStorm))
               .shipClasses(getTurnDataListFromJsonNode(node.get("shipClasses"), turnData::getShipClass))
               .contacts(getTurnDataListFromJsonNode(node.get("contacts"), turnData::getEmpire));
    }
}