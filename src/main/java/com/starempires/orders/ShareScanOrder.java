package com.starempires.orders;

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
import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuperBuilder
@Getter
public abstract class ShareScanOrder extends EmpireBasedOrder {
    private static final String REGEX =
            "(?:" +
                  // coordinate list
                  COORDINATE_LIST_CAPTURE_REGEX + "|" +
                  // map object list
                  SHIP_LOCATION_LIST_CAPTURE_REGEX + "|" +
                  // ALL authorization
                  ALL_TOKEN + "|" +
                  // SHIP :list
                  OBJECT_LIST_CAPTURE_REGEX +
              ")" +
              SPACE_REGEX + TO_TOKEN + SPACE_REGEX + RECIPIENT_LIST_CAPTURE_REGEX;
    protected static final Pattern PATTERN = Pattern.compile(REGEX, Pattern.CASE_INSENSITIVE);

    private static final Pattern COORD_PATTERN =
            Pattern.compile("\\(?\\s*(-?\\d+)\\s*,\\s*(-?\\d+)\\s*\\)?");


    private static List<String> splitCoordinates(String text) {
        final List<String> results = Lists.newArrayList();
        final Matcher m = COORD_PATTERN.matcher(text);

        while (m.find()) {
            final String oblique = m.group(1);
            final String y = m.group(2);
            results.add(oblique + "," + y);   // normalized form
        }
        return results;
    }

    public static <T extends ShareScanOrder> T parse(final TurnData turnData, final Empire empire, final String parameters, final T order) {
        final Matcher matcher = PATTERN.matcher(parameters);
        if (!matcher.matches()) {
            order.addError("Invalid %s order: %s".formatted(order.getOrderType(), parameters));
            return order;
        }

        // Parse target empires
        final String recipientListText = matcher.group(RECIPIENT_LIST_GROUP);
        final List<Empire> recipients = parseRecipients(turnData, empire, recipientListText, order);

        if (recipients.isEmpty()) {
            order.addError("No valid recipients");
            return order;
        }

        order.recipients.addAll(recipients);

        // Determine authorization type and parse accordingly
        final String coordListText = matcher.group(COORDINATE_LIST_GROUP);
        final String locationListText = matcher.group(SHIP_LOCATION_GROUP);
        final String shipListText = matcher.group(OBJECT_LIST_GROUP);

        if (coordListText != null) {
            order.coordinates.addAll(Coordinate.parse(splitCoordinates(coordListText)));
        }
        else if (locationListText != null) {
            final String[] objectNames = locationListText.replace("@", "").split(SPACE_REGEX);
            for (String objectName: objectNames) {
                final MappableObject object = getKnownMappableObjectFromName(empire, objectName);
                if (object == null) {
                    order.addError(objectName, "Unknown location");
                }
                else {
                    order.mapObjects.add(object);
                }
            }
            if (order.mapObjects.isEmpty()) {
                order.addError("No valid locations found");
                return order;
            }
        }
        else if (shipListText != null) {
            // SHIP authorization
            final List<Ship> liveShips = getLiveShipsFromNames(empire, shipListText, order);
            for (Ship ship : liveShips) {
                if (order.getOrderType() == OrderType.AUTHORIZE && ship.isLoaded()) {
                    order.addWarning(ship, "Loaded ships cannot share scan data");
                } else {
                    order.ships.add(ship);
                }
            }

            if (order.ships.isEmpty()) {
                order.addError("No valid ships found");
                return order;
            }
        } else {
            // ALL authorization
            order.allSectors = true;
        }

        order.setReady(true);
        return order;
    }

    protected static <T extends ShareScanOrder> T parseReadyShareScan(final JsonNode node,
                                                                      final TurnData turnData,
                                                                      final OrderType orderType,
                                                                      final ShareScanOrder.ShareScanOrderBuilder<T, ?> builder) {
        EmpireBasedOrder.parseReady(node, turnData, orderType, builder);
        final Empire empire = turnData.getEmpire(node.get("empire").asText());
        return builder
                .ships(getTurnDataListFromJsonNode(node.get("ships"), empire::getShip))
                .coordinates(getCoordinateListFromJsonNode(node.get("coordinates")))
                .mapObjects(getMapObjectListFromNames(empire, getStringList(node, "mapObjects")))
                .allSectors(getBoolean(node, "allSectors"))
                .build();
    }

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    List<Coordinate> coordinates;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @JsonSerialize(using = IdentifiableObject.IdentifiableObjectCollectionSerializer.class)
    @JsonDeserialize(using = IdentifiableObject.DeferredIdentifiableObjectCollectionDeserializer.class)
    List<MappableObject> mapObjects;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @JsonSerialize(using = IdentifiableObject.IdentifiableObjectCollectionSerializer.class)
    @JsonDeserialize(using = IdentifiableObject.DeferredIdentifiableObjectCollectionDeserializer.class)
    final List<Ship> ships;

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    boolean allSectors;
}