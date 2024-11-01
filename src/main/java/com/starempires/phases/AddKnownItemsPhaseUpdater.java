package com.starempires.phases;

import com.starempires.TurnData;

public class AddKnownItemsPhaseUpdater extends PhaseUpdater {

    public AddKnownItemsPhaseUpdater(final TurnData turnData) {
        super(Phase.ADD_KNOWN_ITEMS, turnData);
    }

    @Override
    public void update() {
        // TurnData turnData = getTurnData();
        // List<Order> orders = turnData.getOrders(OrderType.ADDKNOWN);
        // if (orders != null) {
        // for (Order order: orders) {
        // Parameters parameters = order.getParameters();
        // String objectType = parameters.get(0);
        // String name = parameters.get(1);
        //
        // if (objectType.equalsIgnoreCase(Constants.TOKEN_CLASS)) {
        // List<String> empireNames = parameters.subList(2);
        // addKnownShipClass(order, name, empireNames);
        // }
        // else if (objectType.equalsIgnoreCase(Constants.TOKEN_EMPIRE)) {
        // List<String> empireNames = parameters.subList(2);
        // addKnownEmpires(order, name, empireNames);
        // }
        // else if (objectType.equalsIgnoreCase(Constants.TOKEN_PORTAL)) {
        // int lastTurnScanned = parameters.getInt(2);
        // boolean hasNavData = parameters.getBoolean(3);
        // List<String> empireNames = parameters.subList(4);
        // addKnownPortals(order, name, lastTurnScanned, hasNavData, empireNames);
        // }
        // else if (objectType.equalsIgnoreCase(Constants.TOKEN_WORLD)) {
        // int lastTurnScanned = parameters.getInt(2);
        // List<String> empireNames = parameters.subList(3);
        // addKnownWorlds(order, name, lastTurnScanned, empireNames);
        // }
        //
        // }
        // }
    }

    // private void addKnownWorlds(Order order, String name, int lastTurnScanned, List<String> empireNames) {
    // TurnData turnData = getTurnData();
    // World world = turnData.getWorld(name);
    // if (world == null) {
    // addNewsResult(order, order.getEmpireId(), "No world " + name + " exists.");
    // }
    // else {
    // for (String empireName: empireNames) {
    // Empire empire = turnData.getEmpire(empireName);
    // if (empire == null) {
    // addNewsResult(order, order.getEmpireId(), "No empire " + name + " exists.");
    // }
    // else {
    // empire.addKnownWorld(world.getStatus());
    // addNewsResult(order, order.getEmpireId(), "World " + world + " is now known to empire " + empire);
    // }
    // }
    // }
    // }
    //
    // private void addKnownPortals(Order order, String name, int lastTurnScanned, boolean hasNavData, List<String> empireNames) {
    // TurnData turnData = getTurnData();
    // Portal portal = turnData.getPortal(name);
    // if (portal == null) {
    // addNewsResult(order, order.getEmpireId(), "No portal " + name + " exists.");
    // }
    // else {
    // for (String empireName: empireNames) {
    // Empire empire = turnData.getEmpire(empireName);
    // if (empire == null) {
    // addNewsResult(order, order.getEmpireId(), "No empire " + name + " exists.");
    // }
    // else {
    // empire.addKnownPortal(portal.getStatus(), hasNavData);
    // addNewsResult(order, order.getEmpireId(), "Portal " + portal + " is now known to empire " + empire);
    // }
    // }
    // }
    // }
    //
    // private void addKnownEmpires(Order order, String name, List<String> empireNames) {
    // TurnData turnData = getTurnData();
    // Empire foreignEmpire = turnData.getEmpire(name);
    // if (foreignEmpire == null) {
    // addNewsResult(order, order.getEmpireId(), "No empire " + name + " exists.");
    // }
    // else {
    // for (String empireName: empireNames) {
    // Empire empire = turnData.getEmpire(empireName);
    // if (empire == null) {
    // addNewsResult(order, order.getEmpireId(), "No empire " + name + " exists.");
    // }
    // else {
    // empire.addKnownEmpire(foreignEmpire.getId());
    // addNewsResult(order, order.getEmpireId(), "Empire " + foreignEmpire + " is now known to empire " + empire);
    // }
    // }
    // }
    // }
    //
    // private void addKnownShipClass(Order order, String name, List<String> empireNames) {
    // TurnData turnData = getTurnData();
    // ShipClass shipClass = turnData.getShipClass(name);
    // if (shipClass == null) {
    // addNewsResult(order, order.getEmpireId(), "No ship class " + name + " exists.");
    // }
    // else {
    // for (String empireName: empireNames) {
    // Empire empire = turnData.getEmpire(empireName);
    // if (empire == null) {
    // addNewsResult(order, order.getEmpireId(), "No empire " + name + " exists.");
    // }
    // else {
    // empire.addKnownShipClass(shipClass.getId());
    // addNewsResult(order, order.getEmpireId(), "Ship class " + shipClass + " is now known to empire " + empire);
    // }
    // }
    // }
    // }
}