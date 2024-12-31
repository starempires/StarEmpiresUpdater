package com.starempires.updater;

import com.starempires.TurnData;

public class RemoveDestroyedShipsIPhaseUpdater extends RemoveDestroyedShipsPhaseUpdater {

    public RemoveDestroyedShipsIPhaseUpdater(final TurnData turnData) {
        super(Phase.REMOVE_DESTROYED_SHIPS_I, turnData);
    }
}