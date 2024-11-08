package com.starempires.phases;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
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
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SelfDestructShipsPhaseUpdater extends PhaseUpdater {

    final private String SHIPS_GROUP = "ships";
    final private String PARAMETERS_REGEX = "^destruct (?<" + SHIPS_GROUP + ">[\\w]+(?:\\s+[\\w]+)*)$";
    final private Pattern PATTERN = Pattern.compile(PARAMETERS_REGEX, Pattern.CASE_INSENSITIVE);

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
                final Collection<Empire> newsEmpires = turnData.getEmpiresPresent(ship);
                addNews(newsEmpires, "Ship " + ship + " receives " + damage + " collateral damage.");
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
        orders.forEach(order -> {
            final Empire empire = order.getEmpire();
            final Matcher matcher = PATTERN.matcher(order.getParametersAsString());
            final String[] handles = matcher.group(SHIPS_GROUP).split(" ");
            for (String handle: handles) {
                final Ship ship = empire.getShip(handle);
                if (ship == null) {
                    addNewsResult(order, "You do not own ship " + handle);
                }
                else if (ship.isStarbase()) {
                    addNewsResult(order, "Starbase " + ship + " cannot be self-destructed.");
                }
                else if (ship.isLoaded()) {
                    addNewsResult(order, "Ship %s is loaded onto carrier %s and cannot be self-destructed.".formatted(ship, ship.getCarrier().getHandle()));
                }
                else {
                    final Coordinate coordinate = ship.getCoordinate();
                    final Collection<Empire> newsEmpires = turnData.getEmpiresPresent(coordinate);
                    addNewsResult(order, newsEmpires,
                            "Ship " + ship + " (" + plural(ship.getTonnage(), "tonne") + ") self-destructed.");

                    selfDestructs.put(coordinate, ship);

                    final List<Ship> cargos = Lists.newArrayList(ship.getCargo());
                    cargos.sort(Comparator.comparing(Ship::getHandle));
                    for (Ship cargo: cargos) {
                        selfDestructs.put(coordinate, cargo);
                        addNewsResult(order, newsEmpires,
                                "Loaded cargo " + cargo + " (" + plural(cargo.getTonnage(), "tonne") + ") self-destructed.");
                    }
                }
            };
        });
        destructShips(selfDestructs);
    }
}