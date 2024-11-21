package com.starempires.phases;

import com.google.common.collect.Sets;
import com.starempires.TurnData;
import com.starempires.objects.Empire;
import com.starempires.orders.Order;
import com.starempires.orders.OrderType;
import com.starempires.objects.Portal;
import com.starempires.objects.Ship;
import com.starempires.objects.Storm;
import com.starempires.objects.World;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
      parameters are
      REMOVE [WORLD|PORTAL|STORM] object1 object2 ....
      REMOVE SHIP empire ship1 ship2 ....
*/
public class RemoveMapObjectsPhaseUpdater extends PhaseUpdater {

    public RemoveMapObjectsPhaseUpdater(final TurnData turnData) {
        super(Phase.MAP_REMOVE_OBJECTS, turnData);
    }

    private void removeWorlds(final Order order, final List<String> worldNames) {
        worldNames.forEach(worldName -> {
            final World world = turnData.getWorld(worldName);
            final Collection<Empire> empires = turnData.getEmpiresPresent(world);
            turnData.removeWorld(world);
            addNewsResult(order, empires, "World %s has been removed".formatted(world));
        });
    }

    private void removePortals(final Order order, final List<String> portalNames) {
        portalNames.forEach(portalName -> {
            final Portal portal = turnData.getPortal(portalName);
            final Collection<Empire> empires = turnData.getEmpiresPresent(portal);
            turnData.removePortal(portal);
            addNewsResult(order, empires, "Portal %s has been removed".formatted(portal));
        });
    }

    private void removeStorms(final Order order, final List<String> stormNames) {
        stormNames.forEach(stormName -> {
            final Storm storm = turnData.getStorm(stormName);
            final Collection<Empire> empires = turnData.getEmpiresPresent(storm);
            turnData.removeStorm(storm);
            addNewsResult(order, empires, "Storm %s has been removed".formatted(storm));
        });
    }

    private void removeShips(final Order order, final String empireName, final List<String> handles) {
        final Empire empire = turnData.getEmpire(empireName);
        final Collection<Ship> ships = empire.getShips(handles);
        ships.forEach(ship -> {
            final Set<Empire> empires = Sets.newHashSet();
            if (ship.isLoaded()) {
                empires.add(ship.getOwner());
                empires.add(ship.getCarrier().getOwner());
                ship.getCarrier().unloadCargo(ship);
            } else {
                empires.addAll(turnData.getEmpiresPresent(ship));
            }

            ship.getCargo().forEach(Ship::unloadFromCarrier);
            empire.removeShip(ship);
            addNewsResult(order, empires, "Ship %s has been removed".formatted(ship));
        });
    }

    @Override
    public void update() {
        // TODO createSyntheticRemovalsFromMoveOrder
        final List<Order> orders = turnData.getOrders(OrderType.MAPREMOVE);
        for (final Order order : orders) {
//
//            final String objectType = order.getStringParameter(0);
//            final List<String> tokens = order.getParameterSubList(1);
//            switch (MapObject.valueOf(objectType.toUpperCase())) {
//                case WORLD -> removeWorlds(order, tokens);
//                case PORTAL -> removePortals(order, tokens);
//                case STORM -> removeStorms(order, tokens);
//                case SHIP -> {
//                    String empireName = tokens.get(0);
//                    List<String> handles = tokens.subList(1, tokens.size());
//                    removeShips(order, empireName, handles);
//                }
//                default -> addNewsResult(order, order.getEmpire(), "Unknown map object type: " + objectType);
//            }
        }
    }
}