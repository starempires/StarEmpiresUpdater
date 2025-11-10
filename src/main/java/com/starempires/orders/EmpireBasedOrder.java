package com.starempires.orders;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.collect.Lists;
import com.starempires.TurnData;
import com.starempires.objects.Empire;
import com.starempires.objects.IdentifiableObject;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.util.List;

@SuperBuilder
@Getter
public abstract class EmpireBasedOrder extends Order {
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @JsonSerialize(using = IdentifiableObject.IdentifiableObjectCollectionSerializer.class)
    @JsonDeserialize(using = IdentifiableObject.DeferredIdentifiableObjectCollectionDeserializer.class)
    final List<Empire> recipients;

    protected static void parseReady(final JsonNode node, final TurnData turnData, final OrderType orderType, final EmpireBasedOrder.EmpireBasedOrderBuilder<?, ?> builder) {
        Order.parseReady(node,  turnData, orderType, builder);
        builder.recipients(getTurnDataListFromJsonNode(node.get("recipients"), turnData::getEmpire));
    }

    /**
     * Helper method to parse and validate a list of recipient empires from a space-separated string.
     * Validates that each empire exists, is known to the issuing empire, is not the issuing empire itself,
     * and is not the GM.
     * 
     * @param turnData The current turn data containing all empires
     * @param empire The empire issuing the order
     * @param recipientsText Space-separated list of empire names
     * @param order The order object to add errors to
     * @return List of validated Empire objects (may be empty if all validations fail)
     */
    protected static List<Empire> parseRecipients(final TurnData turnData, final Empire empire,
                                                      final String recipientsText, final Order order) {
        final List<Empire> recipients = Lists.newArrayList();
        final String[] recipientNames = recipientsText.split(SPACE_REGEX);
        
        for (String recipientName : recipientNames) {
            final Empire recipient = turnData.getEmpire(recipientName);
            
            if (recipient == null || !empire.isKnownEmpire(recipient)) {
                order.addError("Unknown empire: " + recipientName);
            } else if (recipient.equals(empire)) {
                order.addError("Ignoring your own empire");
            } else if (recipient.isGM()) {
                order.addError("Ignoring the GM");
            } else {
                recipients.add(recipient);
            }
        }
        
        return recipients;
    }
}