package com.starempires.phases;

import com.google.common.collect.Lists;
import com.starempires.TurnData;
import com.starempires.objects.Empire;
import com.starempires.objects.Ship;
import com.starempires.objects.ShipClass;
import com.starempires.objects.SitRep;

import java.util.Collection;
import java.util.List;

import static com.starempires.objects.IdentifiableObject.IDENTIFIABLE_NAME_COMPARATOR;

public class SalvageDesignsPhaseUpdater extends PhaseUpdater {

    public static final float SALVAGE_THRESHOLD = 3.f;

    public SalvageDesignsPhaseUpdater(final TurnData turnData) {
        super(Phase.SALVAGE_DESIGNS, turnData);
    }

    private void salvage(final Ship ship, final Empire salvagingEmpire) {
        final ShipClass shipClass = ship.getShipClass();
        if (!salvagingEmpire.isKnownShipClass(shipClass)) {
            salvagingEmpire.addKnownShipClass(shipClass);
            addNews(salvagingEmpire, "You salvaged the design for the " + ship.getOwner() + " ship class "
                    + shipClass + " from the debris of ship " + ship);
        }
    }

    @Override
    public void update() {
        final List<Ship> possibleSalvages = Lists.newArrayList(turnData.getPossibleSalvages());
        possibleSalvages.sort(IDENTIFIABLE_NAME_COMPARATOR);
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
                if (winner.getFriendlyGuns() > SALVAGE_THRESHOLD * salvageDp) {
                    salvage(ship, winner.getEmpire());
                }
            }
        });
    }
}