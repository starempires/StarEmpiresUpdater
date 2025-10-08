package com.starempires.updater;

import com.starempires.TurnData;
import com.starempires.constants.Constants;
import com.starempires.objects.Empire;
import com.starempires.objects.Ship;
import com.starempires.objects.ShipCondition;
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
                cargo.repair(maxRepair, ShipCondition.REPAIRED);
                addNews(orbital.getOwner(), "Orbital " + orbital + " repaired " + maxRepair + " DP on cargo " + cargo);
            });
        });
    }

    @Override
    public void update() {
        repairOrbitalCargo();
        final List<Order> orders = turnData.getOrders(OrderType.REPAIR);
        orders.forEach(o -> {
            final RepairOrder order = (RepairOrder) o;
            final Empire empire = order.getEmpire();
            final Ship ship = order.getShips().get(0);
            final List<World> worlds = order.getWorlds();
            if (!ship.isAlive()) {
                addNewsResult(order, "Ship " + ship + " has been destroyed");
            } else if (!ship.isRepairable()) {
                addNewsResult(order, "Ship " + ship + " needs no repairs");
            } else {
                for (World world: worlds) {
                    if (!world.isOwnedBy(empire)) {
                        addNewsResult(order, "You do not own world " + world);
                    } else if (world.isInterdicted()) {
                        addNewsResult(order, "World " + world + " is interdicted; no repairs possible");
                    } else if (world.isBlockaded() && !ship.isSameSector(world)) {
                        addNewsResult(order, "World " + world + " is blockaded; no offworld repairs possible");
                    } else if (world.getStockpile() < 1) {
                        addNewsResult(order, "No RUs stockpiled at world " + world);
                    } else if (world.isBlockaded() && !ship.isSameSector(world)) {
                        addNewsResult(order, "World " + world + " is blockaded; no offworld repairs possible");
                    } else {
                        int dpToRepair = order.getDpToRepair();
                        final int maxRepair = ship.getMaxRepairAmount();
                        if (maxRepair < dpToRepair) {
                            addNewsResult(order, "Ship %s needs only %d DP repaired".formatted(ship, maxRepair));
                            dpToRepair = maxRepair;
                        }
                        final int dpRepairedPerRU = ship.isOrbital() ? Constants.DEFAULT_ORBITAL_REPAIR_DP_PER_RU : Constants.DEFAULT_REPAIR_DP_PER_RU;
                        final int stockpile = world.getStockpile();
                        int fee = (int)Math.ceil((double)dpToRepair / dpRepairedPerRU);
                        if (stockpile < fee) {
                            fee = stockpile;
                            dpToRepair = fee * dpRepairedPerRU;
                            addNewsResult(order, "World %s can fund only %d repairs".formatted(world, dpToRepair));
                        }
                        ship.repair(dpToRepair, ShipCondition.REPAIRED);
                        final int remaining = world.adjustStockpile(-fee);
                        final Collection<Empire> newEmpires = turnData.getEmpiresPresent(ship);
                        newEmpires.remove(empire);
                        addNewsResult(order, "World %s repaired %d DP on ship %s (%d RU spent; %d remaining)".formatted(world, dpToRepair, ship, fee, remaining));
                        addNewsResult(order, newEmpires, "%s repaired %d DP on ship %s".formatted(empire, dpToRepair, ship));
                    }
                }
            }
        });
    }
}