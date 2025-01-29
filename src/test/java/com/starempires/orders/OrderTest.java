package com.starempires.orders;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.starempires.objects.Coordinate;
import com.starempires.objects.World;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Mock
    private Function<String, World> mockLookupWorld;

    @Mock
    private JsonNode node;
    @Mock
    private JsonNode valueNode;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getNodeNull() {
        when(node.get("fieldName")).thenReturn(null);
        assertNull(Order.getString(node, "fieldName"));
        assertEquals(0, Order.getInt(node, "fieldName"));
        assertFalse(Order.getBoolean(node, "fieldName"));
        assertTrue(Order.getStringList(node, "fieldName").isEmpty());
    }

    @Test
    void getString() {
        when(node.get("fieldName")).thenReturn(valueNode);
        when(valueNode.asText()).thenReturn("text");
        assertEquals("text", Order.getString(node, "fieldName"));
    }

    @Test
    void getInt() {
        when(node.get("fieldName")).thenReturn(valueNode);
        when(valueNode.asInt()).thenReturn(3);
        assertEquals(3, Order.getInt(node, "fieldName"));
    }

    @Test
    void getBoolean() {
        when(node.get("fieldName")).thenReturn(valueNode);
        when(valueNode.asBoolean()).thenReturn(true);
        assertTrue(Order.getBoolean(node, "fieldName"));
    }

    @Test
    void testGetStringList_WithValidArray() throws Exception {
        // Arrange
        String json = "{ \"myField\": [\"value1\", \"value2\", \"value3\"] }";
        JsonNode node = MAPPER.readTree(json);

        // Act
        List<String> result = Order.getStringList(node, "myField");

        // Assert
        assertEquals(List.of("value1", "value2", "value3"), result);
    }

    @Test
    void testGetStringList_WithNonArrayField() throws Exception {
        // Arrange
        String json = "{ \"myField\": \"value1\" }";
        JsonNode node = MAPPER.readTree(json);

        // Act
        List<String> result = Order.getStringList(node, "myField");

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetStringList_WithEmptyArray() throws Exception {
        // Arrange
        String json = "{ \"myField\": [] }";
        JsonNode node = MAPPER.readTree(json);

        // Act
        List<String> result = Order.getStringList(node, "myField");

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetStringList_WithNullField() throws Exception {
        // Arrange
        String json = "{ \"myField\": null }";
        JsonNode node = MAPPER.readTree(json);

        // Act
        List<String> result = Order.getStringList(node, "myField");

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetTurnDataListFromJsonNode_WithValidNames() throws Exception {
        // Arrange
        final JsonNode node = MAPPER.readTree("[\"object1\"]");
        final World world = World.builder().build();
        when(mockLookupWorld.apply("object1")).thenReturn(world);

        final List<World> result = Order.getTurnDataListFromJsonNode(node, mockLookupWorld);
        assertEquals(List.of(world), result);
        verify(mockLookupWorld, times(1)).apply(anyString());
    }

    @Test
    void testGetTurnDataListFromJsonNode_WithUnknownObjects() throws Exception {
        final JsonNode node = MAPPER.readTree("[\"object1\"]");
        when(mockLookupWorld.apply("object1")).thenReturn(null);

        final List<World> result = Order.getTurnDataListFromJsonNode(node, mockLookupWorld);
        assertTrue(result.isEmpty());
    }

    @Test
    void getTurnDataItemFromJsonNode() throws JsonProcessingException {
        final JsonNode node = MAPPER.readTree("\"object1\"");
        final World world = World.builder().build();
        when(mockLookupWorld.apply("object1")).thenReturn(world);
        assertEquals(world, Order.getTurnDataItemFromJsonNode(node, mockLookupWorld));
    }

    @Test
    void getTurnDataItemFromJsonNodeNull() throws JsonProcessingException {
        assertNull(Order.getTurnDataItemFromJsonNode(null, mockLookupWorld));
    }

    @Test
    void getCoordinateFromJsonNode() throws JsonProcessingException {
        final JsonNode node = MAPPER.readTree( "{ \"oblique\": 1, \"y\": 2}");
        final Coordinate coordinate = Order.getCoordinateFromJsonNode(node);
        assertEquals(1, coordinate.getOblique());
        assertEquals(2, coordinate.getY());
    }

    @Test
    void getCoordinateFromJsonNodeNull() throws JsonProcessingException {
        assertNull(Order.getCoordinateFromJsonNode(null));
    }

    @Test
    void parseReady() {
    }
}