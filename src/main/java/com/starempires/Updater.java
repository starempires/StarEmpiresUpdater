package com.starempires;

import com.starempires.constants.Constants;
import com.starempires.dao.StarEmpiresDAO;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.ParseException;

import java.sql.SQLException;


@Log4j2
public class Updater {

    private StarEmpiresDAO dao;
    private String sessionName;
    private int turnNumber;

    private void extractCommandLineOptions(final String[] args) throws ParseException {
        final Options options = new Options();
        options.addOption(Option.builder("s").argName("session name").longOpt("session").hasArg().desc("session name").required().build());
        options.addOption(Option.builder("t").argName("turn number").longOpt("turn").hasArg().desc("turn number").required().build());

        final CommandLineParser parser = new DefaultParser();
        final HelpFormatter formatter = new HelpFormatter();

        try {
            // Parse command-line arguments
            final CommandLine cmd = parser.parse(options, args);
            sessionName = cmd.getOptionValue(Constants.ARG_SESSION_NAME);
            turnNumber = Integer.parseInt(cmd.getOptionValue(Constants.ARG_TURN_NUMBER));
        } catch (ParseException e) {
            formatter.printHelp("Updater", options);
            throw e;
        }
    }

    public Updater(final String[] args) throws Exception {
        extractCommandLineOptions(args);
//        dao = new FileStarEmpiresDAO();
//        log.info("Running update for session %s, turn %d".formatted(sessionName, turnNumber));
//        final TurnData turnData = loadTurnData(turnNumber);
//        processTurn(turnData);
//        saveTurnData(turnData);
    }

    private TurnData loadTurnData(final int turnNumber) throws Exception {
        final TurnData turnData = dao.loadData(sessionName, turnNumber);
        log.info("Loaded data for session {}, turn {}", sessionName, turnNumber);
        return turnData;
    }

//    private void logResults(TurnData turnData) {
//        TurnNews news = turnData.getNews();
//        news.dump();
//    }
//
//    private void processPhase(final PhaseUpdater phase) {
//        phase.preUpdate();
//        phase.update();
//        phase.postUpdate();
//    }
//
//    private void processTurn(TurnData turnData) throws SecurityException, IllegalArgumentException,
//            NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
//        // TODO:create synthetic orders
//        // MoveMap -> Remove + Add
//        // Deploy -> unload
//        // fire missile -> unload
//
//        // Astronomics phases
//        processPhase(new RemoveMapObjectsPhaseUpdater(turnData, properties_));
//        processPhase(new MoveMapObjectsPhaseUpdater(turnData, properties_));
//        processPhase(new AddMapObjectsPhaseUpdater(turnData, properties_));
//        processPhase(new ModifyMapObjectsPhaseUpdater(turnData, properties_));
//        processPhase(new DriftMapObjectsPhaseUpdater(turnData, properties_));
//        processPhase(new RemoveKnownItemsPhaseUpdater(turnData, properties_));
//        processPhase(new AddKnownItemsPhaseUpdater(turnData, properties_));
//        processPhase(new StabilizePortalsPhaseUpdater(turnData, properties_));
//        processPhase(new CollapsePortalsPhaseUpdater(turnData, properties_));
//        processPhase(new DissipateNebulaePhaseUpdater(turnData, properties_));
//        // processPhase(ActivateNebulaePhaseUpdater(turnData, properties_)););
//        processPhase(new DissipateStormsPhaseUpdater(turnData, properties_));
//        processPhase(new FluctuateStormsPhaseUpdater(turnData, properties_));
//
//        // Logistics phases
//        processPhase(new UnloadShipPhaseUpdater(turnData, properties_));
//        processPhase(new DeployDevicesPhaseUpdater(turnData, properties_));
//        processPhase(new LoadShipPhaseUpdater(turnData, properties_));
//
//        // Combat phases
//        processPhase(new SelfDestructShipsPhaseUpdater(turnData, properties_));
//        processPhase(new FireGunsPhaseUpdater(turnData, properties_));
//        processPhase(new ApplyCombatDamagePhaseUpdater(turnData, properties_));
//        processPhase(new RemoveDestroyedShipsIPhaseUpdater(turnData, properties_));
//        processPhase(new AutoRepairShipsPhaseUpdater(turnData, properties_));
//        processPhase(new DetermineOwnershipIPhaseUpdater(turnData, properties_));
//
//        // Movement phases
//        processPhase(new TransmitPortalNavDataPhaseUpdater(turnData, properties_));
//        processPhase(new MoveShipsPhaseUpdater(turnData, properties_));
//        processPhase(new TraversePortalsPhaseUpdater(turnData, properties_));
//        processPhase(new AcquireNavDataPhaseUpdater(turnData, properties_));
//        processPhase(new WeatherStormsPhaseUpdater(turnData, properties_));
//        processPhase(new ApplyStormDamagePhaseUpdater(turnData, properties_));
//        processPhase(new RemoveDestroyedShipsIIPhaseUpdater(turnData, properties_));
//        processPhase(new DetermineOwnershipIIPhaseUpdater(turnData, properties_));
//        processPhase(new RelocateHomeworldsPhaseUpdater(turnData, properties_));
//        processPhase(new EstablishProhibitionsPhaseUpdater(turnData, properties_));
//
//        // Research phases
//        processPhase(new SalvageDesignsPhaseUpdater(turnData, properties_));
//        processPhase(new CreateDesignsPhaseUpdater(turnData, properties_));
//        processPhase(new GiveDesignsPhaseUpdater(turnData, properties_));
//
//        // Maintenance phases
//        processPhase(new BuildShipsPhaseUpdater(turnData, properties_));
//        processPhase(new RepairShipsPhaseUpdater(turnData, properties_));
//        processPhase(new ToggleTransponderModesPhaseUpdater(turnData, properties_));
//        processPhase(new ConcealShipsPhaseUpdater(turnData, properties_));
//        processPhase(new IdentifyShipsPhaseUpdater(turnData, properties_));
//
//        // Income phases
//        processPhase(new ProduceResourceUnitsPhaseUpdater(turnData, properties_));
//        processPhase(new PoolResourceUnitsPhaseUpdater(turnData, properties_));
//        processPhase(new TransferResourceUnitsPhaseUpdater(turnData, properties_));
//
//        // Scanning phases
//        processPhase(new DenyScanDataPhaseUpdater(turnData, properties_));
//        processPhase(new AuthorizeScanDataPhaseUpdater(turnData, properties_));
//        processPhase(new CollectScanDataPhaseUpdater(turnData, properties_));
//        processPhase(new ShareScanDataPhaseUpdater(turnData, properties_));
//        processPhase(new RecordNewMapObjectsPhaseUpdater(turnData, properties_));
//        LOG.info("Processed all phases for session {}", sessionName_);
//    }
//
//    private void saveTurnData(TurnData turnData) throws SQLException {
//        dao_.saveData(turnData);
//        LOG.info("Saved turn data for session {}, turn {}", sessionName_, turnData.getTurnNumber());
//    }
}
