package com.starempires.creator;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class EmpireCreation {
    private final String starbaseName;
    private final String homeworldName;
}