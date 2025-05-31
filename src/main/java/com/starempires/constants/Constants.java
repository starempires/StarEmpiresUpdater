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

    public static final String DEFAULT_PROPERTIES_FILENAME = "starempires.properties";
    public static final String COMMAND_LINE_OPTION_TURN = "turn";
    public static final String COMMAND_LINE_OPTION_PROPERTIES = "properties";
    public static final String COMMAND_LINE_OPTION_SESSION = "session";
    public static final String COMMAND_LINE_OPTION_EMPIRE = "empire";

    /* tokens used for parsing orders */
    public static final String TOKEN_AT = "at";
    public static final String TOKEN_TO = "to";
    public static final String TOKEN_CARGO = "cargo";
    public static final String TOKEN_PORTAL = "portal";
    public static final String TOKEN_ANY = "any";
    public static final String TOKEN_RANDOM = "random";
    public static final String TOKEN_MISSILE = "missile";
    public static final String TOKEN_FROM = "from";
    public static final String TOKEN_EXCEPT = "except";
    public static final String TOKEN_SECTOR = "sector";
    public static final String TOKEN_SHIP = "ship";

    public static final String TOKEN_PRODUCTION = "production";
    public static final String TOKEN_OWNER = "owner";
    public static final String TOKEN_CONNECT = "connect";
    public static final String TOKEN_DISCONNECT = "disconnect";
    public static final String TOKEN_DP = "dp";
    public static final String TOKEN_NAME = "name";
    public static final String TOKEN_RATING = "rating";
    public static final String TOKEN_NEXT_UNIQUE_ID = "*";
    public static final String TOKEN_DRIFT = "drift";
    public static final String TOKEN_FLUCTUATE = "fluctuate";
    public static final String TOKEN_ALLDATA = "alldata";
    public static final String TOKEN_CLASS = "class";
    public static final String TOKEN_EMPIRE = "empire";
    public static final String TOKEN_PUBLIC = "public";
    public static final String TOKEN_ASCENDING = "ascending";
    public static final String TOKEN_DESCENDING = "decending";
    public static final String TOKEN_MANUAL = "manual";

    /* miscellaneous constants */
    public static final String DASHES = "========================================";
    public static final String EMPIRE_GM = "GM";
    public static final String FORMAT_SERIAL_NUMBER = "%05x";
    public static final String FORMAT_UNIQUE_ID = "%03x";
    public static final String REGEX_CAPTURE_DIGITS = "(\\d+)";
    public static final String SUFFIX_S = "s";
    public static final String SUFFIX_ES = "es";

    public static final int NUM_SECTOR_EDGES = 6;
    public static final int HEX_SIDE_LENGTH = 30;
    public static final int SERIAL_NUMBER_HEX_DIGITS = 5;

    public static final String SESSION_SQL_FILE = "session.sql";

    public static final String ARG_SESSION_NAME = "session";
    public static final String ARG_TURN_NUMBER = "turn";
    public static final String ARG_PROPERTIES_FILE = "properties";
    public static final String ARG_SESSION_CONFIG_FILE = "config";
    public static final String ARG_DATA_DIR = "datadir";
    public static final String ARG_EMPIRE_DATA = "empiredata";
    public static final String ARG_SHIP_CLASSES = "shipclasses";
    public static final String ARG_OUTPUT_DIR = "outdir";
    public static final String ARG_HTML_TEMPLATE_FILE = "htmltemplate";

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

    public static final String FONT_NAME = "Arial";
    public static final int FONT_SIZE = 10;
    public static final int IMAGE_WORLD_RADIUS = HEX_SIDE_LENGTH / 2;
    public static final int IMAGE_PORTAL_RADIUS = HEX_SIDE_LENGTH / 2;
    public static final int IMAGE_COLLAPSED_PORTAL_RADIUS = HEX_SIDE_LENGTH / 2;
    public static final float IMAGE_PORTAL_TWIST = 0.9f;
    public static final int IMAGE_STORM_SPIKE_LENGTH = 7;
    public static final int IMAGE_PORTAL_LINE_DASH_LENGTH = 10;
    public static final int IMAGE_SHIP_DOT_SIZE = 4;
    public static final int IMAGE_MAX_SHIP_DOTS = 6;
    public static final int IMAGE_SHIP_DOTS_VERTICAL_OFFSET = 9;
    public static final int IMAGE_UNKNOWN_SHIP_DOT_RADIUS = 3;
    public static final int IMAGE_HOMEWORLD_INDICATOR_SIZE = 3;

}