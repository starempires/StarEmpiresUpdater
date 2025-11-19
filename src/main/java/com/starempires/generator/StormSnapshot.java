package com.starempires.generator;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.starempires.objects.Storm;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@JsonInclude(Include.ALWAYS)
@SuperBuilder
@Getter
@JsonPropertyOrder(alphabetic = true)
public class StormSnapshot extends IdentifiableObjectSnapshot {

    final int intensity;

    public static StormSnapshot fromStorm(final Storm storm) {
        return StormSnapshot.builder()
                .name(storm.getName())
                .intensity(storm.getIntensity())
                .build();
    }
}