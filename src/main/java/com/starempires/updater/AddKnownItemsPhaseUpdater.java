package com.starempires.updater;

import com.starempires.TurnData;
import com.starempires.objects.Empire;
import com.starempires.objects.IdentifiableObject;
import com.starempires.orders.AddKnownOrder;
import com.starempires.orders.Order;
import com.starempires.orders.OrderType;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public class AddKnownItemsPhaseUpdater extends PhaseUpdater {

    public AddKnownItemsPhaseUpdater(final TurnData turnData) {
        super(Phase.ADD_KNOWN_ITEMS, turnData);
    }

    private <T extends IdentifiableObject> void addKnowledge(
            final Order order,
            final Empire empire,
            final Predicate<T> action,
            final String type,
            final T object) {
        if (action.test(object)) {
            final String message = "Added %s knowledge of %s %s".formatted(empire, type, object);
            addNews(empire, message);
            addNews(order.getEmpire(), message);
        }
    }

    @Override
    public void update() {
        final List<Order> orders = turnData.getOrders(OrderType.ADDKNOWN);
        orders.forEach(o -> {
            final AddKnownOrder order = (AddKnownOrder) o;
            Optional.ofNullable(order.getRecipients())
                    .ifPresent(recipients -> recipients.forEach(recipient -> {
                        Optional.ofNullable(order.getWorlds())
                                .ifPresent(worlds -> worlds.forEach(world ->
                                        addKnowledge(order, recipient, recipient::addKnownWorld, "world", world)));
                        
                        Optional.ofNullable(order.getPortals())
                                .ifPresent(portals -> portals.forEach(portal ->
                                        addKnowledge(order, recipient, recipient::addKnownPortal, "portal", portal)));

                        Optional.ofNullable(order.getNavData())
                                .ifPresent(portals -> portals.forEach(portal ->
                                        addKnowledge(order, recipient, recipient::addNavData, "nav data", portal)));

                        Optional.ofNullable(order.getStorms())
                                .ifPresent(storms -> storms.forEach(storm ->
                                        addKnowledge(order, recipient, recipient::addKnownStorm, "storm", storm)));
                        
                        Optional.ofNullable(order.getShipClasses())
                                .ifPresent(shipClasses -> shipClasses.forEach(shipClass ->
                                        addKnowledge(order, recipient, recipient::addKnownShipClass, "ship class", shipClass)));
                        
                        Optional.ofNullable(order.getContacts())
                                .ifPresent(contacts -> contacts.forEach(contact ->
                                        addKnowledge(order, recipient, recipient::addKnownEmpire, "contact", contact)));
                    }));
        });
    }
}