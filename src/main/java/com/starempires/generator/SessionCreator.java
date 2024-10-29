package com.starempires.generator;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Maps;
import com.starempires.TurnData;
import com.starempires.constants.Constants;
import com.starempires.dao.JsonStarEmpiresDAO;
import com.starempires.dao.StarEmpiresDAO;
import com.starempires.objects.Coordinate;
import com.starempires.objects.Empire;
import com.starempires.objects.EmpireType;
import com.starempires.objects.FrameOfReference;
import com.starempires.objects.HexDirection;
import com.starempires.objects.HullParameters;
import com.starempires.objects.MappableObject;
import com.starempires.objects.Portal;
import com.starempires.objects.ScanData;
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
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Log4j2
public class SessionCreator {

    private static final String TEXT_EXTENSION = ".txt";
    private static final String JSON_EXTENSION = ".json";
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private String sessionName;
    private String dataDir;
    private String outputDir;
    private String configFile;
    private String empireFile;
    private String shipClassFile;
    private String hullParametersFile;

    private StarEmpiresDAO dao;
    private PropertiesUtil galaxyProperties;

    public List<String> loadItems(final String file) throws IOException {
        final Path path = FileSystems.getDefault().getPath(dataDir, file);
        log.info("Loading data from {}", path);
        return Files.readAllLines(path);
    }

    private void extractCommandLineOptions(final String[] args) throws ParseException {
        final Options options = new Options();
        try {
            options.addOption(Option.builder("s").argName("session name").longOpt("session").hasArg().desc("session name").required().build());
            options.addOption(Option.builder("c").argName("config file").longOpt("config").hasArg().desc("config file").required().build());
            options.addOption(Option.builder("d").argName("data dir").longOpt("datadir").hasArg().desc("data dir").required().build());
            options.addOption(Option.builder("o").argName("output dir").longOpt("outputdir").hasArg().desc("output dir").required().build());
            options.addOption(Option.builder("e").argName("empires data").longOpt("empiredata").hasArg().desc("empire data").required().build());
            options.addOption(Option.builder("sc").argName("ship classes").longOpt("shipclasses").hasArg().desc("ship classes").required().build());
            options.addOption(Option.builder("hp").argName("hull parameters").longOpt("hullparameters").hasArg().desc("hull parameters").required().build());

            final CommandLineParser parser = new DefaultParser();
            final CommandLine cmd = parser.parse(options, args);
            // Parse command-line arguments
            sessionName = cmd.getOptionValue("session");
            dataDir = cmd.getOptionValue("datadir");
            outputDir = cmd.getOptionValue("outputdir");
            configFile = cmd.getOptionValue("config");
            empireFile = cmd.getOptionValue("empiredata");
            shipClassFile = cmd.getOptionValue("shipclasses");
            hullParametersFile = cmd.getOptionValue("hullparameters");
        } catch (ParseException e) {
            final HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("SessionCreator", options);
            throw e;
        }
    }

    public SessionCreator(final String[] args) throws ParseException, IOException {
        extractCommandLineOptions(args);
        dao = new JsonStarEmpiresDAO(outputDir);
    }

    public TurnData createSession() throws IOException {

        final PropertiesUtil galaxyProperties = new PropertiesUtil(FileSystems.getDefault().getPath(dataDir, configFile));
        final int radius = galaxyProperties.getInt(Constants.CONFIG_RADIUS);
        final TurnData turnData = TurnData.builder()
             .radius(radius)
             .session(sessionName)
             .build();
        final List<String> shipClassData = loadItems(shipClassFile);
        final List<String> hullParametersData = loadItems(hullParametersFile);

        final List<String> empireData = loadItems(empireFile);

        final ThreadLocalRandom random = ThreadLocalRandom.current();

        // create empires
        final Map<Empire, EmpireCreation> empireCreations = Maps.newHashMap();
        for (final String data: empireData) {
            final String[] empireInfo = Arrays.stream(StringUtils.split(data, ",")).map(String::trim).toArray(String[]::new);
            final FrameOfReference frame = FrameOfReference.builder()
                        .obliqueOffset(random.nextInt(-radius / 2, radius / 2))
                        .yOffset(random.nextInt(-radius / 2, radius / 2))
                        .horizontalMirror(random.nextInt(2) == 1)
                        .verticalMirror(random.nextInt(2) == 1)
                        .rotation(HexDirection.from(random.nextInt(HexDirection.values().length)))
                        .build();
            final Empire empire = Empire.builder().name(empireInfo[0]).abbreviation(empireInfo[1]).empireType(EmpireType.valueOf(empireInfo[2])).
                    frameOfReference(frame).build();
            final EmpireCreation ecd = EmpireCreation.builder().homeworldName(empireInfo[3]).starbaseName(empireInfo[4]).build();
            empireCreations.put(empire, ecd);
        }
        turnData.addEmpires(empireCreations.keySet());
        final Empire gm = Empire.builder().name("GM").abbreviation("GM").empireType(EmpireType.GM).frameOfReference(FrameOfReference.DEFAULT_FRAME_OF_REFERENCE).build();

        // create HullParameters
        final String hullJson = Files.readString(FileSystems.getDefault().getPath(dataDir, hullParametersFile));
        final List<HullParameters> hullParameters = MAPPER.readValue(hullJson, new TypeReference<List<HullParameters>>() { });
        turnData.addHullParameters(hullParameters);

        // create ShipClasses
        final String shipClassJson = Files.readString(FileSystems.getDefault().getPath(dataDir, shipClassFile));
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
            empire.mergeObjectScanStatuses(empire.getKnownWorlds(), ScanStatus.VISIBLE);
            final ScanData scanData = new ScanData();
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
                        empire.mergeScanStatus(coord, ScanStatus.STALE);
                        empire.addKnownStorm(stormCoordinates.get(coord));
                        log.info("Added known storm {} for {}", stormCoordinates.get(coord), empire);
                    }
                    else {
                        empire.mergeScanStatus(coord, ScanStatus.SCANNED);
                    }
                    if (worldCoordinates.containsKey(coord)) {
                        empire.addKnownWorld(worldCoordinates.get(coord));
                        log.info("Added known world {} for {}", worldCoordinates.get(coord), empire);
                    }
                    if (portalCoordinates.containsKey(coord)) {
                        empire.addKnownPortal(portalCoordinates.get(coord));
                        log.info("Added known portal {} for {}", portalCoordinates.get(coord), empire);
                    }
                }
                empire.mergeScanStatus(ship, ScanStatus.VISIBLE);
                log.info("Initialized {} scan coordinates for {}", scanCoordinates.size(), empire);
            });

            // init know empires
            empire.addKnownEmpire(empire);
            empire.addKnownEmpire(gm);

            // init known ship classes
            shipClasses.forEach(empire::addKnownShipClass);

            // init GM
            gm.getScanData().mergeScanStatus(galaxy.getAllCoordinates(), ScanStatus.VISIBLE);
            turnData.getAllEmpires().forEach(gm::addKnownEmpire);
            shipClasses.forEach(gm::addKnownShipClass);
            worldCoordinates.values().forEach(gm::addKnownWorld);
            stormCoordinates.values().forEach(gm::addKnownStorm);
            portalCoordinates.values().forEach(p -> {
                gm.addKnownPortal(p);
                gm.addNavData(p);
            });
        }

        log.info("Initialized session {}", sessionName);
        return turnData;
    }

    public static void main(String[] args) {
        try {
            final SessionCreator creator = new SessionCreator(args);
            final TurnData turnData = creator.createSession();
            creator.saveSession(turnData);
            final TurnData turnData2 = creator.loadSession(turnData.getSession(), turnData.getTurnNumber());
            log.info("Loaded turnData " + turnData2);
        } catch (Exception exception) {
            log.error("Session creation failed", exception);
        }
    }

    private TurnData loadSession(final String session, final int turnNumber) throws Exception {
        return dao.loadData(session, turnNumber);
    }

    private void saveSession(final TurnData turndata) throws Exception {
        dao.saveData(turndata);
    }

    private Galaxy generateGalaxy(final PropertiesUtil galaxyProperties, final Map<Empire, EmpireCreation> empireCreations) throws IOException {
        final List<String> portalNames = loadItems("PortalNames.txt");
        final List<String> worldNames = loadItems("WorldNames.txt");
        final List<String> stormNames = loadItems("StormNames.txt");
        final List<String> nebulaNames = loadItems("NebulaNames.txt");

        final Galaxy galaxy = new Galaxy(galaxyProperties);
        galaxy.initHomeworlds(empireCreations);
        galaxy.initWorlds(worldNames);
        galaxy.initPortals(portalNames);
        galaxy.initNebulaeAndStorms(stormNames, nebulaNames);
        log.info("Generated galaxy");
        return galaxy;
    }
}