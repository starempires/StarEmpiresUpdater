package com.starempires.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.starempires.TurnData;
import com.starempires.objects.Coordinate;
import com.starempires.objects.Empire;
import com.starempires.objects.EmpireType;
import com.starempires.objects.FrameOfReference;
import com.starempires.objects.HullParameters;
import com.starempires.objects.Portal;
import com.starempires.objects.Ship;
import com.starempires.objects.ShipClass;
import com.starempires.objects.Storm;
import com.starempires.objects.World;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.List;

public class BaseTest {

    protected static final ObjectMapper MAPPER = new ObjectMapper();
    protected static final String RESOURCE_DIR = "src/test/resources";
    protected static final String HULL_PARAMETERS_FILE = "test/test.hull-parameters.json";
    protected static final String SHIP_CLASS_FILE = "test/test.ship-classes.json";
    protected static final Coordinate ZERO_COORDINATE = new Coordinate(0, 0);
    protected static final Coordinate ONE_COORDINATE = new Coordinate(1, 1);

    protected static List<HullParameters> hullParameters;
    protected static List<ShipClass> shipClasses;

    protected TurnData turnData;
    protected ShipClass probeClass;
    protected ShipClass carrierClass;
    protected ShipClass starbaseClass;
    protected ShipClass missileClass;
    protected ShipClass fighterClass;
    protected ShipClass frigateClass;
    protected ShipClass hammerClass;
    protected ShipClass shieldClass;
    protected Empire empire1;
    protected Empire empire2;
    protected Empire gm;
    protected World world;
    protected Portal portal;
    protected Storm storm;

    @BeforeAll
    public static void beforeAllBaseTest() throws Exception {
        // create HullParameters
        final String hullJson = Files.readString(FileSystems.getDefault().getPath(RESOURCE_DIR, HULL_PARAMETERS_FILE));
        hullParameters = MAPPER.readValue(hullJson, new TypeReference<List<HullParameters>>() {
        });

        final String shipClassJson = Files.readString(FileSystems.getDefault().getPath(RESOURCE_DIR, SHIP_CLASS_FILE));
        shipClasses = MAPPER.readValue(shipClassJson, new TypeReference<List<ShipClass>>() {
        });
    }

    @BeforeEach
    public void beforeBaseTest() {
        turnData = TurnData
                .builder()
                .turnNumber(1)
                .radius(5)
                .session("test")
                .build();
        turnData.addHullParameters(hullParameters);
        turnData.addShipClasses(shipClasses);
        gm = turnData.addGMEmpire();
        carrierClass = turnData.getShipClass("carrier");
        probeClass = turnData.getShipClass("probe");
        starbaseClass = turnData.getShipClass("starbase");
        missileClass = turnData.getShipClass("nuke");
        fighterClass = turnData.getShipClass("fighter");
        frigateClass = turnData.getShipClass("frigate");
        hammerClass = turnData.getShipClass("hammer");
        shieldClass = turnData.getShipClass("shield");
        empire1 = createEmpire("empire1");
        empire2 = createEmpire("empire2");
        world = createWorld("world", ZERO_COORDINATE, 12);
        world.setOwner(empire1);
        world.setStockpile(12);
        empire1.addKnownWorld(world);
        portal = createPortal("portal", ZERO_COORDINATE, false);
        storm = createStorm("storm", ZERO_COORDINATE, 1);
        turnData.addStorm(storm);
    }

    protected Ship createShip(final ShipClass shipClass, final Coordinate coordinate, final String name, final Empire owner) {
        final Ship ship = Ship
                .builder()
                .shipClass(shipClass)
                .dpRemaining(shipClass.getDp())
                .coordinate(coordinate)
                .name(name)
                .serialNumber(name)
                .owner(owner)
                .build();
        owner.addShip(ship);
        return ship;
    }

    protected Empire createEmpire(final String name) {
        final Empire empire = Empire.builder().name(name).empireType(EmpireType.ACTIVE)
                .frameOfReference(FrameOfReference.DEFAULT_FRAME_OF_REFERENCE)
                .abbreviation(name.substring(0, 2))
                .build();
        turnData.addEmpire(empire);
        return empire;
    }

    protected World createWorld(final String name, final Coordinate coordinate, final int production) {
        final World world = World.builder()
                .production(production)
                .name(name)
                .coordinate(coordinate)
                .build();
        turnData.addWorld(world);
        return world;
    }

    protected Portal createPortal(final String name, final Coordinate coordinate, final boolean collapsed) {
        final Portal portal = Portal.builder()
                .name(name)
                .coordinate(coordinate)
                .collapsed(collapsed)
                .build();
        turnData.addPortal(portal);
        return portal;
    }

    protected Storm createStorm(final String name, final Coordinate coordinate, final int rating) {
        final Storm storm = Storm.builder()
                .name(name)
                .coordinate(coordinate)
                .rating(rating)
                .build();
        turnData.addStorm(storm);
        return storm;
    }
}