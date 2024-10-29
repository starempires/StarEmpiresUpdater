package com.starempires;

import com.starempires.constants.Constants;
import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Log4j2
public class Main {

    public static void main(String[] args) {
        try {
            final Updater updater = new Updater(args);
//            final int turnNumber = updater.getProperties().getInt(Constants.ARG_TURN_NUMBER);
//            final TurnData turnData = updater.loadTurnData(turnNumber);
//            updater.processTurn(turnData);
//            updater.saveTurnData(turnData);
            // updater.logresults(turndata);
        } catch (Exception exception) {
            log.error("Caught exception, turn update not complete", exception);
        }
    }
}