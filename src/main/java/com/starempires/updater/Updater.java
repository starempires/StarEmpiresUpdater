package com.starempires.updater;

import com.starempires.TurnData;
import com.starempires.constants.Constants;
import com.starempires.dao.JsonStarEmpiresDAO;
import com.starempires.dao.StarEmpiresDAO;
import com.starempires.phases.AcquireNavDataPhaseUpdater;
import com.starempires.phases.AddKnownItemsPhaseUpdater;
import com.starempires.phases.AddMapObjectsPhaseUpdater;
import com.starempires.phases.ApplyCombatDamagePhaseUpdater;
import com.starempires.phases.ApplyStormDamagePhaseUpdater;
import com.starempires.phases.AuthorizeScanDataPhaseUpdater;
import com.starempires.phases.AutoRepairShipsPhaseUpdater;
import com.starempires.phases.BuildShipsPhaseUpdater;
import com.starempires.phases.CollapsePortalsPhaseUpdater;
import com.starempires.phases.CollectScanDataPhaseUpdater;
import com.starempires.phases.ConcealShipsPhaseUpdater;
import com.starempires.phases.CreateDesignsPhaseUpdater;
import com.starempires.phases.DenyScanDataPhaseUpdater;
import com.starempires.phases.DeployDevicesPhaseUpdater;
import com.starempires.phases.DetermineOwnershipIIPhaseUpdater;
import com.starempires.phases.DetermineOwnershipIPhaseUpdater;
import com.starempires.phases.DissipateNebulaePhaseUpdater;
import com.starempires.phases.DissipateStormsPhaseUpdater;
import com.starempires.phases.DriftMapObjectsPhaseUpdater;
import com.starempires.phases.EstablishProhibitionsPhaseUpdater;
import com.starempires.phases.FireGunsPhaseUpdater;
import com.starempires.phases.FluctuateStormsPhaseUpdater;
import com.starempires.phases.GiveDesignsPhaseUpdater;
import com.starempires.phases.IdentifyShipsPhaseUpdater;
import com.starempires.phases.LoadShipPhaseUpdater;
import com.starempires.phases.ModifyMapObjectsPhaseUpdater;
import com.starempires.phases.MoveMapObjectsPhaseUpdater;
import com.starempires.phases.MoveShipsPhaseUpdater;
import com.starempires.phases.PhaseUpdater;
import com.starempires.phases.PoolResourceUnitsPhaseUpdater;
import com.starempires.phases.ProduceResourceUnitsPhaseUpdater;
import com.starempires.phases.RecordNewMapObjectsPhaseUpdater;
import com.starempires.phases.RelocateHomeworldsPhaseUpdater;
import com.starempires.phases.RemoveDestroyedShipsIIPhaseUpdater;
import com.starempires.phases.RemoveDestroyedShipsIPhaseUpdater;
import com.starempires.phases.RemoveKnownItemsPhaseUpdater;
import com.starempires.phases.RemoveMapObjectsPhaseUpdater;
import com.starempires.phases.RepairShipsPhaseUpdater;
import com.starempires.phases.SalvageDesignsPhaseUpdater;
import com.starempires.phases.SelfDestructShipsPhaseUpdater;
import com.starempires.phases.ShareScanDataPhaseUpdater;
import com.starempires.phases.StabilizePortalsPhaseUpdater;
import com.starempires.phases.ToggleTransponderModesPhaseUpdater;
import com.starempires.phases.TransferResourceUnitsPhaseUpdater;
import com.starempires.phases.TransmitPortalNavDataPhaseUpdater;
import com.starempires.phases.TraversePortalsPhaseUpdater;
import com.starempires.phases.UnloadShipPhaseUpdater;
import com.starempires.phases.WeatherStormsPhaseUpdater;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;


@Log4j2
public class Updater {

    private final StarEmpiresDAO dao;
    private String sessionName;
    private int turnNumber;
    private String sessionDir;

    private void extractCommandLineOptions(final String[] args) throws ParseException {
        final Options options = new Options();
        try {
            options.addOption(Option.builder("s").argName("session name").longOpt("session").hasArg().desc("session name").required().build());
            options.addOption(Option.builder("t").argName("turn number").longOpt("turn").hasArg().desc("turn number").required().build());
            options.addOption(Option.builder("sd").argName("session dir").longOpt("sessiondir").hasArg().desc("session dir").required().build());

            final CommandLineParser parser = new DefaultParser();
            final CommandLine cmd = parser.parse(options, args);
            sessionName = cmd.getOptionValue(Constants.ARG_SESSION_NAME);
            turnNumber = Integer.parseInt(cmd.getOptionValue(Constants.ARG_TURN_NUMBER));

            sessionDir = cmd.getOptionValue("sessiondir");
        } catch (ParseException e) {
            final HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("Updater", options);
            throw e;
        }
    }

    public Updater(final String[] args) throws Exception {
        extractCommandLineOptions(args);
        dao = new JsonStarEmpiresDAO(sessionDir);
    }

    public static void main(String[] args) {
        try {
            final Updater updater = new Updater(args);
            final TurnData turnData = updater.loadTurnData();
            updater.processTurn(turnData);
            turnData.setTurnNumber(turnData.getTurnNumber() + 1);
            updater.saveTurnData(turnData);
        } catch (Exception exception) {
            log.error("Update failed", exception);
        }
    }

    private TurnData loadTurnData() throws Exception {
        final TurnData turnData = dao.loadTurnData(sessionName, turnNumber);
        log.info("Loaded data for session {}, turn {}", sessionName, turnNumber);
        return turnData;
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
        processPhase(new SelfDestructShipsPhaseUpdater(turnData));
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
        processPhase(new CreateDesignsPhaseUpdater(turnData));
        processPhase(new GiveDesignsPhaseUpdater(turnData));

        // Maintenance phases
        processPhase(new BuildShipsPhaseUpdater(turnData));
        processPhase(new RepairShipsPhaseUpdater(turnData));
        processPhase(new ToggleTransponderModesPhaseUpdater(turnData));
        processPhase(new ConcealShipsPhaseUpdater(turnData));
        processPhase(new IdentifyShipsPhaseUpdater(turnData));

        // Income phases
        processPhase(new ProduceResourceUnitsPhaseUpdater(turnData));
        processPhase(new PoolResourceUnitsPhaseUpdater(turnData));
        processPhase(new TransferResourceUnitsPhaseUpdater(turnData));

        // Scanning phases
        processPhase(new DenyScanDataPhaseUpdater(turnData));
        processPhase(new AuthorizeScanDataPhaseUpdater(turnData));
        processPhase(new CollectScanDataPhaseUpdater(turnData));
        processPhase(new ShareScanDataPhaseUpdater(turnData));
        processPhase(new RecordNewMapObjectsPhaseUpdater(turnData));
        log.info("Processed all phases for session {}", sessionName);
    }

    private void saveTurnData(TurnData turnData) throws Exception {
        dao.saveTurnData(turnData);
        log.info("Saved turn data for session {} turn {}", sessionName, turnData.getTurnNumber());
    }
}