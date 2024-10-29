package com.starempires.objects;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.google.common.collect.Multimap;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CoordinateMultimapSerializer extends JsonSerializer<Multimap<Empire, ? extends Coordinate>> {
    @Override
    public void serialize(Multimap<Empire, ? extends Coordinate> objects, JsonGenerator gen, SerializerProvider serializers) throws IOException, IOException {
        final Map<Empire, List<? extends Coordinate>> map = objects.asMap().entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> List.copyOf(entry.getValue()) // Convert Collection to List
                ));
        gen.writeObject(map);
    }

    @Override
    public boolean isEmpty(SerializerProvider provider, Multimap<Empire, ? extends Coordinate> value) {
        return (value == null || value.isEmpty());
    }
}
