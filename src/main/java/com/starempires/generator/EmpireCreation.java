package com.starempires.generator;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class EmpireCreation {
    private final String starbaseName;
    private final String homeworldName;
}