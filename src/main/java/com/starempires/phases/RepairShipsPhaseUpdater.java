package com.starempires.phases;

import com.starempires.TurnData;
import com.starempires.objects.Empire;
import com.starempires.orders.Order;
import com.starempires.orders.OrderType;
import com.starempires.objects.Ship;
import com.starempires.objects.World;

import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.starempires.objects.IdentifiableObject.IDENTIFIABLE_NAME_COMPARATOR;

public class RepairShipsPhaseUpdater extends PhaseUpdater {

    final static private float REPAIR_DP_PER_RU = 2.0f;
    final static private float ORBITAL_REPAIR_DP_PER_RU = 3.0f;

    // repair ship world amount
    final static private String AMOUNT_GROUP = "amount";
    final static private String WORLD_GROUP = "world";
    final static private String SHIP_GROUP = "ship";
    final static private String REPAIR_REGEX = "^repair\\s+(?<" + SHIP_GROUP + ">\\w+)\\s+(?<" + WORLD_GROUP + ">\\w+)\\s+(?<" + AMOUNT_GROUP + ">\\d+)\\s*$";

    final static private Pattern REPAIR_PATTERN = Pattern.compile(REPAIR_REGEX, Pattern.CASE_INSENSITIVE);

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
        orders.forEach(order -> {
            final Matcher matcher = REPAIR_PATTERN.matcher(order.getParametersAsString());
            if (matcher.matches()) {
                final Empire empire = order.getEmpire();
                final String shipName = matcher.group(SHIP_GROUP);
                final String worldName = matcher.group(WORLD_GROUP);
                final int amount = Integer.parseInt(matcher.group(AMOUNT_GROUP));
                final Ship ship = turnData.getShip(shipName);
                if (ship == null || !ship.getOwner().equals(empire)) {
                    addNewsResult(order, empire, "You do not own ship " + shipName);
                } else if (!ship.isAlive()) {
                    addNewsResult(order, empire, "Ship " + ship + " has been destroyed.");
                } else if (ship.isRepairable()) {
                    addNewsResult(order, empire, "Ship " + ship + " needs no repairs.");
                } else {
                    final World world = turnData.getWorld(worldName);
                    if (world == null || !world.getOwner().equals(empire)) {
                        addNewsResult(order, empire, "You do not own world " + worldName);
                    } else if (world.isInterdicted()) {
                        addNewsResult(order, empire, "World " + world + " is interdicted; no repairs possible.");
                    } else if (world.isBlockaded() && !ship.isSameSector(world)) {
                        addNewsResult(order, empire, "World " + world + " is blockaded; no offworld repairs possible.");
                    } else {
                        final double multiplier = ship.isOrbital() ? ORBITAL_REPAIR_DP_PER_RU : REPAIR_DP_PER_RU;
                        final int stockpile = world.getStockpile();
                        if (stockpile > multiplier) {
                            final int maxRepair = ship.getMaxRepairAmount();
                            final int paidRepairs = (int) Math.round(amount * multiplier);
                            final int amountToRepair = Math.min(maxRepair, paidRepairs);
                            final int fee = (int) Math.min(stockpile, Math.ceil(amountToRepair / multiplier));
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