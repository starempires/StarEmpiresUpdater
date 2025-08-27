package com.starempires.objects;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Represents an identifiable entity with a name
 * 
 * @author john
 *
 */
@RequiredArgsConstructor
@Data
@NoArgsConstructor(force = true)
@SuperBuilder
public abstract class IdentifiableObject implements Comparable<IdentifiableObject> {

    public static final IdentifiableObjectComparator IDENTIFIABLE_NAME_COMPARATOR = new IdentifiableObjectComparator();

    public static class IdentifiableObjectComparator implements Comparator<IdentifiableObject> {
        @Override
        public int compare(@NonNull final IdentifiableObject o1, @NonNull final IdentifiableObject o2) {
            return StringUtils.compareIgnoreCase(o1.getName(), o2.getName());
        }
    }

    /**
     * Serialize IdentifiableObject as just its name
     */
    public static class IdentifiableObjectSerializer extends JsonSerializer<IdentifiableObject> {
        @Override
        public void serialize(IdentifiableObject object, JsonGenerator gen, SerializerProvider serializers) throws IOException, IOException {
            gen.writeString(object.getName());
        }
    }
    /**
     * Serialize Collection of IdentifiableObject as a collection of their names
     */
    public static class IdentifiableObjectCollectionSerializer extends JsonSerializer<Collection<? extends IdentifiableObject>> {
        @Override
        public void serialize(Collection<? extends IdentifiableObject> objects, JsonGenerator gen, SerializerProvider serializers) throws IOException, IOException {
            final List<String> names = objects.stream().map(IdentifiableObject::getName).sorted().toList();
            gen.writeStartArray();
            for (String name : names) {
                gen.writeString(name);
            }
            gen.writeEndArray();
        }

        @Override
        public boolean isEmpty(SerializerProvider provider, Collection<? extends IdentifiableObject> value) {
            return (value == null || value.isEmpty());
        }
    }

    /**
     * Serialize Multimap of IdentifiableObject as a collection of their names
     */
    public static class IdentifiableObjectMultimapSerializer extends JsonSerializer<Multimap<Empire, ? extends IdentifiableObject>> {
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

    /**
     * Deserialize IdentifiableObject as null, which will be replaced later
     */
    public static class DeferredIdentifiableObjectDeserializer extends JsonDeserializer<IdentifiableObject> {
        @Override
        public IdentifiableObject deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException, IOException {
            return null;
        }
    }

    /**
     * Deserialize Array of IdentifiableObject as empty Set, which will be replaced later
     */
    public static class DeferredIdentifiableObjectCollectionDeserializer extends JsonDeserializer<Set<? extends IdentifiableObject>> {
        @Override
        public Set<? extends IdentifiableObject> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException, IOException {
            JsonToken token = p.nextToken();
            while (token != JsonToken.END_ARRAY) {
                token = p.nextToken();
            }
            return Sets.newHashSet();
        }
    }

    /**
     * Deserialize Array of IdentifiableObject as empty Multimap, which will be replaced later
     */
    public static class DeferredIdentifiableObjectMultimapDeserializer extends JsonDeserializer<Multimap<Empire, ? extends IdentifiableObject>> {
        @Override
        public Multimap<Empire, IdentifiableObject> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException, IOException {
            JsonToken token = p.nextToken();
            while (token != JsonToken.END_ARRAY) {
                token = p.nextToken();
            }
            return HashMultimap.create();
        }
    }

    protected final String name;

    @Override
    public String toString() {
        return name;
    }

    @Override
    public int compareTo(final @NonNull IdentifiableObject obj) {
        return StringUtils.compareIgnoreCase(name, obj.name);
    }
}