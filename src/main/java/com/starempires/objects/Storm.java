package com.starempires.objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Lists;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.Validate;

import java.util.List;

@Getter
@Setter
public class Storm extends MappableObject {

    /** current storm intesity */
    private int intensity;
    /** intesity fluctuations for this storm */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private final List<Integer> fluctuations = Lists.newArrayList();

    @Builder
    private Storm(final String name, final Coordinate coordinate, final int intensity) {
        super(name, coordinate);
        this.intensity = intensity;
    }

    @JsonCreator
    private Storm(@JsonProperty("name") final String name,
                  @JsonProperty("oblique") final int oblique,
                  @JsonProperty("y") final int y,
                  @JsonProperty("intensity") final int intensity) {
        super(name, new Coordinate(oblique, y));
        this.intensity = intensity;
    }

    public int getFluctuation(final int num) {
        if (fluctuations.isEmpty()) {
            return 0;
        }
        return fluctuations.get(num % fluctuations.size());
    }

    public void addFluctuation(final int step, final int fluctuation) {
        Validate.isTrue(step < fluctuations.size(), String.format("Step %d exceeeds fluctuation list size", step));
        fluctuations.set(step, fluctuation);
    }

    @Override
    public String toString() {
        return super.toString() + " (intesity " + intensity + ")";
    }
}