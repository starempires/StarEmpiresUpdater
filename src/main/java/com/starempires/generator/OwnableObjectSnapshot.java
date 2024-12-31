package com.starempires.generator;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.experimental.SuperBuilder;

@JsonInclude(Include.NON_DEFAULT)
@SuperBuilder
public abstract class OwnableObjectSnapshot extends MappableObjectSnapshot {

    protected final String owner;

    @JsonInclude(Include.ALWAYS)
    public String getOwner() {
        return owner == null ? "unowned" : owner;
    }
}