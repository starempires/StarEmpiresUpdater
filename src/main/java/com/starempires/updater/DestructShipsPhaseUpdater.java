package com.starempires.updater;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.starempires.TurnData;
import com.starempires.constants.Constants;
import com.starempires.objects.Coordinate;
import com.starempires.objects.Empire;
import com.starempires.objects.Ship;
import com.starempires.objects.ShipCondition;
import com.starempires.orders.DestructOrder;
import com.starempires.orders.Order;
import com.starempires.orders.OrderType;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;

public class DestructShipsPhaseUpdater extends PhaseUpdater {

    public DestructShipsPhaseUpdater(final TurnData turnData) {
        super(Phase.SELF_DESTRUCT_SHIPS, turnData);
    }

    private void applyCollateralDamage(final Coordinate coordinate, final int tonnage) {
        final int interval = turnData.getIntParameter(Constants.PARAMETER_SELF_DESTRUCT_TONNAGE_INTERVAL,
                Constants.DEFAULT_SELF_DESTRUCT_TONNAGE_INTERVAL);
        final int damage = (int) Math.ceil((double) tonnage / (double) interval);

        final Collection<Ship> shipsInSector = turnData.getLiveShips(coordinate);
        shipsInSector.forEach(ship -> {
            if (!ship.hasCondition(ShipCondition.SELF_DESTRUCTED)) {
                ship.inflictCombatDamage(damage);
                final Collection<Empire> newsEmpires = turnData.getEmpiresPresent(ship);
                addNews(newsEmpires, "%s ship %s received %d collateral damage".formatted(ship.getOwner(), ship, damage));
            }
        });
    }

    private void destructShips(final Multimap<Coordinate, Ship> selfDestructs) {
        selfDestructs.asMap().forEach((coordinate, destructedShips) -> {
            // for each sector where self-destruction occurs, apply collateral damage
            final Collection<Empire> newsEmpires = turnData.getEmpiresPresent(coordinate);
            // count collateral damage from destroyed ships, note their cargo
            final int collateralTonnage = destructedShips.stream().map(Ship::getTonnage).mapToInt(i -> i).sum();
            destructedShips.forEach(Ship::destruct);
            applyCollateralDamage(coordinate, collateralTonnage);
        });
    }

    @Override
    public void update() {
        final List<Order> orders = turnData.getOrders(OrderType.DESTRUCT);
        final Multimap<Coordinate, Ship> selfDestructs = HashMultimap.create();
        orders.forEach(o -> {
            final DestructOrder order = (DestructOrder) o;
            for (Ship ship: order.getShips()) {
                if (ship.isStarbase()) {
                    addNewsResult(order, "Starbase " + ship + " cannot be self-destructed.");
                }
                else if (ship.isLoaded()) {
                    addNewsResult(order, "Ship %s is loaded onto carrier %s and cannot be self-destructed.".formatted(ship, ship.getCarrier().getHandle()));
                }
                else {
                    final Coordinate coordinate = ship.getCoordinate();
                    final Collection<Empire> newsEmpires = turnData.getEmpiresPresent(coordinate);
                    addNewsResult(order, newsEmpires,
                            "%s ship %s (%s) self destructed".formatted(ship.getOwner(), ship, plural(ship.getTonnage(), "tonne")));

                    selfDestructs.put(coordinate, ship);

                    final List<Ship> cargos = Lists.newArrayList(ship.getCargo());
                    cargos.sort(Comparator.comparing(Ship::getHandle));
                    for (Ship cargo: cargos) {
                        selfDestructs.put(coordinate, cargo);
                        addNewsResult(order, newsEmpires,
                                "Loaded cargo %s (%s) self destructed".formatted(cargo, plural(cargo.getTonnage(), "tonne")));
                    }
                }
            };
        });
        destructShips(selfDestructs);
    }
}