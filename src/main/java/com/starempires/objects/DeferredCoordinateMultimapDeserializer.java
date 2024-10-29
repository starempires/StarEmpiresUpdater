package com.starempires.objects;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import java.io.IOException;

public class DeferredCoordinateMultimapDeserializer extends JsonDeserializer<Multimap<Empire, ? extends Coordinate>> {
    @Override
    public Multimap<Empire, ? extends Coordinate> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException, IOException {
        return HashMultimap.create();
    }
}
