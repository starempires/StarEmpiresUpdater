package com.starempires.objects;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.util.Collection;

public class IdentifiableObjectSerializer extends JsonSerializer<IdentifiableObject> {
    @Override
    public void serialize(IdentifiableObject object, JsonGenerator gen, SerializerProvider serializers) throws IOException, IOException {
         gen.writeString(object.getName());
    }
}
