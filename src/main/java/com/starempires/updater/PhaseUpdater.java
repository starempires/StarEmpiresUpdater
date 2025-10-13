package com.starempires.updater;

import com.google.common.collect.Lists;
import com.starempires.TurnData;
import com.starempires.constants.Constants;
import com.starempires.objects.Coordinate;
import com.starempires.objects.Empire;
import com.starempires.objects.MappableObject;
import com.starempires.orders.Order;
import com.starempires.objects.Ship;
import com.starempires.objects.ShipClass;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.ObjectUtils;

import java.util.Collection;
import java.util.List;

@Getter
@Log4j2
public abstract class PhaseUpdater {

    protected static final String ALL_SHIPS_TOKEN = "all";

    protected final Phase phase;
    protected final TurnData turnData;

    public PhaseUpdater(final Phase phase, final TurnData turnData) { //}, final PropertiesUtil properties) {
        this.phase = phase;
        this.turnData = turnData;
    }

    public abstract void update();

    public void preUpdate() {
        log.debug("Running update for phase {}", phase);
        turnData.addNewsHeader(phase);
    }

    public void postUpdate() {
        turnData.addNewsFooter(phase);
    }

    protected void addOrderText(final Order order) {
        turnData.addNews(phase, order.toString());
    }

    protected void addNews(final Order order, final String text) {
        addNews(order.getEmpire(), text);
    }

    protected void addNews(final Empire empire, final String text) {
        turnData.addNews(phase, empire, text);
    }

    protected void addNews(final Collection<Empire> empires, final String text) {
        turnData.addNews(phase, empires, text);
    }

    protected static String plural(final int number, final String noun) {
        return plural(number, noun, Constants.SUFFIX_S);
    }

    protected static String plural(final int number, final String noun, final String suffix) {
        String rv = number + " " + noun;
        if (number != 1) {
            rv += suffix;
        }
        return rv;
    }

    protected Coordinate getCoordinateFromLocation(final String location) {
        final MappableObject mapObject = ObjectUtils.firstNonNull(turnData.getWorld(location),turnData.getPortal(location),
                turnData.getStorm(location));
        if (mapObject == null) {
            return null;
        }
        else {
            return mapObject.getCoordinate();
        }
    }

    protected List<Ship> getShipsByHandle(final Order order, final List<String> shipHandles) {
        final Empire empire = order.getEmpire();
        final List<Ship> validShips = Lists.newArrayList();
        if (shipHandles.contains(ALL_SHIPS_TOKEN)) {
            validShips.addAll(empire.getLiveShips());
        }
        else {
            shipHandles.forEach(shipHandle -> {
                if (shipHandle.startsWith("@")) { //ship class
                    final String shipClassName = shipHandle.substring(1);
                    final ShipClass shipClass = turnData.getShipClass(shipClassName);
                    if (shipClass == null || !empire.isKnownShipClass(shipClass)) {
                        addNews(order, "You have no information about ship class " + shipClassName);
                    }
                    else {
                        final Collection<Ship> shipsOfClass = empire.getShips(shipClass).stream().filter(Ship::isAlive).toList();
                        validShips.addAll(shipsOfClass);
                    }
                }
                else {
                    final Ship ship = empire.getShip(shipHandle);
                    if (ship == null) {
                        addNews(order, "You do not own ship " + shipHandle);
                    } else if (!ship.isAlive()) {
                        addNews(order, "Ship " + ship + " is destroyed");
                    } else {
                        validShips.add(ship);
                    }
                }
            });
        }
        return validShips;
    }

    String formatOpRating(final Ship ship) {
        return "%.1f%%".formatted(Math.round(ship.getOperationRating() * 1000f) / 10f);
    }
}