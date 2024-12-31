package com.starempires.updater;

import com.starempires.TurnData;
import com.starempires.constants.Constants;
import com.starempires.objects.Empire;
import com.starempires.objects.Ship;
import com.starempires.objects.World;
import com.starempires.orders.Order;
import com.starempires.orders.OrderType;
import com.starempires.orders.RepairOrder;

import java.util.Collection;
import java.util.List;

import static com.starempires.objects.IdentifiableObject.IDENTIFIABLE_NAME_COMPARATOR;

public class RepairShipsPhaseUpdater extends PhaseUpdater {

    public RepairShipsPhaseUpdater(final TurnData turnData) {
        super(Phase.REPAIR_SHIPS, turnData);
    }

    private void repairOrbitalCargo() {
        final Collection<Ship> ships = turnData.getAllShips();
        ships.stream().filter(Ship::isOrbital).sorted(IDENTIFIABLE_NAME_COMPARATOR).forEach(orbital -> {
            final Collection<Ship> cargos = orbital.getCargo();
            cargos.stream().filter(Ship::isRepairable).sorted(IDENTIFIABLE_NAME_COMPARATOR).forEach(cargo -> {
                final int maxRepair = cargo.getMaxRepairAmount();
                cargo.repair(maxRepair);
                addNews(orbital.getOwner(), "Orbital " + orbital + " repaired " + maxRepair + " DP on cargo " + cargo);
            });
        });
    }

    @Override
    public void update() {
        repairOrbitalCargo();
        final List<Order> orders = turnData.getOrders(OrderType.REPAIR);
        orders.forEach(o -> {
            RepairOrder order = (RepairOrder) o;
            final Empire empire = order.getEmpire();
            final Ship ship = order.getShip();
            final List<World> worlds = order.getWorlds();
            final int dpToRepair = order.getDpToRepair();
            if (!ship.isAlive()) {
                addNewsResult(order, empire, "Ship " + ship + " has been destroyed.");
            } else if (ship.isRepairable()) {
                addNewsResult(order, empire, "Ship " + ship + " needs no repairs.");
            } else {
                for (World world: worlds) {
                    if (!world.getOwner().equals(empire)) {
                        addNewsResult(order, empire, "You do not own world " + world);
                    } else if (world.isInterdicted()) {
                        addNewsResult(order, empire, "World " + world + " is interdicted; no repairs possible.");
                    } else if (world.isBlockaded() && !ship.isSameSector(world)) {
                        addNewsResult(order, empire, "World " + world + " is blockaded; no offworld repairs possible.");
                    } else {
                        final int dpPerRU = ship.isOrbital() ? Constants.DEFAULT_ORBITAL_REPAIR_DP_PER_RU : Constants.DEFAULT_REPAIR_DP_PER_RU;
                        final int stockpile = world.getStockpile();
                        if (stockpile >= dpPerRU) {
                            final int maxRepair = ship.getMaxRepairAmount();
                            final int paidRepairs = dpToRepair * dpPerRU;
                            final int amountToRepair = Math.min(maxRepair, paidRepairs);
                            final int fee = (int) Math.min(stockpile, Math.ceil((double)amountToRepair / dpPerRU));
                            ship.repair(amountToRepair);
                            final Collection<Empire> newEmpires = turnData.getEmpiresPresent(ship);
                            newEmpires.remove(empire);
                            final int remaining = world.adjustStockpile(-fee);
                            addNewsResult(order, "You repaired " + amountToRepair + " DP on ship " + ship
                                            + " (" + fee + " RU fee; " + remaining + " remaining)");
                            addNewsResult(order, newEmpires, empire + " repaired " + amountToRepair + " DP on ship " + ship);
                        } else {
                            addNewsResult(order, empire, "Insufficient stockpile (" + stockpile
                                    + ") on world " + world + "; no repairs possible");
                        }
                    }
                }
            }
        });
    }
}