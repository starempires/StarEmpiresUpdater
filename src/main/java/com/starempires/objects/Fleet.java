package com.starempires.objects;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

public class Fleet {

    private final Multimap<ShipClass, Ship> shipsByClass = HashMultimap.create();
    private final Multimap<Coordinate, Ship> shipsByCoordinate = HashMultimap.create();
    private final Map<String, Ship> shipsByName = Maps.newHashMap();
    private final Map<String, Ship> shipsBySerialNumber = Maps.newHashMap();

    public void addShip(final Ship ship) {
        shipsByName.put(ship.getHandle(), ship);
        shipsBySerialNumber.put(ship.getSerialNumber(), ship);
        shipsByClass.put(ship.getShipClass(), ship);
        shipsByCoordinate.put(ship.getCoordinate(), ship);
    }

    public Ship getShipByHandle(final String handle) {
        return shipsByName.getOrDefault(handle, shipsBySerialNumber.get(handle));
    }

    public Collection<Ship> getShipsByClass(final ShipClass shipClass) {
        return shipsByClass.get(shipClass);
    }

    public Collection<Ship> getShipsByCoordinate(final Coordinate coordinate) {
        return shipsByCoordinate.get(coordinate);
    }

    public Collection<Ship> getAllShips() {
        return shipsBySerialNumber.values();
    }

    public void moveShip(final Ship ship, final Coordinate coordinate) {
        shipsByCoordinate.remove(ship.getCoordinate(), ship);
        ship.moveTo(coordinate);
        shipsByCoordinate.put(coordinate, ship);
    }

    public void traverseShip(final Ship ship, final Coordinate coordinate) {
        shipsByCoordinate.remove(ship.getCoordinate(), ship);
        ship.traverseTo(coordinate);
        shipsByCoordinate.put(coordinate, ship);
    }

    public void moveShips(final @NotNull Set<Ship> ships, final Coordinate coordinate) {
        ships.forEach(ship -> moveShip(ship, coordinate));
    }

    public void traverseShips(final @NotNull Set<Ship> ships, final Coordinate coordinate) {
        ships.forEach(ship -> traverseShip(ship, coordinate));
    }

    public void removeShip(final Ship ship) {
        shipsByClass.remove(ship.getShipClass(), ship);
        shipsByCoordinate.remove(ship.getCoordinate(), ship);
        shipsByName.remove(ship.getName());
        shipsBySerialNumber.remove(ship.getSerialNumber());
    }

    public boolean serialNumberExists(final String serialNumber) {
        return shipsBySerialNumber.containsKey(serialNumber);
    }
}
