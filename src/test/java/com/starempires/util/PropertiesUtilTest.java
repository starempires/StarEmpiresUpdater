package com.starempires.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PropertiesUtilTest {

    private static final List<String> PROPS = List.of("int=3", "boolean=true", "string=foo");
    private PropertiesUtil propertiesUtil;

    @BeforeEach
    void setUp() throws IOException {
        propertiesUtil = new PropertiesUtil(PROPS);
    }

    @Test
    void testInts() {
        assertEquals(3, propertiesUtil.getInt("int"));
        assertEquals(0, propertiesUtil.getInt("unknown"));
    }

    @Test
    void testStrings() {
        assertEquals("foo", propertiesUtil.getString("string"));
        assertNull(propertiesUtil.getString("unknown"));
    }

    @Test
    void testBooleans() {
        assertTrue(propertiesUtil.getBoolean("boolean"));
        assertFalse(propertiesUtil.getBoolean("unknown"));
    }

    @Test
    void testBadData() throws IOException {
        final List<String> badData = List.of("int");
        propertiesUtil = new PropertiesUtil(badData);
        assertEquals(0, propertiesUtil.getInt("int"));
    }
}