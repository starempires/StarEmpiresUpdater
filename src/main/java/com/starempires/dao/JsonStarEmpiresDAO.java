package com.starempires.dao;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.KeyDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.starempires.TurnData;
import com.starempires.generator.EmpireSnapshot;
import com.starempires.objects.Coordinate;
import com.starempires.objects.Empire;
import com.starempires.objects.HullParameters;
import com.starempires.objects.Portal;
import com.starempires.objects.RadialCoordinate;
import com.starempires.objects.Ship;
import com.starempires.objects.ShipClass;
import com.starempires.objects.Storm;
import com.starempires.objects.World;
import com.starempires.orders.Order;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Log4j2
@RequiredArgsConstructor
public class JsonStarEmpiresDAO implements StarEmpiresDAO {
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private final String sessionDir;

    static {
        MAPPER.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);
        MAPPER.enable(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS);
    }

    static class CoordinateKeyDeserializer extends KeyDeserializer {
        @Override
        public Coordinate deserializeKey(String key, com.fasterxml.jackson.databind.DeserializationContext ctxt) throws IOException {
            return Coordinate.parse(key);
        }
    }

    private Path constructPath(final String... args) {
        return FileSystems.getDefault().getPath(sessionDir, StringUtils.join(args, "."));
    }

    @Override
    public TurnData loadTurnData(final String session, final int turnNumber) throws Exception {
        final Path path = constructPath(session, "turndata", Integer.toString(turnNumber), "json");
        final Map<String, Object> jsonData = MAPPER.readValue(path.toFile(), new TypeReference<Map<String, Object>>() { });
        log.info("Loaded data\n" + jsonData);

        int radius = (int) jsonData.get("radius");
        TurnData turnData = TurnData.builder().session(session).turnNumber(turnNumber).radius(radius).build();

        // load objects first
        loadHullParameters(jsonData, turnData);
        loadShipClasses(jsonData, turnData);

        loadStorms(jsonData, turnData);
        loadPortals(jsonData, turnData);

        loadWorlds(jsonData, turnData);
        loadEmpires(jsonData, turnData);
        loadWorldOwners(jsonData, turnData);

        loadShips(jsonData, turnData);
        return turnData;
    }

    private void loadWorldOwners(final Map<String, Object> jsonData, final TurnData turnData) {
        final List<Map<String, Object>> objectData = getObjectData(jsonData, "worlds");
        for (Map<String, Object> data : objectData) {
            final String owner = (String) data.get("owner");
            if (owner != null) {
                final String name = (String) data.get("name");
                final World world = turnData.getWorld(name);
                final Empire empire = turnData.getEmpire(owner);
                world.setOwner(empire);
                log.info("Set world {} owner {}", world, empire);
            }
        }
    }

    private void loadShipClasses(final Map<String, Object> jsonData, final TurnData turnData) {
        final List<Map<String, Object>> objectData = getObjectData(jsonData, "shipClasses");
        final List<ShipClass> shipClasses = Lists.newArrayList();
        for (Map<String, Object> data : objectData) {
            final ShipClass shipClass = MAPPER.convertValue(data, new TypeReference<ShipClass>() {
            });
            log.info("Loaded ship class {}", shipClass);
            shipClasses.add(shipClass);
        }
        turnData.addShipClasses(shipClasses);
    }

    private void loadHullParameters(final Map<String, Object> jsonData, final TurnData turnData) {
        final List<Map<String, Object>> objectData = getObjectData(jsonData, "hullParameters");
        final List<HullParameters> hullParameters = Lists.newArrayList();
        for (Map<String, Object> data : objectData) {
            final HullParameters parameters = MAPPER.convertValue(data, new TypeReference<HullParameters>() {
            });
            log.info("Loaded hull parameters {}", parameters);
            hullParameters.add(parameters);
        }
        turnData.addHullParameters(hullParameters);
    }

    private Coordinate getCoordinate(final Map<String, Object> data) {
        return new Coordinate((int) data.get("oblique"), (int) data.get("y"));
    }

    private int getInt(final Map<String, Object> data, final String key) {
        return (int) data.getOrDefault(key, 0);
    }

    private boolean getBoolean(final Map<String, Object> data, final String key) {
        return (boolean) data.getOrDefault(key, false);
    }

    private Collection<String> getStringCollection(final Map<String, Object> data, final String key) {
        return (Collection<String>) data.getOrDefault(key, Collections.emptyList());
    }

    private Map<String, List<String>> getStringMapList(final Map<String, Object> data, final String key) {
        return (Map<String, List<String>>) data.getOrDefault(key, Collections.emptyMap());
    }

    private Map<String, List<Coordinate>> getStringCoordinateMapList(final Map<String, Object> data, final String key) {
        return (Map<String, List<Coordinate>>) data.getOrDefault(key, Collections.emptyMap());
    }

    private List<Map<String, Object>> getObjectData(final Map<String, Object> jsonData, final String key) {
       return (List<Map<String, Object>>) jsonData.getOrDefault(key, Collections.emptyList());
    }

    private void loadPortals(final Map<String, Object> jsonData, final TurnData turnData) throws JsonProcessingException {
        final List<Map<String, Object>> objectData = getObjectData(jsonData, "portals");
        final Map<String, Portal> portals = Maps.newHashMap();
        for (final Map<String, Object> data : objectData) {
            final Portal portal = MAPPER.convertValue(data, new TypeReference<Portal>() {});
            log.info("Loaded portal {}", portal);
            turnData.addPortal(portal);
            portals.put(portal.getName(), portal);
        }

        // now make connections
        for (final Map<String, Object> data : objectData) {
            final Portal portal = portals.get((String) data.get("name"));
            final Collection<String> connections = getStringCollection(data, "connections");
            connections.stream().map(portals::get).forEach(portal::addConnection);
            log.info("Added connections {} to {}", connections, portal);
        }
    }

    private void loadWorlds(final Map<String, Object> jsonData, final TurnData turnData) {
        final List<Map<String, Object>> objectData = getObjectData(jsonData, "worlds");
        for (Map<String, Object> data : objectData) {
            final World world = MAPPER.convertValue(data, new TypeReference<World>() {});
            log.info("Loaded world {}", world);
            turnData.addWorld(world);
        }
    }

    private void loadStorms(final Map<String, Object> jsonData, final TurnData turnData) {
        final List<Map<String, Object>> objectData = getObjectData(jsonData, "storms");
        for (Map<String, Object> data : objectData) {
            final Storm storm = MAPPER.convertValue(data, new TypeReference<Storm>() {});
            log.info("Loaded storm {}", storm);
            turnData.addStorm(storm);
        }
    }

    private void loadShips(final Map<String, Object> jsonData, final TurnData turnData) {
        final List<Map<String, Object>> objectData = getObjectData(jsonData, "ships");
        final Map<String, Ship> ships = Maps.newHashMap();
        for (Map<String, Object> data : objectData) {
            final Ship ship = MAPPER.convertValue(data, new TypeReference<Ship>() {});
            final String owner = (String)data.get("owner");
            final Empire empire = turnData.getEmpire(owner);
            ship.setOwner(empire);
            empire.addShip(ship);
            log.info("Loaded ship {} for owner {}", ship, empire);
            ships.put(ship.getName(), ship);
        }

        // set ship references
        for (Map<String, Object> data : objectData) {
            final String name = (String) data.get("name");
            final Ship ship = ships.get(name);
            ship.setCarrier(ships.get((String) data.get("carrier")));

            final String shipClass = (String) data.get("shipClass");
            ship.setShipClass(turnData.getShipClass(shipClass));

            final Collection<String> cargo = getStringCollection(data, "cargo");
            cargo.stream().map(ships::get).forEach(ship::addCargo);

            final Collection<String> transponders = getStringCollection(data, "transponders");
            transponders.stream().map(turnData::getEmpire).forEach(ship::addTransponder);
        }
    }

    private void loadEmpires(final Map<String, Object> jsonData, final TurnData turnData) {
        final List<Map<String, Object>> objectData = getObjectData(jsonData, "empires");
        final Map<String, Empire> empires = Maps.newHashMap();
        for (Map<String, Object> data : objectData) {
            final Empire empire = MAPPER.convertValue(data, new TypeReference<Empire>() {});
            log.info("Loaded empire {}", empire);
            empires.put(empire.getName(), empire);
        }
        turnData.addEmpires(empires.values());

        // load known objects
        for (Map<String, Object> data : objectData) {
            final Empire empire = empires.get((String) data.get("name"));

            final Collection<String> knownEmpires = getStringCollection(data, "knownEmpires");
            knownEmpires.stream().map(empires::get).forEach(empire::addKnownEmpire);
            log.info("Added known empires {} to {}", knownEmpires, empire);

            final Collection<String> knownPortals = getStringCollection(data, "knownPortals");
            knownPortals.stream().map(turnData::getPortal).forEach(empire::addKnownPortal);
            log.info("Added known portals {} to {}", knownPortals, empire);

            final Collection<String> navDataPortals = getStringCollection(data, "portalNavData");
            navDataPortals.stream().map(turnData::getPortal).forEach(empire::addNavData);
            log.info("Added nav data for portals {} to {}", navDataPortals, empire);

            final Collection<String> knownStorms = getStringCollection(data, "knownStorms");
            knownStorms.stream().map(turnData::getStorm).forEach(empire::addKnownStorm);
            log.info("Added known storms {} to {}", knownStorms, empire);

            final Collection<String> knownWorlds = getStringCollection(data, "knownWorlds");
            knownWorlds.stream().map(turnData::getWorld).forEach(empire::addKnownWorld);
            log.info("Added known worlds {} to {}", knownWorlds, empire);

            final Collection<String> knownShipClasses = getStringCollection(data, "knownShipClasses");
            knownShipClasses.stream().map(turnData::getShipClass).forEach(empire::addKnownShipClass);
            log.info("Added known ship classes {} to {}", knownShipClasses, empire);

            final Map<String, List<String>> shareShipClasses = getStringMapList(data, "shareShipClasses");
            shareShipClasses.forEach((key, value) -> {
                final Empire shareEmpire = empires.get(key);
                final List<ShipClass> shipClasses = value.stream().map(turnData::getShipClass).toList();
                empire.addShipClassScanAccess(shareEmpire, shipClasses);
            });

            final Map<String, List<String>> shareShips = getStringMapList(data, "shareShips");
            shareShips.forEach((key, value) -> {
                final Empire shareEmpire = empires.get(key);
                final List<Ship> ships = value.stream().map(empire::getShip).toList();
                empire.addShipScanAccess(shareEmpire, ships);
            });

            final Map<String, List<Map<String, Object>>> shareCoordinates =
                    (Map<String, List<Map<String, Object>>>) data.getOrDefault("shareCoordinates", Collections.emptyMap());
            final Map<Empire, List<RadialCoordinate>> shareRadialCoords = shareCoordinates.entrySet().stream()
                    .collect(Collectors.toMap(
                             entry -> empires.get(entry.getKey()),
                             entry -> entry.getValue().stream()
                                    .map(data2 -> MAPPER.convertValue(data2, RadialCoordinate.class))  // Convert each map to a Coordinate
                                    .collect(Collectors.toList())
                    ));
            shareRadialCoords.forEach(empire::addCoordinateScanAccess);

            final Collection<String> shareEmpires = getStringCollection(data, "shareEmpires");
            shareEmpires.stream().map(empires::get).forEach(empire::addEmpireScanAccess);
        }
    }

    @Override
    public void saveTurnData(final String session, final TurnData turnData) throws Exception {
        final Path path = constructPath(session, "turndata", Integer.toString(turnData.getTurnNumber()), "json");
        MAPPER.writerWithDefaultPrettyPrinter().writeValue(path.toFile(), turnData);
        log.info("Wrote {} turn {} data to {}", turnData.getSession(), turnData.getTurnNumber(), path);
    }

    @Override
    public List<? extends Order> loadReadyOrders(final String session, final String empire, final int turnNumber) throws Exception {
        final Path path = constructPath(session, empire, "ready-orders", Integer.toString(turnNumber), "txt");
        // TODO
        return Lists.newArrayList();
    }

    @Override
    public void saveReadyOrders(final String session, final String empire, int turnNumber, final List<Order> orders) throws IOException {
        final Path path = constructPath(session, empire, "ready-orders", Integer.toString(turnNumber), "json");
        MAPPER.writerWithDefaultPrettyPrinter().writeValue(path.toFile(), orders);
        log.info("Wrote {} orders for empire {} turn {} to {}", orders.size(), empire, turnNumber, path);
    }

    @Override
    public void saveSnapshot(final String session, final String empire, int turnNumber, final EmpireSnapshot snapshot) throws IOException {
        final Path path = constructPath(session, empire, "snapshot", Integer.toString(turnNumber), "json");
        MAPPER.writerWithDefaultPrettyPrinter().writeValue(path.toFile(), snapshot);
        log.info("Wrote snapshot for empire {} turn {} to {}", empire, turnNumber, path);
    }

    @Override
    public void saveOrderResults(final String session, final String empire, int turnNumber, final List<Order> orders) throws IOException {
        final Path path = constructPath(session, empire, "orders-results", Integer.toString(turnNumber), "txt");
        final List<String> lines = Lists.newArrayList();
        orders.forEach(order -> {
            final List<String> messages = order.getResults();
            if (messages.isEmpty()) {
                lines.add(String.format("%s: OK", order));
            }
            else if (messages.size() == 1) {
                lines.add(String.format("%s: %s", order, messages.get(0)));
            }
            else {
                lines.add(String.format("%s:\n %s", order, StringUtils.join(messages, "\n ")));
            }
        });
        Files.write(path, lines);
        log.info("Wrote {} order results for empire {} turn {} to {}", orders.size(), empire, turnNumber, path);
    }

    @Override
    public List<HullParameters> loadHullParameters(final String session) throws Exception {
        final Path path = constructPath(session, "hull-parameters", "json");
        final List<HullParameters> hullParameters = Lists.newArrayList();
        for (Map<String, Object> data : MAPPER.readValue(path.toFile(), new TypeReference<List<Map<String, Object>>>() {})) {
            final HullParameters parameters = MAPPER.convertValue(data, new TypeReference<HullParameters>() {});
            log.info("Loaded hull parameters {}", parameters);
            hullParameters.add(parameters);
        }
        return hullParameters;
    }

    @Override
    public void saveColors(final String session, final Map<String, String> colors) throws Exception {
        final Path path = constructPath(session, "map-colors", "json");
        MAPPER.writerWithDefaultPrettyPrinter().writeValue(path.toFile(), colors);
        log.info("Wrote map colors {}", colors);
    }

    @Override
    public Map<String, String> loadColors(final String session) throws Exception {
        final Path path = constructPath(session, "map-colors", "json");
        final Map<String, String> colors = MAPPER.readValue(path.toFile(),new TypeReference<Map<String, String>>() {});
        log.info("Loaded map colors {}", colors);
        return colors;
    }
}