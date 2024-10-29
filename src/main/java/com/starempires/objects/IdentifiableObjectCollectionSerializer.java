package com.starempires.objects;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.util.Collection;

public class IdentifiableObjectCollectionSerializer extends JsonSerializer<Collection<? extends IdentifiableObject>> {
    @Override
    public void serialize(Collection<? extends IdentifiableObject> objects, JsonGenerator gen, SerializerProvider serializers) throws IOException, IOException {
        gen.writeStartArray();
        for (IdentifiableObject object : objects) {
            gen.writeString(object.getName());
        }
        gen.writeEndArray();
    }

    @Override
    public boolean isEmpty(SerializerProvider provider, Collection<? extends IdentifiableObject> value) {
        return (value == null || value.isEmpty());
    }
}
