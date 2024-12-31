package com.starempires.generator;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@JsonInclude(Include.NON_DEFAULT)
@SuperBuilder
public abstract class IdentifiableObjectSnapshot {

    protected final String name;
}