package com.starempires.phases;

import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.util.concurrent.AtomicLongMap;
import com.starempires.TurnData;
import com.starempires.objects.Coordinate;
import com.starempires.objects.DeviceType;
import com.starempires.objects.Empire;
import com.starempires.objects.Ship;
import com.starempires.objects.Storm;
import org.apache.commons.collections4.CollectionUtils;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class WeatherStormsPhaseUpdater extends PhaseUpdater {

    static class ShieldComparator implements Comparator<Ship> {
        private static final Random random_ = new Random();

        @Override
        public int compare(final Ship ship1, final Ship ship2) {
            int rv = ship1.getDpRemaining() - ship2.getDpRemaining();
            if (rv == 0) {
                rv = random_.nextInt() - random_.nextInt();
            }
            return rv;
        }
    }

    public WeatherStormsPhaseUpdater(final TurnData turnData) {
        super(Phase.WEATHER_STORMS, turnData);
    }

    protected int degradeShields(final Collection<Ship> shields, final int totalRating,
            final Collection<Empire> empires) {
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
        for (Map.Entry<Coordinate, Collection<Storm>> entry : stormCoordinates.asMap().entrySet()) {
            final Coordinate coordinate = entry.getKey();
            final Collection<Storm> storms = entry.getValue();
            final int totalRating = storms.stream().mapToInt(Storm::getRating).sum();
            if (totalRating > 0) {
                final Set<Ship> starbases = turnData.getStarbases(coordinate);
                final Collection<Empire> empires = turnData.getEmpiresPresent(coordinate);
                final Collection<Ship> ships = turnData.getLiveShips(coordinate);
                if (CollectionUtils.isEmpty(starbases)) {
                    final Collection<Ship> shields = turnData.getDeployedDevices(coordinate, DeviceType.ION_SHIELD);
                    shields.removeIf(Ship::hasAccruedDamageExceededRemainingDp);
                    final int unshieldedStormRating = degradeShields(shields, totalRating, empires);
                    if (unshieldedStormRating > 0) {
                        ships.forEach(ship -> {
                            addNews(empires,
                                    "Ship " + ship + " suffered " + unshieldedStormRating + " ion storm damage");
                            ship.inflictStormDamage(unshieldedStormRating);
                        });
                    }
                }
                else {
                    ships.removeAll(starbases);
                    ships.forEach(ship -> {
                        addNews(empires, "Ship " + ship
                                + " is protected from ion storm damage by starbase "
                                + starbases.stream().findAny().get());
                    });
                }
            }
        }
    }
}