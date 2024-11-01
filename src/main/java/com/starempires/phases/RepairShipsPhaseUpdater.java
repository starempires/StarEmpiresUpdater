package com.starempires.phases;

import com.google.common.collect.Sets;
import com.starempires.TurnData;
import com.starempires.constants.Constants;
import com.starempires.objects.Empire;
import com.starempires.objects.Order;
import com.starempires.objects.OrderType;
import com.starempires.objects.Ship;
import com.starempires.objects.World;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public class RepairShipsPhaseUpdater extends PhaseUpdater {

    public RepairShipsPhaseUpdater(final TurnData turnData) {
        super(Phase.REPAIR_SHIPS, turnData);
    }

    private void repairOrbitalCargo() {
        final Collection<Ship> ships = turnData.getAllShips();
        ships.stream().filter(Ship::isOrbital).forEach(orbital -> {
            final Set<Ship> cargos = orbital.getCargo();
            cargos.stream().filter(Ship::isRepairable).forEach(cargo -> {
                final int maxRepair = cargo.getMaxRepairAmount();
                cargo.repair(maxRepair);
                final Set<Empire> newsEmpires = Sets.newHashSet(cargo.getOwner(), orbital.getOwner());
                addNews(newsEmpires, "Orbital " + orbital + " repaired " + maxRepair + " DP on cargo " + cargo);
            });
        });
    }

    @Override
    public void update() {
        repairOrbitalCargo();
        final List<Order> orders = turnData.getOrders(OrderType.REPAIR);
        double repairDpPerRu = turnData.getDoubleParameter(Constants.PARAMETER_REPAIR_DP_PER_RU,
                Constants.DEFAULT_REPAIR_DP_PER_RU);
        double orbitalRepairDpPerRu = turnData.getDoubleParameter(Constants.PARAMETER_ORBITAL_REPAIR_DP_PER_RU,
                Constants.DEFAULT_ORBITAL_REPAIR_DP_PER_RU);

        orders.forEach(order -> {
            final Empire empire = order.getEmpire();
            final String empireHandle = order.getStringParameter(0);
            final int amount = order.getIntParameter(1);
            final String worldName = order.getStringParameter(2);
            final Ship ship = turnData.getShip(empireHandle);
            if (ship == null || !ship.getOwner().equals(empire)) {
                addNewsResult(order, empire, "You do not own ship " + empireHandle);
            }
            else if (!ship.isAlive()) {
                addNewsResult(order, empire, "Ship " + ship + " has been destroyed.");
            }
            else if (ship.isRepairable()) {
                addNewsResult(order, empire, "Ship " + ship + " needs no repairs.");
            }
            else {
                final World world = turnData.getWorld(worldName);
                if (world == null || !world.getOwner().equals(empire)) {
                    addNewsResult(order, empire, "You do not own world " + world);
                }
                else if (world.isInterdicted()) {
                    addNewsResult(order, empire, "World " + world + " is interdicted; no repairs possible.");
                }
                else if (world.isBlockaded() && !ship.isSameSector(world)) {
                    addNewsResult(order, empire, "World " + world + " is blockaded; no offworld repairs possible.");
                }
                else {
                    final double multiplier = ship.isOrbital() ? orbitalRepairDpPerRu : repairDpPerRu;
                    final int stockpile = world.getStockpile();
                    if (stockpile > multiplier) {
                        final int maxRepair = ship.getMaxRepairAmount();
                        final int paidRepairs = (int) Math.round(amount * multiplier);
                        final int amountToRepair = Math.min(maxRepair, paidRepairs);
                        final int fee = (int) Math.min(stockpile,
                                Math.ceil(amountToRepair / multiplier));
                        ship.repair(amountToRepair);
                        final Collection<Empire> newEmpires = turnData.getEmpiresPresent(ship);
                        newEmpires.remove(empire);
                        final int remaining = world.adjustStockpile(-fee);
                        addNewsResult(order, empire,
                                "You repaired " + amountToRepair + " DP on ship " + ship
                                        + " (" + fee + " RU fee; " + remaining
                                        + " remaining)");
                        addNewsResult(order, newEmpires,
                                empire + " repaired " + amountToRepair + " DP on ship " + ship);
                    }
                    else {
                        addNewsResult(order, empire, "Insufficient stockpile (" + stockpile
                                + ") on world " + world + "; no repairs possible");
                    }
                }
            }
        });
    }
}