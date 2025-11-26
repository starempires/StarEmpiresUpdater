package com.starempires.updater;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.starempires.TurnData;
import com.starempires.objects.Empire;
import com.starempires.objects.IdentifiableObject;
import com.starempires.objects.Ship;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public abstract class ApplyDamagePhaseUpdater extends PhaseUpdater {
    public ApplyDamagePhaseUpdater(final Phase phase, final TurnData turnData) {
        super(phase, turnData);
    }

    public void update(final List<Ship> damagedShips, final Consumer<Ship> applyDamageType) {
        damagedShips.sort(IdentifiableObject.IDENTIFIABLE_NAME_COMPARATOR);
        final Multimap<Ship, Empire> newsEmpires = HashMultimap.create();
        damagedShips.forEach(ship -> {
            final Collection<Empire> empires = turnData.getEmpiresPresent(ship);
            newsEmpires.putAll(ship, empires);
            newsEmpires.putAll(ship, turnData.getEmpiresPresentFromDeadShips(ship.getCoordinate()));
        });
        final Set<Ship> destroyed = Sets.newHashSet();
        damagedShips.forEach(ship -> {
            applyDamageType.accept(ship);
            if (!ship.isAlive()) {
                final Collection<Empire> empires = newsEmpires.get(ship);
                addNews(empires, "%s ship %s has been destroyed".formatted(ship.getOwner(), ship));
                destroyed.add(ship);
                ship.getCargo()
                    .stream()
                    .sorted(IdentifiableObject.IDENTIFIABLE_NAME_COMPARATOR)
                    .forEach(cargo -> {
                           cargo.setDpRemaining(0);
                           applyDamageType.accept(cargo);
                           destroyed.add(cargo);
                           addNews(empires, "Loaded %s cargo %s has been destroyed".formatted(cargo.getOwner(), cargo));
                     });
            }
            else if (!ship.isOneShot()) {
                for (Empire newsEmpire: newsEmpires.get(ship)) {
                    if (newsEmpire.isKnownShipClass(ship.getShipClass())) {
                        addNews(newsEmpire, String.format("Ship %s now at %s OR", ship, formatOpRating(ship)));
                    }
                }
            }
        });
        turnData.removeDestroyedShips(destroyed);
    }
}