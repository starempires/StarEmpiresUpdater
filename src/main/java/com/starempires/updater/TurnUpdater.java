package com.starempires.updater;

import com.starempires.TurnData;
import com.starempires.dao.S3StarEmpiresDAO;
import com.starempires.dao.StarEmpiresDAO;
import com.starempires.objects.Empire;
import com.starempires.orders.Order;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;


@Log4j2
public class TurnUpdater {

    private static final String ARG_SESSION_NAME = "session";
    private static final String ARG_TURN_NUMBER = "turn";
    private static final String ARG_SESSION_LOCATION = "sessionlocation";

    private final StarEmpiresDAO dao;
    private final String sessionName;
    private final int turnNumber;

    // Registry mapping Phase enums to their updater factories
    private static final Map<Phase, Function<TurnData, PhaseUpdater>> PHASE_REGISTRY = Map.ofEntries(
        Map.entry(Phase.FLUCTUATE_STORMS, FluctuateStormsPhaseUpdater::new),
        Map.entry(Phase.REMOVE_MAP_OBJECTS, RemoveMapObjectsPhaseUpdater::new),
        Map.entry(Phase.MOVE_MAP_OBJECTS, MoveMapObjectsPhaseUpdater::new),
        Map.entry(Phase.ADD_MAP_OBJECTS, AddMapObjectsPhaseUpdater::new),
        Map.entry(Phase.MODIFY_MAP_OBJECTS, ModifyMapObjectsPhaseUpdater::new),
        Map.entry(Phase.REMOVE_KNOWN_ITEMS, RemoveKnownItemsPhaseUpdater::new),
        Map.entry(Phase.ADD_KNOWN_ITEMS, AddKnownItemsPhaseUpdater::new),
        Map.entry(Phase.DRIFT_MAP_OBJECTS, DriftMapObjectsPhaseUpdater::new),
        Map.entry(Phase.STABILIZE_PORTALS, StabilizePortalsPhaseUpdater::new),
        Map.entry(Phase.COLLAPSE_PORTALS, CollapsePortalsPhaseUpdater::new),
        Map.entry(Phase.UNLOAD_SHIPS, UnloadShipPhaseUpdater::new),
        Map.entry(Phase.DEPLOY_DEVICES, DeployDevicesPhaseUpdater::new),
        Map.entry(Phase.LOAD_SHIPS, LoadShipPhaseUpdater::new),
        Map.entry(Phase.SELF_DESTRUCT_SHIPS, DestructShipsPhaseUpdater::new),
        Map.entry(Phase.FIRE_GUNS, FireGunsPhaseUpdater::new),
        Map.entry(Phase.APPLY_COMBAT_DAMAGE, ApplyCombatDamagePhaseUpdater::new),
        Map.entry(Phase.DETERMINE_OWNERSHIP_I, DetermineOwnershipIPhaseUpdater::new),
        Map.entry(Phase.TRANSMIT_PORTAL_NAV_DATA, TransmitPortalNavDataPhaseUpdater::new),
        Map.entry(Phase.MOVE_SHIPS, MoveShipsPhaseUpdater::new),
        Map.entry(Phase.TRAVERSE_PORTALS, TraversePortalsPhaseUpdater::new),
        Map.entry(Phase.ACQUIRE_NAV_DATA, AcquireNavDataPhaseUpdater::new),
        Map.entry(Phase.WEATHER_STORMS, WeatherStormsPhaseUpdater::new),
        Map.entry(Phase.APPLY_STORM_DAMAGE, ApplyStormDamagePhaseUpdater::new),
        Map.entry(Phase.DETERMINE_OWNERSHIP_II, DetermineOwnershipIIPhaseUpdater::new),
        Map.entry(Phase.RELOCATE_HOMEWORLDS, RelocateHomeworldsPhaseUpdater::new),
        Map.entry(Phase.ESTABLISH_PROHIBITIONS, EstablishProhibitionsPhaseUpdater::new),
        Map.entry(Phase.SALVAGE_DESIGNS, SalvageDesignsPhaseUpdater::new),
        Map.entry(Phase.CREATE_DESIGNS, DesignShipsPhaseUpdater::new),
        Map.entry(Phase.GIVE_DESIGNS, GiveDesignsPhaseUpdater::new),
        Map.entry(Phase.BUILD_SHIPS, BuildShipsPhaseUpdater::new),
        Map.entry(Phase.AUTO_REPAIR_SHIPS, AutoRepairShipsPhaseUpdater::new),
        Map.entry(Phase.REPAIR_SHIPS, RepairShipsPhaseUpdater::new),
        Map.entry(Phase.TOGGLE_TRANSPONDER_MODES, ToggleTransponderModesPhaseUpdater::new),
        Map.entry(Phase.CONCEAL_SHIPS, ConcealShipsPhaseUpdater::new),
        Map.entry(Phase.IDENTIFY_SHIPS, IdentifyShipsPhaseUpdater::new),
        Map.entry(Phase.PRODUCE_RESOURCE_UNITS, ProduceResourceUnitsPhaseUpdater::new),
        Map.entry(Phase.POOL_RESOURCE_UNITS, PoolResourceUnitsPhaseUpdater::new),
        Map.entry(Phase.TRANSFER_RESOURCE_UNITS, TransferResourceUnitsPhaseUpdater::new),
        Map.entry(Phase.DENY_SCAN_ACCESS, DenyScanDataPhaseUpdater::new),
        Map.entry(Phase.AUTHORIZE_SCAN_ACCESS, AuthorizeScanDataPhaseUpdater::new),
        Map.entry(Phase.COLLECT_SCAN_DATA, CollectScanDataPhaseUpdater::new),
        Map.entry(Phase.SHARE_SCAN_DATA, ShareScanDataPhaseUpdater::new),
        Map.entry(Phase.RECORD_NEW_MAP_OBJECTS, RecordNewMapObjectsPhaseUpdater::new)
    );

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
            formatter.printHelp("TurnUpdater", options);
            throw e;
        }
    }

    public TurnUpdater(final String sessionsLocation, final String sessionName, final int turnNumber) {
        this.sessionName = sessionName;
        this.turnNumber = turnNumber;
//        dao = new JsonStarEmpiresDAO(sessionsLocation, null);
        dao = new S3StarEmpiresDAO(sessionsLocation, null);
    }

    public TurnData updateTurn() throws Exception {
        final TurnData turnData = loadTurnData();
        final Empire gm = turnData.addGMEmpire();
        loadReadyOrders(turnData);
        processTurn(turnData);
        turnData.setTurnNumber(turnData.getTurnNumber() + 1);
        saveNews(turnData);
        // we don't actually need to serialize all of the GM's info in Empire form
        turnData.removeEmpire(gm);
        saveTurnData(turnData);
        return turnData;
    }

    public static void main(final String[] args) {
        try {
            final CommandLine cmd = extractCommandLineOptions(args);
            final String sessionsLocation = cmd.getOptionValue(ARG_SESSION_LOCATION);
            final String sessionName = cmd.getOptionValue(ARG_SESSION_NAME);
            final int turnNumber = Integer.parseInt(cmd.getOptionValue(ARG_TURN_NUMBER));
            final TurnUpdater turnUpdater = new TurnUpdater(sessionsLocation, sessionName, turnNumber);
            turnUpdater.updateTurn();
        } catch (Exception exception) {
            log.error("Update failed", exception);
        }
    }

    private TurnData loadTurnData() throws Exception {
        final TurnData turnData = dao.loadTurnData(sessionName, turnNumber);
        turnData.clearShipConditions();
        log.info("Loaded data for session {}, turn {}", sessionName, turnNumber);
        return turnData;
    }

    private void loadReadyOrders(final TurnData turnData) throws Exception {
        final Collection<Empire> empires = turnData.getAllEmpires();
        for (Empire empire: empires) {
            final List<Order> orders = dao.loadReadyOrders(sessionName, empire.getName(), turnNumber, turnData);
            turnData.addOrders(orders);
        }
    }

    private void processPhase(final PhaseUpdater phase) {
        phase.preUpdate();
        phase.update();
        phase.postUpdate();
    }

    private void processTurn(TurnData turnData) {
        log.info("Running update for session {} turn {}", sessionName, turnNumber);
        for (Phase phase : Phase.values()) {
            final Function<TurnData, PhaseUpdater> factory = PHASE_REGISTRY.get(phase);
            if (factory != null) {
                processPhase(factory.apply(turnData));
                log.info("Processed phase {}", phase);
            }
        }
        log.info("Processed all phases for session {}", sessionName);
    }

    private void saveTurnData(final TurnData turnData) throws Exception {
        dao.saveTurnData(sessionName, turnData);
        log.info("Saved turn data for session {} turn {}", sessionName, turnData.getTurnNumber());
    }

    private void saveNews(final TurnData turnData) throws Exception {
        dao.saveNews(sessionName, turnData.getNews(), turnData.getTurnNumber());
        log.info("Saved turn news for session {} turn {}", sessionName, turnData.getTurnNumber());
    }
}