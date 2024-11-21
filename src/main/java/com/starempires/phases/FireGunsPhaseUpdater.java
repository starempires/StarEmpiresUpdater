package com.starempires.phases;

import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Ordering;
import com.google.common.collect.TreeMultimap;
import com.starempires.TurnData;
import com.starempires.objects.Coordinate;
import com.starempires.objects.Empire;
import com.starempires.objects.Ship;
import com.starempires.orders.FireOrder;
import com.starempires.orders.Order;
import com.starempires.orders.OrderType;
import lombok.NonNull;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.NavigableSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FireGunsPhaseUpdater extends PhaseUpdater {

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
     * @param order       The Order for this attack
     * @param attackerGun The attacker in handle:guns-to-fire format
     * @return Attacking Ship object with guns readied to fire
     */
    private Ship getAttacker(final Order order, final String attackerGun) {
        final String[] tokens = attackerGun.split(":");
        final String handle;
        final int gunsToFire;
        if (tokens.length == 1) { // no guns specified
            handle = attackerGun;
            gunsToFire = Integer.MAX_VALUE;
        } else { // handle:guns format
            handle = tokens[0];
            gunsToFire = Integer.parseInt(tokens[1]);
        }
        final Ship attacker = order.getEmpire().getShip(handle);
        if (attacker == null) {
            order.addResult("Unknown attacker " + handle);
        } else {
            attacker.orderGunsToFire(gunsToFire);
        }
        return attacker;
    }

    private void partitionAttackers(final FireOrder order, final List<Ship> attackers, final List<Ship> gunships,
                                    final Multimap<Integer, Ship> missiles) {
        for (Ship attacker: attackers) {
            if (attacker.isMissile()) {
                missiles.put(attacker.getAvailableGuns(), attacker);
            } else if (attacker.getAvailableGuns() > 0) {
                gunships.add(attacker);
            } else {
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
            } else {
                Ship gunship = gunships.get(0);
                if (gunship.hasUnfiredGuns()) {
                    rv = gunship;
                    done = true;
                } else {
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
                } else { // select a missile to fire
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
            } else {
                final Collection<Ship> empireTargets = empire.getShips(coordinate);
                order.addResult("Found %d targets for empire %s".formatted(empireTargets.size(), empire));
                return empireTargets.stream();
            }
        }).collect(Collectors.toList());
    }

    private List<Ship> gatherValidAttackers(final FireOrder order, final List<Ship> possibleAttackers) {
        final Empire empire = order.getEmpire();
        final List<Ship> validAttackers = Lists.newArrayList();
        for (final Ship attacker : possibleAttackers) {
            if (!attacker.isAlive()) {
                order.addResult("Omitting destroyed attacker %s".formatted(attacker));
            } else if (attacker.isMissile() && !attacker.isLoaded() && !attacker.wasJustUnloaded()) {
                order.addResult("Omitting unloaded missile %s".formatted(attacker));
            } else if (attacker.isLoaded()) {
                order.addResult("Omitting loaded attacker %s".formatted(attacker));
            } else {
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

    private List<Ship> getSortedValidTargets(@NonNull final FireOrder order) {
        List<Empire> targets = order.getTargets();
        final TargetComparator targetComparator = new TargetComparator(order.isAscending() ? TargetOrder.ASCENDING : TargetOrder.DESCENDING);
        final List<Ship> validTargets = Lists.newArrayList();
        for (final Empire empire : targets) {
            final List<Ship> empireTargets = Lists.newArrayList(empire.getShips(order.getCoordinate()));
            order.addResult("Found %d targets for empire %s".formatted(empireTargets.size(), empire));
            empireTargets.sort(targetComparator);
            validTargets.addAll(empireTargets);
        }
        return validTargets;
    }

    @Override
    public void update() {
        final List<Order> orders = turnData.getOrders(OrderType.FIRE);
        orders.forEach(o -> {
            final FireOrder order = (FireOrder) o;

            final List<Ship> validTargets = getSortedValidTargets(order);
            if (validTargets.isEmpty()) {
                order.addResult("No valid targets found");
                return;
            }

            final List<Ship> attackers = order.getShips().stream().filter(Ship::isAlive).toList();

            final List<Ship> gunships = Lists.newArrayList();
            final TreeMultimap<Integer, Ship> missiles = TreeMultimap.create(Ordering.natural(), ATTACKER_COMPARATOR);
            partitionAttackers(order,  attackers, gunships, missiles);
            gunships.sort(ATTACKER_COMPARATOR);
            resolveCombat(order, gunships, missiles, validTargets);
        });
    }
}