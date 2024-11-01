package com.starempires.phases;

import com.google.common.collect.Sets;
import com.starempires.TurnData;
import com.starempires.objects.Empire;
import com.starempires.objects.OwnableObject;
import com.starempires.objects.Ship;
import com.starempires.objects.World;

import java.util.Collection;
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
            if (currentOwner != null) {
                empires.add(currentOwner);
            }
            final Collection<Ship> ships = turnData.getLiveShips(world);
            final Set<Empire> conqueringEmpires = ships.stream().filter(Ship::isConqueringShip)
                    .map(OwnableObject::getOwner)
                    .collect(Collectors.toSet());
            empires.addAll(conqueringEmpires);

            final Empire newOwner;
            if (empires.isEmpty()) {
                newOwner = null;
            }
            else if (empires.size() == 1) {
                newOwner = empires.stream().findFirst().get();
            }
            else if (empires.contains(currentOwner)) {
                newOwner = currentOwner;
            }
            else {
                newOwner = null;
            }

            if (newOwner != currentOwner) {
                final Collection<Empire> newsEmpires = turnData.getEmpiresPresent(world);
                final String descriptor = world.isHomeworld() ? "Homeworld" : "World";
                if (newOwner == null) {
                    addNews(currentOwner, "You have lost possession of " + descriptor.toLowerCase() + " " + world);
                    addNews(newsEmpires, descriptor + " " + world + " is now unowned");
                }
                else {
                    newsEmpires.remove(currentOwner);
                    newsEmpires.remove(newOwner);
                    addNews(currentOwner, descriptor + " " + world + " has been taken from you by " + newOwner);
                    addNews(newOwner,
                            "You have taken " + descriptor.toLowerCase() + " " + world + " from " + currentOwner);
                    addNews(newsEmpires,
                            descriptor + " " + world + " has been taken from " + currentOwner + " by " + newOwner);
                }
                world.setOwner(newOwner);
                world.setHomeworld(false);
            }
        }
    }
}