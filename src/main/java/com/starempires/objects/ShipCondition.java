package com.starempires.objects;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Getter
public enum ShipCondition {

    AUTO_REPAIRED("A"),
    BUILT("B"),
    DAMAGED_BY_STORM("D"),
    DEPLOYED("P"),
    DESTROYED_BY_STORM("Xs"),
    DESTROYED_IN_COMBAT("Xc"),
    FIRED_GUNS("F"),
    HIT_IN_COMBAT("H"),
    LOADED_CARGO("l"),
    LOADED_ONTO_CARRIER("L"),
    MOVED("M"),
    REPAIRED("R"),
    SELF_DESTRUCTED("S"),
    TOGGLED_TRANSPONDER("T"),
    TRAVERSED_WORMNET("W"),
    UNLOADED_CARGO("u"),
    UNLOADED_FROM_CARRIER("U");

    public static class ShipConditionAbbrevSerializer extends JsonSerializer<ShipCondition> {
        @Override
        public void serialize(final ShipCondition value, final JsonGenerator gen, final SerializerProvider serializers) throws IOException {
            gen.writeString(value.getAbbreviation());
        }
    }

    public static class ShipConditionAbbrevDeserializer extends JsonDeserializer<ShipCondition> {
        @Override
        public ShipCondition deserialize(final JsonParser p, final DeserializationContext ctxt) throws IOException {
            final String abbr = p.getValueAsString();
            return ShipCondition.fromAbbreviation(abbr);
        }
    }

    private static final Map<String, ShipCondition> BY_ABBR =
            Arrays.stream(values()).collect(Collectors.toUnmodifiableMap(
                    ShipCondition::getAbbreviation, Function.identity()));

    private final String abbreviation;

    @JsonValue
    @Override
    public String toString() {
        return name().toLowerCase().replace('_', ' ');
    }

    public static ShipCondition fromAbbreviation(final String abbr) {
        final ShipCondition sc = BY_ABBR.get(abbr);
        if (sc == null) {
            throw new IllegalArgumentException("Unknown ShipCondition abbr: " + abbr);
        }
        return sc;
    }
}