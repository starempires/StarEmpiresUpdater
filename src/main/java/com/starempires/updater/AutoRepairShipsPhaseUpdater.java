package com.starempires.updater;

import com.starempires.TurnData;
import com.starempires.objects.Empire;
import com.starempires.objects.Ship;

import java.util.Collection;

public class AutoRepairShipsPhaseUpdater extends PhaseUpdater {

    public AutoRepairShipsPhaseUpdater(final TurnData turnData) {
        super(Phase.AUTO_REPAIR_SHIPS, turnData);
    }

    @Override
    public void update() {
        final Collection<Ship> ships = turnData.getAllShips();
        ships.stream().filter(Ship::isRepairable)
                .forEach(ship -> {
            final int repaired = ship.autoRepair();
            if (repaired > 0) {
                String damageText;
                final int damage = ship.getDamage();
                if (damage > 0) {
                    damageText = damage + " damage remaining; OR " + formatOpRating(ship);
                }
                else {
                    damageText = "fully repaired";
                }
                final Collection<Empire> newsEmpires = turnData.getEmpiresPresent(ship);
                for (Empire newsEmpire : newsEmpires) {
                    if (newsEmpire.isKnownShipClass(ship.getShipClass())) {
                        addNews(newsEmpire, "Ship %s auto-repaired %d DP (%s)".formatted(ship, repaired, damageText));
                    }
                    else {
                        addNews(newsEmpire, "Ship %s auto-repaired %d DP".formatted(ship, repaired));
                    }
                }
            }
        });
    }
}