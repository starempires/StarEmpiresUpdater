package com.starempires.updater;

import com.starempires.TurnData;
import com.starempires.dao.S3StarEmpiresDAO;
import com.starempires.dao.StarEmpiresDAO;
import com.starempires.orders.Order;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import java.util.List;


@Log4j2
public class TurnUpdater {

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
        loadReadyOrders(turnData);
        processTurn(turnData);
        turnData.setTurnNumber(turnData.getTurnNumber() + 1);
        saveTurnData(turnData);
        saveNews(turnData);
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
        log.info("Loaded data for session {}, turn {}", sessionName, turnNumber);
        return turnData;
    }

    private void loadReadyOrders(final TurnData turnData) throws Exception {
        final List<String> empireNames = dao.loadEmpireNames(sessionName);
        for (String empireName: empireNames) {
            final List<Order> orders = dao.loadReadyOrders(sessionName, empireName, turnNumber, turnData);
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

//        // TODO:create synthetic orders
//        // MoveMap -> Remove + Add
//        // Deploy -> unload
//        // fire missile -> unload

//        // Astronomics
        processPhase(new RemoveMapObjectsPhaseUpdater(turnData));
        processPhase(new MoveMapObjectsPhaseUpdater(turnData));
        processPhase(new AddMapObjectsPhaseUpdater(turnData));
        processPhase(new ModifyMapObjectsPhaseUpdater(turnData));
        processPhase(new DriftMapObjectsPhaseUpdater(turnData));
        processPhase(new RemoveKnownItemsPhaseUpdater(turnData));
        processPhase(new AddKnownItemsPhaseUpdater(turnData));
        processPhase(new StabilizePortalsPhaseUpdater(turnData));
        processPhase(new CollapsePortalsPhaseUpdater(turnData));
        processPhase(new DissipateNebulaePhaseUpdater(turnData));
//        processPhase(new ActivateNebulaePhaseUpdater(turnData));
        processPhase(new DissipateStormsPhaseUpdater(turnData));
        processPhase(new FluctuateStormsPhaseUpdater(turnData));

        // Logistics
        processPhase(new UnloadShipPhaseUpdater(turnData));
        processPhase(new DeployDevicesPhaseUpdater(turnData));
        processPhase(new LoadShipPhaseUpdater(turnData));

        // Combat
        processPhase(new DestructShipsPhaseUpdater(turnData));
        processPhase(new FireGunsPhaseUpdater(turnData));
        processPhase(new ApplyCombatDamagePhaseUpdater(turnData));
        processPhase(new RemoveDestroyedShipsIPhaseUpdater(turnData));
        processPhase(new AutoRepairShipsPhaseUpdater(turnData));
        processPhase(new DetermineOwnershipIPhaseUpdater(turnData));

        // Movement phases
        processPhase(new TransmitPortalNavDataPhaseUpdater(turnData));
        processPhase(new MoveShipsPhaseUpdater(turnData));
        processPhase(new TraversePortalsPhaseUpdater(turnData));
        processPhase(new AcquireNavDataPhaseUpdater(turnData));
        processPhase(new WeatherStormsPhaseUpdater(turnData));
        processPhase(new ApplyStormDamagePhaseUpdater(turnData));
        processPhase(new RemoveDestroyedShipsIIPhaseUpdater(turnData));
        processPhase(new DetermineOwnershipIIPhaseUpdater(turnData));
        processPhase(new RelocateHomeworldsPhaseUpdater(turnData));
        processPhase(new EstablishProhibitionsPhaseUpdater(turnData));

        // Research phases
        processPhase(new SalvageDesignsPhaseUpdater(turnData));
        processPhase(new DesignShipsPhaseUpdater(turnData));
        processPhase(new GiveDesignsPhaseUpdater(turnData));

        // Maintenance phases
        processPhase(new BuildShipsPhaseUpdater(turnData));
        processPhase(new RepairShipsPhaseUpdater(turnData));
        processPhase(new ToggleTransponderModesPhaseUpdater(turnData));
//        processPhase(new ConcealShipsPhaseUpdater(turnData));
//        processPhase(new IdentifyShipsPhaseUpdater(turnData));
//
//        // Income phases
        processPhase(new ProduceResourceUnitsPhaseUpdater(turnData));
        processPhase(new PoolResourceUnitsPhaseUpdater(turnData));
        processPhase(new TransferResourceUnitsPhaseUpdater(turnData));
//
//        // Scanning phases
//        processPhase(new DenyScanDataPhaseUpdater(turnData));
//        processPhase(new AuthorizeScanDataPhaseUpdater(turnData));
        processPhase(new CollectScanDataPhaseUpdater(turnData));
        processPhase(new ShareScanDataPhaseUpdater(turnData));
        processPhase(new RecordNewMapObjectsPhaseUpdater(turnData));
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