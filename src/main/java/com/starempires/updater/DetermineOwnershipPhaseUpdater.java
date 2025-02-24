package com.starempires.updater;

import com.google.common.collect.Sets;
import com.starempires.TurnData;
import com.starempires.objects.Empire;
import com.starempires.objects.OwnableObject;
import com.starempires.objects.Ship;
import com.starempires.objects.World;

import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class DetermineOwnershipPhaseUpdater extends PhaseUpdater {

    public DetermineOwnershipPhaseUpdater(final Phase phase, final TurnData turnData) {
        super(phase, turnData);
    }

    @Override
    public void update() {
        for (World world : turnData.getAllWorlds()) {
            final Set<Empire> empires = Sets.newHashSet();
            final Empire currentOwner = world.getOwner();
            final Collection<Ship> ships = turnData.getLiveShips(world);
            final Set<Empire> conqueringEmpires = ships.stream().filter(Ship::isConqueringShip)
                    .map(OwnableObject::getOwner)
                    .collect(Collectors.toSet());
            empires.addAll(conqueringEmpires);

            final Empire newOwner;
            if (empires.isEmpty()) { // no conquering ships -- no owner
                newOwner = currentOwner;
            }
            else if (empires.size() == 1) { // lone empire present becomes new owner
                newOwner = empires.stream().findFirst().get();
            }
            else if (empires.contains(currentOwner)) { // current owner among many empires retains ownership
                newOwner = currentOwner;
            }
            else { // current owner not among empires present, so no clear owner
                newOwner = null;
            }

            if (!Objects.equals(newOwner, currentOwner)) {
                final Collection<Empire> newsEmpires = turnData.getEmpiresPresent(world);
                final String descriptor = world.isHomeworld() ? "Homeworld" : "World";

                if (newOwner == null) {
                    addNews(newsEmpires, descriptor + " " + world + " is now unowned");
                }
                else {
                    addNews(newsEmpires, descriptor + " " + world + " is now owned by " + newOwner);
                }
                world.setOwner(newOwner);
                world.setHomeworld(false);
            }
        }
    }
}