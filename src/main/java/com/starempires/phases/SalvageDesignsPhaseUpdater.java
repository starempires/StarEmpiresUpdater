package com.starempires.phases;

import com.starempires.TurnData;
import com.starempires.constants.Constants;
import com.starempires.objects.Empire;
import com.starempires.objects.Ship;
import com.starempires.objects.ShipClass;
import com.starempires.objects.SitRep;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public class SalvageDesignsPhaseUpdater extends PhaseUpdater {

    public SalvageDesignsPhaseUpdater(final TurnData turnData) {
        super(Phase.SALVAGE_DESIGNS, turnData);
    }

    private void salvage(final Ship ship, final Empire salvagingEmpire) {
        ShipClass shipClass = ship.getShipClass();
        Empire shipOwner = ship.getOwner();

        if (!salvagingEmpire.isKnownShipClass(shipClass)) {
            salvagingEmpire.addKnownShipClass(shipClass);
            addNews(salvagingEmpire, "You salvaged the design for the " + shipOwner.getName() + " ship class "
                    + shipClass + " from the debris of ship " + ship);
        }
    }

    @Override
    public void update() {
        final Set<Ship> possibleSalvages = turnData.getPossibleSalvages();
        final double salvageThreshold = turnData.getDoubleParameter(Constants.PARAMETER_SALVAGE_THRESHOLD,
                Constants.DEFAULT_SALVAGE_THRESHOLD);

        possibleSalvages.stream().filter(Ship::isSalvageable).forEach(ship -> {
            final Collection<Empire> empires = turnData.getEmpiresPresent(ship);
            final List<SitRep> sitReps = empires.stream()
                    .map(empire -> turnData.getSitRep(empire, ship.getCoordinate()))
                    .toList();
            final int salvageDp = sitReps.stream()
                    .filter(sitRep -> sitRep.getEmpire().equals(ship.getOwner()))
                    .findFirst()
                    .map(SitRep::getFriendlyDp).orElse(0);
            final SitRep winner = sitReps.stream()
                    .filter(sitRep -> !sitRep.getEmpire().equals(ship.getOwner())).min((sr1, sr2) -> Integer.compare(sr1.getFriendlyGuns(), sr2.getFriendlyGuns())).orElse(null);

            if (winner != null) {
                if (winner.getFriendlyGuns() > salvageThreshold * salvageDp) {
                    salvage(ship, winner.getEmpire());
                }
            }
        });
    }
}