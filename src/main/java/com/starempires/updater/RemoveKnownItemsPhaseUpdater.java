package com.starempires.updater;

import com.starempires.TurnData;
import com.starempires.objects.Empire;
import com.starempires.objects.IdentifiableObject;
import com.starempires.orders.Order;
import com.starempires.orders.OrderType;
import com.starempires.orders.RemoveKnownOrder;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public class RemoveKnownItemsPhaseUpdater extends PhaseUpdater {

    public RemoveKnownItemsPhaseUpdater(final TurnData turnData) {
        super(Phase.REMOVE_KNOWN_ITEMS, turnData);
    }

    private <T extends IdentifiableObject> void removeKnowledge(
            final Order order,
            final Empire empire,
            final Predicate<T> action,
            final String type,
            final T object) {
        if (action.test(object)) {
            final String message = "Removed %s knowledge of %s %s".formatted(empire, type, object);
            addNews(empire, message);
            addNews(order.getEmpire(), message);
        }
    }

    @Override
    public void update() {
        final List<Order> orders = turnData.getOrders(OrderType.REMOVEKNOWN);
        orders.forEach(o -> {
            final RemoveKnownOrder order = (RemoveKnownOrder) o;
            Optional.ofNullable(order.getRecipients())
                    .ifPresent(recipients -> recipients.forEach(recipient -> {
                        Optional.ofNullable(order.getWorlds())
                                .ifPresent(worlds -> worlds.forEach(world ->
                                        removeKnowledge(order, recipient, recipient::removeKnownWorld, "world", world)));
                        
                        Optional.ofNullable(order.getPortals())
                                .ifPresent(portals -> portals.forEach(portal ->
                                        removeKnowledge(order, recipient, recipient::removeKnownPortal, "portal", portal)));
                        
                        Optional.ofNullable(order.getStorms())
                                .ifPresent(storms -> storms.forEach(storm ->
                                        removeKnowledge(order, recipient, recipient::removeKnownStorm, "storm", storm)));
                        
                        Optional.ofNullable(order.getShipClasses())
                                .ifPresent(shipClasses -> shipClasses.forEach(shipClass ->
                                        removeKnowledge(order, recipient, recipient::removeKnownShipClass, "ship class", shipClass)));
                        
                        Optional.ofNullable(order.getContacts())
                                .ifPresent(contacts -> contacts.forEach(contact ->
                                        removeKnowledge(order, recipient, recipient::removeKnownEmpire, "contact", contact)));
                    }));
        });
    }
}