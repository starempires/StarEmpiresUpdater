package com.starempires.dao;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.starempires.TurnData;
import com.starempires.TurnNews;
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
import com.starempires.orders.CustomOrderDeserializer;
import com.starempires.orders.Order;
import com.starempires.updater.Phase;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;

import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Log4j2
@RequiredArgsConstructor
@NoArgsConstructor(force = true)
public abstract class StarEmpiresDAO {

    // session-independent game data
    private static final String HULL_PARAMETERS_FILENAME = "hull-parameters.json";
    private static final String MAP_COLORS_FILENAME = "map-colors.json";

    // session-specific game data
    private static final String NEWS_FILENAME = "news.txt";
    private static final String ORDERS_FILENAME = "orders.txt";
    private static final String ORDER_RESULTS_FILENAME = "order-results.txt";
    private static final String ORDER_LOCK_FILENAME = "order-lock.txt";
    private static final String TURN_DATA_FILENAME = "turn-data.json";
    private static final String READY_ORDERS_FILENAME = "ready-orders.json";
    private static final String SNAPSHOT_FILENAME = "snapshot.json";
    private static final String EMPIRE_DATA_FILENAME = "empire-data.txt";

    private static final ObjectMapper MAPPER = new ObjectMapper();

    static {
        MAPPER.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);
        MAPPER.enable(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS);
        MAPPER.enable(SerializationFeature.INDENT_OUTPUT);
    }

    protected final String sessionsLocation;
    protected final String gameDataLocation;

    protected abstract String loadSessionData(final String session, final String filename) throws IOException;
    protected abstract boolean doesSessionDataExist(final String session, final String filename) throws IOException;
    protected abstract String saveSessionData(final String data, final String session, final String filename) throws IOException;
    protected abstract void removeSessionData(final String session, final String filename) throws IOException;
    public abstract String loadGameData(final String filename) throws IOException;

    public enum OrderStatus {
        NONE,
        EXIST,
        LOCKED
    }

    private String getSessionFilename(final String session, final String filename) {
        return StringUtils.joinWith(".", session, filename);
    }

    private String getTurnFilename(final String session, final int turnNumber, final String filename) {
        return StringUtils.joinWith(".", session, turnNumber, filename);
    }

    private String getEmpireFilename(final String session, final String empire, final int turnNumber, final String filename) {
        return StringUtils.joinWith(".", session, empire, turnNumber, filename);
    }

    public TurnData loadTurnData(final String session, final int turnNumber) throws Exception {
        final String filename = getTurnFilename(session, turnNumber, TURN_DATA_FILENAME);
        final String data = loadSessionData(session, filename);
        final Map<String, Object> jsonData = MAPPER.readValue(data, new TypeReference<Map<String, Object>>() { });
        log.debug("Loaded turn data\n{}", jsonData);

        final int radius = (int) jsonData.get("radius");
        TurnData turnData = TurnData.builder().session(session).turnNumber(turnNumber).radius(radius).build();

        // load objects first
        addHullParameters(jsonData, turnData);
        addShipClasses(jsonData, turnData);

        addStorms(jsonData, turnData);
        addPortals(jsonData, turnData);

        addWorlds(jsonData, turnData);
        addEmpires(jsonData, turnData);
        addWorldOwners(jsonData, turnData);

        addShips(jsonData, turnData);
        return turnData;
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

    private void addWorldOwners(final Map<String, Object> jsonData, final TurnData turnData) {
        final List<Map<String, Object>> objectData = getObjectData(jsonData, "worlds");
        for (Map<String, Object> data : objectData) {
            final String owner = (String) data.get("owner");
            if (owner != null) {
                final String name = (String) data.get("name");
                final World world = turnData.getWorld(name);
                final Empire empire = turnData.getEmpire(owner);
                world.setOwner(empire);
                if (world.isHomeworld()) {
                    turnData.setHomeworld(empire, world);
                }
                log.debug("Set world {} owner {}", world, empire);
            }
        }
    }

    private void addShipClasses(final Map<String, Object> jsonData, final TurnData turnData) {
        final List<Map<String, Object>> objectData = getObjectData(jsonData, "shipClasses");
        final List<ShipClass> shipClasses = Lists.newArrayList();
        for (Map<String, Object> data : objectData) {
            final ShipClass shipClass = MAPPER.convertValue(data, new TypeReference<ShipClass>() { });
            log.debug("Loaded ship class {}", shipClass);
            shipClasses.add(shipClass);
        }
        turnData.addShipClasses(shipClasses);
    }

    private void addStorms(final Map<String, Object> jsonData, final TurnData turnData) {
        final List<Map<String, Object>> objectData = getObjectData(jsonData, "storms");
        for (Map<String, Object> data : objectData) {
            final Storm storm = MAPPER.convertValue(data, new TypeReference<Storm>() { });
            log.debug("Loaded storm {}", storm);
            turnData.addStorm(storm);
        }
    }

    private void addShips(final Map<String, Object> jsonData, final TurnData turnData) {
        final List<Map<String, Object>> objectData = getObjectData(jsonData, "ships");
        final Map<String, Ship> ships = Maps.newHashMap();
        for (Map<String, Object> data : objectData) {
            final Ship ship = MAPPER.convertValue(data, new TypeReference<Ship>() { });
            final String owner = (String) data.get("owner");
            final Empire empire = turnData.getEmpire(owner);
            ship.setOwner(empire);
            empire.addShip(ship);
            log.debug("Loaded ship {} for owner {}", ship, empire);
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

    private void addEmpires(final Map<String, Object> jsonData, final TurnData turnData) {
        final List<Map<String, Object>> objectData = getObjectData(jsonData, "empires");
        final Map<String, Empire> empires = Maps.newHashMap();
        for (Map<String, Object> data : objectData) {
            final Empire empire = MAPPER.convertValue(data, new TypeReference<Empire>() { });
            log.debug("Loaded empire {}", empire);
            empires.put(empire.getName(), empire);
        }
        turnData.addEmpires(empires.values());

        // load known objects
        for (Map<String, Object> data : objectData) {
            final Empire empire = empires.get((String) data.get("name"));

            final Collection<String> knownEmpires = getStringCollection(data, "knownEmpires");
            knownEmpires.stream().map(empires::get).forEach(empire::addKnownEmpire);
            log.debug("Added known empires {} to {}", knownEmpires, empire);

            final Collection<String> knownPortals = getStringCollection(data, "knownPortals");
            knownPortals.stream().map(turnData::getPortal).forEach(empire::addKnownPortal);
            log.debug("Added known portals {} to {}", knownPortals, empire);

            final Collection<String> navDataPortals = getStringCollection(data, "portalNavData");
            navDataPortals.stream().map(turnData::getPortal).forEach(empire::addNavData);
            log.debug("Added nav data for portals {} to {}", navDataPortals, empire);

            final Collection<String> knownStorms = getStringCollection(data, "knownStorms");
            knownStorms.stream().map(turnData::getStorm).forEach(empire::addKnownStorm);
            log.debug("Added known storms {} to {}", knownStorms, empire);

            final Collection<String> knownWorlds = getStringCollection(data, "knownWorlds");
            knownWorlds.stream().map(turnData::getWorld).forEach(empire::addKnownWorld);
            log.debug("Added known worlds {} to {}", knownWorlds, empire);

            final Collection<String> knownShipClasses = getStringCollection(data, "knownShipClasses");
            knownShipClasses.stream().map(turnData::getShipClass).forEach(empire::addKnownShipClass);
            log.debug("Added known ship classes {} to {}", knownShipClasses, empire);

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

    private void addPortals(final Map<String, Object> jsonData, final TurnData turnData) throws JsonProcessingException {
        final List<Map<String, Object>> objectData = getObjectData(jsonData, "portals");
        final Map<String, Portal> portals = Maps.newHashMap();
        for (final Map<String, Object> data : objectData) {
            final Portal portal = MAPPER.convertValue(data, new TypeReference<Portal>() { });
            log.debug("Loaded portal {}", portal);
            turnData.addPortal(portal);
            portals.put(portal.getName(), portal);
        }

        // now make connections
        for (final Map<String, Object> data : objectData) {
            final Portal portal = portals.get((String) data.get("name"));
            final Collection<String> connections = getStringCollection(data, "connections");
            connections.stream().map(portals::get).forEach(portal::addConnection);
            log.debug("Added connections {} to {}", connections, portal);
        }
    }

    private void addWorlds(final Map<String, Object> jsonData, final TurnData turnData) {
        final List<Map<String, Object>> objectData = getObjectData(jsonData, "worlds");
        for (Map<String, Object> data : objectData) {
            final World world = MAPPER.convertValue(data, new TypeReference<World>() { });
            log.debug("Loaded world {}", world);
            turnData.addWorld(world);
        }
    }

    private void addHullParameters(final Map<String, Object> jsonData, final TurnData turnData) {
        final List<Map<String, Object>> objectData = getObjectData(jsonData, "hullParameters");
        final List<HullParameters> hullParameters = Lists.newArrayList();
        for (Map<String, Object> data : objectData) {
            final HullParameters parameters = MAPPER.convertValue(data, new TypeReference<HullParameters>() { });
            log.debug("Loaded hull parameters {}", parameters);
            hullParameters.add(parameters);
        }
        turnData.addHullParameters(hullParameters);
    }

    public List<HullParameters> loadHullParameters(final String session) throws Exception {
        final String filename = getSessionFilename(session, HULL_PARAMETERS_FILENAME);
        final String data = loadSessionData(session, filename);
        final List<HullParameters> hullParameters = Lists.newArrayList();
        for (Map<String, Object> dataItem : MAPPER.readValue(data, new TypeReference<List<Map<String, Object>>>() {
        })) {
            final HullParameters parameters = MAPPER.convertValue(dataItem, new TypeReference<HullParameters>() { });
            log.debug("Loaded hull parameters {}", parameters);
            hullParameters.add(parameters);
        }
        return hullParameters;
    }

    public Map<String, String> loadColors(final String session) throws Exception {
        final String filename = getSessionFilename(session, MAP_COLORS_FILENAME);
        final String data = loadSessionData(session, filename);
        final Map<String, String> colors = MAPPER.readValue(data, new TypeReference<Map<String, String>>() { });
        log.debug("Loaded map colors {}", colors);
        return colors;
    }

    public List<String> loadEmpireData(final String session) throws Exception {
        final String filename = getSessionFilename(session, EMPIRE_DATA_FILENAME);
        final String data = loadSessionData(session, filename);
        final List<String> empireData = List.of(data.split("\\n"));
        log.debug("Loaded empire data {}", empireData);
        return empireData;
    }

    public List<String> loadOrders(final String session, final String empire, final int turnNumber) throws Exception {
        final String filename = getEmpireFilename(session, empire, turnNumber, ORDERS_FILENAME);
        final String data = loadSessionData(session, filename);
        final List<String> ordersText = List.of(data.split("\\n"));
        log.info("Loaded {} orders for empire {}, session {}, turn {}", ordersText.size(), empire, session, turnNumber);
        return ordersText;
    }

    public List<String> loadEmpireNames(final String session) throws Exception {
        final List<String> empireData = loadEmpireData(session);
        final List<String> empireNames = empireData.stream()
                .map(str -> str.split(",")[0]) // Split and get the first value
                .collect(Collectors.toList()); //
        log.debug("Loaded empire names {}", empireNames);
        return empireNames;
    }

    public OrderStatus getOrderStatus(final String session, final String empire, final int turnNumber) throws Exception {
        final String readyOrdersFilename = getEmpireFilename(session, empire, turnNumber, READY_ORDERS_FILENAME);
        if (doesSessionDataExist(session, readyOrdersFilename)) {
            final String orderLockFilename = getEmpireFilename(session, empire, turnNumber, ORDER_LOCK_FILENAME);
            if (doesSessionDataExist(session, orderLockFilename)) {
                return OrderStatus.LOCKED;
            } else {
                return OrderStatus.EXIST;
            }
        }
        return OrderStatus.NONE;
    }

    public List<Order> loadReadyOrders(final String session, final String empire, final int turnNumber, final TurnData turnData) throws Exception {
        final String filename = getEmpireFilename(session, empire, turnNumber, READY_ORDERS_FILENAME);
        String data = null;
        try {
            data = loadSessionData(session, filename);
            if (StringUtils.isBlank(data)) {
                throw new NoSuchFileException("File %s is empty".formatted(filename));
            }
        } catch (NoSuchFileException | NoSuchKeyException ex) {
            log.warn("No ready orders found for empire {} turn {}", empire, turnNumber);
            return Collections.emptyList();
        }

        SimpleModule module = new SimpleModule();
        module.addDeserializer(Order.class, new CustomOrderDeserializer(turnData));
        MAPPER.registerModule(module);
        final List<Order> orders = MAPPER.readValue(data, new TypeReference<List<Order>>() { });
        log.debug("Loading {} orders: {}", empire, orders);
        return orders;
    }

    public void saveTurnData(final String session, final TurnData turnData) throws Exception {
        final String output = MAPPER.writeValueAsString(turnData);
        final int turnNumber = turnData.getTurnNumber();
        final String filename = getTurnFilename(session, turnNumber, TURN_DATA_FILENAME);
        final String location = saveSessionData(output, session, filename);
        log.info("Wrote {} turn {} data to {}", turnData.getSession(), turnNumber, location);
    }

    public void saveReadyOrders(final String session, final String empire, int turnNumber, final List<Order> orders) throws IOException {
        final String output = MAPPER.writeValueAsString(orders);
        final String filename = getEmpireFilename(session, empire, turnNumber, READY_ORDERS_FILENAME);
        final String location = saveSessionData(output, session, filename);
        log.info("Wrote {} orders for empire {} turn {} to {}", orders.size(), empire, turnNumber, location);
    }

    public void saveSnapshot(final String session, final String empire, int turnNumber, final EmpireSnapshot snapshot) throws IOException {
        final String output = MAPPER.writeValueAsString(snapshot);
        final String filename = getEmpireFilename(session, empire, turnNumber, SNAPSHOT_FILENAME);
        final String location = saveSessionData(output, session, filename);
        log.info("Wrote snapshot for empire {} turn {} to {}", empire, turnNumber, location);
    }

    public String loadSnapshot(final String session, final String empire, int turnNumber) throws IOException {
        final String filename = getEmpireFilename(session, empire, turnNumber, SNAPSHOT_FILENAME);
        return loadSessionData(session, filename);
    }

    public String loadOrderResults(final String session, final String empire, int turnNumber) throws IOException {
        final String filename = getEmpireFilename(session, empire, turnNumber, ORDER_RESULTS_FILENAME);
        return loadSessionData(session, filename);
    }

    public String loadNews(final String session, final String empire, int turnNumber) throws IOException {
        final String filename = getEmpireFilename(session, empire, turnNumber, NEWS_FILENAME);
        return loadSessionData(session, filename);
    }

    public void saveOrderResults(final String session, final String empire, final int turnNumber, final List<Order> orders) throws IOException {
        final List<String> lines = orders.stream().map(Order::formatResults).collect(Collectors.toList());
        final String filename = getEmpireFilename(session, empire, turnNumber, ORDER_RESULTS_FILENAME);
        final String location = saveSessionData(StringUtils.join(lines, "\n"), session, filename);
        log.info("Wrote {} order results for empire {} turn {} to {}", orders.size(), empire, turnNumber, location);
    }

    public void saveColors(final String session, final Map<String, String> colors) throws Exception {
        final String filename = getSessionFilename(session, MAP_COLORS_FILENAME);
        final String output = MAPPER.writeValueAsString(colors);
        final String location = saveSessionData(output, session, filename);
        log.debug("Wrote map colors {} to {}", colors, location);
    }

    public void saveNews(final String session, final TurnNews turnNews, final int turnNumber) throws Exception {
        final Map<Empire, Multimap<Phase, String>> news = turnNews.getNews();
        for (Empire empire : news.keySet()) {
            final List<String> results = turnNews.getEmpireNews(empire);
            final String filename = getEmpireFilename(session, empire.getName(), turnNumber, NEWS_FILENAME);
            final String location = saveSessionData(StringUtils.join(results, "\n"), session, filename);
            log.info("Saved {} news for turn {} to {}", empire, turnNumber, location);
        }
    }

    public void saveHullParameters(final String sessionName, final List<HullParameters> hullParameters) throws IOException {
        final String filename = getSessionFilename(sessionName, HULL_PARAMETERS_FILENAME);
        final String output = MAPPER.writeValueAsString(hullParameters);
        final String location = saveSessionData(output, sessionName, filename);
        log.debug("Wrote hull parameters to {}", location);
    }

    public void lockOrders(final String session, final String empire, final int turnNumber) throws IOException {
        final String orderLockFilename = getEmpireFilename(session, empire, turnNumber, ORDER_LOCK_FILENAME);
        final String date = ZonedDateTime.now().format(DateTimeFormatter.RFC_1123_DATE_TIME);
        saveSessionData(date, session, orderLockFilename);
    }

    public void unlockOrders(final String session, final String empire, final int turnNumber) throws IOException {
        final String orderLockFilename = getEmpireFilename(session, empire, turnNumber, ORDER_LOCK_FILENAME);
        removeSessionData(session, orderLockFilename);
    }
}