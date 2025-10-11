package com.starempires.generator;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.starempires.TurnData;
import com.starempires.dao.S3StarEmpiresDAO;
import com.starempires.dao.StarEmpiresDAO;
import com.starempires.objects.Coordinate;
import com.starempires.objects.Empire;
import com.starempires.objects.Portal;
import com.starempires.objects.ScanStatus;
import com.starempires.objects.Ship;
import com.starempires.objects.Storm;
import com.starempires.objects.World;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Getter
@Log4j2
public class SnapshotGenerator {
    private static final String ARG_SESSION_NAME = "session";
    private static final String ARG_TURN_NUMBER = "turn";
    private static final String ARG_SESSION_LOCATION = "sessionlocation";

    private final StarEmpiresDAO dao;
    private final String sessionName;
    private final int turnNumber;

    private static CommandLine extractCommandLineOptions(final String[] args) throws ParseException {
        final Options options = new Options();
        try {
            options.addOption(Option.builder("s").argName("session name").longOpt(ARG_SESSION_NAME).hasArg().desc("session name").required().build());
            options.addOption(Option.builder("t").argName("turn number").longOpt(ARG_TURN_NUMBER).hasArg().desc("turn number").required().build());
            options.addOption(Option.builder("sl").argName("sessions locations").longOpt(ARG_SESSION_LOCATION).hasArg().desc("sessions location").required().build());

            final CommandLineParser parser = new DefaultParser();
            return parser.parse(options, args);
        } catch (ParseException e) {
            final HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("SnapshotGenerator", options);
            throw e;
        }
    }

    public TurnData loadTurnData() throws Exception {
        final TurnData turnData = dao.loadTurnData(sessionName, turnNumber);
        log.info("Loaded turn data for session {}, turn {}", sessionName, turnNumber);
        return turnData;
    }

    private List<String> loadEmpireNames() throws Exception {
        final List<String> empireData = dao.loadEmpireData(sessionName);
        final List<String> empireNames = empireData.stream()
                .map(str -> str.split(",")[0]) // Split and get the first value
                .collect(Collectors.toList()); //
        log.debug("Loaded empire names {}", empireNames);
        return empireNames;
    }

    private Map<String, String> loadColors() throws Exception {
        final Map<String, String> colors = dao.loadColors(sessionName);
        log.debug("Loaded map colors for session {}, turn {}", sessionName, turnNumber);
        return colors;
    }

    private void saveSnapshot(final EmpireSnapshot snapshot, final String empireName) throws Exception {
        dao.saveSnapshot(sessionName, empireName, turnNumber, snapshot);
    }

    public void generateSnapshots(final TurnData turnData) throws Exception {
        final Map<String, String> colors = loadColors();
        final List<String> empireNames = loadEmpireNames();
        final long timestamp = System.currentTimeMillis();
        for (String empireName: empireNames) {
            log.debug("Generating snapshot for {}", empireName);
            final EmpireSnapshot snapshot = generate(turnData, colors, empireName, timestamp);
            saveSnapshot(snapshot, empireName);
        }
        final EmpireSnapshot snapshot = generateGM(turnData, colors, timestamp);
        saveSnapshot(snapshot, "GM");
    }

    public static void main(final String[] args) {
        try {
            final CommandLine cmd = extractCommandLineOptions(args);
            final String sessionsLocation = cmd.getOptionValue(ARG_SESSION_LOCATION);
            final String sessionName = cmd.getOptionValue(ARG_SESSION_NAME);
            final int turnNumber = Integer.parseInt(cmd.getOptionValue(ARG_TURN_NUMBER));
            final SnapshotGenerator generator = new SnapshotGenerator(sessionsLocation, sessionName, turnNumber);
            final TurnData turnData = generator.loadTurnData();
            generator.generateSnapshots(turnData);
        } catch (Exception exception) {
            log.error("Error generating snapshots", exception);
        }
    }

    public SnapshotGenerator(final String sessionsLocation, final String sessionName, final int turnNumber) throws Exception {
        this.sessionName = sessionName;
        this.turnNumber = turnNumber;
//        dao = new JsonStarEmpiresDAO(sessionDir, null);
        dao = new S3StarEmpiresDAO(sessionsLocation, null);
    }

    private Map<String, SectorShipSnapshot> getSectorShipSnapshots(final Multimap<Empire, Ship> allEmpireShips, final Empire snapshotEmpire) {
        if (allEmpireShips == null || allEmpireShips.isEmpty()) {
            return null;
        }
        final Map<String, SectorShipSnapshot> snapshots = Maps.newHashMap();
        for (var entry : allEmpireShips.asMap().entrySet()) {
            final Empire empire = entry.getKey();
            final Collection<Ship> empireShips = entry.getValue();
            final Map<String, ShipSnapshot> empireShipsSnapshots = empireShips.stream()
                    .map(ship -> ShipSnapshot.fromShip(ship, snapshotEmpire == null ? ship.getOwner() : snapshotEmpire)).collect(Collectors.toMap(ShipSnapshot::getSerialNumber, Function.identity()));
            final SectorShipSnapshot sectorShipSnapshot =
                    SectorShipSnapshot.builder()
                            .count(empireShips.size())
                            .tonnage(empireShips.stream().mapToInt(Ship::getTonnage).sum())
                            .ships(empireShipsSnapshots)
                            .build();
            snapshots.put(empire.getName(), sectorShipSnapshot);
        }
        return snapshots;
    }

    private Multimap<Coordinate, Coordinate> getKnownConnections(final Empire empire, final TurnData turnData) {
        final var allConnections = turnData.getAllConnections();
        final Multimap<Coordinate, Coordinate> knownConnections = HashMultimap.create();
        for (var entry : allConnections.entries()) {
            if (empire.hasNavData(entry.getKey()) && empire.hasNavData(entry.getValue())) {
                knownConnections.put(empire.toLocal(entry.getKey().getCoordinate()), empire.toLocal(entry.getValue().getCoordinate()));
            }
        }
        return knownConnections;
    }

    private EmpireSnapshot generate(final TurnData turnData, final Map<String, String> colors, final String empireName, final long timestamp) {
        final Empire empire = turnData.getEmpire(empireName);
        final int radius = empire.computeMaxScanExtent();
        // EmpireSnapshot is the "outer" object that contains snapshot information for items known to that empire
        final EmpireSnapshot empireSnapshot = EmpireSnapshot.builder()
                .session(turnData.getSession())
                .abbreviation(empire.getAbbreviation())
                .name(empire.getName())
                .radius(radius)
                .columns(2 * radius + 1)
                .rows(4 * radius + 1)
                .turnNumber(turnData.getTurnNumber())
                .timestamp(timestamp)
                .build();

        // add "global" elements known to this empire
        empireSnapshot.addKnownShipClasses(empire.getKnownShipClasses());

        final Map<String, String> empireColors = colors.entrySet().stream()
                .filter(entry -> !entry.getKey().startsWith("empire-")) // Keep entries whose keys do not start with the prefix
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        empireColors.put(empireName, colors.get("empire-" + empireName));
        final Collection<Empire> knownEmpires = empire.getKnownEmpires();
        empireSnapshot.addKnownEmpires(knownEmpires.stream()
                .map(Empire::getName)
                .collect(Collectors.toSet()));
        knownEmpires.forEach(e -> empireColors.put(e.getName(), colors.get("empire-" + e.getName())));
        empireSnapshot.addColors(empireColors);

        empireSnapshot.addConnections(getKnownConnections(empire, turnData));

        // populate sectors
        final int numColumns = 2 * radius + 1;
        final Coordinate localOrigin = new Coordinate(0, 0);
        // get all known sectors in local coords, then translate each one to galactic
        final Set<Coordinate> surroundingLocalCoords = Coordinate.getSurroundingCoordinates(localOrigin, radius);
        surroundingLocalCoords.forEach(localCoord -> {
            final Coordinate galacticCoord = empire.toGalactic(localCoord);
            final ScanStatus status = empire.getScanStatus(galacticCoord);

            final int row = computeRow(radius, localCoord);
            final int column = computeColumn(numColumns, localCoord);

            World world = turnData.getWorld(galacticCoord);
            if (!empire.isKnownWorld(world)) {
                world = null;
            }
            final Collection<Portal> portals = turnData.getPortals(galacticCoord).stream().filter(empire::isKnownPortal).collect(Collectors.toSet());
            final Collection<Storm> storms = turnData.getStorms(galacticCoord).stream().filter(empire::isKnownStorm).collect(Collectors.toSet());

            Map<String, SectorShipSnapshot> sectorShipSnapshots = null;
            final Multimap<Empire, Ship> sectorShipsByEmpire = HashMultimap.create();
            if (status.isMoreVisible(ScanStatus.STALE)) {
                int unidentifiedTonnage = 0;
                int unidentifiedCount = 0;
                final Set<Empire> empiresPresent = turnData.getEmpiresPresent(galacticCoord);
                if (status == ScanStatus.SCANNED) {
                    for (final Empire empirePresent : empiresPresent) {
                        final Collection<Ship> empireSectorShips = empirePresent.getShips(galacticCoord);
                        if (empire.isKnownEmpire(empirePresent)) {
                            for (final Ship ship : empireSectorShips) {
                                if (ship.isVisibleToEmpire(empire)) {
                                    sectorShipsByEmpire.put(empirePresent, ship);
                                } else {
                                    unidentifiedCount++;
                                    unidentifiedTonnage += ship.getTonnage();
                                }
                            }
                        } else {
                            unidentifiedCount += empireSectorShips.size();
                            unidentifiedTonnage += empireSectorShips.stream()
                                    .map(Ship::getTonnage)
                                    .mapToInt(Integer::intValue)
                                    .sum();
                        }
                    }
                } else {
                    empiresPresent.forEach(empirePresent -> {
                        final Collection<Ship> empireSectorShips = empirePresent.getShips(galacticCoord);
                        for (final Ship ship : empireSectorShips) {
                            if (ship.isVisibleToEmpire(empire)) {
                                sectorShipsByEmpire.put(empirePresent, ship);
                            }
                        }
                    });
                }

                sectorShipSnapshots = getSectorShipSnapshots(sectorShipsByEmpire, empire);

                final SectorSnapshot sectorSnapshot = SectorSnapshot.builder()
                        .oblique(localCoord.getOblique())
                        .y(localCoord.getY())
                        .row(row)
                        .column(column)
                        .status(status)
                        .lastTurnScanned(empire.getLastTurnScanned(galacticCoord))
                        .world(WorldSnapshot.fromWorld(world, empire))
                        .portals(portals.stream().map(portal -> PortalSnapshot.fromPortal(portal, empire)).toList())
                        .ships(sectorShipSnapshots)
                        .storms(storms.stream().map(StormSnapshot::fromStorm).toList())
                        .unidentifiedShipCount(unidentifiedCount)
                        .unidentifiedShipTonnage(unidentifiedTonnage)
                        .build();
                empireSnapshot.addSector(sectorSnapshot);
                log.debug("Adding {} galactic coordinate {}/local coordinate {} to snapshot for empire {}", status, galacticCoord,
                        localCoord, empire);
            } else if (status == ScanStatus.STALE) {
                final SectorSnapshot sectorSnapshot = SectorSnapshot.builder()
                        .oblique(localCoord.getOblique())
                        .y(localCoord.getY())
                        .row(row)
                        .column(column)
                        .status(status)
                        .lastTurnScanned(empire.getLastTurnScanned(galacticCoord))
                        .world(WorldSnapshot.fromWorld(world, empire))
                        .portals(portals.stream().map(portal -> PortalSnapshot.fromPortal(portal, empire)).toList())
                        .storms(storms.stream().map(StormSnapshot::fromStorm).toList())
                        .build();
                empireSnapshot.addSector(sectorSnapshot);
                log.debug("Adding stale galactic coordinate {}/local coordinate {} to snapshot for empire {}", galacticCoord,
                        localCoord, empire);
            }
        });
        return empireSnapshot;
    }

    private EmpireSnapshot generateGM(final TurnData turnData, final Map<String, String> colors, final long timestamp) {
        final int radius = turnData.getRadius();
        // EmpireSnapshot is the "outer" object that contains snapshot information for items known to that empire
        final EmpireSnapshot empireSnapshot = EmpireSnapshot.builder()
                .abbreviation("GM")
                .name("GM")
                .radius(radius)
                .columns(2 * radius + 1)
                .rows(4 * radius + 1)
                .turnNumber(turnData.getTurnNumber())
                .timestamp(timestamp)
                .build();

        // add "global" elements known to this empire
        empireSnapshot.addKnownShipClasses(turnData.getAllShipClasses());

        final Collection<Empire> knownEmpires = turnData.getAllEmpires();
        empireSnapshot.addKnownEmpires(knownEmpires.stream()
                .map(Empire::getName)
                .collect(Collectors.toSet()));

        final Map<String, String> empireColors = colors.entrySet().stream()
                .collect(Collectors.toMap(
                        e -> e.getKey().replace("empire-", ""),
                        Map.Entry::getValue
                ));
        empireSnapshot.addColors(empireColors);

        final var allConnections = turnData.getAllConnections();
        final Multimap<Coordinate, Coordinate> knownConnections = HashMultimap.create();
        for (var entry : allConnections.entries()) {
             knownConnections.put(entry.getKey().getCoordinate(), entry.getValue().getCoordinate());
        }
        empireSnapshot.addConnections(knownConnections);

        // populate sectors
        final int numColumns = 2 * radius + 1;
        final Coordinate origin = new Coordinate(0, 0);
        final Set<Coordinate> surroundingCoords = Coordinate.getSurroundingCoordinates(origin, radius);
        surroundingCoords.forEach(galacticCoord -> {
            final int row = computeRow(radius, galacticCoord);
            final int column = computeColumn(numColumns, galacticCoord);

            final World world = turnData.getWorld(galacticCoord);
            final Collection<Portal> portals = turnData.getPortals(galacticCoord);
            final Collection<Storm> storms = turnData.getStorms(galacticCoord);

            Map<String, SectorShipSnapshot> sectorShipSnapshots = null;
            final Multimap<Empire, Ship> sectorShipsByEmpire = HashMultimap.create();
            final Set<Empire> empiresPresent = turnData.getEmpiresPresent(galacticCoord);
            empiresPresent.forEach(empirePresent -> {
                final Collection<Ship> empireSectorShips = empirePresent.getShips(galacticCoord);
                sectorShipsByEmpire.putAll(empirePresent, empireSectorShips);
            });

            sectorShipSnapshots = getSectorShipSnapshots(sectorShipsByEmpire, null);

            final SectorSnapshot sectorSnapshot = SectorSnapshot.builder()
                        .oblique(galacticCoord.getOblique())
                        .y(galacticCoord.getY())
                        .row(row)
                        .column(column)
                        .status(ScanStatus.VISIBLE)
                        .world(WorldSnapshot.forGM(world))
                        .portals(portals.stream().map(PortalSnapshot::forGM).toList())
                        .ships(sectorShipSnapshots)
                        .storms(storms.stream().map(StormSnapshot::fromStorm).toList())
                        .build();
                empireSnapshot.addSector(sectorSnapshot);
                log.debug("Adding galactic coordinate {} to GM snapshot", galacticCoord);
        });
        return empireSnapshot;
    }

    int computeRow(final int radius, final @NotNull Coordinate coordinate) {
        return -2 * coordinate.getY() + coordinate.getOblique() + 2 * radius;
    }

    int computeColumn(final int numColumns, final @NotNull Coordinate coordinate) {
        return coordinate.getOblique() + numColumns / 2;
    }
}