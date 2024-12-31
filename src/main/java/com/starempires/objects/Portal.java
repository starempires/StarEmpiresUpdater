package com.starempires.objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.collect.Sets;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

@Getter
@Setter
public class Portal extends MappableObject {
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private boolean collapsed;
    @JsonSerialize(using = IdentifiableObject.IdentifiableObjectCollectionSerializer.class)
    @JsonDeserialize(using = IdentifiableObject.DeferredIdentifiableObjectCollectionDeserializer.class)
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private final Set<Portal> connections = Sets.newHashSet(); // exit connections

    @Builder
    private Portal(final String name, final Coordinate coordinate, final boolean collapsed) {
        super(name, coordinate);
        this.collapsed = collapsed;
    }

    @JsonCreator
    private Portal(@JsonProperty("name") final String name,
                   @JsonProperty("oblique") final int oblique,
                   @JsonProperty("y") final int y,
                   @JsonProperty("collapsed") final boolean collapsed) {
        this(name, new Coordinate(oblique, y), collapsed);
    }

    public void addConnection(final Portal portal) {
        connections.add(portal);
    }

    public void removeConnection(final Portal portal) {
        connections.remove(portal);
    }

    public boolean isConnectedTo(final Portal portal) {
        return connections.contains(portal);
    }

    public Portal selectRandomConnection() {
        if (connections.isEmpty()) {
            return null;
        }
        else {
            final List<Portal> availableConnections = connections.stream()
                    .filter(connection -> !connection.isCollapsed())
                    .toList();
            final int num = ThreadLocalRandom.current().nextInt(0, availableConnections.size());
            return availableConnections.get(num);
        }
    }
}