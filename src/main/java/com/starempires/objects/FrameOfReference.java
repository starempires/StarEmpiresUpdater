package com.starempires.objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class FrameOfReference {

    public static final FrameOfReference DEFAULT_FRAME_OF_REFERENCE = new FrameOfReference(0, 0,
            HexDirection.DIRECTION_0, false, false);

    /** offset from galactic (0,0) */
    @JsonIgnore
    private final Coordinate offset;
    /** rotation from galactic "north" in increments of 60 degrees (one hex side) */
    private final HexDirection rotation;
    /** horizontal flip from galactic orientation */
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private final boolean horizontalMirror;
    /** vertical flip from galactic orientation */
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private final boolean verticalMirror;

    @Builder
    @JsonCreator
    private FrameOfReference(
            @JsonProperty("obliqueOffset") final int obliqueOffset,
            @JsonProperty("yOffset") final int yOffset,
            @JsonProperty("hexDirection") final HexDirection rotation,
            @JsonProperty("horizontalMirror") final boolean horizontalMirror,
            @JsonProperty("verticalMirror") final boolean verticalMirror) {
        this.offset = new Coordinate(obliqueOffset, yOffset);
        this.rotation = rotation;
        this.horizontalMirror = horizontalMirror;
        this.verticalMirror = verticalMirror;
    }

    public Coordinate toLocal(final Coordinate galactic) {
        Coordinate local = new Coordinate(galactic);
        local = local.translate(offset).rotate(rotation);
        if (verticalMirror) {
            local = local.verticalMirror();
        }
        if (horizontalMirror) {
            local = local.horizontalMirror();
        }
        return local;
    }

    public Coordinate toGalactic(final Coordinate local) {
        Coordinate galactic = new Coordinate(local);
        if (horizontalMirror) {
            galactic = galactic.horizontalMirror();
        }

        if (verticalMirror) {
            galactic = galactic.verticalMirror();
        }
        galactic = galactic.rotate(HexDirection.opposite(rotation)).translate(Coordinate.negate(offset));
        return galactic;
    }

    @JsonProperty("obliqueOffset")
    public int getObliqueOffset() {
        return offset.getOblique();
    }

    @JsonProperty("yOffset")
    public int getYOffset() {
        return offset.getY();
    }
}