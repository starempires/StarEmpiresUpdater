package com.starempires.objects;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.google.common.collect.Multimap;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class IdentifiableObjectMultimapSerializer extends JsonSerializer<Multimap<Empire, ? extends IdentifiableObject>> {
    @Override
    public void serialize(Multimap<Empire, ? extends IdentifiableObject> objects, JsonGenerator gen, SerializerProvider serializers) throws IOException, IOException {
        final Map<Empire, List<String>> map = objects.asMap().entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().stream()   // List of ShipClass names as JSON value
                                .map(IdentifiableObject::getName)
                                .collect(Collectors.toList())
                ));
        gen.writeObject(map);
    }

    @Override
    public boolean isEmpty(SerializerProvider provider, Multimap<Empire, ? extends  IdentifiableObject> value) {
        return (value == null || value.isEmpty());
    }
}
