package com.starempires.updater;

import com.starempires.TurnData;

public class RemoveDestroyedShipsIIPhaseUpdater extends RemoveDestroyedShipsPhaseUpdater {

    public RemoveDestroyedShipsIIPhaseUpdater(TurnData turnData) {
        super(Phase.REMOVE_DESTROYED_SHIPS_II, turnData);
    }
}