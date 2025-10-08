package com.starempires.updater;

import com.starempires.TurnData;

/**
 parameters are
 MOVE [WORLD|PORTAL|STORM] object TO coordinate ....
 MOVE SHIP empire ship1 TO coordinate
 */
public class AddMapObjectsPhaseUpdater extends PhaseUpdater {

    public AddMapObjectsPhaseUpdater(final TurnData turnData) {
        super(Phase.ADD_MAP_OBJECTS, turnData);
    }

    // private void addWorld(Order order, String worldName, Coordinate coordinate, int production, String empireName) {
    // TurnData turnData = getTurnData();
    // Empire empire = turnData.getEmpire(empireName);
    // int id = World.nextId();
    // World world = new World(id, worldName, coordinate);
    // world.setProduction(production);
    // world.setEmpireId(empire.getId());
    // turnData.addWorld(world);
    // Collection<Integer> empireIds = turnData.getEmpires(world);
    // empireIds.add(order.getEmpireId());
    // empireIds.add(empire.getId());
    // addNewsResult(order, empireIds, "World " + world + " has been added to sector " + coordinate);
    // }
    //
    // private void addPortal(Order order, String portalName, Coordinate coordinate) {
    // TurnData turnData = getTurnData();
    // int id = Portal.nextId();
    // Portal portal = new Portal(id, portalName, coordinate);
    // turnData.addPortal(portal);
    // Collection<Integer> empireIds = turnData.getEmpires(portal);
    // empireIds.add(order.getEmpireId());
    // addNewsResult(order, empireIds, "Portal " + portal + " has been added to sector " + coordinate);
    // }
    //
    // private void addStorm(Order order, String name, Coordinate coordinate, int rating) {
    // TurnData turnData = getTurnData();
    // int id = Storm.nextId();
    // Storm storm = new Storm(id, name, coordinate, rating);
    // turnData.addStorm(storm);
    // Collection<Integer> empireIds = turnData.getEmpires(storm);
    // empireIds.add(order.getEmpireId());
    // addNewsResult(order, empireIds, "Rating-" + rating + " storm " + name + " has been added to sector " + coordinate);
    //
    // }
    //
    // private void addShip(Order order, String empireName, String className, Coordinate coordinate, String handle) {
    // TurnData turnData = getTurnData();
    // int empireId = order.getEmpireId();
    // Empire empire = turnData.getEmpire(empireName);
    // if (empire == null) {
    // addNewsResult(order, empireId, "Empire " + empireName + " does not exist.");
    // }
    // else {
    // ShipClass shipClass = turnData.getShipClass(className);
    // if (shipClass == null) {
    // addNewsResult(order, empireId, "Ship class " + className + " does not exist.");
    // }
    // else {
    // Ship ship = turnData.addNewShip(shipClass, coordinate, empire.getId(), handle);
    // Collection<Integer> empireIds = turnData.getEmpires(ship);
    // empireIds.add(order.getEmpireId());
    // turnData.addShip(ship, shipClass.getId());
    // addNewsResult(order, empireIds, "Ship " + ship + " has been added to sector " + coordinate);
    // }
    // }
    // }
    //
    // private void addShipClass(Order order, String name, HullType hullType, Component components, boolean isPublic) {
    // TurnData turnData = getTurnData();
    // int creatorId = 0;
    // Empire empire = turnData.getEmpire(Constants.EMPIRE_GM);
    // if (empire != null) {
    // creatorId = empire.getId();
    // }
    //
    // int id = ShipClass.nextId();
    // ShipClass shipClass = new ShipClass(id, name);
    // shipClass.setComponents(components);
    // shipClass.setCreatorId(creatorId);
    // shipClass.setTurnCreated(turnData.getTurnNumber());
    // shipClass.setHullType(hullType);
    // shipClass.setPublic(isPublic);
    // turnData.addShipClass(shipClass);
    // addNewsResult(order, order.getEmpireId(), "Ship class " + shipClass + " added.");
    // }

    @Override
    public void update() {
        // TurnData turnData = getTurnData();
        // List<Order> orders = turnData.getOrders(OrderType.MAPADD);
        // if (orders != null) {
        // for (Order order: orders) {
        // Parameters parameters = order.getParameters();
        // String objectType = parameters.get(0);
        // if (objectType.equalsIgnoreCase(Constants.TOKEN_WORLD)) {
        // String worldName = parameters.get(1);
        // Coordinate coordinate = Coordinate.parse(parameters.get(2));
        // int production = parameters.getInt(3);
        // String empireName = parameters.get(4);
        // addWorld(order, worldName, coordinate, production, empireName);
        // }
        // else if (objectType.equalsIgnoreCase(Constants.TOKEN_PORTAL)) {
        // String portalName = parameters.get(1);
        // Coordinate coordinate = Coordinate.parse(parameters.get(2));
        // addPortal(order, portalName, coordinate);
        // }
        // else if (objectType.equalsIgnoreCase(Constants.TOKEN_STORM)) {
        // String name = parameters.get(1);
        // Coordinate coordinate = Coordinate.parse(parameters.get(2));
        // int rating = parameters.getInt(3);
        // addStorm(order, name, coordinate, rating);
        // }
        // else if (objectType.equalsIgnoreCase(Constants.TOKEN_CLASS)) {
        // String name = parameters.get(1);
        // String hullTypeName = parameters.get(2);
        // HullType hullType = HullType.valueOf(hullTypeName);
        // int guns = parameters.getInt(3);
        // int dp = parameters.getInt(4);
        // int engines = parameters.getInt(5);
        // int scan = parameters.getInt(6);
        // int racks = parameters.getInt(7);
        // int tonnage = parameters.getInt(8);
        // int cost = parameters.getInt(9);
        // int ar = parameters.getInt(10);
        // boolean isPublic = parameters.getBoolean(11);
        //
        // Component components = new Component(guns, dp, engines, scan, racks, tonnage, cost, ar);
        // addShipClass(order, name, hullType, components, isPublic);
        // }
        // else if (objectType.equalsIgnoreCase(Constants.TOKEN_SHIP)) {
        // String empireName = parameters.get(1);
        // String className = parameters.get(2);
        // Coordinate coordinate = Coordinate.parse(parameters.get(3));
        // String handle = parameters.get(4);
        // addShip(order, empireName, className, coordinate, handle);
        // }
        // else {
        // addNewsResult(order, order.getEmpireId(), "Unknown map object type: " + objectType);
        // }
        // }
        // }
    }
}