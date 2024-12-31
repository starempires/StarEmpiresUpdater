package com.starempires.updater;

import com.starempires.TurnData;

/**
 parameters are
 MOVE [WORLD|PORTAL|STORM] object TO coordinate ....
 MOVE SHIP empire ship1 TO coordinate
 */
public class MoveMapObjectsPhaseUpdater extends PhaseUpdater {

    public MoveMapObjectsPhaseUpdater(final TurnData turnData) {
        super(Phase.MAP_MOVE_OBJECTS, turnData);
    }

    // private void movePortal(final Order order, final String name, final Coordinate toCoordinate) {
    // final Portal portal = turnData.getPortal(name);
    // final Empire empire = order.getEmpire();
    // if (portal == null) {
    // addNewsResult(order, empire, "No portal named " + name + " exists.");
    // }
    // else {
    // final Coordinate fromCoordinate = portal.getCoordinate();
    // if (fromCoordinate.equals(toCoordinate)) {
    // addNewsResult(order, empire, "Portal " + name + " is already in sector " + toCoordinate);
    // }
    // else {
    // Collection<Empire> empires = turnData.getEmpiresPresent(portal);
    // addNewsResult(order, empires, "Portal " + portal + " has been removed from sector " + fromCoordinate);
    //
    // turnData.movePortal(portal, toCoordinate);
    //
    // empires = turnData.getEmpiresPresent(portal);
    // addNewsResult(order, empires, "Portal " + portal + " has been added to sector " + toCoordinate);
    // }
    // }
    // }
    //
    // private void moveShips(Order order, String empireName, List<String> handles, Coordinate coordinate) {
    // TurnData turnData = getTurnData();
    // int empireId = order.getEmpireId();
    // Empire empire = turnData.getEmpire(empireName);
    // if (empire == null) {
    // addNewsResult(order, empireId, "Empire " + empireName + " does not exist.");
    // }
    // else {
    // List<Ship> ships = turnData.getShips(empire.getId(), handles);
    // if (ships != null) {
    // for (Ship ship : ships) {
    // Collection<Integer> empires = new HashSet<Integer>();
    // if (ship.isLoaded()) {
    // empires.add(ship.getEmpireId());
    // int carrierId = ship.getCarrierId();
    // Ship carrier = turnData.getShip(carrierId);
    // empires.add(carrier.getEmpireId());
    // turnData.unload(ship);
    // }
    // else {
    // empires.addAll(turnData.getEmpires(ship));
    // }
    // empires.add(empireId);
    // addNewsResult(order, empires,
    // "Ship " + ship + "has been removed from sector " + ship.getCoordinate());
    //
    // turnData.moveShip(ship, coordinate);
    //
    // empires = turnData.getEmpires(ship);
    // empires.add(empireId);
    // addNewsResult(order, empires, "Ship " + ship + " has been added to sector " + coordinate);
    // }
    // }
    // }
    // }
    //
    // private void moveStorm(Order order, String name, Coordinate toCoordinate) {
    // TurnData turnData = getTurnData();
    // int empireId = order.getEmpireId();
    // Storm storm = turnData.getStorm(name);
    // if (storm == null) {
    // addNewsResult(order, empireId, "No storm " + name + " exists.");
    // }
    // else {
    // Coordinate fromCoordinate = storm.getCoordinate();
    // if (fromCoordinate.equals(toCoordinate)) {
    // addNewsResult(order, empireId, "Storm " + name + " is already in sector " + toCoordinate);
    // }
    // else {
    // Collection<Integer> empires = turnData.getEmpires(fromCoordinate);
    // empires.add(empireId);
    // addNewsResult(order, empires, "Storm " + storm + " has been removed from sector " + fromCoordinate);
    //
    // turnData.moveStorm(storm, toCoordinate);
    //
    // empires = turnData.getEmpires(toCoordinate);
    // empires.add(empireId);
    // addNewsResult(order, empires, "Storm " + storm + " has been added to sector " + toCoordinate);
    // }
    // }
    // }
    //
    // private void moveWorld(Order order, String worldName, Coordinate toCoordinate) {
    // TurnData turnData = getTurnData();
    // int empireId = order.getEmpireId();
    // World world = turnData.getWorld(worldName);
    // if (world == null) {
    // addNewsResult(order, empireId, "No world named " + worldName + " exists.");
    // }
    // else {
    // Coordinate fromCoordinate = world.getCoordinate();
    // if (fromCoordinate.equals(toCoordinate)) {
    // addNewsResult(order, empireId, "World " + worldName + " is already in sector " + toCoordinate);
    // }
    // else {
    // Collection<Integer> empires = turnData.getEmpires(world);
    // empires.add(empireId);
    // addNewsResult(order, empires, "World " + world + " has been removed from sector " + fromCoordinate);
    //
    // turnData.moveWorld(world, toCoordinate);
    //
    // empires = turnData.getEmpires(world);
    // empires.add(empireId);
    // addNewsResult(order, empires, "World " + world + " has been added to sector " + toCoordinate);
    // }
    // }
    // }

    @Override
    public void update() {
        // final List<Order> orders = turnData.getOrders(OrderType.MAPMOVE);
        // orders.stream().forEach(order -> {
        // final Parameters parameters = order.getParameters();
        // final String objectType = parameters.get(0);
        // String name = parameters.get(1);
        // Coordinate coordinate;
        // switch (objectType) {
        // case Constants.TOKEN_WORLD:
        // coordinate = Coordinate.parse(parameters.get(2));
        // moveWorld(order, name, coordinate);
        // break;
        // case Constants.TOKEN_PORTAL:
        // name = parameters.get(1);
        // coordinate = Coordinate.parse(parameters.get(2));
        // movePortal(order, name, coordinate);
        // break;
        // case Constants.TOKEN_STORM:
        // name = parameters.get(1);
        // coordinate = Coordinate.parse(parameters.get(2));
        // moveStorm(order, name, coordinate);
        // break;
        // case Constants.TOKEN_SHIP:
        // name = parameters.get(1);
        // final int num = parameters.size();
        // final List<String> handles = parameters.subList(1, num - 1);
        // coordinate = Coordinate.parse(parameters.get(num - 1));
        // moveShips(order, name, handles, coordinate);
        // break;
        // default:
        // addNewsResult(order, order.getEmpire(), "Unknown map object type: " + objectType);
        // break;
        // }
        // });
    }
}