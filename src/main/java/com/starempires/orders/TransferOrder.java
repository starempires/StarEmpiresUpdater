package com.starempires.orders;

import com.fasterxml.jackson.databind.JsonNode;
import com.starempires.TurnData;
import com.starempires.objects.Empire;
import com.starempires.objects.World;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Getter
@SuperBuilder
public class TransferOrder extends WorldBasedOrder {

    //TRANSFER from-world {amount|ALL} to-world [empire]

    final static protected String TO_WORLD_GROUP = "toworld";
    final static protected String TO_WORLD_CAPTURE_REGEX = "(?<" + TO_WORLD_GROUP + ">" + ID_REGEX + ")";
    final static protected String RECIPIENT_GROUP = "recipient";
    final static protected String RECIPIENT_CAPTURE_REGEX = "(?:" + SPACE_REGEX + "(?<" + RECIPIENT_GROUP + ">" +ID_REGEX + "))?";

    private static final String REGEX = WORLD_CAPTURE_REGEX + SPACE_REGEX + AMOUNT_CAPTURE_REGEX + SPACE_REGEX + TO_WORLD_CAPTURE_REGEX + RECIPIENT_CAPTURE_REGEX;
    private static final Pattern PATTERN = Pattern.compile(REGEX, Pattern.CASE_INSENSITIVE);

    private World fromWorld;
    private World toWorld;
    private Empire toEmpire;
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
            String fromWorldName = matcher.group(WORLD_GROUP);
            String toWorldName = matcher.group(TO_WORLD_GROUP);
            String amountText = matcher.group(AMOUNT_GROUP);
            String toEmpireName = matcher.group(RECIPIENT_GROUP);
            World fromWorld = turnData.getWorld(fromWorldName);
            if (fromWorld == null || !empire.isKnownWorld(fromWorld) || !fromWorld.isOwnedBy(empire)) {
                order.addError("You do not own world " + fromWorldName);
                order.setReady(false);
                return order;
            }
            World toWorld = turnData.getWorld(fromWorldName);
            if (toWorld == null || !empire.isKnownWorld(toWorld)) {
                order.addError("Unknown world: " + toWorldName);
                order.setReady(false);
                return order;
            }
            if (fromWorld.equals(toWorld)) {
                order.addError(fromWorld, "Cannot transfer to same world");
                order.setReady(false);
                return order;
            }

            Empire toEmpire = empire;
            if (toEmpireName != null) {
                toEmpire = turnData.getEmpire(toEmpireName);
                if (toEmpire == null || empire.isKnownEmpire(toEmpire)) {
                    order.addError("You have no contact with empire " + toEmpireName);
                    order.setReady(false);
                    return order;
                }
                if (!toWorld.isOwnedBy(toEmpire)) {
                    order.addWarning(toWorld, "Empire %s does not currently own %s".formatted(toEmpireName, toWorldName));
                }
            } else if (!toWorld.isOwnedBy(empire)) {
                order.addWarning(toWorld, "You do not currently own world " + toWorldName);
            }

            int amount;
            if (amountText.equalsIgnoreCase("ALL")) {
                order.transferAll = true;
            } else {
                amount = Integer.parseInt(amountText);
                if (amount < 1) {
                    order.addError(fromWorld, "Invalid transfer amount transfer %d".formatted(amount));
                    order.setReady(false);
                    return order;
                }
                if (amount > fromWorld.getStockpile()) {
                    order.addWarning(fromWorld, "Only %d stockpile present".formatted(fromWorld.getStockpile()));
                }
                order.amount = amount;
            }

            order.fromWorld = fromWorld;
            order.toWorld = toWorld;
            order.toEmpire = toEmpire;
        } else {
            order.addError("Invalid transfer order: " + parameters);
            order.setReady(false);
        }
        return order;
    }

    public static TransferOrder parseReady(final JsonNode node, final TurnData turnData) {
        final var builder = TransferOrder.builder();
        WorldBasedOrder.parseReady(node, turnData, OrderType.TRANSFER, builder);
        return builder
                .fromWorld(getTurnDataItemFromJsonNode(node.get("fromWorld"), turnData::getWorld))
                .toWorld(getTurnDataItemFromJsonNode(node.get("toWorld"), turnData::getWorld))
                .toEmpire(getTurnDataItemFromJsonNode(node.get("toEmpire"), turnData::getEmpire))
                .amount(getInt(node, "amount"))
                .transferAll(getBoolean(node, "transferAll"))
                .build();
    }
}