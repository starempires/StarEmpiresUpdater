package com.starempires.creator;

import com.starempires.objects.Coordinate;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class EmpireCreation {
    private final Coordinate center;
    private final String starbaseName;
    private final String homeworldName;
}