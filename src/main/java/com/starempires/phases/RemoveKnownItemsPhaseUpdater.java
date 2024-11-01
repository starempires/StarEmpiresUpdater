package com.starempires.phases;

import com.starempires.TurnData;

public class RemoveKnownItemsPhaseUpdater extends PhaseUpdater {

    public RemoveKnownItemsPhaseUpdater(final TurnData turnData) {
        super(Phase.REMOVE_KNOWN_ITEMS, turnData);
    }

    @Override
    public void update() {
        // TurnData turnData = getTurnData();
        // List<Order> orders = turnData.getOrders(OrderType.REMOVEKNOWN);
        // if (orders != null) {
        // for (Order order: orders) {
        // Parameters parameters = order.getParameters();
        // String objectType = parameters.get(0);
        // String name = parameters.get(1);
        // List<String> empireNames = parameters.subList(2);
        // if (objectType.equalsIgnoreCase(Constants.TOKEN_CLASS)) {
        // removeKnownShipClass(order, name, empireNames);
        // }
        // else if (objectType.equalsIgnoreCase(Constants.TOKEN_EMPIRE)) {
        // removeKnownEmpires(order, name, empireNames);
        // }
        // else if (objectType.equalsIgnoreCase(Constants.TOKEN_PORTAL)) {
        // removeKnownPortals(order, name, empireNames);
        // }
        // else if (objectType.equalsIgnoreCase(Constants.TOKEN_WORLD)) {
        // removeKnownWorlds(order, name, empireNames);
        // }
        //
        // }
        // }
    }

    // private void removeKnownWorlds(Order order, String name, List<String> empireNames) {
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
    // empire.removeKnownWorld(world.getId());
    // addNewsResult(order, order.getEmpireId(), "World " + world + " is no longer known to empire " + empire);
    // }
    // }
    // }
    // }
    //
    // private void removeKnownPortals(Order order, String name, List<String> empireNames) {
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
    // empire.removeKnownPortal(portal.getId());
    // addNewsResult(order, order.getEmpireId(), "Portal " + portal + " is no longer known to empire " + empire);
    // }
    // }
    // }
    // }
    //
    // private void removeKnownEmpires(Order order, String name, List<String> empireNames) {
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
    // empire.removeKnownEmpire(foreignEmpire.getId());
    // addNewsResult(order, order.getEmpireId(), "Empire " + foreignEmpire + " is no longer known to empire " + empire);
    // }
    // }
    // }
    // }
    //
    // private void removeKnownShipClass(Order order, String name, List<String> empireNames) {
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
    // empire.removeKnownShipClass(shipClass.getId());
    // addNewsResult(order, order.getEmpireId(), "Ship class " + shipClass + " is now known to empire " + empire);
    // }
    // }
    // }
    // }

}