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

@Getter
@SuperBuilder
public class TransferOrder extends WorldBasedOrder {

    // order: TRANSFER from-world {amount|MAX} to-world [owner]

    private static final String REGEX = ID_CAPTURE_REGEX + SPACE_REGEX + AMOUNT_CAPTURE_REGEX + SPACE_REGEX +
            DESTINATION_CAPTURE_REGEX + OPTIONAL_OWNER_CAPTURE_REGEX;
    private static final Pattern PATTERN = Pattern.compile(REGEX, Pattern.CASE_INSENSITIVE);

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @JsonSerialize(using = IdentifiableObject.IdentifiableObjectSerializer.class)
    @JsonDeserialize(using = IdentifiableObject.DeferredIdentifiableObjectDeserializer.class)
    private World destination;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @JsonSerialize(using = IdentifiableObject.IdentifiableObjectSerializer.class)
    @JsonDeserialize(using = IdentifiableObject.DeferredIdentifiableObjectDeserializer.class)
    private Empire owner;
    private int amount;
    private boolean transferAll;

    public static TransferOrder parse(final TurnData turnData, final Empire empire, final String parameters) {
        final TransferOrder order = TransferOrder.builder()
                .empire(empire)
                .orderType(OrderType.TRANSFER)
                .parameters(parameters)
                .build();
        final Matcher matcher = PATTERN.matcher(parameters);
        if (matcher.matches()) {
            final String worldName = matcher.group(ID_GROUP);
            final String destinationName = matcher.group(DESTINATION_GROUP);
            final String amountText = matcher.group(AMOUNT_GROUP);
            final String ownerName = matcher.group(OWNER_GROUP); // null if same empire
            final World world = turnData.getWorld(worldName);
            if (!empire.isKnownWorld(world) || !world.isOwnedBy(empire)) {
                order.addError("You do not own world " + worldName);
                return order;
            }
            final World destination = turnData.getWorld(destinationName);
            if (!empire.isKnownWorld(destination)) {
                order.addError("Unknown destination world: " + destinationName);
                return order;
            }
            if (world.equals(destination)) {
                order.addError(world, "Cannot transfer to same world");
                return order;
            }

            Empire owner = empire;
            if (ownerName != null) {
                owner = turnData.getEmpire(ownerName);
                if (!empire.isKnownEmpire(owner)) {
                    order.addError("You have no contact with empire " + ownerName);
                    return order;
                }
                if (!destination.isOwnedBy(owner)) {
                    order.addWarning(destination, "Empire %s does not currently own %s".formatted(owner, destination));
                }
            } else if (!destination.isOwnedBy(empire)) {
                order.addWarning(destination, "You do not currently own world " + destination);
            }

            int amount;
            if (amountText.equalsIgnoreCase(MAX_TOKEN)) {
                order.transferAll = true;
            } else {
                amount = Integer.parseInt(amountText);
                if (amount < 1) {
                    order.addError(world, "Invalid transfer amount %d".formatted(amount));
                    return order;
                }
                if (amount > world.getStockpile()) {
                    order.addWarning(world, "Only %d stockpile present".formatted(world.getStockpile()));
                }
                order.amount = amount;
            }

            order.world = world;
            order.destination = destination;
            order.owner = owner;
            order.setReady(true);
        } else {
            order.addError("Invalid TRANSFER order: " + parameters);
        }
        return order;
    }

    public static TransferOrder parseReady(final JsonNode node, final TurnData turnData) {
        final var builder = TransferOrder.builder();
        WorldBasedOrder.parseReady(node, turnData, OrderType.TRANSFER, builder);
        return builder
                .world(getTurnDataItemFromJsonNode(node.get("world"), turnData::getWorld))
                .destination(getTurnDataItemFromJsonNode(node.get("destination"), turnData::getWorld))
                .owner(getTurnDataItemFromJsonNode(node.get("owner"), turnData::getEmpire))
                .amount(getInt(node, "amount"))
                .transferAll(getBoolean(node, "transferAll"))
                .build();
    }
}