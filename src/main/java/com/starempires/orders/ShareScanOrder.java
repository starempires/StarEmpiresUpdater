package com.starempires.orders;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
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
                    // SECTOR authorization: (coordinate|@location) radius
                    "(?:" + COORDINATE_CAPTURE_REGEX + "|" + SHIP_LOCATION_CAPTURE_REGEX + ")" +
                    SPACE_REGEX + RADIUS_CAPTURE_REGEX +
                    "|" +
                    // ALL authorization: ALL
                    ALL_TOKEN +
                    "|" +
                    // SHIP authorization: ship1 [ship2 ...]
                    OBJECT_LIST_CAPTURE_REGEX +
                    ")" +
                    SPACE_REGEX + TO_TOKEN + SPACE_REGEX + RECIPIENT_LIST_CAPTURE_REGEX;
    protected static final Pattern PATTERN = Pattern.compile(REGEX, Pattern.CASE_INSENSITIVE);

    @JsonInclude(JsonInclude.Include.NON_NULL)
    Coordinate coordinate;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonSerialize(using = IdentifiableObject.IdentifiableObjectSerializer.class)
    @JsonDeserialize(using = IdentifiableObject.DeferredIdentifiableObjectDeserializer.class)
    MappableObject mapObject;

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    int radius;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @JsonSerialize(using = IdentifiableObject.IdentifiableObjectCollectionSerializer.class)
    @JsonDeserialize(using = IdentifiableObject.DeferredIdentifiableObjectCollectionDeserializer.class)
    final List<Ship> ships;

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    boolean allSectors;

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
        final String coordText = matcher.group(COORDINATE_GROUP);
        final String locationText = matcher.group(SHIP_LOCATION_GROUP);
        final String radiusText = matcher.group(RADIUS_GROUP);
        final String shipListText = matcher.group(OBJECT_LIST_GROUP);

        if (coordText != null || locationText != null) {
            order.radius = Integer.parseInt(radiusText);
            if (coordText != null) {
                // Parse as coordinate
                order.coordinate = Coordinate.parse(coordText);
            } else {
                // Parse as named location - remove @ prefix
                final String mapObjectName = locationText.replace("@", "");
                order.mapObject = getKnownMappableObjectFromName(empire, mapObjectName);
            }
        } else if (shipListText != null) {
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
                    .coordinate(getCoordinateFromJsonNode(node.get("coordinate")))
                    .radius(getInt(node, "radius"))
                    .mapObject(getKnownMappableObjectFromName(empire, node.get("mapObject").asText()))
                    .allSectors(getBoolean(node, "allSectors"))
                    .build();
        }
}