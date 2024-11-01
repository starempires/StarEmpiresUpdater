package com.starempires.phases;

import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Ordering;
import com.google.common.collect.TreeMultimap;
import com.starempires.TurnData;
import com.starempires.constants.Constants;
import com.starempires.objects.Coordinate;
import com.starempires.objects.Empire;
import com.starempires.objects.Order;
import com.starempires.objects.OrderType;
import com.starempires.objects.Ship;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.Validate;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.NavigableSet;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FireGunsPhaseUpdater extends PhaseUpdater {

    static enum TargetOrder {
        ASCENDING,
        DESCENDING;
    }

    static class TargetComparator implements Comparator<Ship> {
        private final TargetOrder targetOrder_;

        public TargetComparator(final TargetOrder targetOrder) {
            Validate.notNull(targetOrder, "Must specify TargetOrder for TargetComparator");
            targetOrder_ = targetOrder;
        }

        @Override
        public int compare(Ship ship1, Ship ship2) {
            int rv = Boolean.compare(ship2.isWing(), ship1.isWing());
            if (rv == 0) {
                rv = Boolean.compare(ship1.isMissile(), ship2.isMissile());
                if (rv == 0) {
                    rv = ship1.getDpRemaining() - ship2.getDpRemaining();
                    if (targetOrder_ == TargetOrder.DESCENDING) {
                        rv = -rv;
                    }
                    if (rv == 0) {
                        rv = Double.compare(ship1.getOperationRating(), ship2.getOperationRating());
                        if (targetOrder_ == TargetOrder.DESCENDING) {
                            rv = -rv;
                        }
                        if (rv == 0) {
                            rv = ship1.getTonnage() - ship2.getTonnage();
                            if (targetOrder_ == TargetOrder.DESCENDING) {
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
        public int compare(Ship ship1, Ship ship2) {
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
    protected Ship getAttacker(final Order order, final String attackerGun) {
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

    public void partitionAttackers(final Order order, final List<Ship> attackers, final List<Ship> gunships,
            final Multimap<Integer, Ship> missiles) {
        for (Ship attacker : attackers) {
            if (attacker.isMissile()) {
                if (attacker.isLoaded() || attacker.wasJustUnloaded()) {
                    missiles.put(attacker.getAvailableGuns(), attacker);
                }
                else {
                    order.addResult("Missile " + attacker + " is not ready to fire; skipping");
                }
            }
            else if (!attacker.isLoaded()) {
                if (attacker.getAvailableGuns() > 0) {
                    gunships.add(attacker);
                }
                else {
                    order.addResult("Attacker " + attacker + " has no operational guns; skipping");
                }
            }
            else {
                order.addResult("Omitting loaded ship " + attacker);
            }
        }
    }

    Ship getNextGunship(final List<Ship> gunships) {
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

    protected static Ship selectMissile(final TreeMultimap<Integer, Ship> missiles, final int targetDp) {
        final Integer key = missiles.keySet().ceiling(targetDp);
        if (key != null) {
            final NavigableSet<Ship> keyMissiles = missiles.get(key);
            if (keyMissiles.isEmpty()) {
                return null;
            }
            final Ship missile = keyMissiles.first();
            missiles.remove(key, missile);
            return missile;
        }
        return null;
    }

    public void resolveCombat(final Order order, final List<Ship> gunships, TreeMultimap<Integer, Ship> missiles,
            final List<Ship> targets) {
        int totalUnfiredGuns = gunships.stream().map(Ship::getUnfiredGuns).mapToInt(i -> i).sum();

        for (final Ship target : targets) {
            final Collection<Empire> empiresInSector = turnData.getEmpiresPresent(target);
            int dpRemaining = target.getDpRemaining();
            while (dpRemaining > 0 && (totalUnfiredGuns > 0 || !missiles.isEmpty())) { // target still there, attackers remain
                if (totalUnfiredGuns >= dpRemaining || missiles.isEmpty()) {
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

    List<Ship> gatherValidTargets(final Order order, final List<String> empireNames, final Coordinate coordinate) {
        return empireNames.stream().flatMap(empireName -> {
            final Empire empire = turnData.getEmpire(empireName);
            if (empire == null) {
                order.addResult("Skipping unknown target empire " + empireName);
                return Stream.of();
            }
            else {
                final Collection<Ship> empireTargets = empire.getShips(coordinate);
                order.addResult("Found %d targets for empire %s".formatted(empireTargets.size(), empire));
                return empireTargets.stream();
            }
        }).collect(Collectors.toList());
    }

    List<Ship> gatherValidAttackers(final Order order, final List<String> attackerGuns) {
        final List<Ship> attackers = attackerGuns.stream().map(attackerGun -> {
            Ship ship = getAttacker(order, attackerGun);
            if (ship == null) {
                order.addResult("Omitting unknown attacker " + attackerGun);
                return null;
            }
            if (!ship.isAlive()) {
                order.addResult("Omitting dead attacker " + ship);
                return null;
            }
            return ship;
        }).filter(Objects::nonNull)
                .collect(Collectors.toList());
        final Coordinate coordinate = attackers.stream().findAny().map(Ship::getCoordinate).orElse(null);
        final boolean sameSector = attackers.stream().allMatch(attacker -> attacker.getCoordinate() == coordinate);
        if (!sameSector) {
            order.addResult("Attackers not all in same sector");
            return Collections.emptyList();
        }
        return attackers;
    }

    @Override
    public void update() {
        final List<Order> orders = turnData.getOrders(OrderType.FIRE);
        orders.forEach(order -> {
            // parameters are FIRE [ascending|descending] <attackers> AT <defenders>
            String first = order.getStringParameter(0);
            final List<String> attackerGuns;
            final int index = order.indexOfIgnoreCase(Constants.TOKEN_AT);
            final TargetOrder firstParam = EnumUtils.getEnum(TargetOrder.class, first.toUpperCase().trim(), TargetOrder.ASCENDING);
            final TargetOrder targetOrder = ObjectUtils.firstNonNull(firstParam, TargetOrder.ASCENDING);
            if (firstParam == null) {
                attackerGuns = order.getParameterSubList(0, index);
            }
            else {
                attackerGuns = order.getParameterSubList(1, index);
            }
            final List<String> targetHandles = order.getParameterSubList(index + 1);
            final List<Ship> attackers = gatherValidAttackers(order, attackerGuns);
            if (attackers.isEmpty()) {
                order.addResult("No valid attackers found");
                return;
            }

            final Coordinate coordinate = attackers.stream().findAny().get().getCoordinate();
            final List<Ship> targets = gatherValidTargets(order, targetHandles, coordinate);
            if (targets.isEmpty()) {
                order.addResult("No valid targets found");
                return;
            }

            final List<Ship> gunships = Lists.newArrayList();
            final TreeMultimap<Integer, Ship> missiles = TreeMultimap.create(Ordering.natural(), ATTACKER_COMPARATOR);
            partitionAttackers(order, attackers, gunships, missiles);
            attackers.sort(ATTACKER_COMPARATOR);

            final List<Ship> sortedTargets = Lists.newArrayList(targets);
            final TargetComparator targetComparator = new TargetComparator(targetOrder);
            sortedTargets.sort(targetComparator);
            resolveCombat(order, gunships, missiles, sortedTargets);
        });
    }

}