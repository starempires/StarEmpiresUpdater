package com.starempires.objects;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;

public class DeferredIdentifiableObjectDeserializer extends JsonDeserializer<IdentifiableObject> {
    @Override
    public IdentifiableObject deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException, IOException {
        return null;
    }
}
