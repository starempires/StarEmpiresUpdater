package com.starempires.phases;

import com.starempires.TurnData;
import com.starempires.objects.DeviceType;
import com.starempires.objects.Empire;
import com.starempires.objects.Ship;
import com.starempires.objects.World;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;

public class ProduceResourceUnitsPhaseUpdater extends PhaseUpdater {

    public ProduceResourceUnitsPhaseUpdater(final TurnData turnData) {
        super(Phase.PRODUCE_RESOURCE_UNITS, turnData);
    }

    @Override
    public void update() {
        final Collection<World> worlds = turnData.getAllWorlds();
        worlds.stream().filter(World::isOwned).forEach(world -> {
            final Collection<Empire> newsEmpires = turnData.getEmpiresPresent(world);
            final Collection<Ship> devices = turnData.getDeployedDevices(world, DeviceType.POLLUTION_BOMB);
            final double multiplier = world.getProductionMultiplier();
            final int production = world.getProduction();
            final AtomicInteger amount = new AtomicInteger();
            amount.set((int) Math.ceil(production * multiplier));
            String text = "World " + world + " produced " + amount + " RU";
            if (multiplier != 1.0) {
                text += " (" + production + " x " + multiplier + ")";
            }
            addNews(newsEmpires, text);
            devices.forEach(device -> {
                if (world.isHomeworld()) {
                    addNews(newsEmpires, "Pollution bomb " + device + " has no effect on homeworld " + world);
                }
                else {
                    addNews(newsEmpires, "Pollution bomb " + device + " reduces production of world " +
                            world + " by " + device.getGuns());
                    amount.addAndGet(-device.getGuns());
                }
            });
            world.adjustStockpile(Math.max(amount.get(), 0));
        });
    }
}