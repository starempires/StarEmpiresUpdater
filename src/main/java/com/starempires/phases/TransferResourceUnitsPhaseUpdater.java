package com.starempires.phases;

import com.starempires.TurnData;
import com.starempires.objects.Empire;
import com.starempires.orders.Order;
import com.starempires.orders.OrderType;
import com.starempires.objects.World;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TransferResourceUnitsPhaseUpdater extends PhaseUpdater {

    // transfer [all| amount] world1 world2 [empire]
    private static final String AMOUNT_GROUP = "amount";
    private static final String FROM_GROUP = "from";
    private static final String TO_GROUP = "to";
    private static final String EMPIRE_GROUP = "empire";
    private static final String TRANSFER_REGEX = "^transfer\\s+(?<"+ AMOUNT_GROUP +">all|\\d+)\\s+(?<" +FROM_GROUP +">\\w+)\\s+(?<" + TO_GROUP +">\\w+)(?<" + EMPIRE_GROUP +">:\\s+(\\w+))?\\s*$";

    private static final Pattern TRANSFER_PATTERN = Pattern.compile(TRANSFER_REGEX, Pattern.CASE_INSENSITIVE);

    public TransferResourceUnitsPhaseUpdater(final TurnData turnData) {
        super(Phase.TRANSFER_RESOURCE_UNITS, turnData);
    }

    @Override
    public void update() {
        final List<Order> orders = turnData.getOrders(OrderType.TRANSFER);
        orders.forEach(order -> {
            final Matcher matcher = TRANSFER_PATTERN.matcher(order.getParametersAsString());
            if (matcher.matches()) {
                final Empire empire = order.getEmpire();
                final String fromWorldName = matcher.group(FROM_GROUP);
                final String toWorldName = matcher.group(TO_GROUP);
                final String amountText = matcher.group(AMOUNT_GROUP);
                final String empireName = matcher.group(EMPIRE_GROUP);
                final Empire toEmpire = turnData.getEmpire(empireName);
                final Empire recipient;
                if (toEmpire == null) {
                    recipient = empire;
                }
                else {
                    if (!empire.isKnownEmpire(toEmpire)) {
                        addNewsResult(order, "You have no information about empire " + toEmpire);
                        return;
                    }
                    recipient = toEmpire;
                }

                final World fromWorld = turnData.getWorld(fromWorldName);
                if (fromWorld == null || !fromWorld.isOwnedBy(empire)) {
                    addNewsResult(order, "You do not own world " + fromWorld);
                    return;
                }

                if (fromWorld.isBlockaded()) {
                    addNewsResult(order, "World " + fromWorld + " is blockaded; no RU transfers possible");
                    return;
                }

                final int stockpile = fromWorld.getStockpile();
                if (stockpile <= 0) {
                    addNewsResult(order, "No RUs remaining at world " + fromWorld);
                    return;
                }

                int amount = amountText.equals("all") ? stockpile : Integer.parseInt(amountText);
                if (amount > stockpile) {
                    addNewsResult(order,"Transfer amount " + amount + " exceeds stockpile " + stockpile
                            + " at world " + fromWorld + "; sending available stockpile");
                    amount = stockpile;
                }

                final World toWorld = turnData.getWorld(toWorldName);
                if (toWorld == null || !empire.isKnownWorld(toWorld)) {
                    addNewsResult(order, "You have no information about destination world " + toWorldName);
                }
                else if (!toWorld.isOwnedBy(toEmpire)) {
                    if (recipient.equals(empire)) {
                        addNewsResult(order, "You do not own world " + toWorld);
                    }
                    else {
                        addNewsResult(order, "World " + toWorld + " is not owned by intended recipient " + toEmpire);
                    }
                }
                else {
                    addNews(empire, "World " + fromWorld + " transferred " + amount + " RU to destination " + toWorld);
                    if (!recipient.equals(empire)) {
                        addNews(toEmpire, "World " + toWorld + " has received a shipment of " + amount + "RU from " + empire);
                    }
                    fromWorld.adjustStockpile(-amount);
                    toWorld.adjustStockpile(amount);
                }
            }
        });
    }
}