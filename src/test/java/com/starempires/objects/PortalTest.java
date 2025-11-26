package com.starempires.objects;

import com.starempires.util.BaseTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PortalTest extends BaseTest {

    private Portal portal1, portal2;

    @BeforeEach
    void setUp() {
        portal1 = createPortal("portal1", ZERO_COORDINATE, false);
        portal2 = createPortal("portal2", ONE_COORDINATE, false);
        turnData.addPortal(portal1);
        turnData.addPortal(portal2);
    }

    @Test
    void testConnections() {
        portal1.addConnection(portal2);
        assertTrue(portal1.isConnectedTo(portal2));
        portal1.removeConnection(portal2);
        assertFalse(portal1.isConnectedTo(portal2));
    }

    @Test
    void selectRandomConnection() {
        assertNull(portal1.selectRandomConnection());
        portal1.addConnection(portal2);
        assertEquals(portal2, portal1.selectRandomConnection());
    }

    @Test
    void selectRandomConnectionCollapsed() {
        portal1.addConnection(portal2);
        portal2.setCollapsed(true);
        assertNull(portal1.selectRandomConnection());
    }

    @Test
    void selectRandomConnectionNoConnections() {
        assertNull(portal1.selectRandomConnection());
    }
}