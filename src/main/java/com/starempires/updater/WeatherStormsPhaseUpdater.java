package com.starempires.updater;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.google.common.util.concurrent.AtomicLongMap;
import com.starempires.TurnData;
import com.starempires.objects.Coordinate;
import com.starempires.objects.DeviceType;
import com.starempires.objects.Empire;
import com.starempires.objects.Ship;
import com.starempires.objects.Storm;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

public class WeatherStormsPhaseUpdater extends PhaseUpdater {

    private static final int STARBASE_PROTECTION_RANGE = 1;

    static class ShieldComparator implements Comparator<Ship> {
        private static final Random random = ThreadLocalRandom.current();

        @Override
        public int compare(final Ship ship1, final Ship ship2) {
            int rv = ship1.getDpRemaining() - ship2.getDpRemaining();
            if (rv == 0) {
                rv = random.nextInt() - random.nextInt();
            }
            return rv;
        }
    }

    public WeatherStormsPhaseUpdater(final TurnData turnData) {
        super(Phase.WEATHER_STORMS, turnData);
    }

    protected int degradeShields(final Collection<Ship> shields, final int totalRating, final Collection<Empire> empires) {
        if (shields.isEmpty()) {
            return totalRating;
        }
        int unshieldedDamage = totalRating;
        List<Ship> shieldList = Lists.newArrayList(shields);
        final AtomicLongMap<Ship> shieldDp = AtomicLongMap.create();
        final AtomicLongMap<Ship> shieldDamage = AtomicLongMap.create();
        shieldList.forEach(shield -> shieldDp.put(shield, shield.getDpRemaining()));
        shieldList.sort(new ShieldComparator());
        while (unshieldedDamage > 0 && !shieldList.isEmpty()) {
            final Ship shield = shieldList.remove(0);
            shield.inflictStormDamage(1);
            final long remaining = shieldDp.decrementAndGet(shield);
            shieldDamage.incrementAndGet(shield);
            if (remaining > 0L) {
                shieldList.add(shield);
            }
            unshieldedDamage--;
        }
        shieldDamage.asMap().keySet().forEach(shield -> {
            addNews(empires, "Shield " + shield + " absorbed " + shieldDamage.get(shield) + " ion storm damage");
        });
        return unshieldedDamage;
    }

    @Override
    public void update() {
        final Multimap<Coordinate, Storm> stormCoordinates = turnData.getStormCoordinates();
        for (final Map.Entry<Coordinate, Collection<Storm>> entry : stormCoordinates.asMap().entrySet()) {
            final Coordinate coordinate = entry.getKey();
            final Collection<Storm> storms = entry.getValue();
            final int totalRating = storms.stream().mapToInt(Storm::getRating).sum();
            if (totalRating > 0) {
                // get starbases within range
                final Set<Coordinate> surroundingCoordinates = Coordinate.getSurroundingCoordinates(coordinate, STARBASE_PROTECTION_RANGE);
                final Set<Ship> starbases = Sets.newHashSet();
                surroundingCoordinates.forEach(c -> starbases.addAll(turnData.getStarbases(c)));
                final Collection<Empire> empires = turnData.getEmpiresPresent(coordinate);
                final Collection<Ship> ships = turnData.getLiveShips(coordinate);
                ships.removeAll(starbases);

                final Multimap<Empire, Ship> shipsByEmpire = HashMultimap.create();
                ships.forEach(ship -> shipsByEmpire.put(ship.getOwner(), ship));

                starbases.forEach(s -> {
                    addNews(empires, "Starbase %s protects %s ships".formatted(s, s.getOwner()));
                    shipsByEmpire.removeAll(s.getOwner());
                });

                final Collection<Ship> allShields = turnData.getDeployedDevices(coordinate, DeviceType.ION_SHIELD);
                allShields.removeIf(Ship::hasAccruedTotalDamageExceededRemainingDp);
                final Multimap<Empire, Ship> shieldsByEmpire = HashMultimap.create();
                allShields.forEach(shield -> shieldsByEmpire.put(shield.getOwner(), shield));

                shipsByEmpire.asMap().forEach((empire, shipList) -> {
                    final Collection<Ship> empireShields = shieldsByEmpire.get(empire);
                    final int unshieldedStormRating = degradeShields(empireShields, totalRating, empires);
                    if (unshieldedStormRating > 0) {
                        shipList.forEach(ship -> {
                            addNews(empires, "Ship " + ship + " suffered " + unshieldedStormRating + " ion storm damage");
                            ship.inflictStormDamage(unshieldedStormRating);
                        });
                    }
                });
            }
        }
    }
}