package com.starempires.phases;

import com.starempires.TurnData;

public class ModifyMapObjectsPhaseUpdater extends PhaseUpdater {

    public ModifyMapObjectsPhaseUpdater(final TurnData turnData) {
        super(Phase.MAP_MODIFY_OBJECTS, turnData);
    }

    // private void addPortalConnections(Order order, String portalName, List<String> toPortalNames) {
    // TurnData turnData = getTurnData();
    // int empireId = order.getEmpireId();
    // Portal fromPortal = turnData.getPortal(portalName);
    // if (fromPortal == null) {
    // addNewsResult(order, empireId, "Portal " + portalName + " does not exist.");
    // }
    // else {
    // for (String toPortalName: toPortalNames) {
    // Portal toPortal = turnData.getPortal(toPortalName);
    // if (toPortal == null) {
    // addNewsResult(order, empireId, "Portal " + portalName + " does not exist.");
    // }
    // else {
    // turnData.addPortalConnection(fromPortal, toPortal);
    // addNewsResult(order, empireId, "Added connection from portal " + fromPortal + " to " + toPortal);
    // }
    // }
    // }
    // }
    //
    // private void removePortalConnections(Order order, String portalName, List<String> toPortalNames) {
    // TurnData turnData = getTurnData();
    // int empireId = order.getEmpireId();
    // Portal fromPortal = turnData.getPortal(portalName);
    // if (fromPortal == null) {
    // addNewsResult(order, empireId, "Portal " + portalName + " does not exist.");
    // }
    // else {
    // for (String toPortalName: toPortalNames) {
    // Portal toPortal = turnData.getPortal(toPortalName);
    // if (toPortal == null) {
    // addNewsResult(order, empireId, "Portal " + portalName + " does not exist.");
    // }
    // else {
    // turnData.removePortalConnection(fromPortal, toPortal);
    // addNewsResult(order, empireId, "Removed connection from portal " + fromPortal + " to " + toPortal);
    // }
    // }
    // }
    // }
    //
    // private void modifyShipDpRemaining(Order order, String empireName, String handle, int dpRemaining) {
    // TurnData turnData = getTurnData();
    // int empireId = order.getEmpireId();
    // Empire empire = turnData.getEmpire(empireName);
    // if (empire == null) {
    // addNewsResult(order, empireId, "Empire " + empireName + " does not exist.");
    // }
    // else {
    // Ship ship = turnData.getShip(empire.getId(), handle);
    // if (ship == null) {
    // addNewsResult(order, empireId, "Ship " + empireName + ":" + handle + " does not exist.");
    // }
    // else {
    // Collection<Integer> empires = new HashSet<Integer>();
    // if (ship.isLoaded()) {
    // empires.add(ship.getEmpireId());
    // int carrierId = ship.getCarrierId();
    // Ship carrier = turnData.getShip(carrierId);
    // empires.add(carrier.getEmpireId());
    // }
    // else {
    // empires.addAll(turnData.getEmpires(ship));
    // }
    // empires.add(empireId);
    // ship.setDpRemaining(dpRemaining);
    // addNewsResult(order, empires, "Ship " + handle + " dp remaining has been adjusted.");
    // }
    // }
    // }
    //
    // private void modifyShipName(Order order, String empireName, String handle, String newHandle) {
    // TurnData turnData = getTurnData();
    // int empireId = order.getEmpireId();
    // Empire empire = turnData.getEmpire(empireName);
    // if (empire == null) {
    // addNewsResult(order, empireId, "Empire " + empireName + " does not exist.");
    // }
    // else {
    // Ship ship = turnData.getShip(empire.getId(), handle);
    // if (ship == null) {
    // addNewsResult(order, empireId, "Ship " + empireName + ":" + handle + " does not exist.");
    // }
    // else {
    // Collection<Integer> empires = new HashSet<Integer>();
    // if (ship.isLoaded()) {
    // empires.add(ship.getEmpireId());
    // int carrierId = ship.getCarrierId();
    // Ship carrier = turnData.getShip(carrierId);
    // empires.add(carrier.getEmpireId());
    // }
    // else {
    // empires.addAll(turnData.getEmpires(ship));
    // }
    // empires.add(empireId);
    // addNewsResult(order, empires, "Ship " + ship + " has been renamed to " + handle);
    // turnData.renameShip(ship, newHandle);
    // }
    // }
    // }
    //
    // private void modifyPortalName(Order order, String portalName, String newName) {
    // TurnData turnData = getTurnData();
    // int empireId = order.getEmpireId();
    // Portal portal = turnData.getPortal(portalName);
    // if (portal == null) {
    // addNewsResult(order, empireId, "Portal " + portalName + " does not exist.");
    // }
    // else {
    // Collection<Integer> empires = turnData.getEmpires(portal);
    // empires.add(empireId);
    // addNewsResult(order, empires, "Portal " + portal + " has been renamed to " + newName);
    // turnData.renamePortal(portal, newName);
    // }
    // }
    //
    // private void modifyWorldName(Order order, String worldName, String newName) {
    // TurnData turnData = getTurnData();
    // int empireId = order.getEmpireId();
    // World world = turnData.getWorld(worldName);
    // if (world == null) {
    // addNewsResult(order, empireId, "World " + worldName + " does not exist.");
    // }
    // else {
    // Collection<Integer> empires = turnData.getEmpires(world);
    // empires.add(empireId);
    // addNewsResult(order, empires, "World " + world + " has been renamed to " + newName);
    // turnData.renameWorld(world, newName);
    // }
    // }
    //
    // private void modifyShipOwner(Order order, String empireName, String handle, String newEmpireName) {
    // TurnData turnData = getTurnData();
    // int empireId = order.getEmpireId();
    // Empire empire = turnData.getEmpire(empireName);
    // if (empire == null) {
    // addNewsResult(order, empireId, "Empire " + empireName + " does not exist.");
    // }
    // else {
    // Ship ship = turnData.getShip(empire.getId(), handle);
    // if (ship == null) {
    // addNewsResult(order, empireId, "Ship " + empireName + ":" + handle + " does not exist.");
    // }
    // else {
    // Empire newEmpire = turnData.getEmpire(newEmpireName);
    // if (newEmpire == null) {
    // addNewsResult(order, empireId, "Empire " + newEmpireName + " does not exist.");
    // }
    // else {
    // Collection<Integer> empires = new HashSet<Integer>();
    // if (ship.isLoaded()) {
    // empires.add(ship.getEmpireId());
    // int carrierId = ship.getCarrierId();
    // Ship carrier = turnData.getShip(carrierId);
    // empires.add(carrier.getEmpireId());
    // }
    // else {
    // empires.addAll(turnData.getEmpires(ship));
    // }
    // empires.add(empireId);
    // empires.add(newEmpire.getId());
    // addNewsResult(order, empires, "Ship " + ship + " has been transfered to owner " + newEmpire);
    // turnData.transferShip(ship, newEmpire);
    // }
    // }
    // }
    // }
    //
    // private void modifyWorldOwner(Order order, String worldName, String empireName) {
    // TurnData turnData = getTurnData();
    // int empireId = order.getEmpireId();
    // World world = turnData.getWorld(worldName);
    // if (world == null) {
    // addNewsResult(order, empireId, "World " + worldName + " does not exist.");
    // }
    // else {
    // Empire empire = turnData.getEmpire(empireName);
    // if (empire == null) {
    // addNewsResult(order, empireId, "Empire " + empireName + " does not exist.");
    // }
    // else {
    // Collection<Integer> empires = turnData.getEmpires(world);
    // empires.add(empireId);
    // addNewsResult(order, empires, "World " + world + " has new owner " + empire);
    // world.setEmpireId(empire.getId());
    // }
    // }
    // }
    //
    // private void modifyWorldProduction(Order order, String worldName, int production) {
    // TurnData turnData = getTurnData();
    // int empireId = order.getEmpireId();
    // World world = turnData.getWorld(worldName);
    // if (world == null) {
    // addNewsResult(order, empireId, "World " + worldName + " does not exist.");
    // }
    // else {
    // Collection<Integer> empires = turnData.getEmpires(world);
    // empires.add(empireId);
    // addNewsResult(order, empires, "World " + world + " has new production value " + production);
    // world.setProduction(production);
    // }
    // }
    //
    // private void modifyStormRating(Order order, String stormName, int rating) {
    // TurnData turnData = getTurnData();
    // int empireId = order.getEmpireId();
    // Storm storm = turnData.getStorm(stormName);
    // if (storm == null) {
    // addNewsResult(order, empireId, "Storm " + stormName + " not found");
    // }
    // else {
    // storm.setRating(rating);
    // Collection<Integer> empires = turnData.getEmpires(storm);
    // empires.add(empireId);
    // addNewsResult(order, empires, "Storm " + stormName + " now has rating " + rating);
    // }
    // }
    //
    // private void modifyObjectName(Order order, String objectType, String objectName, String newName) {
    // if (objectType.equalsIgnoreCase(Constants.TOKEN_PORTAL)) {
    // modifyPortalName(order, objectName, newName);
    // }
    // else if (objectType.equalsIgnoreCase(Constants.TOKEN_WORLD)) {
    // modifyWorldName(order, objectName, newName);
    // }
    // }
    //
    // private void modifyObjectDrift(Order order, String objectType, String objectName, List<String> directionIds) {
    // List<DriftDirection> directions = new ArrayList<DriftDirection>();
    // for (String directionId: directionIds) {
    // int id = Integer.parseInt(directionId);
    // DriftDirection direction = DriftDirection.getDriftDirection(id);
    // directions.add(direction);
    // }
    // TurnData turnData = getTurnData();
    // MappableObject object = null;
    // if (objectType.equalsIgnoreCase(Constants.TOKEN_WORLD)) {
    // object = turnData.getWorld(objectName);
    // }
    // else if (objectType.equalsIgnoreCase(Constants.TOKEN_PORTAL)) {
    // object = turnData.getPortal(objectName);
    // }
    // else if (objectType.equalsIgnoreCase(Constants.TOKEN_STORM)) {
    // object = turnData.getStorm(objectName);
    // }
    //
    // if (object == null) {
    // addNewsResult(order, order.getEmpireId(), objectType + " " + objectName + " does not exist.");
    // }
    // else {
    // object.setDrift(directions);
    // addNewsResult(order, order.getEmpireId(), "Modified drift pattern for " + objectType + " " + object);
    // }
    // }
    //
    // private void modifyStormFluctuations(Order order, String stormName, List<String> ratings) {
    // TurnData turnData = getTurnData();
    // Storm storm = turnData.getStorm(stormName);
    // if (storm == null) {
    // addNewsResult(order, order.getEmpireId(), "Storm " + stormName + " does not exist");
    // }
    // else if (ratings != null) {
    // List<Integer> fluctuations = new ArrayList<Integer>();
    // for (String rating: ratings) {
    // int fluctuation = Integer.parseInt(rating);
    // fluctuations.add(fluctuation);
    // }
    // storm.setFluctuations(fluctuations);
    // addNewsResult(order, order.getEmpireId(), "Modified fluctuation pattern for storm " + storm);
    // }
    // }

    @Override
    public void update() {
        // TurnData turnData = getTurnData();
        // List<Order> orders = turnData.getOrders(OrderType.MAPMODIFY);
        // if (orders != null) {
        // for (Order order: orders) {
        // Parameters parameters = order.getParameters();
        // String modification = parameters.get(0);
        // if (modification.equalsIgnoreCase(Constants.TOKEN_CONNECT)) {
        // String portalName = parameters.get(1);
        // List<String> toPortals = parameters.subList(2);
        // addPortalConnections(order, portalName, toPortals);
        // }
        // else if (modification.equalsIgnoreCase(Constants.TOKEN_DISCONNECT)) {
        // String portalName = parameters.get(1);
        // List<String> toPortals = parameters.subList(2);
        // removePortalConnections(order, portalName, toPortals);
        // }
        // else if (modification.equalsIgnoreCase(Constants.TOKEN_DP)) {
        // String empireName = parameters.get(1);
        // String handle = parameters.get(2);
        // int dpRemaining = parameters.getInt(3);
        // modifyShipDpRemaining(order, empireName, handle, dpRemaining);
        // }
        // else if (modification.equalsIgnoreCase(Constants.TOKEN_DRIFT)) {
        // String objectType = parameters.get(1);
        // String objectName = parameters.get(2);
        // List<String> directionIds = parameters.subList(3);
        // modifyObjectDrift(order, objectType, objectName, directionIds);
        // }
        // else if (modification.equalsIgnoreCase(Constants.TOKEN_FLUCTUATE)) {
        // String stormName = parameters.get(1);
        // List<String> ratings = parameters.subList(2);
        // modifyStormFluctuations(order, stormName, ratings);
        // }
        // else if (modification.equalsIgnoreCase(Constants.TOKEN_NAME)) {
        // String objectType = parameters.get(1);
        // if (objectType.equalsIgnoreCase(Constants.TOKEN_SHIP)) {
        // String empireName = parameters.get(2);
        // String handle = parameters.get(3);
        // String newHandle = parameters.get(4);
        // modifyShipName(order, empireName, handle, newHandle);
        // }
        // else {
        // String objectName = parameters.get(2);
        // String newName = parameters.get(3);
        // modifyObjectName(order, objectType, objectName, newName);
        // }
        // }
        // else if (modification.equalsIgnoreCase(Constants.TOKEN_OWNER)) {
        // String objectType = parameters.get(1);
        // if (objectType.equalsIgnoreCase(Constants.TOKEN_SHIP)) {
        // String empireName = parameters.get(2);
        // String handle = parameters.get(3);
        // String newEmpireName = parameters.get(4);
        // modifyShipOwner(order, empireName, handle, newEmpireName);
        // }
        // else if (objectType.equalsIgnoreCase(Constants.TOKEN_WORLD)) {
        // String worldName = parameters.get(2);
        // String empireName = parameters.get(3);
        // modifyWorldOwner(order, worldName, empireName);
        // }
        // }
        // else if (modification.equalsIgnoreCase(Constants.TOKEN_PRODUCTION)) {
        // String worldName = parameters.get(1);
        // int production = parameters.getInt(2);
        // modifyWorldProduction(order, worldName, production);
        // }
        // else if (modification.equalsIgnoreCase(Constants.TOKEN_RATING)) {
        // String stormName = parameters.get(1);
        // int rating = parameters.getInt(2);
        // modifyStormRating(order, stormName, rating);
        // }
        // else {
        // addNewsResult(order, order.getEmpireId(), "Unknown modification type: " + modification);
        // }
        // }
        // }
    }

}