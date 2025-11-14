package com.starempires.creator;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.starempires.TurnData;
import com.starempires.constants.Constants;
import com.starempires.dao.S3StarEmpiresDAO;
import com.starempires.dao.StarEmpiresDAO;
import com.starempires.objects.Coordinate;
import com.starempires.objects.Empire;
import com.starempires.objects.EmpireType;
import com.starempires.objects.FrameOfReference;
import com.starempires.objects.HexDirection;
import com.starempires.objects.HullParameters;
import com.starempires.objects.MappableObject;
import com.starempires.objects.Portal;
import com.starempires.objects.ScanStatus;
import com.starempires.objects.Ship;
import com.starempires.objects.ShipClass;
import com.starempires.objects.Storm;
import com.starempires.objects.World;
import com.starempires.util.PropertiesUtil;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.help.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import static com.starempires.objects.Coordinate.COORDINATE_COMPARATOR;

@Log4j2
public class SessionCreator {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final String COLORS_FILENAME = "Colors.txt";
    private static final String PORTAL_NAMES_FILENAME = "PortalNames.txt";
    private static final String WORLD_NAMES_FILENAME = "WorldNames.txt";
    private static final String STORM_NAMES_FILENAME = "StormNames.txt";
    private static final String NEBULA_NAMES_FILENAME = "NebulaNames.txt";
    private static final String HULL_PARAMETERS_FILENAME = "HullParameters.json";
    private static final String SHIP_CLASSES_FILENAME = "ShipClasses.json";

    private static final String ARG_SESSION_NAME = "session";
    private static final String ARG_GAME_DATA_DIR = "gamedatadir";
    private static final String ARG_SESSIONS_DIR = "sessionsdir";

    final private String sessionName;

    private final StarEmpiresDAO dao;

    public List<String> loadGameDataItems(final String filename) throws IOException {
        final String data = dao.loadGameData(filename);
        return Lists.newArrayList(data.split("\n"));
    }

    private static CommandLine extractCommandLineOptions(final String[] args) throws ParseException, IOException {
        final Options options = new Options();
        try {
            options.addOption(Option.builder("s").argName("session name").longOpt(ARG_SESSION_NAME).hasArg().desc("session name").required().get());
            // location where session creation data and other session-independent files are found
            options.addOption(Option.builder("gd").argName("game data dir").longOpt(ARG_GAME_DATA_DIR).hasArg().desc("location of game data").required().get());
            // location where individual session folders are found
            options.addOption(Option.builder("sd").argName("sessions dir").longOpt(ARG_SESSIONS_DIR).hasArg().desc("location of session data").required().get());

            final CommandLineParser parser = new DefaultParser();
            return parser.parse(options, args);

        } catch (ParseException e) {
            final HelpFormatter formatter = HelpFormatter.builder().get();
            formatter.printHelp("SessionCreator", null, options, null, true);
            throw e;
        }
    }

    public SessionCreator(final String sessionName, final String sessionsDir, final String gameDataDir) {
        this.sessionName = sessionName;
//        dao = new JsonStarEmpiresDAO(sessionsDir, gameDataDir);
        dao = new S3StarEmpiresDAO(sessionsDir, gameDataDir);
    }

    private List<Coordinate> sortEdgeCoordinates(final List<Coordinate> coordinates) {
        final List<Coordinate> bothPositive = Lists.newArrayList();
        final List<Coordinate> bothNegative = Lists.newArrayList();
        final List<Coordinate> onlyObliquePositive = Lists.newArrayList();
        final List<Coordinate> onlyObliqueNegative = Lists.newArrayList();

        for (Coordinate coordinate : coordinates) {
            if (coordinate.getOblique() >= 0) {
                if (coordinate.getY() >= 0) {
                    bothPositive.add(coordinate);
                }
                else {
                    onlyObliquePositive.add(coordinate);
                }
            } else if (coordinate.getY() < 0) {
                bothNegative.add(coordinate);
            } else {
                onlyObliqueNegative.add(coordinate);
            }
        }
        bothPositive.sort(COORDINATE_COMPARATOR);
        bothNegative.sort(COORDINATE_COMPARATOR);
        onlyObliquePositive.sort(COORDINATE_COMPARATOR);
        onlyObliqueNegative.sort(COORDINATE_COMPARATOR);
        final List<Coordinate> edgeCoordinates = Lists.newArrayList();
        edgeCoordinates.addAll(bothPositive);
        edgeCoordinates.addAll(onlyObliquePositive);
        edgeCoordinates.addAll(bothNegative);
        edgeCoordinates.addAll(onlyObliqueNegative);
        return edgeCoordinates;
    }

    public TurnData createSession() throws Exception {
        final List<String> empireData = dao.loadEmpireData(sessionName);
        final List<String> configData = dao.loadConfigFile(sessionName);
        final PropertiesUtil galaxyProperties = new PropertiesUtil(configData);
        final int radius = galaxyProperties.getInt(Constants.CONFIG_RADIUS);
        final TurnData turnData = TurnData.builder()
             .radius(radius)
             .session(sessionName)
             .build();

        final ThreadLocalRandom random = ThreadLocalRandom.current();
        //final List<Coordinate> edgeCoordinates = sortEdgeCoordinates(Coordinate.getSurroundingRing(radius));
        final List<Coordinate> edgeCoordinates = Coordinate.getSurroundingRing(radius);
        final int numEdgeCoordinates = edgeCoordinates.size();

        // create empires
        final Map<Empire, EmpireCreation> empireCreations = Maps.newHashMap();
        int i = 0;
        int numEmpires = empireData.size();
        int interval = numEdgeCoordinates / numEmpires;
        for (final String data: empireData) {
            int index = i * interval;
            final Coordinate edge = edgeCoordinates.get(index);
            final String[] empireInfo = Arrays.stream(StringUtils.split(data, ",")).map(String::trim).toArray(String[]::new);
            if (empireInfo.length != 5) {
                throw new RuntimeException("Invalid empire data: " + data);
            }
            final FrameOfReference frame = FrameOfReference.builder()
                        .obliqueOffset(-edge.getOblique())
                        .yOffset(-edge.getY())
                        .horizontalMirror(random.nextInt(2) == 1)
                        .verticalMirror(random.nextInt(2) == 1)
                        .rotation(HexDirection.from(random.nextInt(HexDirection.values().length)))
                        .build();
            final String empireName = empireInfo[0];
            final String abbreviation = empireInfo[1];
            final EmpireType empireType = EmpireType.valueOf(empireInfo[2]);
            final String homeworldName = empireInfo[3];
            final String starbaseName = empireInfo[4];
            final Empire empire = Empire.builder().name(empireName).abbreviation(abbreviation).empireType(empireType)
                    .frameOfReference(frame).build();
            final EmpireCreation ecd = EmpireCreation.builder().homeworldName(homeworldName).starbaseName(starbaseName).center(edge).build();
            empireCreations.put(empire, ecd);
            i++;
            log.info("Created empire {} with FOR {}", empire, frame);
            turnData.addEmpire(empire);
        }

        // create HullParameters
        final String hullJson = dao.loadGameData(HULL_PARAMETERS_FILENAME);
        final List<HullParameters> hullParameters = MAPPER.readValue(hullJson, new TypeReference<List<HullParameters>>() { });
        turnData.addHullParameters(hullParameters);
        dao.saveHullParameters(sessionName, hullParameters);

        // create ShipClasses
        final String shipClassJson = dao.loadGameData(SHIP_CLASSES_FILENAME);
        final List<ShipClass> shipClasses = MAPPER.readValue(shipClassJson, new TypeReference<List<ShipClass>>() { });
        turnData.addShipClasses(shipClasses);

        final ShipClass starbaseShipClass = shipClasses.stream().filter(ShipClass::isStarbase).findFirst().orElseThrow(() -> new RuntimeException("Cannot find starbase ship class"));

        // create Galaxy
        final Galaxy galaxy = generateGalaxy(galaxyProperties, empireCreations);
        galaxy.getHomeworlds().values().forEach(homeworld -> {
            final Empire empire = homeworld.getOwner();
            final EmpireCreation ec = empireCreations.get(empire);
            final Ship starbase = empire.buildShip(starbaseShipClass, homeworld, ec.getStarbaseName(), 0);
            log.info("Generated {} starbase {}", empire, starbase);
        });

        final Map<Coordinate, Storm> stormCoordinates = galaxy.getStorms().stream().collect(Collectors.toMap(MappableObject::getCoordinate, obj -> obj));
        final Map<Coordinate, World> worldCoordinates = galaxy.getWorlds().stream().collect(Collectors.toMap(MappableObject::getCoordinate, obj -> obj));
        final Map<Coordinate, Portal> portalCoordinates = galaxy.getPortals().stream().collect(Collectors.toMap(MappableObject::getCoordinate, obj -> obj));
        turnData.addStorms(stormCoordinates.values());
        turnData.addWorlds(worldCoordinates.values());
        turnData.addPortals(portalCoordinates.values());

        for (final Empire empire: turnData.getAllEmpires()) {
            // init scan data and known objects for each empire
            empire.mergeObjectScanStatuses(empire.getKnownWorlds(), ScanStatus.VISIBLE, 0);
            final Collection<Ship> ships = empire.getShips();
            ships.stream().filter(ship -> !ship.isLoaded()).forEach(ship -> {
                final int scan;
                if (ship.isAlive() && !stormCoordinates.containsKey(ship.getCoordinate())) {
                    scan = ship.getAvailableScan();
                }
                else {
                    scan = 0;
                }
                final Collection<Coordinate> scanCoordinates = Coordinate.getSurroundingCoordinates(ship, scan);
                for (Coordinate coord: scanCoordinates) {
                    if (stormCoordinates.containsKey(coord)) {
                        empire.mergeScanStatus(coord, ScanStatus.STALE, 0);
                        empire.addKnownStorm(stormCoordinates.get(coord));
                        log.info("Added known storm {} for {}", stormCoordinates.get(coord), empire);
                    }
                    else {
                        empire.mergeScanStatus(coord, ScanStatus.SCANNED, 0);
                        if (worldCoordinates.containsKey(coord)) {
                            empire.addKnownWorld(worldCoordinates.get(coord));
                            log.info("Added known world {} for {}", worldCoordinates.get(coord), empire);
                        }
                        if (portalCoordinates.containsKey(coord)) {
                            empire.addKnownPortal(portalCoordinates.get(coord));
                            log.info("Added known portal {} for {}", portalCoordinates.get(coord), empire);
                        }
                    }
                }
                empire.mergeScanStatus(ship, ScanStatus.VISIBLE, 0);
                log.info("Initialized {} scan coordinates for {}", scanCoordinates.size(), empire);
            });

            // init known ship classes
            shipClasses.forEach(empire::addKnownShipClass);
        }

        saveTurnData(turnData);
        final Map<String, String> colors = createColorMap(turnData);
        saveColors(colors);
        log.info("Initialized session {}", sessionName);
        return turnData;
    }

    private Map<String, String> loadDefaultColors() throws IOException {
        final Map<String, String> colors = Maps.newHashMap();
        final List<String> list = loadGameDataItems(COLORS_FILENAME);
        list.forEach( line -> {
            final String[] parts = StringUtils.split(line.trim(), ":");
            colors.put(parts[0], parts[1]);
        });
        return colors;
    }

    private Map<String, String> createColorMap(final TurnData turnData) throws IOException {
        final Map<String, String> defaultColors = loadDefaultColors();
        final Map<String, String> colors = defaultColors.entrySet().stream()
                .filter(entry -> !entry.getKey().startsWith("empire-")) // Keep entries whose keys do not start with the prefix
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)); // Collect into a new map
        final Collection<Empire> empires = turnData.getAllEmpires();
        int i = 0;
        for (Empire empire: empires) {
            String color = defaultColors.get("empire-" + i);
            colors.put("empire-" + empire.getName(), color);
            i++;
        }
        return colors;
    }

    private void saveColors(final Map<String, String> colors) throws Exception {
        dao.saveColors(sessionName, colors);
    }

    public static void main(String[] args) {
        try {
            final CommandLine cmd = extractCommandLineOptions(args);
            // Parse command-line arguments
            final String sessionName = cmd.getOptionValue(ARG_SESSION_NAME);
            final String gameDataDir = cmd.getOptionValue(ARG_GAME_DATA_DIR);
            final String sessionsDir = cmd.getOptionValue(ARG_SESSIONS_DIR);
            final SessionCreator creator = new SessionCreator(sessionName, sessionsDir, gameDataDir);
            final TurnData turnData = creator.createSession();
            // reload turn data to confirm it's valid
            final TurnData turnData2 = creator.loadTurnData(turnData.getSession(), turnData.getTurnNumber());
            log.info("Loaded turnData {}", turnData2);
        } catch (Exception exception) {
            log.error("Session creation failed", exception);
        }
    }

    private TurnData loadTurnData(final String session, final int turnNumber) throws Exception {
        return dao.loadTurnData(session, turnNumber);
    }

    private void saveTurnData(final TurnData turndata) throws Exception {
        dao.saveTurnData(sessionName, turndata);
    }

    private Galaxy generateGalaxy(final PropertiesUtil galaxyProperties, final Map<Empire, EmpireCreation> empireCreations) throws IOException {
        final List<String> portalNames = loadGameDataItems(PORTAL_NAMES_FILENAME);
        final List<String> worldNames = loadGameDataItems(WORLD_NAMES_FILENAME);
        final List<String> stormNames = loadGameDataItems(STORM_NAMES_FILENAME);
        final List<String> nebulaNames = loadGameDataItems(NEBULA_NAMES_FILENAME);

        final Galaxy galaxy = new Galaxy(galaxyProperties);
        galaxy.initHomeworlds(empireCreations);
        galaxy.initWorlds(worldNames);
        galaxy.initPortals(portalNames);
        galaxy.initNebulaeAndStorms(stormNames, nebulaNames);
        log.info("Generated galaxy");
        return galaxy;
    }
}