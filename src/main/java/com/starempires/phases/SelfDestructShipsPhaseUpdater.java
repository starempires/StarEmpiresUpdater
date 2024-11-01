package com.starempires.phases;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.starempires.TurnData;
import com.starempires.constants.Constants;
import com.starempires.objects.Coordinate;
import com.starempires.objects.Empire;
import com.starempires.objects.Order;
import com.starempires.objects.OrderType;
import com.starempires.objects.Ship;
import com.starempires.objects.ShipCondition;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SelfDestructShipsPhaseUpdater extends PhaseUpdater {

    public SelfDestructShipsPhaseUpdater(final TurnData turnData) {
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
                Collection<Empire> newsEmpires = turnData.getEmpiresPresent(ship);
                addNews(newsEmpires, "Ship " + ship + " receives " + damage + " collateral damage.");
            }
        });
    }

    private void destructShips(final Multimap<Coordinate, Ship> selfDestructs) {
        selfDestructs.asMap().entrySet().forEach(entry -> {
            // for each sector where self-destruction occurs, apply collateral damage
            int collateralTonnage = 0;
            final Coordinate coordinate = entry.getKey();
            final Collection<Ship> destructedShips = entry.getValue();
            final Set<Ship> cargos = new HashSet<Ship>();
            final Collection<Empire> newsEmpires = turnData.getEmpiresPresent(coordinate);
            // count collateral damage from destroyed ships, note their cargo
            collateralTonnage += destructedShips.stream().map(Ship::getTonnage).mapToInt(i -> i).sum();
            destructedShips.forEach(ship -> {
                cargos.addAll(ship.getCargo());
                ship.destruct();
            });

            // destroy and count collateral damage from destroyed cargo
            collateralTonnage += cargos.stream().map(Ship::getTonnage).mapToInt(i -> i).sum();
            cargos.forEach(ship -> {
                addNews(newsEmpires,
                        "Loaded cargo " + ship + " (" + plural(ship.getTonnage(), "tonne") + ") was destroyed.");
                int dpRemaining = ship.getDpRemaining();
                ship.inflictCombatDamage(dpRemaining);
            });
            applyCollateralDamage(coordinate, collateralTonnage);
        });
    }

    @Override
    public void update() {
        final List<Order> orders = turnData.getOrders(OrderType.DESTRUCT);
        final Multimap<Coordinate, Ship> selfDestructs = HashMultimap.create();
        orders.forEach(order -> {
            final Empire empire = order.getEmpire();
            final List<String> handles = order.getParameters();
            handles.forEach(handle -> {
                final Ship ship = empire.getShip(handle);
                if (ship == null) {
                    addNewsResult(order, empire, "You do not own ship " + handle);
                }
                else if (ship.isStarbase()) {
                    addNewsResult(order, empire, "Starbase " + ship + " cannot be self-destructed.");
                }
                else {
                    final Coordinate coordinate = ship.getCoordinate();
                    selfDestructs.put(coordinate, ship);
                    final Collection<Empire> newsEmpires = turnData.getEmpiresPresent(coordinate);
                    addNewsResult(order, newsEmpires,
                            "Ship " + ship + " (" + plural(ship.getTonnage(), "tonne") + ") self-destructed.");

                }
            });
        });
        destructShips(selfDestructs);
    }
}