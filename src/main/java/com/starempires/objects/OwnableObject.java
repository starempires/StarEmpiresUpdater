package com.starempires.objects;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public abstract class OwnableObject extends MappableObject {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonSerialize(using = IdentifiableObjectSerializer.class)
    @JsonDeserialize(using = DeferredIdentifiableObjectDeserializer.class)
    protected Empire owner;

    protected OwnableObject(final String name, final Coordinate coordinate, final Empire owner) {
        super(name, coordinate);
        this.owner = owner;
    }

    public boolean hasSameOwner(final OwnableObject object) {
        return isOwnedBy(object.owner);
    }

    public boolean isOwnedBy(final Empire empire) {
        return Objects.equals(owner, empire);
    }
}