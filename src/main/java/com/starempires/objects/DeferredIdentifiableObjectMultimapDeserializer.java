package com.starempires.objects;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

import java.io.IOException;
import java.util.HashMap;
import java.util.Set;

public class DeferredIdentifiableObjectMultimapDeserializer extends JsonDeserializer<Multimap<Empire, ? extends IdentifiableObject>> {
    @Override
    public Multimap<Empire, IdentifiableObject> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException, IOException {
        return HashMultimap.create();
    }
}
