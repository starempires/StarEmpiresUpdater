package com.starempires.updater;

import com.starempires.TurnData;

public class CollapsePortalsPhaseUpdater extends PhaseUpdater {

    public CollapsePortalsPhaseUpdater(final TurnData turnData) {
        super(Phase.COLLAPSE_PORTALS, turnData);
    }

    @Override
    public void update() {
        // TurnData turnData = getTurnData();
        // Map<Integer, Portal> portals = turnData.getPortals();
        // for (Portal portal : portals.values()) {
        // Collection<World> worldsPresent = turnData.getWorlds(portal);
        // Collection<Portal> portalsPresent = turnData.getPortals(portal);
        // portalsPresent.remove(portal);
        //
        // boolean collapsed = false;
        // String text = null;
        // if (!worldsPresent.isEmpty()) {
        // collapsed = true;
        // text = "in the same sector as world " + worldsPresent.iterator().next();
        // }
        // else if (!portalsPresent.isEmpty()) {
        // collapsed = true;
        // text = "in the same sector as portal " + portalsPresent.iterator().next();
        // }
        // else {
        // Ship device = turnData.getDeployedDevice(portal, DeviceType.PORTAL_HAMMER);
        // if (device != null) {
        // collapsed = true;
        // text = "from portal hammer " + device;
        // }
        // }
        //
        // if (collapsed) {
        // portal.setCollapsed(collapsed);
        // Collection<Integer> empireIds = turnData.getEmpires(portal);
        // empireIds.add(portal.getEmpireId());
        // addNews(empireIds, "Portal " + portal + " " + portal.getCoordinate() + " has collapsed " + text);
        // }
        // }
    }
}