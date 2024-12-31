package com.starempires.generator;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.google.common.collect.Sets;
import com.starempires.objects.Empire;
import com.starempires.objects.Portal;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.util.Set;

@Getter
@SuperBuilder
@JsonInclude(Include.NON_DEFAULT)
@JsonPropertyOrder(alphabetic = true)
public class PortalSnapshot extends IdentifiableObjectSnapshot {

    private final boolean navDataKnown;
    private final boolean collapsed;
    private final Set<String> entrances;
    private final Set<String> exits;

    public static PortalSnapshot fromPortal(final Portal portal, final Empire empire) {

        if (portal == null) {
            return null;
        }

        boolean collapsed = false;
        boolean navDataKnown = false;
        final Set<String> entrances = Sets.newHashSet();
        final Set<String> exits = Sets.newHashSet();

        switch (empire.getScanStatus(portal)) {
            case VISIBLE:
                // intentional fall-through
            case SCANNED:
                collapsed = portal.isCollapsed();
                // intentional fall-through
            case STALE:
                navDataKnown = empire.hasNavData(portal);
                // intentional fall-through
            default: // unknown
        }

        portal.getConnections().stream()
                .filter(empire::hasNavData)
                .forEach(connection -> {
                    exits.add(connection.getName());
                    if (connection.isConnectedTo(portal)) {
                        entrances.add(connection.getName());
                    }
                });
        return PortalSnapshot.builder()
                .navDataKnown(navDataKnown)
                .collapsed(collapsed)
                .entrances(entrances)
                .exits(exits)
                .build();
    }
}