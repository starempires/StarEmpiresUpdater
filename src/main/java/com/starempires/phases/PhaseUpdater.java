package com.starempires.phases;

import com.google.common.collect.Lists;
import com.starempires.TurnData;
import com.starempires.constants.Constants;
import com.starempires.objects.Coordinate;
import com.starempires.objects.Empire;
import com.starempires.objects.MappableObject;
import com.starempires.objects.Order;
import com.starempires.objects.Ship;
import com.starempires.objects.ShipClass;
import lombok.Getter;
import org.apache.commons.lang3.ObjectUtils;

import java.util.Collection;
import java.util.List;

@Getter
public abstract class PhaseUpdater {

    protected static final String ALL_SHIPS_TOKEN = "all";

    static enum MapObject {
        PORTAL,
        SHIP,
        STORM,
        WORLD;
    }

    protected final Phase phase;
    protected final TurnData turnData;

    public PhaseUpdater(final Phase phase, final TurnData turnData) { //}, final PropertiesUtil properties) {
        this.phase = phase;
        this.turnData = turnData;
    }

    public abstract void update();

    public void preUpdate() {
        turnData.addNewsHeader(phase);
    }

    public void postUpdate() {
        turnData.addNewsFooter(phase);
    }

    protected void addNewsResult(final Order order, final String text) {
        turnData.addNews(phase, order.getEmpire(), text);
    }

    protected void addNewsResult(final Order order, final Empire empire, final String text) {
        turnData.addNews(phase, empire, text);
        if (order != null) {
            order.addResult(text);
        }
    }

    protected void addNewsResult(final Order order, final Collection<Empire> empires, final String text) {
        turnData.addNews(phase, empires, text);
        if (order != null) {
            order.addResult(text);
        }
    }

    protected void addNews(final Empire empire, final String text) {
        addNewsResult(null, empire, text);
    }

    protected void addNews(final Collection<Empire> empires, final String text) {
        addNewsResult(null, empires, text);
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
                        addNewsResult(order, "You have no information about ship class " + shipClassName);
                    }
                    else {
                        final Collection<Ship> shipsOfClass = empire.getShips(shipClass).stream().filter(Ship::isAlive).toList();
                        validShips.addAll(shipsOfClass);
                    }
                }
                else {
                    final Ship ship = empire.getShip(shipHandle);
                    if (ship == null) {
                        addNewsResult(order, "You do not own ship " + shipHandle);
                    } else if (!ship.isAlive()) {
                        addNewsResult(order, "Ship " + ship + " is destroyed");
                    } else {
                        validShips.add(ship);
                    }
                }
            });
        }
        return validShips;
    }
}