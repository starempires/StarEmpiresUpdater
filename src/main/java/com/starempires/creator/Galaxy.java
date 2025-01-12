package com.starempires.creator;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.starempires.constants.Constants;
import com.starempires.objects.Coordinate;
import com.starempires.objects.Empire;
import com.starempires.objects.Portal;
import com.starempires.objects.Storm;
import com.starempires.objects.World;
import com.starempires.util.PropertiesUtil;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.Validate;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;

@Log4j2
@Getter
public class Galaxy {

    private final int homeworldProduction;
    private final int homeworldNearbyProduction;
    private final int homeworldNumNearbyWorlds;
    private final int homeworldNearbyRadius;
    private final double worldDensity;
    private final int maxWorldProduction;
    private final int numWormnets;
    private final int maxWormnetPortals;
    private final int minPortalToPortalDistance;
    private final int minPortalToHomeworldDistance;
    private final int minNebulaToHomeworldDistance;
    private final double nebulaDensity;
    private final double stormDensity;
    private final int maxStormRating;
    private final int radius;
    private final List<Coordinate> unoccupied;
    private final Set<Coordinate> allCoordinates;
    private final Map<Coordinate, World> homeworlds = Maps.newHashMap();
    private final Set<World> worlds = Sets.newHashSet();
    private final Set<Portal> portals = Sets.newHashSet();
    private final Set<Storm> storms = Sets.newHashSet();

    public Galaxy(final @NonNull PropertiesUtil properties) {
        homeworldProduction = properties.getInt(Constants.CONFIG_HOMEWORLD_PRODUCTION, Constants.DEFAULT_HOMEWORLD_PRODUCTION);
        Validate.isTrue(homeworldProduction > 0, "Homeworld production must be positive");
        log.info("Homeworld production {}", homeworldProduction);

        homeworldNearbyProduction = properties.getInt(Constants.CONFIG_HOMEWORLD_NEARBY_PRODUCTION, Constants.DEFAULT_HOMEWORLD_NEARBY_PRODUCTION);
        Validate.isTrue(homeworldNearbyProduction >= 0, "Homeworld nearby production must not be negative");
        log.info("Homeworld nearby production {}", homeworldNearbyProduction);

        homeworldNumNearbyWorlds = properties.getInt(Constants.CONFIG_HOMEWORLD_NUM_NEARBY_WORLDS, Constants.DEFAULT_HOMEWORLD_NUM_NEARBY_WORLDS);
        Validate.isTrue(homeworldNumNearbyWorlds >= 0, "Homeworld num nearby worlds must not be negative");
        log.info("Homeworld num nearby worlds {}", homeworldNumNearbyWorlds);

        homeworldNearbyRadius = properties.getInt(Constants.CONFIG_HOMEWORLD_NEARBY_RADIUS, Constants.DEFAULT_HOMEWORLD_NEARBY_RADIUS);
        Validate.isTrue(homeworldNearbyRadius > 0, "Homeworld num nearby radius must be positive");
        log.info("Homeworld nearby radius {}", homeworldNearbyRadius);

        maxWorldProduction = properties.getInt(Constants.CONFIG_MAX_WORLD_PRODUCTION, Constants.DEFAULT_MAX_WORLD_PRODUCTION);
        Validate.isTrue(maxWorldProduction > 0, "Homeworld num nearby radius must be positive");
        log.info("Homeworld num nearby production {}", homeworldNumNearbyWorlds);

        numWormnets = properties.getInt(Constants.CONFIG_NUM_WORMNETS, Constants.DEFAULT_NUM_WORMNETS);
        Validate.isTrue(numWormnets >= 0, "Num wormnets must be positive");
        log.info("Num wormnets {}", numWormnets);

        maxWormnetPortals = properties.getInt(Constants.CONFIG_MAX_WORMNET_PORTALS, Constants.DEFAULT_MAX_WORMNET_PORTALS);
        Validate.isTrue(maxWormnetPortals >= 0, "Max wormnet portals must be non-negative");
        log.info("Max wormnet portals {}", maxWormnetPortals);

        minPortalToPortalDistance = properties.getInt(Constants.CONFIG_MIN_PORTAL_TO_PORTAL_DISTANCE, Constants.DEFAULT_MIN_PORTAL_TO_PORTAL_DISTANCE);
        Validate.isTrue(minPortalToPortalDistance > 1, "Min portal to portal distance must be > 1");
        log.info("Min portal to portal distance {}", minPortalToPortalDistance);

        minPortalToHomeworldDistance = properties.getInt(Constants.CONFIG_MIN_PORTAL_TO_HOMEWORLD_DISTANCE, Constants.DEFAULT_MIN_PORTAL_TO_HOMEWORLD_DISTANCE);
        Validate.isTrue(minPortalToHomeworldDistance > 1, "Min portal to homeworld distance must be > 1");
        log.info("Min portal to homeworld distance {}", minPortalToHomeworldDistance);

        minNebulaToHomeworldDistance = properties.getInt(Constants.CONFIG_MIN_NEBULA_TO_HOMEWORLD_DISTANCE, Constants.DEFAULT_MIN_NEBULA_TO_HOMEWORLD_DISTANCE);
        Validate.isTrue(minNebulaToHomeworldDistance > 1, "Min nebula to homeworld distance must be > 1");
        log.info("Min nebula to homeworld distance {}", minNebulaToHomeworldDistance);

        maxStormRating = properties.getInt(Constants.CONFIG_MAX_STORM_RATING, Constants.DEFAULT_MAX_STORM_RATING);
        Validate.isTrue(maxStormRating >= 0, "Max storm rating must be non-negative");
        log.info("Max storm rating {}", maxStormRating);

        radius = properties.getInt(Constants.CONFIG_RADIUS, Constants.DEFAULT_GALAXY_RADIUS);
        Validate.isTrue(radius > 1, "Max radius must be > 1");
        log.info("Galaxy radius {}", radius);

        worldDensity = properties.getDouble(Constants.CONFIG_WORLD_DENSITY, Constants.DEFAULT_WORLD_DENSITY);
        Validate.exclusiveBetween(0.0, 1.0, worldDensity, "World density must be between 0.0 and 0.1");
        log.info("World density {}", worldDensity);

        stormDensity = properties.getDouble(Constants.CONFIG_STORM_DENSITY, Constants.DEFAULT_STORM_DENSITY);
        Validate.exclusiveBetween(0.0, 1.0, stormDensity, "Storm density must be between 0.0 and 0.1");
        log.info("Storm density {}", stormDensity);

        nebulaDensity = properties.getDouble(Constants.CONFIG_NEBULA_DENSITY, Constants.DEFAULT_NEBULA_DENSITY);
        Validate.exclusiveBetween(0.0, 1.0, nebulaDensity, "Nebula density must be between 0.0 and 0.1");
        log.info("Nebula density {}", nebulaDensity);

        allCoordinates = Coordinate.getSurroundingCoordinates(new Coordinate(0, 0), radius);
        log.info("Generated {} coordinates in galaxy", allCoordinates.size());
        unoccupied = Lists.newArrayList(allCoordinates);
        Collections.shuffle(unoccupied);
    }

    private void addHomeworldNearbyWorlds(final List<String> names) {
        homeworlds.forEach((key, value) -> {
            log.info("Adding nearby worlds to homeworld {}", value);
            addNearbyWorlds(key, names);
        });
    }

    private void addNearbyWorlds(final Coordinate coordinate, final List<String> names) {
        int nearbyProduction = homeworldNearbyProduction;
        int numNearbyWorlds = homeworldNumNearbyWorlds;
        final List<Coordinate> nearbyCoords = Lists
                .newArrayList(Coordinate.getSurroundingCoordinates(coordinate, homeworldNearbyRadius));
        nearbyCoords.retainAll(unoccupied);
        if (nearbyCoords.size() >= numNearbyWorlds) {
            Collections.shuffle(nearbyCoords);
            while (numNearbyWorlds > 0) {
                int production = nearbyProduction / numNearbyWorlds;
                if (numNearbyWorlds > 1) {
                    production += ThreadLocalRandom.current().nextInt(3) - 1;
                }
                if (production < 1) {
                    production = 1;
                }
                numNearbyWorlds--;
                nearbyProduction -= production;
                int id = worlds.size() + 1;
                String name = getName(names);
                Coordinate nearbyCoordinate = nearbyCoords.get(0);
                final World.WorldBuilder builder = World.builder();
                World world = builder.name(name).coordinate(nearbyCoordinate)
                        .production(production).build();
                worlds.add(world);
                log.info("Added nearby world {}, distance {}, production {}", world, Coordinate.distance(coordinate, nearbyCoordinate), production);
                unoccupied.remove(nearbyCoordinate);
                nearbyCoords.remove(0);
            }
        }
    }

    public void initHomeworlds(final Map<Empire, EmpireCreation> empireCreations) {
        for (Map.Entry<Empire, EmpireCreation> entry: empireCreations.entrySet()) {
            final Empire empire = entry.getKey();
            final EmpireCreation ec = entry.getValue();
            final Coordinate coordinate = ec.getCenter();
            final String name = ec.getHomeworldName();

            final World.WorldBuilder builder = World.builder();
            final World world = builder.name(name).coordinate(coordinate)
                    .owner(empire)
                    .production(homeworldProduction)
                    .stockpile(homeworldProduction)
                    .homeworld(true).build();
            homeworlds.put(coordinate, world);
            worlds.add(world);
            unoccupied.remove(coordinate);
            log.info("Added {} homeworld {} ", empire, world);
        }
        log.info("Added {} homeworlds", empireCreations.size());
    }

    private Coordinate getNewPortalCoordinate(final Set<Portal> portals) {
        final Coordinate coordinate = unoccupied.stream()
                .filter(coord -> coord.isBeyondMinDistanceToObjects(portals, minPortalToPortalDistance))
                .filter(coord -> coord.isBeyondMinDistanceToObjects(homeworlds.values(), minPortalToHomeworldDistance))
                .findFirst()
                .orElse(null);
        if (coordinate != null) {
            unoccupied.remove(coordinate);
        }
        return coordinate;
    }

    private String getName(final List<String> names) {
        return names.remove(ThreadLocalRandom.current().nextInt(names.size()));
    }

    private void addWormnet(List<String> names) {
        final int numPortals = 2 + ThreadLocalRandom.current().nextInt(maxWormnetPortals - 2);
        log.info("Generating new wormnet with {} portals", numPortals);
        final Set<Portal> wormnetPortals = Sets.newHashSet();
        IntStream.range(0, numPortals).forEach(i -> {
            final Coordinate coordinate = getNewPortalCoordinate(wormnetPortals);
            if (coordinate != null) {
                final String name = getName(names);
                final Portal portal = Portal.builder().name(name).coordinate(coordinate).build();
                wormnetPortals.add(portal);
            }
        });
        if (wormnetPortals.size() > 1) {
            final Set<Portal> connected = Sets.newHashSet();
            wormnetPortals.forEach(portal -> {
                connected.clear();
                connected.addAll(wormnetPortals);
                connected.remove(portal);
                connected.forEach(portal::addConnection);
                portals.add(portal);
                log.info("Added portal " + portal);
            });
        }
        else {
            log.warn("Can't find enough portal hexes beyond minimum distances");
        }
    }

    public void initPortals(final List<String> names) {
        IntStream.range(0, numWormnets).forEach(i -> addWormnet(names));
    }

    private boolean isNearbyHomeworld(final Coordinate coordinate) {
        return homeworlds.keySet().stream()
                .anyMatch(homeworld -> coordinate.distanceTo(homeworld) <= homeworldNearbyRadius);
    }

    public void initWorlds(final List<String> names) {
        addHomeworldNearbyWorlds(names);
        int totalProduction = 0;
        for (Coordinate coordinate : unoccupied) {
            if (!isNearbyHomeworld(coordinate)) {
                final double random = ThreadLocalRandom.current().nextDouble();
                if (random <= worldDensity) {
                    final String name = getName(names);
                    final int production = 1 + ThreadLocalRandom.current().nextInt(maxWorldProduction - 1);
                    final World world = World.builder().name(name).coordinate(coordinate)
                            .production(production).build();
                    worlds.add(world);
                    log.info("Added world {}, production {}", world, production);
                    totalProduction += production;
                }
            }
        }
        unoccupied.removeAll(worlds.stream().map(World::getCoordinate).toList());
        log.info("Added {} worlds, total production {}", worlds.size(), totalProduction);
    }

    public void initNebulaeAndStorms(final List<String> stormNames, final List<String> nebulaNames) {
        allCoordinates.forEach(coordinate -> {
            if (!homeworlds.containsKey(coordinate) &&
                    coordinate.isBeyondMinDistanceToObjects(homeworlds.values(), minNebulaToHomeworldDistance)) {
                if (ThreadLocalRandom.current().nextDouble() < stormDensity) {
                    final int rating = ThreadLocalRandom.current().nextInt(maxStormRating) + 1;
                    final String name = getName(stormNames);
                    final Storm storm = Storm.builder().coordinate(coordinate).name(name).rating(rating)
                            .build();
                    storms.add(storm);
                    log.info("Added storm {}", storm);
                }
                else if (ThreadLocalRandom.current().nextDouble() < nebulaDensity) {
                    final String name = getName(nebulaNames);
                    final Storm nebula = Storm.builder().coordinate(coordinate).name(name).rating(0)
                            .build();
                    storms.add(nebula);
                    log.info("Added nebula {}", nebula);
                }
            }
        });
        log.info("Added {} storms and nebulae", storms.size());
    }
}