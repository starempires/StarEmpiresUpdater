package com.starempires.objects;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.google.common.collect.Sets;

import java.io.IOException;
import java.util.Set;

public class DeferredIdentifiableObjectCollectionDeserializer extends JsonDeserializer<Set<? extends IdentifiableObject>> {
    @Override
    public Set<IdentifiableObject> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException, IOException {
        return Sets.newHashSet();
    }
}
