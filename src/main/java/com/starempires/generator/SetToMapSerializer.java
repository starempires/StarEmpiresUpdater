package com.starempires.generator;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.starempires.objects.Coordinate;

import java.io.IOException;
import java.util.Set;

public class SetToMapSerializer<T extends Coordinate> extends JsonSerializer<Set<T>> {
    @Override
    public void serialize(Set<T> set, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeStartObject();
        for (T item : set) {
            gen.writeObjectField(item.getName(), item); // Use a key derived from item (e.g., toString())
        }
        gen.writeEndObject();
    }
}