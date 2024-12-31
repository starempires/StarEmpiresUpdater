package com.starempires.generator;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@JsonInclude(Include.NON_DEFAULT)
@SuperBuilder
@Getter
public class KnownEmpireSnapshot extends IdentifiableObjectSnapshot {

}