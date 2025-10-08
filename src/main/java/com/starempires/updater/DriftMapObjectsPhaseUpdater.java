package com.starempires.updater;

import com.starempires.TurnData;
import com.starempires.objects.MappableObject;

import java.util.Collection;

public class DriftMapObjectsPhaseUpdater extends PhaseUpdater {

    public DriftMapObjectsPhaseUpdater(final TurnData turnData) {
        super(Phase.DRIFT_MAP_OBJECTS, turnData);
    }

    private void drift(String type, Collection<? extends MappableObject> objects) {
        objects.forEach(object -> {
            // TODO
        });
    }

    @Override
    public void update() {
        drift("World", turnData.getAllWorlds());
        drift("Portal", turnData.getAllPortals());
        drift("Storm", turnData.getAllStorms());
    }
}