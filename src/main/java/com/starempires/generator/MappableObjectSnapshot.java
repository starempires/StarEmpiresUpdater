package com.starempires.generator;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.starempires.objects.Coordinate;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public abstract class MappableObjectSnapshot extends IdentifiableObjectSnapshot {

    @JsonIgnore
    protected final Coordinate coordinate;
}