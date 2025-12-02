package com.starempires.updater;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.starempires.TurnData;
import com.starempires.objects.Coordinate;
import com.starempires.objects.DeviceType;
import com.starempires.objects.Empire;
import com.starempires.objects.Ship;
import com.starempires.objects.Storm;
import org.apache.commons.lang3.StringUtils;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class WeatherStormsPhaseUpdater extends PhaseUpdater {

    private static final int STARBASE_PROTECTION_RANGE = 1;

    public WeatherStormsPhaseUpdater(final TurnData turnData) {
        super(Phase.WEATHER_STORMS, turnData);
    }

    @Override
    public void update() {
        final Multimap<Coordinate, Storm> stormCoordinates = turnData.getStormCoordinates();
        for (final Map.Entry<Coordinate, Collection<Storm>> entry : stormCoordinates.asMap().entrySet()) {
            final Coordinate coordinate = entry.getKey();
            final Set<Storm> storms = entry.getValue().stream().filter(s -> s.getIntensity() > 0).collect(Collectors.toSet());
            final int totalIntensity = storms.stream().mapToInt(Storm::getIntensity).sum();
            if (totalIntensity > 0) {
                final Collection<Empire> empiresPresent = turnData.getEmpiresPresent(coordinate);
                // get all live unloaded ships present
                final Collection<Ship> ships = turnData.getLiveShips(coordinate).stream().filter(s -> !s.isLoaded()).collect(Collectors.toSet());

                // get starbases within range
                final Set<Coordinate> surroundingCoordinates = Coordinate.getSurroundingCoordinates(coordinate, STARBASE_PROTECTION_RANGE);
                final Set<Ship> starbases = Sets.newHashSet();
                surroundingCoordinates.forEach(c -> starbases.addAll(turnData.getStarbases(c)));
                ships.removeAll(starbases);

                // remove empires with starbase protection
                final Multimap<Empire, Ship> shipsByEmpire = HashMultimap.create();
                ships.forEach(ship -> shipsByEmpire.put(ship.getOwner(), ship));
                String stormText = "storm" + (storms.size() > 1 ? "s" : "") + " " + StringUtils.join(storms, ",");

                starbases.forEach(s -> {
                    addNews(empiresPresent, "Starbase %s protects %s ships from %s".formatted(s, s.getOwner(), stormText));
                    shipsByEmpire.removeAll(s.getOwner());
                });

                // remove empires with deployed ion shield protection
                final Collection<Ship> allShields = turnData.getDeployedDevices(coordinate, DeviceType.ION_SHIELD);
                allShields.forEach(s -> {
                    addNews(empiresPresent, "Shield %s protects %s ships from %s".formatted(s, s.getOwner(), stormText));
                    shipsByEmpire.removeAll(s.getOwner());
                });

                shipsByEmpire.asMap().forEach((empire, shipList) -> {
                    shipList.forEach(ship -> {
                        addNews(empiresPresent, "%s ship %s suffered %d storm damage from %s"
                                .formatted(ship.getOwner(), ship, totalIntensity, stormText));
                        ship.inflictStormDamage(totalIntensity);
                    });
                });
            }
        }
    }
}