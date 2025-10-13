package com.starempires.constants;

public class Constants {

    /* session parameters */
    public static final String PARAMETER_BLOCKADE_THRESHOLD = "blockadeThreshold";
    public static final String PARAMETER_DESIGN_MULTIPLIER = "designMutliplier";
    public static final String PARAMETER_GENERATED_STORM_RATING = "generatedStormRating";
    public static final String PARAMETER_INTERDICTION_THRESHOLD = "interdictionThreshold";
    public static final String PARAMETER_MISSILE_TONNAGE_COST = "missileTonnageCost";
    public static final String PARAMETER_ORBITAL_REPAIR_DP_PER_RU = "orbitalRepairDpPerRu";
    public static final String PARAMETER_PRODUCTION_MULTIPLIER = "productionMultiplier";
    public static final String PARAMETER_REPAIR_DP_PER_RU = "repairDpPerRu";
    public static final String PARAMETER_SALVAGE_THRESHOLD = "salvageThreshold";
    public static final String PARAMETER_SELF_DESTRUCT_TONNAGE_INTERVAL = "selfDestructTonnageInterval";

    /* default values for session parameters */
    public static final float DEFAULT_DESIGN_MULTIPLIER = 0.5f;
    public static final float DEFAULT_AUTO_REPAIR_MULTIPLIER = 0.1f;
    public static final float DEFAULT_INTERDICTION_THRESHOLD = 3.0f;
    public static final int DEFAULT_ORBITAL_REPAIR_DP_PER_RU = 3;
    public static final float DEFAULT_PRODUCTION_MULTIPLIER = 1.0f;
    public static final int DEFAULT_REPAIR_DP_PER_RU = 2;
    public static final float DEFAULT_SALVAGE_THRESHOLD = 3.f;
    public static final int DEFAULT_SELF_DESTRUCT_TONNAGE_INTERVAL = 5;

    /* tokens used for parsing orders */
    public static final String TOKEN_PORTAL = "portal";
    public static final String TOKEN_SHIP = "ship";
    public static final String TOKEN_CLASS = "class";

    /* miscellaneous constants */
    public static final String FORMAT_SERIAL_NUMBER = "%05x";
    public static final String SUFFIX_S = "s";
    public static final String SUFFIX_ES = "es";

    public static final int NUM_SECTOR_EDGES = 6;
    public static final int HEX_SIDE_LENGTH = 30;
    public static final int SERIAL_NUMBER_HEX_DIGITS = 5;

    public static final String CONFIG_NUM_HOMEWORLDS = "numHomeworlds";
    public static final String CONFIG_HOMEWORLD_PRODUCTION = "homeworldProduction";
    public static final String CONFIG_HOMEWORLD_NEARBY_PRODUCTION = "homeworldNearbyProduction";
    public static final String CONFIG_HOMEWORLD_NUM_NEARBY_WORLDS = "homeworldNumNearbyWorlds";
    public static final String CONFIG_HOMEWORLD_NEARBY_RADIUS = "homeworldNearbyRadius";
    public static final String CONFIG_MAX_WORLD_PRODUCTION = "maxWorldProduction";
    public static final String CONFIG_NUM_WORMNETS = "numWormnets";
    public static final String CONFIG_WORLD_DENSITY = "worldDensity";
    public static final String CONFIG_STORM_DENSITY = "stormDensity";
    public static final String CONFIG_NEBULA_DENSITY = "nebulaDensity";
    public static final String CONFIG_MAX_WORMNET_PORTALS = "maxWormnetPortals";
    public static final String CONFIG_MIN_PORTAL_TO_PORTAL_DISTANCE = "minPortalToPortalDistance";
    public static final String CONFIG_MIN_PORTAL_TO_HOMEWORLD_DISTANCE = "minPortalToHomeworldDistance";
    public static final String CONFIG_MIN_NEBULA_TO_HOMEWORLD_DISTANCE = "minNebulaToHomeworldDistance";
    public static final String CONFIG_MAX_STORM_RATING = "maxStormRating";
    public static final String CONFIG_RADIUS = "radius";

    public static final int DEFAULT_NUM_HOMEWORLDS = 6;
    public static final int DEFAULT_HOMEWORLD_PRODUCTION = 12;
    public static final int DEFAULT_HOMEWORLD_NEARBY_PRODUCTION = 10;
    public static final int DEFAULT_HOMEWORLD_NUM_NEARBY_WORLDS = 2;
    public static final int DEFAULT_HOMEWORLD_NEARBY_RADIUS = 2;
    public static final int DEFAULT_MAX_WORLD_PRODUCTION = 9;
    public static final int DEFAULT_NUM_WORMNETS = 3;
    public static final int DEFAULT_MAX_WORMNET_PORTALS = 4;
    public static final int DEFAULT_MIN_PORTAL_TO_PORTAL_DISTANCE = 8;
    public static final int DEFAULT_MIN_PORTAL_TO_HOMEWORLD_DISTANCE = 3;
    public static final int DEFAULT_MIN_NEBULA_TO_HOMEWORLD_DISTANCE = 2;
    public static final int DEFAULT_MAX_STORM_RATING = 10;
    public static final int DEFAULT_GALAXY_RADIUS = 10;

    public static final float DEFAULT_WORLD_DENSITY = 0.1f;
    public static final float DEFAULT_STORM_DENSITY = 0.05f;
    public static final float DEFAULT_NEBULA_DENSITY = 0.05f;
}