package com.starempires.phases;

import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Ordering;
import com.google.common.collect.TreeMultimap;
import com.starempires.TurnData;
import com.starempires.objects.Coordinate;
import com.starempires.objects.Empire;
import com.starempires.objects.MappableObject;
import com.starempires.objects.Order;
import com.starempires.objects.OrderType;
import com.starempires.objects.Ship;
import lombok.NonNull;
import org.apache.commons.lang3.EnumUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.NavigableSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FireGunsPhaseUpdater extends PhaseUpdater {

    final static private String SHIPS_GROUP = "ships";
    final static private String TARGETS_GROUP = "targets";
    final static private String TARGET_ORDER_GROUP = "targetorder";
    final static private String COORDINATE_GROUP = "coordinate";
    final static private String LOCATION_GROUP = "location";

    // parameters are FIRE [ascending|descending] (oblique,y) AT empire1 [empire2 ...]
    final static private String COORDINATE_PARAMETERS_REGEX = "^fire\\s+(?<" + COORDINATE_GROUP + ">\\(?-?[0-9]+,\\s*-?[0-9]+\\)?)\\s+(asc|desc)?\\s+at";
    // parameters are FIRE [ascending|descending] @location AT empire1 [empire2 ...]
    final static private String LOCATION_PARAMETERS_REGEX = "^fire\\s+@(?<" + LOCATION_GROUP + ">\\w+)\\s+(asc|desc)?\\s+at";
    // parameters are FIRE [ascending|descending] ship1 [ship2 ...] AT empire1 [empire2 ...]
    final static private String SHIPS_PARAMETERS_REGEX = "^fire\\s+(?<" + SHIPS_GROUP + ">[\\w]+(?:\\s+[\\w]+)*)\\s+(asc|desc)?\\s+at";
    final static private String TARGET_PARAMETERS_REGEX = "^(?<" + TARGET_ORDER_GROUP + ">asc|desc)?\\s+at\\s+(?<" + TARGETS_GROUP + ">[\\\\w]+(?:\\\\s+[\\\\w]+)*)$";

    final static private Pattern COORDINATE_PATTERN = Pattern.compile(COORDINATE_PARAMETERS_REGEX, Pattern.CASE_INSENSITIVE);
    final static private Pattern LOCATION_PATTERN = Pattern.compile(LOCATION_PARAMETERS_REGEX, Pattern.CASE_INSENSITIVE);
    final static private Pattern SHIPS_PATTERN = Pattern.compile(SHIPS_PARAMETERS_REGEX, Pattern.CASE_INSENSITIVE);
    final static private Pattern TARGET_PATTERN = Pattern.compile(TARGET_PARAMETERS_REGEX, Pattern.CASE_INSENSITIVE);

    static enum TargetOrder {
        ASCENDING,
        DESCENDING;
    }

    static class TargetComparator implements Comparator<Ship> {
        private final TargetOrder targetOrder;

        public TargetComparator(@NonNull final TargetOrder targetOrder) {
            this.targetOrder = targetOrder;
        }

        @Override
        public int compare(@NonNull final Ship ship1, @NonNull final Ship ship2) {
            int rv = Boolean.compare(ship2.isWing(), ship1.isWing());
            if (rv == 0) {
                rv = Boolean.compare(ship1.isMissile(), ship2.isMissile());
                if (rv == 0) {
                    rv = ship1.getDpRemaining() - ship2.getDpRemaining();
                    if (targetOrder == TargetOrder.DESCENDING) {
                        rv = -rv;
                    }
                    if (rv == 0) {
                        rv = Double.compare(ship1.getOperationRating(), ship2.getOperationRating());
                        if (targetOrder == TargetOrder.DESCENDING) {
                            rv = -rv;
                        }
                        if (rv == 0) {
                            rv = ship1.getTonnage() - ship2.getTonnage();
                            if (targetOrder == TargetOrder.DESCENDING) {
                                rv = -rv;
                            }
                            if (rv == 0) {
                                rv = ship1.getHandle().compareToIgnoreCase(ship2.getHandle());
                            }
                        }
                    }
                }
            }
            return rv;
        }
    }

    static class AttackerComparator implements Comparator<Ship> {
        @Override
        public int compare(@NonNull final Ship ship1, @NonNull final Ship ship2) {
            int rv = ship1.getUnfiredGuns() - ship2.getUnfiredGuns();
            if (rv == 0) {
                rv = ship1.getAvailableGuns() - ship2.getAvailableGuns();
                if (rv == 0) {
                    rv = Double.compare(ship1.getOperationRating(), ship2.getOperationRating());
                    if (rv == 0) {
                        rv = ship1.getGuns() - ship2.getGuns();
                        if (rv == 0) {
                            rv = ship1.getTonnage() - ship2.getTonnage();
                            if (rv == 0) {
                                rv = ship1.getHandle().compareToIgnoreCase(ship2.getHandle());
                            }
                        }
                    }
                }
            }
            return rv;
        }
    }

    private static final AttackerComparator ATTACKER_COMPARATOR = new AttackerComparator();

    public FireGunsPhaseUpdater(final TurnData turnData) {
        super(Phase.FIRE_GUNS, turnData);
    }

    /**
     * Find attacker Ship from order string
     * 
     * @param order
     *            The Order for this attack
     * @param attackerGun
     *            The attacker in handle:guns-to-fire format
     * @return Attacking Ship object with guns readied to fire
     */
    private Ship getAttacker(final Order order, final String attackerGun) {
        final String[] tokens = attackerGun.split(":");
        final String handle;
        final int gunsToFire;
        if (tokens.length == 1) { // no guns specified
            handle = attackerGun;
            gunsToFire = Integer.MAX_VALUE;
        }
        else { // handle:guns format
            handle = tokens[0];
            gunsToFire = Integer.parseInt(tokens[1]);
        }
        final Ship attacker = order.getEmpire().getShip(handle);
        if (attacker == null) {
            order.addResult("Unknown attacker " + handle);
        }
        else {
            attacker.orderGunsToFire(gunsToFire);
        }
        return attacker;
    }

    private void partitionAttackers(final Order order, final List<Ship> attackers, final List<Ship> gunships,
            final Multimap<Integer, Ship> missiles) {
        for (Ship attacker : attackers) {
            if (attacker.isMissile()) {
                missiles.put(attacker.getAvailableGuns(), attacker);
            }
            else if (attacker.getAvailableGuns() > 0) {
                gunships.add(attacker);
            }
            else {
                order.addResult("Attacker %s has no operational guns; skipping".formatted(attacker));
            }
        }
    }

    private Ship getNextGunship(final List<Ship> gunships) {
        Ship rv = null;
        boolean done = false;
        while (!done) {
            if (gunships.isEmpty()) {
                done = true;
            }
            else {
                Ship gunship = gunships.get(0);
                if (gunship.hasUnfiredGuns()) {
                    rv = gunship;
                    done = true;
                }
                else {
                    gunships.remove(0);
                }
            }
        }
        return rv;
    }

    /**
     * Return either the missile the min number of guns needed to destroy the targetDp, or else return the
     * missile with most guns less than the targetDp.
     */
    private static Ship selectMissile(final TreeMultimap<Integer, Ship> missiles, final int targetDp) {
        Integer key = missiles.keySet().ceiling(targetDp);
        if (key == null) {
            key = missiles.keySet().floor(targetDp);
        }
        final NavigableSet<Ship> keyMissiles = missiles.get(key);
        final Ship missile = keyMissiles.first();
        missiles.remove(key, missile);
        return missile;
    }

    private void resolveCombat(final Order order, final List<Ship> gunships, TreeMultimap<Integer, Ship> missiles,
            final List<Ship> targets) {
        int totalUnfiredGuns = gunships.stream().map(Ship::getUnfiredGuns).mapToInt(i -> i).sum();

        for (final Ship target : targets) {
            final Collection<Empire> empiresInSector = turnData.getEmpiresPresent(target);
            int dpRemaining = target.getDpRemaining();
            while (dpRemaining > 0 && (totalUnfiredGuns > 0 || !missiles.isEmpty())) { // target still there, attackers remain
                if (totalUnfiredGuns >= dpRemaining || missiles.isEmpty()) {
                    // unfired guns are greater than target remaining DP or else out of missiles and firing guns as much as possible
                    final Ship gunship = getNextGunship(gunships);
                    if (gunship == null) {
                        break;
                    }
                    int gunsToFire = Math.min(gunship.getUnfiredGuns(), dpRemaining);
                    target.inflictCombatDamage(gunsToFire);
                    gunship.fireGuns(gunsToFire);
                    dpRemaining -= gunsToFire;
                    totalUnfiredGuns -= gunsToFire;
                    addNewsResult(order, empiresInSector,
                            "Ship " + gunship + " fired " + plural(gunsToFire, "gun") + " at target " + target);
                }
                else { // select a missile to fire
                    final Ship missile = selectMissile(missiles, dpRemaining);
                    final int missileGuns = turnData.fireMissile(missile, target);
                    addNewsResult(order, empiresInSector,
                            "Missile " + missile + " hit target " + target + " for " + missileGuns + " damage");
                }
            }
        }
    }

    private List<Ship> gatherValidTargets(final Order order, final List<String> empireNames, final Coordinate coordinate) {
        return empireNames.stream().flatMap(empireName -> {
            final Empire empire = turnData.getEmpire(empireName);
            if (empire == null) {
                order.addResult("Skipping unknown target empire %s".formatted(empireName));
                return Stream.of();
            }
            else {
                final Collection<Ship> empireTargets = empire.getShips(coordinate);
                order.addResult("Found %d targets for empire %s".formatted(empireTargets.size(), empire));
                return empireTargets.stream();
            }
        }).collect(Collectors.toList());
    }

    private List<Ship> gatherValidAttackers(final Order order, final List<Ship> possibleAttackers) {
        final Empire empire = order.getEmpire();
        final List<Ship> validAttackers = Lists.newArrayList();
        for (final Ship attacker: possibleAttackers) {
            if (!attacker.isAlive()) {
                order.addResult("Omitting destroyed attacker %s".formatted(attacker));
            }
            else if (attacker.isMissile() && !attacker.isLoaded() && !attacker.wasJustUnloaded()) {
                order.addResult("Omitting unloaded missile %s".formatted(attacker));
            }
            else if (attacker.isLoaded()) {
                order.addResult("Omitting loaded attacker %s".formatted(attacker));
            }
            else {
                validAttackers.add(attacker);
            }
        }
        final Coordinate coordinate = validAttackers.stream().findAny().map(Ship::getCoordinate).orElse(null);
        final boolean sameSector = validAttackers.stream().allMatch(attacker -> attacker.getCoordinate() == coordinate);
        if (!sameSector) {
            order.addResult("Attackers not all in same sector");
            return Collections.emptyList();
        }
        return validAttackers;
    }

    private List<Ship> getValidAttackers(@NonNull final Order order) {
        // parse the order according to its format
        final Matcher coordinateMatcher = COORDINATE_PATTERN.matcher(order.getParametersAsString());
        final Matcher locationMatcher = LOCATION_PATTERN.matcher(order.getParametersAsString());
        final Matcher shipsMatcher = SHIPS_PATTERN.matcher(order.getParametersAsString());

        List<Ship> possibleAttackers = Lists.newArrayList();
        Empire empire = order.getEmpire();
        if (coordinateMatcher.matches()) {
            String coordinateText = coordinateMatcher.group(COORDINATE_GROUP);
            Coordinate coordinate = Coordinate.parse(coordinateText);
            possibleAttackers.addAll(empire.getLiveShips(coordinate));
        } else if (locationMatcher.matches()) {
            String location = locationMatcher.group(LOCATION_GROUP);
            MappableObject mapObject = turnData.getWorld(location);
            if (mapObject == null) {
                mapObject = turnData.getPortal(location);
                if (mapObject == null) {
                    mapObject = turnData.getStorm(location);
                }
            }
            if (mapObject == null) {
                addNewsResult(order, "Unknown location %s".formatted(location));
            } else {
                possibleAttackers.addAll(empire.getLiveShips(mapObject.getCoordinate()));
            }
        } else if (shipsMatcher.matches()) {
            final String attackerNames = shipsMatcher.group(SHIPS_GROUP);
            for (String attackerName : attackerNames.split(" ")) {
                Ship ship = empire.getShip(attackerName);
                if (ship == null) {
                    order.addResult("Omitting unknown attacker %s".formatted(attackerName));
                } else {
                    possibleAttackers.add(ship);
                }
            }
        }

        return gatherValidAttackers(order, possibleAttackers);
    }

    private List<Ship> getSortedValidTargets(@NonNull final Order order, final Coordinate coordinate) {
        final Matcher targetMatcher = TARGET_PATTERN.matcher(order.getParametersAsString());

        List<Ship> validTargets = Lists.newArrayList();
        if (!targetMatcher.matches()) {
            String[] targetEmpires = targetMatcher.group(TARGETS_GROUP).split(" ");
            String targetOrderText = targetMatcher.group(TARGET_ORDER_GROUP);
            final TargetOrder targetOrder = EnumUtils.getEnum(TargetOrder.class, targetOrderText.toUpperCase().trim(), TargetOrder.ASCENDING);
            final TargetComparator targetComparator = new TargetComparator(targetOrder);
            for (final String targetEmpire: targetEmpires) {
                final Empire empire = turnData.getEmpire(targetEmpire);
                if (empire == null) {
                    order.addResult("Skipping unknown target empire %s".formatted(targetEmpire));
                }
                else {
                    final List<Ship> empireTargets = Lists.newArrayList(empire.getShips(coordinate));
                    order.addResult("Found %d targets for empire %s".formatted(empireTargets.size(), empire));
                    empireTargets.sort(targetComparator);
                    validTargets.addAll(empireTargets);
                }
            }
        }
        return validTargets;
    }

    @Override
    public void update() {
        final List<Order> orders = turnData.getOrders(OrderType.FIRE);
        orders.forEach(order -> {
            List<Ship> validAttackers = getValidAttackers(order);
            if (validAttackers.isEmpty()) {
                order.addResult("No valid attackers found");
                return;
            }

            final Coordinate coordinate = validAttackers.stream().findAny().map(Ship::getCoordinate).get();
            List<Ship> validTargets = getSortedValidTargets(order, coordinate);
            if (validTargets.isEmpty()) {
                order.addResult("No valid targets found");
                return;
            }

            final List<Ship> gunships = Lists.newArrayList();
            final TreeMultimap<Integer, Ship> missiles = TreeMultimap.create(Ordering.natural(), ATTACKER_COMPARATOR);
            partitionAttackers(order, validAttackers, gunships, missiles);
            validAttackers.sort(ATTACKER_COMPARATOR);
            resolveCombat(order, gunships, missiles, validTargets);
        });
    }
}