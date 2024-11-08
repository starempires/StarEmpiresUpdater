package com.starempires.phases;

import com.starempires.TurnData;
import com.starempires.constants.Constants;
import com.starempires.objects.Coordinate;
import com.starempires.objects.Empire;
import com.starempires.objects.MappableObject;
import com.starempires.objects.Order;
import lombok.Getter;
import org.apache.commons.lang3.ObjectUtils;

import java.util.Collection;

@Getter
public abstract class PhaseUpdater {

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
}